package zielu.svntoolbox.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.branchConfig.BranchConfigurationDialog;

public class BranchConfigUi {
  private static final Logger LOG = Logger.getInstance(BranchConfigUi.class);

  private BranchConfigUi() {
    throw new IllegalStateException();
  }

  public static void configureBranches(@NotNull Project project, @NotNull VirtualFile file) {
    File root = SvnUtil.getWorkingCopyRoot(VfsUtilCore.virtualToIoFile(file));
    if (root != null) {
      VirtualFile rootFile = VfsUtil.findFileByIoFile(root, true);
      BranchConfigurationDialog.configureBranches(project, rootFile);
    } else {
      LOG.warn("No working copy root for file: " + file);
    }
  }
}
