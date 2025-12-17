package com.gradr;

import java.util.*;
import java.util.function.Supplier;

/**
 * GradeManager - Facade for grade management operations
 * Refactored to adhere to Single Responsibility Principle
 * Delegates to specialized services for different responsibilities
 * 
 * Design Pattern: Facade Pattern
 * - Provides a unified interface to a set of interfaces in a subsystem
 * - Delegates to GradeRepository, GradeCalculator, GradeAuditService, etc.
 * 
 * Implements IGradeCalculator for backward compatibility with Student class
 * 
 * Thread Safety:
 * - Thread-safety is handled by individual services
 */
class GradeManager implements IGradeCalculator {
    // Specialized services (adhering to SRP)
    private final GradeRepository gradeRepository;
    private final GradeCalculator gradeCalculator;
    private final GradeAuditService auditService;
    private final GPARankingService rankingService;
    private final TaskSchedulerService taskScheduler;
    private final StatisticsService statisticsService;
    
    /**
     * Constructor - initializes all services
     */
    public GradeManager() {
        this.gradeRepository = new GradeRepository();
        this.gradeCalculator = new GradeCalculator(gradeRepository);
        this.auditService = new GradeAuditService();
        this.rankingService = new GPARankingService();
        this.taskScheduler = new TaskSchedulerService();
        this.statisticsService = new StatisticsService();
    }
    
    /**
     * Constructor with dependency injection (for testing and flexibility)
     */
    public GradeManager(GradeRepository gradeRepository, 
                       GradeCalculator gradeCalculator,
                       GradeAuditService auditService,
                       GPARankingService rankingService,
                       TaskSchedulerService taskScheduler,
                       StatisticsService statisticsService) {
        this.gradeRepository = gradeRepository;
        this.gradeCalculator = gradeCalculator;
        this.auditService = auditService;
        this.rankingService = rankingService;
        this.taskScheduler = taskScheduler;
        this.statisticsService = statisticsService;
    }

    /**
     * Add grade - delegates to repository and logs to audit service
     * Time Complexity: O(1) + O(log n)
     */
    public void addGrade(Grade grade){
        gradeRepository.addGrade(grade);
        auditService.logGradeAdded(grade);
        statisticsService.clearStatisticsCache();
    }

    /**
     * View grades by student ID - delegates to calculator
     */
    public String viewGradesByStudent(String studentId) {
        return gradeCalculator.viewGradesByStudent(studentId);
    }

    /**
     * Calculate core subject average - delegates to calculator
     */
    public double calculateCoreAverage(String studentId) {
        return gradeCalculator.calculateCoreAverage(studentId);
    }

    /**
     * Calculate elective subject average - delegates to calculator
     */
    public double calculateElectiveAverage(String studentId) {
        return gradeCalculator.calculateElectiveAverage(studentId);
    }

    /**
     * Calculate overall average - delegates to calculator
     */
    public double calculateOverallAverage(String studentId) {
        return gradeCalculator.calculateOverallAverage(studentId);
    }

    /**
     * Get total grade count - delegates to repository
     */
    public int getGradeCount() {
        return gradeRepository.getGradeCount();
    }

    /**
     * Get all grades as array - delegates to repository
     */
    public Grade[] getGrades() {
        return gradeRepository.getGrades();
    }
    
    /**
     * Get all grades as list - delegates to repository
     */
    public List<Grade> getGradeHistory() {
        return gradeRepository.getAllGrades();
    }

    /**
     * Get number of enrolled subjects - delegates to repository
     */
    public int getEnrolledSubjectsCount(String studentId) {
        return gradeRepository.getEnrolledSubjectsCount(studentId);
    }
    
    /**
     * Get unique courses - delegates to repository
     */
    public Set<String> getUniqueCourses() {
        return gradeRepository.getUniqueCourses();
    }
    
    /**
     * Update GPA ranking - delegates to ranking service
     */
    public void updateGPARanking(Student student, double gpa) {
        rankingService.updateGPARanking(student, gpa);
    }
    
    /**
     * Get sorted GPA rankings - delegates to ranking service
     */
    public Map<Double, List<Student>> getGPARankings() {
        return rankingService.getGPARankings();
    }
    
    /**
     * Remove grade - delegates to repository and logs audit
     */
    public boolean removeGrade(Grade grade) {
        boolean removed = gradeRepository.removeGrade(grade);
        if (removed) {
            auditService.logGradeDeleted(grade);
            statisticsService.clearStatisticsCache();
        }
        return removed;
    }
    
    /**
     * Schedule task - delegates to task scheduler
     */
    public void scheduleTask(Task task) {
        taskScheduler.scheduleTask(task);
    }
    
    /**
     * Process next task - delegates to task scheduler
     */
    public Task processNextTask() {
        return taskScheduler.processNextTask();
    }
    
    /**
     * Peek next task - delegates to task scheduler
     */
    public Task peekNextTask() {
        return taskScheduler.peekNextTask();
    }
    
    /**
     * Get pending task count - delegates to task scheduler
     */
    public int getPendingTaskCount() {
        return taskScheduler.getPendingTaskCount();
    }
    
    /**
     * Check if has pending tasks - delegates to task scheduler
     */
    public boolean hasPendingTasks() {
        return taskScheduler.hasPendingTasks();
    }
    
    /**
     * Get subject grades - delegates to repository
     */
    public Map<String, List<Grade>> getSubjectGrades() {
        return gradeRepository.getSubjectGrades();
    }
    
    /**
     * Get grades by subject - delegates to repository
     */
    public List<Grade> getGradesBySubject(String subjectName) {
        return gradeRepository.getGradesBySubject(subjectName);
    }
    
    /**
     * Get statistics with caching - delegates to statistics service
     */
    public Statistics getStatistics(String cacheKey, Supplier<Statistics> calculator) {
        return statisticsService.getStatistics(cacheKey, calculator);
    }
    
    /**
     * Clear statistics cache - delegates to statistics service
     */
    public void clearStatisticsCache() {
        statisticsService.clearStatisticsCache();
    }
    
    /**
     * Add audit entry - delegates to audit service
     */
    public void addAuditEntry(AuditEntry entry) {
        auditService.addAuditEntry(entry);
    }
    
    /**
     * Drain audit log - delegates to audit service
     */
    public List<AuditEntry> drainAuditLog() {
        return auditService.drainAuditLog();
    }
    
    /**
     * Get audit log size - delegates to audit service
     */
    public int getAuditLogSize() {
        return auditService.getAuditLogSize();
    }
}