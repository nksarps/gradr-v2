package com.gradr;

import java.util.*;

/**
 * StudentStatistics - Responsible for calculating student statistics
 * Adheres to Single Responsibility Principle by focusing only on statistical computations
 * 
 * Responsibilities:
 * - Calculate class averages
 * - Calculate student performance metrics
 * - Generate statistical reports
 * 
 * Dependencies:
 * - Depends on StudentRepository for data access
 */
public class StudentStatistics {
    private final StudentRepository studentRepository;
    
    /**
     * Constructor with dependency injection
     */
    public StudentStatistics(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
    
    /**
     * Calculate class average
     * Time Complexity: O(n) where n is the number of students
     */
    public double calculateClassAverage() {
        List<Student> studentsList = studentRepository.getStudentsList();
        
        if (studentsList.isEmpty()) {
            return 0.0;
        }
        
        double totalAverage = 0;
        for (Student student : studentsList) {
            totalAverage += student.calculateAverageGrade();
        }

        return totalAverage / studentRepository.getStudentCount();
    }
    
    /**
     * Get student count (for convenience)
     * Time Complexity: O(1)
     */
    public int getStudentCount() {
        return studentRepository.getStudentCount();
    }
}
