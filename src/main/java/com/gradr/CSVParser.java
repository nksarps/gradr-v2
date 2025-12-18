package com.gradr;

import com.gradr.exceptions.CSVParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CSVParser {
    private String filePath;
    private SystemPerformanceMonitor performanceMonitor;

    /**
     * Constructor for CSVParser
     * @param filePath Path to the CSV file
     */
    public CSVParser(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Constructor for CSVParser with performance monitoring
     * @param filePath Path to the CSV file
     * @param performanceMonitor SystemPerformanceMonitor for tracking I/O operations
     */
    public CSVParser(String filePath, SystemPerformanceMonitor performanceMonitor) {
        this.filePath = filePath;
        this.performanceMonitor = performanceMonitor;
    }

    /**
     * Parses the CSV file and returns an array of grade data
     * Expected CSV format: StudentID,SubjectName,SubjectType,Grade
     * @return ArrayList of String arrays containing grade data
     * @throws IOException if file cannot be read
     */
    public ArrayList<String[]> parseGradeCSV() throws IOException, CSVParseException {
        long startTime = System.currentTimeMillis();
        ArrayList<String[]> gradeData = new ArrayList<>();
        BufferedReader reader = null;
        long fileSize = 0;

        try {
            // Get file size for performance tracking
            try {
                fileSize = Files.size(Paths.get(filePath));
            } catch (IOException e) {
                fileSize = 0;
            }

            reader = new BufferedReader(new FileReader(filePath));
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Split by comma
                String[] data = line.split(",");

                // Trim whitespace from each field
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].trim();
                }

                // Validate data has correct number of fields
                if (data.length == 4) {
                    gradeData.add(data);
                }
            }
        } catch (IOException e) {
            throw new CSVParseException("X ERROR: CSVParseException\n   Failed to read new CSV file: " + filePath);
        } finally {
            if (reader != null) {
                reader.close();
            }

            // Record I/O operation for performance monitoring
            if (performanceMonitor != null) {
                long duration = System.currentTimeMillis() - startTime;
                String filename = Paths.get(filePath).getFileName().toString();
                performanceMonitor.recordIOOperation("CSV Read", filename, duration, fileSize, true);
            }
        }

        return gradeData;
    }

    /**
     * Validates a single grade entry using compiled regex patterns
     * @param data Array containing [StudentID, SubjectName, SubjectType, Grade]
     * @return true if data is valid, false otherwise
     */
    public boolean validateGradeEntry(String[] data) {
        if (data.length != 4) return false;

        // Validate student ID format using compiled pattern
        try {
            ValidationUtils.validateStudentId(data[0]);
        } catch (com.gradr.exceptions.InvalidStudentDataException e) {
            return false;
        }

        // Validate subject type
        String subjectType = data[2];
        if (!subjectType.equals("Core") && !subjectType.equals("Elective")) {
            return false;
        }

        // Validate subject name based on type
        String subjectName = data[1];
        if (subjectType.equals("Core")) {
            if (!subjectName.equals("Mathematics") &&
                    !subjectName.equals("English") &&
                    !subjectName.equals("Science")) {
                return false;
            }
        } else {
            if (!subjectName.equals("Music") &&
                    !subjectName.equals("Art") &&
                    !subjectName.equals("Physical Education")) {
                return false;
            }
        }

        // Validate grade using compiled pattern
        try {
            ValidationUtils.validateGrade(data[3]);
        } catch (com.gradr.exceptions.InvalidStudentDataException e) {
            return false;
        }

        return true;
    }

    /**
     * Gets the file path
     * @return file path
     */
    public String getFilePath() {
        return filePath;
    }
}
