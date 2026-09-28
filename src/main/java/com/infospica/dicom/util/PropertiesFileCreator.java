package com.infospica.dicom.util;

import com.infospica.dicom.Constants;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.state.batch.response.upload.FileScanErrorFilter;
import com.infospica.dicom.process.state.batch.response.upload.UploadErrorFilter;
import com.infospica.dicom.process.state.batch.response.upload.UploadResponseParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PropertiesFileCreator {
    private File configurationFolder = new File("C:\\Infospica-Workspace\\eclarity-client\\deploy\\default-conf");
    private String startupJson = "{\n" +
            "  \"startupType\" : \"Manual\",\n" +
            "  \"retryInterval\" : 1,\n" +
            "  \"echoInterval\" : 60,\n" +
            "  \"echoServer\" : false,\n" +
            "  \"executionMode\" : \"BatchPreference\",\n" +
            "  \"salt\": \"e640aa89326ee03cb2d0c949fc72378b220bb01c2b17f35af103f0bc8d75207bc8871d84386379775ac46d1af99d03418ea79682430deb147f31f541c5661c3e\",\n" +
            "  \"password\": \"48ec2edbf0ba64a1e2678211eb2777dff0245f65a26eb15e4e22ca4f1d2ff8964e6132d498c5afd116e28a626a944b869131da0e2fb025eaf6cee05a6e9759d9\",\n" +
            "  \"configRequired\" : true\n" +
            "}";

    private String scpJson = "{\n" +
            "  \"host\" : \"\",\n" +
            "  \"aet\" : \"SRCPACS\",\n" +
            "  \"port\" : 11112,\n" +
            "  \"commandFolder\" : \".\\\\dcm4che\\\\bin\\\\\",\n" +
            "  \"destFolder\" : \".\\\\tmp\\\\pending\\\\\",\n" +
            "  \"processFolder\" : \".\\\\tmp\\\\process\\\\\",\n" +
            "  \"requestTimeout\" : 5000,\n" +
            "  \"releaseTimeout\" : 5000,\n" +
            "  \"anonymizeInstitutionName\" : false,\n" +
            "  \"anonymizeInstitutionAddress\" : false,\n" +
            "  \"anonymizePatientId\" : false,\n" +
            "  \"anonymizePatientName\" : false,\n" +
            "  \"anonymizeReferringPhysician\" : false,\n" +
            "  \"jsonFromDicom\" : false,\n" +
            "  \"doCompression\" : true,\n" +
            "  \"compressionMethod\" : \"JPEG 2000 Lossless\",\n" +
            "  \"allowFork\" : \"No\",\n" +
            "  \"debugLevel\" : \"Info\",\n" +
            "  \"logFolder\" : \".\\\\logs\\\\\"\n" +
            "}";
    private String scuJson = "{\n" +
            "  \"host\" : \"demo.eclarityhealth.com\",\n" +
            "  \"aet\" : \"DCM4CHEE\",\n" +
            "  \"port\" : 11112,\n" +
            "  \"callingAet\" : \"SRCPACS\",\n" +
            "  \"callingHost\" : \"\",\n" +
            "  \"uploadBackup\" : \".\\\\tmp\\\\completed\\\\\",\n" +
            "  \"keepImagesFor\": 0,\n" +
            "  \"imageCleanupMinute\": 1,\n" +
            "  \"maxFilesPerUpload\": 10\n" +
            "}";

    public String getStartupJson() {
        return startupJson;
    }

    public String getScpJson() {
        return scpJson;
    }

    public String getScuJson() {
        return scuJson;
    }

    public String createDefaultPassword(String password) throws Exception {
        String salt = Security.getRandomToken();
        System.out.println("salt --" + salt);
        return Security.encodeString(password + salt);
    }

    public String createEncryptedConfigurationFile(String encodedInput, String fileName) throws Exception {
        String encryptedContent = ContentEncoderDecoderUtility.getEncryptedContent(encodedInput);
        File configurationFile = new File(configurationFolder, fileName);
        FileWriter fw = new FileWriter(configurationFile);
        IOUtils.write(encryptedContent, fw);
        IOUtils.close(fw);
        return "S";
    }

    public static void main(String[] args) throws Exception {
        PropertiesFileCreator pc = new PropertiesFileCreator();
        //System.out.println(pc.createDefaultPassword("eCScribe@2023"));

        /*pc.createEncryptedConfigurationFile(
                Base64.getEncoder().encodeToString(pc.getStartupJson().getBytes("utf8")), "startup_props.conf");

        pc.createEncryptedConfigurationFile(
                Base64.getEncoder().encodeToString(pc.getScpJson().getBytes("utf8")), "store_scp_props.conf");

        pc.createEncryptedConfigurationFile(
                Base64.getEncoder().encodeToString(pc.getScuJson().getBytes("utf8")), "store_scu_props.conf");*/


        /*String path = "app-error-logger-2023-06-06_091144_222.0.log";
        final Pattern pattern = Pattern.compile("^app-error-logger-(\\d+-\\d+-\\d+)(_)([0-9]+)_([0-9]{3}+).*$");
        final Matcher matcher = pattern.matcher("");
        matcher.reset(path);
        matcher.find();
        System.out.printf("{%s} - {%s} - {%s}", matcher.group(1), matcher.group(3), matcher.group(4));*/


        String msg = "E\"ERROR: Received C-STORE-RSP with Status 0110H for C:\\eclarity\\tmp\\todicom\\4D82E84F\"";
        String msg1 = "Failed to scan file C:\\eclarity\\tmp\\todicom\\3C1A86E4";

        String msg2 = "Failed to scan file C:\\eclarity\\tmp\\todicom\\3C1A86E4: null\n" +
                "java.io.EOFException\n" +
                "        at org.dcm4che3.util.StreamUtils.readFully(StreamUtils.java:69)\n" +
                "        at org.dcm4che3.io.DicomInputStream.readFully(DicomInputStream.java:498)\n" +
                "        at org.dcm4che3.io.DicomInputStream.readValue(DicomInputStream.java:970)\n" +
                "        at org.dcm4che3.io.DicomInputStream.readValue(DicomInputStream.java:735)\n" +
                "        at org.dcm4che3.io.DicomInputStream.readAttributes(DicomInputStream.java:703)\n" +
                "        at org.dcm4che3.io.DicomInputStream.readDataset(DicomInputStream.java:629)\n" +
                "        at org.dcm4che3.io.DicomInputStream.readDataset(DicomInputStream.java:616)\n" +
                "        at org.dcm4che3.io.DicomInputStream.readDatasetUntilPixelData(DicomInputStream.java:598)\n" +
                "        at org.dcm4che3.tool.common.DicomFiles.scan(DicomFiles.java:108)\n" +
                "        at org.dcm4che3.tool.common.DicomFiles.scan(DicomFiles.java:80)\n" +
                "        at org.dcm4che3.tool.common.DicomFiles.scan(DicomFiles.java:74)\n" +
                "        at org.dcm4che3.tool.storescu.StoreSCU.scanFiles(StoreSCU.java:389)\n" +
                "        at org.dcm4che3.tool.storescu.StoreSCU.scanFiles(StoreSCU.java:379)\n" +
                "        at org.dcm4che3.tool.storescu.StoreSCU.main(StoreSCU.java:267)\n" +
                "..\n" +
                "Scanned 9 files in 0.396s (=44ms/file)\n" +
                "Connected to DCM4CHEE in 122ms\n" +
                "E\"ERROR: Received C-STORE-RSP with Status 0110H for C:\\eclarity\\tmp\\todicom\\0B8226A2\"\n" +
                "(0000,0100) US [32769] CommandField\n" +
                "(0000,0120) US [1] MessageIDBeingRespondedTo\n" +
                "(0000,0800) US [257] CommandDataSetType\n" +
                "(0000,0900) US [272] Status\n" +
                "(0000,0902) LO [java.nio.file.AccessDeniedException: /storage/fs1/2023/08/08]\n" +
                "\n" +
                "E\"ERROR: Received C-STORE-RSP with Status 0110H for C:\\eclarity\\tmp\\todicom\\0BE3A5A2\"\n" +
                "(0000,0100) US [32769] CommandField\n" +
                "(0000,0120) US [2] MessageIDBeingRespondedTo\n" +
                "(0000,0800) US [257] CommandDataSetType\n" +
                "(0000,0900) US [272] Status\n" +
                "(0000,0902) LO [java.nio.file.AccessDeniedException: /storage/fs1/2023/08/08]\n" +
                "\n" +
                "E\"ERROR: Received C-STORE-RSP with Status 0110H for C:\\eclarity\\tmp\\todicom\\1A7E4F82\"\n" +
                "(0000,0100) US [32769] CommandField\n" +
                "(0000,0120) US [3] MessageIDBeingRespondedTo\n" +
                "(0000,0800) US [257] CommandDataSetType\n" +
                "(0000,0900) US [272] Status\n" +
                "(0000,0902) LO [java.nio.file.AccessDeniedException: /storage/fs1/2023/08/08]\n" +
                "\n" +
                "E\"ERROR: Received C-STORE-RSP with Status 0110H for C:\\eclarity\\tmp\\todicom\\1B98048C\"\n" +
                "(0000,0100) US [32769] CommandField\n" +
                "(0000,0120) US [4] MessageIDBeingRespondedTo\n" +
                "(0000,0800) US [257] CommandDataSetType\n" +
                "(0000,0900) US [272] Status\n" +
                "(0000,0902) LO [java.nio.file.AccessDeniedException: /storage/fs1/2023/08/08]\n" +
                "\n" +
                "E\"ERROR: Received C-STORE-RSP with Status 0110H for C:\\eclarity\\tmp\\todicom\\1BB20E00\"\n" +
                "(0000,0100) US [32769] CommandField\n" +
                "(0000,0120) US [5] MessageIDBeingRespondedTo\n" +
                "(0000,0800) US [257] CommandDataSetType\n" +
                "(0000,0900) US [272] Status\n" +
                "(0000,0902) LO [java.nio.file.AccessDeniedException: /storage/fs1/2023/08/08]\n" +
                "\n" +
                "E\"ERROR: Received C-STORE-RSP with Status 0110H for C:\\eclarity\\tmp\\todicom\\2CA15F35\"\n" +
                "(0000,0100) US [32769] CommandField\n" +
                "(0000,0120) US [6] MessageIDBeingRespondedTo\n" +
                "(0000,0800) US [257] CommandDataSetType\n" +
                "(0000,0900) US [272] Status\n" +
                "(0000,0902) LO [java.nio.file.AccessDeniedException: /storage/fs1/2023/08/08]\n" +
                "\n" +
                "E\"ERROR: Received C-STORE-RSP with Status 0110H for C:\\eclarity\\tmp\\todicom\\2CE39FF4\"\n" +
                "(0000,0100) US [32769] CommandField\n" +
                "(0000,0120) US [7] MessageIDBeingRespondedTo\n" +
                "(0000,0800) US [257] CommandDataSetType\n" +
                "(0000,0900) US [272] Status\n" +
                "(0000,0902) LO [java.nio.file.AccessDeniedException: /storage/fs1/2023/08/08]\n" +
                "\n" +
                "E\"ERROR: Received C-STORE-RSP with Status 0110H for C:\\eclarity\\tmp\\todicom\\3D6F2768\"\n" +
                "(0000,0100) US [32769] CommandField\n" +
                "(0000,0120) US [8] MessageIDBeingRespondedTo\n" +
                "(0000,0800) US [257] CommandDataSetType\n" +
                "(0000,0900) US [272] Status\n" +
                "(0000,0902) LO [java.nio.file.AccessDeniedException: /storage/fs1/2023/08/08]\n" +
                "\n" +
                "E\"ERROR: Received C-STORE-RSP with Status 0110H for C:\\eclarity\\tmp\\todicom\\4D82E84F\"\n" +
                "(0000,0100) US [32769] CommandField\n" +
                "(0000,0120) US [9] MessageIDBeingRespondedTo\n" +
                "(0000,0800) US [257] CommandDataSetType\n" +
                "(0000,0900) US [272] Status\n" +
                "(0000,0902) LO [java.nio.file.AccessDeniedException: /storage/fs1/2023/08/08]";

        String errorPattern = "E\"ERROR: Received C-STORE-RSP with Status (\\d+)H for (.*)\"";
        String sentPattern = "^([\\.]*)Sent+";

        UploadResponseParser uploadResponseParser = new UploadResponseParser();
        FileScanErrorFilter fileScanErrorFilter = new FileScanErrorFilter(uploadResponseParser);
        UploadErrorFilter uploadErrorFilter = new UploadErrorFilter(uploadResponseParser);

        fileScanErrorFilter.setNextFilter(uploadErrorFilter);

        uploadResponseParser.setResponse(msg2);
        fileScanErrorFilter.applyFilter();
        System.out.println("------- " + uploadResponseParser.getFileList());
        uploadResponseParser.reset();


    }
}
