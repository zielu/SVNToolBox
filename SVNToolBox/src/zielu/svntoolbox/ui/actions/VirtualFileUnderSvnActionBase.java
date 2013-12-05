/* 
 * @(#) $Id:  $
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.SvnVcs;

/**
 * <p></p>
 * <br/>
 * <p>Created on 03.12.13</p>
 *
 * @author Lukasz Zielinski
 */
public abstract class VirtualFileUnderSvnActionBase extends AnAction {

    protected VirtualFileUnderSvnActionBase(@Nullable String text) {
        super(text);
    }

    protected VirtualFileUnderSvnActionBase() {
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);

        final Presentation presentation = e.getPresentation();
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
            presentation.setVisible(false);
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
    public final void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile vFile = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        perform(e, project, vFile);
    }

    protected abstract void perform(AnActionEvent e, @NotNull Project project, @NotNull VirtualFile file);
}
