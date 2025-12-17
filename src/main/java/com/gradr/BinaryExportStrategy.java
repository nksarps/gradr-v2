package com.gradr;

import com.gradr.exceptions.FileExportException;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

/**
 * BinaryExportStrategy - Concrete strategy for binary export
 * Adheres to Open-Closed Principle (implements FileExportStrategy)
 * 
 * Responsibilities:
 * - Export student reports to binary format using Java serialization
 * - Handle binary-specific formatting and writing
 */
public class BinaryExportStrategy implements FileExportStrategy {
    
    private static final Path BINARY_DIR = Paths.get("./reports/binary/");
    private long lastFileSize = 0;
    private long lastWriteTime = 0;
    
    public BinaryExportStrategy() throws FileExportException {
        try {
            Files.createDirectories(BINARY_DIR);
        } catch (IOException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to create binary export directory: " + e.getMessage()
            );
        }
    }
    
    @Override
    public Path export(StudentReport report, String fileName) throws FileExportException {
        long startTime = System.nanoTime();
        Path filePath = BINARY_DIR.resolve(fileName + getFileExtension());
        
        try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(filePath, 
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.TRUNCATE_EXISTING)
        )) {
            // Serialize the entire report object
            oos.writeObject(report);
            oos.flush();
            
        } catch (IOException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to export binary file: " + e.getMessage()
            );
        }
        
        lastWriteTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        try {
            lastFileSize = Files.size(filePath);
        } catch (IOException e) {
            lastFileSize = 0;
        }
        
        return filePath;
    }
    
    @Override
    public String getFormatName() {
        return "Binary";
    }
    
    @Override
    public String getFileExtension() {
        return ".dat";
    }
    
    @Override
    public long getLastFileSize() {
        return lastFileSize;
    }
    
    @Override
    public long getLastWriteTime() {
        return lastWriteTime;
    }
}
