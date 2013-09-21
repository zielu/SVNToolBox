/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.ui.projectView;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public enum NodeType {
    Module {
        @Override
        public String getName(ProjectViewNode node) {
            return null;
        }
    },
    ContentRoot {
        @Override
        public String getName(ProjectViewNode node) {
            return node.getName();
        }
    },
    Package {
        @Override
        public String getName(ProjectViewNode node) {
            PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
            final PsiDirectory psiDirectory = dirNode.getValue();
            final VirtualFile directoryFile = psiDirectory.getVirtualFile();
            return directoryFile.getName();
        }
    },
    File {
        @Override
        public String getName(ProjectViewNode node) {
            if (node instanceof PsiFileNode) {
                PsiFileNode fileNode = (PsiFileNode) node;
                return fileNode.getValue().getName();
            } else if (node instanceof ClassTreeNode) {
                ClassTreeNode classNode = (ClassTreeNode) node;
                return classNode.getPsiClass().getName();
            }
            return null;
        }
    },
    Other {
        @Override
        public String getName(ProjectViewNode node) {
            return null;
        }
    }
    ;

    public abstract String getName(ProjectViewNode node);
    
    public static NodeType fromNode(ProjectViewNode node) {
        if (node instanceof PsiDirectoryNode) {
            PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
            final Project project = dirNode.getProject();
            final PsiDirectory psiDirectory = dirNode.getValue();
            final VirtualFile directoryFile = psiDirectory.getVirtualFile();
            final Object parentValue = dirNode.getParent().getValue();
            if (parentValue instanceof Project) {
                return NodeType.Module; 
            } else if (ProjectRootsUtil.isModuleContentRoot(directoryFile, project) 
                    || ProjectRootsUtil.isSourceOrTestRoot(directoryFile, project)) {
                return NodeType.ContentRoot;
            } else if (ProjectRootsUtil.isInSource(directoryFile, project) || 
                    ProjectRootsUtil.isInTestSource(directoryFile, project)) {
                return NodeType.Package;
            }
        } else if (node instanceof ClassTreeNode || node instanceof PsiFileNode) {
            //ClassTreeNode classNode = (ClassTreeNode) node;
            return NodeType.File;
        }
        return NodeType.Other;
    }
}
