/* 
 * $Id$
 */
package zielu.svntoolbox;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerAdapter;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import org.jetbrains.annotations.NotNull;
import zielu.svntoolbox.ui.SvnBranchWidget;

/**
 * <p></p>
 * <br/>
 * <p>Created on 19.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SVNToolBox extends AbstractProjectComponent {
    private final Project myProject;

    private SvnBranchWidget myBranchWidget;
    private ToolWindowManagerListener myToolWindowManagerListener;
    
    public SVNToolBox(@NotNull Project project) {
        super(project);
        this.myProject = project;
    }
    
    private void connect(final ToolWindowManagerEx toolWindowManager) {
        myToolWindowManagerListener = new ToolWindowManagerAdapter() {
            @Override
            public void toolWindowRegistered(@NotNull String id) {
                //System.out.println("Tool window: "+id);
                if ("Project".equals(id)) {
                    ToolWindow projectWindow = toolWindowManager.getToolWindow(id);
                    //System.out.println("Project window: "+projectWindow);
                    projectWindow.getContentManager().addContentManagerListener(new ContentManagerAdapter() {
                        @Override
                        public void contentAdded(ContentManagerEvent event) {
                            //System.out.println("Content: "+event.getContent());
                            if ("Project".equals(event.getContent().getDisplayName())) {
                                System.out.println("Project content: "+event.getContent().getComponent());
                            }
                        }

                        @Override
                        public void selectionChanged(ContentManagerEvent event) {
                            System.out.println("Selection: "+event.getContent().getComponent());
                        }
                    });
                }
            }
        };
        toolWindowManager.addToolWindowManagerListener(myToolWindowManagerListener);
    }
    
    private void disconnect(ToolWindowManagerEx toolWindowManager) {
        if (myToolWindowManagerListener != null) {
            toolWindowManager.removeToolWindowManagerListener(myToolWindowManagerListener);
        }
    }
    
    @Override
    public void projectOpened() {
        if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
            myBranchWidget = new SvnBranchWidget(myProject);
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
            if (statusBar != null) {
                statusBar.addWidget(myBranchWidget, myProject);
            }
            /*ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
            if (toolWindowManager instanceof ToolWindowManagerEx) {
                connect((ToolWindowManagerEx) toolWindowManager);   
            }*/
            
            /*final ToolWindow projectWindow = toolWindowManager.getToolWindow("Project");
            System.out.println("Project: "+projectWindow);*/
        }
    }

    @Override
    public void projectClosed() {
        if (myBranchWidget != null && !ApplicationManager.getApplication().isHeadlessEnvironment()) {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
            if (statusBar != null) {
                statusBar.removeWidget(myBranchWidget.ID());
            }
            /*ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
            if (toolWindowManager instanceof ToolWindowManagerEx) {
                disconnect((ToolWindowManagerEx) toolWindowManager);   
            }*/
        }
    }
}
