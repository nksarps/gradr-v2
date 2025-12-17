package com.gradr;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * GradeRepository - Responsible for grade storage and retrieval operations
 * Adheres to Single Responsibility Principle by focusing only on data persistence
 * Adheres to Interface Segregation Principle by implementing fine-grained interfaces
 * 
 * Responsibilities:
 * - Store grades in various collections for efficient access
 * - Retrieve grades by student ID, subject, etc.
 * - Manage grade history
 * - Track unique courses
 * 
 * Thread Safety:
 * - All collections are synchronized or concurrent
 * - Modification operations are synchronized
 */
public class GradeRepository implements IGradeReader, IGradeWriter {
    // Synchronized LinkedList for grade history - optimized for frequent insertions/deletions
    // Time Complexity: O(1) for add/remove at ends, O(n) for search
    private final List<Grade> gradeHistory = Collections.synchronizedList(new LinkedList<>());
    
    // Synchronized TreeMap for organizing grades by subject name
    // Time Complexity: O(log n) for put, get, remove operations
    private final Map<String, List<Grade>> subjectGrades = Collections.synchronizedMap(new TreeMap<>());
    
    // Synchronized HashSet for tracking unique courses enrolled across all students
    // Time Complexity: O(1) average case for add, contains, remove operations
    private final Set<String> uniqueCourses = Collections.synchronizedSet(new HashSet<>());
    
    /**
     * Add grade to repository
     * Time Complexity: O(1) - LinkedList add + O(log n) - TreeMap put
     * Thread-safe: Synchronized to ensure atomic update of all collections
     */
    public synchronized void addGrade(Grade grade) {
        // O(1) - Synchronized LinkedList add operation
        gradeHistory.add(grade);
        
        // O(log n) - Synchronized TreeMap put operation
        String subjectName = grade.getSubject().getSubjectName();
        synchronized (subjectGrades) {
            subjectGrades.computeIfAbsent(subjectName, k -> Collections.synchronizedList(new ArrayList<>())).add(grade);
        }
        
        // O(1) average case - Synchronized HashSet add operation
        String courseKey = grade.getSubject().getSubjectName() + " (" + grade.getSubject().getSubjectType() + ")";
        uniqueCourses.add(courseKey);
    }
    
    /**
     * Remove grade from repository
     * Time Complexity: O(n) - Must search LinkedList + O(log n) for TreeMap removal
     * Thread-safe: Synchronized to ensure atomic removal from all collections
     */
    public synchronized boolean removeGrade(Grade grade) {
        boolean removed = gradeHistory.remove(grade);
        
        if (removed) {
            String subjectName = grade.getSubject().getSubjectName();
            synchronized (subjectGrades) {
                List<Grade> subjectGradeList = subjectGrades.get(subjectName);
                if (subjectGradeList != null) {
                    subjectGradeList.remove(grade);
                    if (subjectGradeList.isEmpty()) {
                        subjectGrades.remove(subjectName);
                    }
                }
            }
        }
        
        return removed;
    }
    
    /**
     * Get all grades for a student
     * Time Complexity: O(n) where n is the number of grades
     */
    public List<Grade> getGradesByStudent(String studentId) {
        List<Grade> studentGrades = new ArrayList<>();
        synchronized (gradeHistory) {
            for (Grade grade : gradeHistory) {
                if (grade.getStudentId().equals(studentId)) {
                    studentGrades.add(grade);
                }
            }
        }
        return studentGrades;
    }
    
    /**
     * Get grades by subject name
     * Time Complexity: O(log n) - TreeMap get operation
     */
    public List<Grade> getGradesBySubject(String subjectName) {
        List<Grade> grades = subjectGrades.get(subjectName);
        return grades != null ? new ArrayList<>(grades) : new ArrayList<>();
    }
    
    /**
     * Get all grades
     * Time Complexity: O(1) - Returns reference to list
     */
    public List<Grade> getAllGrades() {
        return new LinkedList<>(gradeHistory);
    }
    
    /**
     * Get all grades as array (for backward compatibility)
     * Time Complexity: O(n) where n is the number of grades
     */
    public Grade[] getGrades() {
        return gradeHistory.toArray(new Grade[0]);
    }
    
    /**
     * Get total grade count
     * Time Complexity: O(1) - LinkedList size operation
     */
    public int getGradeCount() {
        return gradeHistory.size();
    }
    
    /**
     * Get number of enrolled subjects for a student
     * Time Complexity: O(n) where n is the number of grades
     */
    public int getEnrolledSubjectsCount(String studentId) {
        int subjectCount = 0;
        synchronized (gradeHistory) {
            for (Grade grade : gradeHistory) {
                if (grade.getStudentId().equals(studentId)) {
                    subjectCount++;
                }
            }
        }
        return subjectCount;
    }
    
    /**
     * Get unique courses enrolled across all students
     * Time Complexity: O(1) - Returns reference to set
     */
    public Set<String> getUniqueCourses() {
        return new HashSet<>(uniqueCourses);
    }
    
    /**
     * Get grades organized by subject name
     * Time Complexity: O(1) - Returns reference to map
     */
    public Map<String, List<Grade>> getSubjectGrades() {
        return new TreeMap<>(subjectGrades);
    }
}
