package com.gradr;

abstract class Subject {
    private String subjectName;
    private String subjectCode;

    Subject() {

    }

    // Getters
    public String getSubjectName() {
        return subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    // Setters
    public void setSubjectName(String name) {
        subjectName = name;
    }

    public void setSubjectCode(String code) {
        subjectCode = code;
    }

    abstract void displaySubjectDetails ();

    abstract String getSubjectType();
}