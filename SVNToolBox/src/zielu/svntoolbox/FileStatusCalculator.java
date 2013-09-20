/* 
 * $Id$
 */
package zielu.svntoolbox;

import java.io.File;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnStatusUtil;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.SvnVcs;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNInfo;

/**
 * <p></p>
 * <br/>
 * <p>Created on 20.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class FileStatusCalculator {

    @NotNull
    public FileStatus statusFor(@NotNull SvnVcs svn, @NotNull Project project, @NotNull VirtualFile vFile) {
        if (SvnStatusUtil.isUnderControl(project, vFile)) {
            File currentFile = new File(vFile.getPath());
            SVNURL fileUrl = SvnUtil.getUrl(svn, currentFile);
            if (fileUrl != null) {
                SVNInfo info = svn.getInfo(vFile);
                if (info != null) {
                    VirtualFile rootVf = SvnUtil.getVirtualFile(info.getWorkingCopyRoot().getPath());
                    SVNURL branch = SvnUtil.getBranchForUrl(svn, rootVf, fileUrl.toString());
                    return new FileStatus(fileUrl, branch);
                } else {
                    return new FileStatus(fileUrl);
                }
            }
        }
        return new FileStatus();
    }
}
