package com.gradr;

/**
 * Task class for PriorityQueue-based task scheduling
 * Tasks are prioritized based on priority level and timestamp
 */
public class Task implements Comparable<Task> {
    public enum TaskType {
        GRADE_PROCESSING(1),    // Highest priority
        REPORT_GENERATION(2),
        STATISTICS_CALCULATION(3),
        DATA_EXPORT(4),
        CLEANUP(5);            // Lowest priority
        
        private final int priority;
        
        TaskType(int priority) {
            this.priority = priority;
        }
        
        public int getPriority() {
            return priority;
        }
    }
    
    private TaskType taskType;
    private String description;
    private long timestamp;
    private String studentId; // Optional: for student-specific tasks
    
    public Task(TaskType taskType, String description) {
        this.taskType = taskType;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Task(TaskType taskType, String description, String studentId) {
        this(taskType, description);
        this.studentId = studentId;
    }
    
    /**
     * Compare tasks for PriorityQueue ordering
     * Lower priority number = higher priority
     * Time Complexity: O(1) - Constant time comparison
     */
    @Override
    public int compareTo(Task other) {
        // First compare by priority (lower number = higher priority)
        int priorityComparison = Integer.compare(this.taskType.getPriority(), other.taskType.getPriority());
        if (priorityComparison != 0) {
            return priorityComparison;
        }
        
        // If same priority, earlier timestamp = higher priority
        return Long.compare(this.timestamp, other.timestamp);
    }
    
    // Getters
    public TaskType getTaskType() {
        return taskType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    @Override
    public String toString() {
        return String.format("Task[Type: %s, Description: %s, Timestamp: %d]", 
                taskType, description, timestamp);
    }
}

