/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.api.Url;
import zielu.svntoolbox.SvnToolBoxBundle;

/**
 * <p></p>
 * <br/>
 * <p>Created on 03.12.13</p>
 *
 * @author Lukasz Zielinski
 */
public class CopyFileUrlAction extends VirtualFileUnderSvnActionBase {

    public CopyFileUrlAction() {
        super(SvnToolBoxBundle.getString("action.copy.file.url"));
    }

    @Override
    protected void perform(AnActionEvent e, @NotNull Project project, @NotNull VirtualFile file) {
        SvnVcs svn = SvnVcs.getInstance(project);
        File currentFile = new File(file.getPath());
        Url fileUrl = SvnUtil.getUrl(svn, currentFile);
        if (fileUrl != null) {
            CopyPasteManager.getInstance().setContents(new StringSelection(fileUrl.toString()));
        }
    }
}
