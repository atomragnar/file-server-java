package server.utils;

import server.FileDirectory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    public static boolean exists(String fileName) {
        File file = new File(FileDirectory.filesPath + fileName);
        return file.exists();
    }

    private static final File DATA_DIRECTORY = new File(FileDirectory.filesPath);

    public static byte[] getFileBytes(String fileName) throws IOException {
        return Files.readAllBytes(Path.of(FileDirectory.filesPath + fileName));
    }

    private static void createDataDir() {
        if (!DATA_DIRECTORY.exists()) {
            DATA_DIRECTORY.mkdirs();
        }
    }

    public static void saveFile(String fileName, byte[] fileBytes) {
        createDataDir();
        Path path = Path.of(FileDirectory.filesPath + fileName);
        if (Files.notExists(path)) {
            try {
                Files.createFile(path);
                Files.write(path, fileBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(fileName + "already exists!");
        }
    }

    public static void deleteFile(String filename) throws IOException {
        final String filePath = FileDirectory.filesPath + filename;
        Files.delete(Path.of(filePath));
    }

}
