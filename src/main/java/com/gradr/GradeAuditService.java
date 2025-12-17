package com.gradr;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * GradeAuditService - Responsible for audit logging of grade operations
 * Adheres to Single Responsibility Principle by focusing only on auditing
 * Adheres to Dependency Inversion Principle by implementing IGradeAuditService interface
 * 
 * Responsibilities:
 * - Log grade operations (add, remove, update)
 * - Maintain audit trail
 * - Provide audit log access
 * 
 * Thread Safety:
 * - ConcurrentLinkedQueue for non-blocking, thread-safe audit logging
 */
public class GradeAuditService implements IGradeAuditService {
    // ConcurrentLinkedQueue for thread-safe, non-blocking audit logging
    private final Queue<AuditEntry> auditLog = new ConcurrentLinkedQueue<>();
    
    /**
     * Log grade addition
     */
    public void logGradeAdded(Grade grade) {
        auditLog.offer(new AuditEntry(
            AuditEntry.AuditAction.GRADE_RECORDED,
            "SYSTEM",
            grade.getStudentId(),
            String.format("Grade %s recorded for %s", 
                grade.getGradeId(), 
                grade.getSubject().getSubjectName())
        ));
    }
    
    /**
     * Log grade deletion
     */
    public void logGradeDeleted(Grade grade) {
        auditLog.offer(new AuditEntry(
            AuditEntry.AuditAction.GRADE_DELETED,
            "SYSTEM",
            grade.getStudentId(),
            String.format("Grade %s deleted", grade.getGradeId())
        ));
    }
    
    /**
     * Add custom audit entry
     * Time Complexity: O(1) - ConcurrentLinkedQueue offer operation
     */
    public void addAuditEntry(AuditEntry entry) {
        auditLog.offer(entry);
    }
    
    /**
     * Get all audit entries (for processing by background thread)
     * Time Complexity: O(n) where n is the number of entries
     */
    public List<AuditEntry> drainAuditLog() {
        List<AuditEntry> entries = new ArrayList<>();
        AuditEntry entry;
        while ((entry = auditLog.poll()) != null) {
            entries.add(entry);
        }
        return entries;
    }
    
    /**
     * Get current audit log size
     * Time Complexity: O(1) - Queue size operation
     */
    public int getAuditLogSize() {
        return auditLog.size();
    }
}
