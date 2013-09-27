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
    public static final ProjectViewStatus EMPTY = new ProjectViewStatus();

    private final String myBranchName;

    public ProjectViewStatus(String branchName) {
        this.myBranchName = branchName;
    }

    private ProjectViewStatus() {
        myBranchName = null;
    }

    public boolean isEmpty() {
        return myBranchName == null;
    }

    public String getBranchName() {
        return myBranchName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProjectViewStatus that = (ProjectViewStatus) o;

        if (myBranchName != null ? !myBranchName.equals(that.myBranchName) : that.myBranchName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return myBranchName != null ? myBranchName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("myBranchName", myBranchName)
                .toString();
    }
}
