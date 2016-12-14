/* 
 * $Id$
 */
package zielu.svntoolbox.ui.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.10.13</p>
 *
 * @author Lukasz Zielinski
 */
public interface NodeDecoration {
    boolean isForMe(ProjectViewNode node);
    NodeDecorationType getType();
    void decorate(ProjectViewNode node, PresentationData data);
}
