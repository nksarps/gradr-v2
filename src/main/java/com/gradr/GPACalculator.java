package com.gradr;

public class GPACalculator {
    private GradeManager gradeManager;

    public GPACalculator(GradeManager gradeManager) {
        this.gradeManager = gradeManager;
    }

    /**
     * Converts a percentage grade to GPA on a 4.0 scale
     * @param percentage The percentage grade
     * @return The GPA value
     */
    public double convertPercentageToGPA(double percentage) {
        if (percentage >= 93) return 4.0;      // A
        else if (percentage >= 90) return 3.7;  // A-
        else if (percentage >= 87) return 3.3;  // B+
        else if (percentage >= 83) return 3.0;  // B
        else if (percentage >= 80) return 2.7;  // B-
        else if (percentage >= 77) return 2.3;  // C+
        else if (percentage >= 73) return 2.0;  // C
        else if (percentage >= 70) return 1.7;  // C-
        else if (percentage >= 67) return 1.3;  // D+
        else if (percentage >= 60) return 1.0;  // D
        else return 0.0;                        // F
    }

    /**
     * Converts a percentage grade to a letter grade
     * @param percentage The percentage grade
     * @return The letter grade
     */
    public String getLetterGrade(double percentage) {
        if (percentage >= 93) return "A";
        else if (percentage >= 90) return "A-";
        else if (percentage >= 87) return "B+";
        else if (percentage >= 83) return "B";
        else if (percentage >= 80) return "B-";
        else if (percentage >= 77) return "C+";
        else if (percentage >= 73) return "C";
        else if (percentage >= 70) return "C-";
        else if (percentage >= 67) return "D+";
        else if (percentage >= 60) return "D";
        else return "F";
    }

    /**
     * Calculates the cumulative GPA for a student
     * @param studentId The student's ID
     * @return The cumulative GPA on a 4.0 scale
     */
    public double calculateCumulativeGPA(String studentId) {
        double totalGradePoints = 0.0;
        int totalSubjects = 0;

        for (Grade grade : gradeManager.getGrades()) {
            if (grade == null) continue;

            if (grade.getStudentId().equals(studentId)) {
                double percentage = grade.getGrade();
                double gradePoint = convertPercentageToGPA(percentage);
                totalGradePoints += gradePoint;
                totalSubjects++;
            }
        }

        if (totalSubjects == 0) return 0.0;
        return totalGradePoints / totalSubjects;
    }

    /**
     * Calculates the class rank for a student
     * @param studentId The student's ID
     * @param studentManager The student manager to access all students
     * @return The student's rank (1 = highest GPA)
     */
    public int calculateClassRank(String studentId, StudentManager studentManager) {
        double studentGPA = calculateCumulativeGPA(studentId);
        int rank = 1;

        for (int i = 0; i < studentManager.getStudentCount(); i++) {
            Student student = studentManager.getStudents()[i];
            // Checking if the student is not null and is not the student whose rank we are calculating
            if (student != null && !student.getStudentId().equals(studentId)) {
                double otherGPA = calculateCumulativeGPA(student.getStudentId());
                if (otherGPA > studentGPA) {
                    // Increase rank by 1 is student has a higher GPA
                    rank++;
                }
            }
        }

        return rank;
    }

    /**
     * Calculates the class average cumulative GPA across all students.
     * @param studentManager The student manager containing enrolled students.
     * @return The class average GPA on a 4.0 scale.
     */
    public double calculateClassAverageGPA(StudentManager studentManager) {
        double totalGPA = 0.0;
        int count = 0;

        for (int i = 0; i < studentManager.getStudentCount(); i++) {
            Student s = studentManager.getStudents()[i];
            if (s != null) {
                double gpa = calculateCumulativeGPA(s.getStudentId());
                totalGPA += gpa;
                count++;
            }
        }

        if (count == 0) return 0.0;
        return totalGPA / count;
    }


    /**
     * Displays a formatted GPA report for a student matching the required format
     * @param studentId The student's ID
     * @param student The student object
     * @param studentManager The student manager
     * @return A formatted string containing the GPA report
     */
    public String generateGPAReport(String studentId, Student student, StudentManager studentManager) {
        StringBuilder report = new StringBuilder();

        // Student header info
        report.append("_______________________________________________\n\n");
        report.append(String.format("Student: %s - %s\n", studentId, student.getName()));
        report.append(String.format("Type: %s Student\n", student.getStudentType()));
        report.append(String.format("Overall Average: %.2f%%\n\n", student.calculateAverageGrade()));

        // GPA Calculation table
        report.append("GPA CALCULATION (4.0 Scale)\n");
        report.append("_______________________________________________\n");
        report.append(String.format("%-15s | %-6s | %-11s\n", "Subject", "Grade", "GPA Points"));
        report.append("_______________________________________________\n");

        // Display each grade with GPA
        for (Grade grade : gradeManager.getGrades()) {
            if (grade == null) continue;

            if (grade.getStudentId().equals(studentId)) {
                double gradePercent = grade.getGrade();
                String letterGrade = getLetterGrade(gradePercent);
                double gpaPoints = convertPercentageToGPA(gradePercent);

                report.append(String.format("%-15s | %3.0f%% | %.1f (%s)\n",
                        grade.getSubject().getSubjectName(),
                        gradePercent,
                        gpaPoints,
                        letterGrade));
            }
        }

        report.append("_______________________________________________\n\n");

        // Summary statistics
        double cumulativeGPA = calculateCumulativeGPA(studentId);
        String letterGrade = getLetterGrade(student.calculateAverageGrade());
        int rank = calculateClassRank(studentId, studentManager);

        report.append(String.format("Cumulative GPA: %.2f / 4.0\n", cumulativeGPA));
        report.append(String.format("Letter Grade: %s\n", letterGrade));
        report.append(String.format("Class Rank: %d of %d\n\n", rank, studentManager.getStudentCount()));

        // Performance Analysis
        report.append("Performance Analysis:\n");

        if (cumulativeGPA >= 3.5) {
            report.append("Excellent performance (3.5+ GPA)\n");
        }

        if (student.calculateAverageGrade() >= student.getPassingGrade()) {
            report.append("Meeting grade requirements\n");
        }

        double averageClassGPA = calculateClassAverageGPA(studentManager);
        if (cumulativeGPA >= averageClassGPA) {
            report.append(String.format("Above class average (%.2f GPA)\n", averageClassGPA));
        }

        return report.toString();
    }
}