package com.infospica.dicom.process.state.batch;

import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.batch.BatchDicomFileProcessor;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.stereotype.Component;

@Component("_batchProcessStopState")
public class ProcessStopState implements DicomProcessState  {

    private BatchDicomFileProcessor processor;

    @Override
    public String getId() {
        return "Batch_Stop_001";
    }

    public void setProcessor(BatchDicomFileProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(ProcessStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }

    @Override
    public void extractJsonFromImages() {
        LoggerUtility.log(ProcessStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }

    @Override
    public void modifyImages() {
        LoggerUtility.log(ProcessStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }

    @Override
    public void compressImages() {
        LoggerUtility.log(ProcessStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }

    @Override
    public void pushImages() {

    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(ProcessStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }

    @Override
    public void backupImages() {
        LoggerUtility.log(ProcessStopState.class, LoggerUtility.LogLevel.INFO, "Stopped.");
    }
}
