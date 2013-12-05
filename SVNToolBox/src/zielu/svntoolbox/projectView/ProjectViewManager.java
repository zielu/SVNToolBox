/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.projectView;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Sets;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener;
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener.Notification;
import com.intellij.openapi.vcs.update.UpdatedFilesListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.ex.VirtualFileManagerEx;
import com.intellij.util.messages.MessageBusConnection;
import zielu.svntoolbox.FileStatusCalculator;
import zielu.svntoolbox.SvnToolBoxProject;
import zielu.svntoolbox.config.SvnToolBoxProjectState;
import zielu.svntoolbox.util.Vfs;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ProjectViewManager extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());

    private final AtomicBoolean myActive = new AtomicBoolean();
    
    private ProjectViewStatusCache myStatusCache;
    
    private MessageBusConnection myConnection;
    private VirtualFileListener myVfListener;
    
    private AtomicInteger PV_SEQ;
    
    public ProjectViewManager(Project project) {
        super(project);        
    }

    public static ProjectViewManager getInstance(Project project) {
        return project.getComponent(ProjectViewManager.class);
    }

    public ProjectViewStatusCache getStatusCache() {
        return myStatusCache;
    }

    public void refreshProjectView(final Project project) {
        if (SvnToolBoxProjectState.getInstance(project).showingAnyDecorations()) {
            if (myActive.get()) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (myActive.get()) {
                            final ProjectView projectView = ProjectView.getInstance(project);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("[" + PV_SEQ.incrementAndGet() + "] Refreshing Project View");
                            }
                            projectView.refresh();
                        }
                    }
                });
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("[" + PV_SEQ.incrementAndGet() + "] Project View refresh ignored - decorations disabled");
            }
        }
    }

    @Override
    public void initComponent() {
        super.initComponent();
        if (myActive.compareAndSet(false, true)) {
            PV_SEQ = SvnToolBoxProject.getInstance(myProject).sequence();
            myStatusCache = new ProjectViewStatusCache(PV_SEQ);
            myConnection = myProject.getMessageBus().connect();
            myConnection.subscribe(DecorationToggleNotifier.TOGGLE_TOPIC, new DecorationToggleNotifier() {
                @Override
                public void decorationChanged(Project project) {
                    refreshProjectView(project);
                }
            });
            myConnection.subscribe(VcsConfigurationChangeListener.BRANCHES_CHANGED, new Notification() {
                @Override
                public void execute(Project project, VirtualFile vcsRoot) {
                    refreshProjectView(project);
                }
            });
            myConnection.subscribe(DecorationSettingsNotifier.TOGGLE_TOPIC, new DecorationSettingsNotifier() {
                @Override
                public void settingsChanged() {
                    refreshProjectView(myProject);
                }
            });
            myConnection.subscribe(UpdatedFilesListener.UPDATED_FILES, new UpdatedFilesListener() {
                final FileStatusCalculator myStatusCalc = new FileStatusCalculator();
    
                @Override
                public void consume(Set<String> paths) {
                    final Set<String> localPaths = Sets.newLinkedHashSet(paths);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[" + PV_SEQ.incrementAndGet() + "] Updated paths: " + localPaths);
                    }
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            List<VirtualFile> vFiles = Vfs.pathsToFiles(localPaths);
                            boolean somethingEvicted = myStatusCache.evictAll(vFiles);
                            List<VirtualFile> vFilesUnderSvn = myStatusCalc.filterUnderSvn(myProject, vFiles);
                            boolean somethingUnderSvn = vFilesUnderSvn.size() > 0;
                            if (somethingEvicted || somethingUnderSvn) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("[" + PV_SEQ.incrementAndGet() + "] Requesting project view refresh: somethingEvicted=" + somethingEvicted + ", somethingUnderSvn=" + somethingUnderSvn);
                                }
                                refreshProjectView(myProject);
                            }
                        }
                    });
                }
            });
    
            myVfListener = new VirtualFileAdapter() {
                @Override
                public void beforeFileDeletion(VirtualFileEvent event) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[" + PV_SEQ.incrementAndGet() + "] Before deletion: " + event);
                    }
                    myStatusCache.evict(event.getFile());
                }
    
                @Override
                public void beforeFileMovement(VirtualFileMoveEvent event) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[" + PV_SEQ.incrementAndGet() + "] Before move: " + event);
                    }
                    myStatusCache.evict(event.getFile());
                }
            };
            VirtualFileManager vfm = VirtualFileManagerEx.getInstance();
            vfm.addVirtualFileListener(myVfListener);
        }
    }

    @Override
    public void disposeComponent() {
        if (myActive.compareAndSet(true, false)) {
            if (myConnection != null) {
                myConnection.disconnect();
            }
            VirtualFileManager vfm = VirtualFileManagerEx.getInstance();
            if (myVfListener != null) {
                vfm.removeVirtualFileListener(myVfListener);
            }
            myStatusCache.dispose();
        }
        super.disposeComponent();
    }
}
