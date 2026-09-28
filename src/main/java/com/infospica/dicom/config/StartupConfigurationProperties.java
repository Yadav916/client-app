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
import java.io.IOException;
import java.io.Serializable;

@Data
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class StartupConfigurationProperties implements Serializable, Persistable, Restorable<StartupConfigurationProperties> {

    private static final long serialVersionUID = 87651L;
    
    @JsonProperty("startupType")
    private String startupType;

    @JsonProperty("retryInterval")
    private Integer retryInterval;

    @JsonProperty("echoInterval")
    private Integer echoInterval;

    @JsonProperty("echoServer")
    private Boolean echoServer;

    @JsonProperty("executionMode")
    private String executeMode; //StatePreference or ThreadPreference

    @JsonProperty("configRequired")
    private Boolean configurationRequired;

    @JsonProperty("salt")
    private String salt;

    @JsonProperty("password")
    private String password;

    @JsonProperty("centreStatusApiUrl")
    private String centreStatusApiUrl;

    @JsonIgnore
    private File filePath;

    @JsonIgnore
    private File systemLogFolder;

    @Override
    public void restore(StartupConfigurationProperties obj) {
        setStartupType(obj.getStartupType());
        setRetryInterval(obj.retryInterval);
        setEchoInterval(obj.getEchoInterval());
        setEchoServer(obj.getEchoServer());
        setCentreStatusApiUrl(obj.getCentreStatusApiUrl());
    }
}
