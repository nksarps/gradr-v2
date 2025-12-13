package com.gradr;

import java.util.regex.Matcher;

/**
 * ValidationUtils - Static utility methods for input validation using compiled regex patterns
 * 
 * Performance Considerations:
 * - Use Matcher.matches() for full string validation
 * - Use Matcher.find() for partial matches
 * - Reset Matcher objects for reuse (handled internally)
 * - Avoid nested quantifiers (catastrophic backtracking) - patterns are optimized
 * - Document regex complexity
 */
public class ValidationUtils {
    
    /**
     * Validate Student ID
     * Pattern: STU followed by exactly 3 digits
     * Time Complexity: O(1) - Pattern matching is constant time
     * @param studentId The student ID to validate
     * @return ValidationResult with success status and error message if invalid
     */
    public static ValidationResult validateStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return new ValidationResult(false, 
                "X VALIDATION ERROR: Invalid Student ID format\n" +
                "   Required Pattern: STU### (STU followed by exactly 3 digits)\n" +
                "   Examples: STU001, STU042, STU999\n" +
                "   Your input: (empty)");
        }
        
        // Use Matcher.matches() for full string validation
        Matcher matcher = ValidationPatterns.STUDENT_ID.matcher(studentId);
        if (matcher.matches()) {
            return new ValidationResult(true, "✓ Valid Student ID");
        } else {
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid Student ID format\n" +
                "   Required Pattern: STU### (STU followed by exactly 3 digits)\n" +
                "   Examples: STU001, STU042, STU999\n" +
                "   Your input: " + studentId);
        }
    }
    
    /**
     * Validate Email Address
     * Pattern: Standard email format (username@domain.extension)
     * Time Complexity: O(1) - Pattern matching is constant time
     * @param email The email address to validate
     * @return ValidationResult with success status and error message if invalid
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid email format\n" +
                "   Required Pattern: username@domain.extension\n" +
                "   Examples: john.smith@university.edu, jsmith@college.org\n" +
                "   Your input: (empty)");
        }
        
        Matcher matcher = ValidationPatterns.EMAIL.matcher(email);
        if (matcher.matches()) {
            return new ValidationResult(true, "✓ Valid Email Address");
        } else {
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid email format\n" +
                "   Required Pattern: username@domain.extension\n" +
                "   Examples: john.smith@university.edu, jsmith@college.org\n" +
                "   Your input: " + email + (email.contains("@") && !email.contains(".") ? " (missing valid extension)" : ""));
        }
    }
    
    /**
     * Validate Phone Number (supports multiple formats)
     * Formats: (123) 456-7890, 123-456-7890, +1-123-456-7890, 1234567890
     * Time Complexity: O(1) - Pattern matching is constant time
     * @param phone The phone number to validate
     * @return ValidationResult with success status and error message if invalid
     */
    public static ValidationResult validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid phone format\n" +
                "   Accepted patterns:\n" +
                "   (123) 456-7890\n" +
                "   123-456-7890\n" +
                "   +1-123-456-7890\n" +
                "   1234567890\n" +
                "   Your input: (empty)");
        }
        
        // Check all phone formats
        Matcher m1 = ValidationPatterns.PHONE_FORMAT1.matcher(phone);
        Matcher m2 = ValidationPatterns.PHONE_FORMAT2.matcher(phone);
        Matcher m3 = ValidationPatterns.PHONE_FORMAT3.matcher(phone);
        Matcher m4 = ValidationPatterns.PHONE_FORMAT4.matcher(phone);
        
        if (m1.matches() || m2.matches() || m3.matches() || m4.matches()) {
            return new ValidationResult(true, "✓ Valid Phone Number");
        } else {
            String errorDetail = "";
            if (phone.length() < 10) {
                errorDetail = " (too short)";
            } else if (phone.length() > 14) {
                errorDetail = " (too long)";
            } else if (phone.matches("\\d{3}-\\d{4}")) {
                errorDetail = " (missing area code)";
            }
            
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid phone format\n" +
                "   Accepted patterns:\n" +
                "   (123) 456-7890\n" +
                "   123-456-7890\n" +
                "   +1-123-456-7890\n" +
                "   1234567890\n" +
                "   Your input: " + phone + errorDetail);
        }
    }
    
    /**
     * Validate Name
     * Pattern: Letters, spaces, hyphens, and apostrophes
     * Time Complexity: O(1) - Pattern matching is constant time
     * @param name The name to validate
     * @return ValidationResult with success status and error message if invalid
     */
    public static ValidationResult validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid name format\n" +
                "   Required Pattern: Only letters, spaces, hyphens, and apostrophes\n" +
                "   Examples: John Smith, Mary-Jane O'Connor\n" +
                "   Your input: (empty)");
        }
        
        Matcher matcher = ValidationPatterns.NAME.matcher(name);
        if (matcher.matches()) {
            return new ValidationResult(true, "✓ Valid Student Name");
        } else {
            String errorDetail = "";
            if (name.matches(".*\\d.*")) {
                errorDetail = " (contains digits)";
            } else if (name.matches(".*[^a-zA-Z\\s'-].*")) {
                errorDetail = " (contains invalid characters)";
            }
            
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid name format\n" +
                "   Required Pattern: Only letters, spaces, hyphens, and apostrophes\n" +
                "   Examples: John Smith, Mary-Jane O'Connor\n" +
                "   Your input: " + name + errorDetail);
        }
    }
    
    /**
     * Validate Date (YYYY-MM-DD format)
     * Pattern: YYYY-MM-DD
     * Time Complexity: O(1) - Pattern matching is constant time
     * @param date The date string to validate
     * @return ValidationResult with success status and error message if invalid
     */
    public static ValidationResult validateDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid date format\n" +
                "   Pattern required: YYYY-MM-DD\n" +
                "   Example: 2024-11-03\n" +
                "   Your input: (empty)");
        }
        
        Matcher matcher = ValidationPatterns.DATE.matcher(date);
        if (matcher.matches()) {
            return new ValidationResult(true, "✓ Valid Enrollment Date");
        } else {
            String errorDetail = "";
            if (date.contains("/")) {
                errorDetail = " (wrong separators - use hyphens)";
            } else if (date.matches("\\d{2}-\\d{2}-\\d{4}")) {
                errorDetail = " (wrong order - use YYYY-MM-DD)";
            } else if (date.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                errorDetail = " (missing leading zeros)";
            }
            
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid date format\n" +
                "   Pattern required: YYYY-MM-DD\n" +
                "   Example: 2024-11-03\n" +
                "   Your input: " + date + errorDetail);
        }
    }
    
    /**
     * Validate Course Code
     * Pattern: 3 uppercase letters followed by 3 digits
     * Time Complexity: O(1) - Pattern matching is constant time
     * @param courseCode The course code to validate
     * @return ValidationResult with success status and error message if invalid
     */
    public static ValidationResult validateCourseCode(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid course code format\n" +
                "   Required Pattern: 3 uppercase letters followed by 3 digits\n" +
                "   Examples: MAT101, ENG203, CS150\n" +
                "   Your input: (empty)");
        }
        
        Matcher matcher = ValidationPatterns.COURSE_CODE.matcher(courseCode);
        if (matcher.matches()) {
            return new ValidationResult(true, "✓ Valid Course Code");
        } else {
            String errorDetail = "";
            if (courseCode.length() < 6) {
                errorDetail = " (too short)";
            } else if (courseCode.length() > 6) {
                errorDetail = " (too long)";
            } else if (courseCode.matches("[a-z]{3}\\d{3}")) {
                errorDetail = " (must be uppercase)";
            }
            
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid course code format\n" +
                "   Required Pattern: 3 uppercase letters followed by 3 digits\n" +
                "   Examples: MAT101, ENG203, CS150\n" +
                "   Your input: " + courseCode + errorDetail);
        }
    }
    
    /**
     * Validate Grade (0-100)
     * Pattern: Integer between 0 and 100
     * Time Complexity: O(1) - Pattern matching is constant time
     * @param grade The grade value to validate
     * @return ValidationResult with success status and error message if invalid
     */
    public static ValidationResult validateGrade(String grade) {
        if (grade == null || grade.trim().isEmpty()) {
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid grade format\n" +
                "   Required Pattern: Integer between 0 and 100\n" +
                "   Examples: 0, 50, 85, 100\n" +
                "   Your input: (empty)");
        }
        
        Matcher matcher = ValidationPatterns.GRADE.matcher(grade);
        if (matcher.matches()) {
            int gradeValue = Integer.parseInt(grade);
            if (gradeValue >= 0 && gradeValue <= 100) {
                return new ValidationResult(true, "✓ Valid Grade");
            } else {
                return new ValidationResult(false,
                    "X VALIDATION ERROR: Invalid grade range\n" +
                    "   Required Pattern: Integer between 0 and 100\n" +
                    "   Examples: 0, 50, 85, 100\n" +
                    "   Your input: " + grade + " (out of range)");
            }
        } else {
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid grade format\n" +
                "   Required Pattern: Integer between 0 and 100\n" +
                "   Examples: 0, 50, 85, 100\n" +
                "   Your input: " + grade + (grade.contains(".") ? " (decimals not allowed)" : ""));
        }
    }
    
    /**
     * Validate Age (5-100)
     * Pattern: Integer between 5 and 100
     * Time Complexity: O(1) - Pattern matching is constant time
     * @param age The age value to validate
     * @return ValidationResult with success status and error message if invalid
     */
    public static ValidationResult validateAge(String age) {
        if (age == null || age.trim().isEmpty()) {
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid age format\n" +
                "   Required Pattern: Integer between 5 and 100\n" +
                "   Examples: 5, 18, 25, 100\n" +
                "   Your input: (empty)");
        }
        
        Matcher matcher = ValidationPatterns.AGE.matcher(age);
        if (matcher.matches()) {
            return new ValidationResult(true, "✓ Valid Age");
        } else {
            try {
                int ageValue = Integer.parseInt(age);
                if (ageValue < 5) {
                    return new ValidationResult(false,
                        "X VALIDATION ERROR: Invalid age range\n" +
                        "   Required Pattern: Integer between 5 and 100\n" +
                        "   Examples: 5, 18, 25, 100\n" +
                        "   Your input: " + age + " (too young)");
                } else if (ageValue > 100) {
                    return new ValidationResult(false,
                        "X VALIDATION ERROR: Invalid age range\n" +
                        "   Required Pattern: Integer between 5 and 100\n" +
                        "   Examples: 5, 18, 25, 100\n" +
                        "   Your input: " + age + " (too old)");
                }
            } catch (NumberFormatException e) {
                // Not a number
            }
            
            return new ValidationResult(false,
                "X VALIDATION ERROR: Invalid age format\n" +
                "   Required Pattern: Integer between 5 and 100\n" +
                "   Examples: 5, 18, 25, 100\n" +
                "   Your input: " + age);
        }
    }
    
    /**
     * ValidationResult - Container class for validation results
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String message;
        
        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

