/*
 * $Id$
 */
package zielu.svntoolbox.async;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbService.DumbModeListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.svntoolbox.FileStatus;
import zielu.svntoolbox.FileStatusCalculator;
import zielu.svntoolbox.SvnToolBoxApp;
import zielu.svntoolbox.SvnToolBoxProject;
import zielu.svntoolbox.projectView.ProjectViewManager;
import zielu.svntoolbox.projectView.ProjectViewStatus;
import zielu.svntoolbox.projectView.ProjectViewStatusCache;
import zielu.svntoolbox.util.LogStopwatch;
import zielu.svntoolbox.util.MfSupplier;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * <p></p>
 * <br/>
 * <p>Created on 05.12.13</p>
 *
 * @author Lukasz Zielinski
 */
public class AsyncFileStatusCalculator extends AbstractProjectComponent implements AsyncStatusCalc {
    private final Logger log = Logger.getInstance(getClass());

    private final FileStatusCalculator myStatusCalc = new FileStatusCalculator();
    private final BlockingQueue<StatusRequest> myRequestQueue = new LinkedBlockingQueue<StatusRequest>();
    private final Set<VirtualFile> myPendingFiles = ContainerUtil.newConcurrentSet();

    private final AtomicBoolean myActive = new AtomicBoolean();
    private final AtomicBoolean myCalculationInProgress = new AtomicBoolean();
    private final AtomicBoolean myCalculationAllowed = new AtomicBoolean(true);

    private ProjectViewManager myProjectViewManager;
    private Supplier<Integer> pvSeq;
    private MessageBusConnection myConnection;

    public AsyncFileStatusCalculator(Project project) {
        super(project);
    }

    public static AsyncFileStatusCalculator getInstance(@NotNull Project project) {
        return project.getComponent(AsyncFileStatusCalculator.class);
    }

    @Override
    public void initComponent() {
        if (myActive.compareAndSet(false, true)) {
            myProjectViewManager = ProjectViewManager.getInstance(myProject);
            pvSeq = SvnToolBoxProject.getInstance(myProject).sequence();
            myConnection = myProject.getMessageBus().connect();
            myConnection.subscribe(DumbService.DUMB_MODE, new DumbModeListener() {
                @Override
                public void enteredDumbMode() {
                    myCalculationAllowed.compareAndSet(true, false);
                    if (log.isDebugEnabled()) {
                        log.debug("[" + pvSeq.get() + "] Entered Dumb-Mode");
                    }
                }

                @Override
                public void exitDumbMode() {
                    if (log.isDebugEnabled()) {
                        log.debug("[" + pvSeq.get() + "] Exit Dumb-Mode");
                    }
                    myCalculationAllowed.compareAndSet(false, true);
                    calculateStatus();
                }
            });
        }
    }

    @Override
    public void refreshView() {
        if (log.isDebugEnabled()) {
            log.debug("[" + pvSeq.get() + "] Requesting Project View refresh");
        }
        myProjectViewManager.refreshProjectView(myProject);
    }

    public void calculateStatus() {
        final boolean DEBUG = log.isDebugEnabled();
        if (myActive.get()) {
            if (myCalculationAllowed.get()) {
                if (!myCalculationInProgress.get()) {
                    SvnToolBoxApp.getInstance().submit(new Task());
                } else {
                    if (DEBUG) {
                        log.debug("[" + pvSeq.get() + "] Another status calculation in progress");
                    }
                }
            } else {
                if (DEBUG) {
                    log.debug("[" + pvSeq.get() + "] Calculation not available at this moment");
                }
            }
        } else {
            if (DEBUG) {
                log.debug("[" + pvSeq.get() + "] Component inactive - status calculation cancelled");
            }
        }
    }

    @Override
    public void projectClosed() {
        if (myActive.compareAndSet(true, false)) {
            int pendingFiles = myPendingFiles.size();
            myPendingFiles.clear();
            if (log.isDebugEnabled()) {
                log.debug("[" + pvSeq.get() + "] Project closed. Pending: files=" + pendingFiles + ", requests=" + myRequestQueue.size());
            }
            myRequestQueue.clear();
            myConnection.disconnect();
        }
        super.projectClosed();
    }

    @Override
    public void disposeComponent() {
        myRequestQueue.clear();
        int pendingFiles = myPendingFiles.size();
        myPendingFiles.clear();
        if (log.isDebugEnabled()) {
            log.debug("[" + pvSeq.get() + "] Component disposed. Pending: files=" + pendingFiles + ", requests=" + myRequestQueue.size());
        }
    }

    public Optional<FileStatus> scheduleStatusFor(@Nullable Project project, @NotNull VirtualFile vFile) {
        if (myActive.get()) {
            if (project == null) {
                return FileStatus.EMPTY_OPTIONAL;
            } else {
                if (myPendingFiles.add(vFile)) {
                    myRequestQueue.add(new StatusRequest(project, vFile));
                    if (log.isDebugEnabled()) {
                        log.debug("[" + pvSeq.get() + "] Queued request for " + vFile.getPath());
                        log.debug("[" + pvSeq.get() + "] Scheduling on-queued status calculation - " + myRequestQueue.size() + " requests pending");
                    }
                    calculateStatus();
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("[" + pvSeq.get() + "] " + vFile.getPath() + " already awaits calculation");
                    }
                }
            }
        }
        return Optional.empty();
    }

    private class Task implements Runnable {

        @Override
        public void run() {
            final boolean DEBUG = log.isDebugEnabled();
            if (myCalculationInProgress.compareAndSet(false, true)) {
                boolean exhausted = false;
                try {
                    StatusRequest request = myRequestQueue.poll(150, TimeUnit.MILLISECONDS);
                    if (request != null) {
                        if (myPendingFiles.remove(request.file)) {
                            AccessToken token = ApplicationManager.getApplication().acquireReadActionLock();
                            LogStopwatch watch = LogStopwatch.debugStopwatch(pvSeq,
                                    new MfSupplier("Status calculation for {0}", request.file)).start();
                            FileStatus status;
                            try {
                                status = myStatusCalc.statusFor(request.project, request.file);
                            } finally {
                                token.finish();
                                watch.stop();
                            }
                            ProjectViewStatus viewStatus;
                            if (status.isUnderVcs()) {
                                if (status.getBranchName().isPresent()) {
                                    viewStatus = new ProjectViewStatus(status.getBranchName().get());
                                } else {
                                    viewStatus = ProjectViewStatus.NOT_CONFIGURED;
                                }
                            } else {
                                viewStatus = ProjectViewStatus.EMPTY;
                            }
                            ProjectViewStatusCache cache = myProjectViewManager.getStatusCache();
                            cache.add(request.file, viewStatus);
                        } else {
                            if (DEBUG) {
                                log.debug("[" + pvSeq.get() + "] " + request.file.getPath() + " was already calculated");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            log.debug("[" + pvSeq.get() + "] Requests exhausted");
                        }
                        exhausted = true;
                    }
                } catch (InterruptedException e) {
                    log.error(e);
                } finally {
                    if (myCalculationInProgress.compareAndSet(true, false)) {
                        if (exhausted) {
                            refreshView();
                        } else {
                            if (DEBUG) {
                                log.debug("[" + pvSeq.get() + "] Scheduling next status calculation - " + myRequestQueue.size() + " requests pending");
                            }
                            calculateStatus();
                        }
                    }
                }
            } else {
                if (DEBUG) {
                    log.debug("[" + pvSeq.get() + "] Another status calculation in progress");
                }
            }
        }
    }
}
