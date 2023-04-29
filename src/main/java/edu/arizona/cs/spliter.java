package edu.arizona.cs;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class spliter {

    public static void main(String[] args) {

        String inputFilePath = "questionposts.csv";
        String outputFilePathPrefix = "questionposts_";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {

            String header = reader.readLine();
            String line;

            // Split the file into 10 parts
            int partSize = countLines(inputFilePath) / 5;
            int partNumber = 1;
            int linesInPart = 0;

            FileWriter writer = new FileWriter(outputFilePathPrefix + partNumber + ".csv");
            writer.write(header + "\n");

            while ((line = reader.readLine()) != null) {

                if (linesInPart == partSize) {
                    writer.close();
                    partNumber++;
                    writer = new FileWriter(outputFilePathPrefix + partNumber + ".csv");
                    writer.write(header + "\n");
                    linesInPart = 0;
                }

                writer.write(line + "\n");
                linesInPart++;
            }

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done");
    }

    private static int countLines(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }
}