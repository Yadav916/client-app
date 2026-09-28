package com.infospica.dicom.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DirectoryUtility {

    public static int getFileCount(Path filePath, int fileCount) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(filePath)) {
            Spliterator<Path> spliterator = directoryStream.spliterator();
            if (spliterator.estimateSize() == 0) {
                return fileCount;
            }
            Stream<Path> pathStream = StreamSupport.stream(spliterator, true);
            List<Path> paths = pathStream.filter(p -> Files.isDirectory(p))
                    .filter(p -> {
                        try {
                            return !Files.isHidden(p);
                        } catch (IOException e) { }
                        return false;
                    }).collect(Collectors.toList());;
            for(Path path: paths) {
                if(Files.isDirectory(path)) {
                    fileCount = getFileCount(path, fileCount);
                }
            }
            fileCount += getFileCount(filePath);
        } catch (IOException e) {
            // Handle IOException
        }
        return fileCount;
    }

    public static int getFileCount(Path filePath) throws IOException {
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(filePath);) {
            if (directoryStream != null)
                return new Long(StreamSupport.stream(directoryStream.spliterator(), true)
                        .filter(p -> p.toFile().isFile())
                        .filter(p -> {
                            try {
                                return !Files.isHidden(p);
                            } catch (IOException e) { }
                            return false;
                        })                        .count()).intValue();
        } catch (IOException ioe) {

        }
        return 0;
    }
}
