package com.gradr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradeTest {
    Subject subject = new CoreSubject("Mathematics", "MAT001");
    private Grade grade = new Grade("STU001", subject, 78);

    @Test
    public void hundredShouldBeAValidGrade() {
        boolean result = grade.validateGrade(100);
        Assertions.assertTrue(result);
    }

    @Test
    public void zeroShouldBeAValidGrade() {
        boolean result = grade.validateGrade(0);
        Assertions.assertTrue(result);
    }

    @Test
    public void ninetyNineShouldBeAValidGrade() {
        boolean result = grade.validateGrade(99);
        Assertions.assertTrue(result);
    }

    @Test
    public void oneShouldBeAValidGrade() {
        boolean result = grade.validateGrade(1);
        Assertions.assertTrue(result);
    }

    @Test
    public void minusOneShouldNotBeAValidGrade() {
        boolean result = grade.validateGrade(-1);
        Assertions.assertFalse(result);
    }

    @Test
    public void hundredAndOneShouldNotBeAValidGrade() {
        boolean result = grade.validateGrade(101);
        Assertions.assertFalse(result);
    }

    @Test
    public void hundredShouldBeRecorded() {
        boolean result = grade.recordGrade(100);
        Assertions.assertTrue(result);
    }

    @Test
    public void zeroShouldBeRecorded() {
        boolean result = grade.recordGrade(0);
        Assertions.assertTrue(result);
    }

    @Test
    public void ninetyNineShouldBeRecorded() {
        boolean result = grade.recordGrade(99);
        Assertions.assertTrue(result);
    }

    @Test
    public void oneShouldBeRecorded() {
        boolean result = grade.recordGrade(1);
        Assertions.assertTrue(result);
    }

    @Test
    public void fiftyShouldBeRecorded() {
        boolean result = grade.recordGrade(50);
        Assertions.assertTrue(result);
    }

    @Test
    public void minusOneShouldNotBeRecorded() {
        boolean result = grade.recordGrade(-1);
        Assertions.assertFalse(result);
    }

    @Test
    public void hundredAndOneShouldNotBeRecorded() {
        boolean result = grade.recordGrade(101);
        Assertions.assertFalse(result);
    }

    @Test
    public void minusFiftyShouldNotBeRecorded() {
        boolean result = grade.recordGrade(-50);
        Assertions.assertFalse(result);
    }

    @Test
    public void twoHundredShouldNotBeRecorded() {
        boolean result = grade.recordGrade(200);
        Assertions.assertFalse(result);
    }

    @Test
    public void studentIDShouldBeSTU001() {
        String result = grade.getStudentId();
        Assertions.assertEquals("STU001", result);
    }

    @Test
    public void subjectShouldBeMathematics() {
        String result = grade.getSubject().getSubjectName();
        Assertions.assertEquals("Mathematics", result);
    }

    @Test
    public void gradeShouldBeSeventyEight() {
        double result = grade.getGrade();
        Assertions.assertEquals(78, result);
    }

}