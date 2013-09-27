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
import zielu.svntoolbox.config.SvnToolBoxProjectState;
import zielu.svntoolbox.projectView.ProjectViewManager;
import zielu.svntoolbox.util.LogStopwatch;

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
                SvnToolBoxProjectState config = SvnToolBoxProjectState.getInstance(project);
                if (config.showingAnyDecorations()) {
                    NodeDecoration type = NodeDecoration.fromNode(node);
                    if (LOG.isDebugEnabled()) {
                        final int seq = ProjectViewManager.getInstance(project).PV_SEQ.incrementAndGet();
                        LOG.debug("[" + seq + "] Node: " + type + " " + node + " " + node.getClass().getName());
                    }
                    if (type == NodeDecoration.Module) {
                        if (config.showProjectViewModuleDecoration) {
                            type.apply(node, data);
                        }
                    } else if (config.showProjectViewSwitchedDecoration) {
                        LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, "Switched decoration").start();
                        type.apply(node, data);
                        watch.stop();
                    }
                }
            }

        }
    }

    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Decorate package dependencies");
        }
    }
}
