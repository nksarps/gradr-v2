package com.gradr;

class HonorsStudent extends Student {
    private double passingGrade = 60.0;
    private boolean honorsEligible;

    HonorsStudent(String name, int age, String email, String phone) {
        setName(name);
        setAge(age);
        setEmail(email);
        setPhone(phone);
    }

    @Override
    void displayStudentDetails() {
        // Display student details + Honors status
        System.out.printf("%-8s | %-23s | %-18s | %-17.1f%% | %s\n",
                getStudentId(), getName(), getStudentType(), calculateAverageGrade(), getStatus());
        if (checkHonorsEligibility().equals("Yes")) {
            System.out.printf("%-8s | Enrolled Subjects: %s | Passing Grade: %s | Honors Eligible\n",
                    "", getName(), getPassingGrade());
        } else {
            System.out.printf("%-8s | Enrolled Subjects: %s | Passing Grade: %s\n",
                    "", getEnrolledSubjectsCount(), getPassingGrade());
        }

    }

    @Override
    public String getStudentType() {
        return "Honors";
    }

    @Override
    public double getPassingGrade() {
        return passingGrade;
        // return 60.0;
    }

    public String checkHonorsEligibility() {
        double averageGrade = calculateAverageGrade();

        if (averageGrade >= 85.0) {
            return "Yes";
        }
        return "No";
    }
}