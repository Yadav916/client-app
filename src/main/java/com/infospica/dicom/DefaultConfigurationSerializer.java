package com.infospica.dicom;

import com.infospica.dicom.config.StartupConfigurationProperties;
import com.infospica.dicom.config.StoreSCPConfigurationProperties;
import com.infospica.dicom.config.StoreSCUConfigurationProperties;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.service.ApplicationConfigurationService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;

@Component
@DependsOn({"dicomUploaderConfig"})
public class DefaultConfigurationSerializer {

    @Autowired
    private ApplicationConfigurationService applicationConfigurationService;

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void serializeDefaultConfiguration() {
        try {
            File file = Context.getDefaultConfigurationFile();
            if(!file.exists()) {
                applicationConfigurationService.serializeAndStore(Context.getStartupConfigurationProperties(), file);
                applicationConfigurationService.serializeAndStore(Context.getStoreSCPConfigurationProperties(), file);
                applicationConfigurationService.serializeAndStore(Context.getStoreSCUConfigurationProperties(), file);
            }
            StartupConfigurationProperties startupConfigurationProperties = applicationConfigurationService.deserializeAndReturn(StartupConfigurationProperties.class, file, 0);
            Context.setDefaultStartupConfigurationProperties(startupConfigurationProperties);
            StoreSCPConfigurationProperties storeSCPConfigurationProperties = applicationConfigurationService.deserializeAndReturn(StoreSCPConfigurationProperties.class, file, 1);
            Context.setDefaultStoreSCPConfigurationProperties(storeSCPConfigurationProperties);
            StoreSCUConfigurationProperties storeSCUConfigurationProperties = applicationConfigurationService.deserializeAndReturn(StoreSCUConfigurationProperties.class, file, 2);
            Context.setDefaultStoreSCUConfigurationProperties(storeSCUConfigurationProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
