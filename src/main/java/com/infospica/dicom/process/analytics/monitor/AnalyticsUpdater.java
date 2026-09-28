/**
 * Licensed to the Cirakas Consulting Pvt Ltd under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.cirakas.com/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.infospica.dicom.process.analytics.monitor;

import java.util.ArrayList;
import java.util.List;

/**
 *   @author arun.vs
 */
public class AnalyticsUpdater {
    private final List<ProcessMonitor> monitors = new ArrayList<>();

    public void attach(ProcessMonitor monitor) {
        monitors.add(monitor);
    }

    public void notifyAllMonitors() {
        for(ProcessMonitor monitor : monitors) {
            monitor.doUpdate();
        }
    }

    private static class AnalyticsUpdaterHolder {
        private static AnalyticsUpdater instance = null;
    }

    public static AnalyticsUpdater getInstance() {
        if(AnalyticsUpdaterHolder.instance == null) {
            AnalyticsUpdaterHolder.instance = new AnalyticsUpdater();
        }
        return AnalyticsUpdaterHolder.instance;
    }
}
