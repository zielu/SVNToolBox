/* 
 * $Id$
 */
package zielu.svntoolbox;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    public List<VirtualFile> filterUnderSvn(@NotNull Project project, Collection<VirtualFile> files) {
        List<VirtualFile> result = Lists.newArrayListWithCapacity(files.size());
        for (VirtualFile vFile : files) {
            if (filesUnderSvn(project, vFile)) {
                result.add(vFile);
            }
        }
        return result;
    }

    public boolean filesUnderSvn(@Nullable Project project, VirtualFile... vFiles) {
        if (project == null) {
            return false;
        }
        final SvnVcs svn = SvnVcs.getInstance(project);
        return filesUnderSvn(svn, project, vFiles);
    }

    public boolean filesUnderSvn(@NotNull SvnVcs svn, @Nullable Project project, VirtualFile... vFiles) {
        if (project == null) {
            return false;
        }
        boolean result = ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(svn, vFiles);
        if (result && vFiles != null) {
            for (VirtualFile vFile : vFiles) {
                if (!SvnStatusUtil.isUnderControl(project, vFile)) {
                    return false;
                }
            }
        }
        return result;
    }

    @NotNull
    public FileStatus statusFor(@Nullable Project project, @NotNull VirtualFile vFile) {
        if (project == null) {
            return new FileStatus();
        }
        SvnVcs svn = SvnVcs.getInstance(project);
        return statusFor(svn, project, vFile);
    }

    @NotNull
    public FileStatus statusFor(@NotNull SvnVcs svn, @NotNull Project project, @NotNull VirtualFile vFile) {
        if (filesUnderSvn(svn, project, vFile)) {
            File currentFile = new File(vFile.getPath());
            SVNURL fileUrl = SvnUtil.getUrl(svn, currentFile);
            if (fileUrl != null) {
                SVNInfo info = svn.getInfo(vFile);
                if (info != null) {
                    File wcRoot = info.getWorkingCopyRoot();
                    if (wcRoot != null) {
                        VirtualFile rootVf = SvnUtil.getVirtualFile(wcRoot.getPath());
                        SVNURL branch = SvnUtil.getBranchForUrl(svn, rootVf, fileUrl.toString());
                        return new FileStatus(fileUrl, branch);
                    } else {
                        return new FileStatus(fileUrl);
                    }
                } else {
                    return new FileStatus(fileUrl);
                }
            }
        }
        return new FileStatus();
    }
}
