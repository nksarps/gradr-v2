package com.gradr;

/**
 * StudentFactory - Factory for creating Student objects
 * Adheres to Open-Closed Principle by allowing extension through factory methods
 * 
 * Design Pattern: Factory Pattern
 * - Encapsulates student creation logic
 * - Makes system extensible for new student types without modifying existing code
 * 
 * Responsibilities:
 * - Create student instances based on type
 * - Validate student creation parameters
 */
public class StudentFactory {
    
    /**
     * Student types enum for type safety
     */
    public enum StudentType {
        REGULAR,
        HONORS
    }
    
    /**
     * Create a student based on type
     * 
     * @param type Student type (REGULAR or HONORS)
     * @param name Student name
     * @param age Student age
     * @param email Student email
     * @param phone Student phone
     * @return Student instance of appropriate type
     */
    public static Student createStudent(StudentType type, String name, int age, String email, String phone) {
        switch (type) {
            case REGULAR:
                return new RegularStudent(name, age, email, phone);
            case HONORS:
                return new HonorsStudent(name, age, email, phone);
            default:
                throw new IllegalArgumentException("Unknown student type: " + type);
        }
    }
    
    /**
     * Create a student based on type number (for backward compatibility with menu system)
     * 
     * @param typeChoice 1 for Regular, 2 for Honors
     * @param name Student name
     * @param age Student age
     * @param email Student email
     * @param phone Student phone
     * @return Student instance of appropriate type
     */
    public static Student createStudent(int typeChoice, String name, int age, String email, String phone) {
        StudentType type = typeChoice == 1 ? StudentType.REGULAR : StudentType.HONORS;
        return createStudent(type, name, age, email, phone);
    }
    
    /**
     * Get student type enum from choice number
     * 
     * @param typeChoice 1 for Regular, 2 for Honors
     * @return StudentType enum
     */
    public static StudentType getStudentType(int typeChoice) {
        return typeChoice == 1 ? StudentType.REGULAR : StudentType.HONORS;
    }
}
