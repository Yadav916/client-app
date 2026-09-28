package com.infospica.dicom.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.infospica.dicom.config.iface.Persistable;
import com.infospica.dicom.config.iface.Restorable;
import lombok.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

@Data
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreSCPConfigurationProperties implements Serializable, Persistable, Restorable<StoreSCPConfigurationProperties> {

    private static final long serialVersionUID = 32456L;
    
    @JsonProperty("host")
    private String hostName;

    @JsonProperty("aet")
    private String aetName;

    @JsonProperty("port")
    private Integer port;

    @JsonProperty("commandFolder")
    private String commandFolder;

    @JsonProperty("destFolder")
    private String destinationFolder;

    @JsonProperty("processFolder")
    private String processFolder;

    @JsonProperty("requestTimeout")
    private Integer requestTimeout;

    @JsonProperty("releaseTimeout")
    private Integer releaseTimeout;

    @JsonProperty("anonymizeInstitutionName")
    private Boolean anonymizeInstitutionName;

    @JsonProperty("anonymizeInstitutionAddress")
    private Boolean anonymizeInstitutionAddress;

    @JsonProperty("anonymizePatientId")
    private Boolean anonymizePatientId;

    @JsonProperty("anonymizePatientName")
    private Boolean anonymizePatientName;

    @JsonProperty("anonymizeReferringPhysician")
    private Boolean anonymizeReferringPhysician;

    @JsonProperty("jsonFromDicom")
    private Boolean jsonFromDicom;

    @JsonProperty("doCompression")
    private Boolean doCompression;

    @JsonProperty("compressionMethod")
    private String compressionMethod;

    @JsonProperty("allowFork")
    private String allowFork;

    @JsonProperty("debugLevel")
    private String debugLevel;

    @JsonProperty("logFolder")
    private String logFolder;

    @JsonProperty("retryLimit")
    private Integer retryLimit;

    @JsonProperty("status")
    private Integer status;

    @JsonIgnore
    private File filePath;

    @JsonProperty("commandWorkingFolder")
    private String commandWorkingFolder;


    @Override
    public void restore(StoreSCPConfigurationProperties obj) {
        setHostName(obj.getHostName());
        setAetName(obj.getAetName());
        setPort(obj.getPort());
        setCommandFolder(obj.getCommandFolder());
        setDestinationFolder(obj.getDestinationFolder());
        setProcessFolder(obj.getProcessFolder());
        setRequestTimeout(obj.getRequestTimeout());
        setReleaseTimeout(obj.getReleaseTimeout());
        setAnonymizeInstitutionName(obj.getAnonymizeInstitutionName());
        setAnonymizeInstitutionAddress(obj.getAnonymizeInstitutionAddress());
        setAnonymizePatientId(obj.getAnonymizePatientId());
        setAnonymizePatientName(obj.getAnonymizePatientName());
        setAnonymizeReferringPhysician(obj.getAnonymizeReferringPhysician());
        setJsonFromDicom(obj.getJsonFromDicom());
        setDoCompression(obj.getDoCompression());
        setCompressionMethod(obj.getCompressionMethod());
        setAllowFork(obj.getAllowFork());
        setDebugLevel(obj.getDebugLevel());
        setLogFolder(obj.getLogFolder());
        setRetryLimit(obj.getRetryLimit());
        setStatus(obj.getStatus());
        setCommandWorkingFolder(obj.getCommandWorkingFolder());
    }
}
