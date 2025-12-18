package com.gradr;

import java.util.List;
import java.util.Map;

/**
 * IGPARankingService - Interface for GPA ranking operations
 * Adheres to Dependency Inversion Principle
 */
public interface IGPARankingService {
    /**
     * Update GPA ranking for a student
     */
    void updateGPARanking(Student student, double gpa);
    
    /**
     * Get sorted GPA rankings
     */
    Map<Double, List<Student>> getGPARankings();
}
