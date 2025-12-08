package com.gradr;

import com.gradr.exceptions.InvalidGradeException;

interface Gradable {
    boolean recordGrade(double grade) throws InvalidGradeException;
    boolean validateGrade(double grade) throws InvalidGradeException;
}