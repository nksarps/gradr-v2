package com.gradr;

class CoreSubject extends Subject {
    private static boolean mandatory = true;

    CoreSubject(){}

    CoreSubject(String subjectName, String subjectCode) {
        setSubjectName(subjectName);
        setSubjectCode(subjectCode);
    }

    @Override
    public void displaySubjectDetails() {
        // Show subject details
    }

    @Override
    public String getSubjectType() {
        return "Core";
    }

    public boolean isMandatory() {
        return true;
    }
}