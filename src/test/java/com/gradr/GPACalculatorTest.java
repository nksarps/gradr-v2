package com.gradr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GPACalculatorTest {
    private GPACalculator gpaCalculator;

    @BeforeEach
    public void setUp() {
        GradeManager gradeManager = new GradeManager();
        gpaCalculator = new GPACalculator(gradeManager);
    }

    @Test
    public void sixtyPercentShouldReturnD(){
        String result = gpaCalculator.getLetterGrade(60);
        Assertions.assertEquals("D", result);
    }

    @Test
    public void sixtySevenPercentShouldReturnDPlus(){
        String result = gpaCalculator.getLetterGrade(67);
        Assertions.assertEquals("D+", result);
    }

    @Test
    public void seventyPercentShouldReturnCMinus(){
        String result = gpaCalculator.getLetterGrade(70);
        Assertions.assertEquals("C-", result); // Fixed: was "D", should be "C-"
    }

    @Test
    public void seventyThreePercentShouldReturnC(){
        String result = gpaCalculator.getLetterGrade(73);
        Assertions.assertEquals("C", result);
    }

    @Test
    public void seventySevenPercentShouldReturnCPlus(){
        String result = gpaCalculator.getLetterGrade(77);
        Assertions.assertEquals("C+", result);
    }

    @Test
    public void eightyPercentShouldReturnBMinus(){
        String result = gpaCalculator.getLetterGrade(80);
        Assertions.assertEquals("B-", result);
    }

    @Test
    public void eightyThreePercentShouldReturnB(){
        String result = gpaCalculator.getLetterGrade(83);
        Assertions.assertEquals("B", result);
    }

    @Test
    public void eightySevenPercentShouldReturnBPlus(){
        String result = gpaCalculator.getLetterGrade(87);
        Assertions.assertEquals("B+", result);
    }

    @Test
    public void ninetyPercentShouldReturnAMinus(){
        String result = gpaCalculator.getLetterGrade(90);
        Assertions.assertEquals("A-", result);
    }

    @Test
    public void ninetyThreePercentShouldReturnA(){
        String result = gpaCalculator.getLetterGrade(93);
        Assertions.assertEquals("A", result);
    }

    @Test
    public void fiftyNinePercentShouldReturnF(){
        String result = gpaCalculator.getLetterGrade(59);
        Assertions.assertEquals("F", result);
    }

    @Test
    public void zeroPercentShouldReturnF(){
        String result = gpaCalculator.getLetterGrade(0);
        Assertions.assertEquals("F", result);
    }

    @Test
    public void oneHundredPercentShouldReturnA(){
        String result = gpaCalculator.getLetterGrade(100);
        Assertions.assertEquals("A", result);
    }

    @Test
    public void oneHundredPercentShouldReturn4Point0(){
        double result = gpaCalculator.convertPercentageToGPA(100);
        Assertions.assertEquals(4.0, result);
    }

    @Test
    public void ninetyThreePercentShouldReturn4Point0(){
        double result = gpaCalculator.convertPercentageToGPA(93);
        Assertions.assertEquals(4.0, result);
    }

    @Test
    public void ninetyTwoPercentShouldReturn3Point7(){
        double result = gpaCalculator.convertPercentageToGPA(92);
        Assertions.assertEquals(3.7, result);
    }

    @Test
    public void ninetyPercentShouldReturn3Point7(){
        double result = gpaCalculator.convertPercentageToGPA(90);
        Assertions.assertEquals(3.7, result);
    }

    @Test
    public void eightyNinePercentShouldReturn3Point3(){
        double result = gpaCalculator.convertPercentageToGPA(89);
        Assertions.assertEquals(3.3, result);
    }

    @Test
    public void eightySevenPercentShouldReturn3Point3(){
        double result = gpaCalculator.convertPercentageToGPA(87);
        Assertions.assertEquals(3.3, result);
    }

    @Test
    public void eightySixPercentShouldReturn3Point0(){
        double result = gpaCalculator.convertPercentageToGPA(86);
        Assertions.assertEquals(3.0, result);
    }

    @Test
    public void eightyThreePercentShouldReturn3Point0(){
        double result = gpaCalculator.convertPercentageToGPA(83);
        Assertions.assertEquals(3.0, result);
    }

    @Test
    public void eightyTwoPercentShouldReturn2Point7(){
        double result = gpaCalculator.convertPercentageToGPA(82);
        Assertions.assertEquals(2.7, result);
    }

    @Test
    public void eightyPercentShouldReturn2Point7(){
        double result = gpaCalculator.convertPercentageToGPA(80);
        Assertions.assertEquals(2.7, result);
    }

    @Test
    public void seventyNinePercentShouldReturn2Point3(){
        double result = gpaCalculator.convertPercentageToGPA(79);
        Assertions.assertEquals(2.3, result);
    }

    @Test
    public void seventySevenPercentShouldReturn2Point3(){
        double result = gpaCalculator.convertPercentageToGPA(77);
        Assertions.assertEquals(2.3, result);
    }

    @Test
    public void seventySixPercentShouldReturn2Point0(){
        double result = gpaCalculator.convertPercentageToGPA(76);
        Assertions.assertEquals(2.0, result);
    }

    @Test
    public void seventyThreePercentShouldReturn2Point0(){
        double result = gpaCalculator.convertPercentageToGPA(73);
        Assertions.assertEquals(2.0, result);
    }

    @Test
    public void seventyTwoPercentShouldReturn1Point7(){
        double result = gpaCalculator.convertPercentageToGPA(72);
        Assertions.assertEquals(1.7, result);
    }

    @Test
    public void seventyPercentShouldReturn1Point7(){
        double result = gpaCalculator.convertPercentageToGPA(70);
        Assertions.assertEquals(1.7, result);
    }

    @Test
    public void sixtyNinePercentShouldReturn1Point3(){
        double result = gpaCalculator.convertPercentageToGPA(69);
        Assertions.assertEquals(1.3, result);
    }

    @Test
    public void sixtySevenPercentShouldReturn1Point3(){
        double result = gpaCalculator.convertPercentageToGPA(67);
        Assertions.assertEquals(1.3, result);
    }

    @Test
    public void sixtySixPercentShouldReturn1Point0(){
        double result = gpaCalculator.convertPercentageToGPA(66);
        Assertions.assertEquals(1.0, result);
    }

    @Test
    public void sixtyPercentShouldReturn1Point0(){
        double result = gpaCalculator.convertPercentageToGPA(60);
        Assertions.assertEquals(1.0, result);
    }

    @Test
    public void fiftyNinePercentShouldReturn0Point0(){
        double result = gpaCalculator.convertPercentageToGPA(59);
        Assertions.assertEquals(0.0, result);
    }

    @Test
    public void zeroPercentShouldReturn0Point0(){
        double result = gpaCalculator.convertPercentageToGPA(0);
        Assertions.assertEquals(0.0, result);
    }
}