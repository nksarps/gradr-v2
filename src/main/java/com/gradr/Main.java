package com.gradr;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        StudentManager studentManager = new StudentManager();
        GradeManager gradeManager = new GradeManager();

        int choice;
        Student student;
        Subject subject;

        do {
            displayMainMenu();

            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();
            System.out.println();

            switch (choice) {
                case 1:
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
                        System.out.println("Invalid student type.");
                        break;
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
                    String studentId = scanner.nextLine();
                    System.out.println();

                    Student foundStudent = studentManager.findStudent(studentId);

                    if (foundStudent == null) {
                        System.out.println("Invalid ID. Student with this ID does not exist");
                        System.out.println();
                        break;
                    }

                    System.out.println("Student Details:");
                    System.out.printf("Name: %s\n", foundStudent.getName());
                    System.out.printf("Type: %s Student\n", foundStudent.getStudentType());
                    System.out.printf("Current Average: %.1f%%\n", foundStudent.calculateAverageGrade());
                    System.out.println();

                    System.out.println("Subject type:");
                    System.out.println("1. Core Subject (Mathematics, English, Science)");
                    System.out.println("2. Elective Subject (Music, Art, Physical Education)\n");

                    System.out.print("Select type (1-2): ");
                    int subjectTypeChoice = scanner.nextInt();
                    scanner.nextLine();

                    // Setting subject type for displaying Available Subjects
                    String subjectType;
                    if (subjectTypeChoice == 1) {
                        subject = new CoreSubject();
                    } else if (subjectTypeChoice == 2) {
                        subject = new ElectiveSubject();
                    } else {
                        System.out.println("Invalid subject type entered");
                        break;
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

                    System.out.print("Select subject: ");
                    int subjectChoice = scanner.nextInt();
                    scanner.nextLine();

                    if (subjectChoice == 1 || subjectChoice == 2 || subjectChoice == 3) {
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
                    } else {
                        System.out.println("Invalid subject choice");
                        break;
                    }

                    System.out.println();

                    System.out.print("Enter grade: ");
                    int gradeInput = scanner.nextInt();
                    scanner.nextLine();

                    // Validating the grade
                    Grade grade = new Grade(studentId, subject, gradeInput);

                    // Used recordGrade because it validates if the grade can be recorded
                    if (grade.recordGrade(gradeInput)) {
                        grade.setGradeId();

                        System.out.println("GRADE CONFIRMATION");
                        System.out.println("_______________________________________________________");
                        System.out.printf("Grade ID: %s\n", grade.getGradeId());
                        System.out.printf("Student: %s - %s\n", studentId, foundStudent.getName());
                        System.out.printf("Subject: %s (%s)\n", subject.getSubjectName(), subject.getSubjectType());
                        System.out.printf("Grade: %.1f%%\n", (double) gradeInput);
                        System.out.printf("Date: %s\n", grade.getDate());
                        System.out.println("______________________________________________________\n");

                        System.out.print("Confirm grade? (Y/N): ");
                        char confirmGrade = scanner.next().charAt(0);

                        if (confirmGrade == 'Y' || confirmGrade == 'N') {
                            if (confirmGrade == 'Y') {
                                gradeManager.addGrade(grade);

                                System.out.println("Grade added successfully.\n");
                            } else {
                                Grade.gradeCounter--;

                                System.out.println("Grade record cancelled\n");
                            }
                        } else {
                            Grade.gradeCounter--;
                            System.out.println("Invalid input.");
                        }
                    } else {
                        System.out.println("Invalid grade entered");
                        break;
                    }

                    break;
                case 4:
                    System.out.println("VIEW GRADE REPORT");
                    System.out.println("_______________________________________________");
                    System.out.println();

                    System.out.print("Enter Student ID: ");
                    String studentIdForReport = scanner.nextLine();
                    System.out.println();

                    // Get student using ID and display student details
                    Student studentForReport = studentManager.findStudent(studentIdForReport);

                    //If there is a student associated with the ID, continue, else
                    // display an error message
                    if (studentForReport != null) {
                        // Checking if student has grades recorded
                        boolean hasGrades = false;
                        for (Grade studentGrade : gradeManager.getGrades()) {
                            // Using the condition, studentGrade != null, so it doesn't throw an error when
                            // the student has no grades recorded for the display of student
                            // details
                            if (studentGrade != null && studentGrade.getStudentId().equals(studentIdForReport)) {
                                hasGrades = true;
                                break;
                            }
                        }

                        // Printing out different student details for when student has grades recorded and
                        // when the student does not
                        System.out.printf("Student: %s - %s\n", studentForReport.getStudentId(), studentForReport.getName());
                        System.out.printf("Type: %s Student\n", studentForReport.getStudentType());

                        if (hasGrades) {
                            System.out.printf("Current Average: %.1f%%\n", gradeManager.calculateOverallAverage(studentIdForReport));

                            // Check if student is passing (average grade is greater than passing grade)
                             boolean isPassing = studentForReport.isPassing(gradeManager.calculateOverallAverage(studentIdForReport));

                             if (isPassing) {
                                 System.out.print("Status: PASSING\n");
                             } else {
                                 System.out.print("Status: FAILING\n");
                             }

                            gradeManager.viewGradesByStudent(studentIdForReport);

                            // Displaying the student's performance summary
                            System.out.println("Performance Summary:");
                            if (isPassing) {
                                System.out.println("Passing all core subjects");
                                System.out.printf("Meeting passing grade requirement (%.0f%%)\n", studentForReport.getPassingGrade());
                                System.out.println();
                            } else {
                                System.out.println("Failing some subjects");
                                System.out.printf("Failing to meet passing grade requirement (%.0f%%)\n", studentForReport.getPassingGrade());
                                System.out.println();
                            }
                        } else {
                            // For when student has no grades recorded
                            System.out.printf("Passing Grade: %.0f%%\n", studentForReport.getPassingGrade());

                            gradeManager.viewGradesByStudent(studentIdForReport);
                        }
                        System.out.println();

                    } else {
                        System.out.println("Invalid Student ID. Student with this ID does not exist");
                        System.out.println();
                        break;
                    }

                    break;
                case 5:
                    System.out.println("Thank you for using Student Grade Management System!");
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    System.out.println();
            }
        } while (choice != 5);

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
