/* 
 * $Id$
 */
package zielu.svntoolbox;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import org.jetbrains.annotations.NotNull;
import zielu.svntoolbox.extensions.NodeDecorationEP;
import zielu.svntoolbox.ui.projectView.NodeDecoration;
import zielu.svntoolbox.ui.projectView.impl.EmptyDecoration;

/**
 * <p></p>
 * <br/>
 * <p>Created on 14.10.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnToolBoxApp implements ApplicationComponent {
    private final Logger LOG = Logger.getInstance(getClass());
    
    private final List<NodeDecoration> nodeDecorations = Lists.newArrayList();
    
    public static SvnToolBoxApp getInstance() {
        return ApplicationManager.getApplication().getComponent(SvnToolBoxApp.class);
    }
    
    @Override
    public void initComponent() {
        List<NodeDecorationEP> nodeDecorationEPs = Arrays.asList(Extensions.getExtensions(NodeDecorationEP.POINT_NAME));
        Collections.sort(nodeDecorationEPs);
        for (NodeDecorationEP decorationEP : nodeDecorationEPs) {
            NodeDecoration decoration = decorationEP.instantiate();
            nodeDecorations.add(decoration);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Added decoration "+decorationEP.priority+" "+decoration);
            }
        }
    }

    public NodeDecoration decorationFor(ProjectViewNode node) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        NodeDecoration decoration = EmptyDecoration.INSTANCE;
        for (NodeDecoration candidate : nodeDecorations) {
            if (candidate.isForMe(node)) {
                decoration = candidate;
            }
        }
        return decoration;
    }
    
    @Override
    public void disposeComponent() {
        nodeDecorations.clear();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return getClass().getSimpleName();
    }
}
