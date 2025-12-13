package com.gradr;

import java.util.Comparator;

/**
 * Custom Comparator for sorting students by multiple criteria
 * 
 * Sorting Priority:
 * 1. Student Type (Honors before Regular)
 * 2. GPA/Average Grade (descending)
 * 3. Name (ascending alphabetical)
 * 4. Student ID (ascending)
 * 
 * Time Complexity: O(1) - Comparison operation is constant time
 */
public class StudentComparator implements Comparator<Student> {
    
    /**
     * Compare two students based on multiple criteria
     * Time Complexity: O(1) - Constant time comparison
     * 
     * @param s1 First student
     * @param s2 Second student
     * @return Negative if s1 < s2, positive if s1 > s2, zero if equal
     */
    @Override
    public int compare(Student s1, Student s2) {
        // Priority 1: Student Type (Honors before Regular)
        int typeComparison = s2.getStudentType().compareTo(s1.getStudentType());
        if (typeComparison != 0) {
            return typeComparison;
        }
        
        // Priority 2: GPA/Average Grade (descending - higher grades first)
        double gpa1 = s1.calculateAverageGrade();
        double gpa2 = s2.calculateAverageGrade();
        int gpaComparison = Double.compare(gpa2, gpa1); // Reverse order for descending
        if (gpaComparison != 0) {
            return gpaComparison;
        }
        
        // Priority 3: Name (ascending alphabetical)
        int nameComparison = s1.getName().compareToIgnoreCase(s2.getName());
        if (nameComparison != 0) {
            return nameComparison;
        }
        
        // Priority 4: Student ID (ascending)
        return s1.getStudentId().compareTo(s2.getStudentId());
    }
    
    /**
     * Create a comparator that sorts by GPA only (descending)
     * Time Complexity: O(1) - Comparison operation
     */
    public static Comparator<Student> byGPA() {
        return Comparator.comparing(Student::calculateAverageGrade).reversed();
    }
    
    /**
     * Create a comparator that sorts by name only (ascending)
     * Time Complexity: O(1) - Comparison operation
     */
    public static Comparator<Student> byName() {
        return Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER);
    }
    
    /**
     * Create a comparator that sorts by student type then GPA
     * Time Complexity: O(1) - Comparison operation
     */
    public static Comparator<Student> byTypeAndGPA() {
        return Comparator
                .comparing(Student::getStudentType)
                .thenComparing(Student::calculateAverageGrade, Comparator.reverseOrder());
    }
}

