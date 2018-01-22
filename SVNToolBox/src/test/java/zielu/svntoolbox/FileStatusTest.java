package zielu.svntoolbox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNURL;

public class FileStatusTest {
  private SVNURL baseUrl;
  private SVNURL branchUrl;

  @Before
  public void before() throws Exception {
    baseUrl = SVNURL.parseURIEncoded("http://localhost:3690/repo");
    branchUrl = SVNURL.parseURIEncoded("http://localhost:3690/repo/branches/%E6%97%BA%E7%A5%A8%E8%BF%90%E8%90%A5%E5" +
        "%B9%B3%E5%8F%B0");
  }

  @Test
  public void chineseCharactersAreDecoded() {
    FileStatus status = new FileStatus(baseUrl, branchUrl);
    assertThat(status.getBranchName()).contains("旺票运营平台");
  }
}