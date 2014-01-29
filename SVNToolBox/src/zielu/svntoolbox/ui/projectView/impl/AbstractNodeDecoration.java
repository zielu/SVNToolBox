/* 
 * $Id$
 */
package zielu.svntoolbox.ui.projectView.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nullable;
import zielu.svntoolbox.FileStatusCalculator;
import zielu.svntoolbox.SvnToolBoxBundle;
import zielu.svntoolbox.SvnToolBoxProject;
import zielu.svntoolbox.async.AsyncFileStatusCalculator;
import zielu.svntoolbox.config.SvnToolBoxAppState;
import zielu.svntoolbox.projectView.ProjectViewManager;
import zielu.svntoolbox.projectView.ProjectViewStatus;
import zielu.svntoolbox.projectView.ProjectViewStatusCache;
import zielu.svntoolbox.projectView.ProjectViewStatusCache.PutResult;
import zielu.svntoolbox.ui.projectView.NodeDecoration;
import zielu.svntoolbox.ui.projectView.NodeDecorationType;
import zielu.svntoolbox.util.LogStopwatch;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.10.13</p>
 *
 * @author Lukasz Zielinski
 */
public abstract class AbstractNodeDecoration implements NodeDecoration {
    private final static String PREFIX = SvnToolBoxBundle.getString("status.svn.prefix");
    
    protected final Logger LOG = Logger.getInstance(getClass());    
    protected final FileStatusCalculator myStatusCalc = new FileStatusCalculator();
    
    protected abstract String getName(ProjectViewNode node);

    protected abstract VirtualFile getVirtualFile(ProjectViewNode node);

    protected JBColor getBranchColor() {
        return SvnToolBoxAppState.getInstance().getProjectViewDecorationColor();
    }

    protected SimpleTextAttributes getBranchAttributes() {
        return new SimpleTextAttributes(SimpleTextAttributes.STYLE_SMALLER, getBranchColor());
    }
    
    @Nullable
    protected Object getParentValue(ProjectViewNode node) {
        return node.getParent() != null ? node.getParent().getValue() : null;
    }

    @Nullable
    protected String getBranchNameAndCache(ProjectViewNode node) {
        ProjectViewStatusCache cache = ProjectViewManager.getInstance(node.getProject()).getStatusCache();
        VirtualFile vFile = getVirtualFile(node);
        ProjectViewStatus cached = cache.get(vFile);
        if (cached != null) {
            if (!cached.isEmpty()) {
                return cached.getBranchName();
            }
            return null;
        } else {
            PutResult result = cache.add(vFile, ProjectViewStatus.PENDING);                        
            AsyncFileStatusCalculator.getInstance(node.getProject()).scheduleStatusForFileUnderSvn(node.getProject(), vFile);
            if (result != null) {
                return result.getFinalStatus().getBranchName();        
            }
            return null;
        }        
    }

    protected void addSmartText(PresentationData data, String text, SimpleTextAttributes attributes) {
        boolean add = true;
        for (ColoredFragment existing : data.getColoredText()) {
            if (existing.getText().equals(text)) {
                add = false;
            }
        }
        if (add) {
            data.addText(text, attributes);
        }
    }

    protected ColoredFragment formatBranchName(String branchName) {
        return new ColoredFragment(" ["+PREFIX+" " + branchName + "]", getBranchAttributes());
    }

    protected boolean isUnderSvn(ProjectViewNode node, AtomicInteger PV_SEQ) {
        LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, "[" + PV_SEQ.incrementAndGet() + "] Under SVN").start();
        VirtualFile vFile = getVirtualFile(node);
        watch.tick("Get VFile");
        boolean result = false;
        if (vFile != null) {
            boolean underControl = myStatusCalc.filesUnderSvn(node.getProject(), vFile);
            watch.tick("Under control={0}", underControl);
            result = underControl;
        }
        watch.stop();
        return result;
    }

    protected abstract void applyDecorationUnderSvn(ProjectViewNode node, PresentationData data);
    
    @Override
    public final void decorate(ProjectViewNode node, PresentationData data) {
        AtomicInteger PV_SEQ = SvnToolBoxProject.getInstance(node.getProject()).sequence();
        if (isUnderSvn(node, PV_SEQ)) {
            LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, "[" + PV_SEQ.incrementAndGet() + "] Decorate").start();
            applyDecorationUnderSvn(node, data);
            watch.tick("Apply");
        }
    }

    @Override
    public NodeDecorationType getType() {
        return NodeDecorationType.Other;
    }
}
