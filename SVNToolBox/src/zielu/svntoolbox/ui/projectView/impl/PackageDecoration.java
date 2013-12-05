/* 
 * $Id$
 */
package zielu.svntoolbox.ui.projectView.impl;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.ProjectViewDirectoryHelper;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.SimpleTextAttributes;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.10.13</p>
 *
 * @author Lukasz Zielinski
 */
public class PackageDecoration extends AbstractNodeDecoration {
    @Override
    protected String getName(ProjectViewNode node) {
        PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
        final PsiDirectory psiDirectory = dirNode.getValue();
        //as in com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode.updateImpl            
        String name = ProjectViewDirectoryHelper.getInstance(psiDirectory.getProject()).getNodeName(node.getSettings(),
                getParentValue(node), psiDirectory);
        return name;
    }

    @Override
    protected VirtualFile getVirtualFile(ProjectViewNode node) {
        return node.getVirtualFile();
    }

    @Override
    protected void applyDecorationUnderSvn(ProjectViewNode node, PresentationData data) {
        String branchName = getBranchNameAndCache(node);
        if (branchName != null) {
            addSmartText(data, getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            data.addText(formatBranchName(branchName));
        }
    }

    @Override
    public boolean isForMe(ProjectViewNode node) {
        if (node instanceof PsiDirectoryNode) {
            PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
            final Project project = dirNode.getProject();
            final PsiDirectory psiDirectory = dirNode.getValue();
            final VirtualFile directoryFile = psiDirectory.getVirtualFile();
            return (ProjectRootsUtil.isInSource(directoryFile, project) ||
                    ProjectRootsUtil.isInTestSource(directoryFile, project));
        }
        return false;
    }
}
