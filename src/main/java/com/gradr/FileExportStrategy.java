package com.gradr;

import com.gradr.exceptions.FileExportException;
import java.nio.file.Path;

/**
 * FileExportStrategy - Strategy interface for file export operations
 * Adheres to Open-Closed Principle by defining extension points
 * 
 * Design Pattern: Strategy Pattern
 * - Defines family of algorithms (export strategies)
 * - Makes algorithms interchangeable
 * - New export formats can be added without modifying existing code
 * 
 * Responsibilities:
 * - Define contract for export operations
 * - Allow multiple export format implementations
 */
public interface FileExportStrategy {
    
    /**
     * Export student report to file
     * 
     * @param report StudentReport to export
     * @param fileName Base filename (without extension)
     * @return Path to exported file
     * @throws FileExportException if export fails
     */
    Path export(StudentReport report, String fileName) throws FileExportException;
    
    /**
     * Get export format name
     * 
     * @return Format name (e.g., "CSV", "JSON", "Binary")
     */
    String getFormatName();
    
    /**
     * Get file extension for this format
     * 
     * @return File extension (e.g., ".csv", ".json", ".dat")
     */
    String getFileExtension();
    
    /**
     * Get file size of last export operation
     * 
     * @return File size in bytes
     */
    long getLastFileSize();
    
    /**
     * Get write time of last export operation
     * 
     * @return Write time in milliseconds
     */
    long getLastWriteTime();
}
