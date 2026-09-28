package com.infospica.dicom;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

public class RelativePathTest {

    public static void main(String[] args) {
        Object[] result = getRelativePathFrom("C:\\QMICBitbucketWorkspace-2\\wain-enterprise-configuration\\wain-enterprise\\abc.xml",
                "C:\\QMICBitbucketWorkspace-2\\wain-enterprise-configuration\\", true);
        System.out.println(" >>> " + Arrays.toString(result));
        System.out.println(getRestoredPath("abc--09e73e22-bbd6-4b8f-9827-e812b3113a62.xml#ERROR"));

    }

    public static String getRelativePathFrom(String srcPath, String basePath) {
        Path absPath = Paths.get(srcPath);
        Path absBasePath = Paths.get(basePath);
        return absBasePath.relativize(absPath).toString();
    }

    public static Object[] getRelativePathFrom(String srcPath, String basePath, boolean randomize) {
        Path absPath = Paths.get(srcPath);
        Path relativePath = Paths.get(basePath).relativize(absPath);
        Path parentPath = relativePath.getParent();
        String[] extension = getExtension(relativePath);
        String newFileId = extension[0] + "--" + UUID.randomUUID().toString() + extension[1];
        Path newFilename = Paths.get(newFileId);
        Path newRelativePath = (parentPath != null) ? parentPath.resolve(newFilename) : newFilename;
        return new Object[] {newRelativePath.toString(), relativePath.toString()};
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
}
