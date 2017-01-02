/*
 * $Id$
 */
package zielu.svntoolbox.ui.projectView.impl;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.PsiUtilBase;
import zielu.svntoolbox.projectView.ProjectViewStatus;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.10.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ClassFileDecoration extends AbstractNodeDecoration {

    @Override
    protected VirtualFile getVirtualFile(ProjectViewNode node) {
        ClassTreeNode classNode = (ClassTreeNode) node;
        return PsiUtilBase.getVirtualFile(classNode.getPsiClass());
    }

    @Override
    protected void applyDecorationUnderSvn(ProjectViewNode node, PresentationData data) {
        ProjectViewStatus status = getBranchStatusAndCache(node);
        if (shouldApplyDecoration(status)) {
            data.addText(formatBranchName(status));
        }
    }

    @Override
    public boolean isForMe(ProjectViewNode node) {
        if (node instanceof ClassTreeNode) {
            ClassTreeNode classNode = (ClassTreeNode) node;
            if (classNode.isTopLevel()) {
                return true;
            }
        }
        return false;
    }
}
