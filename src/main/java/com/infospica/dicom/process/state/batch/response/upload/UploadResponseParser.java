package com.infospica.dicom.process.state.batch.response.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadResponseParser {
    private final List<File> fileList = new ArrayList<>(10);;
    private final StringBuilder response = new StringBuilder();

    public UploadResponseParser() {

    }

    public String getResponse() {
        return response.toString();
    }

    public void setResponse(String response) {
        this.response.append(response);
    }

    public List<File> getFileList() {
        return fileList;
    }


    public void addToList(String file) {
        this.fileList.add(new File(file));
    }

    public void reset() {
        this.response.delete(0, this.response.length());
        this.fileList.clear();
    }
}
