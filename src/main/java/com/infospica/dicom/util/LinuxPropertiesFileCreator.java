package com.infospica.dicom.util;

import com.infospica.dicom.process.state.batch.response.upload.FileScanErrorFilter;
import com.infospica.dicom.process.state.batch.response.upload.UploadErrorFilter;
import com.infospica.dicom.process.state.batch.response.upload.UploadResponseParser;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.Base64;

public class LinuxPropertiesFileCreator {
    private File configurationFolder = new File("C:\\Infospica-Workspace\\eclarity-client\\deploy\\default-conf-linux");
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
            "  \"commandFolder\" : \"./dcm4che/bin/\",\n" +
            "  \"destFolder\" : \"./tmp/pending/\",\n" +
            "  \"processFolder\" : \"./tmp/process/\",\n" +
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
            "  \"logFolder\" : \"./logs/\"\n" +
            "}";
    private String scuJson = "{\n" +
            "  \"host\" : \"demo.eclarityhealth.com\",\n" +
            "  \"aet\" : \"DCM4CHEE\",\n" +
            "  \"port\" : 11112,\n" +
            "  \"callingAet\" : \"SRCPACS\",\n" +
            "  \"callingHost\" : \"\",\n" +
            "  \"uploadBackup\" : \"./tmp/completed/\",\n" +
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
        LinuxPropertiesFileCreator pc = new LinuxPropertiesFileCreator();
        //System.out.println(pc.createDefaultPassword("eCScribe@2023"));

        pc.createEncryptedConfigurationFile(
                Base64.getEncoder().encodeToString(pc.getStartupJson().getBytes("utf8")), "startup_props.conf");

        pc.createEncryptedConfigurationFile(
                Base64.getEncoder().encodeToString(pc.getScpJson().getBytes("utf8")), "store_scp_props.conf");

        pc.createEncryptedConfigurationFile(
                Base64.getEncoder().encodeToString(pc.getScuJson().getBytes("utf8")), "store_scu_props.conf");

    }
}
