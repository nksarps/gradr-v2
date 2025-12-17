package com.gradr;

import com.gradr.exceptions.FileExportException;

/**
 * ApplicationContext - Simple dependency injection container
 * Adheres to Dependency Inversion Principle
 * 
 * Design Pattern: Service Locator / Dependency Injection Container
 * - Centralized configuration of dependencies
 * - Single place to wire up all services
 * - Makes dependencies explicit and testable
 * 
 * Responsibilities:
 * - Create and configure all services
 * - Manage service lifecycle
 * - Provide access to configured services
 */
public class ApplicationContext {
    // Repositories
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    
    // Services
    private final StudentStatistics studentStatistics;
    private final GradeCalculator gradeCalculator;
    private final GradeAuditService gradeAuditService;
    private final GPARankingService gpaRankingService;
    private final TaskSchedulerService taskSchedulerService;
    private final StatisticsService statisticsService;
    
    // Managers (Facades)
    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    
    // Other services
    private final AuditLogger auditLogger;
    private final CacheManager cacheManager;
    private final PatternSearchService patternSearchService;
    private final SystemPerformanceMonitor performanceMonitor;
    
    // Task scheduler (optional, initialized on first use)
    private TaskScheduler taskScheduler;
    private boolean taskSchedulerInitialized = false;
    
    // Statistics dashboard (optional, initialized on first use)
    private StatisticsDashboard statisticsDashboard;
    
    // Export strategies
    private CSVExportStrategy csvExportStrategy;
    private JSONExportStrategy jsonExportStrategy;
    private BinaryExportStrategy binaryExportStrategy;
    
    /**
     * Initializes the application context with all configured dependencies.
     * Creates and wires up repositories, services, managers, and utilities.
     * Warms the cache and registers shutdown hooks for cleanup.
     */
    public ApplicationContext() throws FileExportException {
        // Create repositories first (no dependencies)
        this.studentRepository = new StudentRepository();
        this.gradeRepository = new GradeRepository();
        
        // Create services (depend on repositories)
        this.gradeCalculator = new GradeCalculator(gradeRepository);
        this.gradeAuditService = new GradeAuditService();
        this.gpaRankingService = new GPARankingService();
        this.taskSchedulerService = new TaskSchedulerService();
        this.statisticsService = new StatisticsService();
        this.studentStatistics = new StudentStatistics(studentRepository);
        
        // Create managers (depend on services)
        this.gradeManager = new GradeManager(
            gradeRepository,
            gradeCalculator,
            gradeAuditService,
            gpaRankingService,
            taskSchedulerService,
            statisticsService
        );
        this.studentManager = new StudentManager(studentRepository, studentStatistics);
        
        // Create other services
        this.auditLogger = new AuditLogger();
        this.cacheManager = new CacheManager();
        this.patternSearchService = new PatternSearchService(studentManager);
        this.performanceMonitor = new SystemPerformanceMonitor(
            studentManager, gradeManager, cacheManager, patternSearchService
        );
        
        // Create export strategies with performance monitoring
        this.csvExportStrategy = new CSVExportStrategy(performanceMonitor);
        this.jsonExportStrategy = new JSONExportStrategy(performanceMonitor);
        this.binaryExportStrategy = new BinaryExportStrategy(performanceMonitor);
        
        // Warm cache on startup
        cacheManager.warmCache(studentManager, gradeManager);
        
        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    /**
     * Returns the student manager facade for student operations.
     */
    public StudentManager getStudentManager() {
        return studentManager;
    }
    
    /**
     * Returns the grade manager facade for grade operations.
     */
    public GradeManager getGradeManager() {
        return gradeManager;
    }
    
    /**
     * Returns the audit logger for tracking system operations.
     */
    public AuditLogger getAuditLogger() {
        return auditLogger;
    }
    
    /**
     * Returns the cache manager for performance optimization.
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    
    /**
     * Returns the pattern search service for regex-based student searches.
     */
    public PatternSearchService getPatternSearchService() {
        return patternSearchService;
    }
    
    /**
     * Returns the performance monitor for system metrics tracking.
     */
    public SystemPerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }
    
    /**
     * Returns the task scheduler for automated task execution.
     * May be null if not yet initialized.
     */
    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }
    
    /**
     * Sets the task scheduler instance for the application.
     * Should be called once when the scheduler is first initialized.
     */
    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
        this.taskSchedulerInitialized = true;
    }
    
    /**
     * Checks whether the task scheduler has been initialized.
     */
    public boolean isTaskSchedulerInitialized() {
        return taskSchedulerInitialized;
    }
    
    /**
     * Returns the statistics dashboard for real-time metrics display.
     * May be null if not yet initialized.
     */
    public StatisticsDashboard getStatisticsDashboard() {
        return statisticsDashboard;
    }
    
    /**
     * Sets the statistics dashboard instance for the application.
     * Should be called once when the dashboard is first initialized.
     */
    public void setStatisticsDashboard(StatisticsDashboard dashboard) {
        this.statisticsDashboard = dashboard;
    }
    
    /**
     * Returns the CSV export strategy for generating CSV reports.
     */
    public CSVExportStrategy getCsvExportStrategy() {
        return csvExportStrategy;
    }
    
    /**
     * Returns the JSON export strategy for generating JSON reports.
     */
    public JSONExportStrategy getJsonExportStrategy() {
        return jsonExportStrategy;
    }
    
    /**
     * Returns the binary export strategy for generating serialized reports.
     */
    public BinaryExportStrategy getBinaryExportStrategy() {
        return binaryExportStrategy;
    }
    
    /**
     * Returns the appropriate export strategy based on format choice.
     * 1 = CSV, 2 = JSON, 3 = Binary.
     */
    public FileExportStrategy getExportStrategy(int formatChoice) {
        switch (formatChoice) {
            case 1: return csvExportStrategy;
            case 2: return jsonExportStrategy;
            case 3: return binaryExportStrategy;
            default: throw new IllegalArgumentException("Invalid format choice: " + formatChoice);
        }
    }
    
    /**
     * Gracefully shuts down all services and releases resources.
     * Called automatically via shutdown hook on JVM exit.
     */
    public void shutdown() {
        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
        cacheManager.shutdown();
        auditLogger.shutdown();
    }
}
