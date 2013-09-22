/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.projectView;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
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

    @Override
    public void projectOpened() {
        MessageBusConnection connection = myProject.getMessageBus().connect();
        connection.subscribe(DecorationToggleNotifier.TOGGLE_TOPIC, new DecorationToggleNotifier() {
            @Override
            public void decorationChanged() {
                ProjectView projectView = ProjectView.getInstance(myProject);
                projectView.refresh();
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
