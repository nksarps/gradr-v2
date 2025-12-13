package com.gradr;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ScheduledTask - Represents a scheduled automated task
 * 
 * Features:
 * - Task scheduling information (daily, hourly, weekly)
 * - Execution history and status tracking
 * - Next execution time calculation
 * - Task configuration and metadata
 */
public class ScheduledTask implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum ScheduleType {
        DAILY, HOURLY, WEEKLY
    }
    
    public enum TaskStatus {
        PENDING, RUNNING, SUCCESS, FAILED
    }
    
    private String taskId;
    private String taskName;
    private ScheduleType scheduleType;
    private int hour; // 0-23
    private int minute; // 0-59
    private int dayOfWeek; // 1-7 (Monday=1, Sunday=7) for weekly tasks
    
    // Execution tracking
    private LocalDateTime lastRunTime;
    private LocalDateTime nextRunTime;
    private TaskStatus lastStatus;
    private long lastExecutionDuration; // milliseconds
    private String lastErrorMessage;
    
    // Task configuration
    private String scope; // e.g., "All Students", "Honors Only"
    private int threadCount;
    private String notificationEmail;
    private boolean emailNotification;
    private boolean logToFile;
    
    // Task metadata
    private LocalDateTime createdAt;
    private boolean active;
    
    private static int taskCounter = 0;
    
    public ScheduledTask(String taskName, ScheduleType scheduleType, int hour, int minute) {
        this.taskId = "TASK-" + String.format("%03d", ++taskCounter);
        this.taskName = taskName;
        this.scheduleType = scheduleType;
        this.hour = hour;
        this.minute = minute;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.lastStatus = TaskStatus.PENDING;
        calculateNextRunTime();
    }
    
    /**
     * Calculate next execution time based on schedule type
     */
    public void calculateNextRunTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        
        switch (scheduleType) {
            case DAILY:
                // Next occurrence today or tomorrow
                if (next.isBefore(now) || next.isEqual(now)) {
                    next = next.plusDays(1);
                }
                break;
            case HOURLY:
                // Next hour at :minute
                next = now.withMinute(minute).withSecond(0).withNano(0);
                if (next.isBefore(now) || next.isEqual(now)) {
                    next = next.plusHours(1);
                }
                break;
            case WEEKLY:
                // Next occurrence on specified day of week
                int currentDayOfWeek = now.getDayOfWeek().getValue();
                int daysUntilNext = dayOfWeek - currentDayOfWeek;
                if (daysUntilNext < 0) {
                    daysUntilNext += 7; // Next week
                } else if (daysUntilNext == 0 && next.isBefore(now)) {
                    daysUntilNext = 7; // Today but time has passed, so next week
                }
                next = next.plusDays(daysUntilNext);
                break;
        }
        
        this.nextRunTime = next;
    }
    
    /**
     * Get countdown to next execution
     */
    public String getCountdown() {
        if (nextRunTime == null) {
            return "Not scheduled";
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (nextRunTime.isBefore(now)) {
            calculateNextRunTime(); // Recalculate if time has passed
        }
        
        java.time.Duration duration = java.time.Duration.between(now, nextRunTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Record task execution
     */
    public void recordExecution(TaskStatus status, long duration, String errorMessage) {
        this.lastRunTime = LocalDateTime.now();
        this.lastStatus = status;
        this.lastExecutionDuration = duration;
        this.lastErrorMessage = errorMessage;
        calculateNextRunTime(); // Calculate next run time
    }
    
    // Getters
    public String getTaskId() { return taskId; }
    public String getTaskName() { return taskName; }
    public ScheduleType getScheduleType() { return scheduleType; }
    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public int getDayOfWeek() { return dayOfWeek; }
    public LocalDateTime getLastRunTime() { return lastRunTime; }
    public LocalDateTime getNextRunTime() { return nextRunTime; }
    public TaskStatus getLastStatus() { return lastStatus; }
    public long getLastExecutionDuration() { return lastExecutionDuration; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public String getScope() { return scope; }
    public int getThreadCount() { return threadCount; }
    public String getNotificationEmail() { return notificationEmail; }
    public boolean isEmailNotification() { return emailNotification; }
    public boolean isLogToFile() { return logToFile; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }
    
    // Setters
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setScope(String scope) { this.scope = scope; }
    public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
    public void setNotificationEmail(String notificationEmail) { this.notificationEmail = notificationEmail; }
    public void setEmailNotification(boolean emailNotification) { this.emailNotification = emailNotification; }
    public void setLogToFile(boolean logToFile) { this.logToFile = logToFile; }
    public void setActive(boolean active) { this.active = active; }
    
    /**
     * Get schedule description
     */
    public String getScheduleDescription() {
        switch (scheduleType) {
            case DAILY:
                return String.format("Every day at %02d:%02d", hour, minute);
            case HOURLY:
                return String.format("Every hour at :%02d", minute);
            case WEEKLY:
                String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                return String.format("Every %s at %02d:%02d", days[dayOfWeek], hour, minute);
            default:
                return "Unknown schedule";
        }
    }
    
    /**
     * Get formatted last run time
     */
    public String getFormattedLastRunTime() {
        if (lastRunTime == null) {
            return "Never";
        }
        return lastRunTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * Get formatted next run time
     */
    public String getFormattedNextRunTime() {
        if (nextRunTime == null) {
            return "Not scheduled";
        }
        return nextRunTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

