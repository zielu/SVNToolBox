/* 
 * $Id$
 */
package zielu.svntoolbox;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import zielu.svntoolbox.extensions.NodeDecorationEP;
import zielu.svntoolbox.ui.SvnBranchWidget;
import zielu.svntoolbox.ui.projectView.NodeDecoration;

/**
 * <p></p>
 * <br/>
 * <p>Created on 19.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnToolBox extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());

    private final Project myProject;

    private SvnBranchWidget myBranchWidget;

    private final List<NodeDecoration> nodeDecorations = Lists.newArrayList();
    
    public SvnToolBox(@NotNull Project project) {
        super(project);
        this.myProject = project;
    }

    public static SvnToolBox getInstance(@NotNull Project project) {
        return project.getComponent(SvnToolBox.class);
    }

    @Override
    public void initComponent() {
        List<NodeDecorationEP> nodeDecorationEPs = Arrays.asList(Extensions.getExtensions(NodeDecorationEP.POINT_NAME));
        Collections.sort(nodeDecorationEPs);
        for (NodeDecorationEP decorationEP : nodeDecorationEPs) {
            nodeDecorations.add(decorationEP.instantiate());    
        }
    }

    public Iterable<NodeDecoration> getNodeDecorations() {
        return nodeDecorations;    
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
