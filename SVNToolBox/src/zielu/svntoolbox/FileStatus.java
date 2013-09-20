/* 
 * $Id$
 */
package zielu.svntoolbox;

import com.google.common.base.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmatesoft.svn.core.SVNURL;

/**
 * <p></p>
 * <br/>
 * <p>Created on 20.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class FileStatus {
    private final boolean underVcs;
    private final SVNURL url;
    private final Optional<SVNURL> branch; 
    private final Optional<String> branchName;
    private final Optional<String> branchDirectory;
    
    public FileStatus() {
        underVcs = false;
        url = null;
        branch = Optional.absent();
        branchName = Optional.absent();
        branchDirectory = Optional.absent();
    }
    
    public FileStatus(@NotNull SVNURL url) {
        underVcs = true;
        this.url = url;
        branch = Optional.absent();
        branchName = Optional.absent();
        branchDirectory = Optional.absent();
    }    
    
    public FileStatus(@NotNull SVNURL url, @Nullable SVNURL branch) {
        underVcs = true;
        this.url = url;
        this.branch = Optional.fromNullable(branch);
        if (branch != null) {
            String[] parts = branch.toString().split("/");
            if (parts.length > 1) {
                branchDirectory = Optional.of(parts[parts.length - 2]);                
            } else {
                branchDirectory = Optional.absent();
            }
            if (parts.length > 0) {                
                branchName = Optional.of(parts[parts.length-1]);                
            } else {
                branchName = Optional.absent();
            }               
        } else {
            branchName = Optional.absent();
            branchDirectory = Optional.absent();                    
        }
    }
    
    public boolean isUnderVcs() {
        return underVcs;        
    }
    
    public SVNURL getURL() {
        return url;        
    }

    public Optional<SVNURL> getBranch() {
        return branch;
    }

    public Optional<String> getBranchName() {
        return branchName;
    }

    public Optional<String> getBranchDirectory() {
        return branchDirectory;
    }
}
