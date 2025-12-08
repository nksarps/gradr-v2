package com.gradr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.gradr.exceptions.InvalidGradeException;

/**
 * Test suite for GradeManager class methods
 */
class GradeManagerTest {
    private GradeManager gradeManager;
    private Student student1;
    private Student student2;

    private Subject math;
    private Subject english;
    private Subject science;
    private Subject music;
    private Subject art;

    @BeforeEach
    void setUp() {
        gradeManager = new GradeManager();

        // Create test students
        student1 = new RegularStudent("John Doe", 16, "john@test.com", "1234567890");
        student2 = new HonorsStudent("Jane Smith", 17, "jane@test.com", "0987654321");

        // Create test subjects
        math = new CoreSubject("Mathematics", "MATH101");
        english = new CoreSubject("English", "ENG101");
        science = new CoreSubject("Science", "SCI101");
        music = new ElectiveSubject("Music", "MUS101");
        art = new ElectiveSubject("Art", "ART101");
    }

    // Tests for addGrade()

    @Test
    @DisplayName("Add a valid grade successfully")
    void testAddValidGrade() {
        Grade grade = new Grade(student1.getStudentId(), math, 85.0);
        gradeManager.addGrade(grade);

        assertEquals(1, gradeManager.getGradeCount());
        assertEquals(grade, gradeManager.getGrades()[0]);
    }

    @Test
    @DisplayName("Add multiple grades for different students")
    void testAddMultipleGrades() {
        Grade grade1 = new Grade(student1.getStudentId(), math, 85.0);
        Grade grade2 = new Grade(student1.getStudentId(), english, 90.0);
        Grade grade3 = new Grade(student2.getStudentId(), math, 95.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);
        gradeManager.addGrade(grade3);

        assertEquals(3, gradeManager.getGradeCount());
    }

    @Test
    @DisplayName("Add grades at boundary values (0 and 100)")
    void testAddGradeBoundaryValues() {
        Grade gradeMin = new Grade(student1.getStudentId(), math, 0.0);
        Grade gradeMax = new Grade(student1.getStudentId(), english, 100.0);

        gradeManager.addGrade(gradeMin);
        gradeManager.addGrade(gradeMax);

        assertEquals(2, gradeManager.getGradeCount());
        assertEquals(0.0, gradeManager.getGrades()[0].getGrade());
        assertEquals(100.0, gradeManager.getGrades()[1].getGrade());
    }

    // Tests for calculateCoreAverage()

    @Test
    @DisplayName("Calculate core average with single core subject")
    void testCoreAverageSingleSubject() {
        Grade grade = new Grade(student1.getStudentId(), math, 85.0);
        gradeManager.addGrade(grade);

        double avg = gradeManager.calculateCoreAverage(student1.getStudentId());
        assertEquals(85.0, avg, 0.01);
    }

    @Test
    @DisplayName("Calculate core average with multiple core subjects")
    void testCoreAverageMultipleSubjects() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), science, 85.0));

        double avg = gradeManager.calculateCoreAverage(student1.getStudentId());
        assertEquals(85.0, avg, 0.01);
    }

    @Test
    @DisplayName("Calculate core average when no core subjects exist")
    void testCoreAverageNoCore() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 85.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), art, 90.0));

        double avg = gradeManager.calculateCoreAverage(student1.getStudentId());
        assertEquals(0.0, avg, 0.01);
    }

    @Test
    @DisplayName("Calculate core average with mixed core and elective subjects")
    void testCoreAverageMixedSubjects() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 95.0)); // Should be ignored

        double avg = gradeManager.calculateCoreAverage(student1.getStudentId());
        assertEquals(85.0, avg, 0.01);
    }

    // ========== Tests for calculateElectiveAverage() ==========

    @Test
    @DisplayName("Calculate elective average with single elective subject")
    void testElectiveAverageSingleSubject() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 88.0));

        double avg = gradeManager.calculateElectiveAverage(student1.getStudentId());
        assertEquals(88.0, avg, 0.01);
    }

    @Test
    @DisplayName("Calculate elective average with multiple elective subjects")
    void testElectiveAverageMultipleSubjects() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 85.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), art, 90.0));

        double avg = gradeManager.calculateElectiveAverage(student1.getStudentId());
        assertEquals(87.5, avg, 0.01);
    }

    @Test
    @DisplayName("Calculate elective average when no elective subjects exist")
    void testElectiveAverageNoElectives() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));

        double avg = gradeManager.calculateElectiveAverage(student1.getStudentId());
        assertEquals(0.0, avg, 0.01);
    }

    @Test
    @DisplayName("Calculate elective average with mixed core and elective subjects")
    void testElectiveAverageMixedSubjects() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 95.0)); // Should be ignored
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), art, 90.0));

        double avg = gradeManager.calculateElectiveAverage(student1.getStudentId());
        assertEquals(85.0, avg, 0.01);
    }

    // Tests for calculateOverallAverage()

    @Test
    @DisplayName("Calculate overall average with no grades")
    void testOverallAverageNoGrades() {
        double avg = gradeManager.calculateOverallAverage(student1.getStudentId());
        assertEquals(0.0, avg, 0.01);
    }

    @Test
    @DisplayName("Calculate overall average with single subject")
    void testOverallAverageSingleSubject() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));

        double avg = gradeManager.calculateOverallAverage(student1.getStudentId());
        assertEquals(85.0, avg, 0.01);
    }

    @Test
    @DisplayName("Calculate overall average with multiple subjects")
    void testOverallAverageMultipleSubjects() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 85.0));

        double avg = gradeManager.calculateOverallAverage(student1.getStudentId());
        assertEquals(85.0, avg, 0.01);
    }

    @Test
    @DisplayName("Calculate overall average with both core and elective subjects")
    void testOverallAverageMixedTypes() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), science, 70.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 85.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), art, 95.0));

        double avg = gradeManager.calculateOverallAverage(student1.getStudentId());
        assertEquals(84.0, avg, 0.01);
    }

    @Test
    @DisplayName("Calculate overall average for different students")
    void testOverallAverageDifferentStudents() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), math, 95.0));

        double avg1 = gradeManager.calculateOverallAverage(student1.getStudentId());
        double avg2 = gradeManager.calculateOverallAverage(student2.getStudentId());

        assertEquals(80.0, avg1, 0.01);
        assertEquals(95.0, avg2, 0.01);
    }

    // Tests for getEnrolledSubjectsCount()

    @Test
    @DisplayName("Get enrolled subjects count with no subjects")
    void testEnrolledSubjectsCountNoSubjects() {
        int count = gradeManager.getEnrolledSubjectsCount(student1.getStudentId());
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Get enrolled subjects count with single subject")
    void testEnrolledSubjectsCountSingleSubject() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));

        int count = gradeManager.getEnrolledSubjectsCount(student1.getStudentId());
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Get enrolled subjects count with multiple subjects")
    void testEnrolledSubjectsCountMultipleSubjects() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 88.0));

        int count = gradeManager.getEnrolledSubjectsCount(student1.getStudentId());
        assertEquals(3, count);
    }

    @Test
    @DisplayName("Get enrolled subjects count for specific student only")
    void testEnrolledSubjectsCountSpecificStudent() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), math, 95.0));

        int count1 = gradeManager.getEnrolledSubjectsCount(student1.getStudentId());
        int count2 = gradeManager.getEnrolledSubjectsCount(student2.getStudentId());

        assertEquals(2, count1);
        assertEquals(1, count2);
    }

    // Tests for viewGradesByStudent()

    @Test
    @DisplayName("View grades when student has no grades")
    void testViewGradesNoGrades() {
        String result = gradeManager.viewGradesByStudent(student1.getStudentId());

        assertTrue(result.contains("No grades recorded for this student"));
        assertFalse(result.contains("GRADE HISTORY"));
    }

    @Test
    @DisplayName("View grades with single grade")
    void testViewGradesSingleGrade() {
        Grade grade = new Grade(student1.getStudentId(), math, 85.0);
        grade.setGradeId();
        gradeManager.addGrade(grade);

        String result = gradeManager.viewGradesByStudent(student1.getStudentId());

        assertTrue(result.contains("GRADE HISTORY"));
        assertTrue(result.contains("Mathematics"));
        assertTrue(result.contains("85.0"));
        assertTrue(result.contains("Total Grades: 1"));
    }

    @Test
    @DisplayName("View grades with multiple grades")
    void testViewGradesMultipleGrades() {
        Grade grade1 = new Grade(student1.getStudentId(), math, 85.0);
        Grade grade2 = new Grade(student1.getStudentId(), english, 90.0);
        Grade grade3 = new Grade(student1.getStudentId(), music, 88.0);

        grade1.setGradeId();
        grade2.setGradeId();
        grade3.setGradeId();

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);
        gradeManager.addGrade(grade3);

        String result = gradeManager.viewGradesByStudent(student1.getStudentId());

        assertTrue(result.contains("GRADE HISTORY"));
        assertTrue(result.contains("Mathematics"));
        assertTrue(result.contains("English"));
        assertTrue(result.contains("Music"));
        assertTrue(result.contains("Total Grades: 3"));
    }

    @Test
    @DisplayName("View grades shows correct averages")
    void testViewGradesShowsAverages() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 85.0));

        String result = gradeManager.viewGradesByStudent(student1.getStudentId());

        assertTrue(result.contains("Core Subjects Average:"));
        assertTrue(result.contains("Elective Subjects Average:"));
        assertTrue(result.contains("Overall Average:"));
    }

    @Test
    @DisplayName("View grades only shows grades for specific student")
    void testViewGradesSpecificStudentOnly() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), math, 95.0));

        String result1 = gradeManager.viewGradesByStudent(student1.getStudentId());
        String result2 = gradeManager.viewGradesByStudent(student2.getStudentId());

        assertTrue(result1.contains("Total Grades: 1"));
        assertTrue(result2.contains("Total Grades: 1"));
    }


    // Integration Tests

    @Test
    @DisplayName("Integration: Multiple students with various grades")
    void testIntegrationMultipleStudents() {
        // Student 1 grades
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 88.0));

        // Student 2 grades
        gradeManager.addGrade(new Grade(student2.getStudentId(), math, 95.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), science, 92.0));

        // Verify counts
        assertEquals(5, gradeManager.getGradeCount());
        assertEquals(3, gradeManager.getEnrolledSubjectsCount(student1.getStudentId()));
        assertEquals(2, gradeManager.getEnrolledSubjectsCount(student2.getStudentId()));

        // Verify averages
        double avg1 = gradeManager.calculateOverallAverage(student1.getStudentId());
        double avg2 = gradeManager.calculateOverallAverage(student2.getStudentId());

        assertEquals(87.67, avg1, 0.1);
        assertEquals(93.5, avg2, 0.1);
    }

    @Test
    @DisplayName("Integration: Core and elective averages calculation")
    void testIntegrationCoreElectiveAverages() {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), science, 70.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 85.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), art, 95.0));

        double coreAvg = gradeManager.calculateCoreAverage(student1.getStudentId());
        double electiveAvg = gradeManager.calculateElectiveAverage(student1.getStudentId());
        double overallAvg = gradeManager.calculateOverallAverage(student1.getStudentId());

        assertEquals(80.0, coreAvg, 0.01);
        assertEquals(90.0, electiveAvg, 0.01);
        assertEquals(84.0, overallAvg, 0.01);
    }
}