/*
 * $Id$
 */
package zielu.svntoolbox;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
    public static final NotificationGroup NOTIFICATION = new NotificationGroup("SVN ToolBox Messages", NotificationDisplayType.STICKY_BALLOON, true);

    private ExecutorService myExecutor;
    private ScheduledExecutorService myScheduledExecutor;

    public static SvnToolBoxApp getInstance() {
        return ApplicationManager.getApplication().getComponent(SvnToolBoxApp.class);
    }

    public Future<?> submit(Runnable task) {
        //return ApplicationManager.getApplication().executeOnPooledThread(task);
        return myExecutor.submit(task);
    }

    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit timeUnit) {
        return myScheduledExecutor.schedule(task, delay, timeUnit);
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
        myScheduledExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(getComponentName() + "-scheduled-pool-%s")
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
