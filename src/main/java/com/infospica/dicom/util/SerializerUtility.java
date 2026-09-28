package com.infospica.dicom.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Base64;

public class SerializerUtility {

    private SerializerUtility() {
        super();
    }

    public static <T extends Serializable> void serialize(T object, File path) throws IOException {
        byte[] byteValue = SerializationUtils.serialize(object);
        String encodedValue = Base64.getEncoder().encodeToString(byteValue);
        try(FileWriter fw = new FileWriter(path)) {
            IOUtils.write(encodedValue, fw);
        }
    }

    public static <T extends Serializable> T deserialize(File path) throws IOException {
        if(path != null && path.exists()) {
            String contents = FileUtils.readFileToString(path, Charset.forName("UTF-8"));
            byte[] byteValue = Base64.getDecoder().decode(contents);
            T sobj1 = SerializationUtils.deserialize(byteValue);
            return sobj1;
        }
        return null;
    }
}
