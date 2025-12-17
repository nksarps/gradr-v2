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
     * Constructor - wires up all dependencies
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
     * Get student manager
     */
    public StudentManager getStudentManager() {
        return studentManager;
    }
    
    /**
     * Get grade manager
     */
    public GradeManager getGradeManager() {
        return gradeManager;
    }
    
    /**
     * Get audit logger
     */
    public AuditLogger getAuditLogger() {
        return auditLogger;
    }
    
    /**
     * Get cache manager
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    
    /**
     * Get pattern search service
     */
    public PatternSearchService getPatternSearchService() {
        return patternSearchService;
    }
    
    /**
     * Get performance monitor
     */
    public SystemPerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }
    
    /**
     * Get or initialize task scheduler
     */
    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }
    
    /**
     * Set task scheduler (initialized on first use)
     */
    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
        this.taskSchedulerInitialized = true;
    }
    
    /**
     * Check if task scheduler is initialized
     */
    public boolean isTaskSchedulerInitialized() {
        return taskSchedulerInitialized;
    }
    
    /**
     * Get or initialize statistics dashboard
     */
    public StatisticsDashboard getStatisticsDashboard() {
        return statisticsDashboard;
    }
    
    /**
     * Set statistics dashboard (initialized on first use)
     */
    public void setStatisticsDashboard(StatisticsDashboard dashboard) {
        this.statisticsDashboard = dashboard;
    }
    
    /**
     * Get CSV export strategy
     */
    public CSVExportStrategy getCsvExportStrategy() {
        return csvExportStrategy;
    }
    
    /**
     * Get JSON export strategy
     */
    public JSONExportStrategy getJsonExportStrategy() {
        return jsonExportStrategy;
    }
    
    /**
     * Get binary export strategy
     */
    public BinaryExportStrategy getBinaryExportStrategy() {
        return binaryExportStrategy;
    }
    
    /**
     * Get export strategy by format choice
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
     * Shutdown - cleanup resources
     */
    public void shutdown() {
        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
        cacheManager.shutdown();
        auditLogger.shutdown();
    }
}
