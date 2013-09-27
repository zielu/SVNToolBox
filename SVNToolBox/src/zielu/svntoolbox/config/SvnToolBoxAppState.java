/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * <p></p>
 * <br/>
 * <p>Created on 27.09.13</p>
 *
 * @author Lukasz Zielinski
 */
@State(
        name = "SvnToolBoxConfig",
        storages = {
                @Storage(
                        file = StoragePathMacros.APP_CONFIG + "/SvnToolBox.xml"
                )}
)
public class SvnToolBoxAppState implements PersistentStateComponent<SvnToolBoxAppState> {

    public static SvnToolBoxAppState getInstance() {
        return ApplicationManager.getApplication().getComponent(SvnToolBoxAppState.class);
    }

    @Nullable
    @Override
    public SvnToolBoxAppState getState() {
        return this;
    }

    @Override
    public void loadState(SvnToolBoxAppState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
