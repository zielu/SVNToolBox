/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.ui.config;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import zielu.svntoolbox.SvnToolBoxBundle;
import zielu.svntoolbox.config.SvnToolBoxAppState;

import javax.swing.JComponent;

/**
 * <p></p>
 * <br/>
 * <p>Created on 27.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnToolBoxConfigurable extends BaseConfigurable {
    private SvnToolBoxForm form;
    private Project project;

    @Nls
    @Override
    public String getDisplayName() {
        return SvnToolBoxBundle.getString("configurable.app.displayName");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;  //TODO: auto-generated method implementation
    }

    private void initComponent() {
        if (form == null) {
            form = new SvnToolBoxForm();
        }
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        initComponent();
        SvnToolBoxAppState state = SvnToolBoxAppState.getInstance();
        form.setRegularColorState(state.customRegularColor, state.getRegularDecorationColor());
        form.setDarkColorState(state.customDarkColor, state.getDarkDecorationColor());

        form.getCsvFile().addBrowseFolderListener("",
                "",
                ProjectManager.getInstance().getDefaultProject(),
                FileChooserDescriptorFactory.createSingleFileDescriptor("csv"));

        return form.getContent();
    }

    @Override
    public void apply() throws ConfigurationException {
        initComponent();
        SvnToolBoxAppState state = SvnToolBoxAppState.getInstance();

        boolean changed = false;

        if(!state.getCsvFile().equalsIgnoreCase(form.getCsvFile().getText())){
            state.setCsvFile(form.getCsvFile().getText());
            changed = true;
        }

        if (state.checkRegularDecorationChanged(form.isRegularColorEnabled(), form.getRegularColor())) {
            state.setRegularDecorationColor(form.isRegularColorEnabled(), form.getRegularColor());
            changed = true;
        }
        if (state.checkDarkDecorationChanged(form.isDarkColorEnabled(), form.getDarkColor())) {
            state.setDarkDecorationColor(form.isDarkColorEnabled(), form.getDarkColor());
            changed = true;
        }

        if (changed) {
            state.fireSettingsChanged();
        }
    }

    @Override
    public boolean isModified() {
        boolean modified = false;
        SvnToolBoxAppState state = SvnToolBoxAppState.getInstance();

        if(!state.getCsvFile().equalsIgnoreCase(form.getCsvFile().getText())){
            modified = true;
        }
        if (state.checkRegularDecorationChanged(form.isRegularColorEnabled(), form.getRegularColor())) {
            modified = true;
        }
        if (state.checkDarkDecorationChanged(form.isDarkColorEnabled(), form.getDarkColor())) {
            modified = true;
        }
        return modified;
    }

    @Override
    public void reset() {
        initComponent();
        SvnToolBoxAppState state = SvnToolBoxAppState.getInstance();

        form.getCsvFile().setText(state.getCsvFile());
        form.setRegularColorState(state.customRegularColor, state.getRegularDecorationColor());
        form.setDarkColorState(state.customDarkColor, state.getDarkDecorationColor());
    }

    @Override
    public void disposeUIResources() {
        form = null;
    }

}
