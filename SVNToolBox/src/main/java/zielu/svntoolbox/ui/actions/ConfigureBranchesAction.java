/* 
 * $Id$
 */
package zielu.svntoolbox.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnBundle;
import zielu.svntoolbox.ui.BranchConfigUi;

/**
 * <p></p>
 * <br/>
 * <p>Created on 23.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ConfigureBranchesAction extends VirtualFileUnderSvnActionBase {

    public ConfigureBranchesAction() {
        super(SvnBundle.getString("action.Subversion.ConfigureBranches.text"));
    }

    @Override
    protected void perform(AnActionEvent e, @NotNull Project project, @NotNull VirtualFile file) {
        BranchConfigUi.configureBranches(project, file);
    }
}
