/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.config;

import com.google.common.base.Strings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.ui.JBColor;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.svntoolbox.projectView.DecorationSettingsNotifier;

import java.awt.Color;

/**
 * <p></p>
 * <br/>
 * <p>Created on 27.09.13</p>
 *
 * @author Lukasz Zielinski
 */
@State(
        name = "SvnToolBoxAppConfig",
        storages = {
                @Storage(
                        id = "other",
                        file = StoragePathMacros.APP_CONFIG + "/SvnToolBox.xml"
                )
        }
)
public class SvnToolBoxAppState implements PersistentStateComponent<SvnToolBoxAppState>, ApplicationComponent {
    public boolean customRegularColor;
    public int regularR = 159;
    public int regularG = 107;
    public int regularB = 0;

    public boolean customDarkColor;
    public int darkR = 159;
    public int darkG = 107;
    public int darkB = 0;

    private String fileCsv;

    @Transient
    private static final Color defaultRegularColor = new Color(159, 107, 0);

    @Transient
    private static final Color defaultDarkColor = new Color(159, 107, 0);

    private static final SvnToolBoxAppState EMPTY = new SvnToolBoxAppState();

    public static SvnToolBoxAppState getInstance() {
        return ApplicationManager.getApplication().getComponent(SvnToolBoxAppState.class);
    }

    public String getCsvFile() {
        return Strings.nullToEmpty(fileCsv);
    }

    public void setCsvFile(String fileCsv) {
        this.fileCsv = fileCsv;
    }

    @Transient
    public Color getCurrentRegularDecorationColor() {
        if (customRegularColor) {
            return getRegularDecorationColor();
        } else {
            return defaultRegularColor;
        }
    }

    @Transient
    public Color getRegularDecorationColor() {
        return new Color(regularR, regularG, regularB);
    }

    @Transient
    public Color getCurrentDarkDecorationColor() {
        if (customDarkColor) {
            return getDarkDecorationColor();
        } else {
            return defaultDarkColor;
        }
    }

    @Transient
    public Color getDarkDecorationColor() {
        return new Color(darkR, darkG, darkB);
    }

    private boolean isChanged(Color newColor, int r, int g, int b) {
        return newColor.getRed() != r
                || newColor.getGreen() != g
                || newColor.getBlue() != b;
    }

    public void setRegularDecorationColor(boolean enabled, Color color) {
        customRegularColor = enabled;
        if (enabled) {
            regularR = color.getRed();
            regularG = color.getGreen();
            regularB = color.getBlue();
        }
    }

    public boolean checkRegularDecorationChanged(boolean enabled, Color color) {
        boolean changed = false;
        if (customRegularColor != enabled) {
            changed = true;
        }
        if (isChanged(color, regularR, regularG, regularB)) {
            changed = true;
        }
        return changed;
    }

    public void setDarkDecorationColor(boolean enabled, Color color) {
        customDarkColor = enabled;
        if (enabled) {
            darkR = color.getRed();
            darkG = color.getGreen();
            darkB = color.getBlue();
        }
    }

    public boolean checkDarkDecorationChanged(boolean enabled, Color color) {
        boolean changed = false;
        if (customDarkColor != enabled) {
            changed = true;
        }
        if (isChanged(color, darkR, darkG, darkB)) {
            changed = true;
        }
        return changed;
    }

    public void fireSettingsChanged() {
        ApplicationManager.getApplication().getMessageBus().
                syncPublisher(DecorationSettingsNotifier.TOGGLE_TOPIC).settingsChanged();
    }

    @Transient
    public JBColor getProjectViewDecorationColor() {
        return new JBColor(getCurrentRegularDecorationColor(), getCurrentDarkDecorationColor());
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

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return getClass().getSimpleName();
    }
}
