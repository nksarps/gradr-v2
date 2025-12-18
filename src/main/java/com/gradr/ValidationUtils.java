package com.gradr;

import com.gradr.exceptions.InvalidStudentDataException;

import java.util.regex.Matcher;

import static com.gradr.ValidationPatterns.*;

/**
 * ValidationUtils - Static utility methods for input validation using compiled regex patterns
 * 
 * Features:
 * - Uses pre-compiled Pattern objects for performance
 * - Provides clear error messages with expected patterns and examples
 * - Throws InvalidStudentDataException for validation errors
 * - Reuses Matcher objects for efficiency
 */
public class ValidationUtils {
    
    /**
     * Validate Student ID format
     * Pattern: STU followed by exactly 3 digits
     * 
     * @param studentId The student ID to validate
     * @throws InvalidStudentDataException if validation fails
     */
    public static void validateStudentId(String studentId) throws InvalidStudentDataException {
        Matcher matcher = STUDENT_ID.matcher(studentId);
        if (!matcher.matches()) {
            throw new InvalidStudentDataException(
                "X VALIDATION ERROR: Invalid Student ID format\n" +
                "   Required Pattern: STU### (STU followed by exactly 3 digits)\n" +
                "   Examples: STU001, STU042, STU999\n" +
                "   Your input: " + studentId
            );
        }
    }
    
    /**
     * Validate Email format
     * Pattern: username@domain.extension
     * 
     * @param email The email to validate
     * @throws InvalidStudentDataException if validation fails
     */
    public static void validateEmail(String email) throws InvalidStudentDataException {
        Matcher matcher = EMAIL.matcher(email);
        if (!matcher.matches()) {
            throw new InvalidStudentDataException(
                "X VALIDATION ERROR: Invalid email format\n" +
                "   Required Pattern: username@domain.extension\n" +
                "   Examples: john.smith@university.edu, jsmith@college.org\n" +
                "   Your input: " + email + (email.contains("@") ? " (missing valid extension)" : " (invalid format)")
            );
        }
    }
    
    /**
     * Validate Phone number format (multiple formats supported)
     * Formats: (123) 456-7890, 123-456-7890, +1-123-456-7890, 1234567890
     * 
     * @param phone The phone number to validate
     * @throws InvalidStudentDataException if validation fails
     */
    public static void validatePhone(String phone) throws InvalidStudentDataException {
        Matcher matcher1 = PHONE_FORMAT1.matcher(phone);
        Matcher matcher2 = PHONE_FORMAT2.matcher(phone);
        Matcher matcher3 = PHONE_FORMAT3.matcher(phone);
        Matcher matcher4 = PHONE_FORMAT4.matcher(phone);
        
        if (!matcher1.matches() && !matcher2.matches() && !matcher3.matches() && !matcher4.matches()) {
            String errorDetail = "";
            if (phone.length() < 10) {
                errorDetail = " (missing area code)";
            } else if (phone.length() > 14) {
                errorDetail = " (too long)";
            } else if (!phone.matches(".*\\d.*")) {
                errorDetail = " (contains non-digits)";
            }
            
            throw new InvalidStudentDataException(
                "X VALIDATION ERROR: Invalid phone format\n" +
                "   Accepted patterns:\n" +
                "   - (123) 456-7890\n" +
                "   - 123-456-7890\n" +
                "   - +1-123-456-7890\n" +
                "   - 1234567890\n" +
                "   Your input: " + phone + errorDetail
            );
        }
    }
    
    /**
     * Validate Name format
     * Pattern: Letters, spaces, hyphens, apostrophes
     * 
     * @param name The name to validate
     * @throws InvalidStudentDataException if validation fails
     */
    public static void validateName(String name) throws InvalidStudentDataException {
        Matcher matcher = NAME.matcher(name);
        if (!matcher.matches()) {
            String errorDetail = "";
            if (name.matches(".*\\d.*")) {
                errorDetail = " (contains digits)";
            } else if (name.matches(".*[^a-zA-Z\\s'-].*")) {
                errorDetail = " (contains invalid characters)";
            }
            
            throw new InvalidStudentDataException(
                "X VALIDATION ERROR: Invalid name format\n" +
                "   Required Pattern: Only letters, spaces, hyphens, and apostrophes\n" +
                "   Examples: John Smith, Mary-Jane O'Connor\n" +
                "   Your input: " + name + errorDetail
            );
        }
    }
    
    /**
     * Validate Date format (YYYY-MM-DD)
     * 
     * @param date The date to validate
     * @throws InvalidStudentDataException if validation fails
     */
    public static void validateDate(String date) throws InvalidStudentDataException {
        Matcher matcher = DATE.matcher(date);
        if (!matcher.matches()) {
            String errorDetail = "";
            if (date.contains("/")) {
                errorDetail = " (wrong separators)";
            } else if (date.length() != 10) {
                errorDetail = " (wrong length)";
            }
            
            throw new InvalidStudentDataException(
                "X VALIDATION ERROR: Invalid date format\n" +
                "   Pattern required: YYYY-MM-DD\n" +
                "   Example: 2024-11-03\n" +
                "   Your input: " + date + errorDetail
            );
        }
    }
    
    /**
     * Validate Course Code format
     * Pattern: 3 uppercase letters followed by 3 digits
     * 
     * @param courseCode The course code to validate
     * @throws InvalidStudentDataException if validation fails
     */
    public static void validateCourseCode(String courseCode) throws InvalidStudentDataException {
        Matcher matcher = COURSE_CODE.matcher(courseCode);
        if (!matcher.matches()) {
            throw new InvalidStudentDataException(
                "X VALIDATION ERROR: Invalid course code format\n" +
                "   Required Pattern: 3 uppercase letters followed by 3 digits\n" +
                "   Examples: MAT101, ENG203, SCI301\n" +
                "   Your input: " + courseCode
            );
        }
    }
    
    /**
     * Validate Grade format (0-100)
     * 
     * @param gradeInput The grade input to validate
     * @throws InvalidStudentDataException if validation fails
     */
    public static void validateGrade(String gradeInput) throws InvalidStudentDataException {
        Matcher matcher = GRADE.matcher(gradeInput);
        if (!matcher.matches()) {
            try {
                double grade = Double.parseDouble(gradeInput);
                if (grade < 0 || grade > 100) {
                    throw new InvalidStudentDataException(
                        "X VALIDATION ERROR: Invalid grade value\n" +
                        "   Required Pattern: 0-100\n" +
                        "   Examples: 0, 50, 85, 100\n" +
                        "   Your input: " + gradeInput + " (out of range)"
                    );
                }
            } catch (NumberFormatException e) {
                throw new InvalidStudentDataException(
                    "X VALIDATION ERROR: Invalid grade format\n" +
                    "   Required Pattern: 0-100 (integer)\n" +
                    "   Examples: 0, 50, 85, 100\n" +
                    "   Your input: " + gradeInput + " (not a number)"
                );
            }
        }
    }
    
    /**
     * Validate Grade value (0-100) from integer
     * 
     * @param grade The grade value to validate
     * @throws InvalidStudentDataException if validation fails
     */
    public static void validateGrade(int grade) throws InvalidStudentDataException {
        if (grade < 0 || grade > 100) {
            throw new InvalidStudentDataException(
                "X VALIDATION ERROR: Invalid grade value\n" +
                "   Required Pattern: 0-100\n" +
                "   Examples: 0, 50, 85, 100\n" +
                "   Your input: " + grade + " (out of range)"
            );
        }
    }
}


