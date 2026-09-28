package com.infospica.dicom.util;

import com.sun.management.OperatingSystemMXBean;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CpuUsageUtility {

    public static Object getCpuUsage() {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        Double value = operatingSystemMXBean.getProcessCpuLoad();
        if (value == -1.0) return Double.NaN;
        return ((int)(value * 1000) / 10.0);
    }
}
