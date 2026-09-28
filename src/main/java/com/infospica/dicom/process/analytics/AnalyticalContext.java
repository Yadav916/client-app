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

package com.infospica.dicom.process.analytics;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *   @author arun.vs
 */
public class AnalyticalContext {
    private final AtomicBoolean scpEchoStatus = new AtomicBoolean(false);
    private final AtomicBoolean scuEchoStatus = new AtomicBoolean(false);
    private final AtomicInteger pendingCount = new AtomicInteger();

    private final AtomicInteger jsonExtractCount = new AtomicInteger();
    private final AtomicInteger jsonExtractProcessedCount = new AtomicInteger();

    private final AtomicInteger modifiedCount = new AtomicInteger();
    private final AtomicInteger modifyProcessedCount = new AtomicInteger();

    private final AtomicInteger compressedCount = new AtomicInteger();
    private final AtomicInteger compressProcessedCount = new AtomicInteger();

    private final AtomicInteger uploadPendingCount = new AtomicInteger();
    private final AtomicInteger processedCount = new AtomicInteger();
    private final AtomicInteger processedErrorCount = new AtomicInteger();
    private final AtomicInteger goodCount = new AtomicInteger();
    private final AtomicInteger badCount = new AtomicInteger();

    private String scpEchoResponse = null;


    public AnalyticalContext() {

    }

    //pending images
    public void setPendingCount(Integer count) {
        /*if(count == 0) pendingCount.set(count);
        pendingCount.set(pendingCount.get() + count);*/
        pendingCount.set(count);
    }
    public Integer getPendingCount() {
        return pendingCount.get();
    }
    public Integer updatePending(int count) {
        if(pendingCount.get() <= 0) {
            pendingCount.set(0);
            return 0;
        } //normally this case should not exists
        return pendingCount.addAndGet(-1 * count);
    }

    // json extract count
    public void setJsonExtractCount(Integer count) {
        jsonExtractCount.set(count);
    }
    public Integer getJsonExtractCount() {
        return jsonExtractCount.get();
    }
    public void setJsonProcessedCount(Integer count) {
        jsonExtractProcessedCount.set(count);
    }
    public Integer updateJsonExtracting() {
        return jsonExtractProcessedCount.incrementAndGet();
    }
    public Integer getJsonProcessedCount() {
        return jsonExtractProcessedCount.get();
    }

    //modified count
    public void setModifiedCount(Integer count) {
        modifiedCount.set(count);
    }
    public Integer getModifiedCount() {
        return modifiedCount.get();
    }
    public void setModifiedProcessedCount(Integer count) {
        modifyProcessedCount.set(count);
    }
    public Integer updateModification() {
        return modifyProcessedCount.incrementAndGet();
    }
    public Integer getModifyProcessedCount() {
        return modifyProcessedCount.get();
    }

    //compressed count
    public void setCompressedCount(Integer count) {
        compressedCount.set(count);
    }
    public Integer getCompressedCount() {
        return compressedCount.get();
    }
    public void setCompressProcessedCount(Integer count) {
        compressProcessedCount.set(count);
    }
    public Integer updateCompression() {
        return compressProcessedCount.incrementAndGet();
    }
    public Integer getCompressProcessedCount() {
        return compressProcessedCount.get();
    }

    //pending images to upload
    public void setUploadPendingCount(Integer count) {
        uploadPendingCount.set(count);
    }
    public Integer getUploadPendingCount() {
        return uploadPendingCount.get();
    }
    public Integer updateUploadPendingCount() {
        if(uploadPendingCount.get() <= 0) {
            uploadPendingCount.set(0);
            return 0;
        }; //normally this case should not exists
        return uploadPendingCount.decrementAndGet();
    }
    public Integer updateUploadPendingCount(int count) {
        if(uploadPendingCount.get() <= 0) {
            uploadPendingCount.set(0);
            return uploadPendingCount.get(); //normally this case should not exists
        }
        return uploadPendingCount.addAndGet(-1 * count);
    }

    //processed count
    public void setProcessedCount(Integer count) {
        processedCount.set(count);
    }
    public Integer getProcessedCount() {
        return processedCount.get();
    }
    public Integer updateProcessed() {
        return processedCount.incrementAndGet();
    }
    public Integer updateProcessed(int count) {
        return processedCount.addAndGet(count);
    }

    //processed error count
    public void setProcessedErrorCount(Integer count) {
        processedErrorCount.set(count);
    }
    public Integer getProcessedErrorCount() {
        return processedErrorCount.get();
    }
    public Integer updateProcessedError() {
        return processedErrorCount.incrementAndGet();
    }
    public Integer updateProcessedError(int count) {
        return processedErrorCount.addAndGet(count);
    }

    //good images count
    public void setGoodCount(Integer count) {
        goodCount.set(count);
    }
    public Integer getGoodCount() {
        return goodCount.get();
    }
    public Integer updateGood() {
        return goodCount.incrementAndGet();
    }
    public Integer updateGood(int count) {
        return goodCount.addAndGet(count);
    }

    //bad images count
    public void setBadCount(Integer count) {
        badCount.set(count);
    }
    public Integer getBadCount() {
        return badCount.get();
    }
    public Integer updateBad() {
        return badCount.incrementAndGet();
    }
    public Integer updateBad(int count) {
        return badCount.addAndGet(count);
    }

    public void updateSCPEchoStatus(boolean status) {
        scpEchoStatus.set(status);
    }
    public boolean getSCPEchoStatus() {
        return scpEchoStatus.get();
    }
    public void updateSCPEchoResponse(String response) {
        this.scpEchoResponse = response;
    }
    public String getSCPEchoResponse() {
        return this.scpEchoResponse;
    }

    public void updateSCUEchoStatus(boolean status) {
        scuEchoStatus.set(status);
    }
    public boolean getSCUEchoStatus() {
        return scuEchoStatus.get();
    }

}
