package com.gradr;

/**
 * IGradeCalculator - Interface for grade calculation operations
 * Adheres to Dependency Inversion Principle
 * High-level modules depend on this abstraction, not on concrete GradeCalculator
 */
public interface IGradeCalculator {
    /**
     * Calculate core subject average for a student
     */
    double calculateCoreAverage(String studentId);
    
    /**
     * Calculate elective subject average for a student
     */
    double calculateElectiveAverage(String studentId);
    
    /**
     * Calculate overall average for a student
     */
    double calculateOverallAverage(String studentId);
    
    /**
     * View grades by student ID and generate formatted report
     */
    String viewGradesByStudent(String studentId);
}
