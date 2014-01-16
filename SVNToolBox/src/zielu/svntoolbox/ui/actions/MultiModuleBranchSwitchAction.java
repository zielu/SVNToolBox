/* 
 * $Id$
 */
package zielu.svntoolbox.ui.actions;

import java.util.Arrays;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import zielu.svntoolbox.SvnToolBoxBundle;
import zielu.svntoolbox.branch.MultiModuleSwitch;

/**
 * <p></p>
 * <br/>
 * <p>Created on 16.01.14</p>
 *
 * @author Lukasz Zielinski
 */
public class MultiModuleBranchSwitchAction extends AnAction {

    public MultiModuleBranchSwitchAction() {
        super(SvnToolBoxBundle.getString("action.switch.branch"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final DataContext dataContext = e.getDataContext();
        
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        Module[] modules = LangDataKeys.MODULE_CONTEXT_ARRAY.getData(dataContext);
        if (project != null && modules != null) {
            MultiModuleSwitch switcher = new MultiModuleSwitch();
            switcher.getSwitchSpecification(project, Arrays.asList(modules));
        }
        //TODO: show non-blocking message why nothing happened
    }
}
