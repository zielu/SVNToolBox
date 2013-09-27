/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import zielu.svntoolbox.SvnToolBoxBundle;
import zielu.svntoolbox.config.SvnToolBoxProjectState;
import zielu.svntoolbox.projectView.DecorationToggleNotifier;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ToggleSvnModuleDecorationAction extends ToggleAction {

    public ToggleSvnModuleDecorationAction() {
        super(SvnToolBoxBundle.getString("action.show.module.decorations"));
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            return SvnToolBoxProjectState.getInstance(project).showProjectViewModuleDecoration;
        }
        return false;
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        Project project = e.getProject();
        if (project != null) {
            SvnToolBoxProjectState.getInstance(project).showProjectViewModuleDecoration = state;
            project.getMessageBus().
                    syncPublisher(DecorationToggleNotifier.TOGGLE_TOPIC).decorationChanged(project);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null);
        super.update(e);
    }
}
