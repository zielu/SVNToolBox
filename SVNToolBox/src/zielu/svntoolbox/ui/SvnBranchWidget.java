/* 
 * $Id$
 */
package zielu.svntoolbox.ui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener;
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener.Notification;
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

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * <p></p>
 * <br/>
 * <p>Created on 19.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnBranchWidget extends EditorBasedWidget implements StatusBarWidget.Multiframe, StatusBarWidget.TextPresentation {
    private final static String NA = "Svn: N/A";
    private final static String EMPTY_BRANCH = "Not configured";

    private final FileStatusCalculator myStatusCalculator = new FileStatusCalculator();
    private final MessageBusConnection myBranchesChangedConnection;
    
    private String myText = NA;            
    private String myToolTip = "";        
    
    public SvnBranchWidget(@NotNull Project project) {
        super(project);
        myBranchesChangedConnection = project.getMessageBus().connect(this);
        myBranchesChangedConnection.subscribe(VcsConfigurationChangeListener.BRANCHES_CHANGED, getBranchesChangedNotification());
    }

    @Override
    public void dispose() {        
        myBranchesChangedConnection.disconnect();
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
                updateLater(true);
            }
        };
    }

    @Override
    public void fileOpened(FileEditorManager source, VirtualFile file) {
        updateLater();
    }

    @Override
    public void fileClosed(FileEditorManager source, VirtualFile file) {
        updateLater();
    }

    @Override
    public void selectionChanged(FileEditorManagerEvent event) {
        updateLater();
    }

    private Notification getBranchesChangedNotification() {
        return new Notification() {
            @Override
            public void execute(Project project, VirtualFile vcsRoot) {
                updateLater(project, Optional.fromNullable(vcsRoot));            
            }
        };    
    }
    
    private boolean empty() {        
        boolean result = setToolTip("");
        if (setText(NA) && !result) {
            result = true;
        }
        return result;
    }
    
    private String prepareBranchText(FileStatus status) {
        StringBuilder text = new StringBuilder("Svn: ");
        if (status.getBranch().isPresent()) {            
            /*if (status.getBranchDirectory().isPresent()) {
                text.append(status.getBranchDirectory().get()).append("/");                
            }*/
            if (status.getBranchName().isPresent()) {
                text.append(status.getBranchName().get());
            } else {
                text.append(EMPTY_BRANCH);
            }   
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
    
    private void updateLater() {
        updateLater(false);
    }
    
    private void updateLater(final boolean maybeOpenBranchConfig) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Optional<UpdateResult> maybeResult = update();
                if (maybeOpenBranchConfig && maybeResult.isPresent()) {
                    UpdateResult result = maybeResult.get();
                    if (result.canConfigureBranches() && !result.status.getBranch().isPresent()) {                        
                        BranchConfigurationDialog.configureBranches(result.project, result.file);    
                    }
                }
            }
        });
    }        
    
    private void updateLater(final @NotNull Project project, final Optional<VirtualFile> vcsRoot) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                update(project, vcsRoot);     
            }
        });
    }
    
    private boolean isChildOf(VirtualFile child, VirtualFile parent) {
        return VfsUtilCore.isAncestor(parent, child, true);    
    }
    
    private Optional<UpdateResult> update(@NotNull Project project, Optional<VirtualFile> vcsRoot) {
        SvnVcs svn = SvnVcs.getInstance(project);
        boolean empty = true;
        FileStatus status = null;
        VirtualFile currentVf = null;
        AtomicBoolean updated = new AtomicBoolean();
        if (svn != null) {                    
            currentVf = getSelectedFile();
            if (currentVf != null) {
                boolean calculateStatus = true;
                if (vcsRoot.isPresent()) {
                    calculateStatus = isChildOf(currentVf, vcsRoot.get());                    
                }
                if (calculateStatus) {
                    status = myStatusCalculator.statusFor(svn, project, currentVf);
                    if (status.isUnderVcs()) {
                        updated.compareAndSet(false, setToolTip(status.getURL().toDecodedString()));                                
                        empty = false;                            
                        updated.compareAndSet(false, setText(prepareBranchText(status)));
                    }
                } else {
                    empty = false;
                }
            }
        }                
        if (empty) {
            updated.compareAndSet(false, empty());
        }
        if (updated.get()) {
            myStatusBar.updateWidget(ID());                    
        }
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
        
        public boolean canConfigureBranches() {
            return project != null && file != null && status != null;
        }
    }
}
