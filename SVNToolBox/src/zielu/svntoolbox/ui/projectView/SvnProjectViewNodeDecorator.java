/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.ui.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import zielu.svntoolbox.SvnToolBoxState;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnProjectViewNodeDecorator implements ProjectViewNodeDecorator {
    private final Logger LOG = Logger.getInstance(getClass());
    
    @Override
    public void decorate(ProjectViewNode node, PresentationData data) {
        if (node != null) {
            Project project = node.getProject();
            if (project != null) {
                SvnToolBoxState config = SvnToolBoxState.getInstance(project);
                if (config.showingAnyDecorations()) {
                    NodeDecoration type = NodeDecoration.fromNode(node);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Node: " + type + " " + node + " " + node.getClass().getName());
                    }
                    if (type == NodeDecoration.Module) {
                        if (config.showProjectViewModuleDecoration) {
                            type.apply(node, data);
                        }
                    } else if (config.showProjectViewSwitchedDecoration) {
                        type.apply(node, data);
                    }
                }
            }
            
        }
    }
    
    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {
        //TODO: auto-generated method implementation
    }
}
