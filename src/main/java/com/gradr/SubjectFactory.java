package com.gradr;

/**
 * SubjectFactory - Factory for creating Subject objects
 * Adheres to Open-Closed Principle by allowing extension through factory methods
 * 
 * Design Pattern: Factory Pattern
 * - Encapsulates subject creation logic
 * - Makes system extensible for new subject types without modifying existing code
 * 
 * Responsibilities:
 * - Create subject instances based on type
 * - Map subject choices to subject names
 */
public class SubjectFactory {
    
    /**
     * Subject types enum for type safety
     */
    public enum SubjectType {
        CORE,
        ELECTIVE
    }
    
    /**
     * Core subject names
     */
    public enum CoreSubjectName {
        MATHEMATICS("Mathematics"),
        ENGLISH("English"),
        SCIENCE("Science");
        
        private final String displayName;
        
        CoreSubjectName(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Elective subject names
     */
    public enum ElectiveSubjectName {
        MUSIC("Music"),
        ART("Art"),
        PHYSICAL_EDUCATION("Physical Education");
        
        private final String displayName;
        
        ElectiveSubjectName(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Create a subject based on type
     * 
     * @param type Subject type (CORE or ELECTIVE)
     * @return Subject instance of appropriate type
     */
    public static Subject createSubject(SubjectType type) {
        switch (type) {
            case CORE:
                return new CoreSubject();
            case ELECTIVE:
                return new ElectiveSubject();
            default:
                throw new IllegalArgumentException("Unknown subject type: " + type);
        }
    }
    
    /**
     * Create a subject based on type choice (for backward compatibility with menu system)
     * 
     * @param typeChoice 1 for Core, 2 for Elective
     * @return Subject instance of appropriate type
     */
    public static Subject createSubject(int typeChoice) {
        SubjectType type = typeChoice == 1 ? SubjectType.CORE : SubjectType.ELECTIVE;
        return createSubject(type);
    }
    
    /**
     * Set subject name based on type and choice
     * 
     * @param subject Subject instance
     * @param typeChoice 1 for Core, 2 for Elective
     * @param subjectChoice 1-3 for specific subject
     */
    public static void setSubjectName(Subject subject, int typeChoice, int subjectChoice) {
        if (typeChoice == 1) {
            // Core subjects
            switch (subjectChoice) {
                case 1:
                    subject.setSubjectName(CoreSubjectName.MATHEMATICS.getDisplayName());
                    break;
                case 2:
                    subject.setSubjectName(CoreSubjectName.ENGLISH.getDisplayName());
                    break;
                case 3:
                    subject.setSubjectName(CoreSubjectName.SCIENCE.getDisplayName());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid core subject choice: " + subjectChoice);
            }
        } else {
            // Elective subjects
            switch (subjectChoice) {
                case 1:
                    subject.setSubjectName(ElectiveSubjectName.MUSIC.getDisplayName());
                    break;
                case 2:
                    subject.setSubjectName(ElectiveSubjectName.ART.getDisplayName());
                    break;
                case 3:
                    subject.setSubjectName(ElectiveSubjectName.PHYSICAL_EDUCATION.getDisplayName());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid elective subject choice: " + subjectChoice);
            }
        }
    }
    
    /**
     * Get core subject names for display
     */
    public static CoreSubjectName[] getCoreSubjects() {
        return CoreSubjectName.values();
    }
    
    /**
     * Get elective subject names for display
     */
    public static ElectiveSubjectName[] getElectiveSubjects() {
        return ElectiveSubjectName.values();
    }
}
