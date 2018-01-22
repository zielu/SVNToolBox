/* 
 * $Id$
 */
package zielu.svntoolbox;

import java.util.Optional;
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
    public static final FileStatus EMPTY = new FileStatus();
    public static final Optional<FileStatus> EMPTY_OPTIONAL = Optional.of(EMPTY);
    
    private final boolean myUnderVcs;
    private final SVNURL myUrl;
    private final String myBranchName;
    private final String myBranchDirectory;

    private FileStatus() {
        myUnderVcs = false;
        myUrl = null;
        myBranchName = null;
        myBranchDirectory = null;
    }

    public FileStatus(@NotNull SVNURL url) {
        myUnderVcs = true;
        myUrl = url;
        myBranchName = null;
        myBranchDirectory = null;
    }

    public FileStatus(@NotNull SVNURL url, @Nullable String branchName) {
        myUnderVcs = true;
        myUrl = url;
        myBranchName = branchName;
        myBranchDirectory = null;
    }

    public FileStatus(@NotNull SVNURL url, @Nullable SVNURL branch) {
        myUnderVcs = true;
        myUrl = url;
        if (branch != null) {
            String[] parts = branch.toDecodedString().split("/");
            if (parts.length > 1) {
                myBranchDirectory = parts[parts.length - 2];
            } else {
                myBranchDirectory = null;
            }
            if (parts.length > 0) {
                myBranchName = parts[parts.length - 1];
            } else {
                myBranchName = null;
            }
        } else {
            myBranchName = null;
            myBranchDirectory = null;
        }
    }

    public boolean isUnderVcs() {
        return myUnderVcs;
    }

    public SVNURL getURL() {
        return myUrl;
    }

    public Optional<String> getBranchName() {
        return Optional.ofNullable(myBranchName);
    }

    public Optional<String> getBranchDirectory() {
        return Optional.ofNullable(myBranchDirectory);
    }
}
