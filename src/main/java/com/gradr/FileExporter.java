package com.gradr;

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
    public FileExporter(String fileName) {
        this.fileLocation = String.format("./reports/%s.txt", fileName);

        try {
            Files.createDirectories(Paths.get("./reports"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create reports directory");
        }
    }

    /**
     * Method that exports student's grades to .txt files
     * @param content
     * @throws IOException
     */
    public void exportGradeToTXT(String content) throws IOException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            throw new IOException("Grades failed to be exported");
        }

    }
}
