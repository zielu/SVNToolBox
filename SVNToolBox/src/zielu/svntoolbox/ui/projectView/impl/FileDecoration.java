/* 
 * $Id$
 */
package zielu.svntoolbox.ui.projectView.impl;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SimpleTextAttributes;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.10.13</p>
 *
 * @author Lukasz Zielinski
 */
public class FileDecoration extends AbstractNodeDecoration {
    @Override
    protected String getName(ProjectViewNode node) {
        PsiFileNode fileNode = (PsiFileNode) node;
        return fileNode.getValue().getName();
    }

    @Override
    protected VirtualFile getVirtualFile(ProjectViewNode node) {
        PsiFileNode fileNode = (PsiFileNode) node;
        return fileNode.getVirtualFile();
    }

    @Override
    protected void applyDecoration(ProjectViewNode node, PresentationData data) {
        String branchName = getBranchNameAndCache(node);
        if (branchName != null) {
            addSmartText(data, getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            data.addText(formatBranchName(branchName));
        }
    }

    @Override
    public boolean isForMe(ProjectViewNode node) {
        return (node instanceof PsiFileNode);
    }
}
