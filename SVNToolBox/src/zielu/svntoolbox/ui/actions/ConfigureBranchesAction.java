/* 
 * $Id$
 */
package zielu.svntoolbox.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.dialogs.BranchConfigurationDialog;

/**
 * <p></p>
 * <br/>
 * <p>Created on 23.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ConfigureBranchesAction extends AnAction {

    public ConfigureBranchesAction() {
        super(SvnBundle.getString("action.Subversion.ConfigureBranches.text"));
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        final DataContext dataContext = e.getDataContext();

        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            presentation.setEnabled(false);
            presentation.setVisible(false);
            return;
        }
        VirtualFile vFile = PlatformDataKeys.VIRTUAL_FILE.getData(dataContext);
        if (vFile == null) {
            presentation.setEnabled(false);
            presentation.setVisible(true);
            return;
        }
        SvnVcs vcs = SvnVcs.getInstance(project);
        if (!ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(vcs, new VirtualFile[]{vFile})) {
            presentation.setEnabled(false);
            presentation.setVisible(true);
            return;
        }
        presentation.setEnabled(true);
        presentation.setVisible(true);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile vFile = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        BranchConfigurationDialog.configureBranches(project, vFile);
    }
}
