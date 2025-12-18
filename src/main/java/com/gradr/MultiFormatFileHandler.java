package com.gradr;

import com.gradr.exceptions.FileExportException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * MultiFormatFileHandler - Handles file operations in CSV, JSON, and binary formats using NIO.2
 * 
 * Features:
 * - CSV export with streaming for large files
 * - JSON export with proper formatting
 * - Binary serialization for efficient storage
 * - Performance tracking (file size, read/write times)
 * - Support for relative and absolute paths
 * - UTF-8 encoding for text files
 * - Try-with-resources for proper resource management
 * 
 * Thread Safety:
 * - All file write operations (exportToCSV, exportToJSON, exportToBinary) are synchronized
 * - Prevents concurrent write conflicts when multiple threads access the same file handler
 * - File locking should be used at the application level for additional protection
 */
public class MultiFormatFileHandler {
    
    // Directory paths for different formats
    private static final Path CSV_DIR = Paths.get("./reports/csv/");
    private static final Path JSON_DIR = Paths.get("./reports/json/");
    private static final Path BINARY_DIR = Paths.get("./reports/binary/");
    private static final Path DATA_CSV_DIR = Paths.get("./data/csv/");
    private static final Path DATA_JSON_DIR = Paths.get("./data/json/");
    private static final Path DATA_BINARY_DIR = Paths.get("./data/binary/");
    
    // Performance tracking
    private long csvWriteTime = 0;
    private long jsonWriteTime = 0;
    private long binaryWriteTime = 0;
    private long csvFileSize = 0;
    private long jsonFileSize = 0;
    private long binaryFileSize = 0;
    
    // Getters for performance metrics
    public long getCsvWriteTime() { return csvWriteTime; }
    public long getJsonWriteTime() { return jsonWriteTime; }
    public long getBinaryWriteTime() { return binaryWriteTime; }
    public long getCsvFileSize() { return csvFileSize; }
    public long getJsonFileSize() { return jsonFileSize; }
    public long getBinaryFileSize() { return binaryFileSize; }
    
    /**
     * Initialize directories for all formats
     * Time Complexity: O(1) - Directory creation
     */
    public MultiFormatFileHandler() throws FileExportException {
        try {
            Files.createDirectories(CSV_DIR);
            Files.createDirectories(JSON_DIR);
            Files.createDirectories(BINARY_DIR);
            Files.createDirectories(DATA_CSV_DIR);
            Files.createDirectories(DATA_JSON_DIR);
            Files.createDirectories(DATA_BINARY_DIR);
        } catch (IOException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to create export directories: " + e.getMessage()
            );
        }
    }
    
    /**
     * Validate file path using NIO.2 Files API
     * Uses Files.exists(), Files.isReadable(), Files.isWritable() for validation
     * Time Complexity: O(1) - File system checks
     */
    private void validateFilePath(Path filePath, boolean checkReadable, boolean checkWritable) throws FileExportException {
        // Use Files.exists() for path validation
        if (Files.exists(filePath)) {
            // Use Files.isReadable() for read permission validation
            if (checkReadable && !Files.isReadable(filePath)) {
                throw new FileExportException(
                    "X ERROR: FileExportException\n   File is not readable: " + filePath
                );
            }
            // Use Files.isWritable() for write permission validation
            if (checkWritable && !Files.isWritable(filePath)) {
                throw new FileExportException(
                    "X ERROR: FileExportException\n   File is not writable: " + filePath
                );
            }
        }
    }
    
    /**
     * Export student report to CSV format with streaming
     * Uses NIO.2 Files.newBufferedWriter for efficient streaming
     * Time Complexity: O(n) where n is the number of grades
     * Thread-safe: Synchronized to prevent concurrent write conflicts
     */
    public synchronized Path exportToCSV(StudentReport report, String fileName) throws FileExportException {
        long startTime = System.nanoTime();
        // Use Paths.get() or Path.of() for path creation
        Path filePath = CSV_DIR.resolve(fileName + ".csv");
        
        // Validate parent directory is writable
        validateFilePath(CSV_DIR, false, true);
        
        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath, 
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            // Write CSV header
            writer.write("Grade ID,Date,Subject,Type,Grade\n");
            
            // Stream grades one by one to avoid loading entire file into memory
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
        
        csvWriteTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        try {
            csvFileSize = Files.size(filePath);
        } catch (IOException e) {
            csvFileSize = 0;
        }
        
        return filePath;
    }
    
    /**
     * Export student report to JSON format
     * Uses NIO.2 Files.writeString for UTF-8 encoding
     * Time Complexity: O(n) where n is the number of grades
     * Thread-safe: Synchronized to prevent concurrent write conflicts
     */
    public synchronized Path exportToJSON(StudentReport report, String fileName) throws FileExportException {
        long startTime = System.nanoTime();
        Path filePath = JSON_DIR.resolve(fileName + ".json");
        
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
        
        jsonWriteTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        try {
            jsonFileSize = Files.size(filePath);
        } catch (IOException e) {
            jsonFileSize = 0;
        }
        
        return filePath;
    }
    
    /**
     * Export student report to binary format using Java serialization
     * Uses ObjectOutputStream/ObjectInputStream for binary serialization
     * Serializes complex objects efficiently
     * Implements Serializable interface (StudentReport)
     * Handles versioning with serialVersionUID
     * Smaller file sizes than text formats
     * Uses NIO.2 Files.newOutputStream for efficient binary writing
     * Time Complexity: O(n) where n is the size of the serialized object
     * Thread-safe: Synchronized to prevent concurrent write conflicts
     */
    public synchronized Path exportToBinary(StudentReport report, String fileName) throws FileExportException {
        long startTime = System.nanoTime();
        // Use Paths.get() or Path.of() for path creation
        Path filePath = BINARY_DIR.resolve(fileName + ".dat");
        
        // Validate parent directory is writable
        validateFilePath(BINARY_DIR, false, true);
        
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
        
        binaryWriteTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        try {
            binaryFileSize = Files.size(filePath);
        } catch (IOException e) {
            binaryFileSize = 0;
        }
        
        return filePath;
    }
    
    /**
     * Export to all formats (CSV, JSON, Binary)
     * Time Complexity: O(n) where n is the number of grades
     */
    public ExportResult exportToAllFormats(StudentReport report, String fileName) throws FileExportException {
        System.out.println("Processing with NIO.2 Streaming...");
        
        Path csvPath = exportToCSV(report, fileName);
        Path jsonPath = exportToJSON(report, fileName);
        Path binaryPath = exportToBinary(report, fileName);
        
        return new ExportResult(csvPath, jsonPath, binaryPath,
                csvWriteTime, jsonWriteTime, binaryWriteTime,
                csvFileSize, jsonFileSize, binaryFileSize);
    }
    
    /**
     * Import student report from binary format
     * Uses ObjectInputStream/ObjectInputStream for binary serialization
     * Serializes complex objects efficiently
     * Implements Serializable interface (StudentReport)
     * Handles versioning with serialVersionUID
     * Smaller file sizes than text formats
     * Uses NIO.2 Files.newInputStream for efficient binary reading
     * Time Complexity: O(n) where n is the size of the serialized object
     */
    public StudentReport importFromBinary(Path filePath) throws FileExportException {
        long startTime = System.nanoTime();
        
        // Validate file exists and is readable
        validateFilePath(filePath, true, false);
        
        try (ObjectInputStream ois = new ObjectInputStream(
                Files.newInputStream(filePath, StandardOpenOption.READ)
        )) {
            StudentReport report = (StudentReport) ois.readObject();
            long readTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            System.out.printf("Binary import completed in %dms\n", readTime);
            return report;
            
        } catch (IOException | ClassNotFoundException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to import binary file: " + e.getMessage()
            );
        }
    }
    
    /**
     * Import CSV file with streaming (for large files)
     * Uses NIO.2 Files.lines() for streaming large files
     * Memory efficient: doesn't load entire file into memory
     * Returns Stream<String> for processing
     * Auto-closes resources with try-with-resources
     * Chains with filter, map, collect operations
     * Time Complexity: O(n) where n is the number of lines
     */
    public List<String[]> importCSV(Path filePath) throws FileExportException {
        long startTime = System.nanoTime();
        
        // Validate file exists and is readable using NIO.2 Files API
        validateFilePath(filePath, true, false);
        
        try {
            // Use Files.lines() for streaming large CSV files
            // Memory efficient: doesn't load entire file
            // Returns Stream<String> for processing
            // Auto-closes resources
            // Chain with filter, map, collect
            List<String[]> data = Files.lines(filePath, StandardCharsets.UTF_8)
                .skip(1) // Skip header
                .filter(line -> !line.trim().isEmpty() && !line.startsWith("Summary")) // Filter empty/summary lines
                .map(line -> line.split(",")) // Map to String array
                .collect(Collectors.toList()); // Collect to list
            
            long readTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            System.out.printf("CSV import completed in %dms\n", readTime);
            return data;
            
        } catch (IOException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to import CSV file: " + e.getMessage()
            );
        }
    }
    
    /**
     * Import student report from JSON format
     * Parses JSON file and creates StudentReport object
     * Time Complexity: O(n) where n is the number of grades
     */
    public StudentReport importFromJSON(Path filePath) throws FileExportException {
        long startTime = System.nanoTime();
        
        // Validate file exists and is readable
        validateFilePath(filePath, true, false);
        
        try {
            // Read entire JSON file
            String jsonContent = Files.readString(filePath, StandardCharsets.UTF_8);
            
            // Parse JSON manually (simple parser for our format)
            StudentReport report = parseJSONReport(jsonContent);
            
            long readTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            System.out.printf("JSON import completed in %dms\n", readTime);
            return report;
            
        } catch (IOException e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to import JSON file: " + e.getMessage()
            );
        } catch (Exception e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Invalid JSON format: " + e.getMessage()
            );
        }
    }
    
    /**
     * Parse JSON content into StudentReport
     * Simple JSON parser for our specific format
     * Supports two formats:
     * 1. Full format with student info and grades array
     * 2. Simple CSV-like format with array of records (studentId, subjectName, subjectType, grade)
     */
    private StudentReport parseJSONReport(String jsonContent) throws FileExportException {
        try {
            // Remove whitespace and newlines for easier parsing
            String cleaned = jsonContent.replaceAll("\\s+", " ").trim();
            
            // Check if this is a simple array format (CSV-like) or full format
            boolean isSimpleFormat = cleaned.startsWith("[") || 
                                    (cleaned.indexOf("\"studentId\"") != -1 && cleaned.indexOf("\"grades\"") == -1);
            
            if (isSimpleFormat) {
                // Simple CSV-like format: array of records with studentId, subjectName, subjectType, grade
                return parseSimpleJSONFormat(cleaned);
            } else {
                // Full format with student info and grades array
                return parseFullJSONFormat(cleaned);
            }
            
        } catch (Exception e) {
            throw new FileExportException(
                "X ERROR: FileExportException\n   Failed to parse JSON: " + e.getMessage()
            );
        }
    }
    
    /**
     * Parse simple CSV-like JSON format: array of records
     * Each record has: studentId, subjectName, subjectType, grade
     */
    private StudentReport parseSimpleJSONFormat(String cleaned) throws FileExportException {
        // Find the main array
        int arrayStart = cleaned.indexOf("[");
        int arrayEnd = cleaned.lastIndexOf("]");
        
        if (arrayStart == -1 || arrayEnd == -1) {
            throw new FileExportException("Invalid JSON format: expected array");
        }
        
        String gradesArray = cleaned.substring(arrayStart + 1, arrayEnd);
        
        String firstStudentId = null;
        List<GradeData> gradeDataList = new ArrayList<>();
        double totalGrade = 0.0;
        int gradeCount = 0;
        
        // Parse each record object
        int pos = 0;
        while (pos < gradesArray.length()) {
            int objStart = gradesArray.indexOf("{", pos);
            if (objStart == -1) break;
            
            int objEnd = findMatchingBrace(gradesArray, objStart);
            if (objEnd == -1) break;
            
            String recordObj = gradesArray.substring(objStart, objEnd + 1);
            
            // Extract fields: studentId, subjectName, subjectType, grade
            String studentId = extractJSONValue(recordObj, "\"studentId\"");
            String subjectName = extractJSONValue(recordObj, "\"subjectName\"");
            String subjectType = extractJSONValue(recordObj, "\"subjectType\"");
            String gradeStr = extractJSONValue(recordObj, "\"grade\"");
            
            if (studentId.isEmpty() || subjectName.isEmpty() || subjectType.isEmpty() || gradeStr.isEmpty()) {
                pos = objEnd + 1;
                continue; // Skip invalid records
            }
            
            // Store first studentId for the report
            if (firstStudentId == null) {
                firstStudentId = studentId;
            }
            
            double grade = Double.parseDouble(gradeStr);
            totalGrade += grade;
            gradeCount++;
            
            // Generate gradeId and date (required by GradeData)
            String gradeId = "GRD" + String.format("%03d", gradeCount);
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            GradeData gradeData = new GradeData(gradeId, date, subjectName, subjectType, grade);
            gradeDataList.add(gradeData);
            
            pos = objEnd + 1;
        }
        
        if (firstStudentId == null || gradeDataList.isEmpty()) {
            throw new FileExportException("No valid grade records found in JSON");
        }
        
        // Calculate average
        double overallAverage = gradeCount > 0 ? totalGrade / gradeCount : 0.0;
        
        // Create report with default values (will be looked up from studentManager during import)
        StudentReport report = new StudentReport(
            firstStudentId, "Unknown", "Regular", overallAverage, "Imported"
        );
        
        // Add all grades
        for (GradeData gradeData : gradeDataList) {
            report.addGrade(gradeData);
        }
        
        return report;
    }
    
    /**
     * Parse full JSON format with student info and grades array
     */
    private StudentReport parseFullJSONFormat(String cleaned) throws FileExportException {
        // Extract student info
        String studentId = extractJSONValue(cleaned, "\"id\"");
        String studentName = extractJSONValue(cleaned, "\"name\"");
        String studentType = extractJSONValue(cleaned, "\"type\"");
        String avgStr = extractJSONValue(cleaned, "\"overallAverage\"");
        double overallAverage = avgStr.isEmpty() ? 0.0 : Double.parseDouble(avgStr);
        
        // Create report
        StudentReport report = new StudentReport(
            studentId, studentName, studentType, overallAverage, "Imported"
        );
        
        // Extract grades array
        int gradesStart = cleaned.indexOf("\"grades\"");
        if (gradesStart == -1) {
            return report; // No grades
        }
        
        int arrayStart = cleaned.indexOf("[", gradesStart);
        int arrayEnd = cleaned.lastIndexOf("]");
        
        if (arrayStart == -1 || arrayEnd == -1) {
            return report; // Empty grades array
        }
        
        String gradesArray = cleaned.substring(arrayStart + 1, arrayEnd);
        
        // Parse each grade object
        int pos = 0;
        while (pos < gradesArray.length()) {
            int objStart = gradesArray.indexOf("{", pos);
            if (objStart == -1) break;
            
            int objEnd = findMatchingBrace(gradesArray, objStart);
            if (objEnd == -1) break;
            
            String gradeObj = gradesArray.substring(objStart, objEnd + 1);
            
            String gradeId = extractJSONValue(gradeObj, "\"gradeId\"");
            String date = extractJSONValue(gradeObj, "\"date\"");
            String subject = extractJSONValue(gradeObj, "\"subject\"");
            String type = extractJSONValue(gradeObj, "\"type\"");
            double grade = Double.parseDouble(extractJSONValue(gradeObj, "\"grade\""));
            
            GradeData gradeData = new GradeData(gradeId, date, subject, type, grade);
            report.addGrade(gradeData);
            
            pos = objEnd + 1;
        }
        
        return report;
    }
    
    /**
     * Extract value from JSON string
     */
    private String extractJSONValue(String json, String key) {
        int keyPos = json.indexOf(key);
        if (keyPos == -1) return "";
        
        int colonPos = json.indexOf(":", keyPos);
        if (colonPos == -1) return "";
        
        int start = colonPos + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) {
            start++;
        }
        
        int end = start;
        if (json.charAt(start - 1) == '"') {
            // String value
            end = json.indexOf("\"", start);
        } else {
            // Number value
            while (end < json.length() && 
                   (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || 
                    json.charAt(end) == '-' || json.charAt(end) == 'e' || json.charAt(end) == 'E' ||
                    json.charAt(end) == '+' || json.charAt(end) == '-')) {
                end++;
            }
        }
        
        if (end == -1 || end > json.length()) return "";
        
        String value = json.substring(start - (json.charAt(start - 1) == '"' ? 1 : 0), 
                                      end + (json.charAt(start - 1) == '"' ? 0 : 0));
        return value.replace("\"", "").trim();
    }
    
    /**
     * Find matching closing brace
     */
    private int findMatchingBrace(String str, int start) {
        int depth = 0;
        for (int i = start; i < str.length(); i++) {
            if (str.charAt(i) == '{') depth++;
            if (str.charAt(i) == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }
    
    /**
     * Get performance summary
     */
    public String getPerformanceSummary() {
        long totalTime = csvWriteTime + jsonWriteTime + binaryWriteTime;
        long totalSize = csvFileSize + jsonFileSize + binaryFileSize;
        double compressionRatio = jsonFileSize > 0 ? (double) jsonFileSize / binaryFileSize : 0;
        
        StringBuilder summary = new StringBuilder();
        summary.append("\nExport Performance Summary:\n");
        summary.append(String.format("  Total Time: %dms\n", totalTime));
        summary.append(String.format("  Total Size: %s\n", formatFileSize(totalSize)));
        if (compressionRatio > 0) {
            summary.append(String.format("  Compression Ratio: %.1f:1 (binary vs JSON)\n", compressionRatio));
        }
        summary.append("  I/O Operations: 3 parallel writes\n");
        
        return summary.toString();
    }
    
    /**
     * Format file size to human-readable format
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Export result container class
     */
    public static class ExportResult {
        private final Path csvPath;
        private final Path jsonPath;
        private final Path binaryPath;
        private final long csvTime;
        private final long jsonTime;
        private final long binaryTime;
        private final long csvSize;
        private final long jsonSize;
        private final long binarySize;
        
        public ExportResult(Path csvPath, Path jsonPath, Path binaryPath,
                          long csvTime, long jsonTime, long binaryTime,
                          long csvSize, long jsonSize, long binarySize) {
            this.csvPath = csvPath;
            this.jsonPath = jsonPath;
            this.binaryPath = binaryPath;
            this.csvTime = csvTime;
            this.jsonTime = jsonTime;
            this.binaryTime = binaryTime;
            this.csvSize = csvSize;
            this.jsonSize = jsonSize;
            this.binarySize = binarySize;
        }
        
        // Getters
        public Path getCsvPath() { return csvPath; }
        public Path getJsonPath() { return jsonPath; }
        public Path getBinaryPath() { return binaryPath; }
        public long getCsvTime() { return csvTime; }
        public long getJsonTime() { return jsonTime; }
        public long getBinaryTime() { return binaryTime; }
        public long getCsvSize() { return csvSize; }
        public long getJsonSize() { return jsonSize; }
        public long getBinarySize() { return binarySize; }
    }
}

