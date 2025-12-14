package com.gradr;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * GradeManager - Optimized with multiple collection types for efficient operations
 * 
 * Collection Optimizations:
 * - Collections.synchronizedList<Grade>: O(1) for frequent insertions/deletions in grade history (thread-safe)
 * - Collections.synchronizedMap<TreeMap>: O(log n) for organizing grades by subject name (thread-safe)
 * - Collections.synchronizedSet<HashSet>: O(1) average case for tracking unique courses (thread-safe)
 * - Collections.synchronizedMap<TreeMap>: O(log n) for sorted GPA rankings (thread-safe)
 * - Collections.synchronizedCollection<PriorityQueue>: O(log n) for task scheduling (thread-safe)
 * - ConcurrentHashMap<String, Statistics>: O(1) thread-safe statistics cache
 * - ConcurrentLinkedQueue<AuditEntry>: Thread-safe, non-blocking audit logging
 * 
 * Thread Safety:
 * - All modification operations (addGrade, removeGrade, updateGPARanking) are synchronized
 * - Collections wrapped with Collections.synchronized* for thread-safe access
 * - Iteration over collections requires external synchronization
 */
class GradeManager {
    // Synchronized LinkedList for grade history - optimized for frequent insertions/deletions
    // Time Complexity: O(1) for add/remove at ends, O(n) for search
    // Maintains chronological order per student
    // Thread-safe: All operations synchronized
    private final List<Grade> gradeHistory = Collections.synchronizedList(new LinkedList<>());
    
    // Synchronized TreeMap for organizing grades by subject name
    // Time Complexity: O(log n) for put, get, remove operations
    // Automatically sorted by subject name, used for subject-wise reports
    // Thread-safe: All operations synchronized
    private final Map<String, List<Grade>> subjectGrades = Collections.synchronizedMap(new TreeMap<>());
    
    // Synchronized HashSet for tracking unique courses enrolled across all students
    // Time Complexity: O(1) average case for add, contains, remove operations
    // Thread-safe: All operations synchronized
    private final Set<String> uniqueCourses = Collections.synchronizedSet(new HashSet<>());
    
    // Synchronized TreeMap for sorted GPA rankings (GPA -> List of Students with that GPA)
    // Time Complexity: O(log n) for put, get, remove operations
    // Automatically maintains sorted order by GPA (descending)
    // Thread-safe: All operations synchronized
    private final Map<Double, List<Student>> gpaRankings = Collections.synchronizedMap(
        new TreeMap<>(Collections.reverseOrder()));
    
    // Synchronized PriorityQueue for task scheduling based on priority
    // Time Complexity: O(log n) for offer/poll operations, O(1) for peek
    // Tasks ordered by priority and scheduled time
    // Thread-safe: All operations synchronized
    private final PriorityQueue<Task> taskQueue = new PriorityQueue<>();
    
    // ConcurrentHashMap for thread-safe statistics cache
    // Time Complexity: O(1) average case for concurrent access
    // No blocking during read operations, thread-safe statistics caching
    private Map<String, Statistics> statsCache = new ConcurrentHashMap<>();
    
    // ConcurrentLinkedQueue for thread-safe, non-blocking audit logging
    // Allows multiple threads to add entries simultaneously
    // Background thread drains entries to file
    private Queue<AuditEntry> auditLog = new ConcurrentLinkedQueue<>();

    /**
     * Add grade to synchronized LinkedList (maintains insertion order)
     * Time Complexity: O(1) - LinkedList add + O(log n) - TreeMap put
     * Also tracks unique courses in HashSet and organizes by subject in TreeMap
     * Thread-safe: Synchronized to ensure atomic update of all collections
     */
    public synchronized void addGrade(Grade grade){
        // O(1) - Synchronized LinkedList add operation (maintains chronological order)
        gradeHistory.add(grade);
        
        // O(log n) - Synchronized TreeMap put operation (organize by subject name)
        String subjectName = grade.getSubject().getSubjectName();
        synchronized (subjectGrades) {
            subjectGrades.computeIfAbsent(subjectName, k -> Collections.synchronizedList(new ArrayList<>())).add(grade);
        }
        
        // O(1) average case - Synchronized HashSet add operation for unique course tracking
        String courseKey = grade.getSubject().getSubjectName() + " (" + grade.getSubject().getSubjectType() + ")";
        uniqueCourses.add(courseKey);
        
        // Invalidate statistics cache when new grade is added
        statsCache.clear();
        
        // Add audit entry (non-blocking, thread-safe)
        auditLog.offer(new AuditEntry(AuditEntry.AuditAction.GRADE_RECORDED, 
            "SYSTEM", grade.getStudentId(), 
            String.format("Grade %s recorded for %s", grade.getGradeId(), subjectName)));
    }

    /**
     * View grades by student ID
     * Time Complexity: O(n) where n is the number of grades (must iterate through all)
     * Thread-safe: Synchronized iteration to prevent ConcurrentModificationException
     */
    public String viewGradesByStudent(String studentId) {
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        int totalCourses = 0;

        // O(n) - Iterate through all grades in synchronized LinkedList
        synchronized (gradeHistory) {
            for (Grade grade : gradeHistory) {
                if (grade.getStudentId().equals(studentId)) {
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
            }
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


    /**
     * Calculate core subject average for a student
     * Time Complexity: O(n) where n is the number of grades
     * Thread-safe: Synchronized iteration to prevent ConcurrentModificationException
     */
    public double calculateCoreAverage(String studentId) {
        double gradeSum = 0;
        int totalCourses = 0;

        // O(n) - Iterate through all grades (synchronized)
        synchronized (gradeHistory) {
            for (Grade grade : gradeHistory) {
                if (grade.getStudentId().equals(studentId)) {
                    if (grade.getSubject().getSubjectType().equals("Core")) {
                        gradeSum += grade.getGrade();
                        totalCourses++;
                    }
                }
            }
        }

        if (totalCourses == 0) return 0.0;
        return gradeSum / totalCourses;
    }

    /**
     * Calculate elective subject average for a student
     * Time Complexity: O(n) where n is the number of grades
     * Thread-safe: Synchronized iteration to prevent ConcurrentModificationException
     */
    public double calculateElectiveAverage(String studentId) {
        double gradeSum = 0;
        int totalCourses = 0;

        // O(n) - Iterate through all grades (synchronized)
        synchronized (gradeHistory) {
            for (Grade grade : gradeHistory) {
                if (grade.getStudentId().equals(studentId)) {
                    if (grade.getSubject().getSubjectType().equals("Elective")) {
                        gradeSum += grade.getGrade();
                        totalCourses++;
                    }
                }
            }
        }

        if (totalCourses == 0) return 0.0;
        return gradeSum / totalCourses;
    }

    /**
     * Calculate overall average for a student
     * Time Complexity: O(n) where n is the number of grades
     * Thread-safe: Synchronized iteration to prevent ConcurrentModificationException
     */
    public double calculateOverallAverage(String studentId) {
        double gradeSum = 0;
        int totalCourses = 0;

        // O(n) - Iterate through all grades (synchronized)
        synchronized (gradeHistory) {
            for (Grade grade : gradeHistory) {
                if (grade.getStudentId().equals(studentId)) {
                    gradeSum += grade.getGrade();
                    totalCourses++;
                }
            }
        }

        if (totalCourses == 0) return 0.0;
        return gradeSum / totalCourses;
    }

    /**
     * Get total grade count
     * Time Complexity: O(1) - LinkedList size operation
     */
    public int getGradeCount() {
        return gradeHistory.size();
    }

    /**
     * Get all grades as array (for backward compatibility)
     * Time Complexity: O(n) where n is the number of grades
     */
    public Grade[] getGrades() {
        return gradeHistory.toArray(new Grade[0]);
    }
    
    /**
     * Get all grades as list
     * Time Complexity: O(1) - Returns reference to list
     */
    public List<Grade> getGradeHistory() {
        return new LinkedList<>(gradeHistory); // Return copy to prevent external modification
    }

    /**
     * Get number of enrolled subjects for a student
     * Time Complexity: O(n) where n is the number of grades
     * Thread-safe: Synchronized iteration to prevent ConcurrentModificationException
     */
    public int getEnrolledSubjectsCount(String studentId) {
        int subjectCount = 0;

        // O(n) - Iterate through all grades (synchronized)
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
     * @return Set of unique course names
     */
    public Set<String> getUniqueCourses() {
        return new HashSet<>(uniqueCourses); // Return copy to prevent external modification
    }
    
    /**
     * Update GPA rankings in synchronized TreeMap
     * Time Complexity: O(log n) where n is the number of unique GPA values
     * Thread-safe: Synchronized to ensure atomic update
     * @param student The student to add/update in rankings
     * @param gpa The student's GPA
     */
    public synchronized void updateGPARanking(Student student, double gpa) {
        // Remove student from old GPA entry if exists (synchronized iteration)
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
        return new TreeMap<>(gpaRankings); // Return copy to prevent external modification
    }
    
    /**
     * Remove grade from history (for grade corrections/deletions)
     * Time Complexity: O(n) - Must search LinkedList to find grade + O(log n) for TreeMap removal
     * Thread-safe: Synchronized to ensure atomic removal from all collections
     * @param grade The grade to remove
     * @return true if grade was removed, false otherwise
     */
    public synchronized boolean removeGrade(Grade grade) {
        // O(n) - Synchronized LinkedList remove operation (must find element first)
        boolean removed = gradeHistory.remove(grade);
        
        if (removed) {
            // O(log n) - Remove from synchronized subjectGrades TreeMap
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
            
            // Invalidate statistics cache
            statsCache.clear();
            
            // Add audit entry (non-blocking, thread-safe)
            auditLog.offer(new AuditEntry(AuditEntry.AuditAction.GRADE_DELETED, 
                "SYSTEM", grade.getStudentId(), 
                String.format("Grade %s deleted", grade.getGradeId())));
        }
        
        return removed;
    }
    
    /**
     * Add task to priority queue
     * Time Complexity: O(log n) where n is the number of tasks in queue
     * Thread-safe: Synchronized to prevent concurrent modification
     * @param task The task to add
     */
    public synchronized void scheduleTask(Task task) {
        // O(log n) - PriorityQueue offer operation
        taskQueue.offer(task);
    }
    
    /**
     * Get and remove the highest priority task
     * Time Complexity: O(log n) where n is the number of tasks in queue
     * Thread-safe: Synchronized to prevent concurrent modification
     * @return The highest priority task, or null if queue is empty
     */
    public synchronized Task processNextTask() {
        // O(log n) - PriorityQueue poll operation
        return taskQueue.poll();
    }
    
    /**
     * Peek at the highest priority task without removing it
     * Time Complexity: O(1) - PriorityQueue peek operation
     * Thread-safe: Synchronized to prevent concurrent modification
     * @return The highest priority task, or null if queue is empty
     */
    public synchronized Task peekNextTask() {
        // O(1) - PriorityQueue peek operation
        return taskQueue.peek();
    }
    
    /**
     * Get the number of pending tasks
     * Time Complexity: O(1) - Queue size operation
     * Thread-safe: Synchronized to prevent concurrent modification
     * @return Number of tasks in queue
     */
    public synchronized int getPendingTaskCount() {
        return taskQueue.size();
    }
    
    /**
     * Check if there are pending tasks
     * Time Complexity: O(1) - Queue isEmpty operation
     * Thread-safe: Synchronized to prevent concurrent modification
     * @return true if there are pending tasks, false otherwise
     */
    public synchronized boolean hasPendingTasks() {
        return !taskQueue.isEmpty();
    }
    
    /**
     * Get grades organized by subject name
     * Time Complexity: O(1) - Returns reference to map
     * @return TreeMap with subject name as key and list of grades as value
     */
    public Map<String, List<Grade>> getSubjectGrades() {
        return new TreeMap<>(subjectGrades); // Return copy to prevent external modification
    }
    
    /**
     * Get grades for a specific subject
     * Time Complexity: O(log n) - TreeMap get operation
     * @param subjectName The name of the subject
     * @return List of grades for the subject, or empty list if not found
     */
    public List<Grade> getGradesBySubject(String subjectName) {
        List<Grade> grades = subjectGrades.get(subjectName);
        return grades != null ? new ArrayList<>(grades) : new ArrayList<>();
    }
    
    /**
     * Get or compute statistics with caching
     * Time Complexity: O(1) average case if cached, O(n) if needs computation
     * @param cacheKey Key for the statistics cache
     * @param calculator Function to compute statistics if not cached
     * @return Statistics object
     */
    public Statistics getStatistics(String cacheKey, java.util.function.Supplier<Statistics> calculator) {
        // O(1) - ConcurrentHashMap get operation (thread-safe, no blocking)
        Statistics stats = statsCache.get(cacheKey);
        
        if (stats == null || stats.isExpired(5)) { // 5 minute cache timeout
            // Compute statistics
            stats = calculator.get();
            // O(1) - ConcurrentHashMap put operation (thread-safe)
            statsCache.put(cacheKey, stats);
        }
        
        return stats;
    }
    
    /**
     * Clear statistics cache
     * Time Complexity: O(1) - ConcurrentHashMap clear operation
     */
    public void clearStatisticsCache() {
        statsCache.clear();
    }
    
    /**
     * Add audit entry to log (non-blocking)
     * Time Complexity: O(1) - ConcurrentLinkedQueue offer operation
     * @param entry The audit entry to add
     */
    public void addAuditEntry(AuditEntry entry) {
        // O(1) - ConcurrentLinkedQueue offer (non-blocking, thread-safe)
        auditLog.offer(entry);
    }
    
    /**
     * Get all audit entries (for processing by background thread)
     * Time Complexity: O(n) where n is the number of entries
     * @return List of audit entries
     */
    public List<AuditEntry> drainAuditLog() {
        List<AuditEntry> entries = new ArrayList<>();
        AuditEntry entry;
        // Drain queue (non-blocking)
        while ((entry = auditLog.poll()) != null) {
            entries.add(entry);
        }
        return entries;
    }
    
    /**
     * Get current audit log size
     * Time Complexity: O(1) - Queue size operation
     * @return Number of pending audit entries
     */
    public int getAuditLogSize() {
        return auditLog.size();
    }
}