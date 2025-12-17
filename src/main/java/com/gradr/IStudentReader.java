package com.gradr;

import com.gradr.exceptions.StudentNotFoundException;
import java.util.List;

/**
 * IStudentReader - Interface for reading student data
 * Adheres to Interface Segregation Principle
 * Clients that only need to read students don't need write operations
 */
public interface IStudentReader {
    /**
     * Find student by ID
     */
    Student findStudent(String studentId) throws StudentNotFoundException;
    
    /**
     * Get student count
     */
    int getStudentCount();
    
    /**
     * Get all students as list
     */
    List<Student> getStudentsList();
    
    /**
     * Get all students as array
     */
    Student[] getStudents();
}
