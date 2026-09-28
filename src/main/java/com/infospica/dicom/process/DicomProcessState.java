package com.infospica.dicom.process;

import java.io.Serializable;

public interface DicomProcessState extends Serializable {
    String getId();
    void moveImages();
    void extractJsonFromImages();
    void modifyImages();
    void compressImages();
    void pushImages();
    void uploadImages();
    void backupImages();
}
