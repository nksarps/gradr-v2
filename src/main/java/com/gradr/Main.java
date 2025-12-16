package com.gradr;

import com.gradr.exceptions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    public static void main(String[] args) throws StudentNotFoundException {
        Scanner scanner = new Scanner(System.in);

        StudentManager studentManager = new StudentManager();
        GradeManager gradeManager = new GradeManager();
        
        // Audit logger (initialized on startup)
        AuditLogger auditLogger = new AuditLogger();
        
        // Cache manager (initialized on startup)
        CacheManager cacheManager = new CacheManager();
        
        // Cache warming on startup
        cacheManager.warmCache(studentManager, gradeManager);
        
        // Pattern search service (for performance monitoring)
        PatternSearchService patternSearchService = new PatternSearchService(studentManager);
        
        // System performance monitor (initialized on startup)
        SystemPerformanceMonitor performanceMonitor = new SystemPerformanceMonitor(
            studentManager, gradeManager, cacheManager, patternSearchService);
        
        // Task scheduler (initialized on first use)
        // Use array to allow modification in shutdown hook
        final TaskScheduler[] taskSchedulerRef = new TaskScheduler[1];
        boolean schedulerInitialized = false;
        
        // Statistics dashboard (initialized on first use)
        // Use array to allow tracking across menu displays
        final StatisticsDashboard[] dashboardRef = new StatisticsDashboard[1];
        
        // Add shutdown hook to properly close scheduler, cache, and audit logger
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (taskSchedulerRef[0] != null) {
                taskSchedulerRef[0].shutdown();
            }
            cacheManager.shutdown();
            auditLogger.shutdown();
        }));

        int choice = 0;
        Student student = null;
        Subject subject;
        Grade grade;

        String studentId;

        do {
            displayMainMenu(taskSchedulerRef[0], dashboardRef[0]);

            // Checking if the user entered a valid number between 1 and 19
            try {
                System.out.print("Enter choice: ");
                choice = scanner.nextInt();
                scanner.nextLine();
                System.out.println();
            } catch (InputMismatchException e) {
                System.out.println(
                        "\n\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-19).\n"
                );
                System.out.println();
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    try{
                        System.out.println("ADD STUDENT (with validation)");
                        System.out.println("_______________________________________________");
                        System.out.println();

                        System.out.print("Enter student name: ");
                        String name = scanner.nextLine();
                        // Validate name using compiled regex pattern
                        ValidationUtils.validateName(name);
                        System.out.println("Valid Student Name");

                        System.out.print("Enter student age: ");
                        String ageInput = scanner.nextLine();
                        
                        // Validate age format and range
                        int age;
                        try {
                            age = Integer.parseInt(ageInput);
                            // Age should be between 5 and 100 (reasonable student age range)
                            if (age < 5 || age > 100) {
                                throw new InvalidStudentDataException(
                                        "X ERROR: InvalidStudentDataException\n   Age must be between 5 and 100.\n   You entered: " + ageInput
                                );
                            }
                        } catch (NumberFormatException e) { // if the user entered a non-number age
                            throw new InvalidStudentDataException(
                                    "X VALIDATION ERROR: Invalid age format\n   Age must be a valid number.\n   You entered: " + ageInput
                            );
                        }

                        System.out.print("Enter student email: ");
                        String email = scanner.nextLine();
                        // Validate email using compiled regex pattern
                        ValidationUtils.validateEmail(email);
                        System.out.println("Valid Email Address");

                        System.out.print("Enter student phone: ");
                        String phone = scanner.nextLine();
                        // Validate phone using compiled regex patterns
                        ValidationUtils.validatePhone(phone);
                        System.out.println("Valid Phone Number");
                        System.out.println();

                        System.out.println("Student type:");
                        System.out.println("1. Regular Student (Passing grade: 50%)");
                        System.out.println("2. Honors Student: (Passing grade: 60%, honors recognition)");
                        System.out.println();

                        System.out.print("Select type (1-2): ");
                        int studentType = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println();

                        if (studentType == 1) {
                            student = new RegularStudent(name, age, email, phone);
                        } else if (studentType == 2) {
                            student = new HonorsStudent(name, age, email, phone);
                        } else {
                            throw new InvalidMenuChoiceException(
                                    "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-2).\n   You entered: " + studentType
                            );
                        }

                        // Adding a student to the array
                        studentManager.addStudent(student);

                        // Setting grade manager after creating student to be able to access the student's grades
                        //  inside the student class
                        student.setGradeManager(gradeManager);

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
                    } catch (InputMismatchException e) {
                        System.out.println("X ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-2).\n");
                        scanner.nextLine(); // Clear invalid input
                        System.out.println();
                    } catch (InvalidMenuChoiceException e) {
                        System.out.println();
                        System.out.println(e.getMessage());
                        System.out.println();
                    } catch (InvalidStudentDataException e) {
                        System.out.println();
                        System.out.println(e.getMessage());
                        System.out.println();
                    }

                    break;
                case 2:
                    // For checking the number of student types displayed when the students in the system
                    // are more than 5
                    int studentDisplayCount = 0;
                    int regularStudentsDisplayCount = 0;
                    int honorStudentsDisplayCount = 0;

                    // getting the number of students added to the system
                    int studentCount = studentManager.getStudentCount();

                    // Displaying student details
                    // If there are no students added yet
                    if (studentCount == 0) {
                        System.out.println("No students found\n");
                    } else {
                        System.out.println("STUDENT LISTING");
                        System.out.println("----------------------------------------------------------------------------------------------------");
                        System.out.println("STU ID   | NAME                    | TYPE               | AVG GRADE         | STATUS                ");
                        System.out.println("----------------------------------------------------------------------------------------------------");

                        // Use custom Comparator to sort students (O(n log n) operation)
                        List<Student> sortedStudents = studentManager.getStudentsList();
                        Collections.sort(sortedStudents, new StudentComparator());
                        
                        if (studentCount <= 5) {
                            // Display all students (sorted by Comparator)
                            for (Student s : sortedStudents) {
                                s.displayStudentDetails();
                                System.out.println("----------------------------------------------------------------------------------------------------");
                            }
                        } else {
                            // Because I have to display 5 students if students are more than 5. 3 Regular students and
                            // 2 honors students (using sorted list from Comparator)
                            for (Student s : sortedStudents) {
                                if (studentDisplayCount >= 5) break;
                                
                                // Check student type to make sure it displays five students
                                // 3 Regular and 2 Honors
                                if (s.getStudentType().equals("Regular") && regularStudentsDisplayCount < 3) {
                                    s.displayStudentDetails();
                                    System.out.println("----------------------------------------------------------------------------------------------------");
                                    regularStudentsDisplayCount++;
                                    studentDisplayCount++;
                                } else if (s.getStudentType().equals("Honors") && honorStudentsDisplayCount < 2) {
                                    s.displayStudentDetails();
                                    System.out.println("----------------------------------------------------------------------------------------------------");
                                    honorStudentsDisplayCount++;
                                    studentDisplayCount++;
                                }
                            }
                        }
                        System.out.println();
                        System.out.printf("Total Students: %d\n", studentCount);
                        System.out.printf("Average Class Grade: %.2f%%\n", studentManager.calculateClassAverage());
                        System.out.println();
                    }

                    break;
                case 3:
                    System.out.println("RECORD GRADE");
                    System.out.println("_______________________________________________");
                    System.out.println();

                    System.out.print("Enter Student ID: ");
                    studentId = scanner.nextLine();
                    System.out.println();

                    try {
                        student = studentManager.findStudent(studentId);

                        System.out.println("Student Details:");
                        System.out.printf("Name: %s\n", student.getName());
                        System.out.printf("Type: %s Student\n", student.getStudentType());
                        System.out.printf("Current Average: %.1f%%\n", student.calculateAverageGrade());
                        System.out.println();

                        System.out.println("Subject type:");
                        System.out.println("1. Core Subject (Mathematics, English, Science)");
                        System.out.println("2. Elective Subject (Music, Art, Physical Education)\n");

                        System.out.print("Select type (1-2): ");
                        int subjectTypeChoice = 0;

                        try {
                            subjectTypeChoice = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-2).\n");
                            scanner.nextLine(); // Clear invalid input
                            break;
                        }

                        String subjectType;
                        if (subjectTypeChoice == 1) {
                            subject = new CoreSubject();
                        } else if (subjectTypeChoice == 2) {
                            subject = new ElectiveSubject();
                        } else {
                            throw new InvalidMenuChoiceException(
                                    "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-2).\n   You entered: " + subjectTypeChoice
                            );
                        }

                        System.out.println();

                        subjectType = subject.getSubjectType();
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
                        int subjectChoice = 0;

                        try {
                            subjectChoice = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println(
                                    "\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-3).\n"
                            );
                            scanner.nextLine();
                            break;
                        }

                        // Checking if the user entered a valid number between 1 and 3 for the subject choice
                        if (subjectChoice < 1 || subjectChoice > 3) {
                            throw new InvalidMenuChoiceException(
                                    "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-3).\n   You entered: " + subjectChoice
                            );
                        }

                        if (subjectTypeChoice == 1) {
                            if (subjectChoice == 1) {
                                subject.setSubjectName("Mathematics");
                            } else if (subjectChoice == 2) {
                                subject.setSubjectName("English");
                            } else {
                                subject.setSubjectName("Science");
                            }
                        } else {
                            if (subjectChoice == 1) {
                                subject.setSubjectName("Music");
                            } else if (subjectChoice == 2) {
                                subject.setSubjectName("Art");
                            } else {
                                subject.setSubjectName("Physical Education");
                            }
                        }

                        System.out.println();

                        System.out.print("Enter grade (0-100): ");
                        String gradeInputStr = scanner.nextLine();
                        
                        int gradeInput;
                        try {
                            // Validate grade format using compiled regex pattern
                            ValidationUtils.validateGrade(gradeInputStr);
                            
                            gradeInput = Integer.parseInt(gradeInputStr);
                            // Additional validation for range
                            ValidationUtils.validateGrade(gradeInput);
                        } catch (InvalidStudentDataException e) {
                            System.out.println();
                            System.out.println(e.getMessage());
                            System.out.println();
                            break;
                        }

                        grade = new Grade(studentId, subject, gradeInput);

                        // Checking if the grade is between 0 and 100
                        if (grade.recordGrade(gradeInput)) {
                            grade.setGradeId();

                            System.out.println("GRADE CONFIRMATION");
                            System.out.println("_______________________________________________________");
                            System.out.printf("Grade ID: %s\n", grade.getGradeId());
                            System.out.printf("Student: %s - %s\n", studentId, student.getName());
                            System.out.printf("Subject: %s (%s)\n", subject.getSubjectName(), subject.getSubjectType());
                            System.out.printf("Grade: %.1f%%\n", (double) gradeInput);
                            System.out.printf("Date: %s\n", grade.getDate());
                            System.out.println("______________________________________________________\n");

                            System.out.print("Confirm grade? (Y/N): ");
                            char confirmGrade = scanner.next().charAt(0);
                            scanner.nextLine();

                            if (confirmGrade == 'Y' || confirmGrade == 'y') {
                                gradeManager.addGrade(grade);
                                
                                // Update GPA rankings using TreeMap (O(log n) operation)
                                GPACalculator gpaCalc = new GPACalculator(gradeManager);
                                double gpa = gpaCalc.calculateCumulativeGPA(studentId);
                                gradeManager.updateGPARanking(student, gpa);
                                
                                // Schedule task for grade processing (PriorityQueue - O(log n))
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
                        } else {
                            // If the grade is not between 0 and 100, throw an error
                            throw new InvalidGradeException(
                                    "X ERROR: InvalidGradeException\n   Grade must be between 0 and 100.\n   You entered: " + gradeInput + "\n"
                            );
                        }

                    } catch (StudentNotFoundException | InvalidMenuChoiceException | InvalidGradeException e) {
                        System.out.println();
                        System.out.println(e.getMessage());
                        System.out.println();
                    }

                    break;
                case 4:
                    System.out.println("VIEW GRADE REPORT");
                    System.out.println("_______________________________________________");
                    System.out.println();

                    System.out.print("Enter Student ID: ");
                    studentId = scanner.nextLine();
                    System.out.println();

                    // Get student using ID and display student grade report

                    try {
                        student = studentManager.findStudent(studentId);

                        //If there is a student associated with the ID, continue, else
                        // throw an error 
                        if (student != null) {
                            // Checking if student has grades recorded
                            boolean hasGrades = false;

                            for (Grade studentGrade : gradeManager.getGrades()) {
                                // Using the condition, studentGrade != null, so it doesn't throw an error when
                                // the student has no grades recorded for the display of student grade report
                                if (studentGrade != null && studentGrade.getStudentId().equals(studentId)) {
                                    hasGrades = true;
                                    break; 
                                }
                            }

                            // Printing out different student details for when student has grades recorded and
                            // when the student does not
                            System.out.printf("Student: %s - %s\n", student.getStudentId(), student.getName());
                            System.out.printf("Type: %s Student\n", student.getStudentType());

                            if (hasGrades) {
                                System.out.printf("Current Average: %.1f%%\n", gradeManager.calculateOverallAverage(studentId));

                                // Check if student is passing (average grade is greater than passing grade)
                                boolean isPassing = student.isPassing(gradeManager.calculateOverallAverage(studentId));

                                if (isPassing) {
                                    System.out.print("Status: PASSING\n");
                                } else {
                                    System.out.print("Status: FAILING\n");
                                }

                                System.out.println();

                                //gradeManager.viewGradesByStudent(studentId);
                                System.out.println(gradeManager.viewGradesByStudent(studentId));

                                // Displaying the student's performance summary
                                System.out.println("Performance Summary:");
                                if (isPassing) {
                                    System.out.println("Passing all core subjects");
                                    System.out.printf("Meeting passing grade requirement (%.0f%%)\n", student.getPassingGrade());
                                    System.out.println();
                                } else {
                                    System.out.println("Failing some subjects");
                                    System.out.printf("Failing to meet passing grade requirement (%.0f%%)\n", student.getPassingGrade());
                                    System.out.println();
                                }
                            } else {
                                // For when student has no grades recorded
                                System.out.printf("Passing Grade: %.0f%%\n", student.getPassingGrade());

                                //gradeManager.viewGradesByStudent(studentId);
                                System.out.println(gradeManager.viewGradesByStudent(studentId));
                            }

                        }
                    } catch (StudentNotFoundException e) {
                        System.out.println();
                        System.out.println(e.getMessage());
                        System.out.println();
                    }

                    break;
                case 5:
                    System.out.println("EXPORT GRADE REPORT (Multi-Format)");
                    System.out.println("_______________________________________________");
                    System.out.println();

                    System.out.print("Enter Student ID: ");
                    studentId = scanner.nextLine();
                    System.out.println();

                    try {
                        student = studentManager.findStudent(studentId);

                        System.out.printf("Student: %s - %s", student.getStudentId(), student.getName());
                        if (student.getEmail() != null) {
                            System.out.printf(" (%s)", student.getEmail());
                        }
                        System.out.println();
                        System.out.printf("Type: %s Student", student.getStudentType());
                        if (student.getPhone() != null) {
                            System.out.printf(" | Phone: %s", student.getPhone());
                        }
                        System.out.println();
                        System.out.printf("Total Grades: %d\n", student.getEnrolledSubjectsCount());
                        System.out.println();

                        System.out.println("Export Format:");
                        System.out.println("1. CSV (Comma-Separated Values)");
                        System.out.println("2. JSON (JavaScript Object Notation)");
                        System.out.println("3. Binary (Serialized Java Object)");
                        System.out.println("4. All formats");
                        System.out.println();

                        System.out.print("Select format (1-4): ");
                        int formatChoice;
                        try {
                            formatChoice = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-4).\n");
                            scanner.nextLine();
                            break;
                        }

                        System.out.println();
                        System.out.println("Report Type:");
                        System.out.println("1. Summary Report");
                        System.out.println("2. Detailed Report");
                        System.out.println("3. Transcript Format");
                        System.out.println("4. Performance Analytics");
                        System.out.println();

                        System.out.print("Select type (1-4): ");
                        int reportTypeChoice;
                        try {
                            reportTypeChoice = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-4).\n");
                            scanner.nextLine();
                            break;
                        }

                        String reportType;
                        switch (reportTypeChoice) {
                            case 1: reportType = "Summary Report"; break;
                            case 2: reportType = "Detailed Report"; break;
                            case 3: reportType = "Transcript Format"; break;
                            case 4: reportType = "Performance Analytics"; break;
                            default: reportType = "Detailed Report"; break;
                        }

                        System.out.print("Enter filename (without extension): ");
                        String fileName = scanner.nextLine();
                        System.out.println();

                        try {
                            // Build StudentReport
                            double overallAverage = student.calculateAverageGrade();
                            StudentReport report = new StudentReport(
                                student.getStudentId(),
                                student.getName(),
                                student.getStudentType(),
                                overallAverage,
                                reportType
                            );

                            // Add all grades to report
                            for (Grade studentGrade : gradeManager.getGrades()) {
                                if (studentGrade != null && studentGrade.getStudentId().equals(studentId)) {
                                    GradeData gradeData = new GradeData(
                                        studentGrade.getGradeId(),
                                        studentGrade.getDate(),
                                        studentGrade.getSubject().getSubjectName(),
                                        studentGrade.getSubject().getSubjectType(),
                                        studentGrade.getGrade()
                                    );
                                    report.addGrade(gradeData);
                                }
                            }

                            // Generate filename with student name
                            String sanitizedFileName = fileName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
                            if (sanitizedFileName.isEmpty()) {
                                sanitizedFileName = student.getName().toLowerCase().replaceAll("[^a-z0-9_]", "_");
                            }
                            String reportTypeSuffix = reportType.toLowerCase().replaceAll(" ", "_");
                            String finalFileName = sanitizedFileName + "_" + reportTypeSuffix;

                            // Export using MultiFormatFileHandler
                            MultiFormatFileHandler fileHandler = new MultiFormatFileHandler();

                            if (formatChoice == 4) {
                                // Export to all formats
                                MultiFormatFileHandler.ExportResult result = fileHandler.exportToAllFormats(report, finalFileName);

                                System.out.println("✓ CSV Export completed");
                                System.out.printf("  File: %s.csv\n", finalFileName);
                                System.out.printf("  Location: %s\n", result.getCsvPath().getParent());
                                System.out.printf("  Size: %s\n", formatFileSize(result.getCsvSize()));
                                System.out.printf("  Content: %d grades + header\n", report.getGrades().size());
                                System.out.printf("  Time: %dms\n", result.getCsvTime());
                                System.out.println();

                                System.out.println("✓ JSON Export completed");
                                System.out.printf("  File: %s.json\n", finalFileName);
                                System.out.printf("  Location: %s\n", result.getJsonPath().getParent());
                                System.out.printf("  Size: %s\n", formatFileSize(result.getJsonSize()));
                                System.out.println("  Structure: Nested objects with metadata");
                                System.out.printf("  Time: %dms\n", result.getJsonTime());
                                System.out.println();

                                System.out.println("✓ Binary Export completed");
                                System.out.printf("  File: %s.dat\n", finalFileName);
                                System.out.printf("  Location: %s\n", result.getBinaryPath().getParent());
                                System.out.printf("  Size: %s (compressed)\n", formatFileSize(result.getBinarySize()));
                                System.out.println("  Format: Serialized StudentReport object");
                                System.out.printf("  Time: %dms\n", result.getBinaryTime());
                                System.out.println();

                                // Performance summary
                                System.out.println(fileHandler.getPerformanceSummary());

                            } else {
                                Path exportedPath = null;
                                long fileSize = 0;
                                long writeTime = 0;

                                switch (formatChoice) {
                                    case 1:
                                        exportedPath = fileHandler.exportToCSV(report, finalFileName);
                                        fileSize = fileHandler.getCsvFileSize();
                                        writeTime = fileHandler.getCsvWriteTime();
                                        System.out.println("✓ CSV Export completed");
                                        System.out.printf("  File: %s.csv\n", finalFileName);
                                        break;
                                    case 2:
                                        exportedPath = fileHandler.exportToJSON(report, finalFileName);
                                        fileSize = fileHandler.getJsonFileSize();
                                        writeTime = fileHandler.getJsonWriteTime();
                                        System.out.println("✓ JSON Export completed");
                                        System.out.printf("  File: %s.json\n", finalFileName);
                                        break;
                                    case 3:
                                        exportedPath = fileHandler.exportToBinary(report, finalFileName);
                                        fileSize = fileHandler.getBinaryFileSize();
                                        writeTime = fileHandler.getBinaryWriteTime();
                                        System.out.println("✓ Binary Export completed");
                                        System.out.printf("  File: %s.dat\n", finalFileName);
                                        break;
                                }

                                if (exportedPath != null) {
                                    System.out.printf("  Location: %s\n", exportedPath.getParent());
                                    System.out.printf("  Size: %s\n", formatFileSize(fileSize));
                                    System.out.printf("  Time: %dms\n", writeTime);
                                    System.out.println();
                                }
                            }

                        } catch (FileExportException e) {
                            System.out.println();
                            System.out.println(e.getMessage());
                            System.out.println();
                        }

                    } catch (StudentNotFoundException e) {
                        System.out.println();
                        System.out.println(e.getMessage());
                        System.out.println();
                    }

                    break;
                case 6:
                    try {
                        System.out.println("IMPORT DATA (Multi-format support)");
                        System.out.println("_______________________________________________");
                        System.out.println();
                        
                        System.out.println("Import Format Options:");
                        System.out.println("1. CSV Format");
                        System.out.println("2. JSON Format");
                        System.out.println("3. Binary Format (.dat)");
                        System.out.println("4. Return to Main Menu");
                        System.out.println();
                        
                        System.out.print("Select format (1-4): ");
                        int importFormat;
                        try {
                            importFormat = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-4).\n");
                            scanner.nextLine();
                            break;
                        }
                        
                        System.out.println();
                        
                        if (importFormat == 4) {
                            break;
                        }
                        
                        if (importFormat < 1 || importFormat > 3) {
                            System.out.println("X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-4).\n");
                            break;
                        }
                        
                        // Get file path
                        System.out.print("Enter file path (or filename if in default directory): ");
                        String fileInput = scanner.nextLine().trim();
                        System.out.println();
                        
                        Path filePath;
                        if (fileInput.contains("/") || fileInput.contains("\\") || fileInput.contains(":")) {
                            // Absolute or relative path provided
                            filePath = Paths.get(fileInput);
                        } else {
                            // Just filename - use appropriate default directory
                            String defaultDir = "";
                            String extension = "";
                            switch (importFormat) {
                                case 1:
                                    defaultDir = "./data/csv/";
                                    extension = fileInput.endsWith(".csv") ? "" : ".csv";
                                    break;
                                case 2:
                                    defaultDir = "./data/json/";
                                    extension = fileInput.endsWith(".json") ? "" : ".json";
                                    break;
                                case 3:
                                    defaultDir = "./data/binary/";
                                    extension = fileInput.endsWith(".dat") ? "" : ".dat";
                                    break;
                            }
                            filePath = Paths.get(defaultDir + fileInput + extension);
                        }
                        
                        System.out.println("Importing from: " + filePath);
                        System.out.println("Validating file...");
                        
                        MultiFormatFileHandler fileHandler = new MultiFormatFileHandler();
                        StudentReport importedReport = null;
                        long importStartTime = System.currentTimeMillis();
                        long fileSize = 0;
                        
                        try {
                            fileSize = Files.size(filePath);
                        } catch (IOException e) {
                            System.out.println("X ERROR: File not found or cannot access file size\n");
                            break;
                        }
                        
                        try {
                            switch (importFormat) {
                                case 1:
                                    // CSV import - parse as grade data
                                    System.out.println("Processing CSV format...");
                                    List<String[]> csvData = fileHandler.importCSV(filePath);
                                    
                                    if (csvData.isEmpty()) {
                                        System.out.println("X ERROR: CSV file is empty or has no valid data\n");
                                        break;
                                    }
                                    
                                    System.out.println("✓ CSV file parsed successfully");
                                    System.out.println("Found " + csvData.size() + " grade records");
                                    System.out.println();
                                    
                                    // Process CSV data
                                    int successCount = 0;
                                    int failCount = 0;
                                    List<String> errors = new ArrayList<>();
                                    
                                    for (int i = 0; i < csvData.size(); i++) {
                                        String[] row = csvData.get(i);
                                        if (row.length < 4) {
                                            failCount++;
                                            errors.add("Row " + (i + 2) + ": Insufficient columns");
                                            continue;
                                        }
                                        
                                        try {
                                            String csvStudentId = row[0].trim();
                                            String subjectName = row[1].trim();
                                            String subjectType = row[2].trim();
                                            double gradeValue = Double.parseDouble(row[3].trim());
                                            
                                            // Validate student exists
                                            Student csvStudent = studentManager.findStudent(csvStudentId);
                                            if (csvStudent == null) {
                                                failCount++;
                                                errors.add("Row " + (i + 2) + ": Student not found (" + csvStudentId + ")");
                                                continue;
                                            }
                                            
                                            // Validate grade range
                                            if (gradeValue < 0 || gradeValue > 100) {
                                                failCount++;
                                                errors.add("Row " + (i + 2) + ": Grade out of range (" + gradeValue + ")");
                                                continue;
                                            }
                                            
                                            // Create subject
                                            Subject csvSubject;
                                            if (subjectType.equalsIgnoreCase("Core")) {
                                                csvSubject = new CoreSubject(subjectName, "");
                                            } else if (subjectType.equalsIgnoreCase("Elective")) {
                                                csvSubject = new ElectiveSubject(subjectName, "");
                                            } else {
                                                failCount++;
                                                errors.add("Row " + (i + 2) + ": Invalid subject type (" + subjectType + ")");
                                                continue;
                                            }
                                            
                                            // Create and add grade
                                            Grade newGrade = new Grade(csvStudentId, csvSubject, gradeValue);
                                            gradeManager.addGrade(newGrade);
                                            successCount++;
                                            
                                        } catch (NumberFormatException e) {
                                            failCount++;
                                            errors.add("Row " + (i + 2) + ": Invalid grade value");
                                        } catch (Exception e) {
                                            failCount++;
                                            errors.add("Row " + (i + 2) + ": " + e.getMessage());
                                        }
                                    }
                                    
                                    long importTime = System.currentTimeMillis() - importStartTime;
                                    
                                    // Record I/O operation
                                    performanceMonitor.recordIOOperation("CSV Read", 
                                        filePath.getFileName().toString(), importTime, fileSize, true);
                                    
                                    System.out.println("Import Summary:");
                                    System.out.println("  Successful: " + successCount);
                                    System.out.println("  Failed: " + failCount);
                                    System.out.println("  Time: " + importTime + "ms");
                                    System.out.println("  File Size: " + formatFileSize(fileSize));
                                    
                                    if (!errors.isEmpty() && errors.size() <= 10) {
                                        System.out.println();
                                        System.out.println("Errors:");
                                        for (String error : errors) {
                                            System.out.println("  - " + error);
                                        }
                                    } else if (errors.size() > 10) {
                                        System.out.println();
                                        System.out.println("Errors (showing first 10):");
                                        for (int i = 0; i < 10; i++) {
                                            System.out.println("  - " + errors.get(i));
                                        }
                                        System.out.println("  ... and " + (errors.size() - 10) + " more errors");
                                    }
                                    System.out.println();
                                    break;
                                    
                                case 2:
                                    // JSON import
                                    System.out.println("Processing JSON format...");
                                    importedReport = fileHandler.importFromJSON(filePath);
                                    System.out.println("✓ JSON file parsed successfully");
                                    
                                    // Process imported report
                                    int jsonImportedCount = processImportedReport(importedReport, studentManager, gradeManager);
                                    
                                    long jsonImportTime = System.currentTimeMillis() - importStartTime;
                                    
                                    // Record I/O operation
                                    performanceMonitor.recordIOOperation("JSON Read", 
                                        filePath.getFileName().toString(), jsonImportTime, fileSize, true);
                                    
                                    System.out.println("Import Summary:");
                                    System.out.println("  Student: " + importedReport.getStudentName() + " (" + importedReport.getStudentId() + ")");
                                    System.out.println("  Grades Imported: " + jsonImportedCount);
                                    System.out.println("  Time: " + jsonImportTime + "ms");
                                    System.out.println("  File Size: " + formatFileSize(fileSize));
                                    System.out.println();
                                    break;
                                    
                                case 3:
                                    // Binary import
                                    System.out.println("Processing Binary format...");
                                    importedReport = fileHandler.importFromBinary(filePath);
                                    System.out.println("✓ Binary file parsed successfully");
                                    
                                    // Process imported report
                                    int binaryImportedCount = processImportedReport(importedReport, studentManager, gradeManager);
                                    
                                    long binaryImportTime = System.currentTimeMillis() - importStartTime;
                                    
                                    // Record I/O operation
                                    performanceMonitor.recordIOOperation("Binary Read", 
                                        filePath.getFileName().toString(), binaryImportTime, fileSize, true);
                                    
                                    System.out.println("Import Summary:");
                                    System.out.println("  Student: " + importedReport.getStudentName() + " (" + importedReport.getStudentId() + ")");
                                    System.out.println("  Grades Imported: " + binaryImportedCount);
                                    System.out.println("  Time: " + binaryImportTime + "ms");
                                    System.out.println("  File Size: " + formatFileSize(fileSize));
                                    System.out.println();
                                    break;
                            }
                            
                        } catch (FileExportException e) {
                            System.out.println();
                            System.out.println("X ERROR: " + e.getMessage());
                            System.out.println();
                        }
                        
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
                        System.out.println();
                    }
                    break;
                case 7:
                    System.out.println("BULK IMPORT GRADES");
                    System.out.println("_______________________________________________");
                    System.out.println();

                    System.out.println("Place your CSV file in: ./imports/");
                    System.out.println();

                    System.out.println("CSV Format Required:");
                    System.out.println("StudentID,SubjectName,SubjectType,Grade");
                    System.out.println("Example: STU001,Mathematics,Core,85");
                    System.out.println();

                    // import files from the imports directory
                    String csvFilePath = "./imports/";

                    System.out.print("Enter filename (without extension): ");
                    String fileName = scanner.nextLine();
                    System.out.println();

                    try {
                        // Construct full file path with .csv extension
                        String fullFilePath = csvFilePath + fileName + ".csv";

                        System.out.println("Validating file... ✓");

                        CSVParser csvParser = new CSVParser(fullFilePath);

                        // Each element in gradeData is a String array representing on row of
                        // data (StudentID, SubjectName, SubjectType, Grade)
                        ArrayList<String[]> gradeData = csvParser.parseGradeCSV();

                        // If there are no rows in the CSV file, throw InvalidFileFormatException
                        if (gradeData.isEmpty()) {
                            throw new InvalidFileFormatException(
                                    "X ERROR: InvalidFileFormatException\n   No valid data found in CSV file.\n   Please check the file format and content."
                            );
                        }

                        System.out.println("Processing grades...");
                        System.out.println();

                        // Tracking import results
                        int successCount = 0;
                        int failCount = 0;

                        // Stores reports about failed imports
                        ArrayList<String> failedRecords = new ArrayList<>();

                        for (int i = 0; i < gradeData.size(); i++) {
                            // Since each element in gradeData is a String array containing
                            // the information. "data" retrieves the current row
                            String[] data = gradeData.get(i);

                            // Adding 2 because the header is skipped during parsing, and we want the array index i
                            // to match the line numbers in the CSV file to make the failed import reports
                            // more understandable
                            int lineNumber = i + 2;

                            try {
                                // data[0] because student id is the first element in the data (String) array
                                Student studentCheck = studentManager.findStudent(data[0]);

                                // if the student is not found, add the failed record to the list
                                // and log why it failed then move on to the next row
                                if (studentCheck == null) {
                                    failCount++;
                                    failedRecords.add(String.format("Row %d: Invalid student ID (%s)", lineNumber, data[0]));
                                    continue;
                                }

                                // Validate entry format
                                if (!csvParser.validateGradeEntry(data)) {
                                    failCount++;

                                    // Check specific validation failure reason
                                    try {
                                        double gradeValue = Double.parseDouble(data[3]);
                                        if (gradeValue < 0 || gradeValue > 100) {
                                            failedRecords.add(String.format("Row %d: Grade out of range (%s)", lineNumber, data[3]));
                                        } else {
                                            failedRecords.add(String.format("Row %d: Invalid data format", lineNumber));
                                        }
                                    } catch (NumberFormatException e) {
                                        failedRecords.add(String.format("Row %d: Invalid grade value", lineNumber));
                                    }
                                    continue;
                                }

                                // Import valid grade
                                String stuId = data[0];
                                String parsedSubjectName = data[1];
                                String parsedSubjectType = data[2];
                                double gradeValue = Double.parseDouble(data[3]);

                                // Create appropriate subject
                                Subject subj;
                                if (parsedSubjectType.equals("Core")) {
                                    subj = new CoreSubject(parsedSubjectName, "");
                                } else {
                                    subj = new ElectiveSubject(parsedSubjectName, "");
                                }

                                // Create and add grade
                                Grade newGrade = new Grade(stuId, subj, gradeValue);

                                if (newGrade.recordGrade(gradeValue)) {
                                    newGrade.setGradeId();
                                    gradeManager.addGrade(newGrade);
                                    successCount++;
                                } else {
                                    failCount++;
                                    failedRecords.add(String.format("Row %d: Failed to record grade", lineNumber));
                                }
                            } catch (StudentNotFoundException e) {
                                failCount++;
                                failedRecords.add(String.format("Row %d: Invalid student ID (%s)", lineNumber, data[0]));
                            } catch (Exception e) {
                                failCount++;
                                failedRecords.add(String.format("Row %d: Processing error", lineNumber));
                            }
                        }

                        // Display import summary
                        System.out.println("IMPORT SUMMARY");
                        System.out.println("_______________________________________________");
                        System.out.printf("Total Rows: %d\n", gradeData.size());
                        System.out.printf("Successfully Imported: %d\n", successCount);
                        System.out.printf("Failed: %d\n", failCount);
                        System.out.println();

                        // Display failed records if any
                        if (failCount > 0) {
                            System.out.println("Failed Records:");
                            for (String failedRecord : failedRecords) {
                                System.out.println(failedRecord);
                            }
                            System.out.println();
                        }

                        // Generate import log
                        if (successCount > 0) {
                            System.out.println("Import completed!");
                            System.out.printf("  %d grades added to system\n", successCount);

                            // Create import log file
                            String timestamp = LocalDateTime.now().format(
                                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                            );
                            String logFileName = "import_log_" + timestamp;
                            FileExporter logExporter = new FileExporter(logFileName);

                            StringBuilder logContent = new StringBuilder();
                            logContent.append("BULK IMPORT LOG\n");
                            logContent.append("_______________________________________________\n\n");
                            logContent.append(String.format("Source File: %s.csv\n", fileName));
                            logContent.append(String.format("Import Date: %s\n", LocalDate.now()));
                            logContent.append(String.format("Total Rows: %d\n", gradeData.size()));
                            logContent.append(String.format("Successfully Imported: %d\n", successCount));
                            logContent.append(String.format("Failed: %d\n\n", failCount));

                            if (failCount > 0) {
                                logContent.append("Failed Records:\n");
                                for (String failedRecord : failedRecords) {
                                    logContent.append(failedRecord + "\n");
                                }
                            }

                            logExporter.exportGradeToTXT(logContent.toString());
                            System.out.printf("  See import_log_%s.txt for details\n", timestamp);
                        }

                        System.out.println();

                    } catch (InvalidFileFormatException e) {
                        System.out.println();
                        System.out.println(e.getMessage());
                        System.out.println();
                    } catch (IOException e) {
                        System.out.println();
                        System.out.println("X ERROR: CSVParseException\n   Failed to read CSV file: " + fileName + ".csv\n   Please check the file exists in ./imports/ directory.");
                        System.out.println();
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   Error processing CSV file: " + e.getMessage());
                        System.out.println();
                    }

                    break;
                case 8:
                    System.out.println("CALCULATE STUDENT GPA");
                    System.out.println("_______________________________________________");
                    System.out.println();

                    System.out.print("Enter Student ID: ");
                    studentId = scanner.nextLine();
                    System.out.println();

                    try {
                        student = studentManager.findStudent(studentId);

                        // Check if student has any grades
                        if (student.getEnrolledSubjectsCount() == 0) {
                            System.out.println("No grades recorded for this student yet.");
                            System.out.println();
                            break;
                        }

                        // Create GPA calculator and generate report
                        GPACalculator gpaCalculator = new GPACalculator(gradeManager);
                        String gpaReport = gpaCalculator.generateGPAReport(studentId, student, studentManager);
                        System.out.println(gpaReport);
                    } catch (StudentNotFoundException e) {
                        System.out.println();
                        System.out.println(e.getMessage());
                        System.out.println();
                    }

                    break;
                case 9:
                    System.out.println("VIEW CLASS STATISTICS");
                    System.out.println("_______________________________________________");
                    System.out.println();

                    // Check if there are any grades recorded
                    if (gradeManager.getGradeCount() == 0) {
                        System.out.println("No grades recorded yet. Statistics unavailable.");
                        System.out.println();
                        break;
                    }

                    // Create ClassStatistics instance and generate report
                    StatisticsCalculator classStats = new StatisticsCalculator(gradeManager, studentManager);
                    String statsReport = classStats.generateClassStatistics();
                    System.out.println(statsReport);

                    break;
                case 10:
                    try {
                        System.out.println("REAL-TIME STATISTICS DASHBOARD");
                        System.out.println("_______________________________________________");
                        System.out.println();
                        System.out.println("Starting background daemon thread...");
                        
                        StatisticsDashboard dashboard = new StatisticsDashboard(studentManager, gradeManager);
                        dashboardRef[0] = dashboard; // Store reference for menu display
                        
                        // Register thread pool with performance monitor
                        performanceMonitor.registerThreadPool("StatisticsDashboard", 
                            dashboard.getExecutorService(), dashboard.getMaxThreadCount());
                        
                        // Start the dashboard (this will perform initial calculation)
                        dashboard.start();
                        
                        System.out.println("✓ Background daemon thread started");
                        System.out.println("✓ Auto-refresh enabled (every 5 seconds)");
                        System.out.println();
                        System.out.println("Waiting for initial statistics calculation...");
                        
                        // Wait for initial calculation to complete
                        int waitCount = 0;
                        while (dashboard.isCalculating() && waitCount < 20) {
                            Thread.sleep(100);
                            waitCount++;
                        }
                        
                        System.out.println();
                        
                        // Display initial dashboard
                        dashboard.displayDashboard();
                        
                        // Auto-refresh approach: background thread handles display automatically
                        // Main thread handles user input in a loop
                        final boolean[] shouldQuit = {false};
                        
                        // Create input reader thread
                        Thread inputThread = new Thread(() -> {
                            Scanner inputScanner = new Scanner(System.in);
                            while (!shouldQuit[0] && dashboard.isRunning()) {
                                try {
                                    System.out.print("\nCommand (Q=Quit | R=Refresh | P=Pause/Resume): ");
                                    System.out.flush();
                                    
                                    if (inputScanner.hasNextLine()) {
                                        String input = inputScanner.nextLine().trim().toUpperCase();
                                        
                                        // Process command
                                        if (input.equals("Q")) {
                                            System.out.println("\n⏹ Stopping dashboard...");
                                            shouldQuit[0] = true;
                                            break;
                                        } else if (input.equals("R")) {
                                            System.out.println("\n🔄 Manual refresh triggered...");
                                            dashboard.refresh();
                                            Thread.sleep(200);
                                            dashboard.displayDashboard();
                                        } else if (input.equals("P")) {
                                            if (dashboard.isPaused()) {
                                                System.out.println("\n▶ Resuming auto-refresh...");
                                                dashboard.resume();
                                            } else {
                                                System.out.println("\n⏸ Pausing auto-refresh...");
                                                dashboard.pause();
                                            }
                                            Thread.sleep(100);
                                            dashboard.displayDashboard();
                                        } else if (!input.isEmpty()) {
                                            System.out.println("❌ Unknown command. Use Q, R, or P.");
                                        }
                                    }
                                } catch (Exception e) {
                                    // Input interrupted, exit gracefully
                                    break;
                                }
                            }
                        });
                        
                        inputThread.setName("DashboardInputThread");
                        inputThread.setDaemon(true);
                        inputThread.start();
                        
                        // Wait for quit signal
                        while (!shouldQuit[0] && dashboard.isRunning()) {
                            Thread.sleep(100);
                        }
                        
                        // Cleanup
                        dashboard.stop();
                        inputThread.interrupt();
                        
                        System.out.println();
                        System.out.println("✓ Dashboard closed successfully");
                        System.out.println("✓ Background thread terminated");
                        System.out.println();
                        
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
                        if (dashboardRef[0] != null) {
                            dashboardRef[0].stop();
                            dashboardRef[0] = null;
                        }
                        System.out.println();
                    }
                    break;
                case 11:
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
                        int scopeChoice;
                        try {
                            scopeChoice = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-4).\n");
                            scanner.nextLine();
                            break;
                        }
                        
                        System.out.println();
                        System.out.println("Report Format:");
                        System.out.println("1. CSV (Comma-Separated Values)");
                        System.out.println("2. JSON (JavaScript Object Notation)");
                        System.out.println("3. Binary (Serialized Java Object)");
                        System.out.println("4. All formats");
                        System.out.println();
                        
                        System.out.print("Select format (1-4): ");
                        int formatChoice;
                        try {
                            formatChoice = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-4).\n");
                            scanner.nextLine();
                            break;
                        }
                        
                        System.out.println();
                        System.out.println("Report Type:");
                        System.out.println("1. Summary Report");
                        System.out.println("2. Detailed Report");
                        System.out.println("3. Transcript Format");
                        System.out.println("4. Performance Analytics");
                        System.out.println();
                        
                        System.out.print("Select type (1-4): ");
                        int reportTypeChoice;
                        try {
                            reportTypeChoice = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-4).\n");
                            scanner.nextLine();
                            break;
                        }
                        
                        String reportType;
                        switch (reportTypeChoice) {
                            case 1: reportType = "Summary Report"; break;
                            case 2: reportType = "Detailed Report"; break;
                            case 3: reportType = "Transcript Format"; break;
                            case 4: reportType = "Performance Analytics"; break;
                            default: reportType = "Detailed Report"; break;
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
                        int threadCount;
                        try {
                            threadCount = scanner.nextInt();
                            scanner.nextLine();
                            if (threadCount < 1 || threadCount > 8) {
                                throw new InvalidMenuChoiceException(
                                    "X ERROR: InvalidMenuChoiceException\n   Thread count must be between 1 and 8.\n   You entered: " + threadCount
                                );
                            }
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-8).\n");
                            scanner.nextLine();
                            break;
                        }
                        
                        System.out.println();
                        System.out.println("Initializing thread pool...");
                        
                        // Initialize batch generator
                        BatchReportGenerator batchGenerator = new BatchReportGenerator(studentManager, gradeManager);
                        if (!batchGenerator.initializeThreadPool(threadCount)) {
                            System.out.println("X ERROR: Failed to initialize thread pool\n");
                            break;
                        }
                        // Register thread pool with performance monitor
                        if (batchGenerator.getExecutorService() != null) {
                            performanceMonitor.registerThreadPool("FixedThreadPool", 
                                batchGenerator.getExecutorService(), batchGenerator.getMaxThreadCount());
                        }
                        System.out.println("✓ Fixed Thread Pool created: " + threadCount + " threads");
                        System.out.println();
                        
                        // Get students based on scope
                        List<Student> studentsToProcess = new ArrayList<>();
                        switch (scopeChoice) {
                            case 1:
                                studentsToProcess = studentManager.getStudentsList();
                                break;
                            case 2:
                                System.out.println("Student Type:");
                                System.out.println("1. Regular Student");
                                System.out.println("2. Honors Student");
                                System.out.println();
                                System.out.print("Select type (1-2): ");
                                int typeChoice = scanner.nextInt();
                                scanner.nextLine();
                                String selectedType = (typeChoice == 1) ? "Regular" : "Honors";
                                for (Student s : studentManager.getStudentsList()) {
                                    if (s.getStudentType().equals(selectedType)) {
                                        studentsToProcess.add(s);
                                    }
                                }
                                break;
                            case 3:
                                System.out.print("Enter minimum grade average: ");
                                double minGrade = scanner.nextDouble();
                                scanner.nextLine();
                                System.out.print("Enter maximum grade average: ");
                                double maxGrade = scanner.nextDouble();
                                scanner.nextLine();
                                for (Student s : studentManager.getStudentsList()) {
                                    double avg = s.calculateAverageGrade();
                                    if (avg >= minGrade && avg <= maxGrade) {
                                        studentsToProcess.add(s);
                                    }
                                }
                                break;
                            case 4:
                                System.out.println("Enter student IDs (comma-separated): ");
                                String idsInput = scanner.nextLine();
                                String[] ids = idsInput.split(",");
                                for (String id : ids) {
                                    try {
                                        Student s = studentManager.findStudent(id.trim());
                                        if (s != null) {
                                            studentsToProcess.add(s);
                                        }
                                    } catch (StudentNotFoundException e) {
                                        // Skip invalid IDs
                                    }
                                }
                                break;
                            default:
                                System.out.println("X ERROR: Invalid scope choice\n");
                                break;
                        }
                        
                        if (studentsToProcess.isEmpty()) {
                            System.out.println("X ERROR: No students selected for batch processing\n");
                            batchGenerator.shutdown(5);
                            break;
                        }
                        
                        System.out.println("Processing " + studentsToProcess.size() + " student reports...");
                        System.out.println();
                        
                        // Generate batch reports
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
                        try {
                            long totalSize = java.nio.file.Files.walk(result.getOutputDir())
                                .filter(java.nio.file.Files::isRegularFile)
                                .mapToLong(p -> {
                                    try {
                                        return java.nio.file.Files.size(p);
                                    } catch (java.io.IOException e) {
                                        return 0;
                                    }
                                })
                                .sum();
                            System.out.println("Total Size: " + formatFileSize(totalSize));
                        } catch (java.io.IOException e) {
                            // Ignore size calculation error
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
                    break;
                case 12:
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
                        int searchOption;

                        try {
                            searchOption = scanner.nextInt();
                            scanner.nextLine();
                            System.out.println();
                        } catch (InputMismatchException e) {
                            System.out.println(
                                    "X ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-4).\n"
                            );
                            scanner.nextLine();
                            break;
                        }

                        if (searchOption >= 1 && searchOption <= 4) {
                            if (searchOption == 1) { // Exact match by student ID
                                System.out.print("Enter Student ID: ");
                                studentId = scanner.nextLine();

                                student = studentManager.findStudent(studentId);

                                if (student != null) {
                                    System.out.printf("Student ID: %s\n", student.getStudentId());
                                    System.out.printf("Name: %s\n", student.getName());
                                    System.out.printf("Type: %s\n", student.getStudentType());
                                    System.out.printf("Age: %d\n", student.getAge());
                                    System.out.printf("Email: %s\n", student.getEmail());
                                    System.out.printf("Passing Grade: %d%%\n", (int) student.getPassingGrade());
                                    System.out.printf("Average: %.2f\n", student.calculateAverageGrade());
                                    System.out.println();
                                }
                            } else if (searchOption == 2) {// Search by name (partial match)
                                System.out.print("Enter name (partial or full): ");
                                String searchName = scanner.nextLine();

                                Student[] students = studentManager.getStudents();

                                if (students.length > 0) {
                                    System.out.println("----------------------------------------------------------------------------|");
                                    System.out.println("STU ID   | NAME                    | TYPE               | AVG GRADE         |");
                                    System.out.println("----------------------------------------------------------------------------|");

                                    int match = 0;

                                    for (Student s : students) {
                                        if (s == null) continue;

                                        if (s.getName().contains(searchName)) {
                                            System.out.printf("%-8s | %-23s | %-18s | %-17.1f%%\n",
                                                    s.getStudentId(), s.getName(), s.getStudentType(), s.calculateAverageGrade());
                                            match++;
                                        }
                                    }
                                    System.out.println();

                                    // if no student matches the search
                                    if (match == 0) {// throw a custom exception here
                                        System.out.println("No student matches the name you entered");
                                        System.out.println();
                                    }
                                } else {
                                    System.out.println("No students have been added");
                                }
                            } else if (searchOption == 3) {// Search by grade range
                                System.out.print("Enter the minimum grade range: ");
                                int minGrade;
                                try {
                                    minGrade = scanner.nextInt();
                                    scanner.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("\nX ERROR: InvalidGradeException\n   Please enter a valid grade.\n");
                                    scanner.nextLine();
                                    break;
                                }

                                System.out.print("Enter the maximum grade range: ");
                                int maxGrade;
                                try {
                                    maxGrade = scanner.nextInt();
                                    scanner.nextLine();
                                    System.out.println();
                                } catch (InputMismatchException e) {
                                    System.out.println("\nX ERROR: InvalidGradeException\n   Please enter a valid grade.\n");
                                    scanner.nextLine();
                                    break;
                                }

                                Student[] students = studentManager.getStudents();

                                if (students.length > 0) {
                                    System.out.println("----------------------------------------------------------------------------|");
                                    System.out.println("STU ID   | NAME                    | TYPE               | AVG GRADE         |");
                                    System.out.println("----------------------------------------------------------------------------|");

                                    int match = 0;

                                    for (Student s : students) {
                                        if (s == null) continue;

                                        double avg = s.calculateAverageGrade();
                                        if (avg >= minGrade && avg <= maxGrade) {
                                            System.out.printf("%-8s | %-23s | %-18s | %-17.1f%%\n",
                                                    s.getStudentId(), s.getName(), s.getStudentType(), avg);
                                            match++;
                                        }
                                    }
                                    System.out.println("----------------------------------------------------------------------------|");
                                    System.out.println("SEARCH RESULTS (" + match + " found with grades between " + minGrade + "% and " + maxGrade + "%)");
                                    System.out.println();

                                    if (match == 0) {
                                        System.out.println("No students found in the specified grade range");
                                        System.out.println();
                                    }
                                } else {
                                    System.out.println("No students have been added");
                                    System.out.println();
                                }

                            } else {
                                System.out.println("Select Student Type:");
                                System.out.println("1. Regular");
                                System.out.println("2. Honors");

                                System.out.print("Enter choice (1-2): ");
                                int typeChoice;

                                try {
                                    typeChoice = scanner.nextInt();
                                    scanner.nextLine();
                                    System.out.println();
                                } catch (InputMismatchException e) {
                                    System.out.println(
                                            "X ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-2).\n"
                                    );
                                    scanner.nextLine();
                                    break;
                                }

                                String searchType;

                                if (typeChoice == 1) {
                                    searchType = "Regular";
                                } else if (typeChoice == 2) {
                                    searchType = "Honors";
                                } else {
                                    throw new InvalidMenuChoiceException(
                                            "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-2).\n"
                                    );
                                }

                                Student[] students = studentManager.getStudents();

                                if (students.length > 0) {
                                    System.out.println("----------------------------------------------------------------------------|");
                                    System.out.println("STU ID   | NAME                    | TYPE               | AVG GRADE         |");
                                    System.out.println("----------------------------------------------------------------------------|");

                                    int match = 0;

                                    for (Student s : students) {
                                        if (s == null) continue;

                                        if (s.getStudentType().equalsIgnoreCase(searchType)) {
                                            System.out.printf("%-8s | %-23s | %-18s | %-17.1f%%\n",
                                                    s.getStudentId(), s.getName(), s.getStudentType(), s.calculateAverageGrade());
                                            match++;
                                        }
                                    }
                                    System.out.println("----------------------------------------------------------------------------|");
                                    System.out.println("SEARCH RESULTS (" + match + " " + searchType + " students found)");
                                    System.out.println();

                                    if (match == 0) {
                                        System.out.println("No " + searchType + " students found");
                                        System.out.println();
                                    }
                                } else {
                                    System.out.println("No students have been added");
                                    System.out.println();
                                }
                            }
                        } else {
                            throw new InvalidMenuChoiceException(
                                    "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-4).\n   You entered: " + searchOption
                            );
                        }
                    } catch (StudentNotFoundException | InvalidMenuChoiceException e) {
                        System.out.println();
                        System.out.println(e.getMessage());
                        System.out.println();
                    }
                    break;
                case 13:
                    try {
                        System.out.println("PATTERN-BASED SEARCH");
                        System.out.println("_______________________________________________");
                        System.out.println();
                        
                        PatternSearchService searchService = new PatternSearchService(studentManager);
                        
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
                            searchTypeChoice = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-5).\n");
                            scanner.nextLine();
                            break;
                        }
                        
                        System.out.println();
                        System.out.print("Case-insensitive search? (Y/N): ");
                        String caseInsensitiveInput = scanner.nextLine().trim().toUpperCase();
                        boolean caseInsensitive = caseInsensitiveInput.equals("Y");
                        System.out.println();
                        
                        PatternSearchService.SearchResults searchResults = null;
                        String patternInput = "";
                        
                        switch (searchTypeChoice) {
                            case 1:
                                System.out.print("Enter email domain pattern: ");
                                patternInput = scanner.nextLine();
                                System.out.println();
                                String emailRegex = patternInput.startsWith("@") ? 
                                    ".*" + java.util.regex.Pattern.quote(patternInput) + "$" : 
                                    ".*@" + java.util.regex.Pattern.quote(patternInput) + "$";
                                System.out.println("Searching with regex: " + emailRegex);
                                System.out.println("Processing " + studentManager.getStudentCount() + " students...");
                                System.out.println();
                                searchResults = searchService.searchByEmailDomain(patternInput, caseInsensitive);
                                break;
                            case 2:
                                System.out.print("Enter phone area code pattern: ");
                                patternInput = scanner.nextLine();
                                System.out.println();
                                System.out.println("Searching with pattern: " + patternInput);
                                System.out.println("Processing " + studentManager.getStudentCount() + " students...");
                                System.out.println();
                                searchResults = searchService.searchByPhoneAreaCode(patternInput, caseInsensitive);
                                break;
                            case 3:
                                System.out.print("Enter student ID pattern (use * for wildcard): ");
                                patternInput = scanner.nextLine();
                                System.out.println();
                                System.out.println("Searching with pattern: " + patternInput);
                                System.out.println("Processing " + studentManager.getStudentCount() + " students...");
                                System.out.println();
                                searchResults = searchService.searchByStudentIdPattern(patternInput, caseInsensitive);
                                break;
                            case 4:
                                System.out.print("Enter name pattern: ");
                                patternInput = scanner.nextLine();
                                System.out.println();
                                System.out.println("Searching with pattern: " + patternInput);
                                System.out.println("Processing " + studentManager.getStudentCount() + " students...");
                                System.out.println();
                                searchResults = searchService.searchByNamePattern(patternInput, caseInsensitive);
                                break;
                            case 5:
                                System.out.print("Enter custom regex pattern: ");
                                patternInput = scanner.nextLine();
                                System.out.println();
                                System.out.println("Searching with regex: " + patternInput);
                                System.out.println("Processing " + studentManager.getStudentCount() + " students...");
                                System.out.println();
                                searchResults = searchService.searchByCustomPattern(patternInput, null, caseInsensitive);
                                break;
                            default:
                                System.out.println("X ERROR: Invalid search type\n");
                                break;
                        }
                        
                        if (searchResults == null) {
                            break;
                        }
                        
                        // Display results
                        List<PatternSearchService.SearchResult> results = searchResults.getResults();
                        PatternSearchService.SearchStatistics stats = searchResults.getStatistics();
                        
                        System.out.println("SEARCH RESULTS (" + results.size() + " found)");
                        System.out.println("_______________________________________________");
                        System.out.println();
                        
                        if (results.isEmpty()) {
                            System.out.println("No matches found for pattern: " + patternInput);
                            System.out.println();
                        } else {
                            System.out.printf("%-10s | %-25s | %-30s\n", "STU ID", "NAME", "EMAIL");
                            System.out.println("_______________________________________________");
                            for (PatternSearchService.SearchResult result : results) {
                                Student s = result.getStudent();
                                String email = s.getEmail() != null ? s.getEmail() : "N/A";
                                System.out.printf("%-10s | %-25s | %-30s\n", 
                                    s.getStudentId(), s.getName(), email);
                            }
                            System.out.println();
                            
                            // Display statistics
                            System.out.println("Pattern Match Statistics:");
                            System.out.println("_______________________________________________");
                            System.out.println("Total Students Scanned: " + stats.getTotalScanned());
                            System.out.println("Matches Found: " + stats.getMatchesFound() + 
                                " (" + String.format("%.0f", stats.getMatchPercentage()) + "%)");
                            System.out.println("Search Time: " + stats.getSearchTime() + "ms");
                            System.out.println("Regex Complexity: " + stats.getRegexComplexity());
                            System.out.println();
                            
                            // Display distribution if available
                            Map<String, Integer> distribution = stats.getDistribution();
                            if (!distribution.isEmpty()) {
                                System.out.println("Distribution Statistics:");
                                System.out.println("_______________________________________________");
                                for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
                                    double percentage = stats.getTotalScanned() > 0 ? 
                                        (entry.getValue() * 100.0 / stats.getTotalScanned()) : 0.0;
                                    System.out.printf("%s: %d students (%.0f%%)\n", 
                                        entry.getKey(), entry.getValue(), percentage);
                                }
                                System.out.println();
                            }
                        }
                        
                    } catch (IllegalArgumentException e) {
                        System.out.println();
                        System.out.println(e.getMessage());
                        System.out.println();
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
                        System.out.println();
                    }
                    break;
                case 14:
                    try {
                        System.out.println("QUERY GRADE HISTORY");
                        System.out.println("_______________________________________________");
                        System.out.println();
                        
                        if (gradeManager.getGradeCount() == 0) {
                            System.out.println("No grades recorded yet. Query unavailable.");
                            System.out.println();
                            break;
                        }
                        
                        System.out.println("Query Options:");
                        System.out.println("1. Filter by Student ID");
                        System.out.println("2. Filter by Subject Name");
                        System.out.println("3. Filter by Subject Type (Core/Elective)");
                        System.out.println("4. Filter by Grade Range");
                        System.out.println("5. Filter by Date Range");
                        System.out.println("6. Combined Filters (Advanced)");
                        System.out.println("7. View All Grades");
                        System.out.println("8. Return to Main Menu");
                        System.out.println();
                        
                        System.out.print("Select option (1-8): ");
                        int queryOption;
                        try {
                            queryOption = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-8).\n");
                            scanner.nextLine();
                            break;
                        }
                        
                        System.out.println();
                        
                        if (queryOption == 8) {
                            break;
                        }
                        
                        List<Grade> queryResults = null;
                        boolean queryExecuted = false;
                        
                        switch (queryOption) {
                            case 1:
                                // Filter by Student ID
                                System.out.print("Enter Student ID: ");
                                String queryStudentId = scanner.nextLine().trim();
                                System.out.println();
                                
                                if (queryStudentId.isEmpty()) {
                                    System.out.println("X ERROR: Student ID cannot be empty\n");
                                    break;
                                }
                                
                                queryResults = queryGradesByStudentId(gradeManager, queryStudentId);
                                queryExecuted = true;
                                break;
                                
                            case 2:
                                // Filter by Subject Name
                                System.out.print("Enter Subject Name (or leave empty for all): ");
                                String subjectName = scanner.nextLine().trim();
                                System.out.println();
                                
                                queryResults = queryGradesBySubjectName(gradeManager, subjectName);
                                queryExecuted = true;
                                break;
                                
                            case 3:
                                // Filter by Subject Type
                                System.out.println("Subject Type:");
                                System.out.println("1. Core");
                                System.out.println("2. Elective");
                                System.out.println("3. Both");
                                System.out.print("Select type (1-3): ");
                                int typeChoice;
                                try {
                                    typeChoice = scanner.nextInt();
                                    scanner.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-3).\n");
                                    scanner.nextLine();
                                    break;
                                }
                                
                                System.out.println();
                                
                                if (typeChoice < 1 || typeChoice > 3) {
                                    System.out.println("X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-3).\n");
                                    break;
                                }
                                
                                String subjectType = "";
                                if (typeChoice == 1) {
                                    subjectType = "Core";
                                } else if (typeChoice == 2) {
                                    subjectType = "Elective";
                                }
                                // typeChoice == 3 means "Both", so subjectType remains empty
                                
                                queryResults = queryGradesBySubjectType(gradeManager, subjectType);
                                queryExecuted = true;
                                break;
                                
                            case 4:
                                // Filter by Grade Range
                                System.out.print("Enter minimum grade (0-100, or leave empty for 0): ");
                                String minGradeInput = scanner.nextLine().trim();
                                System.out.print("Enter maximum grade (0-100, or leave empty for 100): ");
                                String maxGradeInput = scanner.nextLine().trim();
                                System.out.println();
                                
                                try {
                                    double minGrade = minGradeInput.isEmpty() ? 0.0 : Double.parseDouble(minGradeInput);
                                    double maxGrade = maxGradeInput.isEmpty() ? 100.0 : Double.parseDouble(maxGradeInput);
                                    
                                    if (minGrade < 0 || maxGrade > 100 || minGrade > maxGrade) {
                                        System.out.println("X ERROR: Invalid grade range\n   Minimum: 0-100, Maximum: 0-100, Min <= Max\n");
                                        break;
                                    }
                                    
                                    queryResults = queryGradesByRange(gradeManager, minGrade, maxGrade);
                                    queryExecuted = true;
                                } catch (NumberFormatException e) {
                                    System.out.println("X ERROR: InvalidNumberFormatException\n   Please enter valid numbers for grade range.\n");
                                    break;
                                }
                                break;
                                
                            case 5:
                                // Filter by Date Range
                                System.out.println("Date format: YYYY-MM-DD");
                                System.out.print("Enter start date (or leave empty for no start limit): ");
                                String startDate = scanner.nextLine().trim();
                                System.out.print("Enter end date (or leave empty for no end limit): ");
                                String endDate = scanner.nextLine().trim();
                                System.out.println();
                                
                                queryResults = queryGradesByDateRange(gradeManager, startDate, endDate);
                                queryExecuted = true;
                                break;
                                
                            case 6:
                                // Combined Filters
                                try {
                                    queryResults = queryGradesWithCombinedFilters(gradeManager, scanner, studentManager);
                                    queryExecuted = true;
                                } catch (NumberFormatException e) {
                                    System.out.println("X ERROR: InvalidNumberFormatException\n   Please enter valid numbers for grade range.\n");
                                    break;
                                }
                                break;
                                
                            case 7:
                                // View All Grades
                                List<Grade> allGrades = gradeManager.getGradeHistory();
                                queryResults = new ArrayList<>(allGrades);
                                queryExecuted = true;
                                
                                // Debug: Show total grades available
                                if (allGrades.isEmpty() && gradeManager.getGradeCount() > 0) {
                                    System.out.println("⚠ Warning: getGradeHistory() returned empty but getGradeCount() = " + gradeManager.getGradeCount());
                                    System.out.println("   This may indicate a synchronization issue.");
                                    System.out.println();
                                }
                                break;
                                
                            default:
                                System.out.println("X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-8).\n");
                                break;
                        }
                        
                        // Display results only if query was executed
                        if (queryExecuted && queryResults != null) {
                            if (!queryResults.isEmpty()) {
                                displayQueryResults(queryResults, studentManager);
                            } else {
                                System.out.println("No grades found matching the query criteria.");
                                System.out.println();
                            }
                        }
                        
                    } catch (NumberFormatException e) {
                        System.out.println();
                        System.out.println("X ERROR: InvalidNumberFormatException\n   Please enter valid numbers for grade/date ranges.\n");
                        System.out.println();
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
                        System.out.println();
                    }
                    break;
                case 15:
                    try {
                        // Initialize scheduler (singleton pattern - create once)
                        if (!schedulerInitialized) {
                            taskSchedulerRef[0] = new TaskScheduler(studentManager, gradeManager);
                            schedulerInitialized = true;
                            // Register thread pool with performance monitor (ScheduledPool uses 3 threads)
                            performanceMonitor.registerThreadPool("ScheduledPool", null, 3);
                            // Set TaskScheduler reference for task count tracking
                            performanceMonitor.setTaskScheduler(taskSchedulerRef[0]);
                        }
                        
                        TaskScheduler taskScheduler = taskSchedulerRef[0];
                        
                        System.out.println("SCHEDULE AUTOMATED TASKS");
                        System.out.println("_______________________________________________");
                        System.out.println();
                        
                        // Display active tasks
                        List<ScheduledTask> activeTasks = taskScheduler.getActiveTasks();
                        System.out.println("Current Scheduled Tasks: " + activeTasks.size() + " active");
                        System.out.println();
                        
                        if (!activeTasks.isEmpty()) {
                            System.out.println("ACTIVE SCHEDULES");
                            System.out.println("_______________________________________________");
                            for (ScheduledTask task : activeTasks) {
                                String scheduleLabel = "";
                                switch (task.getScheduleType()) {
                                    case DAILY: scheduleLabel = "[DAILY]"; break;
                                    case HOURLY: scheduleLabel = "[HOURLY]"; break;
                                    case WEEKLY: scheduleLabel = "[WEEKLY]"; break;
                                }
                                
                                System.out.println(scheduleLabel + " " + task.getTaskName());
                                System.out.println("  Schedule: " + task.getScheduleDescription());
                                System.out.println("  Last Run: " + task.getFormattedLastRunTime());
                                System.out.println("  Next Run: " + task.getFormattedNextRunTime());
                                
                                String statusSymbol = "";
                                String statusText = "";
                                switch (task.getLastStatus()) {
                                    case SUCCESS:
                                        statusSymbol = "✓";
                                        statusText = "Success";
                                        break;
                                    case RUNNING:
                                        statusSymbol = "⚡";
                                        statusText = "Running";
                                        break;
                                    case FAILED:
                                        statusSymbol = "✗";
                                        statusText = "Failed";
                                        break;
                                    case PENDING:
                                        statusSymbol = "○";
                                        statusText = "Pending";
                                        break;
                                }
                                System.out.println("  Status: " + statusSymbol + " " + statusText);
                                if (task.getLastExecutionDuration() > 0) {
                                    System.out.println("  Duration: " + task.getLastExecutionDuration() + "ms");
                                }
                                System.out.println();
                            }
                        }
                        
                        System.out.println("Add New Scheduled Task:");
                        System.out.println("1. Daily GPA Recalculation");
                        System.out.println("2. Hourly Statistics Cache Refresh");
                        System.out.println("3. Weekly Batch Report Generation");
                        System.out.println("4. Daily Database Backup");
                        System.out.println("5. Custom Schedule");
                        System.out.println("6. Cancel");
                        System.out.println();
                        
                        System.out.print("Select task (1-6): ");
                        int taskChoice;
                        try {
                            taskChoice = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-6).\n");
                            scanner.nextLine();
                            break;
                        }
                        
                        if (taskChoice == 6) {
                            break;
                        }
                        
                        String taskName = "";
                        ScheduledTask.ScheduleType scheduleType = null;
                        int hour = 0;
                        int minute = 0;
                        int dayOfWeek = 1;
                        
                        switch (taskChoice) {
                            case 1:
                                taskName = "Daily GPA Recalculation";
                                scheduleType = ScheduledTask.ScheduleType.DAILY;
                                break;
                            case 2:
                                taskName = "Hourly Statistics Cache Refresh";
                                scheduleType = ScheduledTask.ScheduleType.HOURLY;
                                break;
                            case 3:
                                taskName = "Weekly Batch Report Generation";
                                scheduleType = ScheduledTask.ScheduleType.WEEKLY;
                                break;
                            case 4:
                                taskName = "Daily Database Backup";
                                scheduleType = ScheduledTask.ScheduleType.DAILY;
                                break;
                            case 5:
                                System.out.println("Custom schedules not yet implemented.");
                                System.out.println();
                                break;
                            default:
                                System.out.println("X ERROR: Invalid task choice\n");
                                break;
                        }
                        
                        if (taskName.isEmpty() || scheduleType == null) {
                            break;
                        }
                        
                        System.out.println();
                        System.out.println("CONFIGURE: " + taskName);
                        System.out.println("_______________________________________________");
                        System.out.println();
                        
                        // Get execution time
                        if (scheduleType == ScheduledTask.ScheduleType.HOURLY) {
                            System.out.print("Enter minute (0-59): ");
                            minute = scanner.nextInt();
                            scanner.nextLine();
                            hour = 0; // Not used for hourly
                        } else {
                            System.out.print("Enter hour (0-23): ");
                            hour = scanner.nextInt();
                            scanner.nextLine();
                            System.out.print("Enter minute (0-59): ");
                            minute = scanner.nextInt();
                            scanner.nextLine();
                            
                            if (scheduleType == ScheduledTask.ScheduleType.WEEKLY) {
                                System.out.println();
                                System.out.println("Day of Week:");
                                System.out.println("1. Monday");
                                System.out.println("2. Tuesday");
                                System.out.println("3. Wednesday");
                                System.out.println("4. Thursday");
                                System.out.println("5. Friday");
                                System.out.println("6. Saturday");
                                System.out.println("7. Sunday");
                                System.out.println();
                                System.out.print("Select day (1-7): ");
                                dayOfWeek = scanner.nextInt();
                                scanner.nextLine();
                            }
                        }
                        
                        System.out.println();
                        
                        // Get scope (for tasks that need it)
                        String scope = "All Students";
                        if (taskChoice == 1 || taskChoice == 3) {
                            System.out.println("Target Students:");
                            System.out.println("1. All Students");
                            System.out.println("2. Honors Students Only");
                            System.out.println("3. Students with Grade Changes");
                            System.out.println();
                            System.out.print("Select scope (1-3): ");
                            int scopeChoice = scanner.nextInt();
                            scanner.nextLine();
                            
                            switch (scopeChoice) {
                                case 1: scope = "All Students"; break;
                                case 2: scope = "Honors Students Only"; break;
                                case 3: scope = "Students with Grade Changes"; break;
                            }
                        }
                        
                        System.out.println();
                        
                        // Get thread count (for parallel tasks)
                        int threadCount = 1;
                        if (taskChoice == 1 || taskChoice == 3) {
                            int taskStudentCount = studentManager.getStudentCount();
                            int recommended = Math.min(8, Math.max(2, taskStudentCount / 5));
                            System.out.println("Recommended: " + recommended + " threads for " + taskStudentCount + " students.");
                            System.out.print("Enter thread count (1-8): ");
                            threadCount = scanner.nextInt();
                            scanner.nextLine();
                            if (threadCount < 1 || threadCount > 8) {
                                threadCount = recommended;
                            }
                        }
                        
                        System.out.println();
                        
                        // Get notification settings
                        System.out.println("Notification Settings:");
                        System.out.println("1. Email summary on completion");
                        System.out.println("2. Log to file only");
                        System.out.println("3. Both");
                        System.out.println();
                        System.out.print("Select option (1-3): ");
                        int notifChoice = scanner.nextInt();
                        scanner.nextLine();
                        
                        boolean emailNotif = (notifChoice == 1 || notifChoice == 3);
                        boolean logToFile = (notifChoice == 2 || notifChoice == 3);
                        String notificationEmail = null;
                        
                        if (emailNotif) {
                            System.out.print("Enter notification email: ");
                            notificationEmail = scanner.nextLine();
                            // Validate email
                            try {
                                ValidationUtils.validateEmail(notificationEmail);
                            } catch (InvalidStudentDataException e) {
                                System.out.println(e.getMessage());
                                System.out.println();
                                break;
                            }
                        }
                        
                        System.out.println();
                        System.out.println("TASK CONFIGURATION SUMMARY");
                        System.out.println("_______________________________________________");
                        System.out.println("Task: " + taskName);
                        System.out.println("Schedule: " + 
                            (scheduleType == ScheduledTask.ScheduleType.DAILY ? "Every day at " + String.format("%02d:%02d", hour, minute) :
                             scheduleType == ScheduledTask.ScheduleType.HOURLY ? "Every hour at :" + String.format("%02d", minute) :
                             "Every " + new String[]{"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}[dayOfWeek] + " at " + String.format("%02d:%02d", hour, minute)));
                        if (taskChoice == 1 || taskChoice == 3) {
                            System.out.println("Scope: " + scope + " (" + 
                                (scope.equals("All Students") ? studentManager.getStudentCount() : 
                                 scope.equals("Honors Students Only") ? 
                                    studentManager.getStudentsList().stream().filter(s -> s.getStudentType().equals("Honors")).count() : 
                                    studentManager.getStudentCount()) + ")");
                            System.out.println("Threads: " + threadCount + " (parallel execution)");
                        }
                        System.out.println("Notifications: " + 
                            (emailNotif && logToFile ? "Email + Log file" :
                             emailNotif ? "Email" : "Log file"));
                        if (emailNotif) {
                            System.out.println("Recipient: " + notificationEmail);
                        }
                        System.out.println();
                        
                        System.out.print("Confirm schedule? (Y/N): ");
                        String confirm = scanner.nextLine().trim().toUpperCase();
                        
                        if (!confirm.equals("Y")) {
                            System.out.println("Schedule cancelled.");
                            System.out.println();
                            break;
                        }
                        
                        // Create and schedule task
                        ScheduledTask task = new ScheduledTask(taskName, scheduleType, hour, minute);
                        if (scheduleType == ScheduledTask.ScheduleType.WEEKLY) {
                            task.setDayOfWeek(dayOfWeek);
                        }
                        task.setScope(scope);
                        task.setThreadCount(threadCount);
                        task.setNotificationEmail(notificationEmail);
                        task.setEmailNotification(emailNotif);
                        task.setLogToFile(logToFile);
                        
                        ScheduledTask scheduled = taskScheduler.scheduleTask(task);
                        
                        System.out.println();
                        System.out.println("✓ Task scheduled successfully!");
                        System.out.println("Task ID: " + scheduled.getTaskId());
                        System.out.println("Scheduler Thread: " + taskScheduler.getSchedulerStatus());
                        System.out.println("Next Execution: " + scheduled.getFormattedNextRunTime());
                        System.out.println("Initial Delay: " + scheduled.getCountdown());
                        System.out.println();
                        System.out.println("The task will run automatically in the background.");
                        System.out.println();
                        
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
                        System.out.println();
                    }
                    break;
                case 16:
                    try {
                        // System Performance Monitor (interactive)
                        boolean monitoring = true;
                        while (monitoring) {
                            performanceMonitor.displaySystemPerformance();
                            System.out.print("Press 'Q' to quit, 'R' to refresh: ");
                            String input = scanner.nextLine().trim().toUpperCase();
                            if (input.equals("Q")) {
                                monitoring = false;
                            }
                            // Refresh automatically after 2 seconds if 'R' or any other key
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                monitoring = false;
                            }
                        }
                        System.out.println();
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
                        System.out.println();
                    }
                    break;
                case 17:
                    try {
                        System.out.println("CACHE MANAGEMENT");
                        System.out.println("_______________________________________________");
                        System.out.println();
                        
                        System.out.println("Cache Options:");
                        System.out.println("1. View Cache Statistics");
                        System.out.println("2. View Cache Contents");
                        System.out.println("3. Clear Cache");
                        System.out.println("4. Invalidate by Type");
                        System.out.println("5. Return to Main Menu");
                        System.out.println();
                        
                        System.out.print("Select option (1-5): ");
                        int cacheOption;
                        try {
                            cacheOption = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-5).\n");
                            scanner.nextLine();
                            break;
                        }
                        
                        System.out.println();
                        
                        switch (cacheOption) {
                            case 1:
                                // Display cache statistics
                                CacheManager.CacheStatistics stats = cacheManager.getStatistics();
                                
                                System.out.println("CACHE STATISTICS");
                                System.out.println("_______________________________________________");
                                System.out.println("Hit Rate: " + String.format("%.2f", stats.getHitRate()) + "%");
                                System.out.println("Miss Rate: " + String.format("%.2f", stats.getMissRate()) + "%");
                                System.out.println("Total Hits: " + stats.getTotalHits());
                                System.out.println("Total Misses: " + stats.getTotalMisses());
                                System.out.println("Average Hit Time: " + stats.getAverageHitTime() + " ms");
                                System.out.println("Average Miss Time: " + stats.getAverageMissTime() + " ms");
                                System.out.println("Total Entries: " + stats.getTotalEntries() + " / " + 150);
                                System.out.println("Memory Usage: ~" + (stats.getMemoryUsage() / 1024) + " KB");
                                System.out.println("Eviction Count: " + stats.getEvictionCount());
                                System.out.println();
                                break;
                                
                            case 2:
                                // Display cache contents
                                List<Map<String, Object>> contents = cacheManager.getCacheContents();
                                
                                System.out.println("CACHE CONTENTS (" + contents.size() + " entries)");
                                System.out.println("_______________________________________________");
                                System.out.println();
                                
                                if (contents.isEmpty()) {
                                    System.out.println("Cache is empty.");
                                    System.out.println();
                                } else {
                                    System.out.printf("%-30s | %-15s | %-20s | %-20s | %-10s\n", 
                                        "Key", "Type", "Created", "Last Accessed", "Accesses");
                                    System.out.println("_______________________________________________");
                                    
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    for (Map<String, Object> entry : contents) {
                                        String key = (String) entry.get("key");
                                        String type = (String) entry.get("type");
                                        LocalDateTime createdAt = (LocalDateTime) entry.get("createdAt");
                                        LocalDateTime lastAccessed = (LocalDateTime) entry.get("lastAccessed");
                                        int accessCount = (Integer) entry.get("accessCount");
                                        
                                        // Truncate key if too long
                                        if (key.length() > 28) {
                                            key = key.substring(0, 25) + "...";
                                        }
                                        
                                        System.out.printf("%-30s | %-15s | %-20s | %-20s | %-10d\n",
                                            key, type, 
                                            createdAt.format(formatter),
                                            lastAccessed.format(formatter),
                                            accessCount);
                                    }
                                    System.out.println();
                                }
                                break;
                                
                            case 3:
                                // Clear cache
                                System.out.print("Are you sure you want to clear the cache? (Y/N): ");
                                String confirm = scanner.nextLine().trim().toUpperCase();
                                if (confirm.equals("Y")) {
                                    cacheManager.clear();
                                    System.out.println("Cache cleared successfully.");
                                    System.out.println();
                                } else {
                                    System.out.println("Cache clear cancelled.");
                                    System.out.println();
                                }
                                break;
                                
                            case 4:
                                // Invalidate by type
                                System.out.println("Invalidate Cache by Type:");
                                System.out.println("1. Student Cache");
                                System.out.println("2. Grade Report Cache");
                                System.out.println("3. Statistics Cache");
                                System.out.println();
                                System.out.print("Select type (1-3): ");
                                int typeChoice;
                                try {
                                    typeChoice = scanner.nextInt();
                                    scanner.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-3).\n");
                                    scanner.nextLine();
                                    break;
                                }
                                
                                CacheManager.CacheType typeToInvalidate = null;
                                switch (typeChoice) {
                                    case 1: typeToInvalidate = CacheManager.CacheType.STUDENT; break;
                                    case 2: typeToInvalidate = CacheManager.CacheType.GRADE_REPORT; break;
                                    case 3: typeToInvalidate = CacheManager.CacheType.STATISTICS; break;
                                    default:
                                        System.out.println("X ERROR: Invalid type choice\n");
                                        break;
                                }
                                
                                if (typeToInvalidate != null) {
                                    cacheManager.invalidateByType(typeToInvalidate);
                                    System.out.println("Cache entries of type " + typeToInvalidate.name() + " invalidated.");
                                    System.out.println();
                                }
                                break;
                                
                            case 5:
                                // Return to main menu
                                break;
                                
                            default:
                                System.out.println("X ERROR: Invalid option\n");
                                break;
                        }
                        
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
                        System.out.println();
                    }
                    break;
                case 18:
                    try {
                        System.out.println("AUDIT TRAIL VIEWER");
                        System.out.println("_______________________________________________");
                        System.out.println();
                        
                        AuditTrailViewer viewer = new AuditTrailViewer(auditLogger);
                        
                        System.out.println("Audit Trail Options:");
                        System.out.println("1. View Recent Entries");
                        System.out.println("2. Search by Date Range");
                        System.out.println("3. Search by Operation Type");
                        System.out.println("4. Search by Thread ID");
                        System.out.println("5. View Statistics");
                        System.out.println("6. Return to Main Menu");
                        System.out.println();
                        
                        System.out.print("Select option (1-6): ");
                        int auditOption;
                        try {
                            auditOption = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-6).\n");
                            scanner.nextLine();
                            break;
                        }
                        
                        System.out.println();
                        
                        switch (auditOption) {
                            case 1:
                                // View recent entries
                                System.out.print("Enter number of entries to display (default 50): ");
                                String countInput = scanner.nextLine().trim();
                                int count = countInput.isEmpty() ? 50 : Integer.parseInt(countInput);
                                
                                List<AuditLogger.EnhancedAuditEntry> recent = viewer.getRecentEntries(count);
                                
                                System.out.println("RECENT AUDIT ENTRIES (" + recent.size() + " entries)");
                                System.out.println("_______________________________________________");
                                System.out.println();
                                
                                if (recent.isEmpty()) {
                                    System.out.println("No audit entries found.");
                                    System.out.println();
                                } else {
                                    System.out.printf("%-20s | %-8s | %-20s | %-30s | %-8s | %-10s\n",
                                        "Timestamp", "Thread", "Operation Type", "User Action", "Time(ms)", "Status");
                                    System.out.println("_______________________________________________");
                                    
                                    DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    for (AuditLogger.EnhancedAuditEntry entry : recent) {
                                        LocalDateTime time = LocalDateTime.parse(
                                            entry.getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                        
                                        System.out.printf("%-20s | %-8d | %-20s | %-30s | %-8d | %-10s\n",
                                            time.format(displayFormatter),
                                            entry.getThreadId(),
                                            entry.getOperationType(),
                                            entry.getUserAction().length() > 28 ? 
                                                entry.getUserAction().substring(0, 25) + "..." : entry.getUserAction(),
                                            entry.getExecutionTime(),
                                            entry.isSuccess() ? "SUCCESS" : "FAILED");
                                    }
                                    System.out.println();
                                }
                                break;
                                
                            case 2:
                                // Search by date range
                                System.out.print("Enter start date (yyyy-MM-dd): ");
                                String startDateStr = scanner.nextLine().trim();
                                System.out.print("Enter end date (yyyy-MM-dd): ");
                                String endDateStr = scanner.nextLine().trim();
                                
                                try {
                                    LocalDateTime startDate = LocalDateTime.parse(startDateStr + "T00:00:00");
                                    LocalDateTime endDate = LocalDateTime.parse(endDateStr + "T23:59:59");
                                    
                                    List<AuditLogger.EnhancedAuditEntry> dateEntries = 
                                        auditLogger.readAuditEntries(startDate, endDate);
                                    
                                    System.out.println("SEARCH RESULTS (" + dateEntries.size() + " entries)");
                                    System.out.println("_______________________________________________");
                                    System.out.println();
                                    
                                    if (dateEntries.isEmpty()) {
                                        System.out.println("No entries found for the specified date range.");
                                        System.out.println();
                                    } else {
                                        // Display first 50 entries
                                        for (int i = 0; i < Math.min(50, dateEntries.size()); i++) {
                                            AuditLogger.EnhancedAuditEntry entry = dateEntries.get(i);
                                            System.out.println(entry.getTimestamp() + " | " + 
                                                entry.getOperationType() + " | " + 
                                                entry.getUserAction() + " | " +
                                                (entry.isSuccess() ? "SUCCESS" : "FAILED"));
                                        }
                                        if (dateEntries.size() > 50) {
                                            System.out.println("... and " + (dateEntries.size() - 50) + " more entries");
                                        }
                                        System.out.println();
                                    }
                                } catch (Exception e) {
                                    System.out.println("X ERROR: Invalid date format. Use yyyy-MM-dd");
                                    System.out.println();
                                }
                                break;
                                
                            case 3:
                                // Search by operation type
                                System.out.print("Enter operation type: ");
                                String opType = scanner.nextLine().trim();
                                
                                LocalDateTime endDate = LocalDateTime.now();
                                LocalDateTime startDate = endDate.minusDays(30);
                                List<AuditLogger.EnhancedAuditEntry> allEntries = 
                                    auditLogger.readAuditEntries(startDate, endDate);
                                
                                List<AuditLogger.EnhancedAuditEntry> filtered = 
                                    viewer.filterByOperationType(allEntries, opType);
                                
                                System.out.println("SEARCH RESULTS (" + filtered.size() + " entries)");
                                System.out.println("_______________________________________________");
                                System.out.println();
                                
                                for (AuditLogger.EnhancedAuditEntry entry : filtered) {
                                    System.out.println(entry.getTimestamp() + " | " + 
                                        entry.getUserAction() + " | " +
                                        entry.getExecutionTime() + "ms | " +
                                        (entry.isSuccess() ? "SUCCESS" : "FAILED"));
                                }
                                System.out.println();
                                break;
                                
                            case 4:
                                // Search by thread ID
                                System.out.print("Enter thread ID: ");
                                try {
                                    long threadId = Long.parseLong(scanner.nextLine().trim());
                                    
                                    LocalDateTime endDate2 = LocalDateTime.now();
                                    LocalDateTime startDate2 = endDate2.minusDays(30);
                                    List<AuditLogger.EnhancedAuditEntry> allEntries2 = 
                                        auditLogger.readAuditEntries(startDate2, endDate2);
                                    
                                    List<AuditLogger.EnhancedAuditEntry> filtered2 = 
                                        viewer.filterByThreadId(allEntries2, threadId);
                                    
                                    System.out.println("SEARCH RESULTS (" + filtered2.size() + " entries)");
                                    System.out.println("_______________________________________________");
                                    System.out.println();
                                    
                                    for (AuditLogger.EnhancedAuditEntry entry : filtered2) {
                                        System.out.println(entry.getTimestamp() + " | " + 
                                            entry.getOperationType() + " | " +
                                            entry.getUserAction() + " | " +
                                            entry.getExecutionTime() + "ms");
                                    }
                                    System.out.println();
                                } catch (NumberFormatException e) {
                                    System.out.println("X ERROR: Invalid thread ID");
                                    System.out.println();
                                }
                                break;
                                
                            case 5:
                                // View statistics
                                LocalDateTime endDate3 = LocalDateTime.now();
                                LocalDateTime startDate3 = endDate3.minusDays(7);
                                List<AuditLogger.EnhancedAuditEntry> statsEntries = 
                                    auditLogger.readAuditEntries(startDate3, endDate3);
                                
                                AuditTrailViewer.AuditStatistics stats = 
                                    viewer.calculateStatistics(statsEntries);
                                
                                System.out.println("AUDIT STATISTICS (Last 7 Days)");
                                System.out.println("_______________________________________________");
                                System.out.println("Total Operations: " + stats.getTotalOperations());
                                System.out.println("Successful: " + stats.getSuccessfulOperations() + 
                                    " (" + String.format("%.2f", stats.getSuccessRate()) + "%)");
                                System.out.println("Failed: " + stats.getFailedOperations());
                                System.out.println("Average Execution Time: " + 
                                    String.format("%.2f", stats.getAvgExecutionTime()) + " ms");
                                System.out.println("Min Execution Time: " + stats.getMinExecutionTime() + " ms");
                                System.out.println("Max Execution Time: " + stats.getMaxExecutionTime() + " ms");
                                System.out.println("Average Operations Per Hour: " + 
                                    String.format("%.2f", stats.getAvgOperationsPerHour()));
                                System.out.println();
                                break;
                                
                            case 6:
                                // Return to main menu
                                break;
                                
                            default:
                                System.out.println("X ERROR: Invalid option\n");
                                break;
                        }
                        
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
                        System.out.println();
                    }
                    break;
                case 19:
                    System.out.println("Thank you for using Student Grade Management System!");
                    System.out.println("Goodbye!");
                    break;
                default:
                    try {
                        throw new InvalidMenuChoiceException(
                                "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-19).\n   You entered: " + choice
                        );
                    } catch (InvalidMenuChoiceException e) {
                        System.out.println(
                                "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-19).\n   You entered: " + choice
                        );
                        System.out.println();
                    }
            }
        } while (choice != 19);

        scanner.close();
    }

    /**
     * Formats file size from bytes to human-readable format (B, KB, MB)
     * @param bytes The file size in bytes
     * @return Formatted string (e.g., "1.5 KB", "256 B", "2.3 MB")
     */
    private static String formatFileSize(long bytes) {
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
    
    /**
     * Process imported StudentReport and add grades to the system
     * @return Number of grades successfully imported
     */
    private static int processImportedReport(StudentReport report, StudentManager studentManager, GradeManager gradeManager) {
        if (report == null) {
            return 0;
        }
        
        int importedCount = 0;
        String reportStudentId = report.getStudentId();
        
        // Check if student exists, if not, create it
        try {
            Student reportStudent = studentManager.findStudent(reportStudentId);
            if (reportStudent == null) {
                // Student doesn't exist - would need to create, but we'll skip for now
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
                    System.out.println("⚠ Skipping grade - Student " + reportStudentId + " not found");
                    continue;
                }
                
                // Create subject
                Subject reportSubject;
                if (gradeData.getSubjectType().equalsIgnoreCase("Core")) {
                    reportSubject = new CoreSubject(gradeData.getSubjectName(), "");
                } else {
                    reportSubject = new ElectiveSubject(gradeData.getSubjectName(), "");
                }
                
                // Check if grade already exists (by gradeId)
                boolean gradeExists = false;
                for (Grade existingGrade : gradeManager.getGrades()) {
                    if (existingGrade != null && existingGrade.getGradeId().equals(gradeData.getGradeId())) {
                        gradeExists = true;
                        break;
                    }
                }
                
                if (!gradeExists) {
                    // Create and add grade
                    Grade newGrade = new Grade(reportStudentId, reportSubject, gradeData.getGrade());
                    gradeManager.addGrade(newGrade);
                    importedCount++;
                } else {
                    System.out.println("⚠ Grade " + gradeData.getGradeId() + " already exists, skipping");
                }
                
            } catch (Exception e) {
                System.out.println("⚠ Error importing grade " + gradeData.getGradeId() + ": " + e.getMessage());
            }
        }
        
        return importedCount;
    }
    
    /**
     * Query grades by Student ID
     */
    private static List<Grade> queryGradesByStudentId(GradeManager gradeManager, String studentId) {
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
    private static List<Grade> queryGradesBySubjectName(GradeManager gradeManager, String subjectName) {
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
    private static List<Grade> queryGradesBySubjectType(GradeManager gradeManager, String subjectType) {
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
    private static List<Grade> queryGradesByRange(GradeManager gradeManager, double minGrade, double maxGrade) {
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
    private static List<Grade> queryGradesByDateRange(GradeManager gradeManager, String startDate, String endDate) {
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
     * Query grades with combined filters
     */
    private static List<Grade> queryGradesWithCombinedFilters(GradeManager gradeManager, Scanner scanner, StudentManager studentManager) {
        System.out.println("Combined Filters (leave empty to skip filter):");
        System.out.println();
        
        System.out.print("Student ID: ");
        String filterStudentId = scanner.nextLine().trim();
        
        System.out.print("Subject Name: ");
        String filterSubjectName = scanner.nextLine().trim();
        
        System.out.print("Subject Type (Core/Elective): ");
        String filterSubjectType = scanner.nextLine().trim();
        
        System.out.print("Minimum Grade (0-100): ");
        String minGradeStr = scanner.nextLine().trim();
        double minGrade = minGradeStr.isEmpty() ? 0.0 : Double.parseDouble(minGradeStr);
        
        System.out.print("Maximum Grade (0-100): ");
        String maxGradeStr = scanner.nextLine().trim();
        double maxGrade = maxGradeStr.isEmpty() ? 100.0 : Double.parseDouble(maxGradeStr);
        
        System.out.print("Start Date (YYYY-MM-DD): ");
        String filterStartDate = scanner.nextLine().trim();
        
        System.out.print("End Date (YYYY-MM-DD): ");
        String filterEndDate = scanner.nextLine().trim();
        
        System.out.println();
        
        List<Grade> results = new ArrayList<>();
        
        for (Grade grade : gradeManager.getGradeHistory()) {
            boolean matches = true;
            
            // Student ID filter
            if (!filterStudentId.isEmpty() && !grade.getStudentId().equalsIgnoreCase(filterStudentId)) {
                matches = false;
            }
            
            // Subject Name filter
            if (!filterSubjectName.isEmpty() && !grade.getSubject().getSubjectName().equalsIgnoreCase(filterSubjectName)) {
                matches = false;
            }
            
            // Subject Type filter
            if (!filterSubjectType.isEmpty() && !grade.getSubject().getSubjectType().equalsIgnoreCase(filterSubjectType)) {
                matches = false;
            }
            
            // Grade Range filter
            if (grade.getGrade() < minGrade || grade.getGrade() > maxGrade) {
                matches = false;
            }
            
            // Date Range filter
            String gradeDate = grade.getDate();
            if (!filterStartDate.isEmpty() && gradeDate.compareTo(filterStartDate) < 0) {
                matches = false;
            }
            if (!filterEndDate.isEmpty() && gradeDate.compareTo(filterEndDate) > 0) {
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
    private static void displayQueryResults(List<Grade> results, StudentManager studentManager) {
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
                grade.getSubject().getSubjectName().length() > 10 ? grade.getSubject().getSubjectName().substring(0, 7) + "..." : grade.getSubject().getSubjectName(),
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

    // Class for displaying the Main Menu
    public static void displayMainMenu(TaskScheduler taskScheduler, StatisticsDashboard dashboard) {
        System.out.println("||=============================================================||");
        System.out.println("||             STUDENT GRADE MANAGEMENT - MAIN MENU            ||");
        System.out.println("||=============================================================||");
        System.out.println();

        System.out.println("STUDENT MANAGEMENT");
        System.out.println("1. Add Student (with validation)");
        System.out.println("2. View Students");
        System.out.println("3. Record Grade");
        System.out.println("4. View Grade Report");
        System.out.println();

        System.out.println("FILE OPERATIONS");
        System.out.println("5. Export Grade Report (CSV/JSON/Binary)");
        System.out.println("6. Import Data (Multi-format support) [ENHANCED]");
        System.out.println("7. Bulk Import Grades");
        System.out.println();

        System.out.println("ANALYTICS & REPORTING");
        System.out.println("8. Calculate Student GPA");
        System.out.println("9. View Class Statistics");
        System.out.println("10. Real-Time Statistics Dashboard [NEW]");
        System.out.println("11. Generate Batch Reports [NEW]");
        System.out.println();

        System.out.println("SEARCH & QUERY");
        System.out.println("12. Search Students (Advanced) [ENHANCED]");
        System.out.println("13. Pattern-Based Search [NEW]");
        System.out.println("14. Query Grade History [NEW]");
        System.out.println();

        System.out.println("ADVANCED FEATURES");
        System.out.println("15. Schedule Automated Tasks [NEW]");
        System.out.println("16. View System Performance [NEW]");
        System.out.println("17. Cache Management [NEW]");
        System.out.println("18. Audit Trail Viewer [NEW]");
        System.out.println();

        System.out.println("19. Exit");
        System.out.println();
    }
}
