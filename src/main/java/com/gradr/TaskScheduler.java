package com.gradr;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * TaskScheduler - Manages scheduled automated tasks using ScheduledExecutorService
 * 
 * Features:
 * - ScheduledExecutorService for recurring task execution
 * - Support for daily, hourly, and weekly schedules
 * - Task persistence (save/load from file)
 * - Task execution logging
 * - Notification system (simulated email)
 * - Background execution without blocking
 */
public class TaskScheduler {
    
    private final ScheduledExecutorService scheduler;
    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    
    // Active scheduled tasks
    private final Map<String, ScheduledTask> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> taskFutures = new ConcurrentHashMap<>();
    
    // Task execution log
    private final List<TaskExecutionLog> executionLogs = Collections.synchronizedList(new ArrayList<>());
    
    // Persistence file
    private static final Path SCHEDULES_FILE = Paths.get("./data/schedules.dat");
    
    // Thread pool size for scheduled tasks
    private static final int SCHEDULER_THREADS = 3;
    
    
    public TaskScheduler(StudentManager studentManager, GradeManager gradeManager) {
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
        this.scheduler = Executors.newScheduledThreadPool(SCHEDULER_THREADS);
        loadSchedules(); // Load persisted schedules
    }
    
    /**
     * Schedule a new task
     */
    public ScheduledTask scheduleTask(ScheduledTask task) {
        scheduledTasks.put(task.getTaskId(), task);
        
        // Calculate initial delay
        long initialDelay = calculateInitialDelay(task);
        
        // Schedule based on type
        ScheduledFuture<?> future;
        switch (task.getScheduleType()) {
            case DAILY:
                future = scheduler.scheduleAtFixedRate(
                    () -> executeTask(task),
                    initialDelay,
                    24 * 60 * 60 * 1000, // 24 hours in milliseconds
                    TimeUnit.MILLISECONDS
                );
                break;
            case HOURLY:
                future = scheduler.scheduleAtFixedRate(
                    () -> executeTask(task),
                    initialDelay,
                    60 * 60 * 1000, // 1 hour in milliseconds
                    TimeUnit.MILLISECONDS
                );
                break;
            case WEEKLY:
                future = scheduler.scheduleAtFixedRate(
                    () -> executeTask(task),
                    initialDelay,
                    7 * 24 * 60 * 60 * 1000, // 7 days in milliseconds
                    TimeUnit.MILLISECONDS
                );
                break;
            default:
                return null;
        }
        
        taskFutures.put(task.getTaskId(), future);
        saveSchedules(); // Persist schedules
        return task;
    }
    
    /**
     * Calculate initial delay until first execution
     */
    private long calculateInitialDelay(ScheduledTask task) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = task.getNextRunTime();
        
        if (nextRun == null) {
            task.calculateNextRunTime();
            nextRun = task.getNextRunTime();
        }
        
        java.time.Duration duration = java.time.Duration.between(now, nextRun);
        return Math.max(0, duration.toMillis());
    }
    
    /**
     * Execute a scheduled task
     */
    private void executeTask(ScheduledTask task) {
        long startTime = System.currentTimeMillis();
        ScheduledTask.TaskStatus status = ScheduledTask.TaskStatus.SUCCESS;
        String errorMessage = null;
        
        try {
            // Update status to running
            task.recordExecution(ScheduledTask.TaskStatus.RUNNING, 0, null);
            
            // Execute based on task name
            switch (task.getTaskName()) {
                case "Daily GPA Recalculation":
                    executeGPARecalculation(task);
                    break;
                case "Hourly Statistics Cache Refresh":
                    executeCacheRefresh(task);
                    break;
                case "Weekly Batch Report Generation":
                    executeBatchReportGeneration(task);
                    break;
                case "Daily Database Backup":
                    executeDatabaseBackup(task);
                    break;
                default:
                    // Custom task - log it
                    logTaskExecution(task, "Custom task executed", startTime);
            }
            
            status = ScheduledTask.TaskStatus.SUCCESS;
            
        } catch (Exception e) {
            status = ScheduledTask.TaskStatus.FAILED;
            errorMessage = e.getMessage();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            task.recordExecution(status, duration, errorMessage);
            
            // Log execution
            logTaskExecution(task, status, duration, errorMessage);
            
            // Send notification if configured
            if (task.isEmailNotification() && task.getNotificationEmail() != null) {
                sendNotification(task, status, duration, errorMessage);
            }
        }
    }
    
    /**
     * Execute GPA recalculation task
     */
    private void executeGPARecalculation(ScheduledTask task) {
        List<Student> students = getStudentsByScope(task.getScope());
        GPACalculator gpaCalc = new GPACalculator(gradeManager);
        
        // Use thread pool if configured
        if (task.getThreadCount() > 1) {
            ExecutorService executor = Executors.newFixedThreadPool(task.getThreadCount());
            List<Future<?>> futures = new ArrayList<>();
            
            for (Student student : students) {
                futures.add(executor.submit(() -> {
                    gpaCalc.calculateCumulativeGPA(student.getStudentId());
                }));
            }
            
            // Wait for all to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    throw new RuntimeException("GPA calculation failed: " + e.getMessage(), e);
                }
            }
            
            executor.shutdown();
        } else {
            // Single-threaded execution
            for (Student student : students) {
                gpaCalc.calculateCumulativeGPA(student.getStudentId());
            }
        }
    }
    
    /**
     * Execute cache refresh task
     */
    private void executeCacheRefresh(ScheduledTask task) {
        // Clear and rebuild statistics cache
        gradeManager.clearStatisticsCache();
        
        // Trigger statistics calculation
        StatisticsCalculator statsCalc = new StatisticsCalculator(gradeManager, studentManager);
        try {
            statsCalc.generateClassStatistics();
        } catch (Exception e) {
            // Ignore - cache will be rebuilt on next access
        }
    }
    
    /**
     * Execute batch report generation task
     */
    private void executeBatchReportGeneration(ScheduledTask task) {
        List<Student> students = getStudentsByScope(task.getScope());
        BatchReportGenerator batchGen = new BatchReportGenerator(studentManager, gradeManager);
        
        batchGen.initializeThreadPool(task.getThreadCount());
        batchGen.generateBatchReports(students, "Weekly Report", 4); // All formats
        batchGen.shutdown(5);
    }
    
    /**
     * Execute database backup task
     */
    private void executeDatabaseBackup(ScheduledTask task) {
        // Simulate database backup
        // In a real system, this would backup the database
        // For now, we'll create a backup file with current data
        
        try {
            Path backupDir = Paths.get("./backups/");
            Files.createDirectories(backupDir);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path backupFile = backupDir.resolve("backup_" + timestamp + ".dat");
            
            // Serialize current state (simplified - would backup actual database)
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    Files.newOutputStream(backupFile))) {
                oos.writeObject("Database backup - " + timestamp);
                oos.writeInt(studentManager.getStudentCount());
                oos.writeInt(gradeManager.getGradeCount());
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Backup failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get students by scope
     */
    private List<Student> getStudentsByScope(String scope) {
        List<Student> allStudents = studentManager.getStudentsList();
        
        if (scope == null || scope.equals("All Students")) {
            return allStudents;
        } else if (scope.equals("Honors Students Only")) {
            List<Student> honors = new ArrayList<>();
            for (Student s : allStudents) {
                if (s.getStudentType().equals("Honors")) {
                    honors.add(s);
                }
            }
            return honors;
        } else if (scope.equals("Students with Grade Changes")) {
            // Return all students (simplified - would track recent changes)
            return allStudents;
        }
        
        return allStudents;
    }
    
    /**
     * Log task execution
     */
    private void logTaskExecution(ScheduledTask task, ScheduledTask.TaskStatus status, 
                                 long duration, String errorMessage) {
        TaskExecutionLog log = new TaskExecutionLog(
            task.getTaskId(),
            task.getTaskName(),
            LocalDateTime.now(),
            status,
            duration,
            errorMessage
        );
        executionLogs.add(log);
        
        // Keep only last 100 logs
        if (executionLogs.size() > 100) {
            executionLogs.remove(0);
        }
        
        // Write to file if configured
        if (task.isLogToFile()) {
            writeLogToFile(log);
        }
    }
    
    /**
     * Log task execution (overload for custom messages)
     */
    private void logTaskExecution(ScheduledTask task, String message, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        logTaskExecution(task, ScheduledTask.TaskStatus.SUCCESS, duration, null);
    }
    
    /**
     * Write log to file
     */
    private void writeLogToFile(TaskExecutionLog log) {
        try {
            Path logDir = Paths.get("./logs/");
            Files.createDirectories(logDir);
            
            Path logFile = logDir.resolve("task_executions.log");
            String logEntry = String.format("[%s] %s - %s - Duration: %dms%s\n",
                log.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                log.getTaskId(),
                log.getTaskName(),
                log.getDuration(),
                log.getErrorMessage() != null ? " - ERROR: " + log.getErrorMessage() : ""
            );
            
            Files.write(logFile, logEntry.getBytes(), 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            // Ignore log write errors
        }
    }
    
    /**
     * Send notification (simulated email)
     */
    private void sendNotification(ScheduledTask task, ScheduledTask.TaskStatus status, 
                                 long duration, String errorMessage) {
        String subject = "Task Execution: " + task.getTaskName();
        String body = String.format(
            "Task: %s\n" +
            "Status: %s\n" +
            "Duration: %dms\n" +
            "Time: %s\n" +
            "%s",
            task.getTaskName(),
            status,
            duration,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            errorMessage != null ? "Error: " + errorMessage : "Task completed successfully."
        );
        
        // Simulate email sending (in real system, would send actual email)
        System.out.println("\n[NOTIFICATION] Email sent to " + task.getNotificationEmail());
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println();
    }
    
    /**
     * Get all active scheduled tasks
     */
    public List<ScheduledTask> getActiveTasks() {
        List<ScheduledTask> active = new ArrayList<>();
        for (ScheduledTask task : scheduledTasks.values()) {
            if (task.isActive()) {
                active.add(task);
            }
        }
        return active;
    }
    
    /**
     * Cancel a scheduled task
     */
    public boolean cancelTask(String taskId) {
        ScheduledFuture<?> future = taskFutures.get(taskId);
        if (future != null) {
            future.cancel(false);
            taskFutures.remove(taskId);
            ScheduledTask task = scheduledTasks.get(taskId);
            if (task != null) {
                task.setActive(false);
            }
            saveSchedules();
            return true;
        }
        return false;
    }
    
    /**
     * Save schedules to file
     */
    public void saveSchedules() {
        try {
            Files.createDirectories(SCHEDULES_FILE.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    Files.newOutputStream(SCHEDULES_FILE))) {
                oos.writeObject(new ArrayList<>(scheduledTasks.values()));
            }
        } catch (IOException e) {
            // Ignore save errors
        }
    }
    
    /**
     * Load schedules from file
     */
    @SuppressWarnings("unchecked")
    private void loadSchedules() {
        if (!Files.exists(SCHEDULES_FILE)) {
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                Files.newInputStream(SCHEDULES_FILE))) {
            List<ScheduledTask> tasks = (List<ScheduledTask>) ois.readObject();
            for (ScheduledTask task : tasks) {
                if (task.isActive()) {
                    scheduledTasks.put(task.getTaskId(), task);
                    // Reschedule the task
                    scheduleTask(task);
                }
            }
        } catch (Exception e) {
            // Ignore load errors
        }
    }
    
    /**
     * Get execution logs
     */
    public List<TaskExecutionLog> getExecutionLogs() {
        return new ArrayList<>(executionLogs);
    }
    
    /**
     * Shutdown scheduler
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        saveSchedules(); // Save before shutdown
    }
    
    /**
     * Get scheduler status
     */
    public String getSchedulerStatus() {
        if (scheduler.isShutdown()) {
            return "STOPPED";
        } else if (scheduler.isTerminated()) {
            return "TERMINATED";
        } else {
            return "RUNNING";
        }
    }
    
    /**
     * TaskExecutionLog - Log entry for task execution
     */
    public static class TaskExecutionLog implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String taskId;
        private String taskName;
        private LocalDateTime timestamp;
        private ScheduledTask.TaskStatus status;
        private long duration;
        private String errorMessage;
        
        public TaskExecutionLog(String taskId, String taskName, LocalDateTime timestamp,
                               ScheduledTask.TaskStatus status, long duration, String errorMessage) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.timestamp = timestamp;
            this.status = status;
            this.duration = duration;
            this.errorMessage = errorMessage;
        }
        
        // Getters
        public String getTaskId() { return taskId; }
        public String getTaskName() { return taskName; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public ScheduledTask.TaskStatus getStatus() { return status; }
        public long getDuration() { return duration; }
        public String getErrorMessage() { return errorMessage; }
    }
}

