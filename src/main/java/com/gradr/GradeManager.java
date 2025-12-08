package com.gradr;

class GradeManager {
    private Grade[] grades = new Grade[200];
    private int gradeCount;

    public void addGrade(Grade grade){
        grades[gradeCount] = grade;
        gradeCount++;
    }

    public String viewGradesByStudent(String studentId) {
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        int totalCourses = 0;

        for (Grade grade : grades) {
            if (grade == null) continue;

            if (grade.getStudentId().equals(studentId)) {
                totalCourses++;

                if (!found) {
                    sb.append("GRADE HISTORY\n");
                    sb.append("-------------------------------------------------------------------------------------\n");
                    sb.append("GRD ID   | DATE       | SUBJECT          | TYPE       | GRADE\n");
                    sb.append("-------------------------------------------------------------------------------------\n");
                    found = true;
                }

                sb.append(String.format("%-9s | %-10s | %-16s | %-10s | %-5.1f%%\n",
                        grade.getGradeId(),
                        grade.getDate(),
                        grade.getSubject().getSubjectName(),
                        grade.getSubject().getSubjectType(),
                        grade.getGrade()));
            }
        }

        if (!found) {
            sb.append("_______________________________________________\n");
            sb.append("No grades recorded for this student\n");
            sb.append("_______________________________________________\n\n");
        } else {
            sb.append("\n");
            sb.append(String.format("Total Grades: %d\n", totalCourses));
            sb.append(String.format("Core Subjects Average: %.1f%%\n", calculateCoreAverage(studentId)));
            sb.append(String.format("Elective Subjects Average: %.1f%%\n", calculateElectiveAverage(studentId)));
            sb.append(String.format("Overall Average: %.1f%%\n", calculateOverallAverage(studentId)));
        }

        return sb.toString();
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