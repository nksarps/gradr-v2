package com.gradr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentReport - Serializable class for binary export/import
 * Contains all student grade data in a structured format
 */
public class StudentReport implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String studentId;
    private String studentName;
    private String studentType;
    private double overallAverage;
    private String reportType;
    private List<GradeData> grades;
    
    public StudentReport(String studentId, String studentName, String studentType, 
                        double overallAverage, String reportType) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentType = studentType;
        this.overallAverage = overallAverage;
        this.reportType = reportType;
        this.grades = new ArrayList<>();
    }
    
    public void addGrade(GradeData grade) {
        grades.add(grade);
    }
    
    // Getters
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentType() { return studentType; }
    public double getOverallAverage() { return overallAverage; }
    public String getReportType() { return reportType; }
    public List<GradeData> getGrades() { return grades; }
    
    // Setters (for updating student info after import)
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setStudentType(String studentType) { this.studentType = studentType; }
}

/**
 * GradeData - Serializable data class for individual grades
 */
class GradeData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String gradeId;
    private String date;
    private String subjectName;
    private String subjectType;
    private double grade;
    
    public GradeData(String gradeId, String date, String subjectName, 
                    String subjectType, double grade) {
        this.gradeId = gradeId;
        this.date = date;
        this.subjectName = subjectName;
        this.subjectType = subjectType;
        this.grade = grade;
    }
    
    // Getters
    public String getGradeId() { return gradeId; }
    public String getDate() { return date; }
    public String getSubjectName() { return subjectName; }
    public String getSubjectType() { return subjectType; }
    public double getGrade() { return grade; }
}

