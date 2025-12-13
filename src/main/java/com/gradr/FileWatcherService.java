package com.gradr;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * FileWatcherService - Monitors directories for new import files using NIO.2 WatchService
 * 
 * Features:
 * - Watches for new files in import directories
 * - Supports CSV, JSON, and binary file detection
 * - Automatic file processing on detection
 * - Proper resource management with try-with-resources
 */
public class FileWatcherService {
    private WatchService watchService;
    private Path watchDirectory;
    private boolean isRunning = false;
    
    /**
     * Create a file watcher for the specified directory
     * @param directoryPath Path to watch (supports both relative and absolute paths)
     */
    public FileWatcherService(String directoryPath) throws IOException {
        this.watchDirectory = Paths.get(directoryPath);
        
        // Create directory if it doesn't exist
        if (!Files.exists(watchDirectory)) {
            Files.createDirectories(watchDirectory);
        }
        
        // Initialize WatchService
        this.watchService = FileSystems.getDefault().newWatchService();
        
        // Register directory for file creation events
        watchDirectory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
    }
    
    /**
     * Start watching for new files
     * This method blocks until stop() is called
     */
    public void startWatching() {
        isRunning = true;
        System.out.println("File watcher started. Monitoring: " + watchDirectory);
        System.out.println("Watching for new CSV, JSON, and binary files...");
        
        try {
            while (isRunning) {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        
                        if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path fileName = ev.context();
                            Path fullPath = watchDirectory.resolve(fileName);
                            
                            // Process file based on extension
                            processFile(fullPath);
                        }
                    }
                    
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("File watcher interrupted.");
        } finally {
            try {
                watchService.close();
            } catch (IOException e) {
                System.err.println("Error closing watch service: " + e.getMessage());
            }
        }
    }
    
    /**
     * Process detected file based on its extension
     */
    private void processFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        
        System.out.println("\n[File Watcher] New file detected: " + fileName);
        
        if (fileName.endsWith(".csv")) {
            System.out.println("  → CSV file detected. Ready for import.");
        } else if (fileName.endsWith(".json")) {
            System.out.println("  → JSON file detected. Ready for import.");
        } else if (fileName.endsWith(".dat")) {
            System.out.println("  → Binary file detected. Ready for import.");
        } else {
            System.out.println("  → Unsupported file type. Skipping.");
        }
    }
    
    /**
     * Stop watching for files
     */
    public void stop() {
        isRunning = false;
        System.out.println("File watcher stopped.");
    }
    
    /**
     * Check if watcher is running
     */
    public boolean isRunning() {
        return isRunning;
    }
}

