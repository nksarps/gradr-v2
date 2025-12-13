package com.gradr;

import com.gradr.exceptions.FileExportException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * BatchReportGenerator - Handles concurrent batch report generation using ExecutorService
 * 
 * Features:
 * - FixedThreadPool for parallel processing
 * - Thread-safe file writing
 * - Real-time progress tracking
 * - Performance metrics and statistics
 * - Exception handling per thread
 */
public class BatchReportGenerator {
    
    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    private final MultiFormatFileHandler fileHandler;
    private ExecutorService executorService;
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger failedTasks = new AtomicInteger(0);
    private final AtomicLong totalTime = new AtomicLong(0);
    private final Map<String, Long> reportTimes = new ConcurrentHashMap<>();
    private final Map<String, String> threadStatus = new ConcurrentHashMap<>();
    private final List<String> completedReports = Collections.synchronizedList(new ArrayList<>());
    private final List<String> failedReports = Collections.synchronizedList(new ArrayList<>());
    
    // Batch output directory
    private Path batchOutputDir;
    
    public BatchReportGenerator(StudentManager studentManager, GradeManager gradeManager) {
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
        try {
            this.fileHandler = new MultiFormatFileHandler();
        } catch (FileExportException e) {
            throw new RuntimeException("Failed to initialize file handler: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initialize thread pool with specified number of threads
     * @param threadCount Number of threads (2-8)
     * @return true if initialization successful
     */
    public boolean initializeThreadPool(int threadCount) {
        if (threadCount < 2 || threadCount > 8) {
            return false;
        }
        
        executorService = Executors.newFixedThreadPool(threadCount);
        return true;
    }
    
    /**
     * Generate batch reports for all students
     * @param reportType Type of report to generate
     * @param formatChoice Format choice (1=CSV, 2=JSON, 3=Binary, 4=All)
     * @return BatchResult containing statistics
     */
    public BatchResult generateBatchReports(String reportType, int formatChoice) {
        List<Student> students = studentManager.getStudentsList();
        return generateBatchReports(students, reportType, formatChoice);
    }
    
    /**
     * Generate batch reports for selected students
     * @param students List of students to generate reports for
     * @param reportType Type of report to generate
     * @param formatChoice Format choice (1=CSV, 2=JSON, 3=Binary, 4=All)
     * @return BatchResult containing statistics
     */
    public BatchResult generateBatchReports(List<Student> students, String reportType, int formatChoice) {
        if (executorService == null) {
            throw new IllegalStateException("Thread pool not initialized. Call initializeThreadPool() first.");
        }
        
        // Create batch output directory
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        batchOutputDir = Paths.get("./reports/batch_" + dateStr + "/");
        try {
            java.nio.file.Files.createDirectories(batchOutputDir);
        } catch (java.io.IOException e) {
            System.err.println("Failed to create batch directory: " + e.getMessage());
        }
        
        // Reset counters
        completedTasks.set(0);
        failedTasks.set(0);
        totalTime.set(0);
        reportTimes.clear();
        threadStatus.clear();
        completedReports.clear();
        failedReports.clear();
        
        int totalReports = students.size();
        long startTime = System.currentTimeMillis();
        
        // Submit all tasks
        List<Future<ReportResult>> futures = new ArrayList<>();
        for (Student student : students) {
            Future<ReportResult> future = executorService.submit(() -> 
                generateReportForStudent(student, reportType, formatChoice)
            );
            futures.add(future);
        }
        
        // Monitor progress
        monitorProgress(totalReports, startTime);
        
        // Wait for all tasks to complete
        for (Future<ReportResult> future : futures) {
            try {
                ReportResult result = future.get(30, TimeUnit.SECONDS);
                if (result.isSuccess()) {
                    completedTasks.incrementAndGet();
                    reportTimes.put(result.getStudentId(), result.getTime());
                    completedReports.add(result.getStudentId());
                } else {
                    failedTasks.incrementAndGet();
                    failedReports.add(result.getStudentId() + ": " + result.getErrorMessage());
                }
            } catch (TimeoutException e) {
                failedTasks.incrementAndGet();
                System.err.println("Task timed out");
            } catch (Exception e) {
                failedTasks.incrementAndGet();
                System.err.println("Task failed: " + e.getMessage());
            }
        }
        
        long totalTimeMs = System.currentTimeMillis() - startTime;
        totalTime.set(totalTimeMs);
        
        // Calculate metrics
        double avgTime = completedTasks.get() > 0 ? 
            reportTimes.values().stream().mapToLong(Long::longValue).average().orElse(0) : 0;
        double throughput = totalReports > 0 ? (completedTasks.get() * 1000.0 / totalTimeMs) : 0;
        long estimatedSequential = (long)(avgTime * totalReports);
        
        return new BatchResult(
            totalReports,
            completedTasks.get(),
            failedTasks.get(),
            totalTimeMs,
            avgTime,
            estimatedSequential,
            throughput,
            batchOutputDir
        );
    }
    
    /**
     * Generate report for a single student (thread-safe)
     */
    private ReportResult generateReportForStudent(Student student, String reportType, int formatChoice) {
        String studentId = student.getStudentId();
        String threadName = Thread.currentThread().getName();
        long startTime = System.currentTimeMillis();
        
        try {
            threadStatus.put(threadName, studentId + " ... (in progress)");
            
            // Build StudentReport
            double overallAverage = student.calculateAverageGrade();
            StudentReport report = new StudentReport(
                student.getStudentId(),
                student.getName(),
                student.getStudentType(),
                overallAverage,
                reportType
            );
            
            // Add all grades to report
            for (Grade grade : gradeManager.getGrades()) {
                if (grade != null && grade.getStudentId().equals(studentId)) {
                    GradeData gradeData = new GradeData(
                        grade.getGradeId(),
                        grade.getDate(),
                        grade.getSubject().getSubjectName(),
                        grade.getSubject().getSubjectType(),
                        grade.getGrade()
                    );
                    report.addGrade(gradeData);
                }
            }
            
            // Generate filename
            String sanitizedFileName = student.getName().toLowerCase().replaceAll("[^a-z0-9_]", "_");
            String reportTypeSuffix = reportType.toLowerCase().replaceAll(" ", "_");
            String finalFileName = sanitizedFileName + "_" + reportTypeSuffix;
            
            // Export using thread-safe file handler
            synchronized (fileHandler) {
                if (formatChoice == 4) {
                    // Export to all formats
                    fileHandler.exportToAllFormats(report, 
                        batchOutputDir.resolve(finalFileName).toString());
                } else {
                    switch (formatChoice) {
                        case 1:
                            fileHandler.exportToCSV(report, 
                                batchOutputDir.resolve(finalFileName).toString());
                            break;
                        case 2:
                            fileHandler.exportToJSON(report, 
                                batchOutputDir.resolve(finalFileName).toString());
                            break;
                        case 3:
                            fileHandler.exportToBinary(report, 
                                batchOutputDir.resolve(finalFileName).toString());
                            break;
                    }
                }
            }
            
            long time = System.currentTimeMillis() - startTime;
            threadStatus.put(threadName, studentId + " ✓ (" + time + "ms)");
            
            return new ReportResult(studentId, true, time, null);
            
        } catch (FileExportException e) {
            long time = System.currentTimeMillis() - startTime;
            threadStatus.put(threadName, studentId + " ✗ (failed)");
            return new ReportResult(studentId, false, time, e.getMessage());
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            threadStatus.put(threadName, studentId + " ✗ (error)");
            return new ReportResult(studentId, false, time, e.getMessage());
        }
    }
    
    /**
     * Monitor progress and display updates
     */
    private void monitorProgress(int totalReports, long startTime) {
        while (completedTasks.get() + failedTasks.get() < totalReports) {
            try {
                Thread.sleep(200); // Update every 200ms
                displayProgress(totalReports, startTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        // Final update
        displayProgress(totalReports, startTime);
    }
    
    /**
     * Display current progress with thread status
     */
    private void displayProgress(int totalReports, long startTime) {
        // Simple progress update (works in all terminals)
        int completed = completedTasks.get() + failedTasks.get();
        double progress = totalReports > 0 ? (completed * 100.0 / totalReports) : 0;
        
        // Progress bar
        int barLength = 20;
        int filled = (int)(progress / 100.0 * barLength);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            bar.append(i < filled ? "█" : " ");
        }
        bar.append("]");
        
        long elapsed = System.currentTimeMillis() - startTime;
        double avgTime = completed > 0 ? 
            reportTimes.values().stream().mapToLong(Long::longValue).average().orElse(0) : 0;
        long estimatedRemaining = totalReports > completed ? 
            (long)(avgTime * (totalReports - completed)) : 0;
        double throughput = elapsed > 0 ? (completed * 1000.0 / elapsed) : 0;
        
        // Update progress line (overwrite previous)
        System.out.print("\rProgress: " + bar.toString() + " " + String.format("%.0f", progress) + "% (" + 
            completed + "/" + totalReports + " completed) | Elapsed: " + String.format("%.1f", elapsed / 1000.0) + 
            "s | Est. Remaining: " + String.format("%.1f", estimatedRemaining / 1000.0) + "s | Throughput: " + 
            String.format("%.1f", throughput) + " reports/sec");
        System.out.flush();
    }
    
    /**
     * Shutdown thread pool with timeout
     * @param timeoutSeconds Timeout in seconds
     */
    public void shutdown(long timeoutSeconds) {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                        System.err.println("Thread pool did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Get thread pool statistics
     */
    public ThreadPoolStats getThreadPoolStats() {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
            return new ThreadPoolStats(
                tpe.getPoolSize(),
                tpe.getActiveCount(),
                tpe.getQueue().size(),
                completedTasks.get(),
                tpe.getCompletedTaskCount()
            );
        }
        return new ThreadPoolStats(0, 0, 0, completedTasks.get(), 0);
    }
    
    /**
     * ReportResult - Result of generating a single report
     */
    public static class ReportResult {
        private final String studentId;
        private final boolean success;
        private final long time;
        private final String errorMessage;
        
        public ReportResult(String studentId, boolean success, long time, String errorMessage) {
            this.studentId = studentId;
            this.success = success;
            this.time = time;
            this.errorMessage = errorMessage;
        }
        
        public String getStudentId() { return studentId; }
        public boolean isSuccess() { return success; }
        public long getTime() { return time; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * BatchResult - Statistics from batch generation
     */
    public static class BatchResult {
        private final int totalReports;
        private final int successful;
        private final int failed;
        private final long totalTime;
        private final double avgTimePerReport;
        private final long estimatedSequential;
        private final double throughput;
        private final Path outputDir;
        
        public BatchResult(int totalReports, int successful, int failed, long totalTime,
                          double avgTimePerReport, long estimatedSequential, double throughput, Path outputDir) {
            this.totalReports = totalReports;
            this.successful = successful;
            this.failed = failed;
            this.totalTime = totalTime;
            this.avgTimePerReport = avgTimePerReport;
            this.estimatedSequential = estimatedSequential;
            this.throughput = throughput;
            this.outputDir = outputDir;
        }
        
        public int getTotalReports() { return totalReports; }
        public int getSuccessful() { return successful; }
        public int getFailed() { return failed; }
        public long getTotalTime() { return totalTime; }
        public double getAvgTimePerReport() { return avgTimePerReport; }
        public long getEstimatedSequential() { return estimatedSequential; }
        public double getThroughput() { return throughput; }
        public Path getOutputDir() { return outputDir; }
    }
    
    /**
     * ThreadPoolStats - Statistics about thread pool usage
     */
    public static class ThreadPoolStats {
        private final int poolSize;
        private final int activeThreads;
        private final int queueSize;
        private final int completedTasks;
        private final long totalCompleted;
        
        public ThreadPoolStats(int poolSize, int activeThreads, int queueSize, 
                              int completedTasks, long totalCompleted) {
            this.poolSize = poolSize;
            this.activeThreads = activeThreads;
            this.queueSize = queueSize;
            this.completedTasks = completedTasks;
            this.totalCompleted = totalCompleted;
        }
        
        public int getPoolSize() { return poolSize; }
        public int getActiveThreads() { return activeThreads; }
        public int getQueueSize() { return queueSize; }
        public int getCompletedTasks() { return completedTasks; }
        public long getTotalCompleted() { return totalCompleted; }
        
        public double getThreadUtilization() {
            return poolSize > 0 ? (activeThreads * 100.0 / poolSize) : 0;
        }
    }
}

