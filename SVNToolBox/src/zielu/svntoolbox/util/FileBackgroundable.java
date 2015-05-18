package zielu.svntoolbox.util;

import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public abstract class FileBackgroundable extends Backgroundable {
    private final VirtualFile myFile;

    public FileBackgroundable(@NotNull Project project, @NotNull VirtualFile file, @NotNull String title, boolean canBeCancelled) {
        super(project, title, canBeCancelled);
        myFile = file;
    }

    protected final VirtualFile getFile() {
        return myFile;
    }
}
