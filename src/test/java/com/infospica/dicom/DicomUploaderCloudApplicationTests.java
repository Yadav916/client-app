package com.infospica.dicom;

import com.infospica.dicom.context.Context;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class DicomUploaderCloudApplicationTests {

	public static void main(String[] args) throws IOException {
		int fcount = 0;
		long start = System.currentTimeMillis();
		fcount = getFileCount(Paths.get("C:\\Downloads\\dicomfiles\\DICOM\\2B4388C6\\"), fcount);
		System.out.println(fcount);
		long stop = System.currentTimeMillis();
		System.out.println(" --- " + (stop - start));

		long start1 = System.currentTimeMillis();
		Collection<File> files = FileUtils.listFiles(new File("C:\\Downloads\\dicomfiles\\DICOM\\2B4388C6\\"),
				TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		System.out.println(files.size());
		long stop1 = System.currentTimeMillis();
		System.out.println(" --- " + (stop1 - start1));
	}

	public static int getFileCount(Path filePath, int fileCount) throws IOException {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(filePath)) {
			Spliterator<Path> spliterator = directoryStream.spliterator();
			if (spliterator.estimateSize() == 0) {
				return fileCount;
			}

			Stream<Path> pathStream = StreamSupport.stream(spliterator, true);
			List<Path> paths = pathStream.filter(p -> Files.isDirectory(p)).collect(Collectors.toList());;
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
						.filter(p -> p.toFile().isFile()).count()).intValue();
		} catch (IOException ioe) {

		}
		return 0;
	}
}
