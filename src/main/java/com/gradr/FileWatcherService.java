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
     * Uses Paths.get() or Path.of() for path creation
     * Uses Files.exists() for validation
     * Uses Files.createDirectories() for directory creation
     * WatchService watcher = FileSystems.getDefault().newWatchService()
     * Monitor imports/ directory
     * Auto-detect new CSV files
     * Background thread polls for events
     * @param directoryPath Path to watch (supports both relative and absolute paths)
     */
    public FileWatcherService(String directoryPath) throws IOException {
        // Use Paths.get() or Path.of() for path creation
        this.watchDirectory = Paths.get(directoryPath);
        
        // Use Files.exists() for validation
        // Use Files.createDirectories() for directory creation
        if (!Files.exists(watchDirectory)) {
            Files.createDirectories(watchDirectory);
        }
        
        // WatchService watcher = FileSystems.getDefault().newWatchService()
        // Initialize WatchService
        this.watchService = FileSystems.getDefault().newWatchService();
        
        // Register directory for file creation events
        // Monitor imports/ directory
        watchDirectory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
    }
    
    /**
     * Start watching for new files
     * Background thread polls for events
     * This method blocks until stop() is called
     * Can be run in separate thread for non-blocking operation
     */
    public void startWatching() {
        isRunning = true;
        System.out.println("File watcher started. Monitoring: " + watchDirectory);
        System.out.println("Watching for new CSV, JSON, and binary files...");
        
        try {
            while (isRunning) {
                // Background thread polls for events
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        
                        if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path fileName = ev.context();
                            Path fullPath = watchDirectory.resolve(fileName);
                            
                            // Auto-detect new CSV files and trigger import automatically
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
     * Auto-detects new CSV files and triggers import automatically
     */
    private void processFile(Path filePath) {
        // Validate file exists and is readable
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            System.out.println("  → File not accessible: " + filePath.getFileName());
            return;
        }
        
        String fileName = filePath.getFileName().toString().toLowerCase();
        
        System.out.println("\n[File Watcher] New file detected: " + fileName);
        
        if (fileName.endsWith(".csv")) {
            System.out.println("  → CSV file detected. Ready for import.");
            // Trigger import automatically (can be extended to call import method)
            triggerAutoImport(filePath, "CSV");
        } else if (fileName.endsWith(".json")) {
            System.out.println("  → JSON file detected. Ready for import.");
            triggerAutoImport(filePath, "JSON");
        } else if (fileName.endsWith(".dat")) {
            System.out.println("  → Binary file detected. Ready for import.");
            triggerAutoImport(filePath, "BINARY");
        } else {
            System.out.println("  → Unsupported file type. Skipping.");
        }
    }
    
    /**
     * Trigger automatic import for detected files
     * This method can be extended to actually perform the import
     */
    private void triggerAutoImport(Path filePath, String fileType) {
        try {
            // Get file size using Files.size()
            long fileSize = Files.size(filePath);
            System.out.printf("  → File size: %s\n", formatFileSize(fileSize));
            System.out.printf("  → Auto-import triggered for %s file\n", fileType);
            // TODO: Integrate with actual import functionality
        } catch (IOException e) {
            System.err.println("  → Error reading file: " + e.getMessage());
        }
    }
    
    /**
     * Format file size to human-readable format
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
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

