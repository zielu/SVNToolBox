/* 
 * $Id$
 */
package zielu.svntoolbox.async;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * <p></p>
 * <br/>
 * <p>Created on 05.12.13</p>
 *
 * @author Lukasz Zielinski
 */
public class StatusRequest {
    public final Project project;
    public final VirtualFile file;

    public StatusRequest(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;
    }
}
