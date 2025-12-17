package com.gradr;

/**
 * MenuHandler - Handles menu-driven application flow
 * Adheres to Single Responsibility Principle - responsible only for menu navigation
 * 
 * Responsibilities:
 * - Display menu and get user choice
 * - Route to appropriate command handlers
 * - Handle main application loop
 * 
 * This is a simplified version demonstrating SOLID architecture.
 * For full implementation, see original Main.java
 */
public class MenuHandler {
    private final ApplicationContext context;
    private final ConsoleUI ui;
    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    private final CacheManager cacheManager;
    
    // Optional services
    private TaskScheduler taskScheduler;
    private StatisticsDashboard dashboard;
    
    public MenuHandler(ApplicationContext context, ConsoleUI ui) {
        this.context = context;
        this.ui = ui;
        this.studentManager = context.getStudentManager();
        this.gradeManager = context.getGradeManager();
        this.cacheManager = context.getCacheManager();
    }
    
    /**
     * Main application loop
     */
    public void run() {
        int choice = 0;
        
        do {
            ui.displayMainMenu(taskScheduler, dashboard);
            choice = ui.getMenuChoice();
            
            if (choice == -1) {
                continue; // Invalid input
            }
            
            try {
                handleMenuChoice(choice);
            } catch (Exception e) {
                ui.displayError("Error: " + e.getMessage());
            }
            
        } while (choice != 19);
        
        System.out.println("Thank you for using Student Grade Management System!");
    }
    
    /**
     * Route menu choice to appropriate handler
     */
    private void handleMenuChoice(int choice) throws Exception {
        switch (choice) {
            case 1:
                handleAddStudent();
                break;
            case 2:
                handleViewStudents();
                break;
            case 3:
                handleRecordGrade();
                break;
            case 4:
                handleViewGradeReport();
                break;
            case 5:
                handleExportReport();
                break;
            case 6:
                handleImportData();
                break;
            case 7:
                handleBulkImport();
                break;
            case 8:
                handleCalculateGPA();
                break;
            case 9:
                handleClassStatistics();
                break;
            case 10:
                handleStatisticsDashboard();
                break;
            case 11:
                handleBatchReports();
                break;
            case 12:
                handleAdvancedSearch();
                break;
            case 13:
                handlePatternSearch();
                break;
            case 14:
                handleQueryGradeHistory();
                break;
            case 15:
                handleScheduleTasks();
                break;
            case 16:
                handleSystemPerformance();
                break;
            case 17:
                handleCacheManagement();
                break;
            case 18:
                handleAuditTrail();
                break;
            case 19:
                // Exit
                break;
            default:
                ui.displayError("X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-19).");
                break;
        }
    }
    
    // ========== Command Handlers ==========
    // NOTE: These are simplified stubs. Full implementation in original Main.java
    // For production, each handler would be extracted to separate Command classes
    
    private void handleAddStudent() throws Exception {
        System.out.println("ADD STUDENT");
        System.out.println("_______________________________________________");
        System.out.println();
        
        System.out.print("Enter student name: ");
        String name = ui.getScanner().nextLine();
        ValidationUtils.validateName(name);
        
        System.out.print("Enter student age: ");
        int age = Integer.parseInt(ui.getScanner().nextLine());
        
        System.out.print("Enter student email: ");
        String email = ui.getScanner().nextLine();
        ValidationUtils.validateEmail(email);
        
        System.out.print("Enter student phone: ");
        String phone = ui.getScanner().nextLine();
        ValidationUtils.validatePhone(phone);
        
        System.out.println("\nStudent type:");
        System.out.println("1. Regular Student");
        System.out.println("2. Honors Student");
        System.out.print("Select type (1-2): ");
        int type = ui.getScanner().nextInt();
        ui.getScanner().nextLine();
        
        // Use factory for student creation (OCP)
        Student student = StudentFactory.createStudent(type, name, age, email, phone);
        studentManager.addStudent(student);
        student.setGradeManager(gradeManager);
        
        // Cache the new student
        cacheManager.put("student:" + student.getStudentId(), student, CacheManager.CacheType.STUDENT);
        
        ui.displaySuccess("Student added successfully! ID: " + student.getStudentId());
    }
    
    private void handleViewStudents() {
        System.out.println("STUDENT LISTING");
        System.out.println("----------------------------------------------------------------------------------------------------");
        
        if (studentManager.getStudentCount() == 0) {
            System.out.println("No students found\n");
            return;
        }
        
        System.out.println("STU ID   | NAME                    | TYPE               | AVG GRADE         | STATUS                ");
        System.out.println("----------------------------------------------------------------------------------------------------");
        
        for (Student student : studentManager.getStudentsList()) {
            student.displayStudentDetails();
            System.out.println("----------------------------------------------------------------------------------------------------");
        }
        
        System.out.printf("\nTotal Students: %d\n", studentManager.getStudentCount());
        System.out.printf("Average Class Grade: %.2f%%\n\n", studentManager.calculateClassAverage());
    }
    
    private void handleRecordGrade() throws Exception {
        System.out.println("RECORD GRADE - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleViewGradeReport() throws Exception {
        System.out.println("VIEW GRADE REPORT - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleExportReport() throws Exception {
        System.out.println("EXPORT REPORT - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleImportData() throws Exception {
        System.out.println("IMPORT DATA - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleBulkImport() throws Exception {
        System.out.println("BULK IMPORT - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleCalculateGPA() throws Exception {
        System.out.println("CALCULATE GPA - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleClassStatistics() {
        System.out.println("CLASS STATISTICS - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleStatisticsDashboard() {
        System.out.println("STATISTICS DASHBOARD - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleBatchReports() {
        System.out.println("BATCH REPORTS - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleAdvancedSearch() {
        System.out.println("ADVANCED SEARCH - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handlePatternSearch() {
        System.out.println("PATTERN SEARCH - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleQueryGradeHistory() {
        System.out.println("QUERY GRADE HISTORY - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleScheduleTasks() {
        System.out.println("SCHEDULE TASKS - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleSystemPerformance() {
        System.out.println("SYSTEM PERFORMANCE - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleCacheManagement() {
        System.out.println("CACHE MANAGEMENT - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
    
    private void handleAuditTrail() {
        System.out.println("AUDIT TRAIL - See original Main.java for full implementation");
        System.out.println("This is a simplified demonstration of SOLID architecture.\n");
    }
}
