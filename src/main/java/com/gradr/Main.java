package com.gradr;

import com.gradr.exceptions.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws StudentNotFoundException, CSVParseException {
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

            try {
                System.out.print("Enter choice: ");
                choice = scanner.nextInt();
                scanner.nextLine();
                System.out.println();
            } catch (InputMismatchException e) {
                System.out.println(
                        "X ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-10).\n"
                );
                scanner.nextLine();
                break;
            }

            switch (choice) {
                case 1:
                    try{
                        System.out.println("ADD STUDENT");
                        System.out.println("_______________________________________________");
                        System.out.println();

                        System.out.print("Enter student name: ");
                        String name = scanner.nextLine();

                        System.out.print("Enter student age: ");
                        int age = scanner.nextInt();
                        scanner.nextLine();

                        System.out.print("Enter student email: ");
                        String email = scanner.nextLine();

                        System.out.print("Enter student phone: ");
                        String phone = scanner.nextLine();
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

                        if (studentCount <= 5) {
                            for (int i = 0; i < studentCount; i++) {
                                Student s = studentManager.getStudents()[i];

                                s.displayStudentDetails();
                                System.out.println("----------------------------------------------------------------------------------------------------");
                            }
                        } else {
                            // Because I have to display 5 students if students are more than 5. 3 Regular students and
                            // 2 honors students
                            for (int i = 0; i < studentCount && studentDisplayCount < 5; i++) {
                                Student s = studentManager.getStudents()[i];

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
                        int subjectTypeChoice;

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
                        int gradeInput;

                        try {
                            gradeInput = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println(
                                    "\nX ERROR: InvalidGradeException\n   Please enter a valid number (0-100).\n"
                            );
                            scanner.nextLine();
                            break;
                        }

                        grade = new Grade(studentId, subject, gradeInput);

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
                            throw new InvalidGradeException(
                                    "X ERROR: InvalidGradeException\n   Grade must be between 0 and 100.\n   You entered: " + gradeInput + "\n"
                            );
                        }

                    } catch (StudentNotFoundException | InvalidMenuChoiceException | InvalidGradeException e) {
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

                    // Get student using ID and display student details

                    try {
                        student = studentManager.findStudent(studentId);

                        //If there is a student associated with the ID, continue, else
                        // display an error message
                        if (student != null) {
                            // Checking if student has grades recorded
                            boolean hasGrades = false;
                            for (Grade studentGrade : gradeManager.getGrades()) {
                                // Using the condition, studentGrade != null, so it doesn't throw an error when
                                // the student has no grades recorded for the display of student
                                // details
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

                        } else {
                            System.out.println("Invalid Student ID. Student with this ID does not exist");
                            System.out.println();
                            break;
                        }
                    } catch (StudentNotFoundException e) {
                        System.out.println(e.getMessage());
                        System.out.println();
                    }

                    break;
                case 5:
                    System.out.println("EXPORT GRADE REPORT");
                    System.out.println("_______________________________________________");
                    System.out.println();

                    System.out.print("Enter Student ID: ");
                    studentId = scanner.nextLine();
                    System.out.println();

                    try {
                        student = studentManager.findStudent(studentId);

                        System.out.printf("Student: %s - %s\n", student.getStudentId(), student.getName());
                        System.out.printf("Type: %s Student\n", student.getStudentType());
                        // Grades are added for subjects so this works for the number of grades
                        System.out.printf("Total Grades: %d\n", student.getEnrolledSubjectsCount());
                        System.out.println();

                        System.out.println("Export options:");
                        System.out.println("1. Summary Report (overview only)");
                        System.out.println("2. Detailed Report (all grades)");
                        System.out.println("3. Both");
                        System.out.println();

                        System.out.print("Select option (1-3): ");
                        int exportOption;

                        try {
                            exportOption = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-3).\n");
                            scanner.nextLine();
                            break;
                        }

                        if (exportOption >= 1 && exportOption <= 3) {
                            System.out.print("Enter filename (without extension): ");
                            String fileName = scanner.nextLine();
                            System.out.println();

                            try{
                                FileExporter exporter = new FileExporter(fileName);
                                StringBuilder content= new StringBuilder();

                                content.append("==================================================\n");
                                content.append("                   GRADE REPORT                   \n");
                                content.append("==================================================\n\n");

                                // Added option 3 for when user wants BOTH
                                if (exportOption == 1 || exportOption == 3) {
                                    // SUMMARY REPORT
                                    content.append("                SUMMARY REPORT                \n\n");
                                    content.append(String.format("Student ID: %s\n", student.getStudentId()));
                                    content.append(String.format("Name: %s\n", student.getName()));
                                    content.append(String.format("Type: %s\n", student.getStudentType()));
                                    content.append(String.format("Total Subjects: %d\n", student.getEnrolledSubjectsCount()));

                                    // Adding student summary
                                    double average = student.calculateAverageGrade();
                                    content.append(String.format("Overall Average: %.2f\n", average));

                                    // Performance analysis
                                    content.append("\nPerformance Analysis:\n");
                                    if (average >= 85) {
                                        content.append("- Excellent performance\n");
                                    } else if (average >= 70) {
                                        content.append("- Good performance\n");
                                    } else if (average >= 50) {
                                        content.append("- Satisfactory performance\n");
                                    } else {
                                        content.append("- Needs improvement\n");
                                    }
                                    content.append("\n");
                                }

                                if (exportOption == 2 || exportOption == 3) {
                                    if (exportOption == 3) {
                                        content.append("==================================================\n");
                                    }

                                    content.append("                DETAILED REPORT                \n\n");
                                    content.append(String.format("Student ID: %s\n", student.getStudentId()));
                                    content.append(String.format("Name: %s\n", student.getName()));
                                    content.append(String.format("Type: %s\n", student.getStudentType()));

                                    content.append("All Grades:\n");
                                    content.append("==================================================\n");
                                    content.append(gradeManager.viewGradesByStudent(studentId));

                                    content.append("\n");
                                    double average = student.calculateAverageGrade();
                                    content.append(String.format("Overall Average: %.2f\n", average));

                                }

                                content.append("==================================================\n");
                                content.append("                  End of Report                 \n\n");
                                content.append("==================================================\n");

                                exporter.exportGradeToTXT(content.toString());

                                System.out.println("Report exported successfully!");
                                System.out.printf("File: %s.txt\n", fileName);
                                System.out.println("Location: ./reports/");
                                System.out.println("Size: 2.4 KB"); // change to the size of the file
                                System.out.printf("Contains: %d grades, averages, performance summary\n", student.getEnrolledSubjectsCount()); // change to what it actually contains
                                System.out.println();

                            } catch (FileExportException e) {
                                throw new FileExportException(
                                        "X ERROR: FileExportException\n   Failed to export file"

                                );
                            }
                        } else {
                            throw new InvalidMenuChoiceException(
                                    "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-3).\n   You entered: " + exportOption
                            );
                        }
                    } catch (StudentNotFoundException | InvalidMenuChoiceException | FileExportException e) {
                        System.out.println(e.getMessage());
                        System.out.println();
                    }

                    break;
                case 6:
                    System.out.println("CALCULATE STUDENT GPA");
                    System.out.println("_______________________________________________");
                    System.out.println();

                    System.out.print("Enter Student ID: ");
                    studentId = scanner.nextLine();
                    System.out.println();

                    try {
                        student = studentManager.findStudent(studentId);

                        if (student == null) {
                            System.out.println("Invalid ID. Student with this ID does not exist");
                            System.out.println();
                            break;
                        }

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
                        System.out.println(e.getMessage());
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

                        // If there are no rows in the CSV file, break out of the case
                        if (gradeData.isEmpty()) {
                            System.out.println("No valid data found in CSV file.");
                            System.out.println();
                            break;
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

                            // data[0] because student id is the first element in the data (String) array
                            Student studentCheck = studentManager.findStudent(data[0]);

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
                            try {
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

                    } catch (IOException e) {
                        throw new CSVParseException(
                                "X ERROR: CSVParseException\\n Please check the file exists in ./imports/ directory."
                        );
                    } catch (Exception e) {
                        System.out.println("Error processing CSV file: " + e.getMessage());
                        System.out.println();
                    }

                    break;
                case 8:
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
                case 9:
                    System.out.println("SEARCH STUDENTS");
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
                                    System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number.\n");
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
                                    System.out.println("\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number.\n");
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
                                            "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-2).\n   You entered: " + typeChoice
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
                        } else {// Throw a custom input menu choice exception here
                            throw new InvalidMenuChoiceException(
                                    "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-4).\n   You entered: " + searchOption
                            );
                        }
                    } catch (StudentNotFoundException | InvalidMenuChoiceException e) {
                        System.out.println(e.getMessage());
                        System.out.println();
                    }
                    break;
                case 10:
                    System.out.println("Thank you for using Student Grade Management System!");
                    System.out.println("Goodbye!");
                    break;
                default:
                    try {
                        throw new InvalidMenuChoiceException(
                                "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-10).\n   You entered: " + choice
                        );
                    } catch (InvalidMenuChoiceException e) {
                        System.out.println(
                                "X ERROR: InvalidMenuChoiceException\n   Please select a valid option (1-10).\n   You entered: " + choice
                        );
                        System.out.println();
                    }
            }
        } while (choice != 10);

        scanner.close();
    }

    // Class for displaying the Main Menu
    public static void displayMainMenu() {
        System.out.println("||=============================================||");
        System.out.println("||     STUDENT GRADE MANAGEMENT - MAIN MENU    ||");
        System.out.println("||=============================================||");
        System.out.println();

        System.out.println("1. Add Student");
        System.out.println("2. View Students");
        System.out.println("3. Record Grade");
        System.out.println("4. View Grade Report");
        System.out.println("5. Export Grade Report [NEW]");
        System.out.println("6. Calculate Student GPA [NEW]");
        System.out.println("7. Bulk Import Grades [NEW]");
        System.out.println("8. View Class Statistics [NEW]");
        System.out.println("9. Search Students [NEW]");
        System.out.println("10. Exit");
        System.out.println();
    }

}
