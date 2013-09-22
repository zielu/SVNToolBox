/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.projectView;

import com.intellij.util.messages.Topic;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public interface DecorationToggleNotifier {
    Topic<DecorationToggleNotifier> TOGGLE_TOPIC = Topic.create("Toggle decorations", DecorationToggleNotifier.class);
    
    void decorationChanged();
}
