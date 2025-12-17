package com.gradr;

abstract class Student {
    private String studentId;
    private String name;
    private int age;
    private String email;
    private String phone;
    private String status;

    static int studentCounter;

    // Adding the gradeCalculator to be able to access the student's grades from here
    // Depends on IGradeCalculator abstraction (Dependency Inversion Principle)
    private IGradeCalculator gradeCalculator;

    // Adding a setter to be able to get an instance of gradeCalculator in the student class
    // This is used after student has been created in Main
    // Accepts abstraction, not concrete class (DIP)
    public void setGradeCalculator(IGradeCalculator gradeCalculator) {
        this.gradeCalculator = gradeCalculator;
    }
    
    // Legacy method for backward compatibility
    @Deprecated
    public void setGradeManager(GradeManager gradeManager) {
        this.gradeCalculator = gradeManager;
    }

    Student() {
        setStudentId();
        setStatus();
    }

    abstract void displayStudentDetails();

    abstract String getStudentType();

    abstract double getPassingGrade();

    // Using the grade calculator instance here (depends on abstraction)
    public double calculateAverageGrade() {
        // Using the calculateOverallAverage method from IGradeCalculator to get the average of
        // student marks
        if (gradeCalculator == null) return 0.0;
        return gradeCalculator.calculateOverallAverage(studentId);
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
    // Uses IGradeReader abstraction (would need to be added as separate dependency or use calculator)
    public int getEnrolledSubjectsCount() {
        // For now, we'll need to pass this through the calculator
        // In a more complete refactoring, we'd inject IGradeReader separately
        if (gradeCalculator == null) return 0;
        // Workaround: cast to GradeManager to access this method
        // In production code, we'd extend IGradeCalculator interface
        if (gradeCalculator instanceof GradeManager) {
            return ((GradeManager) gradeCalculator).getEnrolledSubjectsCount(studentId);
        }
        return 0;
    }
}