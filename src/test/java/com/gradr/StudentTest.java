package com.gradr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Student methods:
 * - calculateAverageGrade()
 * - isPassing()
 * - getEnrolledSubjectsCount()
 */
class StudentTest {
    private GradeManager gradeManager;
    private StudentManager studentManager;
    private RegularStudent regularStudent;
    private HonorsStudent honorsStudent;
    private Subject mathSubject;
    private Subject englishSubject;
    private Subject scienceSubject;
    private Subject musicSubject;

    @BeforeEach
    void setUp() {
        // Reset static counters
        Student.studentCounter = 0;
        Grade.gradeCounter = 0;

        // Initialize managers
        gradeManager = new GradeManager();
        studentManager = new StudentManager();

        // Create students
        regularStudent = new RegularStudent("John Doe", 16, "john@test.com", "123-456-7890");
        regularStudent.setGradeManager(gradeManager);
        studentManager.addStudent(regularStudent);

        honorsStudent = new HonorsStudent("Jane Smith", 17, "jane@test.com", "098-765-4321");
        honorsStudent.setGradeManager(gradeManager);
        studentManager.addStudent(honorsStudent);

        // Create subjects
        mathSubject = new CoreSubject("Mathematics", "MATH101");
        englishSubject = new CoreSubject("English", "ENG101");
        scienceSubject = new CoreSubject("Science", "SCI101");
        musicSubject = new ElectiveSubject("Music", "MUS101");
    }

    // calculateAverageGrade() Tests

    @Test
    @DisplayName("calculateAverageGrade: Returns 0.0 when no grades recorded")
    void testCalculateAverageGradeNoGrades() {
        double average = regularStudent.calculateAverageGrade();
        assertEquals(0.0, average, 0.01);
    }


    @Test
    @DisplayName("calculateAverageGrade: Calculates correct average for single grade")
    void testCalculateAverageGradeSingleGrade() {
        Grade grade = new Grade(regularStudent.getStudentId(), mathSubject, 85.0);
        gradeManager.addGrade(grade);

        double average = regularStudent.calculateAverageGrade();
        assertEquals(85.0, average, 0.01);
    }

    @Test
    @DisplayName("calculateAverageGrade: Calculates correct average for multiple grades")
    void testCalculateAverageGradeMultipleGrades() {
        Grade math = new Grade(regularStudent.getStudentId(), mathSubject, 80.0);
        Grade english = new Grade(regularStudent.getStudentId(), englishSubject, 90.0);
        Grade science = new Grade(regularStudent.getStudentId(), scienceSubject, 70.0);

        gradeManager.addGrade(math);
        gradeManager.addGrade(english);
        gradeManager.addGrade(science);

        double average = regularStudent.calculateAverageGrade();
        assertEquals(80.0, average, 0.01);
    }

    @Test
    @DisplayName("calculateAverageGrade: Includes both core and elective subjects")
    void testCalculateAverageGradeCoreAndElective() {
        Grade math = new Grade(regularStudent.getStudentId(), mathSubject, 85.0);
        Grade music = new Grade(regularStudent.getStudentId(), musicSubject, 95.0);

        gradeManager.addGrade(math);
        gradeManager.addGrade(music);

        double average = regularStudent.calculateAverageGrade();
        assertEquals(90.0, average, 0.01);
    }

    @Test
    @DisplayName("calculateAverageGrade: Handles perfect scores")
    void testCalculateAverageGradePerfectScores() {
        Grade math = new Grade(regularStudent.getStudentId(), mathSubject, 100.0);
        Grade english = new Grade(regularStudent.getStudentId(), englishSubject, 100.0);

        gradeManager.addGrade(math);
        gradeManager.addGrade(english);

        double average = regularStudent.calculateAverageGrade();
        assertEquals(100.0, average, 0.01);
    }

    @Test
    @DisplayName("calculateAverageGrade: Handles very low scores")
    void testCalculateAverageGradeLowScores() {
        Grade math = new Grade(regularStudent.getStudentId(), mathSubject, 10.0);
        Grade english = new Grade(regularStudent.getStudentId(), englishSubject, 20.0);

        gradeManager.addGrade(math);
        gradeManager.addGrade(english);

        double average = regularStudent.calculateAverageGrade();
        assertEquals(15.0, average, 0.01);
    }

    @Test
    @DisplayName("calculateAverageGrade: Only includes grades for specific student")
    void testCalculateAverageGradeMultipleStudents() {
        Grade math1 = new Grade(regularStudent.getStudentId(), mathSubject, 80.0);
        gradeManager.addGrade(math1);

        Grade math2 = new Grade(honorsStudent.getStudentId(), mathSubject, 95.0);
        gradeManager.addGrade(math2);

        double regularAverage = regularStudent.calculateAverageGrade();
        double honorsAverage = honorsStudent.calculateAverageGrade();

        assertEquals(80.0, regularAverage, 0.01);
        assertEquals(95.0, honorsAverage, 0.01);
    }

    @Test
    @DisplayName("calculateAverageGrade: Handles decimal grades correctly")
    void testCalculateAverageGradeDecimalGrades() {
        Grade math = new Grade(regularStudent.getStudentId(), mathSubject, 85.5);
        Grade english = new Grade(regularStudent.getStudentId(), englishSubject, 92.3);
        Grade science = new Grade(regularStudent.getStudentId(), scienceSubject, 78.7);

        gradeManager.addGrade(math);
        gradeManager.addGrade(english);
        gradeManager.addGrade(science);

        double average = regularStudent.calculateAverageGrade();
        assertEquals(85.5, average, 0.01);
    }

    // isPassing() Tests

    @Test
    @DisplayName("isPassing: Regular student passes with grade >= 50%")
    void testIsPassingRegularStudentPassing() {
        assertTrue(regularStudent.isPassing(50.0));
        assertTrue(regularStudent.isPassing(75.0));
        assertTrue(regularStudent.isPassing(100.0));
    }

    @Test
    @DisplayName("isPassing: Regular student fails with grade < 50%")
    void testIsPassingRegularStudentFailing() {
        assertFalse(regularStudent.isPassing(49.9));
        assertFalse(regularStudent.isPassing(25.0));
        assertFalse(regularStudent.isPassing(0.0));
    }

    @Test
    @DisplayName("isPassing: Honors student passes with grade >= 60%")
    void testIsPassingHonorsStudentPassing() {
        assertTrue(honorsStudent.isPassing(60.0));
        assertTrue(honorsStudent.isPassing(85.0));
        assertTrue(honorsStudent.isPassing(100.0));
    }

    @Test
    @DisplayName("isPassing: Honors student fails with grade < 60%")
    void testIsPassingHonorsStudentFailing() {
        assertFalse(honorsStudent.isPassing(59.9));
        assertFalse(honorsStudent.isPassing(50.0));
        assertFalse(honorsStudent.isPassing(0.0));
    }

    @Test
    @DisplayName("isPassing: Tests boundary values for regular student")
    void testIsPassingRegularStudentBoundary() {
        assertTrue(regularStudent.isPassing(50.0));
        assertFalse(regularStudent.isPassing(49.999));
    }

    @Test
    @DisplayName("isPassing: Tests boundary values for honors student")
    void testIsPassingHonorsStudentBoundary() {
        assertTrue(honorsStudent.isPassing(60.0));
        assertFalse(honorsStudent.isPassing(59.999));
    }

    @Test
    @DisplayName("isPassing: Works with calculateAverageGrade integration")
    void testIsPassingIntegrationWithAverageGrade() {
        Grade math1 = new Grade(regularStudent.getStudentId(), mathSubject, 60.0);
        Grade english1 = new Grade(regularStudent.getStudentId(), englishSubject, 70.0);
        gradeManager.addGrade(math1);
        gradeManager.addGrade(english1);

        double regularAvg = regularStudent.calculateAverageGrade();
        assertTrue(regularStudent.isPassing(regularAvg));

        Grade math2 = new Grade(honorsStudent.getStudentId(), mathSubject, 80.0);
        Grade english2 = new Grade(honorsStudent.getStudentId(), englishSubject, 90.0);
        gradeManager.addGrade(math2);
        gradeManager.addGrade(english2);

        double honorsAvg = honorsStudent.calculateAverageGrade();
        assertTrue(honorsStudent.isPassing(honorsAvg));
    }

    // getEnrolledSubjectsCount() Tests

    @Test
    @DisplayName("getEnrolledSubjectsCount: Returns 0 when no grades recorded")
    void testGetEnrolledSubjectsCountNoGrades() {
        int count = regularStudent.getEnrolledSubjectsCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("getEnrolledSubjectsCount: Returns 1 for single subject")
    void testGetEnrolledSubjectsCountSingleSubject() {
        Grade math = new Grade(regularStudent.getStudentId(), mathSubject, 85.0);
        gradeManager.addGrade(math);

        int count = regularStudent.getEnrolledSubjectsCount();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("getEnrolledSubjectsCount: Returns correct count for multiple subjects")
    void testGetEnrolledSubjectsCountMultipleSubjects() {
        Grade math = new Grade(regularStudent.getStudentId(), mathSubject, 85.0);
        Grade english = new Grade(regularStudent.getStudentId(), englishSubject, 90.0);
        Grade science = new Grade(regularStudent.getStudentId(), scienceSubject, 78.0);

        gradeManager.addGrade(math);
        gradeManager.addGrade(english);
        gradeManager.addGrade(science);

        int count = regularStudent.getEnrolledSubjectsCount();
        assertEquals(3, count);
    }

    @Test
    @DisplayName("getEnrolledSubjectsCount: Counts both core and elective subjects")
    void testGetEnrolledSubjectsCountCoreAndElective() {
        Grade math = new Grade(regularStudent.getStudentId(), mathSubject, 85.0);
        Grade music = new Grade(regularStudent.getStudentId(), musicSubject, 95.0);

        gradeManager.addGrade(math);
        gradeManager.addGrade(music);

        int count = regularStudent.getEnrolledSubjectsCount();
        assertEquals(2, count);
    }

    @Test
    @DisplayName("getEnrolledSubjectsCount: Only counts grades for specific student")
    void testGetEnrolledSubjectsCountMultipleStudents() {
        Grade math1 = new Grade(regularStudent.getStudentId(), mathSubject, 80.0);
        Grade english1 = new Grade(regularStudent.getStudentId(), englishSubject, 75.0);
        gradeManager.addGrade(math1);
        gradeManager.addGrade(english1);

        Grade math2 = new Grade(honorsStudent.getStudentId(), mathSubject, 95.0);
        gradeManager.addGrade(math2);

        int regularCount = regularStudent.getEnrolledSubjectsCount();
        int honorsCount = honorsStudent.getEnrolledSubjectsCount();

        assertEquals(2, regularCount);
        assertEquals(1, honorsCount);
    }

    @Test
    @DisplayName("getEnrolledSubjectsCount: Handles duplicate subject entries")
    void testGetEnrolledSubjectsCountDuplicateSubjects() {
        Grade math1 = new Grade(regularStudent.getStudentId(), mathSubject, 70.0);
        Grade math2 = new Grade(regularStudent.getStudentId(), mathSubject, 85.0);

        gradeManager.addGrade(math1);
        gradeManager.addGrade(math2);

        int count = regularStudent.getEnrolledSubjectsCount();
        assertEquals(2, count);
    }

    @Test
    @DisplayName("getEnrolledSubjectsCount: Updates dynamically when grades are added")
    void testGetEnrolledSubjectsCountDynamicUpdate() {
        assertEquals(0, regularStudent.getEnrolledSubjectsCount());

        Grade math = new Grade(regularStudent.getStudentId(), mathSubject, 80.0);
        gradeManager.addGrade(math);
        assertEquals(1, regularStudent.getEnrolledSubjectsCount());

        Grade english = new Grade(regularStudent.getStudentId(), englishSubject, 85.0);
        gradeManager.addGrade(english);
        assertEquals(2, regularStudent.getEnrolledSubjectsCount());

        Grade science = new Grade(regularStudent.getStudentId(), scienceSubject, 90.0);
        gradeManager.addGrade(science);
        assertEquals(3, regularStudent.getEnrolledSubjectsCount());
    }
}