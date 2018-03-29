package zielu.svntoolbox.ui.actions;

import static com.google.common.collect.Iterables.getLast;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static zielu.svntoolbox.SvnToolBoxBundle.getString;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.common.base.Strings;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.JList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.api.Revision;
import org.jetbrains.idea.svn.commandLine.SvnBindException;
import org.jetbrains.idea.svn.info.Info;
import org.jetbrains.idea.svn.lock.Lock;
import zielu.svntoolbox.SvnToolBoxApp;
import zielu.svntoolbox.SvnToolBoxBundle;
import zielu.svntoolbox.lockinfo.SvnLockOwnerComponent;
import zielu.svntoolbox.util.FileBackgroundable;

/**
 * <p></p>
 * <br/>
 * <p>Created on 2015-05-18</p>
 *
 * @author BONO Adil
 * @author Lukasz Zielinski
 */
public class ShowLockInfoTask extends FileBackgroundable {
    private static final String FIELD_DELIMITER = " : ";
    private static final Splitter splitter = Splitter.on(FIELD_DELIMITER).trimResults();

  private final Logger LOG = Logger.getInstance(getClass());

    private Notification createNoLockNotification() {
        return SvnToolBoxApp.NOTIFICATION.createNotification(
            getString("configurable.app.svnlock.noinfo.title"),
            getString("configurable.app.svnlock.noinfo.content"),
            NotificationType.INFORMATION,
            null
        );
    }

    private void showNoLockNotification(Project project) {
        final Notification notification = createNoLockNotification();
      SvnToolBoxApp.getInstance().schedule(() -> UIUtil.invokeAndWaitIfNeeded((Runnable) notification::expire),
          5, TimeUnit.SECONDS);
        notification.notify(project);
    }

    public ShowLockInfoTask(@NotNull Project project, @NotNull VirtualFile file) {
        super(project, file, SvnToolBoxBundle.getString("action.show.lock.info"), false);
    }

    @Override
    public void run(ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        //get url
        SvnVcs svn = SvnVcs.getInstance(getProject());
        indicator.setText(getString("configurable.app.svnlock.noinfo.get.file.info") + getFile().getPath());
        Info fileInfo = svn.getInfo(getFile());
        if (fileInfo == null) {
            indicator.stop();
            return;
        }

        //get lock infos
        try {
            indicator.setText(getString("configurable.app.svnlock.noinfo.get.url.info") + fileInfo.getURL());
            Info urlInfo = svn.getInfo(fileInfo.getURL(), Revision.UNDEFINED);
            if (urlInfo == null) {
                indicator.stop();
                return;
            }

            List<String> datas = Lists.newArrayList();
            indicator.setText(getString("configurable.app.svnlock.noinfo.get.lock.info") + fileInfo.getURL());
            Lock lock = urlInfo.getLock();
            if (lock != null) {
                String owner = SvnLockOwnerComponent.getInstance().getOwner(lock.getOwner());
                datas.add(getString("configurable.app.svnlock.owner.label") + FIELD_DELIMITER + owner);
                datas.add(getString("configurable.app.svnlock.comment.label") + FIELD_DELIMITER + lock.getComment());
                datas.add(getString("configurable.app.svnlock.creation.label") + FIELD_DELIMITER + (lock.getCreationDate() != null ? lock.getCreationDate() : EMPTY));
                datas.add(getString("configurable.app.svnlock.expiration.label") + FIELD_DELIMITER + (lock.getExpirationDate() != null ? lock.getExpirationDate() : EMPTY));
              indicator.stop();
              showLockPopupInEdt(datas);
            } else {
              indicator.stop();
              showNoLockNotification(getProject());
            }
        } catch (SvnBindException sbe) {
          indicator.stop();
          LOG.warn("Could not get info for " + getFile(), sbe);
        }
    }

  private void showLockPopupInEdt(List<String> datas) {
    UIUtil.invokeLaterIfNeeded(() -> showLockPopup(datas));
  }

  private void showLockPopup(List<String> datas) {
    final JList<String> list = new JBList<>(datas);

    JBPopupFactory.getInstance()
        .createListPopupBuilder(list)
        .setTitle(getString("configurable.app.svnlock.title"))
        .setResizable(true)
        .setItemChoosenCallback(() -> itemChosen(list))
        .createPopup()
        .showInFocusCenter();
  }

  private void itemChosen(JList<String> list) {
    if (list.getSelectedIndices().length > 0) {
      String selectedValue = list.getSelectedValue();

      if (Strings.isNotBlank(selectedValue) && selectedValue.contains(FIELD_DELIMITER)) {
        CopyPasteManager.getInstance().setContents(new StringSelection(getLast(splitter.split(selectedValue))));
      }
    }
  }
}
