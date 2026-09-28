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

package com.infospica.dicom.process;

import com.infospica.dicom.command.pool.ExtractDicomCommandPool;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 *   @author arun.vs
 */
@Component
public class ExtractTask {

    @Autowired
    private ApplicationContext applicationContext;
    private ExtractDicomCommandPool extractDicomCommandPool;

    public Mono<String> toJson(File srcFile) {
        try {
            String result = extractDicomCommandPool.execute(srcFile);
            LoggerUtility.log(ExtractTask.class, LoggerUtility.LogLevel.DEBUG, (result.length() > 100 ? result.substring(0, 100) + "..." : result));
            Context.updateJsonExtractImage();
            return Mono.just(result);
        } catch (Exception e) {
            LoggerUtility.log(ExtractTask.class,  LoggerUtility.LogLevel.ERROR, "Error during extract json from the image: Cause" + e.getMessage());
        }
        return Mono.just("#ERROR");
    }

    public String toJson(File srcFile, boolean flag) {
        try {
            String result = extractDicomCommandPool.execute(srcFile);
            LoggerUtility.log(ExtractTask.class, LoggerUtility.LogLevel.DEBUG, (result.length() > 100 ? result.substring(0, 100) + "..." : result));
            Context.updateJsonExtractImage();
            return result;
        } catch (Exception e) {
            LoggerUtility.log(ExtractTask.class,  LoggerUtility.LogLevel.ERROR, "Error during extract json from the image: Cause" + e.getMessage());
        }
        return "#ERROR";
    }

    @PostConstruct
    public void initCommandPool() {
        this.extractDicomCommandPool = new ExtractDicomCommandPool(applicationContext);
    }
}
