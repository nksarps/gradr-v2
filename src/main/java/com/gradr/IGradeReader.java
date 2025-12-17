package com.gradr;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * IGradeReader - Interface for reading grade data
 * Adheres to Interface Segregation Principle
 * Clients that only need to read grades don't need write operations
 */
public interface IGradeReader {
    /**
     * Get grades by student
     */
    List<Grade> getGradesByStudent(String studentId);
    
    /**
     * Get grades by subject
     */
    List<Grade> getGradesBySubject(String subjectName);
    
    /**
     * Get all grades
     */
    List<Grade> getAllGrades();
    
    /**
     * Get all grades as array
     */
    Grade[] getGrades();
    
    /**
     * Get grade count
     */
    int getGradeCount();
    
    /**
     * Get enrolled subjects count for student
     */
    int getEnrolledSubjectsCount(String studentId);
    
    /**
     * Get unique courses
     */
    Set<String> getUniqueCourses();
    
    /**
     * Get subject grades map
     */
    Map<String, List<Grade>> getSubjectGrades();
}
