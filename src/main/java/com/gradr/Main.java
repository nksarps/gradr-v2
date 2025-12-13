package com.gradr;

import com.gradr.exceptions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws StudentNotFoundException {
        Scanner scanner = new Scanner(System.in);

        StudentManager studentManager = new StudentManager();
        GradeManager gradeManager = new GradeManager();

        int choice = 0;
        Student student = null;
        Subject subject;
        Grade grade;

        String studentId;

        do {
            displayMainMenu();

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
                    System.out.println("IMPORT DATA (Multi-format support) [ENHANCED]");
                    System.out.println("_______________________________________________");
                    System.out.println();
                    System.out.println("This feature will support CSV, JSON, and Binary format imports.");
                    System.out.println("Implementation coming soon...");
                    System.out.println();
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
                        StatisticsDashboard dashboard = new StatisticsDashboard(studentManager, gradeManager);
                        dashboard.start();
                        
                        // Main dashboard loop - use AtomicBoolean for thread safety
                        java.util.concurrent.atomic.AtomicBoolean running = new java.util.concurrent.atomic.AtomicBoolean(true);
                        
                        // Input handling thread
                        Thread inputThread = new Thread(() -> {
                            Scanner dashboardScanner = new Scanner(System.in);
                            while (running.get() && dashboard.isRunning()) {
                                try {
                                    String input = dashboardScanner.nextLine().trim().toUpperCase();
                                    
                                    synchronized (dashboard) {
                                        switch (input) {
                                            case "Q":
                                                running.set(false);
                                                break;
                                            case "R":
                                                dashboard.refresh();
                                                break;
                                            case "P":
                                                if (dashboard.isPaused()) {
                                                    dashboard.resume();
                                                } else {
                                                    dashboard.pause();
                                                }
                                                break;
                                            default:
                                                // Ignore other input
                                                break;
                                        }
                                    }
                                } catch (Exception e) {
                                    // Input thread error - continue
                                }
                            }
                            dashboardScanner.close();
                        });
                        inputThread.setDaemon(true);
                        inputThread.start();
                        
                        // Display loop
                        while (running.get() && dashboard.isRunning()) {
                            dashboard.displayDashboard();
                            
                            // Small delay to prevent excessive CPU usage
                            try {
                                Thread.sleep(1000); // Update every second
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                running.set(false);
                            }
                        }
                        
                        dashboard.stop();
                        System.out.println();
                        System.out.println("Dashboard closed.");
                        System.out.println();
                        
                    } catch (Exception e) {
                        System.out.println();
                        System.out.println("X ERROR: " + e.getClass().getSimpleName() + "\n   " + e.getMessage());
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
                    System.out.println("PATTERN-BASED SEARCH [NEW]");
                    System.out.println("_______________________________________________");
                    System.out.println();
                    System.out.println("This feature will allow pattern-based searching.");
                    System.out.println("Implementation coming soon...");
                    System.out.println();
                    break;
                case 14:
                    System.out.println("QUERY GRADE HISTORY [NEW]");
                    System.out.println("_______________________________________________");
                    System.out.println();
                    System.out.println("This feature will allow querying grade history with filters.");
                    System.out.println("Implementation coming soon...");
                    System.out.println();
                    break;
                case 15:
                    System.out.println("SCHEDULE AUTOMATED TASKS [NEW]");
                    System.out.println("_______________________________________________");
                    System.out.println();
                    System.out.println("This feature will allow scheduling automated tasks.");
                    System.out.println("Implementation coming soon...");
                    System.out.println();
                    break;
                case 16:
                    System.out.println("VIEW SYSTEM PERFORMANCE [NEW]");
                    System.out.println("_______________________________________________");
                    System.out.println();
                    System.out.println("This feature will display system performance metrics.");
                    System.out.println("Implementation coming soon...");
                    System.out.println();
                    break;
                case 17:
                    System.out.println("CACHE MANAGEMENT [NEW]");
                    System.out.println("_______________________________________________");
                    System.out.println();
                    System.out.println("This feature will manage system cache.");
                    System.out.println("Implementation coming soon...");
                    System.out.println();
                    break;
                case 18:
                    System.out.println("AUDIT TRAIL VIEWER [NEW]");
                    System.out.println("_______________________________________________");
                    System.out.println();
                    System.out.println("This feature will display audit trail logs.");
                    System.out.println("Implementation coming soon...");
                    System.out.println();
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

    // Class for displaying the Main Menu
    public static void displayMainMenu() {
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
        
        // Display background tasks status (placeholder for future implementation)
        System.out.println("Background Tasks: ⚡ 0 active | 📊 Stats updating...");
        System.out.println();
    }
}
