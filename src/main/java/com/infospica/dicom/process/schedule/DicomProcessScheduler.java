package com.infospica.dicom.process.schedule;

public class DicomProcessScheduler {

    private final ScheduleProcessManager processManager;

    public DicomProcessScheduler(final ScheduleProcessManager processManager) {
        this.processManager = processManager;
    }

    public Runnable initScheduler() {
        return this.processManager::prepare;
    }

    public Runnable moveImages() {
        return this.processManager::moveImages;
    }

    public Runnable extractJsonFromImages() {
        return this.processManager::extractJsonFromImages;
    }

    public Runnable modifyImages() {
        return this.processManager::modifyImages;
    }

    public Runnable compressImages() {
        return this.processManager::compressImages;
    }

    public Runnable uploadImages() {
        return this.processManager::uploadImages;
    }

    public Runnable backupImages() {
        return this.processManager::backupImages;
    }
}
