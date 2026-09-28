package com.infospica.dicom.process.state.batch;

import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.batch.BatchDicomFileProcessor;
import com.infospica.dicom.process.batch.BatchDicomUploadProcessor;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.stereotype.Component;

@Component("_batchUploadStopState")
public class UploadStopState implements DicomProcessState  {

    private BatchDicomUploadProcessor processor;

    @Override
    public String getId() {
        return "Batch_Stop_001";
    }

    public void setProcessor(BatchDicomUploadProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(UploadStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }

    @Override
    public void extractJsonFromImages() {
        LoggerUtility.log(UploadStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }

    @Override
    public void modifyImages() {
        LoggerUtility.log(UploadStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }

    @Override
    public void compressImages() {
        LoggerUtility.log(UploadStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }

    @Override
    public void pushImages() {

    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(UploadStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }

    @Override
    public void backupImages() {
        LoggerUtility.log(UploadStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }
}
