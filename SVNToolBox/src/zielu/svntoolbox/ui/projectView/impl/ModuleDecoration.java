/* 
 * $Id$
 */
package zielu.svntoolbox.ui.projectView.impl;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.ModuleGroup;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import zielu.svntoolbox.ui.projectView.NodeDecorationType;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.10.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ModuleDecoration extends AbstractNodeDecoration {

    @Override
    public NodeDecorationType getType() {
        return NodeDecorationType.Module;
    }

    @Override
    protected String getName(ProjectViewNode node) {
        return null;
    }

    @Override
    protected VirtualFile getVirtualFile(ProjectViewNode node) {
        return node.getVirtualFile();
    }

    @Override
    protected void applyDecorationUnderSvn(ProjectViewNode node, PresentationData data) {
        String branchName = getBranchNameAndCache(node);
        if (branchName != null) {
            data.addText(formatBranchName(branchName));
        }
    }

    @Override
    public boolean isForMe(ProjectViewNode node) {
        if (node instanceof PsiDirectoryNode) {
            PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
            final Object parentValue = dirNode.getParent().getValue();
            return (parentValue instanceof Project || parentValue instanceof ModuleGroup);
        }
        return false;
    }
}
