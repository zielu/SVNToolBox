package zielu.svntoolbox.ui.actions;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.jgoodies.common.base.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.commandLine.SvnBindException;
import org.jetbrains.idea.svn.info.Info;
import org.jetbrains.idea.svn.lock.Lock;
import org.tmatesoft.svn.core.wc.SVNRevision;
import zielu.svntoolbox.config.SvnToolBoxAppState;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Iterables.getLast;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static zielu.svntoolbox.SvnToolBoxBundle.getString;

/**
 * <p></p>
 * <br/>
 * <p>Created on 22.04.15</p>
 *
 * @author BONO Adil
 */
public class ShowLockInfoAction extends VirtualFileUnderSvnActionBase {

    public ShowLockInfoAction() {
        super(getString("action.show.lock.info"));
    }

    private static final String FIELD_DELIMITER = " : ";
    private static final Splitter splitter = Splitter.on(FIELD_DELIMITER).trimResults();

    @Override
    protected void perform(AnActionEvent e, @NotNull Project project, @NotNull VirtualFile file) {
        //get url
        SvnVcs svn = SvnVcs.getInstance(project);
        Info fileInfo = svn.getInfo(file);
        if (fileInfo == null) {
            return ;
        }

        //get lock infos
        try {
            Info urlInfo = svn.getInfo(fileInfo.getURL(), SVNRevision.UNDEFINED);
            if(urlInfo == null)
                return;

            List<String> datas = Lists.newArrayList();

            Lock lock = urlInfo.getLock();
            if(lock != null) {
                datas.add(getString("configurable.app.svnlock.owner.label") + FIELD_DELIMITER + getOwnerCSVDetail(lock.getOwner()));
                datas.add(getString("configurable.app.svnlock.comment.label") + FIELD_DELIMITER + lock.getComment());
                datas.add(getString("configurable.app.svnlock.creation.label") + FIELD_DELIMITER + (lock.getCreationDate() != null ? lock.getCreationDate() : EMPTY));
                datas.add(getString("configurable.app.svnlock.expiration.label") + FIELD_DELIMITER + (lock.getExpirationDate() != null ? lock.getExpirationDate() : EMPTY));
            }

            final JList list = new JBList(datas);

            JBPopupFactory.getInstance()
                .createListPopupBuilder(list)
                .setTitle(getString("configurable.app.svnlock.title"))
                .setResizable(true)
                .setItemChoosenCallback(new Runnable() {
                        public void run() {
                            if (list.getSelectedIndices().length > 0) {
                                String selectedValue = (String) list.getSelectedValue();

                                if (Strings.isNotBlank(selectedValue) && selectedValue.contains(FIELD_DELIMITER)) {
                                    CopyPasteManager.getInstance().setContents(new StringSelection(getLast(splitter.split(selectedValue))));
                                }
                            }
                        }
                    }
                ).createPopup().showInFocusCenter();


        } catch (SvnBindException sbe) {
            sbe.printStackTrace();
            return;
        }
    }

    private String getOwnerCSVDetail(String owner) {
        if(Strings.isBlank(owner))
            return EMPTY;

        String csvFile = SvnToolBoxAppState.getInstance().getCsvFile();
        if(Strings.isBlank(csvFile))
            return owner;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(csvFile));
            String line;
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] users = line.split(";");

                if(owner.equalsIgnoreCase(users[0])) {
                    return users[0] + " - " + users[1];
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return owner;
    }

}
