package com.gradr;

import com.gradr.exceptions.InvalidGradeException;

import java.time.LocalDate;

public class Grade implements Gradable {
    static int gradeCounter;

    private String gradeId;
    private String studentId;
    private Subject subject;
    private double grade;
    private String date;

    Grade(String studentId, Subject subject, double grade){
        setStudentId(studentId);
        setSubject(subject);
        setGrade(grade);
        setDate();
    }

    public void setGradeId() {
        gradeId = String.format("GRD%03d", ++gradeCounter);
    }

    public void setDate() {
        date = LocalDate.now().toString();
    }

    // Getters
    public int getGradeCounter() {
        return gradeCounter;
    }

    public String getGradeId() {
        return gradeId;
    }

    public String getStudentId() {
        return studentId;
    }

    public Subject getSubject() {
        return subject;
    }

    public double getGrade() {
        return grade;
    }

    public String getDate() {
        return date;
    }

    // Setters
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    @Override
    public boolean validateGrade(double grade) throws InvalidGradeException {
        if (grade < 0 || grade > 100) {
            throw new InvalidGradeException(
                    "X ERROR: InvalidGradeException\n  Grade must be between 0 and 100"
            );
        }
        return true;
    }

    @Override
    public boolean recordGrade(double grade) throws InvalidGradeException {
        return validateGrade(grade);
    }
}