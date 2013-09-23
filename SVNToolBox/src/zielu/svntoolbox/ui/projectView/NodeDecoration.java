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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.idea.svn.SvnStatusUtil;
import zielu.svntoolbox.FileStatus;
import zielu.svntoolbox.FileStatusCalculator;
import zielu.svntoolbox.util.LogStopwatch;

import java.awt.Color;

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
        protected boolean isAppliedOnlyForSwitched() {
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
        protected boolean isAppliedOnlyForSwitched() {
            return true;
        }

        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {
            FileStatus status = statusCalc.statusFor(node.getProject(), getVirtualFile(node));
            if (status.getBranchName().isPresent()) {
                addSmartText(data, getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
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
        protected boolean isAppliedOnlyForSwitched() {
            return true;
        }

        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {
            FileStatus status = statusCalc.statusFor(node.getProject(), getVirtualFile(node));
            if (status.getBranchName().isPresent()) {
                addSmartText(data, getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
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
        protected boolean isAppliedOnlyForSwitched() {
            return true;
        }

        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {
            FileStatus status = statusCalc.statusFor(node.getProject(), getVirtualFile(node));
            if (status.getBranchName().isPresent()) {
                addSmartText(data, getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                data.addText(formatBranchName(status.getBranchName().get()));
            }
        }
    },
    ClassFile {
        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {
            FileStatus status = statusCalc.statusFor(node.getProject(), getVirtualFile(node));
            if (status.getBranchName().isPresent()) {
                addSmartText(data, getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
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
        protected boolean isAppliedOnlyForSwitched() {
            return true;
        }
    },
    None {
        @Override
        public String getName(ProjectViewNode node) {
            return null;
        }

        @Override
        protected VirtualFile getVirtualFile(ProjectViewNode node) {
            return null;
        }

        @Override
        protected boolean isAppliedOnlyForSwitched() {
            return false;
        }

        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {}
    }
    ;

    protected final static Logger LOG = Logger.getInstance(NodeDecoration.class);
    
    protected final FileStatusCalculator statusCalc = new FileStatusCalculator();
    protected final static Color BRANCH_COLOR = new JBColor(new Color(159, 107, 0), new Color(159, 107, 0));
    protected final static SimpleTextAttributes BRANCH_ATTRIBUTES = 
            new SimpleTextAttributes(SimpleTextAttributes.STYLE_SMALLER, BRANCH_COLOR);
    
    protected abstract void decorate(ProjectViewNode node, PresentationData data);
    public abstract String getName(ProjectViewNode node);
    protected abstract VirtualFile getVirtualFile(ProjectViewNode node);
    protected abstract boolean isAppliedOnlyForSwitched();
    
    protected void addSmartText(PresentationData data, String text, SimpleTextAttributes attributes) {
        boolean add = true;
        for (ColoredFragment existing : data.getColoredText()) {
            if(existing.getText().equals(text)) {
                add = false;
            }
        }
        if (add) {
            data.addText(text, attributes);
        }
    }
    
    protected ColoredFragment formatBranchName(String branchName) {
        return new ColoredFragment(" [Svn: "+branchName+"]", BRANCH_ATTRIBUTES);
    }
    
    protected boolean isUnderSvn(ProjectViewNode node) {
        LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, "Under SVN").start();
        VirtualFile vFile = getVirtualFile(node);
        watch.tick("Get VFile");
        boolean result = false;                
        if (vFile != null) {
            boolean underControl = SvnStatusUtil.isUnderControl(node.getProject(), vFile);
            watch.tick("Under control");
            result = underControl;
        }
        return result;
    }
    
    public void apply(ProjectViewNode node, PresentationData data) {        
        if (isUnderSvn(node)) {
            if (isAppliedOnlyForSwitched()) {
                LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, "Switched").start();
                ChangeListManager manager = ChangeListManager.getInstance(node.getProject());
                if (manager != null) {
                    com.intellij.openapi.vcs.FileStatus status = manager.getStatus(getVirtualFile(node));
                    watch.tick("Check status");
                    if (status.equals(
                            com.intellij.openapi.vcs.FileStatus.SWITCHED)) {
                        decorate(node, data);
                        watch.tick("Decoration");
                    }
                }
            } else {
                decorate(node, data);    
            }
        }
    }
    
    public static NodeDecoration fromNode(ProjectViewNode node) {
        LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, "Node type detection").start();
        
        NodeDecoration result = NodeDecoration.None; 
        if (node instanceof PsiDirectoryNode) {
            PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
            final Project project = dirNode.getProject();
            final PsiDirectory psiDirectory = dirNode.getValue();
            final VirtualFile directoryFile = psiDirectory.getVirtualFile();
            final Object parentValue = dirNode.getParent().getValue();
            //System.out.println("Parent: "+parentValue.getClass().getName());
            if (parentValue instanceof Project || parentValue instanceof ModuleGroup) {
                result = NodeDecoration.Module;
            } else if (ProjectRootsUtil.isModuleContentRoot(directoryFile, project) 
                    || ProjectRootsUtil.isSourceOrTestRoot(directoryFile, project)) {
                result = NodeDecoration.ContentRoot;
            } else if (ProjectRootsUtil.isInSource(directoryFile, project) || 
                    ProjectRootsUtil.isInTestSource(directoryFile, project)) {
                result = Package;
            }
        } else if (node instanceof ClassTreeNode) {
            ClassTreeNode classNode = (ClassTreeNode) node;
            if (classNode.isTopLevel()) {
                result = NodeDecoration.ClassFile;
            }
        } else if (node instanceof PsiFileNode) {
            result = NodeDecoration.File;
        }
        
        watch.stop();
        
        return result;
    }
}
