/* 
 * $Id$
 */
package zielu.svntoolbox.projectView;

import com.google.common.base.Objects;

/**
 * <p></p>
 * <br/>
 * <p>Created on 24.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ProjectViewStatus {
    private final String branchName;
    private final Boolean switched;

    public ProjectViewStatus(String branchName, boolean switched) {
        this.branchName = branchName;
        this.switched = switched;
    }

    public ProjectViewStatus(String branchName) {
        this.branchName = branchName;
        switched = null;
    }

    public ProjectViewStatus() {
        branchName = null;
        switched = null;
    }

    public boolean isEmpty() {
        return branchName == null && switched == null;
    }

    public String getBranchName() {
        return branchName;
    }

    public boolean hasSwitchedInfo() {
        return switched != null;
    }

    public boolean isSwitched() {
        return switched;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("branchName", branchName)
                .add("switched", switched)
                .toString();
    }
}
