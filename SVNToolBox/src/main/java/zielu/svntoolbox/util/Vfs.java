/* 
 * @(#) $Id:  $
 */
package zielu.svntoolbox.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.vcs.changes.FilePathsHelper;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * <p></p>
 * <br/>
 * <p>Created on 27.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public enum Vfs {
    instance;

    public static List<VirtualFile> pathsToFiles(Collection<String> paths) {
        final Set<String> converted = new LinkedHashSet<String>();
        for (String path : paths) {
            converted.add(FilePathsHelper.convertPath(path));
        }
        LocalFileSystem fs = LocalFileSystem.getInstance();
        List<VirtualFile> vFiles = Lists.newArrayListWithCapacity(converted.size());
        for (String convert : converted) {
            VirtualFile vFile = fs.findFileByPath(convert);
            if (vFile != null) {
                vFiles.add(vFile);
            }
        }
        return vFiles;
    }
}
