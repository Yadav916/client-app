package com.infospica.dicom.process;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.service.SCUService;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;
import com.infospica.dicom.util.SerializerUtility;
import com.infospica.dicom.util.cleaner.BackupCleaner;
import com.infospica.dicom.util.cleaner.FileNameWithTimeFilter;
import com.infospica.dicom.process.state.*;
import com.infospica.dicom.service.SCPService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Data
@Getter
@Setter
public class DicomUploadProcessor {

    private final SCPService scpService;
    private final SCUService scuService;
    private final MoveImageState moveImageState;
    private final ExtractJSONState extractJson;
    private final ModifyImageState modifyImageState;
    private final CompressImageState compressImageState;
    private final UploadImageState uploadImageState;
    private final BackupState backupState;

    private DicomProcessState state;

    private Runnable runnablePingRemoteServer;
    private Runnable runnableStartServer;
    private Runnable runnableStopServer;
    private Runnable runnablePingSCPServer;
    private Runnable runnableCleanBackup;
    private Runnable runnableCleanAllBackup;

    public DicomUploadProcessor(final SCPService scpService, final SCUService scuService, final MoveImageState moveImageState,
                                final ExtractJSONState extractJson, final ModifyImageState modifyImageState,
                                final CompressImageState compressImageState, final UploadImageState uploadImageState,
                                final BackupState backupState) {
        this.scpService = scpService;
        this.scuService = scuService;
        this.moveImageState =  moveImageState;
        this.extractJson = extractJson;
        this.modifyImageState = modifyImageState;
        this.compressImageState = compressImageState;
        this.uploadImageState = uploadImageState;
        this.backupState = backupState;

        this.scpService.setProcessor(this);
        this.moveImageState.setProcessor(this);
        this.extractJson.setProcessor(this);
        this.modifyImageState.setProcessor(this);
        this.compressImageState.setProcessor(this);
        this.uploadImageState.setProcessor(this);
        this.backupState.setProcessor(this);

        this.state = this.moveImageState;

    }

    public Runnable startServer() {
        if(runnableStartServer == null) {
            runnableStartServer = () -> {
                Context.updateSCPEchoResponse(null);
                if (this.scpService != null) {
                    this.scpService.start();
                }
            };
        }
        return runnableStartServer;
    }

    public Runnable stopServer() {
        if(runnableStopServer == null) {
            runnableStopServer = () -> {
                if (this.scpService != null) {
                    this.scpService.stop();
                }
            };
        }
        return runnableStopServer;
    }

    public Runnable pingRemoteServer() {
        if(runnablePingRemoteServer == null) {
            runnablePingRemoteServer = () -> {
                if (this.scuService != null) {
                    Context.updateSCUEchoStatus(this.scuService.isServerReachable());
                }
            };
        }
        return runnablePingRemoteServer;
    }

    public Runnable pingSCPServer() {
        if(runnablePingSCPServer == null) {
            runnablePingSCPServer = () -> {
                if (this.scpService != null) {
                    Context.updateSCPEchoStatus(this.scpService.isRunning());
                }
            };
        }
        return runnablePingSCPServer;
    }

    public Runnable uploadFiles() {
        return () -> {
            if(this.state != null) {
                this.state.moveImages();
            }
            if(this.state != null) {
                this.state.extractJsonFromImages();
            }
            if(this.state != null) {
                this.state.modifyImages();
            }
            if(this.state != null) {
                this.state.compressImages();
            }
            if(this.state != null) {
                this.state.uploadImages();
            }
            if(this.state != null) {
                this.state.backupImages();
            }
        };
    }

    public Runnable cleanBackup() {
        if(runnableCleanBackup == null) {
            runnableCleanBackup = () -> {
                int keepDays = Context.getStoreSCUConfigurationProperties().getKeepImagesFor();
                new BackupCleaner().clean(keepDays);
            };
        }
        return runnableCleanBackup;
    }

    public Runnable cleanAllBackup() {
        if(runnableCleanAllBackup == null) {
            runnableCleanAllBackup = () -> {
                new BackupCleaner().clean(0);
            };
        }
        return runnableCleanAllBackup;
    }

    public void stop() {
        this.state = null;
    }



}
