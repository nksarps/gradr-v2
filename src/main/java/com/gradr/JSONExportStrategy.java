package com.gradr;

import com.gradr.exceptions.FileExportException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * JSONExportStrategy - Concrete strategy for JSON export
 * Adheres to Open-Closed Principle (implements FileExportStrategy)
 * 
 * Responsibilities:
 * - Export student reports to JSON format
 * - Handle JSON-specific formatting and writing
 */
public class JSONExportStrategy implements FileExportStrategy {
    
    private static final Path JSON_DIR = Paths.get("./reports/json/");
    private long lastFileSize = 0;
    private long lastWriteTime = 0;
    private SystemPerformanceMonitor performanceMonitor;
    
    public JSONExportStrategy() throws FileExportException {
        try {
            Files.createDirectories(JSON_DIR);
        } catch (IOException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to create JSON export directory: " + e.getMessage()
            );
        }
    }

    public JSONExportStrategy(SystemPerformanceMonitor performanceMonitor) throws FileExportException {
        this.performanceMonitor = performanceMonitor;
        try {
            Files.createDirectories(JSON_DIR);
        } catch (IOException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to create JSON export directory: " + e.getMessage()
            );
        }
    }
    
    @Override
    public Path export(StudentReport report, String fileName) throws FileExportException {
        long startTime = System.nanoTime();
        Path filePath = JSON_DIR.resolve(fileName + getFileExtension());
        
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"student\": {\n");
            json.append(String.format("    \"id\": \"%s\",\n", report.getStudentId()));
            json.append(String.format("    \"name\": \"%s\",\n", report.getStudentName()));
            json.append(String.format("    \"type\": \"%s\",\n", report.getStudentType()));
            json.append(String.format("    \"overallAverage\": %.2f,\n", report.getOverallAverage()));
            json.append(String.format("    \"totalGrades\": %d\n", report.getGrades().size()));
            json.append("  },\n");
            json.append("  \"grades\": [\n");
            
            // Add grades as JSON objects
            for (int i = 0; i < report.getGrades().size(); i++) {
                GradeData grade = report.getGrades().get(i);
                json.append("    {\n");
                json.append(String.format("      \"gradeId\": \"%s\",\n", grade.getGradeId()));
                json.append(String.format("      \"date\": \"%s\",\n", grade.getDate()));
                json.append(String.format("      \"subject\": \"%s\",\n", grade.getSubjectName()));
                json.append(String.format("      \"type\": \"%s\",\n", grade.getSubjectType()));
                json.append(String.format("      \"grade\": %.1f\n", grade.getGrade()));
                json.append(i < report.getGrades().size() - 1 ? "    },\n" : "    }\n");
            }
            
            json.append("  ],\n");
            json.append(String.format("  \"metadata\": {\n"));
            json.append(String.format("    \"exportDate\": \"%s\",\n", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            json.append(String.format("    \"reportType\": \"%s\"\n", report.getReportType()));
            json.append("  }\n");
            json.append("}");
            
            // Write with explicit UTF-8 encoding using NIO.2
            Files.writeString(filePath, json.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
        } catch (IOException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to export JSON file: " + e.getMessage()
            );
        }
        
        lastWriteTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        try {
            lastFileSize = Files.size(filePath);
        } catch (IOException e) {
            lastFileSize = 0;
        }

        // Record I/O operation for performance monitoring
        if (performanceMonitor != null) {
            performanceMonitor.recordIOOperation("JSON Write", fileName + ".json", lastWriteTime, lastFileSize, false);
        }
        
        return filePath;
    }
    
    @Override
    public String getFormatName() {
        return "JSON";
    }
    
    @Override
    public String getFileExtension() {
        return ".json";
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
