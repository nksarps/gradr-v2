package com.gradr;

import com.gradr.exceptions.FileExportException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class FileExporter {
    // The location of the file to be exported eg. ./reports/fileName.txt
    private String fileLocation;

    /**
     * Constructor for the FileExporter
     * @param fileName - The name of the file to be exported eg. "report1"
     * @throws FileExportException - If the file cannot be created
     */
    public FileExporter(String fileName) throws FileExportException {
        this.fileLocation = String.format("./reports/%s.txt", fileName);

        // Creating the directory to store the exported files if it doesn't exist
        try {
            Files.createDirectories(Paths.get("./reports"));
        } catch (IOException e) {
            throw new FileExportException("X ERROR: FileExportException\n   Failed to create reports directory");
        }
    }

    /**
     * Method that exports student's grades to .txt files
     * @param content - The content of the file to be exported  
     * @throws FileExportException
     */
    public void exportGradeToTXT(String content) throws FileExportException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            throw new FileExportException("X ERROR: FileExportException\n   Failed to export grades to file: " + fileLocation);
        }

    }
}
