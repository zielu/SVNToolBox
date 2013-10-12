/* 
 * $Id$
 */
package zielu.svntoolbox.extensions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.AbstractExtensionPointBean;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.util.xmlb.annotations.Attribute;
import zielu.svntoolbox.ui.projectView.NodeDecoration;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.10.13</p>
 *
 * @author Lukasz Zielinski
 */
public class NodeDecorationEP extends AbstractExtensionPointBean implements Comparable<NodeDecorationEP> {
    public final static ExtensionPointName<NodeDecorationEP> 
            POINT_NAME = ExtensionPointName.create("svntoolbox.nodeDecorationPoint");
    
    @Attribute("priority")
    public Integer priority;
    
    @Attribute("implementationClass")
    public String implementationClass;
    
    public NodeDecoration instantiate() {
        try {
            return instantiate(implementationClass, ApplicationManager.getApplication().getPicoContainer());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int compareTo(NodeDecorationEP o) {
        return priority.compareTo(o.priority);
    }
}
