package com.gradr;

abstract class Student {
    private String studentId;
    private String name;
    private int age;
    private String email;
    private String phone;
    private String status;

    static int studentCounter;

    // Adding the gradeManager to be able to access the student's grades from here
    private GradeManager gradeManager;

    // Adding a setter to be able to get an instance of gradeManager in the student class
    // This is used after student has been created in Main
    public void setGradeManager(GradeManager gradeManager) {
        this.gradeManager = gradeManager;
    }

    Student() {
        setStudentId();
        setStatus();
    }

    abstract void displayStudentDetails();

    abstract String getStudentType();

    abstract double getPassingGrade();

    // Using the grade manager instance here
    public double calculateAverageGrade() {
        // Using the calculateOverallAverage method from GradeManager to get the average of
        // student marks
        if (gradeManager == null) return 0.0;
        return gradeManager.calculateOverallAverage(studentId);
    }

    public boolean isPassing(double averageGrade) {
        return averageGrade >= getPassingGrade();
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }

    public String getStudentId() {
        return studentId;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStatus() {
        this.status = "Active";
    }

    public void setStudentId() {
        // Question: Why does ++studentCounter work but studentCounter++ does not
        studentId = String.format("STU%03d", ++studentCounter);
    }

    // Getting the number of subjects the student is enrolled in
    public int getEnrolledSubjectsCount() {
        if (gradeManager == null) return 0;
        return gradeManager.getEnrolledSubjectsCount(studentId);
    }
}