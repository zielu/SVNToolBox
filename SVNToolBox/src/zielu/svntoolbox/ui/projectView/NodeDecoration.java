/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.ui.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.ModuleGroup;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeListManagerEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.idea.svn.SvnStatusUtil;
import org.jetbrains.idea.svn.SvnUtil;
import zielu.svntoolbox.FileStatus;
import zielu.svntoolbox.FileStatusCalculator;

import java.awt.Color;
import java.io.File;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public enum NodeDecoration {
    
    Module {
        @Override
        public String getName(ProjectViewNode node) {
            return null;
        }

        @Override
        protected VirtualFile getVirtualFile(ProjectViewNode node) {
            return node.getVirtualFile();
        }

        @Override
        protected boolean isSwitchedAware() {
            return false;
        }

        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {
            FileStatus status = statusCalc.statusFor(node.getProject(), getVirtualFile(node));
            if (status.getBranchName().isPresent()) {
                data.addText(formatBranchName(status.getBranchName().get()));
            }
        }
    },
    ContentRoot {
        @Override
        public String getName(ProjectViewNode node) {
            return node.getName();
        }

        @Override
        protected VirtualFile getVirtualFile(ProjectViewNode node) {
            return node.getVirtualFile();
        }

        @Override
        protected boolean isSwitchedAware() {
            return true;
        }

        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {
            FileStatus status = statusCalc.statusFor(node.getProject(), getVirtualFile(node));
            if (status.getBranchName().isPresent()) {
                data.addText(getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                data.addText(formatBranchName(status.getBranchName().get()));
            }
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

        @Override
        protected VirtualFile getVirtualFile(ProjectViewNode node) {
            return node.getVirtualFile();
        }

        @Override
        protected boolean isSwitchedAware() {
            return true;
        }

        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {
            FileStatus status = statusCalc.statusFor(node.getProject(), getVirtualFile(node));
            if (status.getBranchName().isPresent()) {
                data.addText(getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                data.addText(formatBranchName(status.getBranchName().get()));
            }
        }
    },
    File {
        @Override
        public String getName(ProjectViewNode node) {           
            PsiFileNode fileNode = (PsiFileNode) node;
            return fileNode.getValue().getName();           
        }

        @Override
        protected VirtualFile getVirtualFile(ProjectViewNode node) {
            PsiFileNode fileNode = (PsiFileNode) node;
            return fileNode.getVirtualFile();
        }

        @Override
        protected boolean isSwitchedAware() {
            return true;
        }

        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {
            FileStatus status = statusCalc.statusFor(node.getProject(), getVirtualFile(node));
            if (status.getBranchName().isPresent()) {
                data.addText(getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                data.addText(formatBranchName(status.getBranchName().get()));
            }
        }
    },
    ClassFile {
        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {
            FileStatus status = statusCalc.statusFor(node.getProject(), getVirtualFile(node));
            if (status.getBranchName().isPresent()) {
                data.addText(getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                data.addText(formatBranchName(status.getBranchName().get()));
            }
        }

        @Override
        public String getName(ProjectViewNode node) {
            ClassTreeNode classNode = (ClassTreeNode) node;
            return classNode.getPsiClass().getName();
        }

        @Override
        protected VirtualFile getVirtualFile(ProjectViewNode node) {
            ClassTreeNode classNode = (ClassTreeNode) node;
            return PsiUtilBase.getVirtualFile(classNode.getPsiClass());
        }

        @Override
        protected boolean isSwitchedAware() {
            return true;
        }
    },
    Other {
        @Override
        public String getName(ProjectViewNode node) {
            return null;
        }

        @Override
        protected VirtualFile getVirtualFile(ProjectViewNode node) {
            return null;
        }

        @Override
        protected boolean isSwitchedAware() {
            return false;
        }

        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {}
    }
    ;

    protected final FileStatusCalculator statusCalc = new FileStatusCalculator();
    protected final static Color BRANCH_COLOR = new JBColor(new Color(159, 107, 0), new Color(159, 107, 0));
    protected final static SimpleTextAttributes BRANCH_ATTRIBUTES = 
            new SimpleTextAttributes(SimpleTextAttributes.STYLE_SMALLER, BRANCH_COLOR);
    
    protected abstract void decorate(ProjectViewNode node, PresentationData data);
    public abstract String getName(ProjectViewNode node);
    protected abstract VirtualFile getVirtualFile(ProjectViewNode node);
    protected abstract boolean isSwitchedAware();
    
    protected ColoredFragment formatBranchName(String branchName) {
        return new ColoredFragment(" [Svn: "+branchName+"]", BRANCH_ATTRIBUTES);
    }
    
    protected boolean isUnderSvn(ProjectViewNode node) {
        VirtualFile vFile = getVirtualFile(node);
        if (vFile != null && SvnStatusUtil.isUnderControl(node.getProject(), vFile)) {
            return SvnUtil.isSvnVersioned(node.getProject(), new File(vFile.getPath()));
        }
        return false;
    }
    
    public void apply(ProjectViewNode node, PresentationData data) {
        if (isUnderSvn(node)) {
            if (isSwitchedAware()) {
                ChangeListManager manager = ChangeListManagerEx.getInstance(node.getProject());
                if (manager != null) {
                    if (manager.getStatus(getVirtualFile(node)).equals(
                            com.intellij.openapi.vcs.FileStatus.SWITCHED)) {
                        decorate(node, data);
                    }
                }
            } else {
                decorate(node, data);    
            }
        }
    }
    
    public static NodeDecoration fromNode(ProjectViewNode node) {
        if (node instanceof PsiDirectoryNode) {
            PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
            final Project project = dirNode.getProject();
            final PsiDirectory psiDirectory = dirNode.getValue();
            final VirtualFile directoryFile = psiDirectory.getVirtualFile();
            final Object parentValue = dirNode.getParent().getValue();
            //System.out.println("Parent: "+parentValue.getClass().getName());
            if (parentValue instanceof Project || parentValue instanceof ModuleGroup) {
                return NodeDecoration.Module; 
            } else if (ProjectRootsUtil.isModuleContentRoot(directoryFile, project) 
                    || ProjectRootsUtil.isSourceOrTestRoot(directoryFile, project)) {
                return NodeDecoration.ContentRoot;
            } else if (ProjectRootsUtil.isInSource(directoryFile, project) || 
                    ProjectRootsUtil.isInTestSource(directoryFile, project)) {
                return Package;
            }
        } else if (node instanceof ClassTreeNode) {
            ClassTreeNode classNode = (ClassTreeNode) node;
            if (classNode.isTopLevel()) {
                return NodeDecoration.ClassFile;
            }
        } else if (node instanceof PsiFileNode) {
            return NodeDecoration.File;
        }
        return NodeDecoration.Other;
    }
}
