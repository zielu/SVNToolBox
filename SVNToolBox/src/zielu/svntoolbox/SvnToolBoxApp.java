/* 
 * $Id$
 */
package zielu.svntoolbox;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
    
    private final List<NodeDecoration> myNodeDecorations = Lists.newArrayList();
    
    private ExecutorService myExecutor;
    
    public static SvnToolBoxApp getInstance() {
        return ApplicationManager.getApplication().getComponent(SvnToolBoxApp.class);
    }
    
    public Future<?> submit(Runnable task) {
        //return ApplicationManager.getApplication().executeOnPooledThread(task);
        return myExecutor.submit(task);
    }
    
    @Override
    public void initComponent() {
        myExecutor = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat(getComponentName()+"-pool-%s")
                        .setPriority(Thread.NORM_PRIORITY)
                        .build()
        );
        List<NodeDecorationEP> nodeDecorationEPs = Arrays.asList(Extensions.getExtensions(NodeDecorationEP.POINT_NAME));
        Collections.sort(nodeDecorationEPs);
        for (NodeDecorationEP decorationEP : nodeDecorationEPs) {
            NodeDecoration decoration = decorationEP.instantiate();
            myNodeDecorations.add(decoration);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Added decoration "+decorationEP.priority+" "+decoration);
            }
        }        
    }

    public NodeDecoration decorationFor(ProjectViewNode node) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        for (NodeDecoration candidate : myNodeDecorations) {
            if (candidate.isForMe(node)) {
                return candidate;
            }
        }
        return EmptyDecoration.INSTANCE;
    }
    
    @Override
    public void disposeComponent() {
        if (myExecutor != null) {
            myExecutor.shutdownNow();
        }        
        myNodeDecorations.clear();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return getClass().getSimpleName();
    }
}
