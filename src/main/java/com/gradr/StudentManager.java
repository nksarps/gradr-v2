package com.gradr;

import com.gradr.exceptions.StudentNotFoundException;

import java.util.*;

/**
 * StudentManager - Optimized with HashMap for O(1) student lookup by ID
 * 
 * Collection Optimization:
 * - HashMap<String, Student>: O(1) average case lookup, insertion, and deletion
 * - ArrayList<Student>: Maintains insertion order for iteration
 */
class StudentManager {
    // HashMap for O(1) student lookup by ID
    // Time Complexity: O(1) average case for get, put, remove operations
    private Map<String, Student> studentsMap = new HashMap<>();
    
    // ArrayList to maintain insertion order for iteration
    // Time Complexity: O(1) amortized for add, O(n) for iteration
    private List<Student> studentsList = new ArrayList<>();

    /**
     * Add student to both HashMap and ArrayList
     * Time Complexity: O(1) average case (HashMap put) + O(1) amortized (ArrayList add)
     */
    public void addStudent(Student student) {
        // O(1) average case - HashMap put operation
        studentsMap.put(student.getStudentId(), student);
        // O(1) amortized - ArrayList add operation
        studentsList.add(student);
    }

    /**
     * Find student by ID using HashMap
     * Time Complexity: O(1) average case (HashMap get operation)
     * Previous implementation: O(n) linear search through array
     */
    public Student findStudent(String studentId) throws StudentNotFoundException {
        // O(1) average case - HashMap get operation
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
     */
    public void viewAllStudents() {
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
     */
    public double calculateClassAverage() {
        if (studentsList.isEmpty()) {
            return 0.0;
        }
        
        double totalAverage = 0;
        // O(n) - Iterate through all students
        for (Student student : studentsList) {
            totalAverage += student.calculateAverageGrade();
        }

        return totalAverage / getStudentCount();
    }
}