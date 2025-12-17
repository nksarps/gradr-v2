package com.gradr;

import com.gradr.exceptions.StudentNotFoundException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StudentRepository - Responsible for student storage and retrieval operations
 * Adheres to Single Responsibility Principle by focusing only on data persistence
 * Adheres to Interface Segregation Principle by implementing fine-grained interfaces
 * 
 * Responsibilities:
 * - Store students in collections
 * - Retrieve students by ID
 * - Manage student list
 * 
 * Thread Safety:
 * - ConcurrentHashMap for O(1) thread-safe student lookup by ID
 * - Collections.synchronizedList for maintaining insertion order
 */
public class StudentRepository implements IStudentReader, IStudentWriter {
    // ConcurrentHashMap for O(1) thread-safe student lookup by ID
    private final Map<String, Student> studentsMap = new ConcurrentHashMap<>();
    
    // Synchronized list to maintain insertion order for iteration
    private final List<Student> studentsList = Collections.synchronizedList(new ArrayList<>());

    /**
     * Add student to repository
     * Time Complexity: O(1) average case
     * Thread-safe: Synchronized to ensure atomic update
     */
    public synchronized void addStudent(Student student) {
        studentsMap.put(student.getStudentId(), student);
        studentsList.add(student);
    }

    /**
     * Find student by ID
     * Time Complexity: O(1) average case
     * Thread-safe: ConcurrentHashMap get operation is thread-safe
     */
    public Student findStudent(String studentId) throws StudentNotFoundException {
        Student student = studentsMap.get(studentId);
        
        if (student == null) {
            throw new StudentNotFoundException(
                    "X ERROR: StudentNotFoundException\n   Student with ID '" + studentId + "' does not exist"
            );
        }
        
        return student;
    }

    /**
     * Get student count
     * Time Complexity: O(1)
     */
    public int getStudentCount() {
        return studentsMap.size();
    }

    /**
     * Get all students as array (for backward compatibility)
     * Time Complexity: O(n)
     */
    public Student[] getStudents() {
        return studentsList.toArray(new Student[0]);
    }
    
    /**
     * Get all students as list
     * Time Complexity: O(1) - Returns copy of list
     */
    public List<Student> getStudentsList() {
        return new ArrayList<>(studentsList);
    }

    /**
     * View all students (for debugging)
     * Time Complexity: O(n)
     */
    public synchronized void viewAllStudents() {
        System.out.println(studentsList.toString());
    }
}
