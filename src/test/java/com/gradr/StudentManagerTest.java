package com.gradr;

import com.gradr.exceptions.StudentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class StudentManagerTest {

    private StudentManager studentManager;
    private GradeManager gradeManager;

    @BeforeEach
    void setUp() {
        studentManager = new StudentManager();
        gradeManager = new GradeManager();

        // Reset static counter for consistent student IDs in tests
        Student.studentCounter = 0;
    }

    //  addStudent() Tests

    @Test
    @DisplayName("addStudent() - Should add a regular student successfully")
    void testAddStudentRegularStudent() {
        Student student = new RegularStudent("John Doe", 20, "john@email.com", "123-456-7890");
        student.setGradeManager(gradeManager);

        studentManager.addStudent(student);

        assertEquals(1, studentManager.getStudentCount());
        assertEquals("STU001", student.getStudentId());
        assertNotNull(studentManager.getStudents()[0]);
    }

    @Test
    @DisplayName("addStudent() - Should add an honors student successfully")
    void testAddStudentHonorsStudent() {
        Student student = new HonorsStudent("Jane Smith", 21, "jane@email.com", "098-765-4321");
        student.setGradeManager(gradeManager);

        studentManager.addStudent(student);

        // Assert
        assertEquals(1, studentManager.getStudentCount());
        assertEquals("Honors", student.getStudentType());
    }

    @Test
    @DisplayName("addStudent() - Should add multiple students and increment count")
    void testAddStudentMultipleStudents() {
        Student student1 = new RegularStudent("Alice", 19, "alice@email.com", "111-111-1111");
        Student student2 = new HonorsStudent("Bob", 20, "bob@email.com", "222-222-2222");
        Student student3 = new RegularStudent("Charlie", 21, "charlie@email.com", "333-333-3333");

        studentManager.addStudent(student1);
        studentManager.addStudent(student2);
        studentManager.addStudent(student3);

        assertEquals(3, studentManager.getStudentCount());
        assertEquals("STU001", student1.getStudentId());
        assertEquals("STU002", student2.getStudentId());
        assertEquals("STU003", student3.getStudentId());
    }

    @Test
    @DisplayName("addStudent() - Should maintain student order in array")
    void testAddStudentMaintainsOrder() {
        Student student1 = new RegularStudent("First", 19, "first@email.com", "111-111-1111");
        Student student2 = new RegularStudent("Second", 20, "second@email.com", "222-222-2222");

        studentManager.addStudent(student1);
        studentManager.addStudent(student2);

        assertEquals("First", studentManager.getStudents()[0].getName());
        assertEquals("Second", studentManager.getStudents()[1].getName());
    }

    // findStudent() Tests

    @Test
    @DisplayName("findStudent() - Should find existing student by ID")
    void testFindStudentExistingStudent() throws StudentNotFoundException {
        Student student = new RegularStudent("John Doe", 20, "john@email.com", "123-456-7890");
        studentManager.addStudent(student);
        String studentId = student.getStudentId();

        Student foundStudent = studentManager.findStudent(studentId);

        assertNotNull(foundStudent);
        assertEquals(studentId, foundStudent.getStudentId());
        assertEquals("John Doe", foundStudent.getName());
    }

    @Test
    @DisplayName("findStudent() - Should throw exception when searching empty manager")
    void testFindStudentEmptyManager() {
        assertThrows(
                StudentNotFoundException.class,
                () -> studentManager.findStudent("STU001")
        );
    }

    @Test
    @DisplayName("findStudent() - Should find correct student among multiple students")
    void testFindStudentMultipleStudents() throws StudentNotFoundException {
        Student student1 = new RegularStudent("Alice", 19, "alice@email.com", "111-111-1111");
        Student student2 = new HonorsStudent("Bob", 20, "bob@email.com", "222-222-2222");
        Student student3 = new RegularStudent("Charlie", 21, "charlie@email.com", "333-333-3333");

        studentManager.addStudent(student1);
        studentManager.addStudent(student2);
        studentManager.addStudent(student3);

        Student foundStudent = studentManager.findStudent("STU002");

        assertEquals("Bob", foundStudent.getName());
        assertEquals("Honors", foundStudent.getStudentType());
    }

    // calculateClassAverage() Tests

    @Test
    @DisplayName("calculateClassAverage() - Should calculate average for single student with grades")
    void testCalculateClassAverageSingleStudent() {
        Student student = new RegularStudent("John", 20, "john@email.com", "123-456-7890");
        student.setGradeManager(gradeManager);
        studentManager.addStudent(student);

        Subject math = new CoreSubject("Mathematics", "MATH101");
        Subject english = new CoreSubject("English", "ENG101");

        Grade grade1 = new Grade(student.getStudentId(), math, 100.0);
        Grade grade2 = new Grade(student.getStudentId(), english, 90.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);

        double classAverage = studentManager.calculateClassAverage();

        // Assert
        assertEquals(95.0, classAverage, 0.01);
    }

    @Test
    @DisplayName("calculateClassAverage() - Should calculate average for multiple students")
    void testCalculateClassAverageMultipleStudents() {
        Student student1 = new RegularStudent("Alice", 19, "alice@email.com", "111-111-1111");
        Student student2 = new RegularStudent("Bob", 20, "bob@email.com", "222-222-2222");

        student1.setGradeManager(gradeManager);
        student2.setGradeManager(gradeManager);

        studentManager.addStudent(student1);
        studentManager.addStudent(student2);

        Subject math = new CoreSubject("Mathematics", "MATH101");
        Grade grade1 = new Grade(student1.getStudentId(), math, 80.0);
        gradeManager.addGrade(grade1);

        Subject english = new CoreSubject("English", "ENG101");
        Grade grade2 = new Grade(student2.getStudentId(), english, 90.0);
        gradeManager.addGrade(grade2);

        double classAverage = studentManager.calculateClassAverage();

        // Assert - Class average should be (80 + 90) / 2 = 85
        assertEquals(85.0, classAverage, 0.01);
    }

    @Test
    @DisplayName("calculateClassAverage() - Should return 0.0 for students with no grades")
    void testCalculateClassAverageNoGrades() {
        Student student = new RegularStudent("John", 20, "john@email.com", "123-456-7890");
        student.setGradeManager(gradeManager);
        studentManager.addStudent(student);

        double classAverage = studentManager.calculateClassAverage();

        assertEquals(0.0, classAverage, 0.01);
    }

    @Test
    @DisplayName("calculateClassAverage() - Should handle students with different grade counts")
    void testCalculateClassAverageDifferentGradeCounts() {
        Student student1 = new RegularStudent("Alice", 19, "alice@email.com", "111-111-1111");
        Student student2 = new RegularStudent("Bob", 20, "bob@email.com", "222-222-2222");

        student1.setGradeManager(gradeManager);
        student2.setGradeManager(gradeManager);

        studentManager.addStudent(student1);
        studentManager.addStudent(student2);


        Subject math = new CoreSubject("Mathematics", "MATH101");
        Subject english = new CoreSubject("English", "ENG101");
        Subject science = new CoreSubject("Science", "SCI101");

        // Student1 has 3 grades (average: 80)
        gradeManager.addGrade(new Grade(student1.getStudentId(), math, 75.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), english, 80.0));
        gradeManager.addGrade(new Grade(student1.getStudentId(), science, 85.0));

        // Student2 has 1 grade (average: 90)
        gradeManager.addGrade(new Grade(student2.getStudentId(), math, 90.0));

        double classAverage = studentManager.calculateClassAverage();

        // Assert - Class average should be (80 + 90) / 2 = 85
        assertEquals(85.0, classAverage, 0.01);
    }

    @Test
    @DisplayName("calculateClassAverage() - Should handle mix of Regular and Honors students")
    void testCalculateClassAverage_MixedStudentTypes() {
        Student regular = new RegularStudent("Regular Student", 19, "regular@email.com", "111-111-1111");
        Student honors = new HonorsStudent("Honors Student", 20, "honors@email.com", "222-222-2222");

        regular.setGradeManager(gradeManager);
        honors.setGradeManager(gradeManager);

        studentManager.addStudent(regular);
        studentManager.addStudent(honors);

        // Add same grades to both
        Subject math = new CoreSubject("Mathematics", "MATH101");
        gradeManager.addGrade(new Grade(regular.getStudentId(), math, 70.0));
        gradeManager.addGrade(new Grade(honors.getStudentId(), math, 90.0));

        double classAverage = studentManager.calculateClassAverage();

        assertEquals(80.0, classAverage, 0.01);
    }




}