package com.gradr;

import com.gradr.exceptions.InvalidGradeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradeTest {
    Subject subject = new CoreSubject("Mathematics", "MAT001");
    private Grade grade = new Grade("STU001", subject, 78);

    @Test
    public void hundredShouldBeAValidGrade() {
        assertDoesNotThrow(() -> {
            boolean result = grade.validateGrade(100);
            assertTrue(result);
        });
    }

    @Test
    public void zeroShouldBeAValidGrade() {
        assertDoesNotThrow(() -> {
            boolean result = grade.validateGrade(0);
            assertTrue(result);
        });
    }

    @Test
    public void ninetyNineShouldBeAValidGrade() {
        assertDoesNotThrow(() -> {
            boolean result = grade.validateGrade(99);
            assertTrue(result);
        });
    }

    @Test
    public void oneShouldBeAValidGrade() {
        assertDoesNotThrow(() -> {
            boolean result = grade.validateGrade(1);
            assertTrue(result);
        });
    }

    @Test
    public void minusOneShouldNotBeAValidGrade() {
        assertThrows(InvalidGradeException.class, () -> {
            grade.validateGrade(-1);
        });
    }

    @Test
    public void hundredAndOneShouldNotBeAValidGrade() {
        assertThrows(InvalidGradeException.class, () -> {
            grade.validateGrade(101);
        });
    }

    @Test
    public void hundredShouldBeRecorded() {
        assertDoesNotThrow(() -> {
            boolean result = grade.recordGrade(100);
            assertTrue(result);
        });
    }

    @Test
    public void zeroShouldBeRecorded() {
        assertDoesNotThrow(() -> {
            boolean result = grade.recordGrade(0);
            assertTrue(result);
        });
    }

    @Test
    public void ninetyNineShouldBeRecorded() {
        assertDoesNotThrow(() -> {
            boolean result = grade.recordGrade(99);
            assertTrue(result);
        });
    }

    @Test
    public void oneShouldBeRecorded() {
        assertDoesNotThrow(() -> {
            boolean result = grade.recordGrade(1);
            assertTrue(result);
        });
    }

    @Test
    public void fiftyShouldBeRecorded() {
        assertDoesNotThrow(() -> {
            boolean result = grade.recordGrade(50);
            assertTrue(result);
        });
    }

    @Test
    public void minusOneShouldNotBeRecorded() {
        assertThrows(InvalidGradeException.class, () -> {
            grade.recordGrade(-1);
        });
    }

    @Test
    public void hundredAndOneShouldNotBeRecorded() {
        assertThrows(InvalidGradeException.class, () -> {
            grade.recordGrade(101);
        });
    }

    @Test
    public void minusFiftyShouldNotBeRecorded() {
        assertThrows(InvalidGradeException.class, () -> {
            grade.recordGrade(-50);
        });
    }

    @Test
    public void twoHundredShouldNotBeRecorded() {
        assertThrows(InvalidGradeException.class, () -> {
            grade.recordGrade(200);
        });
    }

    @Test
    public void studentIDShouldBeSTU001() {
        String result = grade.getStudentId();
        assertEquals("STU001", result);
    }

    @Test
    public void subjectShouldBeMathematics() {
        String result = grade.getSubject().getSubjectName();
        assertEquals("Mathematics", result);
    }

    @Test
    public void gradeShouldBeSeventyEight() {
        double result = grade.getGrade();
        assertEquals(78, result);
    }
}