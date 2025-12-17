package com.gradr;

import java.util.*;

/**
 * GradeCalculator - Responsible for all grade-related calculations
 * Adheres to Single Responsibility Principle by focusing only on computations
 * Adheres to Dependency Inversion Principle by depending on IGradeReader abstraction
 * 
 * Responsibilities:
 * - Calculate averages (core, elective, overall)
 * - Generate grade reports
 * - Compute statistics
 * 
 * Dependencies:
 * - Depends on IGradeReader abstraction for data access
 */
public class GradeCalculator implements IGradeCalculator {
    private final IGradeReader gradeRepository;
    
    /**
     * Constructor with dependency injection (depends on abstraction)
     */
    public GradeCalculator(IGradeReader gradeRepository) {
        this.gradeRepository = gradeRepository;
    }
    
    /**
     * Calculate core subject average for a student
     * Time Complexity: O(n) where n is the number of grades
     */
    public double calculateCoreAverage(String studentId) {
        double gradeSum = 0;
        int totalCourses = 0;
        
        List<Grade> studentGrades = gradeRepository.getGradesByStudent(studentId);
        for (Grade grade : studentGrades) {
            if (grade.getSubject().getSubjectType().equals("Core")) {
                gradeSum += grade.getGrade();
                totalCourses++;
            }
        }
        
        if (totalCourses == 0) return 0.0;
        return gradeSum / totalCourses;
    }
    
    /**
     * Calculate elective subject average for a student
     * Time Complexity: O(n) where n is the number of grades
     */
    public double calculateElectiveAverage(String studentId) {
        double gradeSum = 0;
        int totalCourses = 0;
        
        List<Grade> studentGrades = gradeRepository.getGradesByStudent(studentId);
        for (Grade grade : studentGrades) {
            if (grade.getSubject().getSubjectType().equals("Elective")) {
                gradeSum += grade.getGrade();
                totalCourses++;
            }
        }
        
        if (totalCourses == 0) return 0.0;
        return gradeSum / totalCourses;
    }
    
    /**
     * Calculate overall average for a student
     * Time Complexity: O(n) where n is the number of grades
     */
    public double calculateOverallAverage(String studentId) {
        double gradeSum = 0;
        int totalCourses = 0;
        
        List<Grade> studentGrades = gradeRepository.getGradesByStudent(studentId);
        for (Grade grade : studentGrades) {
            gradeSum += grade.getGrade();
            totalCourses++;
        }
        
        if (totalCourses == 0) return 0.0;
        return gradeSum / totalCourses;
    }
    
    /**
     * View grades by student ID and generate formatted report
     * Time Complexity: O(n) where n is the number of grades
     */
    public String viewGradesByStudent(String studentId) {
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        int totalCourses = 0;
        
        List<Grade> studentGrades = gradeRepository.getGradesByStudent(studentId);
        
        for (Grade grade : studentGrades) {
            totalCourses++;
            
            if (!found) {
                sb.append("GRADE HISTORY\n");
                sb.append("-------------------------------------------------------------------------------------\n");
                sb.append("GRD ID   | DATE       | SUBJECT          | TYPE       | GRADE\n");
                sb.append("-------------------------------------------------------------------------------------\n");
                found = true;
            }
            
            sb.append(String.format("%-9s | %-10s | %-16s | %-10s | %-5.1f%%\n",
                    grade.getGradeId(),
                    grade.getDate(),
                    grade.getSubject().getSubjectName(),
                    grade.getSubject().getSubjectType(),
                    grade.getGrade()));
        }
        
        if (!found) {
            sb.append("_______________________________________________\n");
            sb.append("No grades recorded for this student\n");
            sb.append("_______________________________________________\n\n");
        } else {
            sb.append("\n");
            sb.append(String.format("Total Grades: %d\n", totalCourses));
            sb.append(String.format("Core Subjects Average: %.1f%%\n", calculateCoreAverage(studentId)));
            sb.append(String.format("Elective Subjects Average: %.1f%%\n", calculateElectiveAverage(studentId)));
            sb.append(String.format("Overall Average: %.1f%%\n", calculateOverallAverage(studentId)));
        }
        
        return sb.toString();
    }
}
