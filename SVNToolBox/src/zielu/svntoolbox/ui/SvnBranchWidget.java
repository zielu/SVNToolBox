/* 
 * $Id$
 */
package zielu.svntoolbox.ui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Suppliers;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener;
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener.Notification;
import com.intellij.openapi.vcs.update.UpdatedFilesListener;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.dialogs.BranchConfigurationDialog;
import zielu.svntoolbox.FileStatus;
import zielu.svntoolbox.FileStatusCalculator;
import zielu.svntoolbox.SvnToolBoxBundle;
import zielu.svntoolbox.util.LogStopwatch;
import zielu.svntoolbox.util.Vfs;

/**
 * <p></p>
 * <br/>
 * <p>Created on 19.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnBranchWidget extends EditorBasedWidget implements StatusBarWidget.Multiframe, StatusBarWidget.TextPresentation {
    private final Logger LOG = Logger.getInstance(getClass());

    private final static String NA = SvnToolBoxBundle.getString("status.svn.na");
    private final static String EMPTY_BRANCH = SvnToolBoxBundle.getString("status.svn.notConfigured");
    private final static String PREFIX = SvnToolBoxBundle.getString("status.svn.prefix");

    private final FileStatusCalculator myStatusCalculator = new FileStatusCalculator();
    private final MessageBusConnection myConnection;

    private final static boolean READ_INFO_IN_OTHER_THREAD = false;

    private String myText = NA;
    private String myToolTip = "";

    public SvnBranchWidget(@NotNull Project project) {
        super(project);
        myConnection = project.getMessageBus().connect(this);
        myConnection.subscribe(VcsConfigurationChangeListener.BRANCHES_CHANGED, getBranchesChangedNotification());
        myConnection.subscribe(UpdatedFilesListener.UPDATED_FILES, new UpdatedFilesListener() {
            @Override
            public void consume(Set<String> paths) {
                List<VirtualFile> vFiles = Vfs.pathsToFiles(paths);
                final List<VirtualFile> underSvn = myStatusCalculator.filterUnderSvn(getProject(), vFiles);
                if (!underSvn.isEmpty()) {
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            VirtualFile currentFile = getCurrentFile();
                            if (currentFile != null && isAffected(currentFile, underSvn)) {
                                runUpdate();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void dispose() {
        myConnection.disconnect();
        super.dispose();
    }

    @NotNull
    @Override
    public String ID() {
        return getClass().getName();
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType platformType) {
        return this;
    }

    @Override
    public StatusBarWidget copy() {
        return new SvnBranchWidget(myProject);
    }

    @NotNull
    @Override
    public String getText() {
        return myText;
    }

    @NotNull
    @Override
    public String getMaxPossibleText() {
        return "0000000000000000";
    }

    @Override
    public float getAlignment() {
        return Component.LEFT_ALIGNMENT;
    }

    @Nullable
    @Override
    public String getTooltipText() {
        return myToolTip;
    }

    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return new Consumer<MouseEvent>() {
            @Override
            public void consume(MouseEvent mouseEvent) {
                runUpdate(true);
            }
        };
    }

    @Override
    public void fileOpened(FileEditorManager source, VirtualFile file) {
        runUpdate();
    }

    @Override
    public void fileClosed(FileEditorManager source, VirtualFile file) {
        runUpdate();
    }

    @Override
    public void selectionChanged(FileEditorManagerEvent event) {
        runUpdate();
    }

    private Notification getBranchesChangedNotification() {
        return new Notification() {
            @Override
            public void execute(Project project, VirtualFile vcsRoot) {
                runUpdate(project, Optional.fromNullable(vcsRoot));
            }
        };
    }

    private boolean empty() {
        boolean result = setToolTip(SvnToolBoxBundle.getString("status.svn.na.tooltip"));
        if (setText(PREFIX+" "+NA) && !result) {
            result = true;
        }
        return result;
    }

    private String prepareBranchText(FileStatus status) {
        StringBuilder text = new StringBuilder(PREFIX).append(" ");
        if (status.getBranchName().isPresent()) {
            text.append(status.getBranchName().get());
        } else {
            text.append(EMPTY_BRANCH);
        }
        return text.toString();
    }

    private boolean setText(String text) {
        if (!Objects.equal(myText, text)) {
            myText = text;
            return true;
        }
        return false;
    }

    private boolean setToolTip(String toolTip) {
        if (!Objects.equal(myToolTip, toolTip)) {
            myToolTip = toolTip;
            return true;
        }
        return false;
    }

    private void runUpdate() {
        runUpdate(false);
    }

    private void runUpdate(final boolean maybeOpenBranchConfig) {
        Runnable update = new Runnable() {
            @Override
            public void run() {
                final Optional<UpdateResult> maybeResult = update();
                updateUi(maybeResult, maybeOpenBranchConfig);
            }
        };
        execUpdate(update);
    }

    private void runUpdate(final @NotNull Project project, final Optional<VirtualFile> vcsRoot) {
        Runnable update = new Runnable() {
            @Override
            public void run() {
                final Optional<UpdateResult> maybeResult = update(project, vcsRoot);
                updateUi(maybeResult, false);
            }
        };
        execUpdate(update);
    }

    private void execUpdate(Runnable updateTask) {
        if (READ_INFO_IN_OTHER_THREAD) {
            LOG.debug("Will execute update in pool");
            ApplicationManager.getApplication().executeOnPooledThread(updateTask);
        } else {
            LOG.debug("Will execute update as readAction");
            ApplicationManager.getApplication().runReadAction(updateTask);
        }
    }

    private boolean isChildOf(VirtualFile child, VirtualFile parent) {
        return VfsUtilCore.isAncestor(parent, child, true);
    }

    private boolean isAffected(VirtualFile file, Collection<VirtualFile> files) {
        for (VirtualFile vFile : files) {
            if (vFile.isDirectory() && isChildOf(file, vFile)) {
                return true;
            } else if (file.equals(vFile)) {
                return true;
            }
        }
        return false;
    }

    private void updateStatusBar() {
        if (myStatusBar != null) {
            myStatusBar.updateWidget(ID());
        }
    }
    
    private void updateUi(final Optional<UpdateResult> maybeResult, final boolean maybeOpenBranchConfig) {
        if (maybeResult.isPresent()) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, Suppliers.ofInstance("UpdateUi")).start();
                    UpdateResult result = maybeResult.get();
                    boolean empty = true;
                    AtomicBoolean updated = new AtomicBoolean();
                    if (result.hasStatus()) {
                        if (result.status.isUnderVcs()) {
                            updated.compareAndSet(false, setToolTip(result.status.getURL().toDecodedString()));
                            empty = false;
                            updated.compareAndSet(false, setText(prepareBranchText(result.status)));
                        }
                        if (empty) {
                            updated.compareAndSet(false, empty());
                        }
                        if (updated.get()) {
                            updateStatusBar();
                        }
                        watch.stop();
                        if (maybeOpenBranchConfig && maybeResult.isPresent()) {
                            if (result.canConfigureBranches() && !result.status.getBranchName().isPresent()) {
                                BranchConfigurationDialog.configureBranches(result.project, result.file);
                            }
                        }
                    }
                }
            });
        }
    }

    @Nullable
    private VirtualFile getCurrentFile() {
        if (READ_INFO_IN_OTHER_THREAD) {
            final AtomicReference<VirtualFile> selectedFile = new AtomicReference<VirtualFile>();
            ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    selectedFile.set(getSelectedFile());
                }
            }, ModalityState.any());
            return selectedFile.get();
        } else {
            return getSelectedFile();
        }
    }

    private Optional<UpdateResult> update(@NotNull Project project, Optional<VirtualFile> vcsRoot) {
        LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, Suppliers.ofInstance("Update")).start();
        SvnVcs svn = SvnVcs.getInstance(project);
        FileStatus status = null;
        VirtualFile currentVf = null;
        if (svn != null) {
            currentVf = getCurrentFile();
            if (currentVf != null) {
                boolean calculateStatus = true;
                if (vcsRoot.isPresent()) {
                    calculateStatus = isChildOf(currentVf, vcsRoot.get());
                }
                if (calculateStatus) {
                    status = myStatusCalculator.statusFor(svn, project, currentVf);
                }
            }
        }
        watch.stop();
        return Optional.of(new UpdateResult(project, currentVf, status));
    }

    private Optional<UpdateResult> update() {
        if (isDisposed()) {
            return Optional.absent();
        } else {
            Project project = getProject();
            if (project == null) {
                empty();
                return Optional.absent();
            }
            return update(project, Optional.<VirtualFile>absent());
        }
    }

    private class UpdateResult {
        private Project project;
        private VirtualFile file;
        private FileStatus status;

        private UpdateResult(Project project, VirtualFile file, FileStatus status) {
            this.project = project;
            this.file = file;
            this.status = status;
        }

        public boolean hasStatus() {
            return status != null;
        }

        public boolean canConfigureBranches() {
            return project != null && file != null && status != null;
        }
    }
}
