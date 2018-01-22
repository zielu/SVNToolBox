/* 
 * $Id$
 */
package zielu.svntoolbox.projectView;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Cache makes fallowing assumptions:
 * <ul>
 * <li>file data is added top-down i.e. first parent then children</li>
 * </ul>
 * </p>
 * <br/>
 * <p>Created on 24.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ProjectViewStatusCache implements Disposable {
    private final Logger LOG = Logger.getInstance(getClass());

    //TODO: maybe use ConcurrentSkipListMap ?? if so remember that current size must be maintained externally
    //see class docs for explanation 
    private final Map<String, ProjectViewStatus> myDirBranchesCache = new ConcurrentHashMap<String, ProjectViewStatus>();
    private final Map<String, ProjectViewStatus> myFileBranchesCache = new ConcurrentHashMap<String, ProjectViewStatus>();

    private final AtomicBoolean myActive = new AtomicBoolean(true);

    private final Supplier<Integer> SEQ;

    public ProjectViewStatusCache(Supplier<Integer> seq) {
        SEQ = seq;
    }

    @Nullable
    private String getKeyFor(@Nullable VirtualFile vFile) {
        return vFile != null ? vFile.getPath() : null;
    }
    
    private Map<String, ProjectViewStatus> getCacheFor(VirtualFile vFile) {
        if (vFile.isDirectory()) {
            return myDirBranchesCache;
        } else {
            return myFileBranchesCache;
        }
    }

    private String getCacheReport() {
        return "dirCacheSize=" + myDirBranchesCache.size() + ", fileCacheSize=" + myFileBranchesCache.size();
    }

    @Nullable
    public ProjectViewStatus get(VirtualFile file) {
        if (myActive.get()) {
            ProjectViewStatus status = getCacheFor(file).get(getKeyFor(file));
            if (status != null && LOG.isDebugEnabled()) {
                LOG.debug("[" + SEQ.get() + "] Found cached status for " + file.getPath() + ", " + getCacheReport() + ", status=" + status);
            }
            return status;
        }
        return null;
    }

    /**
     * Add new file status. 
     *
     * Final status may be different than one passed and should be read from operation result.
     *
     * @param file      file to add
     * @param candidate proposed status
     * @return operation result or <code>null</code> when cache is disposed
     */
    @Nullable
    public PutResult add(VirtualFile file, ProjectViewStatus candidate) {
        if (myActive.get()) {
            if (!candidate.isEmpty() && isFirstNotEmptyParentStatusEqualTo(file, candidate)) {
                //relevant status already cached for parent
                //net result is that annotations are shown only for switched roots and not their children
                candidate = ProjectViewStatus.EMPTY;
            }
            ProjectViewStatus oldStatus = getCacheFor(file).put(getKeyFor(file), candidate);
            if (LOG.isDebugEnabled()) {
                LOG.debug("[" + SEQ.get() + "] Cached candidate for " + file.getPath() +
                        ", cacheAfter=[" + getCacheReport() + "], new=" + candidate + ", previous=" + oldStatus);
            }
            return new PutResult(candidate, oldStatus);
        }
        return null;
    }

    private boolean isFirstNotEmptyParentStatusEqualTo(VirtualFile vFile, ProjectViewStatus toCheck) {
        for (VirtualFile current = vFile.getParent(); current != null; current = current.getParent()) {
            //look only in dir cache as parents for dirs and files will always be dirs
            ProjectViewStatus status = myDirBranchesCache.get(getKeyFor(current));
            if (status != null) {
                if (!status.isEmpty()) {
                    if (status.isTemporary()) {
                        if (toCheck.isTemporary()) {
                            return status.equals(toCheck);
                        } else {
                            return false;    
                        }
                    } else {
                        if (toCheck.isTemporary()) {
                            return true;
                        } else {
                            return status.equals(toCheck);
                        }
                    }
                }
            }
        }
        return false;
    }

    private int evictChildren(VirtualFile parent) {
        if (parent.isDirectory()) {
            String parentPath = parent.getPath();
            //evict files in all subdirectories
            Set<String> toEvict = Sets.newHashSetWithExpectedSize(myFileBranchesCache.size());
            for (String path : myFileBranchesCache.keySet()) {
                if (path.startsWith(parentPath)) {
                    toEvict.add(path);
                }
            }
            int evictedCount = 0;
            for (String path : toEvict) {
                if (evictPath(path)) {
                    evictedCount++;
                }
            }
            //evict all subdirectories
            toEvict = Sets.newHashSetWithExpectedSize(myDirBranchesCache.size());
            for (String path : myDirBranchesCache.keySet()) {
                if (path.startsWith(parentPath)) {
                    toEvict.add(path);
                }
            }
            for (String path : toEvict) {
                if (evictPath(path)) {
                    evictedCount++;
                }
            }
            return evictedCount;
        }
        return 0;
    }

    private boolean evictPath(String filePath) {
        ProjectViewStatus oldStatus = myDirBranchesCache.remove(filePath);
        if (oldStatus == null) {
            oldStatus = myFileBranchesCache.remove(filePath);
        }
        boolean result = oldStatus != null;
        if (result && LOG.isDebugEnabled()) {
            LOG.debug("[" + SEQ.get() + "] Evicted status for " + filePath + ", sizeAfter=[" + getCacheReport() + "], evicted=" + oldStatus);
        }
        return result;
    }

    /**
     * Evict passed file status
     *
     * @param file
     * @return
     */
    public boolean evict(VirtualFile file) {
        if (myActive.get()) {
            return evictPath(getKeyFor(file));
        }
        return false;
    }

    /**
     * Evict passed file status and if it is a directory all its children
     *
     * @param file
     * @return
     */
    public boolean evictAll(VirtualFile file) {
        return evictAll(Collections.singleton(file));
    }

    /**
     * Evict passed files status and if they are directories all their children
     *
     * @param files
     * @return
     */
    public boolean evictAll(Collection<VirtualFile> files) {
        if (myActive.get()) {
            boolean result = false;
            int evictedCount = 0;
            List<VirtualFile> directories = Lists.newArrayListWithCapacity(files.size());
            for (VirtualFile file : files) {
                boolean localStatus = evict(file);
                if (localStatus) {
                    result = true;
                    evictedCount++;
                }
                if (file.isDirectory()) {
                    directories.add(file);
                }
            }
            for (VirtualFile dir : directories) {
                int count = evictChildren(dir);
                if (count > 0) {
                    result = true;
                    evictedCount += count;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("[" + SEQ.get() + "] Evicted bulk, totalCount=" + evictedCount + ", sizeAfter=[" + getCacheReport() + "]");
            }
            return result;
        }
        return false;
    }

    private void disposeCache(String name, Map<?, ?> cache) {
        int size = cache.size();
        cache.clear();
        if (LOG.isDebugEnabled()) {
            LOG.debug("[" + SEQ.get() + "] " + name + " cache disposed, had " + size + " entries");
        }
    }

    public void clear() {
        disposeCache("Dir", myDirBranchesCache);
        disposeCache("File", myFileBranchesCache);
    }

    @Override
    public void dispose() {
        myActive.set(false);
        clear();
    }

    public class PutResult {
        private final ProjectViewStatus finalStatus;
        private final ProjectViewStatus oldStatus;

        public PutResult(ProjectViewStatus finalStatus, @Nullable ProjectViewStatus oldStatus) {
            this.finalStatus = finalStatus;
            this.oldStatus = oldStatus;
        }

        public ProjectViewStatus getFinalStatus() {
            return finalStatus;
        }

        public boolean hasOldStatus() {
            return oldStatus != null;
        }

        @Nullable
        public ProjectViewStatus getOldStatus() {
            return oldStatus;
        }
    }
}
