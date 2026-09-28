package com.infospica.dicom.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;

@Service
public class ApplicationConfigurationService {

    public <T extends Serializable> void serializeAndStore(T object, File file) throws IOException {
        byte[] input = SerializationUtils.serialize(object);
        String encodedInput1 = Base64.getEncoder().encodeToString(input);
        FileUtils.writeStringToFile(file, encodedInput1 + "\r\n", "UTF-8", true);
    }

    public <T> T deserializeAndReturn(Class<T> clazz, File file, int index) throws IOException {
        List<String> contents = FileUtils.readLines(file, "UTF-8");
        if(contents.size() > index) {
            byte[] decodedOutput = Base64.getDecoder().decode(contents.get(index));
            Object sobj = SerializationUtils.deserialize(decodedOutput);
            return clazz.cast(sobj);
        }
        return null;
    }
}
