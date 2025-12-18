package com.gradr;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AuditTrailViewer - View and search audit trail entries
 * 
 * Features:
 * - Display recent audit entries with filtering
 * - Search by date range, operation type, thread ID
 * - Display audit statistics (operations per hour, average execution time)
 */
public class AuditTrailViewer {
    
    private final AuditLogger auditLogger;
    
    public AuditTrailViewer(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }
    
    /**
     * Get recent audit entries
     */
    public List<AuditLogger.EnhancedAuditEntry> getRecentEntries(int count) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7); // Last 7 days
        
        List<AuditLogger.EnhancedAuditEntry> entries = auditLogger.readAuditEntries(startDate, endDate);
        
        // Sort by timestamp descending (most recent first)
        entries.sort((a, b) -> {
            LocalDateTime timeA = LocalDateTime.parse(a.getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime timeB = LocalDateTime.parse(b.getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return timeB.compareTo(timeA);
        });
        
        return entries.stream().limit(count).collect(Collectors.toList());
    }
    
    /**
     * Filter entries by operation type
     */
    public List<AuditLogger.EnhancedAuditEntry> filterByOperationType(
            List<AuditLogger.EnhancedAuditEntry> entries, String operationType) {
        return entries.stream()
            .filter(e -> e.getOperationType().equalsIgnoreCase(operationType))
            .collect(Collectors.toList());
    }
    
    /**
     * Filter entries by thread ID
     */
    public List<AuditLogger.EnhancedAuditEntry> filterByThreadId(
            List<AuditLogger.EnhancedAuditEntry> entries, long threadId) {
        return entries.stream()
            .filter(e -> e.getThreadId() == threadId)
            .collect(Collectors.toList());
    }
    
    /**
     * Filter entries by date range
     */
    public List<AuditLogger.EnhancedAuditEntry> filterByDateRange(
            List<AuditLogger.EnhancedAuditEntry> entries, 
            LocalDateTime startDate, LocalDateTime endDate) {
        return entries.stream()
            .filter(e -> {
                LocalDateTime entryTime = LocalDateTime.parse(
                    e.getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return !entryTime.isBefore(startDate) && !entryTime.isAfter(endDate);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Filter entries by success status
     */
    public List<AuditLogger.EnhancedAuditEntry> filterBySuccessStatus(
            List<AuditLogger.EnhancedAuditEntry> entries, boolean successOnly) {
        if (!successOnly) return entries;
        return entries.stream()
            .filter(AuditLogger.EnhancedAuditEntry::isSuccess)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate audit statistics
     */
    public AuditStatistics calculateStatistics(List<AuditLogger.EnhancedAuditEntry> entries) {
        if (entries.isEmpty()) {
            return new AuditStatistics(0, 0, 0, 0, 0, 0, 0);
        }
        
        long totalOperations = entries.size();
        long successfulOperations = entries.stream().filter(AuditLogger.EnhancedAuditEntry::isSuccess).count();
        long failedOperations = totalOperations - successfulOperations;
        
        // Calculate average execution time
        double avgExecutionTime = entries.stream()
            .mapToLong(AuditLogger.EnhancedAuditEntry::getExecutionTime)
            .average()
            .orElse(0.0);
        
        // Group by hour for operations per hour
        Map<Integer, Long> operationsPerHour = entries.stream()
            .collect(Collectors.groupingBy(
                e -> {
                    LocalDateTime time = LocalDateTime.parse(
                        e.getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    return time.getHour();
                },
                Collectors.counting()
            ));
        
        // Calculate operations per hour (average)
        double avgOperationsPerHour = operationsPerHour.values().stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        // Find min and max execution times
        long minExecutionTime = entries.stream()
            .mapToLong(AuditLogger.EnhancedAuditEntry::getExecutionTime)
            .min()
            .orElse(0);
        
        long maxExecutionTime = entries.stream()
            .mapToLong(AuditLogger.EnhancedAuditEntry::getExecutionTime)
            .max()
            .orElse(0);
        
        return new AuditStatistics(
            totalOperations,
            successfulOperations,
            failedOperations,
            avgExecutionTime,
            avgOperationsPerHour,
            minExecutionTime,
            maxExecutionTime
        );
    }
    
    /**
     * Get unique operation types
     */
    public Set<String> getOperationTypes(List<AuditLogger.EnhancedAuditEntry> entries) {
        return entries.stream()
            .map(AuditLogger.EnhancedAuditEntry::getOperationType)
            .collect(Collectors.toSet());
    }
    
    /**
     * Get unique thread IDs
     */
    public Set<Long> getThreadIds(List<AuditLogger.EnhancedAuditEntry> entries) {
        return entries.stream()
            .map(AuditLogger.EnhancedAuditEntry::getThreadId)
            .collect(Collectors.toSet());
    }
    
    /**
     * AuditStatistics - Statistics about audit entries
     */
    public static class AuditStatistics {
        private final long totalOperations;
        private final long successfulOperations;
        private final long failedOperations;
        private final double avgExecutionTime;
        private final double avgOperationsPerHour;
        private final long minExecutionTime;
        private final long maxExecutionTime;
        
        public AuditStatistics(long totalOperations, long successfulOperations, 
                             long failedOperations, double avgExecutionTime,
                             double avgOperationsPerHour, long minExecutionTime, 
                             long maxExecutionTime) {
            this.totalOperations = totalOperations;
            this.successfulOperations = successfulOperations;
            this.failedOperations = failedOperations;
            this.avgExecutionTime = avgExecutionTime;
            this.avgOperationsPerHour = avgOperationsPerHour;
            this.minExecutionTime = minExecutionTime;
            this.maxExecutionTime = maxExecutionTime;
        }
        
        // Getters
        public long getTotalOperations() { return totalOperations; }
        public long getSuccessfulOperations() { return successfulOperations; }
        public long getFailedOperations() { return failedOperations; }
        public double getAvgExecutionTime() { return avgExecutionTime; }
        public double getAvgOperationsPerHour() { return avgOperationsPerHour; }
        public long getMinExecutionTime() { return minExecutionTime; }
        public long getMaxExecutionTime() { return maxExecutionTime; }
        
        public double getSuccessRate() {
            return totalOperations > 0 ? (successfulOperations * 100.0 / totalOperations) : 0.0;
        }
    }
}

