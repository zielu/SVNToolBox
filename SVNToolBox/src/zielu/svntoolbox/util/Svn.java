/* 
 * $Id$
 */
package zielu.svntoolbox.util;

import java.io.File;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnBranchConfigurationManager;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.branchConfig.SvnBranchConfigurationNew;

/**
 * <p></p>
 * <br/>
 * <p>Created on 16.01.14</p>
 *
 * @author Lukasz Zielinski
 */
public enum Svn {
    instance;

    /**
     * 
     * 
     * Assumption is made that file is under SVN
     * 
     * @param project
     * @param currentFile
     * @return
     * @throws VcsException
     */
    @NotNull    
    public static SvnBranchConfigurationNew getBranchConfig(@NotNull Project project, @NotNull File currentFile) throws VcsException {
        SvnBranchConfigurationManager branchManager = SvnBranchConfigurationManager.getInstance(project);
        //TODO: there is also #getWorkingCopyRootNew for Svn 1.8
        File root = SvnUtil.getWorkingCopyRoot(currentFile);
        return branchManager.get(VfsUtil.findFileByIoFile(root, false));
    }
    
    /**
     * 
     * 
     * Assumption is made that file is under SVN
     * 
     * @param project
     * @param currentFile
     * @return
     * @throws VcsException
     */
    @NotNull    
    public static SvnBranchConfigurationNew getBranchConfig(@NotNull Project project, @NotNull VirtualFile currentFile) throws VcsException {
        return getBranchConfig(project, new File(currentFile.getPath()));
    }
}
