package com.cmips.integration.framework.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Watches a directory for new files matching a pattern.
 *
 * <p>This class uses Java's WatchService to monitor a directory for new files.
 * It can be used to implement file-based integration triggers.
 *
 * <p>Example usage:
 * <pre>
 * FilePattern pattern = FilePattern.glob("*.xml");
 * try (FileWatcher watcher = new FileWatcher(Paths.get("/data/input"), pattern)) {
 *     while (true) {
 *         Optional&lt;Path&gt; newFile = watcher.nextFile(Duration.ofSeconds(30));
 *         if (newFile.isPresent()) {
 *             processFile(newFile.get());
 *         }
 *     }
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class FileWatcher implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(FileWatcher.class);

    private final Path directory;
    private final FilePattern pattern;
    private final WatchService watchService;
    private final BlockingQueue<Path> fileQueue;
    private final Thread watchThread;
    private volatile boolean running;

    /**
     * Creates a new FileWatcher for the specified directory and pattern.
     *
     * @param directory the directory to watch
     * @param pattern the file pattern to match
     * @throws IOException if the watch service cannot be created
     */
    public FileWatcher(Path directory, FilePattern pattern) throws IOException {
        this(directory, pattern, true);
    }

    /**
     * Creates a new FileWatcher with optional scan of existing files.
     *
     * @param directory the directory to watch
     * @param pattern the file pattern to match
     * @param includeExisting whether to include existing files in the queue
     * @throws IOException if the watch service cannot be created
     */
    public FileWatcher(Path directory, FilePattern pattern, boolean includeExisting)
            throws IOException {
        this.directory = directory;
        this.pattern = pattern;
        this.fileQueue = new LinkedBlockingQueue<>();
        this.running = true;

        // Create directory if it doesn't exist
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // Include existing files if requested
        if (includeExisting) {
            try (var stream = Files.list(directory)) {
                stream.filter(Files::isRegularFile)
                        .filter(pattern::matches)
                        .forEach(fileQueue::add);
            }
        }

        // Set up watch service
        this.watchService = FileSystems.getDefault().newWatchService();
        directory.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        // Start background thread
        this.watchThread = new Thread(this::watchLoop, "FileWatcher-" + directory.getFileName());
        this.watchThread.setDaemon(true);
        this.watchThread.start();

        log.info("Started file watcher for {} with pattern {}", directory, pattern);
    }

    /**
     * Gets the next file, blocking until one is available.
     *
     * @return the next matching file
     * @throws InterruptedException if the thread is interrupted
     */
    public Path nextFile() throws InterruptedException {
        return fileQueue.take();
    }

    /**
     * Gets the next file, blocking up to the specified timeout.
     *
     * @param timeout the maximum time to wait
     * @return an Optional containing the next file, or empty if timeout expires
     * @throws InterruptedException if the thread is interrupted
     */
    public Optional<Path> nextFile(Duration timeout) throws InterruptedException {
        Path file = fileQueue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        return Optional.ofNullable(file);
    }

    /**
     * Gets the next file without waiting.
     *
     * @return an Optional containing the next file, or empty if none available
     */
    public Optional<Path> pollFile() {
        return Optional.ofNullable(fileQueue.poll());
    }

    /**
     * Returns the number of files currently in the queue.
     *
     * @return the queue size
     */
    public int getQueueSize() {
        return fileQueue.size();
    }

    /**
     * Checks if the watcher is running.
     *
     * @return {@code true} if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the directory being watched.
     *
     * @return the directory path
     */
    public Path getDirectory() {
        return directory;
    }

    /**
     * Returns the file pattern.
     *
     * @return the pattern
     */
    public FilePattern getPattern() {
        return pattern;
    }

    private void watchLoop() {
        while (running) {
            try {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) {
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        log.warn("Watch event overflow for {}", directory);
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path fileName = pathEvent.context();
                    Path fullPath = directory.resolve(fileName);

                    if (Files.isRegularFile(fullPath) && pattern.matches(fullPath)) {
                        // Wait a moment to ensure file is completely written
                        Thread.sleep(100);
                        if (Files.exists(fullPath)) {
                            fileQueue.add(fullPath);
                            log.debug("New file detected: {}", fullPath);
                        }
                    }
                }

                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in file watcher loop", e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        watchThread.interrupt();
        try {
            watchThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        watchService.close();
        log.info("Stopped file watcher for {}", directory);
    }
}
