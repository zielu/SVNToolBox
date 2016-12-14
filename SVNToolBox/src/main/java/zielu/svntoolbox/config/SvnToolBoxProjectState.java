/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.config;

import com.intellij.lifecycle.PeriodicalTasksCloser;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
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
        storages = {
                @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
                @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/SvnToolBox.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class SvnToolBoxProjectState implements PersistentStateComponent<SvnToolBoxProjectState> {
    public boolean showProjectViewModuleDecoration = true;
    public boolean showProjectViewSwitchedDecoration = true;

    public static SvnToolBoxProjectState getInstance(@NotNull Project project) {
        return PeriodicalTasksCloser.getInstance().safeGetService(project, SvnToolBoxProjectState.class);
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
