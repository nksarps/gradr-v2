package com.gradr;

import com.gradr.exceptions.StudentNotFoundException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StudentManager - Optimized with ConcurrentHashMap for O(1) thread-safe student lookup by ID
 * 
 * Collection Optimization:
 * - ConcurrentHashMap<String, Student>: O(1) average case lookup, insertion, and deletion (thread-safe)
 * - Collections.synchronizedList<Student>: Maintains insertion order for iteration (thread-safe)
 * 
 * Thread Safety:
 * - ConcurrentHashMap: Thread-safe for concurrent lookups and modifications
 * - Collections.synchronizedList: Thread-safe list with synchronized access
 * - All modification operations are synchronized to ensure atomic updates
 */
class StudentManager {
    // ConcurrentHashMap for O(1) thread-safe student lookup by ID
    // Time Complexity: O(1) average case for get, put, remove operations
    // Thread-safe: Supports concurrent access without external synchronization
    private final Map<String, Student> studentsMap = new ConcurrentHashMap<>();
    
    // Synchronized list to maintain insertion order for iteration
    // Time Complexity: O(1) amortized for add, O(n) for iteration
    // Thread-safe: All operations are synchronized
    private final List<Student> studentsList = Collections.synchronizedList(new ArrayList<>());

    /**
     * Add student to both ConcurrentHashMap and synchronized ArrayList
     * Time Complexity: O(1) average case (ConcurrentHashMap put) + O(1) amortized (ArrayList add)
     * Thread-safe: Synchronized to ensure atomic update of both collections
     */
    public synchronized void addStudent(Student student) {
        // O(1) average case - ConcurrentHashMap put operation (thread-safe)
        studentsMap.put(student.getStudentId(), student);
        // O(1) amortized - Synchronized ArrayList add operation (thread-safe)
        studentsList.add(student);
    }

    /**
     * Find student by ID using ConcurrentHashMap
     * Time Complexity: O(1) average case (ConcurrentHashMap get operation)
     * Previous implementation: O(n) linear search through array
     * Thread-safe: ConcurrentHashMap get operation is thread-safe
     */
    public Student findStudent(String studentId) throws StudentNotFoundException {
        // O(1) average case - ConcurrentHashMap get operation (thread-safe, no blocking)
        Student student = studentsMap.get(studentId);
        
        if (student == null) {
            throw new StudentNotFoundException(
                    "X ERROR: StudentNotFoundException\n   Student with ID '" + studentId + "' does not exist"
            );
        }
        
        return student;
    }

    /**
     * View all students
     * Time Complexity: O(n) where n is the number of students
     * Thread-safe: Synchronized to prevent ConcurrentModificationException during iteration
     */
    public synchronized void viewAllStudents() {
        System.out.println(studentsList.toString());
    }

    /**
     * Get student count
     * Time Complexity: O(1) - HashMap size operation
     */
    public int getStudentCount() {
        return studentsMap.size();
    }

    /**
     * Get all students as array (for backward compatibility)
     * Time Complexity: O(n) where n is the number of students
     * @return Array of students maintaining insertion order
     */
    public Student[] getStudents() {
        // O(n) - Convert ArrayList to array
        return studentsList.toArray(new Student[0]);
    }
    
    /**
     * Get all students as list (optimized version)
     * Time Complexity: O(1) - Returns reference to list
     * @return List of students maintaining insertion order
     */
    public List<Student> getStudentsList() {
        return new ArrayList<>(studentsList); // Return copy to prevent external modification
    }

    /**
     * Calculate class average
     * Time Complexity: O(n) where n is the number of students
     * Thread-safe: Synchronized to prevent ConcurrentModificationException during iteration
     */
    public synchronized double calculateClassAverage() {
        if (studentsList.isEmpty()) {
            return 0.0;
        }
        
        double totalAverage = 0;
        // O(n) - Iterate through all students (synchronized list iteration)
        synchronized (studentsList) {
            for (Student student : studentsList) {
                totalAverage += student.calculateAverageGrade();
            }
        }

        return totalAverage / getStudentCount();
    }
}