package com.gradr;

import com.gradr.exceptions.CSVParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CSVParser {
    private String filePath;

    /**
     * Constructor for CSVParser
     * @param filePath Path to the CSV file
     */
    public CSVParser(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Parses the CSV file and returns an array of grade data
     * Expected CSV format: StudentID,SubjectName,SubjectType,Grade
     * @return ArrayList of String arrays containing grade data
     * @throws IOException if file cannot be read
     */
    public ArrayList<String[]> parseGradeCSV() throws IOException, CSVParseException {
        ArrayList<String[]> gradeData = new ArrayList<>();
        BufferedReader reader = null;

        try {
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
        }

        return gradeData;
    }

    /**
     * Validates a single grade entry
     * @param data Array containing [StudentID, SubjectName, SubjectType, Grade]
     * @return true if data is valid, false otherwise
     */
    public boolean validateGradeEntry(String[] data) {
        if (data.length != 4) return false;

        // Validate student ID format (STUxxx)
        if (!data[0].matches("STU\\d{3}")) return false;

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

        // Validate grade (must be a number between 0 and 100)
        try {
            double grade = Double.parseDouble(data[3]);
            if (grade < 0 || grade > 100) return false;
        } catch (NumberFormatException e) {
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
