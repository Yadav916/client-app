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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.infospica.dicom.context;

import ch.qos.logback.classic.LoggerContext;
import com.infospica.dicom.Constants;
import com.infospica.dicom.config.StartupConfigurationProperties;
import com.infospica.dicom.config.StoreSCPConfigurationProperties;
import com.infospica.dicom.config.StoreSCUConfigurationProperties;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 *   @author arun.vs
 */
@Data
@Getter
@Setter
public class FolderContext {

    private File machineDestinationFolder;
    private File processSourceFolder;

    private File extractJsonFolder;

    private File anonymizeSourceFolder;
    private File anonymizeDestinationFolder;

    private File compressSourceFolder;
    private File compressDestinationFolder;

    private File uploadSourceFolder;
    private File uploadProcessFolder;
    private File uploadBackupFolder;
    //private File scuProcessMetadataFolder;

    private File currentUploadBackupFolder = null;

    private File logFolder = null;

    private File commandFolder = null;
    private File commandWorkingFolder = null;

    private final File defaultConfigurationFile;

    public FolderContext() {
        try {
            this.defaultConfigurationFile = new File(".", "._default");
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void init() throws IOException {
        if(Context.getStoreSCPConfigurationProperties().getCommandFolder().isEmpty()) {
            LoggerUtility.log(FolderContext.class, LoggerUtility.LogLevel.ERROR, "Unable to proceed, command folder not configured.");
            return;
        }
        if(!new File(Context.getStoreSCPConfigurationProperties().getCommandFolder(), "storescp").exists()) {
            LoggerUtility.log(FolderContext.class, LoggerUtility.LogLevel.ERROR, "Unable to proceed, command folder not valid.");
            return;
        }

        if(Context.getStoreSCPConfigurationProperties().getDestinationFolder().isEmpty()) {
            LoggerUtility.log(FolderContext.class, LoggerUtility.LogLevel.ERROR, "Unable to proceed, destination folder not configured.");
            return;
        }
        if(Context.getStoreSCPConfigurationProperties().getProcessFolder().isEmpty()) {
            LoggerUtility.log(FolderContext.class, LoggerUtility.LogLevel.ERROR, "Unable to proceed, process folder not configured.");
            return;
        }
        if(Context.getStoreSCUConfigurationProperties().getUploadBackup().isEmpty()) {
            LoggerUtility.log(FolderContext.class, LoggerUtility.LogLevel.ERROR, "Unable to proceed, upload backup not configured.");
            return;
        }
        this.commandFolder = new File(Context.getStoreSCPConfigurationProperties().getCommandFolder()).getCanonicalFile();
        this.machineDestinationFolder = new File(Context.getStoreSCPConfigurationProperties().getDestinationFolder()).getCanonicalFile();
        FileUtility.makeDirectories(this.machineDestinationFolder);

        this.processSourceFolder = new File(Context.getStoreSCPConfigurationProperties().getProcessFolder()).getCanonicalFile();
        FileUtility.makeDirectories(this.processSourceFolder);

        //considering other working folders outside process folder; so taking the parent process folder
        File processParent = this.processSourceFolder.getParentFile();

        setExtractJsonFolders(processParent);
        setAnonymizedFolders(processParent);
        setCompressFolders(processParent);
        setUploadFolders();
        setLogFolder();
        setCommandWorkingFolder();

        FileUtility.makeDirectories(this.uploadBackupFolder);
    }
    public boolean isValid() {
        return (this.machineDestinationFolder != null) && (this.processSourceFolder != null) && (this.uploadBackupFolder != null)
                && (this.commandFolder != null);
    }

    private void setExtractJsonFolders(File processParent) {
        //setting json save folder
        this.extractJsonFolder = new File(processParent, "dicomjson");
        FileUtility.makeDirectories(this.extractJsonFolder);
    }

    private void setAnonymizedFolders(File processParent) {
        //setting anonymize folder
        if(Context.isAnonymizeEnabled()) {
            this.anonymizeSourceFolder = new File(this.processSourceFolder.getAbsolutePath());
            this.anonymizeDestinationFolder = new File(processParent, "anonymized");
            FileUtility.makeDirectories(this.anonymizeDestinationFolder);
        }
    }
    private void setCompressFolders(File processParent) {
        //setting compress folder
        if(Context.isAnonymizeEnabled()) {
            this.compressSourceFolder = new File(this.anonymizeDestinationFolder.getAbsolutePath());
        } else {
            this.compressSourceFolder = new File(this.processSourceFolder.getAbsolutePath());
        }
        this.compressDestinationFolder = new File(processParent, "compressed");
        FileUtility.makeDirectories(this.compressDestinationFolder);
    }

    private void setUploadFolders() throws IOException {
        if(Context.isCompressionEnabled()) {
            this.uploadSourceFolder = new File(this.compressDestinationFolder.getAbsolutePath());
        } else if(Context.isAnonymizeEnabled()) {
            this.uploadSourceFolder = new File(this.anonymizeDestinationFolder.getAbsolutePath());
        } else {
            this.uploadSourceFolder = new File(this.processSourceFolder.getAbsolutePath());
        }
        this.uploadProcessFolder = new File(this.processSourceFolder.getParentFile(), "todicom");
        FileUtility.makeDirectories(this.uploadProcessFolder);

        //this.scuProcessMetadataFolder = new File(this.processSourceFolder.getParentFile(), "scutmp");
        //FileUtility.makeDirectories(this.scuProcessMetadataFolder);

        this.uploadBackupFolder = new File(Context.getStoreSCUConfigurationProperties().getUploadBackup()).getCanonicalFile();
    }

    public void setCurrentUploadBackupFolder() {
        File backupFolder = new File(this.uploadBackupFolder, Constants._FOLDER_FORMAT.format(new Date()));
        FileUtility.makeDirectories(backupFolder);
        this.currentUploadBackupFolder = backupFolder;
    }

    public File getCurrentUploadBackupFolder() {
        if(this.currentUploadBackupFolder == null) setCurrentUploadBackupFolder();
        return this.currentUploadBackupFolder;
    }

    public void setLogFolder() {
        String lfolder = Context.getStoreSCPConfigurationProperties().getLogFolder();
        if(lfolder == null || lfolder.trim().isEmpty()) {
            lfolder = System.getProperty("java.io.tmpdir");
        }
        this.logFolder = new File(lfolder);
        if(!this.logFolder.exists()) {
            this.logFolder.mkdirs();
        }
    }

    public void setCommandWorkingFolder() throws IOException {
        String workingFolderPath = Context.getStoreSCPConfigurationProperties().getCommandWorkingFolder();
        if(workingFolderPath == null || workingFolderPath.trim().isEmpty()) {
            // Use command folder as working folder if not specified
            workingFolderPath = Context.getStoreSCPConfigurationProperties().getCommandFolder();
            if(workingFolderPath == null || workingFolderPath.trim().isEmpty()) {
                LoggerUtility.log(FolderContext.class, LoggerUtility.LogLevel.ERROR, "Unable to proceed, command working folder not configured.");
                return;
            }
        }
        this.commandWorkingFolder = new File(workingFolderPath).getCanonicalFile();
        FileUtility.makeDirectories(this.commandWorkingFolder);
    }

    public File getLogFolder() {
        return this.logFolder;
    }
    public File getSystemLogFolder() {
        return Context.getStartupConfigurationProperties().getSystemLogFolder();
    }
}
