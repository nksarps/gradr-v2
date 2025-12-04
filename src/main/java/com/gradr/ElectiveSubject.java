package com.gradr;

class ElectiveSubject extends Subject {
    private static boolean mandatory = false;

    ElectiveSubject() {}

    ElectiveSubject(String subjectName, String subjectCode) {
        setSubjectName(subjectName);
        setSubjectCode(subjectCode);
    }

    @Override
    public void displaySubjectDetails() {
        // display subject details
    }

    @Override
    public String getSubjectType() {
        return "Elective";
    }

    public boolean isMandatory() {
        return false;
    }

}