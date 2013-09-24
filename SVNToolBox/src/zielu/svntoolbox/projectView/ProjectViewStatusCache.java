/* 
 * $Id$
 */
package zielu.svntoolbox.projectView;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p></p>
 * <br/>
 * <p>Created on 24.09.13</p>
 *
 * @author Lukasz Zielinski
 */
public class ProjectViewStatusCache implements Disposable {
    private final Logger LOG = Logger.getInstance(getClass());

    private final Map<String, ProjectViewStatus> branchesCache = new ConcurrentHashMap<String, ProjectViewStatus>();
    private AtomicBoolean active = new AtomicBoolean(true);

    @Nullable
    public ProjectViewStatus getBranch(VirtualFile file) {
        if (active.get()) {
            ProjectViewStatus status = branchesCache.get(file.getPath());
            if (status != null && LOG.isDebugEnabled()) {
                LOG.debug("Found cached status for " + file + ", size=" + branchesCache.size() + ", existing=" + status);
            }
            return status;
        }
        return null;
    }

    @Nullable
    public ProjectViewStatus put(VirtualFile file, ProjectViewStatus branch) {
        if (active.get()) {
            ProjectViewStatus oldStatus = branchesCache.put(file.getPath(), branch);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cached status for " + file + ", sizeAfter=" + branchesCache.size() + ", previous=" + oldStatus);
            }
            return oldStatus;
        }
        return null;
    }

    public boolean evict(VirtualFile file) {
        if (active.get()) {
            ProjectViewStatus oldStatus = branchesCache.remove(file.getPath());
            boolean result = oldStatus != null;
            if (result && LOG.isDebugEnabled()) {
                LOG.debug("Evicted status for " + file + ", sizeAfter=" + branchesCache.size() + ", evicted=" + oldStatus);
            }
            return result;
        }
        return false;
    }

    public boolean evictAll(Iterable<VirtualFile> files) {
        if (active.get()) {
            boolean result = false;
            int evictedCount = 0;
            for (VirtualFile file : files) {
                ProjectViewStatus oldStatus = branchesCache.remove(file.getPath());
                boolean localStatus = oldStatus != null;
                if (localStatus) {
                    result = true;
                    evictedCount++;
                }
                if (localStatus && LOG.isDebugEnabled()) {
                    LOG.debug("Evicted status for " + file + ", evicted=" + oldStatus);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Evicted " + evictedCount + " entries, sizeAfter=" + branchesCache.size());
            }
            return result;
        }
        return false;
    }

    public boolean evictNotSwitched(VirtualFile file) {
        if (active.get()) {
            ProjectViewStatus status = branchesCache.get(file.getPath());
            if (status != null && status.hasSwitchedInfo()) {
                if (!status.isSwitched()) {
                    evict(file);
                }
            }
        }
        return false;
    }

    public boolean evictSwitched(VirtualFile file) {
        if (active.get()) {
            ProjectViewStatus status = branchesCache.get(file.getPath());
            if (status != null && status.hasSwitchedInfo()) {
                if (status.isSwitched()) {
                    evict(file);
                }
            }
        }
        return false;
    }

    @Override
    public void dispose() {
        active.set(false);
        int size = branchesCache.size();
        branchesCache.clear();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache disposed, had " + size + " entries");
        }
    }
}
