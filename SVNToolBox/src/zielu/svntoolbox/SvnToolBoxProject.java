/* 
 * $Id$
 */
package zielu.svntoolbox;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import zielu.svntoolbox.ui.SvnBranchWidget;

/**
 * <p></p>
 * <br/>
 * <p>Created on 19.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnToolBoxProject extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());

    private final Project myProject;

    private SvnBranchWidget myBranchWidget;

    
    
    public SvnToolBoxProject(@NotNull Project project) {
        super(project);
        this.myProject = project;
    }

    public static SvnToolBoxProject getInstance(@NotNull Project project) {
        return project.getComponent(SvnToolBoxProject.class);
    }    
    
    @Override
    public void projectOpened() {
        if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
            myBranchWidget = new SvnBranchWidget(myProject);
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
            if (statusBar != null) {
                statusBar.addWidget(myBranchWidget, myProject);
            }
        }
        LOG.debug("Project opened");
    }

    @Override
    public void projectClosed() {
        if (myBranchWidget != null && !ApplicationManager.getApplication().isHeadlessEnvironment()) {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
            if (statusBar != null) {
                statusBar.removeWidget(myBranchWidget.ID());
            }
        }
        LOG.debug("Project closed");
    }
}
