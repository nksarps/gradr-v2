package com.gradr;

class StudentManager {
    private Student[] students = new Student[50];
    private int studentCount = 0;

    // added student to the counter array
    public void addStudent(Student student) {
        students[studentCount] = student;
        studentCount++;
    }

    public Student findStudent(String studentId) {
        for (Student student : students) {
            // To prevent error when wrong ID is provided
            if (student == null) return null;

            if (student.getStudentId().equals(studentId)) {
                return student;
            }
        }
        return null;
    }

    // Returns are students
    public void viewAllStudents() {
        // return students; // Return type was Student[]
        System.out.println(students.toString());
    }

    // produces the number of students
    public int getStudentCount() {
        return studentCount;
    }

    // Getter for students array
    public Student[] getStudents() {
        return students;
    }

    // Calculating the average grade for the whole class to be displayed in the option 2
    // in the main menu
    public double calculateClassAverage() {
        double totalAverage = 0;

        // Calculating the average of all the students added
        for (int i = 0; i < studentCount; i++) {
            totalAverage += students[i].calculateAverageGrade();
        }

        return totalAverage / getStudentCount();
    }
}