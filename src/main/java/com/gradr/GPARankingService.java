package com.gradr;

import java.util.*;

/**
 * GPARankingService - Responsible for maintaining GPA rankings
 * Adheres to Single Responsibility Principle by focusing only on GPA ranking management
 * Adheres to Dependency Inversion Principle by implementing IGPARankingService interface
 * 
 * Responsibilities:
 * - Maintain sorted GPA rankings
 * - Update student rankings
 * - Provide ranking queries
 * 
 * Thread Safety:
 * - Synchronized TreeMap for sorted GPA rankings
 */
public class GPARankingService implements IGPARankingService {
    // Synchronized TreeMap for sorted GPA rankings (GPA -> List of Students with that GPA)
    // Time Complexity: O(log n) for put, get, remove operations
    // Automatically maintains sorted order by GPA (descending)
    private final Map<Double, List<Student>> gpaRankings = Collections.synchronizedMap(
        new TreeMap<>(Collections.reverseOrder()));
    
    /**
     * Update GPA ranking for a student
     * Time Complexity: O(log n) where n is the number of unique GPA values
     * Thread-safe: Synchronized to ensure atomic update
     */
    public synchronized void updateGPARanking(Student student, double gpa) {
        // Remove student from old GPA entry if exists
        synchronized (gpaRankings) {
            for (List<Student> students : gpaRankings.values()) {
                students.remove(student);
            }
            
            // Add student to new GPA entry
            // O(log n) - Synchronized TreeMap put operation
            gpaRankings.computeIfAbsent(gpa, k -> Collections.synchronizedList(new ArrayList<>())).add(student);
        }
    }
    
    /**
     * Get sorted GPA rankings
     * Time Complexity: O(1) - Returns reference to map
     * @return TreeMap with GPA as key (sorted in descending order) and list of students as value
     */
    public Map<Double, List<Student>> getGPARankings() {
        return new TreeMap<>(gpaRankings);
    }
    
    /**
     * Get students with a specific GPA
     * Time Complexity: O(log n) - TreeMap get operation
     */
    public List<Student> getStudentsWithGPA(double gpa) {
        List<Student> students = gpaRankings.get(gpa);
        return students != null ? new ArrayList<>(students) : new ArrayList<>();
    }
    
    /**
     * Clear all rankings
     * Time Complexity: O(1) - TreeMap clear operation
     */
    public synchronized void clearRankings() {
        gpaRankings.clear();
    }
}
