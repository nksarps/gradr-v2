package com.gradr;

import com.gradr.exceptions.StudentNotFoundException;

import java.util.*;

/**
 * StudentManager - Facade for student management operations
 * Refactored to adhere to Single Responsibility Principle
 * Delegates to specialized services for different responsibilities
 * 
 * Design Pattern: Facade Pattern
 * - Provides a unified interface to a set of interfaces in a subsystem
 * - Delegates to StudentRepository and StudentStatistics
 * 
 * Thread Safety:
 * - Thread-safety is handled by individual services
 */
class StudentManager {
    // Specialized services (adhering to SRP)
    private final StudentRepository studentRepository;
    private final StudentStatistics studentStatistics;
    
    /**
     * Constructor - initializes all services
     */
    public StudentManager() {
        this.studentRepository = new StudentRepository();
        this.studentStatistics = new StudentStatistics(studentRepository);
    }
    
    /**
     * Constructor with dependency injection (for testing and flexibility)
     */
    public StudentManager(StudentRepository studentRepository, StudentStatistics studentStatistics) {
        this.studentRepository = studentRepository;
        this.studentStatistics = studentStatistics;
    }

    /**
     * Add student - delegates to repository
     */
    public void addStudent(Student student) {
        studentRepository.addStudent(student);
    }

    /**
     * Find student by ID - delegates to repository
     */
    public Student findStudent(String studentId) throws StudentNotFoundException {
        return studentRepository.findStudent(studentId);
    }

    /**
     * View all students - delegates to repository
     */
    public void viewAllStudents() {
        studentRepository.viewAllStudents();
    }

    /**
     * Get student count - delegates to repository
     */
    public int getStudentCount() {
        return studentRepository.getStudentCount();
    }

    /**
     * Get all students as array - delegates to repository
     */
    public Student[] getStudents() {
        return studentRepository.getStudents();
    }
    
    /**
     * Get all students as list - delegates to repository
     */
    public List<Student> getStudentsList() {
        return studentRepository.getStudentsList();
    }

    /**
     * Calculate class average - delegates to statistics
     */
    public double calculateClassAverage() {
        return studentStatistics.calculateClassAverage();
    }
}