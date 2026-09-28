package com.infospica.dicom.process.state.batch.response.upload;

import com.infospica.dicom.process.state.batch.response.ResponseFilter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileScanErrorFilter implements ResponseFilter {

    private UploadResponseParser uploadResponseParser;
    private ResponseFilter filter;

    private Pattern pattern = Pattern.compile("Failed to scan file (.*)?:");
    private Matcher matcher = pattern.matcher("");

    public FileScanErrorFilter(UploadResponseParser uploadResponseParser) {
        this.uploadResponseParser = uploadResponseParser;
    }

    @Override
    public void setNextFilter(ResponseFilter filter) {
        this.filter = filter;
    }

    @Override
    public void applyFilter() {
        matcher.reset(uploadResponseParser.getResponse());
        while(matcher.find()) {
            String file = matcher.group(1).trim();
            uploadResponseParser.addToList(file);
        }
        if(filter != null) {
            filter.applyFilter();;
        }
    }
}
