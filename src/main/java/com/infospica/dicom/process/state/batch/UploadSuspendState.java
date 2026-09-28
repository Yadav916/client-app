package com.infospica.dicom.process.state.batch;

import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.batch.BatchDicomUploadProcessor;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.stereotype.Component;

@Component("_batchUploadSuspendState")
public class UploadSuspendState implements DicomProcessState  {

    private BatchDicomUploadProcessor processor;

    @Override
    public String getId() {
        return "Batch_Upload_Suspend_001";
    }

    public void setProcessor(BatchDicomUploadProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {

    }

    @Override
    public void extractJsonFromImages() {

    }

    @Override
    public void modifyImages() {

    }

    @Override
    public void compressImages() {

    }

    @Override
    public void pushImages() {

    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(UploadSuspendState.class, LoggerUtility.LogLevel.INFO, "Server not responding. Suspended.");
    }

    @Override
    public void backupImages() {
        LoggerUtility.log(UploadSuspendState.class, LoggerUtility.LogLevel.INFO, "Server not responding. Suspended.");
    }
}
