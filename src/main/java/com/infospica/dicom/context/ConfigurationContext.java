/**
 * Licensed to the Cirakas Consulting Pvt Ltd under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.cirakas.com/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.infospica.dicom.context;

import com.infospica.dicom.config.StartupConfigurationProperties;
import com.infospica.dicom.config.StoreSCPConfigurationProperties;
import com.infospica.dicom.config.StoreSCUConfigurationProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author arun.vs
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationContext {

    @Getter
    @Setter
    private StartupConfigurationProperties startupConfigurationProperties;

    @Getter
    @Setter
    private StoreSCPConfigurationProperties scpConfigurationProperties;

    @Getter
    @Setter
    private StoreSCUConfigurationProperties scuConfigurationProperties;

    @Getter
    @Setter
    private StartupConfigurationProperties defaultStartupConfigurationProperties;

    @Getter
    @Setter
    private StoreSCPConfigurationProperties defaultScpConfigurationProperties;

    @Getter
    @Setter
    private StoreSCUConfigurationProperties defaultScuConfigurationProperties;

    private static class ConfigurationContextHolder {

        private static ConfigurationContext instance = null;
    }

    public static ConfigurationContext getInstance() {
        if (ConfigurationContextHolder.instance == null) {
            ConfigurationContextHolder.instance = new ConfigurationContext();
        }
        return ConfigurationContextHolder.instance;
    }

    public String getCommandFolder() {
        return scpConfigurationProperties.getCommandFolder();
    }

    public String getCommandWorkingFolder() {
        return scpConfigurationProperties.getCommandWorkingFolder();
    }

    public boolean isValidCommandFolder() {
        return !scpConfigurationProperties.getCommandFolder().isEmpty();
    }

    public String getSCPRequestTimeout() {
        return String.valueOf(scpConfigurationProperties.getRequestTimeout());
    }

    public String getSCPReleaseTimeout() {
        return String.valueOf(scpConfigurationProperties.getReleaseTimeout());
    }

    public Boolean isJsonExtractionEnabled() {
        return scpConfigurationProperties.getJsonFromDicom();
    }

    public Boolean isConfigurationRequired() {
        return startupConfigurationProperties.getConfigurationRequired();
    }

    public Boolean isAnonymizeEnabled() {
        return scpConfigurationProperties.getAnonymizeInstitutionName() || scpConfigurationProperties.getAnonymizeInstitutionAddress()
                || scpConfigurationProperties.getAnonymizePatientId() || scpConfigurationProperties.getAnonymizePatientName()
                || scpConfigurationProperties.getAnonymizeReferringPhysician();
    }

    public Boolean isAnonymizePatientId() {
        return scpConfigurationProperties.getAnonymizePatientId();
    }

    public Boolean isAnonymizePatientName() {
        return scpConfigurationProperties.getAnonymizePatientName();
    }

    public Boolean isAnonymizeInstitutionName() {
        return scpConfigurationProperties.getAnonymizeInstitutionName();
    }

    public Boolean isAnonymizeInstitutionAddress() {
        return scpConfigurationProperties.getAnonymizeInstitutionAddress();
    }

    public Boolean isAnonymizeReferringPhysician() {
        return scpConfigurationProperties.getAnonymizeReferringPhysician();
    }

    public Boolean isCompressionEnabled() {
        return scpConfigurationProperties.getDoCompression();
    }

    public String getCompressionMethod() {
        if (scpConfigurationProperties.getCompressionMethod().equalsIgnoreCase("JPEG 2000 Lossless")) {
            return "--j2kr";
        } else if (scpConfigurationProperties.getCompressionMethod().equalsIgnoreCase("JPEG LS Lossless")) {
            return "--jlsl";
        } else if (scpConfigurationProperties.getCompressionMethod().equalsIgnoreCase("JPEG 2000 Lossy")) {
            return "--j2ki";
        }
        return null;
    }

    //ping servers
    public Boolean isPingEnabled() {
        return startupConfigurationProperties.getEchoServer();
    }

    public Integer getPingInterval() {
        Integer interval = startupConfigurationProperties.getEchoInterval();
        return interval == null || interval == 0 ? 1 : interval;
    }

    public Boolean isManualStart() {
        return startupConfigurationProperties.getStartupType().equalsIgnoreCase("MANUAL");
    }

    public boolean isStatePreference() {
        return startupConfigurationProperties.getExecuteMode().equalsIgnoreCase("StatePreference");
    }

    public boolean isThreadPreference() {
        return startupConfigurationProperties.getExecuteMode().equalsIgnoreCase("ThreadPreference");
    }

    public boolean isBatchPreference() {
        return startupConfigurationProperties.getExecuteMode().equalsIgnoreCase("BatchPreference");
    }

    public boolean shallCleanBackup() {
        return scuConfigurationProperties.getKeepImagesFor().intValue() > 0;
    }

    public boolean shallCleanImmediately() {
        return scuConfigurationProperties.getKeepImagesFor().intValue() <= 0;
    }
}
