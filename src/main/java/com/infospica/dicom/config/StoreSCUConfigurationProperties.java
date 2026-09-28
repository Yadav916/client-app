package com.infospica.dicom.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infospica.dicom.config.iface.Persistable;
import com.infospica.dicom.config.iface.Restorable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.io.Serializable;

@Data
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreSCUConfigurationProperties implements Serializable, Persistable, Restorable<StoreSCUConfigurationProperties> {

    private static final long serialVersionUID = 61323L;
    
    @JsonProperty("host")
    private String hostName;

    @JsonProperty("aet")
    private String aetName;

    @JsonProperty("port")
    private Integer port;

    @JsonProperty("uploadBackup")
    private String uploadBackup;

    @JsonProperty("keepImagesFor")
    private Integer keepImagesFor = -1;

    @JsonProperty("imageCleanupMinute")
    private Integer imageCleanupMinute;

    @JsonProperty("callingAet")
    private String callingAetName;

    @JsonProperty("callingHost")
    private String callingHost;

    @JsonProperty("maxFilesPerUpload")
    private Integer maxFilesPerUpload;

    @JsonIgnore
    private File filePath;

    @Override
    public void restore(StoreSCUConfigurationProperties obj) {
        setHostName(obj.getHostName());
        setAetName(obj.getAetName());
        setPort(obj.getPort());
        setUploadBackup(obj.getUploadBackup());
        setImageCleanupMinute(obj.getImageCleanupMinute());
        setCallingAetName(obj.getCallingAetName());
        setCallingHost(obj.getCallingHost());
    }
}
