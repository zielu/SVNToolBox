/* 
 * $Id$
 */
package zielu.svntoolbox;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.SvnConfiguration;
import org.jetbrains.idea.svn.SvnStatusUtil;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.branchConfig.SvnBranchConfigurationManager;
import org.jetbrains.idea.svn.branchConfig.SvnBranchConfigurationNew;
import org.jetbrains.idea.svn.info.Info;
import org.tmatesoft.svn.core.SVNURL;
import zielu.svntoolbox.util.LogStopwatch;

/**
 * <p></p>
 * <br/>
 * <p>Created on 20.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class FileStatusCalculator {
    private final Logger LOG = Logger.getInstance(getClass());

    public List<VirtualFile> filterUnderSvn(@NotNull Project project, Collection<VirtualFile> files) {
        List<VirtualFile> result = Lists.newArrayListWithCapacity(files.size());
        for (VirtualFile vFile : files) {
            if (fastAllFilesUnderSvn(project, vFile)) {
                result.add(vFile);
            }
        }
        return result;
    }

    public boolean allFilesUnderSvn(@Nullable Project project, VirtualFile... vFiles) {
        if (project == null) {
            return false;
        }
        final SvnVcs svn = SvnVcs.getInstance(project);
        return allFilesUnderSvn(svn, project, vFiles);
    }

    public boolean allFilesUnderSvn(@NotNull SvnVcs svn, @Nullable Project project, VirtualFile... vFiles) {
        return allFilesUnderSvnImpl(svn, project, false, vFiles);
    }
    
    public boolean fastAllFilesUnderSvn(@Nullable Project project, VirtualFile... vFiles) {
        if (project == null) {
            return false;
        }
        final SvnVcs svn = SvnVcs.getInstance(project);
        return fastAllFilesUnderSvn(svn, project, vFiles);
    }
    
    public boolean fastAllFilesUnderSvn(@NotNull SvnVcs svn, @Nullable Project project, VirtualFile... vFiles) {
        return allFilesUnderSvnImpl(svn, project, true, vFiles);    
    }
    
    private boolean allFilesUnderSvnImpl(@NotNull SvnVcs svn, @Nullable Project project, boolean fast, VirtualFile... vFiles) {
        if (project == null) {
            return false;
        }
        boolean result = ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(svn, vFiles);
        if (result && vFiles != null) {
            for (VirtualFile vFile : vFiles) {
                if (!SvnStatusUtil.isUnderControl(project, vFile)) {
                    return false;
                } else if (!fast) {
                    if (!getWCRoot(VfsUtilCore.virtualToIoFile(vFile)).isPresent()) {
                        return false;
                    }
                }
            }
        }
        return result;
    }
    
    @NotNull
    public FileStatus statusFor(@Nullable Project project, @NotNull VirtualFile vFile) {
        if (project == null) {
            return FileStatus.EMPTY;
        }
        SvnVcs svn = SvnVcs.getInstance(project);
        return statusFor(svn, project, vFile);        
    }

    private Optional<VirtualFile> getWCRoot(File currentFile) {
        //TODO: there is also #getWorkingCopyRootNew for Svn 1.8
        File root = SvnUtil.getWorkingCopyRootNew(currentFile);
        if (root == null) {
            LOG.debug("WC root not found for: file="+currentFile.getAbsolutePath());
            return Optional.absent();
        } else {
            VirtualFile rootVf = SvnUtil.getVirtualFile(root.getPath());
            assert rootVf != null: "Root VF not found for: "+root.getPath();
            return Optional.of(rootVf); 
        } 
    }
    
    private Optional<FileStatus> statusForCli(Project project, SVNURL fileUrl, File currentFile) {
        LogStopwatch watch = LogStopwatch.debugStopwatch(LOG, SvnToolBoxProject.getInstance(project).sequence(),
                Suppliers.ofInstance("Status For Cli")).start();
        Optional<VirtualFile> root = getWCRoot(currentFile);
        watch.tick("WC Root");
        if (root.isPresent()) {
            try {
                SvnBranchConfigurationManager branchManager = SvnBranchConfigurationManager.getInstance(project);
                watch.tick("Root VF by File");
                SvnBranchConfigurationNew branchConfig = branchManager.get(root.get());
                watch.tick("Branch Config");
                String fileUrlPath = fileUrl.toString();
                String baseName = branchConfig.getBaseName(fileUrlPath);
                watch.tick("Base Name");
                watch.stop();
                return Optional.of(new FileStatus(fileUrl, baseName));
            } catch (VcsException e) {
                LOG.error("Could not get branch configuration", e);
            }            
        } else {
            watch.stop();
        }
        return Optional.absent();
    }
    
    private Optional<FileStatus> statusForSvnKit(Info info, SvnVcs svn, SVNURL fileUrl, File currentFile) {
        Optional<VirtualFile> root = getWCRoot(currentFile);
        if (root.isPresent()) {
            SVNURL branch = SvnUtil.getBranchForUrl(svn, root.get(), fileUrl.toString());
            return Optional.of(new FileStatus(fileUrl, branch));
        }
        return Optional.absent();
    }
    
    @NotNull
    public FileStatus statusFor(@NotNull SvnVcs svn, @NotNull Project project, @NotNull VirtualFile vFile) {
        File currentFile = VfsUtilCore.virtualToIoFile(vFile);
        SVNURL fileUrl = SvnUtil.getUrl(svn, currentFile);
        if (fileUrl != null) {
            Info info = svn.getInfo(vFile);
            if (info != null) {
                SvnConfiguration svnConfig = SvnConfiguration.getInstance(project);
                Optional<FileStatus> status;
                if (svnConfig.isCommandLine()) {
                    status = statusForCli(project, fileUrl, currentFile);                    
                } else {
                    status = statusForSvnKit(info, svn, fileUrl, currentFile);                    
                }
                if (status.isPresent()) {
                    return status.get();
                }
            } else {
                return new FileStatus(fileUrl);
            }
        }
        return FileStatus.EMPTY;
    }
}
