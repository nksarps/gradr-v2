package com.gradr;

import com.gradr.exceptions.FileExportException;

/**
 * MainRefactored - Refactored main class adhering to SOLID principles
 * 
 * This is a simplified version of Main.java that demonstrates proper architecture:
 * - Uses ApplicationContext for dependency injection (DIP)
 * - Delegates UI to ConsoleUI (SRP)
 * - Delegates business logic to MenuHandler (SRP)
 * - Minimal code in main method
 * 
 * To run this version: java com.gradr.MainRefactored
 * To run original: java com.gradr.Main
 */
public class MainRefactored {
    
    public static void main(String[] args) {
        try {
            // Initialize application context (handles all dependency injection)
            ApplicationContext context = new ApplicationContext();
            
            // Initialize UI
            ConsoleUI ui = new ConsoleUI();
            
            // Initialize menu handler (delegates all menu operations)
            MenuHandler menuHandler = new MenuHandler(context, ui);
            
            // Run application
            menuHandler.run();
            
            // Cleanup
            ui.close();
            
        } catch (FileExportException e) {
            System.err.println("Failed to initialize application: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
