package com.gradr;

class GradeManager {
    private Grade[] grades = new Grade[200];
    private int gradeCount;

    public void addGrade(Grade grade){
        grades[gradeCount] = grade;
        gradeCount++;
    }

    public void viewGradesByStudent(String studentId) {
        // Print out all the grades by the student with the provided studentID
        // The variable found is for checking if the student has grades added
        boolean found = false;
        int totalCourses = 0;

        for (Grade grade : grades) {
            // To prevent the code from throwing an error when the grades array is empty
            if (grade == null) continue;

            if (grade.getStudentId().equals(studentId)) {
                totalCourses++;

                // Printing the table header once for when a grade for found
                // for the student. It doesn't print again because found is set to true
                if (!found) {
                    System.out.println("GRADE HISTORY");
                    System.out.println("-------------------------------------------------------------------------------------");
                    System.out.println("GRD ID   | DATE       | SUBJECT          | TYPE       | GRADE");
                    System.out.println("-------------------------------------------------------------------------------------");
                    found = true;
                }

                System.out.printf("%-9s | %-10s | %-16s | %-10s | %-5.1f%%\n",
                        grade.getGradeId(),
                        grade.getDate(),
                        grade.getSubject().getSubjectName(),
                        grade.getSubject().getSubjectType(),
                        grade.getGrade());
            }
        }

        if (!found) {
            System.out.println("_______________________________________________");
            System.out.println("No grades recorded for this student");
            System.out.println("_______________________________________________");
            System.out.println();
        } else {
            System.out.println();
            System.out.printf("Total Grades: %d\n", totalCourses);
            System.out.printf("Core Subjects Average: %.1f%%\n", calculateCoreAverage(studentId));
            System.out.printf("Elective Subjects Average: %.1f%%\n", calculateElectiveAverage(studentId));
            System.out.printf("Overall Average: %.1f%%\n", calculateOverallAverage(studentId));
            System.out.println();

        }
    }

    public double calculateCoreAverage(String studentId) {
        double gradeSum = 0;
        int totalCourses = 0;

        for (Grade grade : grades) {
            if (grade == null) continue;

            if (grade.getStudentId().equals(studentId)) {
                if (grade.getSubject().getSubjectType().equals("Core")) {
                    gradeSum += grade.getGrade();
                    totalCourses++;
                }
            }
        }

        // To prevent the method from throwing an error by dividing by 0
        // if there are no core subjects
        if (totalCourses == 0) return 0.0;

        return gradeSum / totalCourses;
    }

    public double calculateElectiveAverage(String studentId) {
        double gradeSum = 0;
        int totalCourses = 0;

        for (Grade grade : grades) {
            if (grade == null) continue;

            if (grade.getStudentId().equals(studentId)) {
                if (grade.getSubject().getSubjectType().equals("Elective")) {
                    gradeSum += grade.getGrade();
                    totalCourses++;
                }
            }
        }

        // To prevent the method from throwing an error by dividing by 0
        // if there are no elective subjects
        if (totalCourses == 0) return 0.0;

        return gradeSum / totalCourses;
    }

    public double calculateOverallAverage(String studentId) {
        double gradeSum = 0;
        int totalCourses = 0;

        for (Grade grade : grades) {
            // To prevent an error if the grades array is empty
            if (grade == null) continue;

            if (grade.getStudentId().equals(studentId)) {
                gradeSum += grade.getGrade();
                totalCourses++;
            }
        }

        // To prevent the method from throwing an error by dividing by 0
        // if there are no subjects
        if (totalCourses == 0) return 0.0;

        return gradeSum / totalCourses;
    }

    public int getGradeCount() {
        return gradeCount;
    }

    public Grade[] getGrades() {
        return grades;
    }

    // Getting the number of enrolled subjects for students
    // This is to be used in the Student class where we display the
    // number of enrolled subjects in the students table
    public int getEnrolledSubjectsCount(String studentId) {
        int subjectCount = 0;

        for (Grade grade : grades) {
            if (grade == null) continue;

            if (grade.getStudentId().equals(studentId)) {
                subjectCount++;
            }
        }

        return subjectCount;
    }
}