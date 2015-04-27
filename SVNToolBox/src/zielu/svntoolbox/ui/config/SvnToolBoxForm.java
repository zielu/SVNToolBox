/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.ui.config;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.CheckBoxWithColorChooser;
import zielu.svntoolbox.SvnToolBoxBundle;

import javax.swing.*;
import java.awt.Color;

/**
 * <p></p>
 * <br/>
 * <p>Created on 29.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnToolBoxForm {
    private JPanel content;
    private CheckBoxWithColorChooser regularColorChooser;
    private CheckBoxWithColorChooser darkColorChooser;
    private TextFieldWithBrowseButton csvFileChooser;

    public JComponent getContent() {
        return content;
    }

    protected void createUIComponents() {
        regularColorChooser = new CheckBoxWithColorChooser(SvnToolBoxBundle.getString("configurable.app.regularColor.text"));
        darkColorChooser = new CheckBoxWithColorChooser(SvnToolBoxBundle.getString("configurable.app.darkColor.text"));
        csvFileChooser = new TextFieldWithBrowseButton();
    }

    public TextFieldWithBrowseButton getCsvFile() {
        return csvFileChooser;
    }

    public void setCsvFile(TextFieldWithBrowseButton csvFileChooser) {
        this.csvFileChooser = csvFileChooser;
    }

    public boolean isRegularColorEnabled() {
        return regularColorChooser.isSelected();
    }

    public Color getRegularColor() {
        return regularColorChooser.getColor();
    }

    public boolean isDarkColorEnabled() {
        return darkColorChooser.isSelected();
    }

    public Color getDarkColor() {
        return darkColorChooser.getColor();
    }

    public void setRegularColorState(boolean enabled, Color color) {
        regularColorChooser.setSelected(enabled);
        regularColorChooser.setColor(color);
    }

    public void setDarkColorState(boolean enabled, Color color) {
        darkColorChooser.setSelected(enabled);
        darkColorChooser.setColor(color);
    }
}
