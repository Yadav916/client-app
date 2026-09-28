package com.infospica.dicom.config.iface;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.infospica.dicom.util.ContentEncoderDecoderUtility;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;

public interface Persistable {

    public File getFilePath();

    default void writeConfiguration() throws Exception {
        if(getFilePath() == null) return;
        /*ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(getFilePath(), this);*/

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        String encodedContent = Base64.getEncoder().encodeToString(writer.writeValueAsBytes(this));
        try(FileWriter fw = new FileWriter(getFilePath());) {
            IOUtils.write(ContentEncoderDecoderUtility.getEncryptedContent(encodedContent), fw);
        }
    }
}
