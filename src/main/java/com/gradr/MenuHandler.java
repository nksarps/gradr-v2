package com.gradr;

import com.gradr.exceptions.InvalidMenuChoiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            
        } while (choice != 20);
        
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
                handleBulkStudentImport();
                break;
            case 3:
                handleViewStudents();
                break;
            case 4:
                handleRecordGrade();
                break;
            case 5:
                handleViewGradeReport();
                break;
            case 6:
                handleExportReport();
                break;
            case 7:
                handleImportData();
                break;
            case 8:
                handleBulkImport();
                break;
            case 9:
                handleCalculateGPA();
                break;
            case 10:
                handleClassStatistics();
                break;
            case 11:
                handleStatisticsDashboard();
                break;
            case 12:
                handleBatchReports();
                break;
            case 13:
                handleAdvancedSearch();
                break;
            case 14:
                handlePatternSearch();
                break;
            case 15:
                handleQueryGradeHistory();
                break;
            case 16:
                handleScheduleTasks();
                break;
            case 17:
                handleSystemPerformance();
                break;
            case 18:
                handleCacheManagement();
                break;
            case 19:
                handleAuditTrail();
                break;
            case 20:
                // Exit
                break;
            default:
                ui.displayError("X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-20).");
                break;
        }
    }
    
    // ========== Command Handlers ==========
    
    private void handleAddStudent() throws Exception {
        System.out.println("ADD STUDENT");
        System.out.println("_______________________________________________");
        System.out.println();
        
        long startTime = System.currentTimeMillis();
        
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
        
        // Audit log
        long executionTime = System.currentTimeMillis() - startTime;
        context.getAuditLogger().log("STUDENT_ADD", 
            "Added student: " + student.getName(), 
            executionTime, true, null, 
            "Student ID: " + student.getStudentId() + ", Type: " + student.getStudentType());
        
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
    
    private void handleBulkStudentImport() throws Exception {
        System.out.println("ADD BULK STUDENTS FROM CSV");
        System.out.println("_______________________________________________");
        System.out.println();
        System.out.println("CSV Format: StudentName, Age, studentEmail, phone, studentType");
        System.out.println("StudentType: 1 = Regular, 2 = Honors");
        System.out.println();
        
        long startTime = System.currentTimeMillis();
        
        System.out.print("Enter CSV file name (without extension): ");
        String fileName = ui.getScanner().nextLine();
        
        // Construct full file path with imports directory and .csv extension
        String filePath = "imports/" + fileName + ".csv";
        
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filePath));
            String line;
            boolean isFirstLine = true;
            int successCount = 0;
            int errorCount = 0;
            java.util.List<String> errors = new java.util.ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Split by comma
                String[] data = line.split(",");
                
                // Validate data has correct number of fields
                if (data.length != 5) {
                    errors.add("Invalid data format: " + line);
                    errorCount++;
                    continue;
                }
                
                try {
                    // Trim whitespace from each field
                    String name = data[0].trim();
                    int age = Integer.parseInt(data[1].trim());
                    String email = data[2].trim();
                    String phone = data[3].trim();
                    int studentType = Integer.parseInt(data[4].trim());
                    
                    // Validate inputs
                    ValidationUtils.validateName(name);
                    ValidationUtils.validateEmail(email);
                    ValidationUtils.validatePhone(phone);
                    
                    // Validate student type
                    if (studentType != 1 && studentType != 2) {
                        throw new IllegalArgumentException("Student type must be 1 (Regular) or 2 (Honors)");
                    }
                    
                    // Create student using factory
                    Student student = StudentFactory.createStudent(studentType, name, age, email, phone);
                    studentManager.addStudent(student);
                    student.setGradeManager(gradeManager);
                    
                    // Cache the new student
                    cacheManager.put("student:" + student.getStudentId(), student, CacheManager.CacheType.STUDENT);
                    
                    successCount++;
                    
                } catch (Exception e) {
                    errors.add("Error processing line: " + line + " - " + e.getMessage());
                    errorCount++;
                }
            }
            
            reader.close();
            
            // Display results
            System.out.println();
            System.out.println("IMPORT RESULTS");
            System.out.println("_______________________________________________");
            System.out.printf("Successfully imported: %d students\n", successCount);
            System.out.printf("Errors: %d\n", errorCount);
            
            if (!errors.isEmpty()) {
                System.out.println("\nError Details:");
                for (String error : errors) {
                    System.out.println("  - " + error);
                }
            }
            
            // Audit log
            long executionTime = System.currentTimeMillis() - startTime;
            context.getAuditLogger().log("BULK_STUDENT_IMPORT", 
                "Imported " + successCount + " students from CSV", 
                executionTime, true, null, 
                "File: " + filePath + ", Errors: " + errorCount);
            
            System.out.println();
            
        } catch (java.io.FileNotFoundException e) {
            ui.displayError("X ERROR: FileNotFoundException\n   File not found: " + filePath);
            
            // Audit log for failure
            long executionTime = System.currentTimeMillis() - startTime;
            context.getAuditLogger().log("BULK_STUDENT_IMPORT", 
                "Failed to import students from CSV", 
                executionTime, false, "FileNotFoundException: " + filePath, null);
                
        } catch (java.io.IOException e) {
            ui.displayError("X ERROR: IOException\n   Error reading file: " + e.getMessage());
            
            // Audit log for failure
            long executionTime = System.currentTimeMillis() - startTime;
            context.getAuditLogger().log("BULK_STUDENT_IMPORT", 
                "Failed to import students from CSV", 
                executionTime, false, "IOException: " + e.getMessage(), null);
        }
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
        
        // Display minimum 5 students (3 Regular, 2 Honors) as per US-1
        int regularCount = 0;
        int honorsCount = 0;
        int displayedCount = 0;
        
        for (Student student : studentManager.getStudentsList()) {
            boolean shouldDisplay = false;
            
            if (student.getStudentType().equals("Regular") && regularCount < 3) {
                shouldDisplay = true;
                regularCount++;
            } else if (student.getStudentType().equals("Honors") && honorsCount < 2) {
                shouldDisplay = true;
                honorsCount++;
            }
            
            if (shouldDisplay) {
                student.displayStudentDetails();
                System.out.println("----------------------------------------------------------------------------------------------------");
                displayedCount++;
            }
            
            // Stop after displaying minimum 5 students (3 Regular, 2 Honors)
            if (displayedCount >= 5 && regularCount >= 3 && honorsCount >= 2) {
                break;
            }
        }
        
        System.out.printf("\nTotal Students: %d\n", studentManager.getStudentCount());
        System.out.printf("Average Class Grade: %.2f%%\n\n", studentManager.calculateClassAverage());
    }
    
    private void handleRecordGrade() throws Exception {
        System.out.println("RECORD GRADE");
        System.out.println("_______________________________________________");
        System.out.println();

        long startTime = System.currentTimeMillis();

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
                
                // Audit log
                long executionTime = System.currentTimeMillis() - startTime;
                context.getAuditLogger().log("GRADE_RECORD", 
                    "Recorded grade for " + student.getName(), 
                    executionTime, true, null, 
                    "Student: " + studentId + ", Subject: " + subject.getSubjectName() + ", Grade: " + gradeInput);
                
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

        long startTime = System.currentTimeMillis();

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
        // Pass performance monitor for I/O tracking
        SystemPerformanceMonitor perfMonitor = context.getPerformanceMonitor();
        FileExportStrategy exportStrategy;
        switch (formatChoice) {
            case 1:
                exportStrategy = perfMonitor != null ? 
                    new CSVExportStrategy(perfMonitor) : new CSVExportStrategy();
                break;
            case 2:
                exportStrategy = perfMonitor != null ? 
                    new JSONExportStrategy(perfMonitor) : new JSONExportStrategy();
                break;
            case 3:
                exportStrategy = perfMonitor != null ? 
                    new BinaryExportStrategy(perfMonitor) : new BinaryExportStrategy();
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
            
            // Audit log
            long executionTime = System.currentTimeMillis() - startTime;
            context.getAuditLogger().log("REPORT_EXPORT", 
                "Exported report for " + student.getName(), 
                executionTime, true, null, 
                "Student: " + studentId + ", Format: " + exportStrategy.getFormatName() + ", Grades: " + report.getGrades().size());
            
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
        System.out.println("IMPORT GRADE DATA (Multi-Format)");
        System.out.println("_______________________________________________");
        System.out.println();

        System.out.println("Supported formats:");
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

        System.out.println();
        System.out.println("Place your file in the appropriate directory:");
        System.out.println("  CSV:    ./data/csv/");
        System.out.println("  JSON:   ./data/json/");
        System.out.println("  Binary: ./data/binary/");
        System.out.println();

        System.out.print("Enter filename (without extension): ");
        String fileName = ui.getScanner().nextLine();
        System.out.println();

        // Use MultiFormatFileHandler for import (SRP: file handling separated)
        MultiFormatFileHandler fileHandler = new MultiFormatFileHandler();
        StudentReport importedReport = null;
        java.nio.file.Path filePath = null;

        // Determine file path and import based on format (OCP: extensible for new formats)
        switch (formatChoice) {
            case 1: // CSV
                filePath = java.nio.file.Paths.get("./data/csv/" + fileName + ".csv");
                if (!java.nio.file.Files.exists(filePath)) {
                    System.out.println("X ERROR: File not found: " + filePath);
                    System.out.println();
                    return;
                }
                System.out.println("Importing from CSV format...");
                importedReport = importFromCSV(fileHandler, filePath);
                break;

            case 2: // JSON
                filePath = java.nio.file.Paths.get("./data/json/" + fileName + ".json");
                if (!java.nio.file.Files.exists(filePath)) {
                    System.out.println("X ERROR: File not found: " + filePath);
                    System.out.println();
                    return;
                }
                System.out.println("Importing from JSON format...");
                importedReport = fileHandler.importFromJSON(filePath);
                break;

            case 3: // Binary
                filePath = java.nio.file.Paths.get("./data/binary/" + fileName + ".dat");
                if (!java.nio.file.Files.exists(filePath)) {
                    System.out.println("X ERROR: File not found: " + filePath);
                    System.out.println();
                    return;
                }
                System.out.println("Importing from Binary format...");
                importedReport = fileHandler.importFromBinary(filePath);
                break;
        }

        if (importedReport == null) {
            System.out.println("X ERROR: Failed to import data\n");
            return;
        }

        // Display import summary
        System.out.println();
        System.out.println("IMPORT SUMMARY");
        System.out.println("_______________________________________________");
        System.out.printf("Student ID: %s\n", importedReport.getStudentId());
        System.out.printf("Student Name: %s\n", importedReport.getStudentName());
        System.out.printf("Student Type: %s\n", importedReport.getStudentType());
        System.out.printf("Grades to Import: %d\n", importedReport.getGrades().size());
        System.out.printf("Average Grade: %.2f%%\n", importedReport.getOverallAverage());
        System.out.println("_______________________________________________");
        System.out.println();

        System.out.print("Confirm import? (Y/N): ");
        char confirm = ui.getScanner().next().charAt(0);
        ui.getScanner().nextLine();

        if (confirm == 'Y' || confirm == 'y') {
            // Process imported report and add grades (delegates to helper method - SRP)
            int importedCount = processImportedReport(importedReport);

            System.out.println();
            System.out.println("Import completed successfully!");
            System.out.printf("Grades imported: %d\n", importedCount);
            System.out.println();

            // Invalidate caches (grades changed)
            cacheManager.invalidateByType(CacheManager.CacheType.GRADE_REPORT);
            cacheManager.invalidateByType(CacheManager.CacheType.STATISTICS);

        } else if (confirm == 'N' || confirm == 'n') {
            System.out.println("Import cancelled\n");
        } else {
            throw new InvalidMenuChoiceException(
                "X ERROR: InvalidMenuChoiceException\n   Please enter Y or N.\n   You entered: " + confirm
            );
        }
    }

    /**
     * Import from CSV format (SRP: separate CSV parsing logic)
     */
    private StudentReport importFromCSV(MultiFormatFileHandler fileHandler, java.nio.file.Path filePath) throws Exception {
        java.util.List<String[]> csvData = fileHandler.importCSV(filePath);

        if (csvData.isEmpty()) {
            throw new Exception("CSV file is empty");
        }

        // Extract student ID from first record
        String studentId = csvData.get(0)[0];
        double totalGrade = 0.0;
        int gradeCount = 0;

        // Calculate average from all grades
        for (String[] row : csvData) {
            if (row.length >= 4) {
                totalGrade += Double.parseDouble(row[3]);
                gradeCount++;
            }
        }

        double overallAverage = gradeCount > 0 ? totalGrade / gradeCount : 0.0;

        // Create report (student info will be updated from studentManager)
        StudentReport report = new StudentReport(
            studentId, "Unknown", "Regular", overallAverage, "Imported"
        );

        // Add grades to report
        int counter = 1;
        for (String[] row : csvData) {
            if (row.length >= 4) {
                String gradeId = "GRD" + String.format("%03d", counter++);
                String date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String subjectName = row[1];
                String subjectType = row[2];
                double grade = Double.parseDouble(row[3]);

                GradeData gradeData = new GradeData(gradeId, date, subjectName, subjectType, grade);
                report.addGrade(gradeData);
            }
        }

        return report;
    }

    /**
     * Process imported report and add grades to system (SRP: separate import processing logic)
     */
    private int processImportedReport(StudentReport report) {
        if (report == null) {
            return 0;
        }

        int importedCount = 0;
        String reportStudentId = report.getStudentId();

        // Check if student exists and update report with actual student info
        try {
            Student reportStudent = studentManager.findStudent(reportStudentId);
            if (reportStudent != null) {
                report.setStudentName(reportStudent.getName());
                report.setStudentType(reportStudent.getStudentType());
            } else {
                System.out.println("⚠ Warning: Student " + reportStudentId + " not found. Grades will be imported but student must exist.");
            }
        } catch (Exception e) {
            System.out.println("⚠ Warning: Could not verify student " + reportStudentId);
        }

        // Import all grades from the report
        for (GradeData gradeData : report.getGrades()) {
            try {
                // Verify student exists
                try {
                    studentManager.findStudent(reportStudentId);
                } catch (Exception e) {
                    System.out.println("⚠ Skipping grade for non-existent student: " + reportStudentId);
                    continue;
                }

                // Create subject using factory (OCP)
                Subject reportSubject;
                if (gradeData.getSubjectType().equalsIgnoreCase("Core")) {
                    reportSubject = new CoreSubject();
                } else {
                    reportSubject = new ElectiveSubject();
                }
                reportSubject.setSubjectName(gradeData.getSubjectName());

                // Check if grade already exists
                boolean gradeExists = false;
                for (Grade existingGrade : gradeManager.getGrades()) {
                    if (existingGrade.getStudentId().equals(reportStudentId) &&
                        existingGrade.getSubject().getSubjectName().equals(gradeData.getSubjectName()) &&
                        existingGrade.getGrade() == gradeData.getGrade()) {
                        gradeExists = true;
                        break;
                    }
                }

                if (!gradeExists) {
                    // Create and add grade
                    Grade newGrade = new Grade(reportStudentId, reportSubject, (int) gradeData.getGrade());
                    newGrade.recordGrade((int) gradeData.getGrade());
                    newGrade.setGradeId();
                    gradeManager.addGrade(newGrade);
                    importedCount++;
                } else {
                    System.out.println("⚠ Skipping duplicate grade: " + gradeData.getGradeId());
                }

            } catch (Exception e) {
                System.out.println("⚠ Error importing grade " + gradeData.getGradeId() + ": " + e.getMessage());
            }
        }

        return importedCount;
    }
    
    private void handleBulkImport() throws Exception {
        System.out.println("BULK IMPORT GRADES");
        System.out.println("_______________________________________________");
        System.out.println();

        System.out.println("Place your CSV file in: ./imports/");
        System.out.println();

        System.out.println("CSV Format Required:");
        System.out.println("StudentID,SubjectName,SubjectType,Grade");
        System.out.println("Example: STU001,Mathematics,Core,85");
        System.out.println();

        System.out.print("Enter filename (without extension): ");
        String fileName = ui.getScanner().nextLine();
        System.out.println();

        // Build file path
        String csvFilePath = "./imports/" + fileName + ".csv";
        java.nio.file.Path filePath = java.nio.file.Paths.get(csvFilePath);

        // Validate file exists
        if (!java.nio.file.Files.exists(filePath)) {
            System.out.println("X ERROR: File not found: " + csvFilePath);
            System.out.println();
            return;
        }

        // Validate file is not empty
        try {
            if (java.nio.file.Files.size(filePath) == 0) {
                throw new com.gradr.exceptions.InvalidFileFormatException(
                    "X ERROR: InvalidFileFormatException\n   CSV file is empty: " + fileName + ".csv"
                );
            }
        } catch (java.io.IOException e) {
            System.out.println("X ERROR: IOException\n   Could not read file: " + e.getMessage());
            System.out.println();
            return;
        }

        System.out.println("Processing CSV file...");
        System.out.println();

        // Use CSVParser to parse the file (SRP: CSV parsing separated)
        // Pass performance monitor for I/O tracking
        SystemPerformanceMonitor perfMonitor = context.getPerformanceMonitor();
        CSVParser parser = perfMonitor != null ? 
            new CSVParser(csvFilePath, perfMonitor) : new CSVParser(csvFilePath);
        java.util.ArrayList<String[]> gradeData;

        try {
            gradeData = parser.parseGradeCSV();
        } catch (java.io.IOException e) {
            System.out.println("X ERROR: IOException\n   Failed to read CSV file: " + e.getMessage());
            System.out.println();
            return;
        } catch (com.gradr.exceptions.CSVParseException e) {
            System.out.println("X ERROR: CSVParseException\n   " + e.getMessage());
            System.out.println();
            return;
        }

        if (gradeData.isEmpty()) {
            System.out.println("X ERROR: No valid data found in CSV file");
            System.out.println();
            return;
        }

        // Display preview
        System.out.println("IMPORT PREVIEW");
        System.out.println("_______________________________________________");
        System.out.printf("Total Records: %d\n", gradeData.size());
        System.out.println();
        
        // Show first few records
        int previewCount = Math.min(5, gradeData.size());
        System.out.println("First " + previewCount + " records:");
        for (int i = 0; i < previewCount; i++) {
            String[] record = gradeData.get(i);
            System.out.printf("  %s - %s (%s): %.1f%%\n", 
                record[0], record[1], record[2], Double.parseDouble(record[3]));
        }
        if (gradeData.size() > 5) {
            System.out.println("  ... and " + (gradeData.size() - 5) + " more");
        }
        System.out.println("_______________________________________________");
        System.out.println();

        System.out.print("Proceed with import? (Y/N): ");
        char confirm = ui.getScanner().next().charAt(0);
        ui.getScanner().nextLine();

        if (confirm != 'Y' && confirm != 'y') {
            System.out.println("Import cancelled\n");
            return;
        }

        // Process bulk import (delegates to helper method - SRP)
        BulkImportResult result = processBulkImport(gradeData);

        // Display results
        System.out.println();
        System.out.println("BULK IMPORT COMPLETED");
        System.out.println("_______________________________________________");
        System.out.printf("Successfully imported: %d grades\n", result.successCount);
        System.out.printf("Skipped (errors): %d grades\n", result.errorCount);
        System.out.printf("Skipped (duplicates): %d grades\n", result.duplicateCount);
        System.out.println("_______________________________________________");
        System.out.println();

        // Show error details if any
        if (!result.errors.isEmpty() && result.errors.size() <= 10) {
            System.out.println("Error Details:");
            for (String error : result.errors) {
                System.out.println("  " + error);
            }
            System.out.println();
        } else if (result.errors.size() > 10) {
            System.out.println("Showing first 10 errors:");
            for (int i = 0; i < 10; i++) {
                System.out.println("  " + result.errors.get(i));
            }
            System.out.println("  ... and " + (result.errors.size() - 10) + " more errors");
            System.out.println();
        }

        // Invalidate caches if any grades were imported
        if (result.successCount > 0) {
            cacheManager.invalidateByType(CacheManager.CacheType.GRADE_REPORT);
            cacheManager.invalidateByType(CacheManager.CacheType.STATISTICS);
        }
    }

    /**
     * Process bulk import from parsed CSV data (SRP: separate import processing logic)
     */
    private BulkImportResult processBulkImport(java.util.ArrayList<String[]> gradeData) {
        BulkImportResult result = new BulkImportResult();

        for (int i = 0; i < gradeData.size(); i++) {
            String[] record = gradeData.get(i);
            int rowNum = i + 2; // +2 because of 0-based index and header row

            try {
                // Validate record has correct format
                if (record.length != 4) {
                    result.addError("Row " + rowNum + ": Invalid format (expected 4 columns)");
                    continue;
                }

                String studentId = record[0];
                String subjectName = record[1];
                String subjectType = record[2];
                String gradeStr = record[3];

                // Validate student exists
                Student student;
                try {
                    student = studentManager.findStudent(studentId);
                } catch (Exception e) {
                    result.addError("Row " + rowNum + ": Student not found - " + studentId);
                    continue;
                }

                // Validate subject type
                if (!subjectType.equalsIgnoreCase("Core") && !subjectType.equalsIgnoreCase("Elective")) {
                    result.addError("Row " + rowNum + ": Invalid subject type - " + subjectType);
                    continue;
                }

                // Validate and parse grade
                double gradeValue;
                try {
                    gradeValue = Double.parseDouble(gradeStr);
                    if (gradeValue < 0 || gradeValue > 100) {
                        result.addError("Row " + rowNum + ": Grade out of range (0-100) - " + gradeValue);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    result.addError("Row " + rowNum + ": Invalid grade value - " + gradeStr);
                    continue;
                }

                // Create subject using factory (OCP)
                Subject subject;
                if (subjectType.equalsIgnoreCase("Core")) {
                    subject = new CoreSubject();
                } else {
                    subject = new ElectiveSubject();
                }
                subject.setSubjectName(subjectName);

                // Check for duplicate grade
                boolean isDuplicate = false;
                for (Grade existingGrade : gradeManager.getGrades()) {
                    if (existingGrade.getStudentId().equals(studentId) &&
                        existingGrade.getSubject().getSubjectName().equalsIgnoreCase(subjectName) &&
                        existingGrade.getGrade() == gradeValue) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (isDuplicate) {
                    result.duplicateCount++;
                    continue;
                }

                // Create and add grade
                Grade grade = new Grade(studentId, subject, (int) gradeValue);
                if (grade.recordGrade((int) gradeValue)) {
                    grade.setGradeId();
                    gradeManager.addGrade(grade);
                    
                    // Update GPA for the student
                    GPACalculator gpaCalc = new GPACalculator(gradeManager);
                    double gpa = gpaCalc.calculateCumulativeGPA(studentId);
                    gradeManager.updateGPARanking(student, gpa);
                    
                    result.successCount++;
                }

            } catch (Exception e) {
                result.addError("Row " + rowNum + ": Unexpected error - " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * Inner class to track bulk import results
     */
    private static class BulkImportResult {
        int successCount = 0;
        int errorCount = 0;
        int duplicateCount = 0;
        java.util.List<String> errors = new java.util.ArrayList<>();

        void addError(String error) {
            errors.add(error);
            errorCount++;
        }
    }
    
    private void handleCalculateGPA() throws Exception {
        System.out.println("CALCULATE STUDENT GPA");
        System.out.println("_______________________________________________");
        System.out.println();

        System.out.print("Enter Student ID: ");
        String studentId = ui.getScanner().nextLine();
        System.out.println();

        // Find student (try cache first) - DIP: depends on abstraction
        Student student = findStudentWithCache(studentId);

        // Check if student has any grades
        if (student.getEnrolledSubjectsCount() == 0) {
            System.out.println("No grades recorded for this student yet.");
            System.out.println("GPA calculation unavailable.");
            System.out.println();
            return;
        }

        // Create GPA calculator and generate report (delegates to GPACalculator - SRP)
        GPACalculator gpaCalculator = new GPACalculator(gradeManager);
        String gpaReport = gpaCalculator.generateGPAReport(studentId, student, studentManager);
        
        // Display the report
        System.out.println(gpaReport);
        System.out.println();
    }
    
    private void handleClassStatistics() {
        System.out.println("VIEW CLASS STATISTICS");
        System.out.println("_______________________________________________");
        System.out.println();

        // Check if there are any grades recorded
        if (gradeManager.getGradeCount() == 0) {
            System.out.println("No grades recorded yet. Statistics unavailable.");
            System.out.println();
            return;
        }

        try {
            // Try to get cached statistics first
            String statsCacheKey = "statistics:class";
            String statsReport = (String) cacheManager.get(statsCacheKey);
            
            if (statsReport == null) {
                // Cache miss - calculate statistics
                StatisticsCalculator classStats = new StatisticsCalculator(gradeManager, studentManager);
                statsReport = classStats.generateClassStatistics();
                // Cache the statistics report
                cacheManager.put(statsCacheKey, statsReport, CacheManager.CacheType.STATISTICS);
            }
            
            System.out.println(statsReport);
        } catch (Exception e) {
            System.out.println();
            System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
            System.out.println();
        }
    }
    
    private void handleStatisticsDashboard() {
        try {
            System.out.println("REAL-TIME STATISTICS DASHBOARD");
            System.out.println("_______________________________________________");
            System.out.println();
            System.out.println("Starting background daemon thread...");
            
            // Create dashboard instance
            StatisticsDashboard newDashboard = new StatisticsDashboard(studentManager, gradeManager);
            dashboard = newDashboard; // Store reference for menu display
            
            // Register thread pool with performance monitor if available
            SystemPerformanceMonitor performanceMonitor = context.getPerformanceMonitor();
            if (performanceMonitor != null) {
                performanceMonitor.registerThreadPool("StatisticsDashboard", 
                    newDashboard.getExecutorService(), newDashboard.getMaxThreadCount());
            }
            
            // Start the dashboard (this will perform initial calculation)
            newDashboard.start();
            
            System.out.println("✓ Background daemon thread started");
            System.out.println("✓ Auto-refresh enabled (every 5 seconds)");
            System.out.println();
            System.out.println("Waiting for initial statistics calculation...");
            
            // Wait for initial calculation to complete
            int waitCount = 0;
            while (newDashboard.isCalculating() && waitCount < 20) {
                Thread.sleep(250);
                waitCount++;
            }
            
            System.out.println();
            
            // Display initial dashboard
            newDashboard.displayDashboard();
            
            // Auto-refresh approach: background thread handles display automatically
            // Main thread handles user input in a loop
            final boolean[] shouldQuit = {false};
            
            // Create input reader thread
            Thread inputThread = new Thread(() -> {
                try {
                    System.out.println();
                    System.out.println("Dashboard Commands:");
                    System.out.println("  'r' - Refresh now");
                    System.out.println("  'q' - Quit dashboard");
                    System.out.println();
                    System.out.println("Press Enter to continue...");
                    
                    while (!shouldQuit[0] && newDashboard.isRunning()) {
                        if (System.in.available() > 0) {
                            String input = ui.getScanner().nextLine().trim().toLowerCase();
                            
                            if (input.equals("q")) {
                                shouldQuit[0] = true;
                                break;
                            } else if (input.equals("r")) {
                                System.out.println("\nRefreshing dashboard...");
                                newDashboard.displayDashboard();
                                System.out.println("\nPress 'r' to refresh, 'q' to quit");
                            } else if (!input.isEmpty()) {
                                System.out.println("\nInvalid command. Press 'r' to refresh, 'q' to quit");
                            }
                        }
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    shouldQuit[0] = true;
                }
            });
            
            inputThread.setName("DashboardInputThread");
            inputThread.setDaemon(true);
            inputThread.start();
            
            // Wait for quit signal
            while (!shouldQuit[0] && newDashboard.isRunning()) {
                Thread.sleep(500);
            }
            
            // Cleanup
            newDashboard.stop();
            inputThread.interrupt();
            
            System.out.println();
            System.out.println("✓ Dashboard closed successfully");
            System.out.println("✓ Background thread terminated");
            System.out.println();
            
        } catch (Exception e) {
            System.out.println();
            System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
            if (dashboard != null) {
                dashboard.stop();
            }
            System.out.println();
        }
    }
    
    private void handleBatchReports() {
        try {
            System.out.println("GENERATE BATCH REPORTS");
            System.out.println("_______________________________________________");
            System.out.println();
            
            // Report Scope
            System.out.println("Report Scope:");
            System.out.println("1. All Students (" + studentManager.getStudentCount() + " students)");
            System.out.println("2. By Student Type (Regular/Honors)");
            System.out.println("3. By Grade Range");
            System.out.println("4. Custom Selection");
            System.out.println();
            
            System.out.print("Select scope (1-4): ");
            int scopeChoice = ui.getScanner().nextInt();
            ui.getScanner().nextLine();
            
            if (scopeChoice < 1 || scopeChoice > 4) {
                throw new InvalidMenuChoiceException(
                    "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-4).\n   You entered: " + scopeChoice
                );
            }
            
            System.out.println();
            System.out.println("Report Format:");
            System.out.println("1. CSV (Comma-Separated Values)");
            System.out.println("2. JSON (JavaScript Object Notation)");
            System.out.println("3. Binary (Serialized Java Object)");
            System.out.println("4. All formats");
            System.out.println();
            
            System.out.print("Select format (1-4): ");
            int formatChoice = ui.getScanner().nextInt();
            ui.getScanner().nextLine();
            
            if (formatChoice < 1 || formatChoice > 4) {
                throw new InvalidMenuChoiceException(
                    "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-4).\n   You entered: " + formatChoice
                );
            }
            
            System.out.println();
            System.out.println("Report Type:");
            System.out.println("1. Summary Report");
            System.out.println("2. Detailed Report");
            System.out.println("3. Transcript Format");
            System.out.println("4. Performance Analytics");
            System.out.println();
            
            System.out.print("Select type (1-4): ");
            int reportTypeChoice = ui.getScanner().nextInt();
            ui.getScanner().nextLine();
            
            if (reportTypeChoice < 1 || reportTypeChoice > 4) {
                throw new InvalidMenuChoiceException(
                    "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-4).\n   You entered: " + reportTypeChoice
                );
            }
            
            String reportType;
            switch (reportTypeChoice) {
                case 1: reportType = "summary"; break;
                case 2: reportType = "detailed"; break;
                case 3: reportType = "transcript"; break;
                case 4: reportType = "analytics"; break;
                default: reportType = "detailed"; break;
            }
            
            System.out.println();
            System.out.println("Concurrency Settings:");
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            System.out.println("Available Processors: " + availableProcessors);
            int recommendedMin = Math.max(2, availableProcessors / 2);
            int recommendedMax = Math.min(8, availableProcessors);
            System.out.println("Recommended Threads: " + recommendedMin + "-" + recommendedMax);
            System.out.println();
            
            System.out.print("Enter number of threads (1-8): ");
            int threadCount = ui.getScanner().nextInt();
            ui.getScanner().nextLine();
            
            if (threadCount < 1 || threadCount > 8) {
                throw new InvalidMenuChoiceException(
                    "X ERROR: InvalidMenuChoiceException\n   Thread count must be between 1 and 8.\n   You entered: " + threadCount
                );
            }
            
            System.out.println();
            System.out.println("Initializing thread pool...");
            
            // Initialize batch generator (SRP: separate batch generation logic)
            BatchReportGenerator batchGenerator = new BatchReportGenerator(studentManager, gradeManager);
            if (!batchGenerator.initializeThreadPool(threadCount)) {
                System.out.println("X ERROR: Failed to initialize thread pool\n");
                return;
            }
            
            // Register thread pool with performance monitor if available
            SystemPerformanceMonitor performanceMonitor = context.getPerformanceMonitor();
            if (performanceMonitor != null && batchGenerator.getExecutorService() != null) {
                performanceMonitor.registerThreadPool("BatchReportGenerator", 
                    batchGenerator.getExecutorService(), threadCount);
            }
            
            System.out.println("✓ Fixed Thread Pool created: " + threadCount + " threads");
            System.out.println();
            
            // Get students based on scope (delegates to helper method - SRP)
            java.util.List<Student> studentsToProcess = getStudentsByScope(scopeChoice);
            
            if (studentsToProcess.isEmpty()) {
                System.out.println("No students found matching the selected scope.");
                System.out.println();
                batchGenerator.shutdown(5);
                return;
            }
            
            System.out.println("Processing " + studentsToProcess.size() + " student reports...");
            System.out.println();
            
            // Generate batch reports (delegates to BatchReportGenerator - DIP)
            BatchReportGenerator.BatchResult result = batchGenerator.generateBatchReports(
                studentsToProcess, reportType, formatChoice
            );
            
            // Shutdown thread pool
            batchGenerator.shutdown(5);
            
            // Display results
            System.out.println();
            System.out.println("✓ BATCH GENERATION COMPLETED!");
            System.out.println();
            System.out.println("EXECUTION SUMMARY");
            System.out.println("_______________________________________________");
            System.out.println("Total Reports: " + result.getTotalReports());
            System.out.println("Successful: " + result.getSuccessful());
            System.out.println("Failed: " + result.getFailed());
            System.out.println("Total Time: " + String.format("%.1f", result.getTotalTime() / 1000.0) + " seconds");
            System.out.println("Avg Time per Report: " + String.format("%.0f", result.getAvgTimePerReport()) + "ms");
            System.out.println("Sequential Processing (estimated): " + 
                String.format("%.0f", result.getEstimatedSequential() / 1000.0) + " seconds");
            System.out.println("Concurrent Processing (actual): " + 
                String.format("%.1f", result.getTotalTime() / 1000.0) + " seconds");
            double performanceGain = result.getEstimatedSequential() > 0 ? 
                (double)result.getEstimatedSequential() / result.getTotalTime() : 1.0;
            System.out.println("Performance Gain: " + String.format("%.1f", performanceGain) + "x faster");
            System.out.println();
            
            BatchReportGenerator.ThreadPoolStats stats = batchGenerator.getThreadPoolStats();
            System.out.println("Thread Pool Statistics:");
            System.out.println("Peak Thread Count: " + stats.getPoolSize());
            System.out.println("Total Tasks Executed: " + result.getTotalReports());
            System.out.println("Average Queue Time: " + String.format("%.0f", 0.0) + "ms");
            System.out.println("Thread Utilization: " + String.format("%.1f", stats.getThreadUtilization()) + "%");
            System.out.println();
            
            System.out.println("Output Location: " + result.getOutputDir());
            System.out.println("Total Files Generated: " + result.getSuccessful());
            
            // Calculate total file size
            try {
                long totalSize = java.nio.file.Files.walk(result.getOutputDir())
                    .filter(java.nio.file.Files::isRegularFile)
                    .mapToLong(p -> {
                        try { return java.nio.file.Files.size(p); } 
                        catch (java.io.IOException e) { return 0; }
                    })
                    .sum();
                System.out.println("Total Size: " + formatFileSize(totalSize));
            } catch (java.io.IOException e) {
                // Ignore if we can't calculate size
            }
            System.out.println();
            
        } catch (InvalidMenuChoiceException e) {
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
        } catch (Exception e) {
            System.out.println();
            System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
            System.out.println();
        }
    }
    
    /**
     * Get students based on scope selection (SRP: separate scope filtering logic)
     */
    private java.util.List<Student> getStudentsByScope(int scopeChoice) throws Exception {
        java.util.List<Student> studentsToProcess = new java.util.ArrayList<>();
        
        switch (scopeChoice) {
            case 1: // All Students
                studentsToProcess = new java.util.ArrayList<>(studentManager.getStudentsList());
                break;
                
            case 2: // By Student Type
                System.out.println("Student Type Filter:");
                System.out.println("1. Regular Students only");
                System.out.println("2. Honors Students only");
                System.out.println("3. Both types");
                System.out.println();
                
                System.out.print("Select type (1-3): ");
                int typeChoice = ui.getScanner().nextInt();
                ui.getScanner().nextLine();
                
                for (Student student : studentManager.getStudentsList()) {
                    if (typeChoice == 1 && student.getStudentType().equals("Regular")) {
                        studentsToProcess.add(student);
                    } else if (typeChoice == 2 && student.getStudentType().equals("Honors")) {
                        studentsToProcess.add(student);
                    } else if (typeChoice == 3) {
                        studentsToProcess.add(student);
                    }
                }
                break;
                
            case 3: // By Grade Range
                System.out.print("Enter minimum average grade (0-100): ");
                double minGrade = ui.getScanner().nextDouble();
                System.out.print("Enter maximum average grade (0-100): ");
                double maxGrade = ui.getScanner().nextDouble();
                ui.getScanner().nextLine();
                System.out.println();
                
                for (Student student : studentManager.getStudentsList()) {
                    double avg = student.calculateAverageGrade();
                    if (avg >= minGrade && avg <= maxGrade) {
                        studentsToProcess.add(student);
                    }
                }
                break;
                
            case 4: // Custom Selection
                System.out.println("Available Students:");
                int index = 1;
                for (Student student : studentManager.getStudentsList()) {
                    System.out.printf("%d. %s (%s) - Avg: %.1f%%\n", 
                        index++, student.getName(), student.getStudentId(), student.calculateAverageGrade());
                }
                System.out.println();
                
                System.out.print("Enter student numbers (comma-separated, e.g., 1,3,5): ");
                String input = ui.getScanner().nextLine();
                String[] indices = input.split(",");
                
                java.util.List<Student> allStudents = studentManager.getStudentsList();
                for (String indexStr : indices) {
                    try {
                        int studentIndex = Integer.parseInt(indexStr.trim()) - 1;
                        if (studentIndex >= 0 && studentIndex < allStudents.size()) {
                            studentsToProcess.add(allStudents.get(studentIndex));
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid indices
                    }
                }
                break;
                
            default:
                throw new InvalidMenuChoiceException("Invalid scope choice");
        }
        
        return studentsToProcess;
    }
    
    /**
     * Format file size from bytes to human-readable format (SRP: separate formatting logic)
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            double kb = bytes / 1024.0;
            return String.format("%.2f KB", kb);
        } else {
            double mb = bytes / (1024.0 * 1024.0);
            return String.format("%.2f MB", mb);
        }
    }
    
    private void handleAdvancedSearch() {
        System.out.println("SEARCH STUDENTS (Advanced) [ENHANCED]");
        System.out.println("_______________________________________________");
        System.out.println();

        System.out.println("Search options:");
        System.out.println("1. By Student ID");
        System.out.println("2. By name (partial match)");
        System.out.println("3. By Grade Range");
        System.out.println("4. By Student Type");
        System.out.println();

        try {
            System.out.print("Select option (1-4): ");
            int searchOption = ui.getScanner().nextInt();
            ui.getScanner().nextLine();
            
            if (searchOption < 1 || searchOption > 4) {
                throw new InvalidMenuChoiceException(
                    "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-4).\n   You entered: " + searchOption
                );
            }

            System.out.println();

            switch (searchOption) {
                case 1: // By Student ID
                    searchByStudentId();
                    break;
                    
                case 2: // By name (partial match)
                    searchByName();
                    break;
                    
                case 3: // By Grade Range
                    searchByGradeRange();
                    break;
                    
                case 4: // By Student Type
                    searchByStudentType();
                    break;
            }
            
        } catch (InvalidMenuChoiceException e) {
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
        } catch (Exception e) {
            System.out.println();
            System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
            System.out.println();
        }
    }
    
    /**
     * Search for student by ID (SRP: separate search logic)
     */
    private void searchByStudentId() throws Exception {
        System.out.print("Enter Student ID: ");
        String searchId = ui.getScanner().nextLine();
        System.out.println();
        
        // Find student (try cache first) - DIP: depends on abstraction
        Student foundStudent = findStudentWithCache(searchId);
        
        // Display result
        System.out.println("SEARCH RESULTS (1 found)");
        System.out.println("_______________________________________________");
        System.out.println();
        System.out.printf("Student ID: %s\n", foundStudent.getStudentId());
        System.out.printf("Name: %s\n", foundStudent.getName());
        System.out.printf("Type: %s Student\n", foundStudent.getStudentType());
        System.out.printf("Age: %d\n", foundStudent.getAge());
        System.out.printf("Email: %s\n", foundStudent.getEmail());
        System.out.printf("Phone: %s\n", foundStudent.getPhone());
        System.out.printf("Average Grade: %.2f%%\n", foundStudent.calculateAverageGrade());
        System.out.printf("Total Grades: %d\n", foundStudent.getEnrolledSubjectsCount());
        System.out.printf("Status: %s\n", foundStudent.getStatus());
        System.out.println();
    }
    
    /**
     * Search for students by name (partial match) (SRP: separate search logic)
     */
    private void searchByName() {
        System.out.print("Enter name (partial match): ");
        String searchName = ui.getScanner().nextLine().toLowerCase();
        System.out.println();
        
        // Search through all students
        java.util.List<Student> matchedStudents = new java.util.ArrayList<>();
        for (Student student : studentManager.getStudentsList()) {
            if (student.getName().toLowerCase().contains(searchName)) {
                matchedStudents.add(student);
            }
        }
        
        // Display results
        System.out.println("SEARCH RESULTS (" + matchedStudents.size() + " found)");
        System.out.println("_______________________________________________");
        System.out.println();
        
        if (matchedStudents.isEmpty()) {
            System.out.println("No students found matching: " + searchName);
        } else {
            System.out.println("STU ID   | NAME                    | TYPE        | AVG GRADE | TOTAL GRADES");
            System.out.println("------------------------------------------------------------------------------");
            
            for (Student student : matchedStudents) {
                System.out.printf("%-8s | %-23s | %-11s | %-9.2f | %-12d\n",
                    student.getStudentId(),
                    student.getName().length() > 23 ? student.getName().substring(0, 20) + "..." : student.getName(),
                    student.getStudentType(),
                    student.calculateAverageGrade(),
                    student.getEnrolledSubjectsCount());
            }
        }
        System.out.println();
    }
    
    /**
     * Search for students by grade range (SRP: separate search logic)
     */
    private void searchByGradeRange() {
        System.out.print("Enter minimum average grade (0-100): ");
        double minGrade = ui.getScanner().nextDouble();
        System.out.print("Enter maximum average grade (0-100): ");
        double maxGrade = ui.getScanner().nextDouble();
        ui.getScanner().nextLine();
        System.out.println();
        
        // Validate range
        if (minGrade < 0 || maxGrade > 100 || minGrade > maxGrade) {
            System.out.println("Invalid grade range. Please enter values between 0-100 with min <= max.");
            System.out.println();
            return;
        }
        
        // Search through all students
        java.util.List<Student> matchedStudents = new java.util.ArrayList<>();
        for (Student student : studentManager.getStudentsList()) {
            double avg = student.calculateAverageGrade();
            if (avg >= minGrade && avg <= maxGrade) {
                matchedStudents.add(student);
            }
        }
        
        // Display results
        System.out.println("SEARCH RESULTS (" + matchedStudents.size() + " found)");
        System.out.println("Grade Range: " + minGrade + "% - " + maxGrade + "%");
        System.out.println("_______________________________________________");
        System.out.println();
        
        if (matchedStudents.isEmpty()) {
            System.out.println("No students found in grade range: " + minGrade + "% - " + maxGrade + "%");
        } else {
            System.out.println("STU ID   | NAME                    | TYPE        | AVG GRADE | STATUS");
            System.out.println("--------------------------------------------------------------------------");
            
            // Sort by average grade (descending)
            matchedStudents.sort((s1, s2) -> Double.compare(s2.calculateAverageGrade(), s1.calculateAverageGrade()));
            
            for (Student student : matchedStudents) {
                System.out.printf("%-8s | %-23s | %-11s | %-9.2f | %-10s\n",
                    student.getStudentId(),
                    student.getName().length() > 23 ? student.getName().substring(0, 20) + "..." : student.getName(),
                    student.getStudentType(),
                    student.calculateAverageGrade(),
                    student.getStatus());
            }
            
            // Display statistics
            System.out.println("--------------------------------------------------------------------------");
            double totalAvg = matchedStudents.stream()
                .mapToDouble(Student::calculateAverageGrade)
                .average()
                .orElse(0.0);
            System.out.printf("Average Grade (filtered): %.2f%%\n", totalAvg);
        }
        System.out.println();
    }
    
    /**
     * Search for students by type (SRP: separate search logic)
     */
    private void searchByStudentType() {
        System.out.println("Student Type:");
        System.out.println("1. Regular Students");
        System.out.println("2. Honors Students");
        System.out.println("3. Both");
        System.out.println();
        
        System.out.print("Select type (1-3): ");
        int typeChoice = ui.getScanner().nextInt();
        ui.getScanner().nextLine();
        System.out.println();
        
        if (typeChoice < 1 || typeChoice > 3) {
            System.out.println("Invalid choice. Please select 1-3.");
            System.out.println();
            return;
        }
        
        // Search through all students
        java.util.List<Student> matchedStudents = new java.util.ArrayList<>();
        String searchType = "";
        
        for (Student student : studentManager.getStudentsList()) {
            if (typeChoice == 1 && student.getStudentType().equals("Regular")) {
                matchedStudents.add(student);
                searchType = "Regular";
            } else if (typeChoice == 2 && student.getStudentType().equals("Honors")) {
                matchedStudents.add(student);
                searchType = "Honors";
            } else if (typeChoice == 3) {
                matchedStudents.add(student);
                searchType = "All";
            }
        }
        
        // Display results
        System.out.println("SEARCH RESULTS (" + matchedStudents.size() + " found)");
        if (!searchType.equals("All")) {
            System.out.println("Type: " + searchType + " Students");
        }
        System.out.println("_______________________________________________");
        System.out.println();
        
        if (matchedStudents.isEmpty()) {
            System.out.println("No " + searchType.toLowerCase() + " students found.");
        } else {
            System.out.println("STU ID   | NAME                    | TYPE        | AVG GRADE | PASSING GRADE | STATUS");
            System.out.println("--------------------------------------------------------------------------------------");
            
            for (Student student : matchedStudents) {
                System.out.printf("%-8s | %-23s | %-11s | %-9.2f | %-13d%% | %-10s\n",
                    student.getStudentId(),
                    student.getName().length() > 23 ? student.getName().substring(0, 20) + "..." : student.getName(),
                    student.getStudentType(),
                    student.calculateAverageGrade(),
                    (int) student.getPassingGrade(),
                    student.getStatus());
            }
            
            // Display statistics by type
            System.out.println("--------------------------------------------------------------------------------------");
            if (typeChoice == 3) {
                // Show breakdown by type
                long regularCount = matchedStudents.stream()
                    .filter(s -> s.getStudentType().equals("Regular"))
                    .count();
                long honorsCount = matchedStudents.stream()
                    .filter(s -> s.getStudentType().equals("Honors"))
                    .count();
                System.out.printf("Regular Students: %d, Honors Students: %d\n", regularCount, honorsCount);
            }
            
            double avgGrade = matchedStudents.stream()
                .mapToDouble(Student::calculateAverageGrade)
                .average()
                .orElse(0.0);
            System.out.printf("Average Grade: %.2f%%\n", avgGrade);
        }
        System.out.println();
    }
    
    private void handlePatternSearch() {
        try {
            System.out.println("PATTERN-BASED SEARCH");
            System.out.println("_______________________________________________");
            System.out.println();
            
            PatternSearchService searchService = context.getPatternSearchService();
            
            System.out.println("Search Type:");
            System.out.println("1. Email Domain Pattern (e.g., @university.edu)");
            System.out.println("2. Phone Area Code Pattern (e.g., 555)");
            System.out.println("3. Student ID Pattern (e.g., STU0**)");
            System.out.println("4. Name Pattern (regex)");
            System.out.println("5. Custom Regex Pattern");
            System.out.println();
            
            System.out.print("Select type (1-5): ");
            int searchTypeChoice;
            try {
                searchTypeChoice = ui.getScanner().nextInt();
                ui.getScanner().nextLine();
            } catch (InputMismatchException e) {
                ui.getScanner().nextLine();
                throw new InvalidMenuChoiceException("Please enter a valid number (1-5).");
            }
            
            System.out.println();
            System.out.print("Case-insensitive search? (Y/N): ");
            String caseInsensitiveInput = ui.getScanner().nextLine().trim().toUpperCase();
            boolean caseInsensitive = caseInsensitiveInput.equals("Y");
            System.out.println();
            
            PatternSearchService.SearchResults searchResults = null;
            String patternInput = "";
            
            switch (searchTypeChoice) {
                case 1:
                    // Email Domain Pattern
                    System.out.print("Enter email domain (e.g., @university.edu): ");
                    patternInput = ui.getScanner().nextLine().trim();
                    if (!patternInput.startsWith("@")) {
                        patternInput = "@" + patternInput;
                    }
                    System.out.println();
                    searchResults = searchService.searchByEmailDomain(patternInput, caseInsensitive);
                    break;
                    
                case 2:
                    // Phone Area Code Pattern
                    System.out.print("Enter area code (e.g., 555): ");
                    patternInput = ui.getScanner().nextLine().trim();
                    System.out.println();
                    searchResults = searchService.searchByPhoneAreaCode(patternInput, caseInsensitive);
                    break;
                    
                case 3:
                    // Student ID Pattern
                    System.out.print("Enter Student ID pattern (e.g., STU0**): ");
                    patternInput = ui.getScanner().nextLine().trim();
                    System.out.println();
                    searchResults = searchService.searchByStudentIdPattern(patternInput, caseInsensitive);
                    break;
                    
                case 4:
                    // Name Pattern
                    System.out.print("Enter name pattern (regex): ");
                    patternInput = ui.getScanner().nextLine().trim();
                    System.out.println();
                    searchResults = searchService.searchByNamePattern(patternInput, caseInsensitive);
                    break;
                    
                case 5:
                    // Custom Regex Pattern
                    System.out.println("Custom Regex Search");
                    System.out.println("1. Search in Names");
                    System.out.println("2. Search in Emails");
                    System.out.println("3. Search in Phones");
                    System.out.println("4. Search in Student IDs");
                    System.out.println();
                    
                    System.out.print("Select field (1-4): ");
                    int fieldChoice;
                    try {
                        fieldChoice = ui.getScanner().nextInt();
                        ui.getScanner().nextLine();
                    } catch (InputMismatchException e) {
                        ui.getScanner().nextLine();
                        throw new InvalidMenuChoiceException("Please enter a valid number (1-4).");
                    }
                    
                    System.out.println();
                    System.out.print("Enter regex pattern: ");
                    patternInput = ui.getScanner().nextLine().trim();
                    System.out.println();
                    
                    String fieldName;
                    switch (fieldChoice) {
                        case 1: fieldName = "name"; break;
                        case 2: fieldName = "email"; break;
                        case 3: fieldName = "phone"; break;
                        case 4: fieldName = "studentId"; break;
                        default: throw new InvalidMenuChoiceException("Invalid field choice. Please select 1-4.");
                    }
                    
                    searchResults = searchService.searchByCustomPattern(fieldName, patternInput, caseInsensitive);
                    break;
                    
                default:
                    throw new InvalidMenuChoiceException("Invalid search type. Please select 1-5.");
            }
            
            if (searchResults == null) {
                throw new IllegalStateException("Search operation failed to return results.");
            }
            
            // Display results
            List<PatternSearchService.SearchResult> results = searchResults.getResults();
            PatternSearchService.SearchStatistics stats = searchResults.getStatistics();
            
            System.out.println("SEARCH RESULTS (" + results.size() + " found)");
            System.out.println("_______________________________________________");
            System.out.println();
            
            if (results.isEmpty()) {
                System.out.println("No students match the pattern: " + patternInput);
                System.out.println();
            } else {
                System.out.printf("%-10s | %-20s | %-12s | %-25s | %-12s%n", 
                    "STUDENT ID", "NAME", "TYPE", "MATCH FIELD", "MATCHED VALUE");
                System.out.println("_______________________________________________");
                
                for (PatternSearchService.SearchResult result : results) {
                    System.out.printf("%-10s | %-20s | %-12s | %-25s | %-12s%n",
                        result.getStudent().getStudentId(),
                        result.getStudent().getName().length() > 20 ? 
                            result.getStudent().getName().substring(0, 17) + "..." : result.getStudent().getName(),
                        result.getStudent().getStudentType(),
                        result.getMatchedField(),
                        result.getMatchedText().length() > 12 ? 
                            result.getMatchedText().substring(0, 9) + "..." : result.getMatchedText());
                }
                System.out.println("_______________________________________________");
                System.out.println();
                
                // Display statistics
                System.out.println("SEARCH STATISTICS");
                System.out.println("_______________________________________________");
                System.out.println("Total Scanned: " + stats.getTotalScanned());
                System.out.println("Matches Found: " + stats.getMatchesFound());
                System.out.println("Match Rate: " + String.format("%.1f", stats.getMatchPercentage()) + "%");
                System.out.println("Search Time: " + stats.getSearchTime() + "ms");
                System.out.println("Regex Complexity: " + stats.getRegexComplexity());
                System.out.println();
                
                // Display distribution by student type
                System.out.println("Distribution by Type:");
                int regularCount = 0;
                int honorsCount = 0;
                for (PatternSearchService.SearchResult result : results) {
                    if (result.getStudent().getStudentType().equals("Regular")) {
                        regularCount++;
                    } else {
                        honorsCount++;
                    }
                }
                System.out.println("  Regular Students: " + regularCount);
                System.out.println("  Honors Students: " + honorsCount);
                System.out.println();
            }
            
        } catch (IllegalArgumentException e) {
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
        } catch (InvalidMenuChoiceException e) {
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
        } catch (Exception e) {
            System.out.println();
            System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
            System.out.println();
        }
    }
    
    private void handleQueryGradeHistory() {
        try {
            System.out.println("QUERY GRADE HISTORY");
            System.out.println("_______________________________________________");
            System.out.println();
            
            if (gradeManager.getGradeCount() == 0) {
                System.out.println("No grades recorded yet. History unavailable.");
                System.out.println();
                return;
            }
            
            System.out.println("Query Options:");
            System.out.println("1. Filter by Student ID");
            System.out.println("2. Filter by Subject Name");
            System.out.println("3. Filter by Subject Type (Core/Elective)");
            System.out.println("4. Filter by Grade Range");
            System.out.println("5. Filter by Date Range");
            System.out.println("6. View All Grades");
            System.out.println("7. Return to Main Menu");
            System.out.println();
            
            System.out.print("Select option (1-7): ");
            int queryOption;
            try {
                queryOption = ui.getScanner().nextInt();
                ui.getScanner().nextLine();
            } catch (InputMismatchException e) {
                ui.getScanner().nextLine();
                throw new InvalidMenuChoiceException("Please enter a valid number (1-7).");
            }
            
            System.out.println();
            
            if (queryOption == 7) {
                return; // Return to main menu
            }
            
            if (queryOption < 1 || queryOption > 7) {
                throw new InvalidMenuChoiceException("Invalid option. Please select 1-7.");
            }
            
            List<Grade> queryResults = new ArrayList<>();
            
            switch (queryOption) {
                case 1:
                    // Filter by Student ID
                    System.out.print("Enter Student ID: ");
                    String studentId = ui.getScanner().nextLine().trim();
                    System.out.println();
                    queryResults = queryGradesByStudentId(studentId);
                    break;
                    
                case 2:
                    // Filter by Subject Name
                    System.out.println("Common Subjects:");
                    System.out.println("Core: Mathematics, English, Science");
                    System.out.println("Elective: Music, Art, Physical Education");
                    System.out.println();
                    System.out.print("Enter subject name (or leave empty for all): ");
                    String subjectName = ui.getScanner().nextLine().trim();
                    System.out.println();
                    queryResults = queryGradesBySubjectName(subjectName);
                    break;
                    
                case 3:
                    // Filter by Subject Type
                    System.out.println("Subject Types:");
                    System.out.println("1. Core Subjects");
                    System.out.println("2. Elective Subjects");
                    System.out.println("3. Both");
                    System.out.println();
                    System.out.print("Select type (1-3): ");
                    int typeChoice;
                    try {
                        typeChoice = ui.getScanner().nextInt();
                        ui.getScanner().nextLine();
                    } catch (InputMismatchException e) {
                        ui.getScanner().nextLine();
                        throw new InvalidMenuChoiceException("Please enter a valid number (1-3).");
                    }
                    
                    System.out.println();
                    String subjectType;
                    switch (typeChoice) {
                        case 1: subjectType = "Core"; break;
                        case 2: subjectType = "Elective"; break;
                        case 3: subjectType = ""; break; // Empty for both
                        default: throw new InvalidMenuChoiceException("Invalid choice. Please select 1-3.");
                    }
                    queryResults = queryGradesBySubjectType(subjectType);
                    break;
                    
                case 4:
                    // Filter by Grade Range
                    System.out.print("Enter minimum grade (0-100): ");
                    String minGradeStr = ui.getScanner().nextLine().trim();
                    System.out.print("Enter maximum grade (0-100): ");
                    String maxGradeStr = ui.getScanner().nextLine().trim();
                    System.out.println();
                    
                    double minGrade, maxGrade;
                    try {
                        minGrade = Double.parseDouble(minGradeStr);
                        maxGrade = Double.parseDouble(maxGradeStr);
                        if (minGrade < 0 || minGrade > 100 || maxGrade < 0 || maxGrade > 100) {
                            throw new NumberFormatException("Grades must be between 0 and 100");
                        }
                        if (minGrade > maxGrade) {
                            throw new NumberFormatException("Minimum grade cannot be greater than maximum grade");
                        }
                    } catch (NumberFormatException e) {
                        throw new NumberFormatException("Invalid grade range: " + e.getMessage());
                    }
                    
                    queryResults = queryGradesByRange(minGrade, maxGrade);
                    break;
                    
                case 5:
                    // Filter by Date Range
                    System.out.println("Date format: YYYY-MM-DD (e.g., 2024-01-15)");
                    System.out.print("Enter start date (or leave empty for all): ");
                    String startDate = ui.getScanner().nextLine().trim();
                    System.out.print("Enter end date (or leave empty for all): ");
                    String endDate = ui.getScanner().nextLine().trim();
                    System.out.println();
                    
                    queryResults = queryGradesByDateRange(startDate, endDate);
                    break;
                    
                case 6:
                    // View All Grades
                    queryResults = new ArrayList<>(gradeManager.getGradeHistory());
                    break;
            }
            
            // Display results
            displayQueryResults(queryResults);
            
        } catch (NumberFormatException e) {
            System.out.println();
            System.out.println("X ERROR: NumberFormatException\n   " + e.getMessage());
            System.out.println();
        } catch (InvalidMenuChoiceException e) {
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
        } catch (Exception e) {
            System.out.println();
            System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
            System.out.println();
        }
    }
    
    /**
     * Query grades by Student ID
     */
    private List<Grade> queryGradesByStudentId(String studentId) {
        List<Grade> results = new ArrayList<>();
        for (Grade grade : gradeManager.getGradeHistory()) {
            if (grade.getStudentId().equalsIgnoreCase(studentId)) {
                results.add(grade);
            }
        }
        return results;
    }
    
    /**
     * Query grades by Subject Name
     */
    private List<Grade> queryGradesBySubjectName(String subjectName) {
        List<Grade> results = new ArrayList<>();
        List<Grade> allGrades = gradeManager.getGradeHistory();
        
        if (subjectName.isEmpty()) {
            return new ArrayList<>(allGrades);
        }
        
        for (Grade grade : allGrades) {
            if (grade != null && grade.getSubject() != null && 
                grade.getSubject().getSubjectName() != null &&
                grade.getSubject().getSubjectName().equalsIgnoreCase(subjectName)) {
                results.add(grade);
            }
        }
        return results;
    }
    
    /**
     * Query grades by Subject Type
     */
    private List<Grade> queryGradesBySubjectType(String subjectType) {
        List<Grade> results = new ArrayList<>();
        List<Grade> allGrades = gradeManager.getGradeHistory();
        
        if (subjectType.isEmpty()) {
            // Return all grades (for "Both" option)
            return new ArrayList<>(allGrades);
        }
        
        for (Grade grade : allGrades) {
            if (grade != null && grade.getSubject() != null && 
                grade.getSubject().getSubjectType() != null &&
                grade.getSubject().getSubjectType().equalsIgnoreCase(subjectType)) {
                results.add(grade);
            }
        }
        return results;
    }
    
    /**
     * Query grades by Grade Range
     */
    private List<Grade> queryGradesByRange(double minGrade, double maxGrade) {
        List<Grade> results = new ArrayList<>();
        List<Grade> allGrades = gradeManager.getGradeHistory();
        
        for (Grade grade : allGrades) {
            if (grade != null && grade.getGrade() >= minGrade && grade.getGrade() <= maxGrade) {
                results.add(grade);
            }
        }
        return results;
    }
    
    /**
     * Query grades by Date Range
     */
    private List<Grade> queryGradesByDateRange(String startDate, String endDate) {
        List<Grade> results = new ArrayList<>();
        List<Grade> allGrades = gradeManager.getGradeHistory();
        
        for (Grade grade : allGrades) {
            if (grade == null || grade.getDate() == null) {
                continue;
            }
            
            String gradeDate = grade.getDate();
            boolean matches = true;
            
            if (!startDate.isEmpty() && gradeDate.compareTo(startDate) < 0) {
                matches = false;
            }
            if (!endDate.isEmpty() && gradeDate.compareTo(endDate) > 0) {
                matches = false;
            }
            
            if (matches) {
                results.add(grade);
            }
        }
        return results;
    }
    
    /**
     * Display query results with statistics
     */
    private void displayQueryResults(List<Grade> results) {
        System.out.println("QUERY RESULTS");
        System.out.println("_______________________________________________");
        System.out.println();
        
        System.out.printf("%-9s | %-12s | %-20s | %-10s | %-16s | %-10s | %-6s%n", 
            "GRD ID", "STUDENT ID", "STUDENT NAME", "SUBJECT", "SUBJECT TYPE", "DATE", "GRADE");
        System.out.println("_______________________________________________");
        
        double totalGrade = 0.0;
        Map<String, Integer> subjectCount = new HashMap<>();
        Map<String, Integer> studentCount = new HashMap<>();
        
        for (Grade grade : results) {
            String studentName = "Unknown";
            try {
                Student student = studentManager.findStudent(grade.getStudentId());
                studentName = student.getName();
            } catch (Exception e) {
                // Student not found, use "Unknown"
            }
            
            System.out.printf("%-9s | %-12s | %-20s | %-10s | %-16s | %-10s | %-6.1f%%%n",
                grade.getGradeId(),
                grade.getStudentId(),
                studentName.length() > 20 ? studentName.substring(0, 17) + "..." : studentName,
                grade.getSubject().getSubjectName().length() > 10 ? 
                    grade.getSubject().getSubjectName().substring(0, 7) + "..." : grade.getSubject().getSubjectName(),
                grade.getSubject().getSubjectType(),
                grade.getDate(),
                grade.getGrade());
            
            totalGrade += grade.getGrade();
            subjectCount.put(grade.getSubject().getSubjectName(), 
                subjectCount.getOrDefault(grade.getSubject().getSubjectName(), 0) + 1);
            studentCount.put(grade.getStudentId(), 
                studentCount.getOrDefault(grade.getStudentId(), 0) + 1);
        }
        
        System.out.println("_______________________________________________");
        System.out.println();
        
        // Statistics
        System.out.println("Statistics:");
        System.out.println("  Total Grades: " + results.size());
        if (results.size() > 0) {
            System.out.printf("  Average Grade: %.2f%%%n", totalGrade / results.size());
            System.out.println("  Unique Students: " + studentCount.size());
            System.out.println("  Unique Subjects: " + subjectCount.size());
            
            // Top subjects
            if (!subjectCount.isEmpty()) {
                System.out.println();
                System.out.println("  Top Subjects:");
                subjectCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> System.out.printf("    - %s: %d grades%n", entry.getKey(), entry.getValue()));
            }
        }
        System.out.println();
    }
    
    private void handleScheduleTasks() {
        try {
            System.out.println("SCHEDULE AUTOMATED TASKS");
            System.out.println("_______________________________________________");
            System.out.println();
            
            // Initialize TaskScheduler if not already initialized
            TaskScheduler taskScheduler = context.getTaskScheduler();
            if (taskScheduler == null) {
                taskScheduler = new TaskScheduler(studentManager, gradeManager);
                context.setTaskScheduler(taskScheduler);
                System.out.println("✓ Task Scheduler initialized");
                System.out.println();
            }
            
            // Display current scheduled tasks
            List<ScheduledTask> currentTasks = taskScheduler.getActiveTasks();
            int activeCount = 0;
            for (ScheduledTask task : currentTasks) {
                if (task.isActive()) {
                    activeCount++;
                }
            }
            
            System.out.println("Current Status: " + activeCount + " active task(s), " + 
                             (currentTasks.size() - activeCount) + " inactive");
            System.out.println();
            
            System.out.println("Task Management:");
            System.out.println("1. Schedule New Task");
            System.out.println("2. View Scheduled Tasks");
            System.out.println("3. View Task Execution Log");
            System.out.println("4. Cancel Scheduled Task");
            System.out.println("5. Return to Main Menu");
            System.out.println();
            
            System.out.print("Select option (1-5): ");
            int taskOption;
            try {
                taskOption = ui.getScanner().nextInt();
                ui.getScanner().nextLine();
            } catch (InputMismatchException e) {
                ui.getScanner().nextLine();
                throw new InvalidMenuChoiceException("Please enter a valid number (1-5).");
            }
            
            System.out.println();
            
            switch (taskOption) {
                case 1:
                    // Schedule New Task
                    scheduleNewTask(taskScheduler);
                    break;
                    
                case 2:
                    // View Scheduled Tasks
                    viewScheduledTasks(taskScheduler);
                    break;
                    
                case 3:
                    // View Task Execution Log
                    viewTaskExecutionLog(taskScheduler);
                    break;
                    
                case 4:
                    // Cancel Scheduled Task
                    cancelScheduledTask(taskScheduler);
                    break;
                    
                case 5:
                    // Return to Main Menu
                    return;
                    
                default:
                    throw new InvalidMenuChoiceException("Invalid option. Please select 1-5.");
            }
            
        } catch (InvalidMenuChoiceException e) {
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
        } catch (Exception e) {
            System.out.println();
            System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
            System.out.println();
        }
    }
    
    /**
     * Schedule a new task
     */
    private void scheduleNewTask(TaskScheduler taskScheduler) {
        try {
            System.out.println("SCHEDULE NEW TASK");
            System.out.println("_______________________________________________");
            System.out.println();
            
            System.out.println("Task Type:");
            System.out.println("1. GPA Update & Ranking");
            System.out.println("2. Batch Report Generation");
            System.out.println("3. Database Backup");
            System.out.println("4. Cache Refresh");
            System.out.println("5. Grade Reminder Notifications");
            System.out.println();
            
            System.out.print("Select type (1-5): ");
            int typeChoice;
            try {
                typeChoice = ui.getScanner().nextInt();
                ui.getScanner().nextLine();
            } catch (InputMismatchException e) {
                ui.getScanner().nextLine();
                throw new InvalidMenuChoiceException("Please enter a valid number (1-5).");
            }
            
            if (typeChoice < 1 || typeChoice > 5) {
                throw new InvalidMenuChoiceException("Invalid task type. Please select 1-5.");
            }
            
            System.out.println();
            
            // Get task name
            System.out.print("Enter task name: ");
            String taskName = ui.getScanner().nextLine().trim();
            System.out.println();
            
            // Get schedule type
            System.out.println("Schedule Type:");
            System.out.println("1. Daily");
            System.out.println("2. Hourly");
            System.out.println("3. Weekly");
            System.out.println();
            
            System.out.print("Select schedule (1-3): ");
            int scheduleChoice;
            try {
                scheduleChoice = ui.getScanner().nextInt();
                ui.getScanner().nextLine();
            } catch (InputMismatchException e) {
                ui.getScanner().nextLine();
                throw new InvalidMenuChoiceException("Please enter a valid number (1-3).");
            }
            
            System.out.println();
            
            ScheduledTask.ScheduleType scheduleType;
            switch (scheduleChoice) {
                case 1: scheduleType = ScheduledTask.ScheduleType.DAILY; break;
                case 2: scheduleType = ScheduledTask.ScheduleType.HOURLY; break;
                case 3: scheduleType = ScheduledTask.ScheduleType.WEEKLY; break;
                default: throw new InvalidMenuChoiceException("Invalid schedule type. Please select 1-3.");
            }
            
            // Get task-specific configuration
            String scope = "All Students";
            int threadCount = 2;
            
            if (typeChoice == 2) { // Batch Report Generation
                System.out.println("Report Scope:");
                System.out.println("1. All Students");
                System.out.println("2. Honors Students Only");
                System.out.println("3. Students with Grade Changes");
                System.out.println();
                
                System.out.print("Select scope (1-3): ");
                int scopeChoice;
                try {
                    scopeChoice = ui.getScanner().nextInt();
                    ui.getScanner().nextLine();
                } catch (InputMismatchException e) {
                    ui.getScanner().nextLine();
                    throw new InvalidMenuChoiceException("Please enter a valid number (1-3).");
                }
                
                switch (scopeChoice) {
                    case 1: scope = "All Students"; break;
                    case 2: scope = "Honors Students Only"; break;
                    case 3: scope = "Students with Grade Changes"; break;
                    default: throw new InvalidMenuChoiceException("Invalid scope. Please select 1-3.");
                }
                
                System.out.println();
                System.out.print("Enter number of threads (1-8): ");
                try {
                    threadCount = Integer.parseInt(ui.getScanner().nextLine().trim());
                    if (threadCount < 1 || threadCount > 8) {
                        throw new NumberFormatException("Thread count must be between 1 and 8");
                    }
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Invalid thread count: " + e.getMessage());
                }
                System.out.println();
            }
            
            // Create task with default hour and minute
            int hour = 0;  // Default midnight
            int minute = 0;
            
            ScheduledTask task = new ScheduledTask(taskName, scheduleType, hour, minute);
            task.setScope(scope);
            task.setThreadCount(threadCount);
            task.setLogToFile(true);
            task.setEmailNotification(true);
            
            // Schedule the task
            ScheduledTask scheduledTask = taskScheduler.scheduleTask(task);
            
            System.out.println("✓ TASK SCHEDULED SUCCESSFULLY!");
            System.out.println();
            System.out.println("Task Details:");
            System.out.println("  Task ID: " + scheduledTask.getTaskId());
            System.out.println("  Name: " + scheduledTask.getTaskName());
            System.out.println("  Schedule: " + scheduledTask.getScheduleDescription());
            System.out.println("  Status: " + (scheduledTask.isActive() ? "Active" : "Inactive"));
            System.out.println("  Next Run: " + scheduledTask.getFormattedNextRunTime());
            System.out.println("  Created: " + scheduledTask.getFormattedLastRunTime());
            System.out.println();
            
        } catch (InvalidMenuChoiceException | NumberFormatException e) {
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
        }
    }
    
    /**
     * View scheduled tasks
     */
    private void viewScheduledTasks(TaskScheduler taskScheduler) {
        System.out.println("SCHEDULED TASKS");
        System.out.println("_______________________________________________");
        System.out.println();
        
        List<ScheduledTask> tasks = taskScheduler.getActiveTasks();
        
        if (tasks.isEmpty()) {
            System.out.println("No tasks scheduled.");
            System.out.println();
            return;
        }
        
        System.out.printf("%-10s | %-25s | %-15s | %-10s%n", 
            "TASK ID", "NAME", "SCHEDULE", "STATUS");
        System.out.println("_______________________________________________");
        
        for (ScheduledTask task : tasks) {
            System.out.printf("%-10s | %-25s | %-15s | %-10s%n",
                task.getTaskId(),
                task.getTaskName().length() > 25 ? task.getTaskName().substring(0, 22) + "..." : task.getTaskName(),
                task.getScheduleType(),
                task.isActive() ? "Active" : "Inactive");
        }
        
        System.out.println("_______________________________________________");
        System.out.println();
        System.out.println("Total Tasks: " + tasks.size());
        
        // Count by status
        int active = 0, inactive = 0;
        for (ScheduledTask task : tasks) {
            if (task.isActive()) {
                active++;
            } else {
                inactive++;
            }
        }
        
        System.out.println("Active: " + active + " | Inactive: " + inactive);
        System.out.println();
    }
    
    /**
     * View task execution log
     */
    private void viewTaskExecutionLog(TaskScheduler taskScheduler) {
        System.out.println("TASK EXECUTION LOG");
        System.out.println("_______________________________________________");
        System.out.println();
        
        List<TaskScheduler.TaskExecutionLog> logs = taskScheduler.getExecutionLogs();
        
        if (logs.isEmpty()) {
            System.out.println("No execution logs available.");
            System.out.println();
            return;
        }
        
        // Display last 10 logs
        int displayCount = Math.min(10, logs.size());
        System.out.println("Showing last " + displayCount + " execution(s):");
        System.out.println();
        
        for (int i = logs.size() - displayCount; i < logs.size(); i++) {
            TaskScheduler.TaskExecutionLog log = logs.get(i);
            System.out.println("Task: " + log.getTaskName());
            System.out.println("  Time: " + log.getTimestamp());
            System.out.println("  Status: " + log.getStatus());
            System.out.println("  Duration: " + log.getDuration() + "ms");
            if (log.getErrorMessage() != null) {
                System.out.println("  Error: " + log.getErrorMessage());
            }
            System.out.println();
        }
        
        // Statistics
        int successCount = 0, failureCount = 0;
        long totalDuration = 0;
        for (TaskScheduler.TaskExecutionLog log : logs) {
            if (log.getStatus() == ScheduledTask.TaskStatus.SUCCESS) {
                successCount++;
            } else if (log.getStatus() == ScheduledTask.TaskStatus.FAILED) {
                failureCount++;
            }
            totalDuration += log.getDuration();
        }
        
        System.out.println("Summary:");
        System.out.println("  Total Executions: " + logs.size());
        System.out.println("  Successful: " + successCount);
        System.out.println("  Failed: " + failureCount);
        System.out.println("  Avg Duration: " + (logs.size() > 0 ? totalDuration / logs.size() : 0) + "ms");
        System.out.println();
    }
    
    /**
     * Cancel a scheduled task
     */
    private void cancelScheduledTask(TaskScheduler taskScheduler) {
        System.out.println("CANCEL SCHEDULED TASK");
        System.out.println("_______________________________________________");
        System.out.println();
        
        List<ScheduledTask> tasks = taskScheduler.getActiveTasks();
        
        if (tasks.isEmpty()) {
            System.out.println("No tasks to cancel.");
            System.out.println();
            return;
        }
        
        // Display active tasks
        List<ScheduledTask> activeTasks = new ArrayList<>();
        for (ScheduledTask task : tasks) {
            if (task.isActive()) {
                activeTasks.add(task);
            }
        }
        
        if (activeTasks.isEmpty()) {
            System.out.println("No active tasks to cancel.");
            System.out.println();
            return;
        }
        
        System.out.println("Active Tasks:");
        for (int i = 0; i < activeTasks.size(); i++) {
            ScheduledTask task = activeTasks.get(i);
            System.out.printf("%d. %s - %s schedule%n", 
                i + 1, task.getTaskName(), task.getScheduleType());
        }
        System.out.println();
        
        System.out.print("Select task to cancel (1-" + activeTasks.size() + "): ");
        int cancelChoice;
        try {
            cancelChoice = ui.getScanner().nextInt();
            ui.getScanner().nextLine();
        } catch (InputMismatchException e) {
            ui.getScanner().nextLine();
            System.out.println();
            System.out.println("Invalid input. Please enter a valid number.");
            System.out.println();
            return;
        }
        
        if (cancelChoice < 1 || cancelChoice > activeTasks.size()) {
            System.out.println();
            System.out.println("Invalid selection.");
            System.out.println();
            return;
        }
        
        ScheduledTask taskToCancel = activeTasks.get(cancelChoice - 1);
        boolean cancelled = taskScheduler.cancelTask(taskToCancel.getTaskId());
        
        System.out.println();
        if (cancelled) {
            System.out.println("✓ Task '" + taskToCancel.getTaskName() + "' cancelled successfully.");
        } else {
            System.out.println("X Failed to cancel task '" + taskToCancel.getTaskName() + "'.");
        }
        System.out.println();
    }
    
    private void handleSystemPerformance() {
        try {
            System.out.println("SYSTEM PERFORMANCE MONITOR");
            System.out.println("_______________________________________________");
            System.out.println();
            
            // Get performance monitor from ApplicationContext
            SystemPerformanceMonitor performanceMonitor = context.getPerformanceMonitor();
            if (performanceMonitor == null) {
                System.out.println("X Performance monitor not initialized.");
                System.out.println();
                return;
            }
            
            System.out.println("Performance Views:");
            System.out.println("1. Resource Utilization (CPU, Memory, Threads)");
            System.out.println("2. System Performance (Collections, Thread Pools, I/O)");
            System.out.println("3. Detailed Performance Metrics");
            System.out.println("4. Return to Main Menu");
            System.out.println();
            
            System.out.print("Select view (1-4): ");
            int viewChoice;
            try {
                viewChoice = ui.getScanner().nextInt();
                ui.getScanner().nextLine();
            } catch (InputMismatchException e) {
                ui.getScanner().nextLine();
                throw new InvalidMenuChoiceException("Please enter a valid number (1-4).");
            }
            
            System.out.println();
            
            switch (viewChoice) {
                case 1:
                    // Resource Utilization
                    performanceMonitor.displayResourceUtilization();
                    break;
                    
                case 2:
                    // System Performance
                    performanceMonitor.displaySystemPerformance();
                    break;
                    
                case 3:
                    // Detailed Performance Metrics
                    performanceMonitor.displayDetailedPerformance();
                    break;
                    
                case 4:
                    // Return to Main Menu
                    return;
                    
                default:
                    throw new InvalidMenuChoiceException("Invalid view choice. Please select 1-4.");
            }
            
        } catch (InvalidMenuChoiceException e) {
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
        } catch (Exception e) {
            System.out.println();
            System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
            System.out.println();
        }
    }
    
    private void handleCacheManagement() {
        try {
            System.out.println("CACHE MANAGEMENT");
            System.out.println("_______________________________________________");
            System.out.println();
            
            // Get cache manager from ApplicationContext
            CacheManager cacheManager = context.getCacheManager();
            if (cacheManager == null) {
                System.out.println("X Cache manager not initialized.");
                System.out.println();
                return;
            }
            
            // Display current cache statistics
            CacheManager.CacheStatistics stats = cacheManager.getStatistics();
            System.out.println("Current Cache Status:");
            System.out.printf("Entries: %d/150 (%.1f%% full)%n", 
                stats.getTotalEntries(), 
                (stats.getTotalEntries() * 100.0 / 150));
            System.out.printf("Hit Rate: %.1f%% | Miss Rate: %.1f%%%n", 
                stats.getHitRate(), 
                stats.getMissRate());
            System.out.println("Memory Usage: " + formatMemory(stats.getMemoryUsage()));
            System.out.println();
            
            System.out.println("Cache Management Options:");
            System.out.println("1. View Cache Statistics");
            System.out.println("2. View Cache Contents");
            System.out.println("3. Invalidate Cache by Type");
            System.out.println("4. Clear Entire Cache");
            System.out.println("5. Warm Cache");
            System.out.println("6. Return to Main Menu");
            System.out.println();
            
            System.out.print("Select option (1-6): ");
            int cacheOption;
            try {
                cacheOption = ui.getScanner().nextInt();
                ui.getScanner().nextLine();
            } catch (InputMismatchException e) {
                ui.getScanner().nextLine();
                throw new InvalidMenuChoiceException("Please enter a valid number (1-6).");
            }
            
            System.out.println();
            
            switch (cacheOption) {
                case 1:
                    // View Cache Statistics
                    displayCacheStatistics(cacheManager);
                    break;
                    
                case 2:
                    // View Cache Contents
                    displayCacheContents(cacheManager);
                    break;
                    
                case 3:
                    // Invalidate Cache by Type
                    invalidateCacheByType(cacheManager);
                    break;
                    
                case 4:
                    // Clear Entire Cache
                    clearEntireCache(cacheManager);
                    break;
                    
                case 5:
                    // Warm Cache
                    warmCache(cacheManager);
                    break;
                    
                case 6:
                    // Return to Main Menu
                    return;
                    
                default:
                    throw new InvalidMenuChoiceException("Invalid option. Please select 1-6.");
            }
            
        } catch (InvalidMenuChoiceException e) {
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
        } catch (Exception e) {
            System.out.println();
            System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
            System.out.println();
        }
    }
    
    /**
     * Display detailed cache statistics
     */
    private void displayCacheStatistics(CacheManager cacheManager) {
        System.out.println("CACHE STATISTICS");
        System.out.println("_______________________________________________");
        System.out.println();
        
        CacheManager.CacheStatistics stats = cacheManager.getStatistics();
        
        System.out.println("Cache Performance:");
        System.out.printf("Total Entries: %d/150 (%.1f%% capacity)%n", 
            stats.getTotalEntries(),
            (stats.getTotalEntries() * 100.0 / 150));
        System.out.println("Memory Usage: " + formatMemory(stats.getMemoryUsage()));
        System.out.println();
        
        System.out.println("Hit/Miss Statistics:");
        long totalRequests = stats.getTotalHits() + stats.getTotalMisses();
        System.out.printf("Total Requests: %d%n", totalRequests);
        System.out.printf("Cache Hits: %d (%.1f%%)%n", stats.getTotalHits(), stats.getHitRate());
        System.out.printf("Cache Misses: %d (%.1f%%)%n", stats.getTotalMisses(), stats.getMissRate());
        System.out.println();
        
        System.out.println("Performance Metrics:");
        System.out.printf("Avg Hit Time: %dms%n", stats.getAverageHitTime());
        System.out.printf("Avg Miss Time: %dms%n", stats.getAverageMissTime());
        System.out.printf("Evictions: %d (LRU policy)%n", stats.getEvictionCount());
        System.out.println();
        
        // Performance assessment
        System.out.println("Performance Assessment:");
        if (stats.getHitRate() >= 80) {
            System.out.println("✓ Excellent cache performance (>= 80% hit rate)");
        } else if (stats.getHitRate() >= 60) {
            System.out.println("✓ Good cache performance (60-79% hit rate)");
        } else if (stats.getHitRate() >= 40) {
            System.out.println("⚠ Moderate cache performance (40-59% hit rate)");
        } else if (totalRequests > 0) {
            System.out.println("⚠ Poor cache performance (< 40% hit rate)");
            System.out.println("  Consider warming cache or reviewing access patterns");
        } else {
            System.out.println("ℹ No cache access yet");
        }
        System.out.println();
    }
    
    /**
     * Display cache contents with metadata
     */
    private void displayCacheContents(CacheManager cacheManager) {
        System.out.println("CACHE CONTENTS");
        System.out.println("_______________________________________________");
        System.out.println();
        
        java.util.List<java.util.Map<String, Object>> contents = cacheManager.getCacheContents();
        
        if (contents.isEmpty()) {
            System.out.println("Cache is empty.");
            System.out.println();
            return;
        }
        
        System.out.printf("%-30s | %-15s | %-8s | %-19s | %-8s%n", 
            "KEY", "TYPE", "ACCESSED", "LAST ACCESS", "EXPIRED");
        System.out.println("_______________________________________________");
        
        int displayCount = 0;
        for (java.util.Map<String, Object> entry : contents) {
            if (displayCount >= 20) { // Limit to 20 entries
                System.out.println("... and " + (contents.size() - 20) + " more entries");
                break;
            }
            
            String key = (String) entry.get("key");
            String type = (String) entry.get("type");
            int accessCount = (Integer) entry.get("accessCount");
            java.time.LocalDateTime lastAccessed = (java.time.LocalDateTime) entry.get("lastAccessed");
            boolean isExpired = (Boolean) entry.get("isExpired");
            
            // Truncate key if too long
            String displayKey = key.length() > 30 ? key.substring(0, 27) + "..." : key;
            
            // Format last accessed time
            String lastAccessStr = lastAccessed.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            System.out.printf("%-30s | %-15s | %-8d | %-19s | %-8s%n",
                displayKey,
                type,
                accessCount,
                lastAccessStr,
                isExpired ? "Yes" : "No");
            
            displayCount++;
        }
        
        System.out.println("_______________________________________________");
        System.out.println();
        System.out.printf("Total Entries: %d%n", contents.size());
        
        // Count by type
        java.util.Map<String, Integer> typeCounts = new java.util.HashMap<>();
        for (java.util.Map<String, Object> entry : contents) {
            String type = (String) entry.get("type");
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }
        
        System.out.println("Distribution by Type:");
        for (java.util.Map.Entry<String, Integer> typeEntry : typeCounts.entrySet()) {
            System.out.printf("  %s: %d entries%n", typeEntry.getKey(), typeEntry.getValue());
        }
        System.out.println();
    }
    
    /**
     * Invalidate cache entries by type
     */
    private void invalidateCacheByType(CacheManager cacheManager) {
        System.out.println("INVALIDATE CACHE BY TYPE");
        System.out.println("_______________________________________________");
        System.out.println();
        
        System.out.println("Cache Types:");
        System.out.println("1. Student Cache");
        System.out.println("2. Grade Report Cache");
        System.out.println("3. Statistics Cache");
        System.out.println("4. Cancel");
        System.out.println();
        
        System.out.print("Select type to invalidate (1-4): ");
        int typeChoice;
        try {
            typeChoice = ui.getScanner().nextInt();
            ui.getScanner().nextLine();
        } catch (InputMismatchException e) {
            ui.getScanner().nextLine();
            System.out.println();
            System.out.println("Invalid input. Please enter a valid number.");
            System.out.println();
            return;
        }
        
        System.out.println();
        
        if (typeChoice == 4) {
            System.out.println("Operation cancelled.");
            System.out.println();
            return;
        }
        
        CacheManager.CacheType cacheType;
        String typeName;
        switch (typeChoice) {
            case 1:
                cacheType = CacheManager.CacheType.STUDENT;
                typeName = "Student";
                break;
            case 2:
                cacheType = CacheManager.CacheType.GRADE_REPORT;
                typeName = "Grade Report";
                break;
            case 3:
                cacheType = CacheManager.CacheType.STATISTICS;
                typeName = "Statistics";
                break;
            default:
                System.out.println("Invalid cache type selection.");
                System.out.println();
                return;
        }
        
        // Confirm invalidation
        System.out.print("Confirm invalidation of " + typeName + " cache? (Y/N): ");
        String confirm = ui.getScanner().nextLine().trim().toUpperCase();
        System.out.println();
        
        if (!confirm.equals("Y")) {
            System.out.println("Operation cancelled.");
            System.out.println();
            return;
        }
        
        cacheManager.invalidateByType(cacheType);
        System.out.println("✓ " + typeName + " cache invalidated successfully.");
        System.out.println();
        
        // Display updated statistics
        CacheManager.CacheStatistics stats = cacheManager.getStatistics();
        System.out.printf("Cache now contains %d entries.%n", stats.getTotalEntries());
        System.out.println();
    }
    
    /**
     * Clear entire cache
     */
    private void clearEntireCache(CacheManager cacheManager) {
        System.out.println("CLEAR ENTIRE CACHE");
        System.out.println("_______________________________________________");
        System.out.println();
        
        CacheManager.CacheStatistics statsBefore = cacheManager.getStatistics();
        
        System.out.println("⚠ WARNING: This will clear ALL cached data!");
        System.out.printf("Current cache contains %d entries.%n", statsBefore.getTotalEntries());
        System.out.println();
        
        System.out.print("Are you sure you want to clear the entire cache? (Y/N): ");
        String confirm = ui.getScanner().nextLine().trim().toUpperCase();
        System.out.println();
        
        if (!confirm.equals("Y")) {
            System.out.println("Operation cancelled.");
            System.out.println();
            return;
        }
        
        cacheManager.clear();
        System.out.println("✓ Cache cleared successfully.");
        System.out.println();
        
        // Display results
        CacheManager.CacheStatistics statsAfter = cacheManager.getStatistics();
        System.out.printf("Entries removed: %d%n", statsBefore.getTotalEntries());
        System.out.printf("Current entries: %d%n", statsAfter.getTotalEntries());
        System.out.println("Memory freed: " + formatMemory(statsBefore.getMemoryUsage()));
        System.out.println();
        
        System.out.println("Note: Cache will be automatically repopulated on next access.");
        System.out.println();
    }
    
    /**
     * Warm cache with frequently accessed data
     */
    private void warmCache(CacheManager cacheManager) {
        System.out.println("WARM CACHE");
        System.out.println("_______________________________________________");
        System.out.println();
        
        CacheManager.CacheStatistics statsBefore = cacheManager.getStatistics();
        System.out.printf("Current cache entries: %d%n", statsBefore.getTotalEntries());
        System.out.println();
        
        System.out.println("Warming cache with frequently accessed data...");
        System.out.println();
        
        long startTime = System.currentTimeMillis();
        cacheManager.warmCache(studentManager, gradeManager);
        long duration = System.currentTimeMillis() - startTime;
        
        CacheManager.CacheStatistics statsAfter = cacheManager.getStatistics();
        
        System.out.println("✓ Cache warming completed!");
        System.out.println();
        
        System.out.println("Results:");
        System.out.printf("Entries before: %d%n", statsBefore.getTotalEntries());
        System.out.printf("Entries after: %d%n", statsAfter.getTotalEntries());
        System.out.printf("New entries: %d%n", statsAfter.getTotalEntries() - statsBefore.getTotalEntries());
        System.out.printf("Time taken: %dms%n", duration);
        System.out.println();
        
        System.out.println("Cached Data:");
        System.out.println("✓ All student records");
        System.out.println("✓ Class statistics");
        System.out.println();
    }
    
    /**
     * Format memory size for display
     */
    private String formatMemory(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    private void handleAuditTrail() {
        try {
            System.out.println("AUDIT TRAIL VIEWER");
            System.out.println("_______________________________________________");
            System.out.println();
            
            // Get audit logger from ApplicationContext
            AuditLogger auditLogger = context.getAuditLogger();
            if (auditLogger == null) {
                System.out.println("X Audit logger not initialized.");
                System.out.println();
                return;
            }
            
            // Create audit trail viewer
            AuditTrailViewer viewer = new AuditTrailViewer(auditLogger);
            
            System.out.println("Audit Trail Options:");
            System.out.println("1. View Recent Audit Entries");
            System.out.println("2. Filter by Operation Type");
            System.out.println("3. Filter by Date Range");
            System.out.println("4. Search by Keyword");
            System.out.println("5. Export Audit Log");
            System.out.println("6. Return to Main Menu");
            System.out.println();
            
            System.out.print("Select option (1-6): ");
            int auditOption;
            try {
                auditOption = ui.getScanner().nextInt();
                ui.getScanner().nextLine();
            } catch (InputMismatchException e) {
                ui.getScanner().nextLine();
                throw new InvalidMenuChoiceException("Please enter a valid number (1-6).");
            }
            
            System.out.println();
            
            switch (auditOption) {
                case 1:
                    // View Recent Audit Entries
                    viewRecentAuditEntries(viewer);
                    break;
                    
                case 2:
                    // Filter by Operation Type
                    filterByOperationType(viewer);
                    break;
                    
                case 3:
                    // Filter by Date Range
                    filterByDateRange(viewer);
                    break;
                    
                case 4:
                    // Search by Keyword
                    searchByKeyword(viewer);
                    break;
                    
                case 5:
                    // Export Audit Log
                    exportAuditLog(viewer);
                    break;
                    
                case 6:
                    // Return to Main Menu
                    return;
                    
                default:
                    throw new InvalidMenuChoiceException("Invalid option. Please select 1-6.");
            }
            
        } catch (InvalidMenuChoiceException e) {
            System.out.println();
            System.out.println(e.getMessage());
            System.out.println();
        } catch (Exception e) {
            System.out.println();
            System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
            System.out.println();
        }
    }
    
    /**
     * View recent audit entries with pagination
     */
    private void viewRecentAuditEntries(AuditTrailViewer viewer) {
        System.out.println("VIEW RECENT AUDIT ENTRIES");
        System.out.println("_______________________________________________");
        System.out.println();
        
        System.out.print("Number of entries to display (max 100): ");
        int count;
        try {
            count = ui.getScanner().nextInt();
            ui.getScanner().nextLine();
            
            if (count < 1 || count > 100) {
                System.out.println();
                System.out.println("Please enter a number between 1 and 100.");
                System.out.println();
                return;
            }
        } catch (InputMismatchException e) {
            ui.getScanner().nextLine();
            System.out.println();
            System.out.println("Invalid input. Please enter a valid number.");
            System.out.println();
            return;
        }
        
        System.out.println();
        System.out.println("Loading audit entries...");
        
        // Flush pending audit entries before reading
        AuditLogger auditLogger = context.getAuditLogger();
        if (auditLogger != null) {
            auditLogger.flush();
        }
        
        System.out.println();
        
        java.util.List<AuditLogger.EnhancedAuditEntry> entries = viewer.getRecentEntries(count);
        
        if (entries.isEmpty()) {
            System.out.println("No audit entries found.");
            System.out.println();
            return;
        }
        
        displayAuditEntries(entries, viewer);
    }
    
    /**
     * Filter audit entries by operation type
     */
    private void filterByOperationType(AuditTrailViewer viewer) {
        System.out.println("FILTER BY OPERATION TYPE");
        System.out.println("_______________________________________________");
        System.out.println();
        
        // Get recent entries first
        java.util.List<AuditLogger.EnhancedAuditEntry> allEntries = viewer.getRecentEntries(1000);
        
        if (allEntries.isEmpty()) {
            System.out.println("No audit entries found.");
            System.out.println();
            return;
        }
        
        // Get unique operation types
        Set<String> operationTypes = viewer.getOperationTypes(allEntries);
        
        System.out.println("Available Operation Types:");
        java.util.List<String> typesList = new java.util.ArrayList<>(operationTypes);
        java.util.Collections.sort(typesList);
        
        for (int i = 0; i < typesList.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, typesList.get(i));
        }
        System.out.println();
        
        System.out.print("Select operation type (1-" + typesList.size() + "): ");
        int typeChoice;
        try {
            typeChoice = ui.getScanner().nextInt();
            ui.getScanner().nextLine();
            
            if (typeChoice < 1 || typeChoice > typesList.size()) {
                System.out.println();
                System.out.println("Invalid selection.");
                System.out.println();
                return;
            }
        } catch (InputMismatchException e) {
            ui.getScanner().nextLine();
            System.out.println();
            System.out.println("Invalid input. Please enter a valid number.");
            System.out.println();
            return;
        }
        
        System.out.println();
        
        String selectedType = typesList.get(typeChoice - 1);
        java.util.List<AuditLogger.EnhancedAuditEntry> filteredEntries = 
            viewer.filterByOperationType(allEntries, selectedType);
        
        System.out.println("Filtered by: " + selectedType);
        System.out.println();
        
        displayAuditEntries(filteredEntries, viewer);
    }
    
    /**
     * Filter audit entries by date range
     */
    private void filterByDateRange(AuditTrailViewer viewer) {
        System.out.println("FILTER BY DATE RANGE");
        System.out.println("_______________________________________________");
        System.out.println();
        
        System.out.println("Enter date range (format: yyyy-MM-dd)");
        System.out.println();
        
        System.out.print("Start date: ");
        String startDateStr = ui.getScanner().nextLine().trim();
        
        System.out.print("End date: ");
        String endDateStr = ui.getScanner().nextLine().trim();
        System.out.println();
        
        try {
            java.time.LocalDateTime startDate = java.time.LocalDate.parse(
                startDateStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            java.time.LocalDateTime endDate = java.time.LocalDate.parse(
                endDateStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE).atTime(23, 59, 59);
            
            if (startDate.isAfter(endDate)) {
                System.out.println("Start date must be before end date.");
                System.out.println();
                return;
            }
            
            System.out.println("Loading audit entries...");
            System.out.println();
            
            // Get all entries in date range
            AuditLogger auditLogger = context.getAuditLogger();
            java.util.List<AuditLogger.EnhancedAuditEntry> entries = 
                auditLogger.readAuditEntries(startDate, endDate);
            
            if (entries.isEmpty()) {
                System.out.println("No audit entries found in the specified date range.");
                System.out.println();
                return;
            }
            
            // Sort by timestamp descending
            entries.sort((a, b) -> {
                java.time.LocalDateTime timeA = java.time.LocalDateTime.parse(
                    a.getTimestamp(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                java.time.LocalDateTime timeB = java.time.LocalDateTime.parse(
                    b.getTimestamp(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return timeB.compareTo(timeA);
            });
            
            System.out.printf("Date Range: %s to %s%n", startDateStr, endDateStr);
            System.out.println();
            
            displayAuditEntries(entries, viewer);
            
        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("Invalid date format. Please use yyyy-MM-dd (e.g., 2025-12-17)");
            System.out.println();
        }
    }
    
    /**
     * Search audit entries by keyword
     */
    private void searchByKeyword(AuditTrailViewer viewer) {
        System.out.println("SEARCH BY KEYWORD");
        System.out.println("_______________________________________________");
        System.out.println();
        
        System.out.print("Enter search keyword: ");
        String keyword = ui.getScanner().nextLine().trim();
        System.out.println();
        
        if (keyword.isEmpty()) {
            System.out.println("Keyword cannot be empty.");
            System.out.println();
            return;
        }
        
        System.out.println("Searching audit entries...");
        System.out.println();
        
        // Get recent entries
        java.util.List<AuditLogger.EnhancedAuditEntry> allEntries = viewer.getRecentEntries(1000);
        
        if (allEntries.isEmpty()) {
            System.out.println("No audit entries found.");
            System.out.println();
            return;
        }
        
        // Filter by keyword (case-insensitive search in user action and details)
        java.util.List<AuditLogger.EnhancedAuditEntry> matchingEntries = allEntries.stream()
            .filter(entry -> {
                String lowerKeyword = keyword.toLowerCase();
                boolean matchesUserAction = entry.getUserAction() != null && 
                    entry.getUserAction().toLowerCase().contains(lowerKeyword);
                boolean matchesDetails = entry.getDetails() != null && 
                    entry.getDetails().toLowerCase().contains(lowerKeyword);
                boolean matchesOperation = entry.getOperationType() != null && 
                    entry.getOperationType().toLowerCase().contains(lowerKeyword);
                return matchesUserAction || matchesDetails || matchesOperation;
            })
            .collect(java.util.stream.Collectors.toList());
        
        if (matchingEntries.isEmpty()) {
            System.out.printf("No audit entries found matching keyword: \"%s\"%n", keyword);
            System.out.println();
            return;
        }
        
        System.out.printf("Search Results for: \"%s\"%n", keyword);
        System.out.println();
        
        displayAuditEntries(matchingEntries, viewer);
    }
    
    /**
     * Export audit log to file
     */
    private void exportAuditLog(AuditTrailViewer viewer) {
        System.out.println("EXPORT AUDIT LOG");
        System.out.println("_______________________________________________");
        System.out.println();
        
        System.out.print("Enter filename (without extension): ");
        String fileName = ui.getScanner().nextLine().trim();
        System.out.println();
        
        if (fileName.isEmpty()) {
            System.out.println("Filename cannot be empty.");
            System.out.println();
            return;
        }
        
        System.out.println("Loading audit entries...");
        
        // Get recent entries
        java.util.List<AuditLogger.EnhancedAuditEntry> entries = viewer.getRecentEntries(10000);
        
        if (entries.isEmpty()) {
            System.out.println("No audit entries to export.");
            System.out.println();
            return;
        }
        
        // Export to CSV format
        String exportPath = "./reports/" + fileName + ".csv";
        
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(exportPath);
            java.nio.file.Files.createDirectories(filePath.getParent());
            
            try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(filePath)) {
                // Write CSV header
                writer.write("Timestamp,Thread ID,Operation Type,User Action,Execution Time (ms),Status,Error Message,Details");
                writer.newLine();
                
                // Write entries
                for (AuditLogger.EnhancedAuditEntry entry : entries) {
                    writer.write(String.format("\"%s\",%d,\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\"",
                        entry.getTimestamp(),
                        entry.getThreadId(),
                        entry.getOperationType(),
                        entry.getUserAction(),
                        entry.getExecutionTime(),
                        entry.isSuccess() ? "SUCCESS" : "FAILED",
                        entry.getErrorMessage() != null ? entry.getErrorMessage().replace("\"", "\"\"") : "",
                        entry.getDetails() != null ? entry.getDetails().replace("\"", "\"\"") : ""));
                    writer.newLine();
                }
            }
            
            long fileSize = java.nio.file.Files.size(filePath);
            
            System.out.println();
            System.out.println("✓ Audit log exported successfully!");
            System.out.println();
            System.out.println("Export Details:");
            System.out.printf("File: %s%n", exportPath);
            System.out.printf("Entries: %d%n", entries.size());
            System.out.printf("File Size: %s%n", formatFileSize(fileSize));
            System.out.println();
            
        } catch (java.io.IOException e) {
            System.out.println();
            System.out.println("X ERROR: Failed to export audit log: " + e.getMessage());
            System.out.println();
        }
    }
    
    /**
     * Display audit entries with statistics
     */
    private void displayAuditEntries(java.util.List<AuditLogger.EnhancedAuditEntry> entries, 
                                    AuditTrailViewer viewer) {
        // Display entries (limit to 50 for readability)
        int displayLimit = Math.min(entries.size(), 50);
        
        System.out.printf("%-19s | %-8s | %-20s | %-30s | %-8s | %-8s%n",
            "TIMESTAMP", "THREAD", "OPERATION", "USER ACTION", "TIME(ms)", "STATUS");
        System.out.println("_______________________________________________");
        
        for (int i = 0; i < displayLimit; i++) {
            AuditLogger.EnhancedAuditEntry entry = entries.get(i);
            
            String timestamp = entry.getTimestamp().substring(0, Math.min(19, entry.getTimestamp().length()));
            String operation = entry.getOperationType();
            if (operation.length() > 20) operation = operation.substring(0, 17) + "...";
            
            String userAction = entry.getUserAction();
            if (userAction.length() > 30) userAction = userAction.substring(0, 27) + "...";
            
            String status = entry.isSuccess() ? "SUCCESS" : "FAILED";
            
            System.out.printf("%-19s | %-8d | %-20s | %-30s | %-8d | %-8s%n",
                timestamp,
                entry.getThreadId(),
                operation,
                userAction,
                entry.getExecutionTime(),
                status);
        }
        
        if (entries.size() > displayLimit) {
            System.out.println("... and " + (entries.size() - displayLimit) + " more entries");
        }
        
        System.out.println("_______________________________________________");
        System.out.println();
        
        // Calculate and display statistics
        AuditTrailViewer.AuditStatistics stats = viewer.calculateStatistics(entries);
        
        System.out.println("Audit Statistics:");
        System.out.printf("Total Entries: %d%n", stats.getTotalOperations());
        System.out.printf("Successful: %d (%.1f%%)%n", 
            stats.getSuccessfulOperations(), 
            stats.getSuccessRate());
        System.out.printf("Failed: %d (%.1f%%)%n", 
            stats.getFailedOperations(), 
            (100.0 - stats.getSuccessRate()));
        System.out.println();
        
        System.out.println("Performance Metrics:");
        System.out.printf("Avg Execution Time: %.2fms%n", stats.getAvgExecutionTime());
        System.out.printf("Min Execution Time: %dms%n", stats.getMinExecutionTime());
        System.out.printf("Max Execution Time: %dms%n", stats.getMaxExecutionTime());
        System.out.println();
    }
}
