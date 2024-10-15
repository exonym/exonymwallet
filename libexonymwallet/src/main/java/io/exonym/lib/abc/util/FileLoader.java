package io.exonym.lib.abc.util;


import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

public class FileLoader {
    

    public static Map<String, ByteArrayOutputStream> loadFilesAsMap(Path folderPath) throws IOException {
        Map<String, ByteArrayOutputStream> filesMap = new HashMap<>();

        if (Files.isDirectory(folderPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
                for (Path filePath : stream) {
                    if (Files.isRegularFile(filePath) && !Files.isHidden(filePath)) {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        Files.copy(filePath, buffer);
                        filesMap.put(filePath.getFileName().toString(), buffer);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Provided path is not a directory: " + folderPath);
        }
        return filesMap;

    }

}
