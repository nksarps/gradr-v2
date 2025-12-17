package com.gradr;

import com.gradr.exceptions.InvalidMenuChoiceException;
import java.util.List;

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

        System.out.println();
        System.out.print("Select type (1-2): ");
        int type = ui.getScanner().nextInt();
        ui.getScanner().nextLine();
        
        // Use factory for student creation (OCP)
        Student student = StudentFactory.createStudent(type, name, age, email, phone);
        studentManager.addStudent(student);
        student.setGradeManager(gradeManager);
        
        // Cache the new student
        cacheManager.put("student:" + student.getStudentId(), student, CacheManager.CacheType.STUDENT);
        
        // Display complete student details
        System.out.println();
        System.out.println("Student added successfully!");
        System.out.println("All inputs validated with regex patterns");
        System.out.println();
        System.out.printf("Student ID: %s\n", student.getStudentId());
        System.out.printf("Name: %s\n", student.getName());
        System.out.printf("Type: %s\n", student.getStudentType());
        System.out.printf("Age: %d\n", student.getAge());
        System.out.printf("Email: %s\n", student.getEmail());
        System.out.printf("Passing Grade: %d%%\n", (int) student.getPassingGrade());
        
        if (student.getStudentType().equals("Honors")) {
            HonorsStudent honorsStudent = (HonorsStudent) student;
            String isEligible = honorsStudent.checkHonorsEligibility();
            System.out.printf("Honors Eligible: %s\n", isEligible);
        }
        System.out.printf("Status: %s\n", student.getStatus());
        System.out.println();
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
        System.out.println("RECORD GRADE");
        System.out.println("_______________________________________________");
        System.out.println();

        System.out.print("Enter Student ID: ");
        String studentId = ui.getScanner().nextLine();
        System.out.println();

        // Find student (try cache first)
        Student student = findStudentWithCache(studentId);
        
        // Display student details
        System.out.println("Student Details:");
        System.out.printf("Name: %s\n", student.getName());
        System.out.printf("Type: %s Student\n", student.getStudentType());
        System.out.printf("Current Average: %.1f%%\n", student.calculateAverageGrade());
        System.out.println();

        // Get subject type using factory (OCP)
        System.out.println("Subject type:");
        System.out.println("1. Core Subject (Mathematics, English, Science)");
        System.out.println("2. Elective Subject (Music, Art, Physical Education)\n");

        System.out.print("Select type (1-2): ");
        int subjectTypeChoice = ui.getScanner().nextInt();
        ui.getScanner().nextLine();
        
        if (subjectTypeChoice < 1 || subjectTypeChoice > 2) {
            throw new InvalidMenuChoiceException(
                "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-2).\n   You entered: " + subjectTypeChoice
            );
        }

        // Use SubjectFactory to create subject (OCP)
        Subject subject = SubjectFactory.createSubject(subjectTypeChoice);
        String subjectType = subject.getSubjectType();
        
        System.out.println();
        System.out.printf("Available %s Subjects\n", subjectType);
        
        if (subjectType.equals("Core")) {
            System.out.println("1. Mathematics");
            System.out.println("2. English");
            System.out.println("3. Science");
        } else {
            System.out.println("1. Music");
            System.out.println("2. Art");
            System.out.println("3. Physical Education");
        }
        System.out.println();

        System.out.print("Select subject (1-3): ");
        int subjectChoice = ui.getScanner().nextInt();
        ui.getScanner().nextLine();

        if (subjectChoice < 1 || subjectChoice > 3) {
            throw new InvalidMenuChoiceException(
                "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-3).\n   You entered: " + subjectChoice
            );
        }

        // Use SubjectFactory to set subject name (OCP)
        SubjectFactory.setSubjectName(subject, subjectTypeChoice, subjectChoice);
        
        System.out.println();
        System.out.print("Enter grade (0-100): ");
        String gradeInputStr = ui.getScanner().nextLine();
        
        // Validate grade using ValidationUtils
        ValidationUtils.validateGrade(gradeInputStr);
        int gradeInput = Integer.parseInt(gradeInputStr);
        ValidationUtils.validateGrade(gradeInput);

        // Create grade
        Grade grade = new Grade(studentId, subject, gradeInput);

        // Validate and record grade
        if (grade.recordGrade(gradeInput)) {
            grade.setGradeId();

            // Display confirmation
            System.out.println("GRADE CONFIRMATION");
            System.out.println("_______________________________________________________");
            System.out.printf("Grade ID: %s\n", grade.getGradeId());
            System.out.printf("Student: %s - %s\n", studentId, student.getName());
            System.out.printf("Subject: %s (%s)\n", subject.getSubjectName(), subject.getSubjectType());
            System.out.printf("Grade: %.1f%%\n", (double) gradeInput);
            System.out.printf("Date: %s\n", grade.getDate());
            System.out.println("______________________________________________________\n");

            System.out.print("Confirm grade? (Y/N): ");
            char confirmGrade = ui.getScanner().next().charAt(0);
            ui.getScanner().nextLine();

            if (confirmGrade == 'Y' || confirmGrade == 'y') {
                gradeManager.addGrade(grade);
                
                // Invalidate caches (grades changed)
                cacheManager.invalidateByType(CacheManager.CacheType.GRADE_REPORT);
                cacheManager.invalidateByType(CacheManager.CacheType.STATISTICS);

                // Update GPA rankings
                GPACalculator gpaCalc = new GPACalculator(gradeManager);
                double gpa = gpaCalc.calculateCumulativeGPA(studentId);
                gradeManager.updateGPARanking(student, gpa);
                
                // Schedule task for grade processing
                Task gradeTask = new Task(Task.TaskType.GRADE_PROCESSING, 
                        "Process grade for " + student.getName(), studentId);
                gradeManager.scheduleTask(gradeTask);
                
                System.out.println("Grade added successfully.\n");
            } else if (confirmGrade == 'N' || confirmGrade == 'n') {
                Grade.gradeCounter--;
                System.out.println("Grade record cancelled\n");
            } else {
                Grade.gradeCounter--;
                throw new InvalidMenuChoiceException(
                    "X ERROR: InvalidMenuChoiceException\n   Please enter Y or N.\n   You entered: " + confirmGrade
                );
            }
        }
    }
    
    /**
     * Helper method to find student with caching (DRY principle)
     */
    private Student findStudentWithCache(String studentId) throws Exception {
        String cacheKey = "student:" + studentId;
        Student cachedStudent = (Student) cacheManager.get(cacheKey);
        
        if (cachedStudent != null) {
            return cachedStudent; // Cache hit
        } else {
            // Cache miss - fetch and cache
            Student student = studentManager.findStudent(studentId);
            cacheManager.put(cacheKey, student, CacheManager.CacheType.STUDENT);
            return student;
        }
    }
    
    private void handleViewGradeReport() throws Exception {
        System.out.println("VIEW GRADE REPORT");
        System.out.println("_______________________________________________");
        System.out.println();

        System.out.print("Enter Student ID: ");
        String studentId = ui.getScanner().nextLine();
        System.out.println();

        // Find student (try cache first) - DIP: depends on abstraction
        Student student = findStudentWithCache(studentId);
        
        // Display student information
        System.out.println("STUDENT INFORMATION");
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.printf("Student ID: %s\n", student.getStudentId());
        System.out.printf("Name: %s\n", student.getName());
        System.out.printf("Type: %s Student\n", student.getStudentType());
        System.out.printf("Age: %d\n", student.getAge());
        System.out.printf("Email: %s\n", student.getEmail());
        System.out.printf("Phone: %s\n", student.getPhone());
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println();

        // Check cache for grade report (SRP: cache handling separated)
        String cacheKey = "grade_report:" + studentId;
        String cachedReport = (String) cacheManager.get(cacheKey);
        
        String gradeReport;
        if (cachedReport != null) {
            // Cache hit
            gradeReport = cachedReport;
        } else {
            // Cache miss - delegate to GradeManager (DIP: depends on abstraction)
            gradeReport = gradeManager.viewGradesByStudent(studentId);
            
            // Cache the result
            cacheManager.put(cacheKey, gradeReport, CacheManager.CacheType.GRADE_REPORT);
        }
        
        // Display grade report
        System.out.print(gradeReport);
        System.out.println();
    }
    
    private void handleExportReport() throws Exception {
        System.out.println("EXPORT REPORT");
        System.out.println("_______________________________________________");
        System.out.println();

        System.out.print("Enter Student ID: ");
        String studentId = ui.getScanner().nextLine();
        System.out.println();

        // Find student (try cache first) - DIP: depends on abstraction
        Student student = findStudentWithCache(studentId);
        
        // Display student information
        System.out.println("Student Information:");
        System.out.printf("ID: %s\n", student.getStudentId());
        System.out.printf("Name: %s\n", student.getName());
        System.out.printf("Type: %s Student\n", student.getStudentType());
        System.out.println();

        // Select export format (OCP: Strategy pattern allows extensibility)
        System.out.println("Select Export Format:");
        System.out.println("1. CSV (Comma-Separated Values)");
        System.out.println("2. JSON (JavaScript Object Notation)");
        System.out.println("3. Binary (.dat file)");
        System.out.println();

        System.out.print("Select format (1-3): ");
        int formatChoice = ui.getScanner().nextInt();
        ui.getScanner().nextLine();

        if (formatChoice < 1 || formatChoice > 3) {
            throw new InvalidMenuChoiceException(
                "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-3).\n   You entered: " + formatChoice
            );
        }

        // Create export strategy based on choice (OCP: Strategy Pattern)
        FileExportStrategy exportStrategy;
        switch (formatChoice) {
            case 1:
                exportStrategy = new CSVExportStrategy();
                break;
            case 2:
                exportStrategy = new JSONExportStrategy();
                break;
            case 3:
                exportStrategy = new BinaryExportStrategy();
                break;
            default:
                throw new InvalidMenuChoiceException("Invalid format choice");
        }

        // Build StudentReport from student data (SRP: data preparation)
        StudentReport report = buildStudentReport(student);
        
        // Generate filename
        String fileName = student.getStudentId() + "_" + student.getName().replace(" ", "_") + "_report";
        
        System.out.println();
        System.out.print("Confirm export? (Y/N): ");
        char confirm = ui.getScanner().next().charAt(0);
        ui.getScanner().nextLine();
        
        if (confirm == 'Y' || confirm == 'y') {
            // Delegate export to strategy (DIP: depends on abstraction)
            java.nio.file.Path exportedFile = exportStrategy.export(report, fileName);
            
            System.out.println();
            System.out.println("Export successful!");
            System.out.printf("Format: %s\n", exportStrategy.getFormatName());
            System.out.printf("File: %s\n", exportedFile.toString());
            System.out.printf("Total Grades Exported: %d\n", report.getGrades().size());
            System.out.println();
        } else if (confirm == 'N' || confirm == 'n') {
            System.out.println("Export cancelled\n");
        } else {
            throw new InvalidMenuChoiceException(
                "X ERROR: InvalidMenuChoiceException\n   Please enter Y or N.\n   You entered: " + confirm
            );
        }
    }
    
    /**
     * Build StudentReport from Student data (SRP: separate report building logic)
     */
    private StudentReport buildStudentReport(Student student) {
        // Create report with student details
        StudentReport report = new StudentReport(
            student.getStudentId(),
            student.getName(),
            student.getStudentType(),
            student.calculateAverageGrade(),
            "Detailed Grade Report"
        );
        
        // Get all grades for the student (delegates to GradeRepository via GradeManager)
        List<Grade> studentGrades = gradeManager.getGradeHistory();
        
        // Filter and add student's grades to report
        for (Grade grade : studentGrades) {
            if (grade.getStudentId().equals(student.getStudentId())) {
                GradeData gradeData = new GradeData(
                    grade.getGradeId(),
                    grade.getDate(),
                    grade.getSubject().getSubjectName(),
                    grade.getSubject().getSubjectType(),
                    grade.getGrade()
                );
                report.addGrade(gradeData);
            }
        }
        
        return report;
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
