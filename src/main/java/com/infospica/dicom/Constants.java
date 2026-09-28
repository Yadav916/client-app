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

package com.infospica.dicom;

import org.apache.commons.lang3.SystemUtils;

import java.text.SimpleDateFormat;

/**
 *   @author arun.vs
 */
public class Constants {

    private Constants() {
        super();
    }

    //command bean name
    public static final String MODIFY_COMMAND_BEAN_NAME = "modifyDicomCommand";
    public static final String COMPRESS_COMMAND_BEAN_NAME = "compressDicomCommand";
    public static final String EXTRACT_COMMAND_BEAN_NAME = "extractDicomCommand";
    public static final String UPLOAD_COMMAND_BEAN_NAME = "uploadDicomCommand";
    public static final String SIMPLE_UPLOAD_COMMAND_BEAN_NAME = "simpleUploadDicomCommand";



    public static SimpleDateFormat _FOLDER_FORMAT = new SimpleDateFormat("dd-MM-yyyy_HHmmss_SSS");
    public static SimpleDateFormat _SCP_LOG_FILE_FORMAT = new SimpleDateFormat("dd-MM-yyyy_000000_777");
    public static SimpleDateFormat _DATE_ONLY = new SimpleDateFormat("dd-MM-yyyy");
    public static SimpleDateFormat _TIME_ONLY = new SimpleDateFormat("HHmmss");
    public static SimpleDateFormat _TIME_MILLIS_ONLY = new SimpleDateFormat("SSS");

    public static String COMMAND_EXTENSION = SystemUtils.IS_OS_WINDOWS ? ".bat" : "";
    public static String SCRIPT_COMMAND_EXTENSION = SystemUtils.IS_OS_WINDOWS ? ".bat" : ".sh";

    public static String SYSTEM_EXCLUDED_FILES = ".DS_Store";
}
