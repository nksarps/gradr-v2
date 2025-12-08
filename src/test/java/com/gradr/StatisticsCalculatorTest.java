package com.gradr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.gradr.exceptions.StudentNotFoundException;

/**
 * Test suite for StatisticsCalculator class methods
 */
class StatisticsCalculatorTest {
    private StatisticsCalculator statsCalculator;
    private GradeManager gradeManager;
    private StudentManager studentManager;
    private Student student1;
    private Student student2;
    private Student student3;
    private Subject math;
    private Subject english;
    private Subject science;
    private Subject music;
    private Subject art;
    private Subject pe;

    @BeforeEach
    void setUp() {
        gradeManager = new GradeManager();
        studentManager = new StudentManager();
        statsCalculator = new StatisticsCalculator(gradeManager, studentManager);

        // Create test students
        student1 = new RegularStudent("John Doe", 16, "john@test.com", "1234567890");
        student2 = new HonorsStudent("Jane Smith", 17, "jane@test.com", "0987654321");
        student3 = new RegularStudent("Bob Johnson", 16, "bob@test.com", "5555555555");

        student1.setGradeManager(gradeManager);
        student2.setGradeManager(gradeManager);
        student3.setGradeManager(gradeManager);

        studentManager.addStudent(student1);
        studentManager.addStudent(student2);
        studentManager.addStudent(student3);

        // Create test subjects
        math = new CoreSubject("Mathematics", "MATH101");
        english = new CoreSubject("English", "ENG101");
        science = new CoreSubject("Science", "SCI101");
        music = new ElectiveSubject("Music", "MUS101");
        art = new ElectiveSubject("Art", "ART101");
        pe = new ElectiveSubject("Physical Education", "PE101");
    }

    // Since the statistical methods are private, we'll test them through
    // the public generateClassStatistics() method and verify the output

    /**
     * Helper method to extract statistical values from the generated report
     */
    private String generateStatsReport() throws StudentNotFoundException {
        return statsCalculator.generateClassStatistics();
    }

    // Tests for calculateMean()

    @Test
    @DisplayName("Calculate mean with single grade")
    void testMeanSingleGrade() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));

        String report = generateStatsReport();
        assertTrue(report.contains("Mean (Average):       85.0%"));
    }

    @Test
    @DisplayName("Calculate mean with multiple grades")
    void testMeanMultipleGrades() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), science, 70.0));

        String report = generateStatsReport();
        // Mean = (80 + 90 + 70) / 3 = 80.0
        assertTrue(report.contains("Mean (Average):       80.0%"));
    }

    @Test
    @DisplayName("Calculate mean with decimal values")
    void testMeanDecimalValues() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.5));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 92.3));
        gradeManager.addGrade(new Grade(student1.getStudentId(), science, 78.2));

        String report = generateStatsReport();
        // Mean = (85.5 + 92.3 + 78.2) / 3 = 85.33...
        assertTrue(report.contains("Mean (Average):       85.3%"));
    }

    @Test
    @DisplayName("Calculate mean across multiple students")
    void testMeanMultipleStudents() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), math, 90.0));
        gradeManager.addGrade(new Grade(student3.getStudentId(), math, 70.0));

        String report = generateStatsReport();
        // Mean = (80 + 90 + 70) / 3 = 80.0
        assertTrue(report.contains("Mean (Average):       80.0%"));
    }

    // Tests for calculateMedian()

    @Test
    @DisplayName("Calculate median with odd number of grades")
    void testMedianOddCount() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 70.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), science, 90.0));

        String report = generateStatsReport();
        // Sorted: [70, 80, 90], median = 80
        assertTrue(report.contains("Median:               80.0%"));
    }

    @Test
    @DisplayName("Calculate median with even number of grades")
    void testMedianEvenCount() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 70.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 80.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), science, 85.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), music, 90.0));

        String report = generateStatsReport();
        // Sorted: [70, 80, 85, 90], median = (80 + 85) / 2 = 82.5
        assertTrue(report.contains("Median:               82.5%"));
    }

    @Test
    @DisplayName("Calculate median with single grade")
    void testMedianSingleGrade() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));

        String report = generateStatsReport();
        assertTrue(report.contains("Median:               85.0%"));
    }

    @Test
    @DisplayName("Calculate median with unsorted data")
    void testMedianUnsortedData() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 70.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), science, 80.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), music, 75.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), art, 95.0));

        String report = generateStatsReport();
        // Sorted: [70, 75, 80, 90, 95], median = 80
        assertTrue(report.contains("Median:               80.0%"));
    }

    // Tests for calculateMode()

    @Test
    @DisplayName("Calculate mode with clear most frequent value")
    void testModeClearWinner() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 85.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), science, 85.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), music, 90.0));
        gradeManager.addGrade(new Grade(student3.getStudentId(), art, 75.0));

        String report = generateStatsReport();
        // Mode = 85 (appears 3 times)
        assertTrue(report.contains("Mode:                 85.0%"));
    }

    @Test
    @DisplayName("Calculate mode with single grade")
    void testModeSingleGrade() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 88.0));

        String report = generateStatsReport();
        assertTrue(report.contains("Mode:                 88.0%"));
    }

    @Test
    @DisplayName("Calculate mode with all unique values")
    void testModeAllUnique() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 70.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), science, 90.0));

        String report = generateStatsReport();
        // When all unique, returns first value (70 after rounding)
        assertTrue(report.contains("Mode:                 70.0%") ||
                report.contains("Mode:                 80.0%") ||
                report.contains("Mode:                 90.0%"));
    }

    // Tests for calculateStandardDeviation()

    @Test
    @DisplayName("Calculate standard deviation with identical values")
    void testStdDevIdenticalValues() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 80.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), science, 80.0));

        String report = generateStatsReport();
        // StdDev = 0 when all values are the same
        assertTrue(report.contains("Standard Deviation:   0.0%"));
    }

    @Test
    @DisplayName("Calculate standard deviation with varied values")
    void testStdDevVariedValues() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 70.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 80.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), science, 90.0));

        String report = generateStatsReport();
        // Mean = 80, StdDev = sqrt(((70-80)² + (80-80)² + (90-80)²) / 3) ≈ 8.16
        assertTrue(report.contains("Standard Deviation:   8.") ||
                report.contains("Standard Deviation:   9."));
    }

    @Test
    @DisplayName("Calculate standard deviation with single value")
    void testStdDevSingleValue() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));

        String report = generateStatsReport();
        // StdDev = 0 for single value
        assertTrue(report.contains("Standard Deviation:   0.0%"));
    }

    @Test
    @DisplayName("Calculate standard deviation with large spread")
    void testStdDevLargeSpread() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 0.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 50.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), science, 100.0));

        String report = generateStatsReport();
        // Mean = 50, StdDev = sqrt(((0-50)² + (50-50)² + (100-50)²) / 3) ≈ 40.82
        assertTrue(report.contains("Standard Deviation:   40.") ||
                report.contains("Standard Deviation:   41."));
    }

    // Tests for calculateSubjectAverage()

    @Test
    @DisplayName("Calculate subject average for Mathematics")
    void testSubjectAverageMathematics() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), math, 90.0));
        gradeManager.addGrade(new Grade(student3.getStudentId(), math, 70.0));

        String report = generateStatsReport();
        // Math average = (80 + 90 + 70) / 3 = 80.0
        assertTrue(report.contains("Mathematics:        80.0%"));
    }

    @Test
    @DisplayName("Calculate subject average for English")
    void testSubjectAverageEnglish() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 85.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), english, 95.0));

        String report = generateStatsReport();
        // English average = (85 + 95) / 2 = 90.0
        assertTrue(report.contains("English:            90.0%"));
    }

    @Test
    @DisplayName("Calculate subject average for Music")
    void testSubjectAverageMusic() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 88.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), music, 92.0));
        gradeManager.addGrade(new Grade(student3.getStudentId(), music, 90.0));

        String report = generateStatsReport();
        // Music average = (88 + 92 + 90) / 3 = 90.0
        assertTrue(report.contains("Music:              90.0%"));
    }

    @Test
    @DisplayName("Calculate subject average when subject has no grades")
    void testSubjectAverageNoGrades() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));
        // No English grades added

        String report = generateStatsReport();
        // English average = 0.0 when no grades
        assertTrue(report.contains("English:            0.0%"));
    }

    @Test
    @DisplayName("Calculate subject average with single grade")
    void testSubjectAverageSingleGrade() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), science, 87.5));

        String report = generateStatsReport();
        assertTrue(report.contains("Science:            87.5%"));
    }

    // Tests for calculateCoreSubjectsAverage()

    @Test
    @DisplayName("Calculate core subjects average with all core subjects")
    void testCoreSubjectsAverageAllSubjects() throws StudentNotFoundException {
        // Add grades for all three core subjects
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), math, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 85.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), english, 95.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), science, 75.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), science, 85.0));

        String report = generateStatsReport();
        // Core average = (80+90+85+95+75+85) / 6 = 85.0
        assertTrue(report.contains("Core Subjects:        85.0% average"));
    }

    @Test
    @DisplayName("Calculate core subjects average with only some core subjects")
    void testCoreSubjectsAverageSomeSubjects() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), math, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 70.0));
        // No science grades

        String report = generateStatsReport();
        // Core average = (80 + 90 + 70) / 3 = 80.0
        assertTrue(report.contains("Core Subjects:        80.0% average"));
    }

    @Test
    @DisplayName("Calculate core subjects average excludes electives")
    void testCoreSubjectsAverageExcludesElectives() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 100.0)); // Should be ignored
        gradeManager.addGrade(new Grade(student1.getStudentId(), art, 100.0)); // Should be ignored

        String report = generateStatsReport();
        // Core average = (80 + 90) / 2 = 85.0 (ignores music and art)
        assertTrue(report.contains("Core Subjects:        85.0% average"));
    }

    @Test
    @DisplayName("Calculate core subjects average with no core grades")
    void testCoreSubjectsAverageNoCoreGrades() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 85.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), art, 90.0));

        String report = generateStatsReport();
        // Core average = 0.0 when no core grades
        assertTrue(report.contains("Core Subjects:        0.0% average"));
    }

    // Tests for calculateElectiveSubjectsAverage()

    @Test
    @DisplayName("Calculate elective subjects average with all electives")
    void testElectiveSubjectsAverageAllSubjects() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 85.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), music, 95.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), art, 80.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), art, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), pe, 88.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), pe, 92.0));

        String report = generateStatsReport();
        // Elective average = (85+95+80+90+88+92) / 6 = 88.33...
        assertTrue(report.contains("Elective Subjects:    88.") ||
                report.contains("Elective Subjects:    89."));
    }

    @Test
    @DisplayName("Calculate elective subjects average with some electives")
    void testElectiveSubjectsAverageSomeSubjects() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 90.0));
        gradeManager.addGrade(new Grade(student2.getStudentId(), art, 80.0));
        // No PE grades

        String report = generateStatsReport();
        // Elective average = (90 + 80) / 2 = 85.0
        assertTrue(report.contains("Elective Subjects:    85.0% average"));
    }

    @Test
    @DisplayName("Calculate elective subjects average excludes core")
    void testElectiveSubjectsAverageExcludesCore() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), music, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), art, 90.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 100.0)); // Should be ignored
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 100.0)); // Should be ignored

        String report = generateStatsReport();
        // Elective average = (80 + 90) / 2 = 85.0 (ignores math and english)
        assertTrue(report.contains("Elective Subjects:    85.0% average"));
    }

    @Test
    @DisplayName("Calculate elective subjects average with no elective grades")
    void testElectiveSubjectsAverageNoElectiveGrades() throws StudentNotFoundException {
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 85.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 90.0));

        String report = generateStatsReport();
        // Elective average = 0.0 when no elective grades
        assertTrue(report.contains("Elective Subjects:    0.0% average"));
    }
}
