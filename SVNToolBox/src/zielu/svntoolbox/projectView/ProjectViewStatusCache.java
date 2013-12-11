/* 
 * $Id$
 */
package zielu.svntoolbox.projectView;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
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
    private final Map<VirtualFile, ProjectViewStatus> myDirBranchesCache = new ConcurrentHashMap<VirtualFile, ProjectViewStatus>();
    private final Map<VirtualFile, ProjectViewStatus> myFileBranchesCache = new ConcurrentHashMap<VirtualFile, ProjectViewStatus>();

    private final AtomicBoolean myActive = new AtomicBoolean(true);

    private final AtomicInteger SEQ;

    public ProjectViewStatusCache(AtomicInteger seq) {
        SEQ = seq;
    }

    private Map<VirtualFile, ProjectViewStatus> getCacheFor(VirtualFile vFile) {
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
            ProjectViewStatus status = getCacheFor(file).get(file);
            if (status != null && LOG.isDebugEnabled()) {
                LOG.debug("[" + SEQ.incrementAndGet() + "] Found cached status for " + file.getPath() + ", " + getCacheReport() + ", status=" + status);
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
            ProjectViewStatus oldStatus = getCacheFor(file).put(file, candidate);
            if (LOG.isDebugEnabled()) {
                LOG.debug("[" + SEQ.incrementAndGet() + "] Cached candidate for " + file.getPath() +
                        ", cacheAfter=[" + getCacheReport() + "], new=" + candidate + ", previous=" + oldStatus);
            }
            return new PutResult(candidate, oldStatus);
        }
        return null;
    }

    private boolean isFirstNotEmptyParentStatusEqualTo(VirtualFile vFile, ProjectViewStatus toCheck) {
        for (VirtualFile current = vFile.getParent(); current != null; current = current.getParent()) {
            //look only in dir cache as parents for dirs and files will always be dirs
            ProjectViewStatus status = myDirBranchesCache.get(current);
            if (status != null) {
                if (!status.isEmpty()) {
                    return status.equals(toCheck);
                }
            }
        }
        return false;
    }

    private int evictChildren(VirtualFile parent) {
        if (parent.isDirectory()) {
            String parentPath = parent.getPath();
            //evict files in all subdirectories
            Set<VirtualFile> toEvict = Sets.newHashSetWithExpectedSize(myFileBranchesCache.size());
            for (VirtualFile vFile : myFileBranchesCache.keySet()) {
                if (vFile.getPath().startsWith(parentPath)) {
                    toEvict.add(vFile);
                }
            }
            int evictedCount = 0;
            for (VirtualFile vFile : toEvict) {
                if (evictFile(vFile)) {
                    evictedCount++;
                }
            }
            //evict all subdirectories
            toEvict = Sets.newHashSetWithExpectedSize(myDirBranchesCache.size());
            for (VirtualFile vDir : myDirBranchesCache.keySet()) {
                if (vDir.getPath().startsWith(parentPath)) {
                    toEvict.add(vDir);
                }
            }
            for (VirtualFile vFile : toEvict) {
                if (evictFile(vFile)) {
                    evictedCount++;
                }
            }
            return evictedCount;
        }
        return 0;
    }

    private boolean evictFile(VirtualFile vFile) {
        Map<VirtualFile, ProjectViewStatus> cache = getCacheFor(vFile);
        ProjectViewStatus oldStatus = cache.remove(vFile);
        boolean result = oldStatus != null;
        if (result && LOG.isDebugEnabled()) {
            LOG.debug("[" + SEQ.incrementAndGet() + "] Evicted status for " + vFile.getPath() + ", sizeAfter=[" + getCacheReport() + "], evicted=" + oldStatus);
        }
        return result;
    }

    public boolean evict(VirtualFile file) {
        if (myActive.get()) {
            return evictFile(file);
        }
        return false;
    }

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
                LOG.debug("[" + SEQ.incrementAndGet() + "] Evicted bulk, totalCount=" + evictedCount + ", sizeAfter=[" + getCacheReport() + "]");
            }
            return result;
        }
        return false;
    }

    private void disposeCache(String name, Map<VirtualFile, ProjectViewStatus> cache) {
        int size = cache.size();
        cache.clear();
        if (LOG.isDebugEnabled()) {
            LOG.debug("[" + SEQ.incrementAndGet() + "] " + name + " cache disposed, had " + size + " entries");
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
