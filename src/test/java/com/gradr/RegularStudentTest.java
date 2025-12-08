package com.gradr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RegularStudentTest {

    private RegularStudent regularStudent;

    @BeforeEach
    public void setUp() {
        regularStudent = new RegularStudent("John Doe", 20, "john@example.com", "123-456-7890");
    }

    @Test
    public void testGetPassingGrade() {
        assertEquals(50.0, regularStudent.getPassingGrade(), 0.001);
    }

    @Test
    public void testGetStudentType() {
        assertEquals("Regular", regularStudent.getStudentType());
    }
}