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

import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;

/**
 *   @author arun.vs
 */
@Component
public class UploadTask {

    @Autowired
    private ApplicationContext applicationContext;

    public Mono<File> uploadFile(File srcFile) {
        try {
            String uploadedFile = ((ExecutableCommand)applicationContext.getBean("uploadDicomCommand")).execute(srcFile.getAbsolutePath());
            //LoggerUtility.log(UploadTask.class, LoggerUtility.LogLevel.DEBUG, uploadedFile);
            LoggerUtility.log(UploadTask.class, LoggerUtility.LogLevel.DEBUG, srcFile.getAbsolutePath() + "[Uploaded]");
            Context.updateProcessedImage();
            return Mono.just(srcFile);
        } catch (Exception e) {
            LoggerUtility.log(UploadTask.class,  LoggerUtility.LogLevel.ERROR, "Error during uploading the image: Cause" + e.getMessage());
        }
        return Mono.just(new File(srcFile.getParent(), srcFile.getName().concat("#UPLOADERROR")));
    }

}
