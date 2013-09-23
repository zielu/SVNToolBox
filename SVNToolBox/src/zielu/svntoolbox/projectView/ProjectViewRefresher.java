/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.projectView;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener;
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener.Notification;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ProjectViewRefresher extends AbstractProjectComponent {
    private MessageBusConnection connection;
    
    public ProjectViewRefresher(Project project) {
        super(project);
    }

    private void refreshProjectView(Project project) {
        ProjectView projectView = ProjectView.getInstance(project);
        projectView.refresh();
    }
    
    @Override
    public void projectOpened() {
        connection = myProject.getMessageBus().connect();
        connection.subscribe(DecorationToggleNotifier.TOGGLE_TOPIC, new DecorationToggleNotifier() {
            @Override
            public void decorationChanged() {
                refreshProjectView(myProject);
            }
        });
        connection.subscribe(VcsConfigurationChangeListener.BRANCHES_CHANGED, new Notification() {
            @Override
            public void execute(Project project, VirtualFile vcsRoot) {
                refreshProjectView(project);
            }
        });
    }

    @Override
    public void projectClosed() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}
