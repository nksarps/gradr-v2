package com.gradr;

import com.gradr.exceptions.FileExportException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class FileExporter {
    private String fileLocation;

    /**
     * Constructor for the FileExporter
     * @param fileName
     */
    public FileExporter(String fileName) throws FileExportException {
        this.fileLocation = String.format("./reports/%s.txt", fileName);

        try {
            Files.createDirectories(Paths.get("./reports"));
        } catch (IOException e) {
            throw new FileExportException("Failed to create reports directory");
        }
    }

    /**
     * Method that exports student's grades to .txt files
     * @param content
     * @throws FileExportException
     */
    public void exportGradeToTXT(String content) throws FileExportException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            throw new FileExportException("Failed to export grades to file: " + fileLocation);
        }

    }
}
