/* 
 * $Id$
 */
package zielu.svntoolbox.projectView;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import zielu.svntoolbox.SvnToolBoxBundle;

/**
 * <p></p>
 * <br/>
 * <p>Created on 24.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ProjectViewStatus {
    public static final ProjectViewStatus EMPTY = new ProjectViewStatus() {
        @Override
        public boolean equals(Object other) {
            return this == other;            
        }    
    };
    public static final ProjectViewStatus PENDING = new ProjectViewStatus(SvnToolBoxBundle.getString("status.svn.pending"), true) {
        @Override
        public boolean equals(Object other) {
            return this == other;            
        }        
    };
    public static final ProjectViewStatus NOT_CONFIGURED = new ProjectViewStatus(SvnToolBoxBundle.getString("status.svn.notConfigured"), false) {
        @Override
        public boolean equals(Object other) {
            return this == other;            
        }    
    };
    
    private final String myBranchName;
    private final boolean myTemporary;
    
    public ProjectViewStatus(String branchName) {
        this(branchName, false);
    }

    private ProjectViewStatus(String branchName, boolean temporary) {
        myBranchName = Preconditions.checkNotNull(branchName, "Null branch name");
        myTemporary = temporary;
    }
    
    private ProjectViewStatus() {
        myBranchName = null;
        myTemporary = false;
    }

    public boolean isEmpty() {
        return myBranchName == null;
    }

    public boolean isTemporary() {
        return myTemporary;
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
                .add("branchName", myBranchName)
                .add("temporary", myTemporary)
                .toString();
    }
}
