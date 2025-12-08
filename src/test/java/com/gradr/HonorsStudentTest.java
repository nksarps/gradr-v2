package com.gradr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HonorsStudentTest {

    private HonorsStudent honorsStudent;
    private GradeManager gradeManager;

    @BeforeEach
    public void setUp() {
        honorsStudent = new HonorsStudent("Jane Doe", 21, "jane@example.com", "987-654-3210");
        gradeManager = new GradeManager();
        honorsStudent.setGradeManager(gradeManager);
    }

    @Test
    public void testGetPassingGrade() {
        assertEquals(60.0, honorsStudent.getPassingGrade(), 0.001);
    }

    @Test
    public void testGetStudentType() {
        assertEquals("Honors", honorsStudent.getStudentType());
    }

    @Test
    public void testCheckHonorsEligibility_Eligible() {
        // Add grades to achieve average >= 85.0
        Subject math = new CoreSubject("Mathematics", "MATH101");
        Grade grade1 = new Grade(honorsStudent.getStudentId(), math, 90.0);
        gradeManager.addGrade(grade1);

        Subject english = new CoreSubject("English", "ENG101");
        Grade grade2 = new Grade(honorsStudent.getStudentId(), english, 80.0);
        gradeManager.addGrade(grade2);

        // Average: (90 + 80) / 2 = 85.0
        assertEquals("Yes", honorsStudent.checkHonorsEligibility());
    }

    @Test
    public void testCheckHonorsEligibility_NotEligible() {
        // Add grades to achieve average < 85.0
        Subject math = new CoreSubject("Mathematics", "MATH101");
        Grade grade1 = new Grade(honorsStudent.getStudentId(), math, 80.0);
        gradeManager.addGrade(grade1);

        Subject english = new CoreSubject("English", "ENG101");
        Grade grade2 = new Grade(honorsStudent.getStudentId(), english, 84.0);
        gradeManager.addGrade(grade2);

        // Average: (80 + 84) / 2 = 82.0
        assertEquals("No", honorsStudent.checkHonorsEligibility());
    }

    @Test
    public void testCheckHonorsEligibility_NoGrades() {
        // No grades added, average should be 0.0
        assertEquals("No", honorsStudent.checkHonorsEligibility());
    }
}