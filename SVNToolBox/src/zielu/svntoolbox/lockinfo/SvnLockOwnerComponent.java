package zielu.svntoolbox.lockinfo;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.jgoodies.common.base.Strings;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import zielu.svntoolbox.config.SvnToolBoxAppState;

public class SvnLockOwnerComponent implements ApplicationComponent {
    private static final Splitter semicolon = Splitter.on(';');

    private final Logger LOG = Logger.getInstance(getClass());
    private final Map<String, String> ownerMapping = Maps.newHashMap();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public static SvnLockOwnerComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(SvnLockOwnerComponent.class);
    }

    @Override
    public void initComponent() {
    }

    private Map<String, String> loadFile(File csvFile) {
        Closer closer = Closer.create();
        try {
            Map<String, String> mappings = Maps.newHashMap();
            BufferedReader reader = closer.register(Files.newBufferedReader(csvFile.toPath(), Charsets.UTF_8));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                List<String> columns = semicolon.splitToList(line);
                if (columns.size() > 1) {
                    mappings.put(columns.get(0).toLowerCase(), columns.get(1));
                }
            }
            return mappings;
        } catch (IOException e) {
            LOG.error("Failed to load csv file: " + csvFile, e);
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                LOG.error("Failed to close csv file: " + csvFile, e);
            }
        }
        return Collections.emptyMap();
    }

    private Optional<String> loadFromMappings(String owner, String csvFile) {
        lock.writeLock().lock();
        File file = new File(csvFile);
        if (file.exists()) {
            Map<String, String> mappings = loadFile(file);
            ownerMapping.clear();
            ownerMapping.putAll(mappings);
            lock.readLock().lock();
            lock.writeLock().unlock();
            try {
                return Optional.fromNullable(ownerMapping.get(owner.toLowerCase()));
            } finally {
                lock.readLock().unlock();
            }
        } else if (ownerMapping.size() > 0) {
            ownerMapping.clear();
        }
        lock.writeLock().unlock();
        return Optional.absent();
    }

    public String getOwner(String owner) {
        if (Strings.isBlank(owner)) {
            return StringUtils.EMPTY;
        } else {
            String csvFile = SvnToolBoxAppState.getInstance().getCsvFile();
            if (Strings.isBlank(csvFile)) {
                return owner;
            } else {
                Optional<String> mappedOwner = loadFromMappings(owner, csvFile);
                if (mappedOwner.isPresent()) {
                    return owner + " - " + mappedOwner.get();
                } else {
                    return owner;
                }
            }
        }
    }

    @Override
    public void disposeComponent() {
        lock.writeLock().lock();
        try {
            ownerMapping.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @NotNull
    @Override
    public String getComponentName() {
        return getClass().getSimpleName();
    }
}
