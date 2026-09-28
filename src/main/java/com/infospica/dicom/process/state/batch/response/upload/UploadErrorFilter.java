package com.infospica.dicom.process.state.batch.response.upload;

import com.infospica.dicom.process.state.batch.response.ResponseFilter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UploadErrorFilter implements ResponseFilter {

    private UploadResponseParser uploadResponseParser;
    private ResponseFilter filter;

    private Pattern pattern = Pattern.compile("E*\"ERROR: Received C-STORE-RSP with Status ([a-fA-F0-9]+)H? for (.*)\"");
    private Matcher matcher = pattern.matcher("");

    public UploadErrorFilter(UploadResponseParser uploadResponseParser) {
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
            uploadResponseParser.addToList(matcher.group(2));
        }
        if(filter != null) {
            filter.applyFilter();;
        }
    }
}
