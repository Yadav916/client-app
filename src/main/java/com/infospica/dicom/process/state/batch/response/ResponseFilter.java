package com.infospica.dicom.process.state.batch.response;

public interface ResponseFilter {
    void setNextFilter(ResponseFilter filter);
    void applyFilter();
}
