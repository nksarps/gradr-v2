package com.gradr;

/**
 * IGradeAuditService - Interface for audit operations
 * Adheres to Dependency Inversion Principle
 */
public interface IGradeAuditService {
    /**
     * Log grade addition
     */
    void logGradeAdded(Grade grade);
    
    /**
     * Log grade deletion
     */
    void logGradeDeleted(Grade grade);
    
    /**
     * Add custom audit entry
     */
    void addAuditEntry(AuditEntry entry);
}
