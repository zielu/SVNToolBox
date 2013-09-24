/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.projectView;

import com.google.common.collect.Lists;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.FilePathsHelper;
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener;
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener.Notification;
import com.intellij.openapi.vcs.update.UpdatedFilesListener;
import com.intellij.openapi.vfs.LocalFileOperationsHandler;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileManagerListener;
import com.intellij.openapi.vfs.ex.VirtualFileManagerEx;
import com.intellij.util.ThrowableConsumer;
import com.intellij.util.containers.HashSet;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ProjectViewManager extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());

    private final ProjectViewStatusCache statusCache;

    private MessageBusConnection myConnection;
    private LocalFileOperationsHandler myFileHandler;
    private VirtualFileListener myVfListener;
    private VirtualFileManagerListener myVfmListener;

    public ProjectViewManager(Project project) {
        super(project);
        statusCache = new ProjectViewStatusCache();
    }

    public static ProjectViewManager getInstance(Project project) {
        return project.getComponent(ProjectViewManager.class);
    }

    public ProjectViewStatusCache getStatusCache() {
        return statusCache;
    }

    private void refreshProjectView(final Project project) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ProjectView projectView = ProjectView.getInstance(project);
                projectView.refresh();
            }
        });
    }

    @Override
    public void initComponent() {
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
        myConnection.subscribe(UpdatedFilesListener.UPDATED_FILES, new UpdatedFilesListener() {
            @Override
            public void consume(Set<String> paths) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Updated paths: " + paths);
                }
                final HashSet<String> converted = new HashSet<String>();
                for (String path : paths) {
                    converted.add(FilePathsHelper.convertPath(path));
                }
                LocalFileSystem fs = LocalFileSystem.getInstance();
                List<VirtualFile> vFiles = Lists.newArrayListWithCapacity(converted.size());
                for (String convert : converted) {
                    VirtualFile vFile = fs.findFileByPath(convert);
                    if (vFile != null) {
                        vFiles.add(vFile);
                    }
                }
                if (statusCache.evictAll(vFiles)) {
                    refreshProjectView(myProject);
                }
            }
        });

        myFileHandler = new LocalFileOperationsHandler() {
            @Override
            public boolean delete(VirtualFile file) throws IOException {
                return statusCache.evict(file);
            }

            @Override
            public boolean move(VirtualFile file, VirtualFile toDir) throws IOException {
                return statusCache.evict(file);
            }

            @Nullable
            @Override
            public File copy(VirtualFile file, VirtualFile toDir, String copyName) throws IOException {
                return null;  //TODO: auto-generated method implementation
            }

            @Override
            public boolean rename(VirtualFile file, String newName) throws IOException {
                return statusCache.evict(file);
            }

            @Override
            public boolean createFile(VirtualFile dir, String name) throws IOException {
                return false;  //TODO: auto-generated method implementation
            }

            @Override
            public boolean createDirectory(VirtualFile dir, String name) throws IOException {
                return false;  //TODO: auto-generated method implementation
            }

            @Override
            public void afterDone(ThrowableConsumer<LocalFileOperationsHandler, IOException> invoker) {
                //TODO: auto-generated method implementation
            }
        };
        LocalFileSystem.getInstance().registerAuxiliaryFileOperationsHandler(myFileHandler);
        /*myVfListener = new VirtualFileListener() {
            @Override
            public void propertyChanged(VirtualFilePropertyEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Property changed: " + event);
                }
            }

            @Override
            public void contentsChanged(VirtualFileEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Contents changed: " + event);
                }
            }

            @Override
            public void fileCreated(VirtualFileEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created: " + event);
                }
            }

            @Override
            public void fileDeleted(VirtualFileEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Deleted: " + event);
                }
            }

            @Override
            public void fileMoved(VirtualFileMoveEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Moved: " + event);
                }
            }

            @Override
            public void fileCopied(VirtualFileCopyEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Copied: " + event);
                }
            }

            @Override
            public void beforePropertyChange(VirtualFilePropertyEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Before property change: " + event);
                }
            }

            @Override
            public void beforeContentsChange(VirtualFileEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Before content change: " + event);
                }
            }

            @Override
            public void beforeFileDeletion(VirtualFileEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Before deletion: " + event);
                }
            }

            @Override
            public void beforeFileMovement(VirtualFileMoveEvent event) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Before property change: " + event);
                }
            }
        };
        myVfmListener = new VirtualFileManagerListener() {
            @Override
            public void beforeRefreshStart(boolean asynchronous) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Before refresh async = " + asynchronous);
                }
            }

            @Override
            public void afterRefreshFinish(boolean asynchronous) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("After refresh async = " + asynchronous);
                }
            }
        };*/
        /*VirtualFileManager vfm = VirtualFileManagerEx.getInstance();
        vfm.addVirtualFileListener(myVfListener);
        vfm.addVirtualFileManagerListener(myVfmListener);*/
    }

    @Override
    public void disposeComponent() {
        if (myConnection != null) {
            myConnection.disconnect();
        }
        if (myFileHandler != null) {
            LocalFileSystem.getInstance().unregisterAuxiliaryFileOperationsHandler(myFileHandler);
        }
        VirtualFileManager vfm = VirtualFileManagerEx.getInstance();
        if (myVfListener != null) {
            vfm.removeVirtualFileListener(myVfListener);
        }
        if (myVfmListener != null) {
            vfm.removeVirtualFileManagerListener(myVfmListener);
        }
        statusCache.dispose();
    }
}
