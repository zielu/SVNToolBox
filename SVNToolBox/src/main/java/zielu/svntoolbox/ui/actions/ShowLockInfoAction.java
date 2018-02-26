package zielu.svntoolbox.ui.actions;

import static zielu.svntoolbox.SvnToolBoxBundle.getString;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * <p></p>
 * <br/>
 * <p>Created on 22.04.15</p>
 *
 * @author BONO Adil
 * @author Lukasz Zielinski
 */
public class ShowLockInfoAction extends VirtualFileUnderSvnActionBase {
    public ShowLockInfoAction() {
        super(getString("action.show.lock.info"));
    }

    @Override
    protected void perform(AnActionEvent e, @NotNull Project project, @NotNull VirtualFile file) {
        new ShowLockInfoTask(project, file).queue();
    }
}
