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
import zielu.svntoolbox.util.IntegerSequenceSupplier;

import java.util.function.Supplier;

/**
 * <p></p>
 * <br/>
 * <p>Created on 19.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnToolBoxProject extends AbstractProjectComponent {
    private final Logger log = Logger.getInstance(getClass());

    private final Supplier<Integer> pvSeq = new IntegerSequenceSupplier();
    
    private SvnBranchWidget myBranchWidget;    
    
    public SvnToolBoxProject(@NotNull Project project) {
        super(project);
    }

    public static SvnToolBoxProject getInstance(@NotNull Project project) {
        return project.getComponent(SvnToolBoxProject.class);
    }    
    
    public Supplier<Integer> sequence() {
        return pvSeq;
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
        log.debug("Project opened");
    }

    @Override
    public void projectClosed() {
        if (myBranchWidget != null && !ApplicationManager.getApplication().isHeadlessEnvironment()) {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
            if (statusBar != null) {
                statusBar.removeWidget(myBranchWidget.ID());
            }
        }
        log.debug("Project closed");
    }
}
