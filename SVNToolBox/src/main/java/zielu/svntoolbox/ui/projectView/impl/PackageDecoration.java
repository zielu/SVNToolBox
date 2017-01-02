/* 
 * $Id$
 */
package zielu.svntoolbox.ui.projectView.impl;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import zielu.svntoolbox.projectView.ProjectViewStatus;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.10.13</p>
 *
 * @author Lukasz Zielinski
 */
public class PackageDecoration extends AbstractNodeDecoration {

    @Override
    protected VirtualFile getVirtualFile(ProjectViewNode node) {
        return node.getVirtualFile();
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
        if (node instanceof PsiDirectoryNode) {
            PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
            final Project project = dirNode.getProject();
            final PsiDirectory psiDirectory = dirNode.getValue();
            final VirtualFile directoryFile = psiDirectory.getVirtualFile();
            return ProjectRootsUtil.isInSource(directoryFile, project) ||
                    ProjectRootsUtil.isInTestSource(directoryFile, project);
        }
        return false;
    }
}
