package com.gradr;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AuditLogger - Thread-safe audit logging with asynchronous file writing
 * 
 * Features:
 * - ConcurrentLinkedQueue for thread-safe log entry collection
 * - SingleThreadExecutor for sequential log file writing
 * - ISO 8601 timestamp format
 * - Thread ID tracking
 * - Execution time tracking
 * - Success/failure status
 * - Log rotation (daily and 10MB size-based)
 * - No log entries lost during concurrent operations
 */
public class AuditLogger {
    
    /**
     * Enhanced AuditEntry with all required fields
     */
    public static class EnhancedAuditEntry {
        private final String timestamp; // ISO 8601 format
        private final long threadId;
        private final String operationType;
        private final String userAction;
        private final long executionTime; // milliseconds
        private final boolean success;
        private final String errorMessage;
        private final String details;
        
        public EnhancedAuditEntry(String operationType, String userAction, 
                                 long executionTime, boolean success, 
                                 String errorMessage, String details) {
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            this.threadId = Thread.currentThread().getId();
            this.operationType = operationType;
            this.userAction = userAction;
            this.executionTime = executionTime;
            this.success = success;
            this.errorMessage = errorMessage;
            this.details = details;
        }
        
        // Getters
        public String getTimestamp() { return timestamp; }
        public long getThreadId() { return threadId; }
        public String getOperationType() { return operationType; }
        public String getUserAction() { return userAction; }
        public long getExecutionTime() { return executionTime; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public String getDetails() { return details; }
        
        /**
         * Format entry for file output
         */
        public String toLogLine() {
            StringBuilder sb = new StringBuilder();
            sb.append(timestamp).append("|");
            sb.append(threadId).append("|");
            sb.append(operationType).append("|");
            sb.append(userAction).append("|");
            sb.append(executionTime).append("|");
            sb.append(success ? "SUCCESS" : "FAILED").append("|");
            if (errorMessage != null) {
                sb.append(errorMessage.replace("|", "\\|")).append("|");
            } else {
                sb.append("|");
            }
            if (details != null) {
                sb.append(details.replace("|", "\\|"));
            }
            return sb.toString();
        }
        
        /**
         * Parse from log line
         */
        public static EnhancedAuditEntry fromLogLine(String line) {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 7) return null;
            
            try {
                String operationType = parts[2];
                String userAction = parts[3];
                long executionTime = Long.parseLong(parts[4]);
                boolean success = "SUCCESS".equals(parts[5]);
                String errorMessage = parts.length > 6 && !parts[6].isEmpty() ? 
                    parts[6].replace("\\|", "|") : null;
                String details = parts.length > 7 && !parts[7].isEmpty() ? 
                    parts[7].replace("\\|", "|") : null;
                
                EnhancedAuditEntry entry = new EnhancedAuditEntry(
                    operationType, userAction, executionTime, success, errorMessage, details);
                return entry;
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    // Thread-safe queue for log entries
    private final ConcurrentLinkedQueue<EnhancedAuditEntry> logQueue = new ConcurrentLinkedQueue<>();
    
    // Single-threaded executor for sequential file writing
    private final ExecutorService logWriter = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "AuditLogWriter");
        t.setDaemon(true);
        return t;
    });
    
    // Log file configuration
    private static final Path LOG_DIR = Paths.get("./logs/audit/");
    private static final long MAX_LOG_SIZE = 10 * 1024 * 1024; // 10MB
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Current log file
    private Path currentLogFile;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AtomicLong totalEntries = new AtomicLong(0);
    
    public AuditLogger() {
        try {
            Files.createDirectories(LOG_DIR);
            currentLogFile = getLogFileForDate(LocalDateTime.now());
            startLogWriter();
        } catch (IOException e) {
            System.err.println("Failed to initialize audit logger: " + e.getMessage());
        }
    }
    
    /**
     * Log an operation
     */
    public void log(String operationType, String userAction, long executionTime, 
                   boolean success, String errorMessage, String details) {
        EnhancedAuditEntry entry = new EnhancedAuditEntry(
            operationType, userAction, executionTime, success, errorMessage, details);
        logQueue.offer(entry);
        totalEntries.incrementAndGet();
    }
    
    /**
     * Start background log writer
     */
    private void startLogWriter() {
        logWriter.submit(() -> {
            while (isRunning.get() || !logQueue.isEmpty()) {
                try {
                    // Drain queue and write to file
                    List<EnhancedAuditEntry> entries = new ArrayList<>();
                    EnhancedAuditEntry entry;
                    while ((entry = logQueue.poll()) != null) {
                        entries.add(entry);
                    }
                    
                    if (!entries.isEmpty()) {
                        writeEntriesToFile(entries);
                    }
                    
                    // Sleep briefly to avoid busy waiting
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error writing audit log: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Write entries to file with rotation
     */
    private synchronized void writeEntriesToFile(List<EnhancedAuditEntry> entries) throws IOException {
        // Check if we need to rotate (new day or size exceeded)
        LocalDateTime now = LocalDateTime.now();
        Path logFileForToday = getLogFileForDate(now);
        
        if (!logFileForToday.equals(currentLogFile)) {
            // New day - rotate
            currentLogFile = logFileForToday;
        } else if (Files.exists(currentLogFile)) {
            // Check size
            long fileSize = Files.size(currentLogFile);
            if (fileSize >= MAX_LOG_SIZE) {
                // Rotate by appending timestamp
                String baseName = currentLogFile.getFileName().toString();
                String nameWithoutExt = baseName.substring(0, baseName.lastIndexOf('.'));
                String extension = baseName.substring(baseName.lastIndexOf('.'));
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
                Path rotatedFile = currentLogFile.getParent().resolve(
                    nameWithoutExt + "_" + timestamp + extension);
                Files.move(currentLogFile, rotatedFile);
                currentLogFile = logFileForToday;
            }
        }
        
        // Write entries
        try (BufferedWriter writer = Files.newBufferedWriter(
                currentLogFile, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND)) {
            for (EnhancedAuditEntry entry : entries) {
                writer.write(entry.toLogLine());
                writer.newLine();
            }
        }
    }
    
    /**
     * Get log file path for a specific date
     */
    private Path getLogFileForDate(LocalDateTime date) {
        String dateStr = date.format(DATE_FORMATTER);
        return LOG_DIR.resolve("audit_" + dateStr + ".log");
    }
    
    /**
     * Read audit entries from log files
     */
    public List<EnhancedAuditEntry> readAuditEntries(LocalDateTime startDate, LocalDateTime endDate) {
        List<EnhancedAuditEntry> entries = new ArrayList<>();
        
        LocalDateTime current = startDate;
        while (!current.isAfter(endDate)) {
            Path logFile = getLogFileForDate(current);
            if (Files.exists(logFile)) {
                try {
                    List<String> lines = Files.readAllLines(logFile);
                    for (String line : lines) {
                        EnhancedAuditEntry entry = EnhancedAuditEntry.fromLogLine(line);
                        if (entry != null) {
                            LocalDateTime entryTime = LocalDateTime.parse(
                                entry.getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            if (!entryTime.isBefore(startDate) && !entryTime.isAfter(endDate)) {
                                entries.add(entry);
                            }
                        }
                    }
                } catch (IOException e) {
                    // Ignore - file may be locked or unreadable
                }
            }
            current = current.plusDays(1);
        }
        
        return entries;
    }
    
    /**
     * Get all log files
     */
    public List<Path> getAllLogFiles() {
        List<Path> logFiles = new ArrayList<>();
        try {
            Files.list(LOG_DIR)
                .filter(p -> p.getFileName().toString().startsWith("audit_") && 
                            p.getFileName().toString().endsWith(".log"))
                .sorted()
                .forEach(logFiles::add);
        } catch (IOException e) {
            // Ignore
        }
        return logFiles;
    }
    
    /**
     * Shutdown logger
     */
    public void shutdown() {
        isRunning.set(false);
        logWriter.shutdown();
        try {
            if (!logWriter.awaitTermination(5, TimeUnit.SECONDS)) {
                logWriter.shutdownNow();
            }
        } catch (InterruptedException e) {
            logWriter.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Write remaining entries
        List<EnhancedAuditEntry> remaining = new ArrayList<>();
        EnhancedAuditEntry entry;
        while ((entry = logQueue.poll()) != null) {
            remaining.add(entry);
        }
        if (!remaining.isEmpty()) {
            try {
                writeEntriesToFile(remaining);
            } catch (IOException e) {
                System.err.println("Error writing final audit entries: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get total entries logged
     */
    public long getTotalEntries() {
        return totalEntries.get();
    }
}

