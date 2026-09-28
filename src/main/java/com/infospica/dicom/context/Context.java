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

import com.infospica.dicom.config.StartupConfigurationProperties;
import com.infospica.dicom.config.StoreSCPConfigurationProperties;
import com.infospica.dicom.config.StoreSCUConfigurationProperties;
import com.infospica.dicom.process.analytics.AnalyticalContext;
import com.infospica.dicom.util.EchoServer;
import com.infospica.dicom.util.FileUtility;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *   @author arun.vs
 */
public class Context {

    private static ConfigurationContext configurationContext;
    private static FolderContext folderContext;
    private static AnalyticalContext analyticalContext;
    private static ExecutionContext executionContext;

    private static boolean batchUploadMetadataFileCreated = false;

    public static void setFolderContext(FolderContext fctx) {
        Context.folderContext = fctx;
    }
    public static void setAnalyticalContext(AnalyticalContext actx) {
        Context.analyticalContext = actx;
    }
    public static void setExecutionContext(ExecutionContext ectx) {
        Context.executionContext = ectx;
    }
    public static ConfigurationContext getConfigurationContext() {
        if(configurationContext == null) {
            synchronized (Context.class) {
                configurationContext = ConfigurationContext.getInstance();
            }
        }
        return ConfigurationContext.getInstance();
    }

    public static StartupConfigurationProperties getStartupConfigurationProperties() {
        return getConfigurationContext().getStartupConfigurationProperties();
    }

    public static void setStartupConfigurationProperties(StartupConfigurationProperties startupConfigurationProperties) {
        getConfigurationContext().setStartupConfigurationProperties(startupConfigurationProperties);
    }

    public static StoreSCPConfigurationProperties getStoreSCPConfigurationProperties() {
        return getConfigurationContext().getScpConfigurationProperties();
    }

    public static void setStoreSCPConfigurationProperties(StoreSCPConfigurationProperties storeSCPConfigurationProperties) {
        getConfigurationContext().setScpConfigurationProperties(storeSCPConfigurationProperties);
    }

    public static StoreSCUConfigurationProperties getStoreSCUConfigurationProperties() {
        return getConfigurationContext().getScuConfigurationProperties();
    }

    public static void setStoreSCUConfigurationProperties(StoreSCUConfigurationProperties storeSCUConfigurationProperties) {
        getConfigurationContext().setScuConfigurationProperties(storeSCUConfigurationProperties);
    }

    //default configuration
    public static StartupConfigurationProperties getDefaultStartupConfigurationProperties() {
        return getConfigurationContext().getDefaultStartupConfigurationProperties();
    }

    public static void setDefaultStartupConfigurationProperties(StartupConfigurationProperties startupConfigurationProperties) {
        getConfigurationContext().setDefaultStartupConfigurationProperties(startupConfigurationProperties);
    }

    public static StoreSCPConfigurationProperties getDefaultStoreSCPConfigurationProperties() {
        return getConfigurationContext().getDefaultScpConfigurationProperties();
    }

    public static void setDefaultStoreSCPConfigurationProperties(StoreSCPConfigurationProperties storeSCPConfigurationProperties) {
        getConfigurationContext().setDefaultScpConfigurationProperties(storeSCPConfigurationProperties);
    }

    public static StoreSCUConfigurationProperties getDefaultStoreSCUConfigurationProperties() {
        return getConfigurationContext().getDefaultScuConfigurationProperties();
    }

    public static void setDefaultStoreSCUConfigurationProperties(StoreSCUConfigurationProperties storeSCUConfigurationProperties) {
        getConfigurationContext().setDefaultScuConfigurationProperties(storeSCUConfigurationProperties);
    }

    public static File getCommandFolder() {
        return folderContext.getCommandFolder();
    }

    public static File getMachineDestinationFolder() {
        return folderContext.getMachineDestinationFolder();
    }

    public static File getProcessSourceFolder() {
        return folderContext.getProcessSourceFolder();
    }

    public static File getExtractJsonFolder() {
        return folderContext.getExtractJsonFolder();
    }

    public static File getAnonymizedSourceFolder() {
        return folderContext.getAnonymizeSourceFolder();
    }

    public static File getAnonymizedDestinationFolder() {
        return folderContext.getAnonymizeDestinationFolder();
    }

    public static File getCompressSourceFolder() {
        return folderContext.getCompressSourceFolder();
    }

    public static File getCompressDestinationFolder() {
        return folderContext.getCompressDestinationFolder();
    }

    public static File getUploadSourceFolder() {
        return folderContext.getUploadSourceFolder();
    }

    public static File getUploadProcessFolder() {
        return folderContext.getUploadProcessFolder();
    }

    /*public static File getScuProcessMetadataFolder() {
        return folderContext.getScuProcessMetadataFolder();
    }*/

    public static File getUploadBackupFolder() {
        return folderContext.getUploadBackupFolder();
    }

    public static File getLogFolder() {
        return folderContext.getLogFolder();
    }
    public static File getSystemLogFolder() {
        return folderContext.getSystemLogFolder();
    }

    public static File getCommandWorkingFolder() {
        return folderContext.getCommandWorkingFolder();
    }
    public static void createMandatoryFolderIfMissing(File folder) {
        synchronized (Context.class) {
            FileUtility.makeDirectories(folder);
        }
    }

    public static void setCurrentUploadBackupFolder() {
        folderContext.setCurrentUploadBackupFolder();
    }
    public static File getCurrentUploadBackupFolder() {
        return folderContext.getCurrentUploadBackupFolder();
    }
    public static File getDefaultConfigurationFile() {
        return folderContext.getDefaultConfigurationFile();
    }

    //process/state control flags
    public static boolean isJsonExtractionEnabled() {
        return getConfigurationContext().isJsonExtractionEnabled();
    }

    public static boolean isAnonymizeEnabled() {
        return getConfigurationContext().isAnonymizeEnabled();
    }
    public static boolean isAnonymizePatientId() {
        return getConfigurationContext().isAnonymizePatientId();
    }
    public static boolean isAnonymizePatientName() {
        return getConfigurationContext().isAnonymizePatientName();
    }
    public static boolean isAnonymizeInstitutionName() {
        return getConfigurationContext().isAnonymizeInstitutionName();
    }
    public static boolean isAnonymizeInstitutionAddress() {
        return getConfigurationContext().isAnonymizeInstitutionAddress();
    }
    public static boolean isAnonymizeReferringPhysician() {
        return getConfigurationContext().isAnonymizeReferringPhysician();
    }

    public static boolean isCompressionEnabled() {
        return getConfigurationContext().isCompressionEnabled();
    }
    public static String getCompressionMethod() {
        return getConfigurationContext().getCompressionMethod();
    }

    public static boolean isPingEnabled() {
        return getConfigurationContext().isPingEnabled();
    }
    public static int getPingInterval() {
        return getConfigurationContext().getPingInterval();
    }

    //analytics
    //pending images count
    public static void setPendingImage(int count) {
        analyticalContext.setPendingCount(count);
    }
    public static Integer getPendingImage() {
        return analyticalContext.getPendingCount();
    }
    public static Integer updatePendingImage(int count) {
        return analyticalContext.updatePending(count);
    }

    //json extract
    public static void setJsonExtractImage(int count) {
        analyticalContext.setJsonExtractCount(count);
    }
    public static Integer getJsonExtractImage() {
        return analyticalContext.getJsonExtractCount();
    }
    public static void setJsonProcessedCount(Integer count) {
        analyticalContext.setJsonProcessedCount(count);
    }
    public static Integer updateJsonExtractImage() {
        return analyticalContext.updateJsonExtracting();
    }
    public static Integer getJsonProcessedCount() {
        return analyticalContext.getJsonProcessedCount();
    }

    //modified count
    public static void setModifiedImage(int count) {
        analyticalContext.setModifiedCount(count);
    }
    public static Integer getModifiedImage() {
        return analyticalContext.getModifiedCount();
    }
    public static void setModifiedProcessedCount(Integer count) {
        analyticalContext.setModifiedProcessedCount(count);
    }
    public static Integer updateModifiedImage() {
        return analyticalContext.updateModification();
    }
    public static Integer getModifyProcessedCount() {
        return analyticalContext.getModifyProcessedCount();
    }


    //compressed count
    public static void setCompressedImage(int count) {
        analyticalContext.setCompressedCount(count);
    }
    public static Integer getCompressedImage() {
        return analyticalContext.getCompressedCount();
    }
    public static void setCompressProcessedCount(Integer count) {
        analyticalContext.setCompressProcessedCount(count);
    }
    public static Integer updateCompressedImage() {
        return analyticalContext.updateCompression();
    }
    public static Integer getCompressProcessedCount() {
        return analyticalContext.getCompressProcessedCount();
    }

    //pending images to upload
    public static void setUploadPendingCount(int count) {
        analyticalContext.setUploadPendingCount(count);
    }
    public static Integer getUploadPendingCount() {
        return analyticalContext.getUploadPendingCount();
    }
    public static Integer updateUploadPendingCount() {
        return analyticalContext.updateUploadPendingCount();
    }
    public static Integer updateUploadPendingCount(int count) {
        return analyticalContext.updateUploadPendingCount(count);
    }

    //processed count
    public static void setProcessedImage(int count) {
        analyticalContext.setProcessedCount(count);
    }
    public static Integer getProcessedImage() {
        return analyticalContext.getProcessedCount();
    }
    public static Integer updateProcessedImage() {
        return analyticalContext.updateProcessed();
    }
    public static Integer updateProcessedImage(int processed) {
        return analyticalContext.updateProcessed(processed);
    }

    //processed error count
    public static void setProcessedErrorImage(int count) {
        analyticalContext.setProcessedErrorCount(count);
    }
    public static Integer getProcessedErrorImage() {
        return analyticalContext.getProcessedErrorCount();
    }
    public static Integer updateProcessedErrorImage() {
        return analyticalContext.updateProcessedError();
    }
    public static Integer updateProcessedErrorImage(int processed) {
        return analyticalContext.updateProcessedError(processed);
    }

    //good count
    public static void setGoodImage(int count) {
        analyticalContext.setGoodCount(count);
    }
    public static Integer getGoodImage() {
        return analyticalContext.getGoodCount();
    }
    public static Integer updateGoodImage() {
        return analyticalContext.updateGood();
    }
    public static Integer updateGoodImage(int count) {
        return analyticalContext.updateGood(count);
    }

    //bad count
    public static void setBadImage(int count) {
        analyticalContext.setBadCount(count);
    }
    public static Integer getBadImage() {
        return analyticalContext.getBadCount();
    }
    public static Integer updateBadImage() {
        return analyticalContext.updateBad();
    }
    public static Integer updateBadImage(int count) {
        return analyticalContext.updateBad(count);
    }

    public static void updateSCPEchoStatus() {
        analyticalContext.updateSCPEchoStatus(EchoServer.isServerListening());
    }
    public static void updateSCUEchoStatus() {
        analyticalContext.updateSCUEchoStatus(EchoServer.isRemoteServerListening());
    }
    public static void updateSCPEchoStatus(boolean flag) {
        analyticalContext.updateSCPEchoStatus(flag);
    }
    public static void updateSCPEchoResponse(String response) {
        analyticalContext.updateSCPEchoResponse(response);
    }
    public static void updateSCUEchoStatus(boolean flag) {
        analyticalContext.updateSCUEchoStatus(flag);
    }

    public static boolean getSCPEchoStatus() {
        return analyticalContext.getSCPEchoStatus();
    }
    public static boolean hasSCPEchoResponse() {
        return analyticalContext.getSCPEchoResponse() != null;
    }
    public static boolean getSCUEchoStatus() {
        return analyticalContext.getSCUEchoStatus();
    }

    //Upload service related flags
    public static boolean isManualStart() {
        return configurationContext.isManualStart();
    }
    public static boolean isStatePreference() { //The execution preference
        return configurationContext.isStatePreference();
    }
    public static boolean isThreadPreference() {
        return configurationContext.isThreadPreference();
    }
    public static boolean isBatchPreference() {
        return configurationContext.isBatchPreference();
    }

    //execution context flags
    public static void setUploadServiceStarted(boolean flag) {
        synchronized (Context.class) {
            executionContext.setUploadServiceStarted(flag);
        }
    }
    public static boolean isUploadServiceStarted() {
        return executionContext.isUploadServiceStarted();
    }
    public static void setTerminate(boolean flag) {
        synchronized (Context.class) {
            executionContext.setTerminate(flag);
        }
    }
    public static boolean isConfigurationRequired() {
        return configurationContext.isConfigurationRequired();
    }
    public static boolean isTerminated() {
        return executionContext.isTerminate();
    }

    public static void setLastProcessDone(Date date) {
        ExecutionContext.ProcessControl processControl = executionContext.getProcessControl();
        if(date != null) {
            if(processControl.getLastProcessDone() == null) {
                processControl.setLastProcessDone(new Date());
            }
            long lastTime = processControl.getLastProcessDone().getTime();
            long currentTime = new Date().getTime();
            processControl.setProcessIdle(TimeUnit.MILLISECONDS.toMinutes(currentTime - lastTime));
        } else {
            processControl.setLastProcessDone(null);
            processControl.setProcessIdle(0L);
        }
    }
    public static long getProcessIdleTime() {
        return executionContext.getProcessControl().getProcessIdle();
    }

    //Folder related
    public static boolean canStartUploadService() {
        return folderContext.isValid() && configurationContext.isValidCommandFolder();
    }

    public static boolean canStartSCPService() {
        return folderContext.isValid();
    }

    public static void refreshFolderContext() {
        try {
            folderContext.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean shallCleanBackup() {
        return configurationContext.shallCleanBackup() &&
                (folderContext.getUploadBackupFolder() != null && folderContext.getUploadBackupFolder().exists());
    }
    public static boolean shallCleanImmediately() {
        return Context.skipLogCreation() &&
                (folderContext.getUploadBackupFolder() != null && folderContext.getUploadBackupFolder().exists());
    }
    public static boolean skipLogCreation() {
        return configurationContext.shallCleanImmediately();
    }

    public static void setBackupCleanProcessScheduled(boolean flag) {
        synchronized (Context.class) {
            executionContext.setBackupCleanProcessScheduled(flag);
        }
    }
    public static boolean isBackupCleanProcessScheduled() {
        return executionContext.isBackupCleanProcessScheduled();
    }

    public static void setBackupCleanStarted(boolean flag) {
        synchronized (Context.class) {
            executionContext.setBackupCleanStarted(flag);
        }
    }
    public static boolean isBackupCleanStarted() {
        return executionContext.isBackupCleanStarted();
    }

    public static void setBatchUploadMetadataFileCreated(boolean created) {
        synchronized (Context.class) {
            Context.batchUploadMetadataFileCreated = created;
        }
    }

    public static boolean isBatchUploadMetadataFileCreated() {
        return Context.batchUploadMetadataFileCreated;
    }
}
