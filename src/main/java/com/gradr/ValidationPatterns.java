package com.gradr;

import java.util.regex.Pattern;

/**
 * ValidationPatterns - Static final Pattern objects for regex validation
 * 
 * All regex patterns are:
 * - Compiled once as static final Pattern objects
 * - Reused for multiple validations
 * - Documented with examples and explanation
 * - Tested with valid and invalid inputs
 * 
 * Performance: Patterns are compiled once at class loading time, not on each validation
 */
public class ValidationPatterns {
    
    /**
     * Student ID Pattern: STU followed by exactly 3 digits
     * Examples: STU001, STU042, STU999
     * Invalid: stu-001, STU12, STU1234
     */
    public static final Pattern STUDENT_ID = Pattern.compile("^STU\\d{3}$");
    
    /**
     * Email Pattern: Standard email format
     * Format: username@domain.extension
     * Examples: john.smith@university.edu, jsmith@college.org, user.name+tag@example.co.uk
     * Invalid: john.smith@uni, @domain.com, user@
     */
    public static final Pattern EMAIL = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    /**
     * Phone Pattern: Multiple formats supported
     * Format 1: (123) 456-7890
     * Format 2: 123-456-7890
     * Format 3: +1-123-456-7890
     * Format 4: 1234567890 (10 digits)
     * Examples: (555) 123-4567, 555-123-4567, +1-555-123-4567, 5551234567
     * Invalid: 555-0123 (missing area code), 12345 (too short)
     */
    public static final Pattern PHONE_FORMAT1 = Pattern.compile("^\\(\\d{3}\\) \\d{3}-\\d{4}$"); // (123) 456-7890
    public static final Pattern PHONE_FORMAT2 = Pattern.compile("^\\d{3}-\\d{3}-\\d{4}$"); // 123-456-7890
    public static final Pattern PHONE_FORMAT3 = Pattern.compile("^\\+1-\\d{3}-\\d{3}-\\d{4}$"); // +1-123-456-7890
    public static final Pattern PHONE_FORMAT4 = Pattern.compile("^\\d{10}$"); // 1234567890
    
    /**
     * Name Pattern: Letters, spaces, hyphens, and apostrophes
     * Examples: John Smith, Mary-Jane O'Connor, Jean-Pierre
     * Invalid: John123 (contains digits), John@Smith (contains special chars)
     */
    public static final Pattern NAME = Pattern.compile("^[a-zA-Z]+(['-\\s][a-zA-Z]+)*$");
    
    /**
     * Date Pattern: YYYY-MM-DD format
     * Examples: 2024-11-03, 2023-01-15, 2025-12-31
     * Invalid: 2024/11/03 (wrong separators), 11-03-2024 (wrong order), 2024-1-3 (missing zeros)
     */
    public static final Pattern DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    
    /**
     * Course Code Pattern: 3 uppercase letters followed by 3 digits
     * Examples: MAT101, ENG203, CS150, PHY301
     * Invalid: mat101 (lowercase), MAT10 (too short), MAT1010 (too long)
     */
    public static final Pattern COURSE_CODE = Pattern.compile("^[A-Z]{3}\\d{3}$");
    
    /**
     * Grade Pattern: 0-100 (integer)
     * Examples: 0, 50, 85, 100
     * Invalid: -1 (negative), 101 (too high), 85.5 (decimal)
     */
    public static final Pattern GRADE = Pattern.compile("^(100|[1-9]?\\d)$");
    
    /**
     * Age Pattern: Valid age range (5-100)
     * Examples: 5, 18, 25, 100
     * Invalid: 4 (too young), 101 (too old), -5 (negative)
     */
    public static final Pattern AGE = Pattern.compile("^([5-9]|[1-9]\\d|100)$");
}

