/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.09.13</p>
 *
 * @author Lukasz Zielinski
 */
@State(
    name = "SvnToolBoxConfig",
    storages = @Storage("SvnToolBox.xml")
)
public class SvnToolBoxProjectState implements PersistentStateComponent<SvnToolBoxProjectState> {
    public boolean showProjectViewModuleDecoration = true;
    public boolean showProjectViewSwitchedDecoration = true;

    public static SvnToolBoxProjectState getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, SvnToolBoxProjectState.class);
    }

    public boolean showingAnyDecorations() {
        return showProjectViewModuleDecoration || showProjectViewSwitchedDecoration;
    }

    @Nullable
    @Override
    public SvnToolBoxProjectState getState() {
        return this;
    }

    @Override
    public void loadState(SvnToolBoxProjectState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
