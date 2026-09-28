package com.infospica.dicom.util;

import org.apache.commons.io.FileUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtility {

    /**
     * Returns the retry count from a filename like file#ERROR#2. Returns 1 if not present.
     */
    public static int getErrorRetryCount(File file) {
        String name = file.getName();
        if (name.contains("#ERROR#")) {
            try {
                return Integer.parseInt(name.substring(name.lastIndexOf("#ERROR#") + 7));
            } catch (Exception ignored) {}
        }
        return 1;
    }

    /**
     * Returns a new File with the retry count incremented (or set to 1 if not present).
     */
    public static File incrementErrorRetry(File file) {
        String name = file.getName();
        int count = getErrorRetryCount(file);
        String base;
        if (name.contains("#ERROR#")) {
            base = name.substring(0, name.lastIndexOf("#ERROR#"));
        } else if (name.endsWith("#ERROR")) {
            base = name.substring(0, name.length() - 6);
        } else {
            base = name;
        }
        return new File(file.getParentFile(), base + "#ERROR#" + (count + 1));
    }

    /**
     * Returns a new File with #ERROR#count removed from the name.
     */
    public static File removeErrorRetry(File file) {
        String name = file.getName();
        String base = name;
        if (name.contains("#ERROR#")) {
            base = name.substring(0, name.lastIndexOf("#ERROR#"));
        } else if (name.endsWith("#ERROR")) {
            base = name.substring(0, name.length() - 6);
        }
        return new File(file.getParentFile(), base);
    }

    private FileUtility() {
        super();
    }

    public static void makeDirectories(File file) {
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    public static File newDestinationFile(File file, String srcRoot, File destRoot) {
        String sabPath = getRelativePathFrom(file.getAbsolutePath(), srcRoot);
        return new File(destRoot, sabPath);
    }

    public static File newDestinationFile(File file, String srcRoot, File destRoot, boolean randomize) {
        String sabPath = randomize ? getRelativePathFrom(file.getAbsolutePath(), srcRoot)
                : getRelativePathFrom(file.getAbsolutePath(), srcRoot, randomize);
        return new File(destRoot, sabPath);
    }

    public static File moveAsErrorFile(File errorFile, File destFile, String suffix) throws IOException {
        if(destFile == null) {
            destFile = new File(errorFile.getParentFile(), errorFile.getName());
        }
        File srcFile = new File(errorFile.getParentFile(), errorFile.getName().replaceAll(suffix, ""));
        FileUtils.moveFile(srcFile, destFile);
        return destFile;
    }

    public static String getRelativePathFrom(String srcPath, String basePath) {
        Path absPath = Paths.get(srcPath);
        Path absBasePath = Paths.get(basePath);
        return absBasePath.relativize(absPath).toString();
    }

    public static String getRelativePathFrom(String srcPath, String basePath, boolean randomize) {
        Path absPath = Paths.get(srcPath);
        Path relativePath = Paths.get(basePath).relativize(absPath);
        Path parentPath = relativePath.getParent();
        String[] extension = getExtension(relativePath);
        String newFileId = extension[0] + "--" + UUID.randomUUID().toString() + extension[1];
        Path newFilename = Paths.get(newFileId);
        Path newRelativePath = (parentPath != null) ? parentPath.resolve(newFilename) : newFilename;
        return newRelativePath.toString();
    }

    public static String getRestoredPath(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        int dashIndex = fileName.indexOf("--");
        String fileNameRestored = dashIndex >= 0 ? fileName.substring(0, dashIndex) : fileName;
        fileNameRestored += dotIndex >= 0 ? fileName.substring(dotIndex) : "";
        return fileNameRestored;
    }

    public static String[] getExtension(Path relativePath) {
        //Path absPath = Paths.get(givenPath);
        String fileName = relativePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf(".");
        String[] extension = new String[2];
        if(dotIndex >= 0) {
            extension[0] = fileName.substring(0, dotIndex);
            extension[1] = fileName.substring(dotIndex);
        } else {
            extension[0] = fileName;
            extension[1] = "";
        }
        return extension;
    }

    public static Mono<File> renameFile(File file, Scheduler scheduler) {
        return Mono.fromCallable(() -> {
            //if(file.exists()) {
            File newFile = new File(file.getParentFile(), file.getName().replaceAll("#ERROR", ""));
            FileUtils.moveFile(file, newFile);
            //}
            return file;
        }).subscribeOn(scheduler);
    }
}
