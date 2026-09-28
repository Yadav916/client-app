package com.infospica.dicom.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.LoggerUtility;
import lombok.Data;

import javax.swing.JOptionPane;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CentreStatusService {

    private static final String DEFAULT_API_URL = "";
    private final RestTemplate restTemplate;

    public CentreStatusService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetches the centre status from the API based on the configured AE Title
     */
    public boolean updateCentreStatus() {
        try {
            String aeTitle = Context.getStoreSCUConfigurationProperties().getCallingAetName();
            if (aeTitle == null || aeTitle.trim().isEmpty()) {
                LoggerUtility.log(CentreStatusService.class, LoggerUtility.LogLevel.WARN, 
                    "AE Title is not configured, skipping centre status check");
                return false;
            }

            // Get API URL from configuration, fallback to default if not set
            String apiUrl = Context.getStartupConfigurationProperties().getCentreStatusApiUrl();
            if (apiUrl == null || apiUrl.trim().isEmpty()) {
                 try {
                JOptionPane.showMessageDialog(null, 
                    "Application Base API URL is not configured.", 
                    "eCScribe PACS", 
                    JOptionPane.WARNING_MESSAGE);
            } catch (Exception ignored) {
            }
            }

            String url = "https://"+apiUrl+"/api/centre/centre-status" + "?aeTitle=" + aeTitle;
            LoggerUtility.log(CentreStatusService.class, LoggerUtility.LogLevel.INFO, 
                "Fetching centre status from: " + url);

            CentreStatusResponse response = restTemplate.getForObject(url, CentreStatusResponse.class);
            
            if (response != null) {
                LoggerUtility.log(CentreStatusService.class, LoggerUtility.LogLevel.INFO, 
                    "Centre status received - Status: " + response.getStatus() + ", RetryLimit: " + response.getRetryLimit());
                
                // Update configuration with API response
                Context.getStoreSCPConfigurationProperties().setStatus(response.getStatus());
                Context.getStoreSCPConfigurationProperties().setRetryLimit(response.getRetryLimit());
                
                // Return true if status is 1 (active)
                return response.getStatus() != null && response.getStatus() == 1;
            } else {
                LoggerUtility.log(CentreStatusService.class, LoggerUtility.LogLevel.ERROR, 
                    "Received null response from centre status API");
                return false;
            }
        } catch (Exception e) {
            LoggerUtility.log(CentreStatusService.class, 
                "Error fetching centre status: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if the centre is active without updating configuration
     * @return true if status is 1, false otherwise
     */
    public boolean isCentreActive() {
        Integer status = Context.getStoreSCPConfigurationProperties().getStatus();
        return status != null && status == 1;
    }

    @Data
    public static class CentreStatusResponse {
        @JsonProperty("status")
        private Integer status;
        
        @JsonProperty("retryLimit")
        private Integer retryLimit;
    }
}
