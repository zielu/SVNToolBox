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
import zielu.svntoolbox.SvnToolBoxApp;
import zielu.svntoolbox.SvnToolBoxProject;
import zielu.svntoolbox.config.SvnToolBoxProjectState;
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
                    SvnToolBoxApp svnToolBox = SvnToolBoxApp.getInstance();
                    NodeDecoration decoration = svnToolBox.decorationFor(node);                    
                                      
                    if (LOG.isDebugEnabled()) {
                        final int seq = SvnToolBoxProject.getInstance(project).sequence().incrementAndGet();
                        LOG.debug("[" + seq + "] Node: " + decoration.getClass().getName() 
                                + " " + node + " " + node.getClass().getName());
                    }
                    if (decoration.getType() == NodeDecorationType.Module) {
                        if (config.showProjectViewModuleDecoration) {
                            decoration.decorate(node, data);
                        }
                    } else if (config.showProjectViewSwitchedDecoration) {
                        LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, "Switched decoration").start();
                        decoration.decorate(node, data);
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
