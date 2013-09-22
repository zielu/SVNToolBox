/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.ui.projectView;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import zielu.svntoolbox.SvnToolBoxState;
import zielu.svntoolbox.projectView.DecorationToggleNotifier;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ToggleSvnModuleDecorationAction extends ToggleAction {
    
    @Override
    public boolean isSelected(AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            return SvnToolBoxState.getInstance(project).showProjectViewModuleDecoration;
        }
        return false;
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        Project project = e.getProject();
        if (project != null) {
            SvnToolBoxState.getInstance(project).showProjectViewModuleDecoration = state;
            project.getMessageBus().
                    syncPublisher(DecorationToggleNotifier.TOGGLE_TOPIC).decorationChanged();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null);
        super.update(e);
    }
}
