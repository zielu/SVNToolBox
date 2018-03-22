/* 
 * $Id$
 */
package zielu.svntoolbox;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.api.Url;

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
    private final Url myUrl;
    private final String myBranchName;

    private FileStatus() {
        myUnderVcs = false;
        myUrl = null;
        myBranchName = null;
    }

    public FileStatus(@NotNull Url url) {
        myUnderVcs = true;
        myUrl = url;
        myBranchName = null;
    }

    public FileStatus(@NotNull Url url, @Nullable String branchName) {
        myUnderVcs = true;
        myUrl = url;
        myBranchName = branchName;
    }

    public boolean isUnderVcs() {
        return myUnderVcs;
    }

    public Url getURL() {
        return myUrl;
    }

    public Optional<String> getBranchName() {
        return Optional.ofNullable(myBranchName);
    }
}
