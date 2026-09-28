package com.infospica.dicom.process.schedule;

public interface ScheduleProcessManager {

    void prepare();
    void moveImages();
    void extractJsonFromImages();
    void modifyImages();
    void compressImages();
    void uploadImages();
    void backupImages();
}
