package com.infospica.dicom.process.batch;

import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.state.batch.*;
import com.infospica.dicom.util.EchoServer;
import com.infospica.dicom.util.LoggerUtility;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BatchDicomUploadProcessor {

    private final UploadImageState uploadImageState;
    private final SimpleUploadImageState simpleUploadImageState;
    private final BackupState backupState;
    private final UploadSuspendState uploadSuspendState;
    private final UploadStopState uploadStopState;

    private DicomProcessState state;

    //Thread instances
    private Runnable runnableStart;
    private Runnable runnableStop;
    private Runnable runnableMonitorAndResume;
    private Runnable runnableUploadFiles;

    private long suspendedTime;

    public BatchDicomUploadProcessor(final UploadImageState uploadImageState, final BackupState backupState, final UploadSuspendState uploadSuspendState, final UploadStopState uploadStopState) {
        this.uploadImageState = uploadImageState;
        this.backupState = backupState;
        this.uploadSuspendState = uploadSuspendState;
        this.uploadStopState = uploadStopState;
        this.simpleUploadImageState = null;
        setProcessor();
        setInitialState();
    }

    public BatchDicomUploadProcessor(final SimpleUploadImageState simpleUploadImageState, final BackupState backupState, final UploadSuspendState uploadSuspendState, final UploadStopState uploadStopState) {
        this.simpleUploadImageState = simpleUploadImageState;
        this.backupState = backupState;
        this.uploadSuspendState = uploadSuspendState;
        this.uploadStopState = uploadStopState;
        this.uploadImageState = null;
        setProcessor();
        setInitialState();
    }

    private void setProcessor() {
        if(this.uploadImageState != null) {
            this.uploadImageState.setProcessor(this);
        }
        if(this.simpleUploadImageState != null) {
            this.simpleUploadImageState.setProcessor(this);
        }
        this.backupState.setProcessor(this);
        this.uploadStopState.setProcessor(this);
    }

    private void setInitialState() {
        if(this.uploadImageState != null) {
            this.state = this.uploadImageState;
        }
        if(this.simpleUploadImageState != null) {
            this.state = this.simpleUploadImageState;
        }
    }

    public Runnable uploadFiles() {
        if(runnableUploadFiles == null) {
            runnableUploadFiles = () -> {
                if (this.state != null) {
                    this.state.uploadImages();
                }
                if (this.state != null) {
                    this.state.backupImages();
                }
            };
        }
        return runnableUploadFiles;
    }

    public Runnable monitorAndResume() {
        if(runnableMonitorAndResume == null) {
            runnableMonitorAndResume = () -> {
                if (this.state == this.uploadSuspendState) {
                    if (EchoServer.isRemoteServerListening()) {
                        synchronized (BatchDicomUploadProcessor.class) {
                            this.state = this.uploadImageState;
                        }
                    }
                }
            };
        }
        return runnableMonitorAndResume;
    }

    public Runnable start() {
        if(runnableStart == null) {
            runnableStart = () -> {
                synchronized (BatchDicomUploadProcessor.class) {
                    this.state = this.uploadImageState;
                }
            };
        }
        return runnableStart;
    }

    public Runnable stop() {
        if(runnableStop == null) {
            runnableStop = () -> {
                synchronized (BatchDicomUploadProcessor.this) {
                    this.state = this.uploadStopState;
                }
            };
        }
        return runnableStop;
    }

    public void setState(DicomProcessState state) {
        LoggerUtility.log(BatchDicomUploadProcessor.class, LoggerUtility.LogLevel.INFO, "Is stopped state: " + (this.state == this.uploadStopState));
        if(this.state != this.uploadStopState) {
            this.state = state;
        }
    }

    public void suspendUpload() {
        if(this.state != this.uploadStopState) {
            synchronized (BatchDicomUploadProcessor.this) {
                this.state = this.uploadSuspendState;
                this.suspendedTime = System.currentTimeMillis();
            }
        }
    }
}
