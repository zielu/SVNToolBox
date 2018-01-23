package zielu.svntoolbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.tmatesoft.svn.core.SVNURL;

@RunWith(Parameterized.class)
public class FileStatusEncodingTest {
  private SVNURL baseUrl;

  @Parameterized.Parameters(name = "URL={0}, branchName={1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "http://localhost:3690/repo/branches/%E6%97%BA%E7%A5%A8%E8%BF%90%E8%90%A5%E5" +
                "%B9%B3%E5%8F%B0",
            "旺票运营平台"
        },
        {
            "http://localhost:3690/repo/branches/master",
            "master"
        }
    });
  }

  private SVNURL branchUrl;
  private String expectedBranchName;

  public FileStatusEncodingTest(String branchUrl, String expectedBranchName) throws Exception {
    this.branchUrl = SVNURL.parseURIEncoded(branchUrl);
    this.expectedBranchName = expectedBranchName;
  }

  @Before
  public void before() throws Exception {
    baseUrl = SVNURL.parseURIEncoded("http://localhost:3690/repo");
  }

  @Test
  public void charactersAreDecoded() {
    FileStatus status = new FileStatus(baseUrl, branchUrl);
    assertThat(status.getBranchName()).contains(expectedBranchName);
  }
}