/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.ui.config;

import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import zielu.svntoolbox.SvnToolBoxBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * <p></p>
 * <br/>
 * <p>Created on 27.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnToolBoxConfigurable extends BaseConfigurable {
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

    @Nullable
    @Override
    public JComponent createComponent() {
        return new JPanel();
    }

    @Override
    public void apply() throws ConfigurationException {
        //TODO: auto-generated method implementation
    }

    @Override
    public void reset() {
        //TODO: auto-generated method implementation
    }

    @Override
    public void disposeUIResources() {
        //TODO: auto-generated method implementation
    }

}
