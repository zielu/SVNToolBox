/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.projectView;

import com.intellij.util.messages.Topic;

/**
 * <p></p>
 * <br/>
 * <p>Created on 29.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public interface DecorationSettingsNotifier {
    Topic<DecorationSettingsNotifier> TOGGLE_TOPIC = Topic.create("Decoration Settings Change",
            DecorationSettingsNotifier.class);

    void settingsChanged();
}
