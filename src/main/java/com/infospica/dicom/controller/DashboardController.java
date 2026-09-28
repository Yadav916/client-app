package com.infospica.dicom.controller;

import com.infospica.dicom.config.StartupConfigurationProperties;
import com.infospica.dicom.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping(path = {"/", "/dashboard"})
    public String showDashboard() {
        System.out.println("= StartupConfigurationProperties == " + Context.getStartupConfigurationProperties());
        return "index";
    }
}
