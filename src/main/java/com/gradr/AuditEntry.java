package com.gradr;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AuditEntry - Serializable class for audit logging
 * Used with ConcurrentLinkedQueue for thread-safe, non-blocking audit logging
 */
public class AuditEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum AuditAction {
        STUDENT_ADDED,
        STUDENT_UPDATED,
        GRADE_RECORDED,
        GRADE_UPDATED,
        GRADE_DELETED,
        REPORT_EXPORTED,
        DATA_IMPORTED,
        SYSTEM_ACCESS
    }
    
    private AuditAction action;
    private String userId;
    private String studentId;
    private String details;
    private LocalDateTime timestamp;
    private String ipAddress; // Optional
    
    public AuditEntry(AuditAction action, String userId, String details) {
        this.action = action;
        this.userId = userId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    public AuditEntry(AuditAction action, String userId, String studentId, String details) {
        this(action, userId, details);
        this.studentId = studentId;
    }
    
    // Getters
    public AuditAction getAction() { return action; }
    public String getUserId() { return userId; }
    public String getStudentId() { return studentId; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getIpAddress() { return ipAddress; }
    
    // Setters
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s", 
            timestamp, action, userId != null ? userId : "SYSTEM", details);
    }
}

