package com.gradr;

/**
 * IGradeWriter - Interface for writing grade data
 * Adheres to Interface Segregation Principle
 * Clients that only need to add/remove grades don't need read operations
 */
public interface IGradeWriter {
    /**
     * Add grade to repository
     */
    void addGrade(Grade grade);
    
    /**
     * Remove grade from repository
     */
    boolean removeGrade(Grade grade);
}
