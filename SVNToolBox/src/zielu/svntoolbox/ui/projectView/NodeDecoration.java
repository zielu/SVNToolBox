/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.ui.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.ModuleGroup;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode;
import com.intellij.ide.projectView.impl.nodes.ProjectViewDirectoryHelper;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nullable;
import zielu.svntoolbox.FileStatus;
import zielu.svntoolbox.FileStatusCalculator;
import zielu.svntoolbox.projectView.ProjectViewManager;
import zielu.svntoolbox.projectView.ProjectViewStatus;
import zielu.svntoolbox.projectView.ProjectViewStatusCache;
import zielu.svntoolbox.projectView.ProjectViewStatusCache.PutResult;
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
        protected void decorate(ProjectViewNode node, PresentationData data) {
            String branchName = getBranchNameAndCache(node);
            if (branchName != null) {
                data.addText(formatBranchName(branchName));
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
        protected void decorate(ProjectViewNode node, PresentationData data) {
            String branchName = getBranchNameAndCache(node);
            if (branchName != null) {
                addSmartText(data, getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                data.addText(formatBranchName(branchName));
            }
        }
    },
    Package {
        @Override
        public String getName(ProjectViewNode node) {
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
        protected void decorate(ProjectViewNode node, PresentationData data) {
            String branchName = getBranchNameAndCache(node);
            if (branchName != null) {
                addSmartText(data, getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                data.addText(formatBranchName(branchName));
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
        protected void decorate(ProjectViewNode node, PresentationData data) {
            String branchName = getBranchNameAndCache(node);
            if (branchName != null) {
                addSmartText(data, getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                data.addText(formatBranchName(branchName));
            }
        }
    },
    ClassFile {
        @Override
        protected void decorate(ProjectViewNode node, PresentationData data) {
            String branchName = getBranchNameAndCache(node);
            if (branchName != null) {
                addSmartText(data, getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                data.addText(formatBranchName(branchName));
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
        protected void decorate(ProjectViewNode node, PresentationData data) {
        }
    };

    protected final static Logger LOG = Logger.getInstance(NodeDecoration.class);

    protected final FileStatusCalculator myStatusCalc = new FileStatusCalculator();
    protected final static Color BRANCH_COLOR = new JBColor(new Color(159, 107, 0), new Color(159, 107, 0));
    protected final static SimpleTextAttributes BRANCH_ATTRIBUTES =
            new SimpleTextAttributes(SimpleTextAttributes.STYLE_SMALLER, BRANCH_COLOR);

    protected abstract void decorate(ProjectViewNode node, PresentationData data);

    public abstract String getName(ProjectViewNode node);

    protected abstract VirtualFile getVirtualFile(ProjectViewNode node);

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
        return new ColoredFragment(" [Svn: " + branchName + "]", BRANCH_ATTRIBUTES);
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

    public void apply(ProjectViewNode node, PresentationData data) {
        ProjectViewManager pvManager = ProjectViewManager.getInstance(node.getProject());
        if (isUnderSvn(node, pvManager)) {
            LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, "[" + pvManager.PV_SEQ.incrementAndGet() + "] Switched").start();
            decorate(node, data);
            watch.tick("Decoration");
        }
    }

    public static NodeDecoration fromNode(ProjectViewNode node) {
        ProjectViewManager pvManager = ProjectViewManager.getInstance(node.getProject());
        LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, "[" + pvManager.PV_SEQ.incrementAndGet() + "] Node type detection").start();

        NodeDecoration result = NodeDecoration.None;
        if (node instanceof PsiDirectoryNode) {
            PsiDirectoryNode dirNode = (PsiDirectoryNode) node;
            final Project project = dirNode.getProject();
            final PsiDirectory psiDirectory = dirNode.getValue();
            final VirtualFile directoryFile = psiDirectory.getVirtualFile();
            final Object parentValue = dirNode.getParent().getValue();
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
