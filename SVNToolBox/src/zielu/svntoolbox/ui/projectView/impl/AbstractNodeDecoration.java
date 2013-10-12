/* 
 * $Id$
 */
package zielu.svntoolbox.ui.projectView.impl;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nullable;
import zielu.svntoolbox.FileStatus;
import zielu.svntoolbox.FileStatusCalculator;
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
        } else {
            FileStatus status = myStatusCalc.statusFor(node.getProject(), vFile);
            PutResult result;
            if (status.getBranchName().isPresent()) {
                result = cache.add(vFile, new ProjectViewStatus(status.getBranchName().get()));
            } else {
                result = cache.add(vFile, ProjectViewStatus.EMPTY);
            }
            if (result != null) {
                if (result.getFinalStatus().isEmpty()) {
                    return null;
                } else {
                    return result.getFinalStatus().getBranchName();
                }
            }
        }
        return null;
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
        return new ColoredFragment(" [Svn: " + branchName + "]", getBranchAttributes());
    }

    protected boolean isUnderSvn(ProjectViewNode node, ProjectViewManager manager) {
        LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, "[" + manager.PV_SEQ.incrementAndGet() + "] Under SVN").start();
        VirtualFile vFile = getVirtualFile(node);
        watch.tick("Get VFile");
        boolean result = false;
        if (vFile != null) {
            boolean underControl = myStatusCalc.filesUnderSvn(node.getProject(), vFile);
            watch.tick("Under control={0}", underControl);
            result = underControl;
        }
        return result;
    }

    protected abstract void applyDecoration(ProjectViewNode node, PresentationData data);
    
    @Override
    public void decorate(ProjectViewNode node, PresentationData data) {
        ProjectViewManager pvManager = ProjectViewManager.getInstance(node.getProject());
        if (isUnderSvn(node, pvManager)) {
            LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, "[" + pvManager.PV_SEQ.incrementAndGet() + "] Switched").start();
            applyDecoration(node, data);
            watch.tick("Decoration");
        }
    }

    protected ProjectViewManager getProjectViewManager(ProjectViewNode node) {
        return ProjectViewManager.getInstance(node.getProject());    
    }

    @Override
    public NodeDecorationType getType() {
        return NodeDecorationType.Other;
    }
}
