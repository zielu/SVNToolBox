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
    private final boolean myUnderVcs;
    private final SVNURL myUrl;
    private final Optional<SVNURL> myBranch;
    private final Optional<String> myBranchName;
    private final Optional<String> myBranchDirectory;

    public FileStatus() {
        myUnderVcs = false;
        myUrl = null;
        myBranch = Optional.absent();
        myBranchName = Optional.absent();
        myBranchDirectory = Optional.absent();
    }

    public FileStatus(@NotNull SVNURL url) {
        myUnderVcs = true;
        this.myUrl = url;
        myBranch = Optional.absent();
        myBranchName = Optional.absent();
        myBranchDirectory = Optional.absent();
    }

    public FileStatus(@NotNull SVNURL url, @Nullable SVNURL branch) {
        myUnderVcs = true;
        this.myUrl = url;
        this.myBranch = Optional.fromNullable(branch);
        if (branch != null) {
            String[] parts = branch.toString().split("/");
            if (parts.length > 1) {
                myBranchDirectory = Optional.of(parts[parts.length - 2]);
            } else {
                myBranchDirectory = Optional.absent();
            }
            if (parts.length > 0) {
                myBranchName = Optional.of(parts[parts.length - 1]);
            } else {
                myBranchName = Optional.absent();
            }
        } else {
            myBranchName = Optional.absent();
            myBranchDirectory = Optional.absent();
        }
    }

    public boolean isUnderVcs() {
        return myUnderVcs;
    }

    public SVNURL getURL() {
        return myUrl;
    }

    public Optional<SVNURL> getBranch() {
        return myBranch;
    }

    public Optional<String> getBranchName() {
        return myBranchName;
    }

    public Optional<String> getBranchDirectory() {
        return myBranchDirectory;
    }
}
