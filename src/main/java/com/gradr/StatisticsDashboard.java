package com.gradr;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * StatisticsDashboard - Real-time statistics dashboard with background daemon thread
 * 
 * Features:
 * - Background daemon thread calculates statistics every 5 seconds
 * - Auto-refreshing dashboard display
 * - Thread status (RUNNING, PAUSED, STOPPED)
 * - Manual refresh and pause/resume functionality
 * - Thread-safe collections and operations
 * - Performance metrics and cache hit rate
 */
public class StatisticsDashboard {
    
    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    
    // Background thread management
    private Thread backgroundThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicInteger refreshInterval = new AtomicInteger(5); // seconds
    
    // Thread-safe statistics storage
    private final ConcurrentHashMap<String, Object> statistics = new ConcurrentHashMap<>();
    private final AtomicLong lastUpdateTime = new AtomicLong(0);
    private final AtomicInteger cacheHits = new AtomicInteger(0);
    private final AtomicInteger cacheMisses = new AtomicInteger(0);
    private final AtomicLong calculationTime = new AtomicLong(0);
    
    // Thread pool for statistics calculation
    private ExecutorService statsPool;
    
    // Grade tracking for "last 5 minutes"
    private final ConcurrentHashMap<String, Long> recentGradeTimestamps = new ConcurrentHashMap<>();
    private final AtomicInteger gradesAddedLast5Min = new AtomicInteger(0);
    
    public StatisticsDashboard(StudentManager studentManager, GradeManager gradeManager) {
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
        this.statsPool = Executors.newCachedThreadPool();
    }
    
    /**
     * Start the background daemon thread
     */
    public void start() {
        if (isRunning.get()) {
            return; // Already running
        }
        
        isRunning.set(true);
        isPaused.set(false);
        
        backgroundThread = new Thread(() -> {
            while (isRunning.get()) {
                if (!isPaused.get()) {
                    calculateStatistics();
                }
                
                // Sleep for refresh interval
                try {
                    Thread.sleep(refreshInterval.get() * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        backgroundThread.setDaemon(true);
        backgroundThread.setName("StatisticsDashboard-Thread");
        backgroundThread.start();
    }
    
    /**
     * Stop the background thread
     */
    public void stop() {
        isRunning.set(false);
        if (backgroundThread != null) {
            backgroundThread.interrupt();
            try {
                backgroundThread.join(2000); // Wait up to 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (statsPool != null) {
            statsPool.shutdown();
            try {
                if (!statsPool.awaitTermination(2, TimeUnit.SECONDS)) {
                    statsPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                statsPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Pause the background thread
     */
    public void pause() {
        isPaused.set(true);
    }
    
    /**
     * Resume the background thread
     */
    public void resume() {
        isPaused.set(false);
    }
    
    /**
     * Manually refresh statistics
     */
    public void refresh() {
        calculateStatistics();
    }
    
    /**
     * Calculate statistics (thread-safe)
     */
    private void calculateStatistics() {
        long startTime = System.currentTimeMillis();
        
        // Calculate statistics directly (cache is private in GradeManager)
        cacheMisses.incrementAndGet();
        
        // Calculate fresh statistics
        Future<Map<String, Object>> future = statsPool.submit(() -> {
                Map<String, Object> stats = new HashMap<>();
                
                try {
                    // Basic counts
                    int totalStudents = studentManager.getStudentCount();
                    int totalGrades = gradeManager.getGradeCount();
                    stats.put("totalStudents", totalStudents);
                    stats.put("totalGrades", totalGrades);
                    
                    // Calculate grade distribution
                    Map<String, Integer> distribution = calculateGradeDistribution();
                    stats.put("gradeDistribution", distribution);
                    
                    // Calculate statistical measures
                    double[] allGrades = getAllGradesArray();
                    if (allGrades.length > 0) {
                        stats.put("mean", calculateMean(allGrades));
                        stats.put("median", calculateMedian(allGrades));
                        stats.put("stdDev", calculateStandardDeviation(allGrades));
                    } else {
                        stats.put("mean", 0.0);
                        stats.put("median", 0.0);
                        stats.put("stdDev", 0.0);
                    }
                    
                    // Top performers
                    List<Map<String, Object>> topPerformers = getTopPerformers(3);
                    stats.put("topPerformers", topPerformers);
                    
                    // Recent grades (last 5 minutes)
                    updateRecentGrades();
                    stats.put("gradesAddedLast5Min", gradesAddedLast5Min.get());
                    
                    // Thread pool status
                    stats.put("activeThreads", Thread.activeCount());
                    
                    // Memory usage
                    Runtime runtime = Runtime.getRuntime();
                    long totalMemory = runtime.totalMemory();
                    long freeMemory = runtime.freeMemory();
                    long usedMemory = totalMemory - freeMemory;
                    stats.put("memoryUsed", usedMemory);
                    stats.put("memoryTotal", runtime.maxMemory());
                    
                    // Cache hit rate
                    int totalRequests = cacheHits.get() + cacheMisses.get();
                    double hitRate = totalRequests > 0 ? 
                        (cacheHits.get() * 100.0 / totalRequests) : 0.0;
                    stats.put("cacheHitRate", hitRate);
                    
                    // Average processing time
                    long avgProcessingTime = calculationTime.get() > 0 ? 
                        calculationTime.get() / Math.max(1, cacheMisses.get()) : 0;
                    stats.put("avgProcessingTime", avgProcessingTime);
                    
                } catch (Exception e) {
                    // Handle errors gracefully
                    stats.put("error", e.getMessage());
                }
                
                return stats;
            });
            
            try {
                Map<String, Object> stats = future.get(2, TimeUnit.SECONDS);
                statistics.putAll(stats);
                lastUpdateTime.set(System.currentTimeMillis());
                
                long calcTime = System.currentTimeMillis() - startTime;
                calculationTime.addAndGet(calcTime);
                
        } catch (TimeoutException e) {
            statistics.put("error", "Calculation timeout");
        } catch (Exception e) {
            statistics.put("error", e.getMessage());
        }
    }
    
    /**
     * Calculate grade distribution
     */
    private Map<String, Integer> calculateGradeDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("A", 0); // 90-100
        distribution.put("B", 0); // 80-89
        distribution.put("C", 0); // 70-79
        distribution.put("D", 0); // 60-69
        distribution.put("F", 0); // 0-59
        
        for (Grade grade : gradeManager.getGrades()) {
            if (grade != null) {
                double gradeValue = grade.getGrade();
                if (gradeValue >= 90) distribution.put("A", distribution.get("A") + 1);
                else if (gradeValue >= 80) distribution.put("B", distribution.get("B") + 1);
                else if (gradeValue >= 70) distribution.put("C", distribution.get("C") + 1);
                else if (gradeValue >= 60) distribution.put("D", distribution.get("D") + 1);
                else distribution.put("F", distribution.get("F") + 1);
            }
        }
        
        return distribution;
    }
    
    /**
     * Get all grades as array
     */
    private double[] getAllGradesArray() {
        List<Double> gradesList = new ArrayList<>();
        for (Grade grade : gradeManager.getGrades()) {
            if (grade != null) {
                gradesList.add(grade.getGrade());
            }
        }
        return gradesList.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    /**
     * Calculate mean
     */
    private double calculateMean(double[] grades) {
        if (grades.length == 0) return 0.0;
        double sum = 0;
        for (double grade : grades) {
            sum += grade;
        }
        return sum / grades.length;
    }
    
    /**
     * Calculate median
     */
    private double calculateMedian(double[] grades) {
        if (grades.length == 0) return 0.0;
        double[] sorted = grades.clone();
        Arrays.sort(sorted);
        if (sorted.length % 2 == 0) {
            return (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2.0;
        } else {
            return sorted[sorted.length / 2];
        }
    }
    
    /**
     * Calculate standard deviation
     */
    private double calculateStandardDeviation(double[] grades) {
        if (grades.length == 0) return 0.0;
        double mean = calculateMean(grades);
        double sumSquaredDiff = 0;
        for (double grade : grades) {
            sumSquaredDiff += Math.pow(grade - mean, 2);
        }
        return Math.sqrt(sumSquaredDiff / grades.length);
    }
    
    /**
     * Get top performers
     */
    private List<Map<String, Object>> getTopPerformers(int count) {
        List<Student> students = studentManager.getStudentsList();
        GPACalculator gpaCalc = new GPACalculator(gradeManager);
        
        return students.stream()
            .map(student -> {
                Map<String, Object> info = new HashMap<>();
                info.put("studentId", student.getStudentId());
                info.put("name", student.getName());
                info.put("average", student.calculateAverageGrade());
                info.put("gpa", gpaCalc.calculateCumulativeGPA(student.getStudentId()));
                return info;
            })
            .sorted((a, b) -> Double.compare(
                (Double)b.get("average"), 
                (Double)a.get("average")
            ))
            .limit(count)
            .collect(Collectors.toList());
    }
    
    /**
     * Update recent grades tracking
     */
    private void updateRecentGrades() {
        long fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000);
        
        // Count grades added in last 5 minutes
        int recentCount = 0;
        for (Grade grade : gradeManager.getGrades()) {
            if (grade != null) {
                // Use grade ID as key, store timestamp
                String gradeKey = grade.getGradeId();
                if (!recentGradeTimestamps.containsKey(gradeKey)) {
                    recentGradeTimestamps.put(gradeKey, System.currentTimeMillis());
                    recentCount++;
                } else {
                    long timestamp = recentGradeTimestamps.get(gradeKey);
                    if (timestamp >= fiveMinutesAgo) {
                        recentCount++;
                    }
                }
            }
        }
        
        // Clean old entries
        recentGradeTimestamps.entrySet().removeIf(entry -> entry.getValue() < fiveMinutesAgo);
        
        gradesAddedLast5Min.set(recentCount);
    }
    
    /**
     * Display the dashboard
     */
    public void displayDashboard() {
        // Don't clear screen - just print newlines to separate updates
        // This prevents interference with input handling
        System.out.println();
        System.out.println();
        
        System.out.println("REAL-TIME STATISTICS DASHBOARD");
        System.out.println("_______________________________________________");
        
        // Status line
        String threadStatus = isPaused.get() ? "PAUSED" : (isRunning.get() ? "RUNNING" : "STOPPED");
        System.out.printf("Auto-refresh: %s (%d sec) | Thread: %s\n", 
            isPaused.get() ? "Paused" : "Enabled", 
            refreshInterval.get(), 
            threadStatus);
        System.out.println("Press 'Q' to quit | 'R' to refresh now | 'P' to pause/resume");
        System.out.println();
        
        // Last updated timestamp
        if (lastUpdateTime.get() > 0) {
            LocalDateTime lastUpdate = LocalDateTime.ofEpochSecond(
                lastUpdateTime.get() / 1000, 0, 
                java.time.ZoneOffset.UTC
            );
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println("Last Updated: " + lastUpdate.format(formatter));
        } else {
            System.out.println("Last Updated: Calculating...");
        }
        System.out.println();
        
        // System Status
        System.out.println("SYSTEM STATUS");
        System.out.println("_______________________________________________");
        System.out.println("Total Students: " + statistics.getOrDefault("totalStudents", 0));
        System.out.println("Active Threads: " + statistics.getOrDefault("activeThreads", 0));
        System.out.printf("Cache Hit Rate: %.1f%%\n", 
            (Double)statistics.getOrDefault("cacheHitRate", 0.0));
        
        long memoryUsed = (Long)statistics.getOrDefault("memoryUsed", 0L);
        long memoryTotal = (Long)statistics.getOrDefault("memoryTotal", 1L);
        System.out.printf("Memory Usage: %s / %s\n", 
            formatMemory(memoryUsed), 
            formatMemory(memoryTotal));
        System.out.println();
        
        // Live Statistics
        System.out.println("LIVE STATISTICS");
        System.out.println("_______________________________________________");
        System.out.println("Total Grades: " + statistics.getOrDefault("totalGrades", 0));
        System.out.println("Grades Added (last 5 min): " + 
            statistics.getOrDefault("gradesAddedLast5Min", 0));
        System.out.println("Average Processing Time: " + 
            statistics.getOrDefault("avgProcessingTime", 0) + "ms");
        System.out.println();
        
        // Grade Distribution
        @SuppressWarnings("unchecked")
        Map<String, Integer> distribution = (Map<String, Integer>)statistics.get("gradeDistribution");
        if (distribution != null) {
            System.out.println("Grade Distribution (Live):");
            System.out.println("_______________________________________________");
            int total = distribution.values().stream().mapToInt(Integer::intValue).sum();
            if (total > 0) {
                displayGradeBar("90-100% (A)", distribution.get("A"), total);
                displayGradeBar("80-89% (B)", distribution.get("B"), total);
                displayGradeBar("70-79% (C)", distribution.get("C"), total);
                displayGradeBar("60-69% (D)", distribution.get("D"), total);
                displayGradeBar("0-59% (F)", distribution.get("F"), total);
            }
            System.out.println();
        }
        
        // Current Statistics
        System.out.println("Current Statistics:");
        System.out.println("_______________________________________________");
        double mean = (Double)statistics.getOrDefault("mean", 0.0);
        double median = (Double)statistics.getOrDefault("median", 0.0);
        double stdDev = (Double)statistics.getOrDefault("stdDev", 0.0);
        System.out.printf("Mean: %.1f%%\n", mean);
        System.out.printf("Median: %.1f%%\n", median);
        System.out.printf("Std Dev: %.1f%%\n", stdDev);
        System.out.println();
        
        // Top Performers
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topPerformers = (List<Map<String, Object>>)statistics.get("topPerformers");
        if (topPerformers != null && !topPerformers.isEmpty()) {
            System.out.println("Top Performers (Live Rankings):");
            System.out.println("_______________________________________________");
            int rank = 1;
            for (Map<String, Object> performer : topPerformers) {
                System.out.printf("%d. %s %s: %.1f%% GPA: %.2f\n",
                    rank++,
                    performer.get("studentId"),
                    performer.get("name"),
                    (Double)performer.get("average"),
                    (Double)performer.get("gpa")
                );
            }
            System.out.println();
        }
        
        // Auto-refresh countdown
        if (isRunning.get() && !isPaused.get()) {
            long timeSinceUpdate = System.currentTimeMillis() - lastUpdateTime.get();
            long timeUntilNext = (refreshInterval.get() * 1000) - timeSinceUpdate;
            if (timeUntilNext > 0) {
                System.out.println("Auto-refresh in: " + (timeUntilNext / 1000) + " seconds...");
            }
        }
    }
    
    /**
     * Display grade distribution bar
     */
    private void displayGradeBar(String label, int count, int total) {
        double percentage = total > 0 ? (count * 100.0 / total) : 0;
        int barLength = 30;
        int filled = (int)(percentage / 100.0 * barLength);
        
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            bar.append(i < filled ? "█" : " ");
        }
        
        System.out.printf("%-15s: %5.1f%% (%d grades) [%s]\n", 
            label, percentage, count, bar.toString());
    }
    
    /**
     * Format memory size
     */
    private String formatMemory(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.0f KB", bytes / 1024.0);
        return String.format("%.0f MB", bytes / (1024.0 * 1024.0));
    }
    
    /**
     * Get thread status
     */
    public String getThreadStatus() {
        if (isPaused.get()) return "PAUSED";
        if (isRunning.get()) return "RUNNING";
        return "STOPPED";
    }
    
    /**
     * Check if dashboard is running
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * Check if dashboard is paused
     */
    public boolean isPaused() {
        return isPaused.get();
    }
}

