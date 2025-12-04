package com.gradr;

class RegularStudent extends Student {
    private double passingGrade = 50.0;

    RegularStudent(String name, int age, String email, String phone) {
        setName(name);
        setAge(age);
        setEmail(email);
        setPhone(phone);
    }

    // Passing gradeManager as an argument here to get the average grade for display
    @Override
    public void displayStudentDetails() {
        // Display student details
        System.out.printf("%-8s | %-23s | %-18s | %-17.1f%% | %20s\n",
                getStudentId(), getName(), getStudentType(), calculateAverageGrade(), getStatus());
        System.out.printf("%-8s | Enrolled Subjects: %s | Passing Grade: %s\n",
                "", getEnrolledSubjectsCount(), getPassingGrade());
    }

    @Override
    public String getStudentType() {
        return "Regular";
    }

    @Override
    public double getPassingGrade() {
        return passingGrade;
        // return 50.0;
    }
}