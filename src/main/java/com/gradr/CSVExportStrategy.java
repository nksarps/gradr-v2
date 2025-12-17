package com.gradr;

import com.gradr.exceptions.FileExportException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

/**
 * CSVExportStrategy - Concrete strategy for CSV export
 * Adheres to Open-Closed Principle (implements FileExportStrategy)
 * 
 * Responsibilities:
 * - Export student reports to CSV format
 * - Handle CSV-specific formatting and writing
 */
public class CSVExportStrategy implements FileExportStrategy {
    
    private static final Path CSV_DIR = Paths.get("./reports/csv/");
    private long lastFileSize = 0;
    private long lastWriteTime = 0;
    private SystemPerformanceMonitor performanceMonitor;
    
    public CSVExportStrategy() throws FileExportException {
        try {
            Files.createDirectories(CSV_DIR);
        } catch (IOException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to create CSV export directory: " + e.getMessage()
            );
        }
    }

    public CSVExportStrategy(SystemPerformanceMonitor performanceMonitor) throws FileExportException {
        this.performanceMonitor = performanceMonitor;
        try {
            Files.createDirectories(CSV_DIR);
        } catch (IOException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to create CSV export directory: " + e.getMessage()
            );
        }
    }
    
    @Override
    public Path export(StudentReport report, String fileName) throws FileExportException {
        long startTime = System.nanoTime();
        Path filePath = CSV_DIR.resolve(fileName + getFileExtension());
        
        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath, 
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            // Write CSV header
            writer.write("Grade ID,Date,Subject,Type,Grade\n");
            
            // Stream grades one by one
            for (GradeData grade : report.getGrades()) {
                writer.write(String.format("%s,%s,%s,%s,%.1f\n",
                    grade.getGradeId(),
                    grade.getDate(),
                    grade.getSubjectName(),
                    grade.getSubjectType(),
                    grade.getGrade()
                ));
            }
            
            // Write summary data
            writer.write(String.format("\nSummary,Student ID,%s\n", report.getStudentId()));
            writer.write(String.format("Summary,Name,%s\n", report.getStudentName()));
            writer.write(String.format("Summary,Type,%s\n", report.getStudentType()));
            writer.write(String.format("Summary,Overall Average,%.2f\n", report.getOverallAverage()));
            writer.write(String.format("Summary,Total Grades,%d\n", report.getGrades().size()));
            
        } catch (IOException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to export CSV file: " + e.getMessage()
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
            performanceMonitor.recordIOOperation("CSV Write", fileName + ".csv", lastWriteTime, lastFileSize, false);
        }
        
        return filePath;
    }
    
    @Override
    public String getFormatName() {
        return "CSV";
    }
    
    @Override
    public String getFileExtension() {
        return ".csv";
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
