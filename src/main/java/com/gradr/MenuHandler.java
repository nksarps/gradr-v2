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
        CSVParser parser = new CSVParser(csvFilePath);
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
