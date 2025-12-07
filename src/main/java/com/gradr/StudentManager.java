package com.gradr;

import com.gradr.exceptions.StudentNotFoundException;

class StudentManager {
    private Student[] students = new Student[50];
    private int studentCount = 0;

    // added student to the counter array
    public void addStudent(Student student) {
        students[studentCount] = student;
        studentCount++;
    }

    public Student findStudent(String studentId) throws StudentNotFoundException {
        for (Student student : students) {
            if (student == null) continue;

            if (student.getStudentId().equals(studentId)) {
                return student;
            }
        }

        throw new StudentNotFoundException(
                "X ERROR: StudentNotFoundException\n   Student with ID '" + studentId + "' does not exist"
        );
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