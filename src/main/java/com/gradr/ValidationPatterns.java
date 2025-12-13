package com.gradr;

import java.util.regex.Pattern;

/**
 * ValidationPatterns - All regex patterns compiled once as static final Pattern objects
 * 
 * Pattern Compilation Best Practices:
 * - All regex patterns compiled once as static final Pattern objects
 * - Reused for multiple validations (performance optimization)
 * - Documented with examples and explanation
 * - Tested with valid and invalid inputs
 * 
 * Performance Considerations:
 * - Use Matcher.find() for partial matches
 * - Use Matcher.matches() for full string validation
 * - Reset Matcher objects for reuse
 * - Avoid nested quantifiers (catastrophic backtracking)
 * - Document regex complexity
 */
public class ValidationPatterns {
    
    // Helper method to safely compile patterns with error handling
    private static Pattern safeCompile(String pattern, String patternName) {
        try {
            return Pattern.compile(pattern);
        } catch (java.util.regex.PatternSyntaxException e) {
            throw new RuntimeException(
                "X ERROR: Failed to compile " + patternName + " pattern.\n" +
                "   Pattern syntax error: " + e.getMessage() + "\n" +
                "   Pattern: " + pattern, e
            );
        }
    }
    
    /**
     * Student ID Pattern: STU followed by exactly 3 digits
     * Examples: STU001, STU042, STU999
     * Complexity: O(1) - Simple pattern matching
     */
    public static final Pattern STUDENT_ID;
    
    /**
     * Email Pattern: username@domain.extension
     * Allows: letters, numbers, dots, hyphens, underscores, plus, percent before @
     * Domain: letters, numbers, dots, hyphens
     * Extension: 2+ letters
     * Examples: john.smith@university.edu, jsmith@college.org
     * Complexity: O(n) where n is email length
     */
    public static final Pattern EMAIL;
    
    /**
     * Phone Pattern: Multiple formats supported
     * Format 1: (123) 456-7890
     * Format 2: 123-456-7890
     * Format 3: +1-123-456-7890
     * Format 4: 1234567890 (10 digits)
     * Examples: (123) 456-7890, 123-456-7890, +1-123-456-7890, 1234567890
     * Complexity: O(n) where n is phone length
     */
    public static final Pattern PHONE_FORMAT1;
    public static final Pattern PHONE_FORMAT2;
    public static final Pattern PHONE_FORMAT3;
    public static final Pattern PHONE_FORMAT4;
    
    /**
     * Name Pattern: Letters, spaces, hyphens, apostrophes
     * Allows: First name, optional middle names, last name
     * Examples: John Smith, Mary-Jane O'Connor, Jean-Pierre
     * Complexity: O(n) where n is name length
     * Note: Dash moved to end of character class to avoid range interpretation
     */
    public static final Pattern NAME;
    
    /**
     * Date Pattern: YYYY-MM-DD format
     * Examples: 2024-11-03, 2025-01-15
     * Complexity: O(1) - Fixed length pattern
     */
    public static final Pattern DATE;
    
    /**
     * Course Code Pattern: 3 uppercase letters followed by 3 digits
     * Examples: MAT101, ENG203, SCI301
     * Complexity: O(1) - Fixed length pattern
     */
    public static final Pattern COURSE_CODE;
    
    /**
     * Grade Pattern: 0-100 (integer)
     * Allows: 0-9, 10-99, 100
     * Examples: 0, 50, 85, 100
     * Complexity: O(1) - Simple numeric pattern
     */
    public static final Pattern GRADE;
    
    // Static initializer block to compile all patterns with error handling
    static {
        STUDENT_ID = safeCompile("STU\\d{3}", "STUDENT_ID");
        EMAIL = safeCompile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", "EMAIL");
        PHONE_FORMAT1 = safeCompile("\\(\\d{3}\\) \\d{3}-\\d{4}", "PHONE_FORMAT1");
        PHONE_FORMAT2 = safeCompile("\\d{3}-\\d{3}-\\d{4}", "PHONE_FORMAT2");
        PHONE_FORMAT3 = safeCompile("\\+1-\\d{3}-\\d{3}-\\d{4}", "PHONE_FORMAT3");
        PHONE_FORMAT4 = safeCompile("\\d{10}", "PHONE_FORMAT4");
        // Fixed: moved dash to end of character class ['\s-] instead of ['-\s]
        NAME = safeCompile("^[a-zA-Z]+(['\\s-][a-zA-Z]+)*$", "NAME");
        DATE = safeCompile("^\\d{4}-\\d{2}-\\d{2}$", "DATE");
        COURSE_CODE = safeCompile("^[A-Z]{3}\\d{3}$", "COURSE_CODE");
        GRADE = safeCompile("^(100|[1-9]?\\d)$", "GRADE");
    }
    
    // Private constructor to prevent instantiation
    private ValidationPatterns() {
        throw new AssertionError("ValidationPatterns should not be instantiated");
    }
}

