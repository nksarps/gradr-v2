package com.gradr;

import com.gradr.exceptions.*;
import java.util.*;

/**
 * ConsoleUI - Handles all console-based user interactions
 * Adheres to Single Responsibility Principle - responsible only for UI display and input
 * 
 * Responsibilities:
 * - Display menus
 * - Get user input
 * - Display messages and results
 * - Format output
 */
public class ConsoleUI {
    private final Scanner scanner;
    
    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }
    
    public Scanner getScanner() {
        return scanner;
    }
    
    /**
     * Display main menu
     */
    public void displayMainMenu(TaskScheduler taskScheduler, StatisticsDashboard dashboard) {
        System.out.println("||=============================================================||");
        System.out.println("||             STUDENT GRADE MANAGEMENT - MAIN MENU            ||");
        System.out.println("||=============================================================||");
        System.out.println();

        System.out.println("STUDENT MANAGEMENT");
        System.out.println("1. Add Student (with validation)");
        System.out.println("2. View Students");
        System.out.println("3. Record Grade");
        System.out.println("4. View Grade Report");
        System.out.println();

        System.out.println("FILE OPERATIONS");
        System.out.println("5. Export Grade Report (CSV/JSON/Binary)");
        System.out.println("6. Import Data (Multi-format support) [ENHANCED]");
        System.out.println("7. Bulk Import Grades");
        System.out.println();

        System.out.println("ANALYTICS & REPORTING");
        System.out.println("8. Calculate Student GPA");
        System.out.println("9. View Class Statistics");
        System.out.println("10. Real-Time Statistics Dashboard [NEW]");
        System.out.println("11. Generate Batch Reports [NEW]");
        System.out.println();

        System.out.println("SEARCH & QUERY");
        System.out.println("12. Search Students (Advanced) [ENHANCED]");
        System.out.println("13. Pattern-Based Search [NEW]");
        System.out.println("14. Query Grade History [NEW]");
        System.out.println();

        System.out.println("ADVANCED FEATURES");
        System.out.println("15. Schedule Automated Tasks [NEW]");
        System.out.println("16. View System Performance [NEW]");
        System.out.println("17. Cache Management [NEW]");
        System.out.println("18. Audit Trail Viewer [NEW]");
        System.out.println();

        System.out.println("19. Exit");
        System.out.println();
    }
    
    /**
     * Get menu choice from user
     */
    public int getMenuChoice() {
        try {
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            System.out.println();
            return choice;
        } catch (InputMismatchException e) {
            System.out.println("\n\nX ERROR: InvalidMenuChoiceException\n   Please enter a valid number (1-19).\n");
            System.out.println();
            scanner.nextLine();
            return -1; // Invalid choice
        }
    }
    
    /**
     * Display error message
     */
    public void displayError(String message) {
        System.out.println();
        System.out.println(message);
        System.out.println();
    }
    
    /**
     * Display success message
     */
    public void displaySuccess(String message) {
        System.out.println(message);
        System.out.println();
    }
    
    /**
     * Format file size to human-readable format
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Close scanner
     */
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
