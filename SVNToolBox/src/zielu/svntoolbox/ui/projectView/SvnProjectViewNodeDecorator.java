/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.ui.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;

/**
 * <p></p>
 * <br/>
 * <p>Created on 21.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class SvnProjectViewNodeDecorator implements ProjectViewNodeDecorator {
    @Override
    public void decorate(ProjectViewNode node, PresentationData data) {
        if (node != null) {
            NodeType type = NodeType.fromNode(node);
            System.out.println("Node: "+type+" "+node+" "+node.getClass().getName());
            switch (type) {
                case Module: {
                    data.addText(new ColoredFragment(" [Svn: branch]", 
                        new SimpleTextAttributes(SimpleTextAttributes.STYLE_SMALLER, JBColor.ORANGE)));
                    break;
                }
                case ContentRoot: {
                    data.addText(type.getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    data.addText(new ColoredFragment(" [Svn: branch]", 
                        new SimpleTextAttributes(SimpleTextAttributes.STYLE_SMALLER, JBColor.BLUE)));
                    break;    
                }
                case Package: {
                    data.addText(type.getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    data.addText(new ColoredFragment(" [Svn: branch]", 
                       new SimpleTextAttributes(SimpleTextAttributes.STYLE_SMALLER, JBColor.GREEN)));
                    break;    
                }
                case File: {
                    data.addText(type.getName(node), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    data.addText(new ColoredFragment(" [Svn: branch]", 
                       new SimpleTextAttributes(SimpleTextAttributes.STYLE_SMALLER, JBColor.yellow)));
                    break;    
                }
            }
        }
    }
    
    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {
        //TODO: auto-generated method implementation
    }
}
