package com.gradr;

/**
 * IStudentWriter - Interface for writing student data
 * Adheres to Interface Segregation Principle
 * Clients that only need to add students don't need read operations
 */
public interface IStudentWriter {
    /**
     * Add student to repository
     */
    void addStudent(Student student);
}
