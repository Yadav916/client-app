package com.infospica.dicom.process.batch;

import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.state.batch.*;
import com.infospica.dicom.util.DirectoryUtility;
import com.infospica.dicom.util.LoggerUtility;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.*;
import java.util.Collection;

@Data
@Getter
@Setter
public class BatchDicomFileProcessor {

    private final MoveImageState moveImageState;
    private final ExtractJSONState extractJSONState;
    private final ModifyImageState modifyImageState;
    private final CompressImageState compressImageState;
    private final PushImageState pushImageState;
    private final ProcessStopState processStopState;

    private DicomProcessState state;

    protected static long READ_POSITION = 0;

    //Thread instances
    private Runnable runnableProcessFiles;
    private Runnable runnableMonitorAnonymizedFolder;
    private Runnable runnableMonitorCompressedFolder;
    private Runnable runnableStart;
    private Runnable runnableStop;
    private Runnable runnableMonitorPendingFiles;

    //Flux instances
    private Flux<Integer> fluxAnonymized;
    private Flux<Integer> fluxCompressed;

    public BatchDicomFileProcessor(final MoveImageState moveImageState,
                                   final ExtractJSONState extractJSONState, final ModifyImageState modifyImageState,
                                   final CompressImageState compressImageState, final PushImageState pushImageState,
                                   final ProcessStopState processStopState) {
        this.moveImageState =  moveImageState;
        this.extractJSONState = extractJSONState;
        this.modifyImageState = modifyImageState;
        this.compressImageState = compressImageState;
        this.pushImageState = pushImageState;
        this.processStopState = processStopState;
        setProcessor();
        setState(this.moveImageState);
    }

    private void setProcessor() {
        this.moveImageState.setProcessor(this);
        this.extractJSONState.setProcessor(this);
        this.modifyImageState.setProcessor(this);
        this.compressImageState.setProcessor(this);
        this.pushImageState.setProcessor(this);
        this.processStopState.setProcessor(this);
    }

    public Runnable processFiles() {
        if(runnableProcessFiles == null) {
            runnableProcessFiles = () -> {
                if (this.state != null) {
                    this.state.moveImages();
                }
                if (this.state != null) {
                    this.state.extractJsonFromImages();
                }
                if (this.state != null) {
                    this.state.modifyImages();
                }
                if (this.state != null) {
                    this.state.compressImages();
                }
                if (this.state != null) {
                    this.state.pushImages();
                }
            };
        }
        return runnableProcessFiles;
    }

    public Runnable monitorAnonymizedFolder() {
        if(runnableMonitorAnonymizedFolder == null) {
            runnableMonitorAnonymizedFolder = () -> {
                getFluxForAnonymizedFileCount().subscribe(c -> {
                    if (Context.getModifiedImage() <= c) {
                        Context.setModifiedImage(c);
                    }
                }).dispose();
            };
        }
        return runnableMonitorAnonymizedFolder;
    }

    public Runnable monitorCompressedFolder() {
        if(runnableMonitorCompressedFolder == null) {
            runnableMonitorCompressedFolder = () -> {
                getFluxForCompressedFileCount().subscribe(c -> {
                    if (Context.getCompressedImage() <= c) {
                        Context.setCompressedImage(c);
                    }
                }).dispose();
            };
        }
        return runnableMonitorCompressedFolder;
    }

    public Runnable start() {
        if(runnableStart == null) {
            runnableStart = () -> {
                synchronized (BatchDicomFileProcessor.class) {
                    restoreState("_state");
                }
            };
        }
        return runnableStart;
    }

    public Runnable stop() {
        if(runnableStop == null) {
            runnableStop = () -> {
                synchronized (BatchDicomFileProcessor.this) {
                    saveCurrentState();
                    this.state = this.processStopState;
                }
            };
        }
        return runnableStop;
    }

    public void setState(DicomProcessState state) {
        LoggerUtility.log(BatchDicomFileProcessor.class, LoggerUtility.LogLevel.INFO, "Is stopped state: " + (this.state == this.processStopState));
        if(this.state != this.processStopState) {
            if(state != this.state) {
                saveCurrentState();
            }
            this.state = state;
        }
    }

    public void forceToState(DicomProcessState state) {
        saveState(state.getId(), "_state");
        if(this.state != this.processStopState) {
            this.state = state;
        }
    }

    public void restoreState(String stateFile) {
        if (this.state == null || this.state == this.processStopState) {
            String previousState = null;
            if (Context.getCommandWorkingFolder() != null && Context.getCommandWorkingFolder().exists()) {
                File file = new File(Context.getCommandWorkingFolder(), stateFile);
                try {
                    //previousState = SerializerUtility.deserialize(file);
                    if(file.exists()) {
                        previousState = FileUtils.readFileToString(file, "UTF-8");
                    }
                } catch (Exception e) {
                    LoggerUtility.log(BatchDicomFileProcessor.class, "Error loading previous the state.", e);
                } finally {
                    clearSavedState();
                }
            }
            LoggerUtility.log(BatchDicomFileProcessor.class, LoggerUtility.LogLevel.INFO, "Previous state from file: " + previousState);
            if (previousState == null) this.state = this.moveImageState;
            else this.state = getMatchedState(previousState);
        }
    }

    private void saveCurrentState() {
        if(this.state != null) {
            saveState(this.state.getId(), "_state");
        }
    }

    private void saveState(String stateId, String stateFile) {
        try {
            LoggerUtility.log(BatchDicomFileProcessor.class, LoggerUtility.LogLevel.INFO, "The current state is: " + stateId);
            //SerializerUtility.serialize(stateId, new File(Context.getCommandWorkingFolder(), stateFile));
            File stateFileObj = new File(Context.getCommandWorkingFolder(), stateFile);
            FileUtils.writeStringToFile(stateFileObj, stateId, "UTF-8");
        } catch (Exception e) {
            LoggerUtility.log(BatchDicomFileProcessor.class, "Error saving the state.", e);
        }
    }

    private Flux<Integer> getFluxForAnonymizedFileCount() {
        if(fluxAnonymized == null) {
            fluxAnonymized = Flux.create((FluxSink<Integer> fluxSink) -> {
                int fileSize = 0;
                if (Context.getAnonymizedDestinationFolder().exists()) {
                    Collection<File> files = FileUtils.listFiles(Context.getAnonymizedDestinationFolder(),
                            TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
                    fileSize = files.size();
                }
                fluxSink.next(fileSize);
                fluxSink.complete();
            });
        }
        return fluxAnonymized;
    }

    private Flux<Integer> getFluxForCompressedFileCount() {
        if(fluxCompressed == null) {
            fluxCompressed = Flux.create((FluxSink<Integer> fluxSink) -> {
                int fileSize = 0;
                if (Context.getCompressDestinationFolder().exists()) {
                    Collection<File> files = FileUtils.listFiles(Context.getCompressDestinationFolder(),
                            TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
                    fileSize = files.size();
                }
                fluxSink.next(fileSize);
                fluxSink.complete();
            });
        }
        return fluxCompressed;
    }

    /**
     * Monitor the file count
     */
    public Runnable monitorFileCount() {
        if(runnableMonitorPendingFiles == null) {
            runnableMonitorPendingFiles = () -> {
                try {
                    int pendingCount = DirectoryUtility.getFileCount(Context.getMachineDestinationFolder().toPath(), 0);
                    Context.setPendingImage(pendingCount);

                    int toProcessFileCount = DirectoryUtility.getFileCount(Context.getProcessSourceFolder().toPath(), 0);
                    if (Context.isJsonExtractionEnabled()) {
                        int extractCount = toProcessFileCount;
                        if(extractCount == 0) Context.setJsonProcessedCount(0);
                        //extractCount -= Context.getJsonProcessedCount();
                        Context.setJsonExtractImage(extractCount);
                    }
                    if (Context.isAnonymizeEnabled()) {
                        int anonymizeCount = toProcessFileCount;
                        if(anonymizeCount == 0) Context.setModifiedProcessedCount(0);
                        //anonymizeCount -= Context.getModifyProcessedCount();
                        Context.setModifiedImage(anonymizeCount);
                    }
                    if (Context.isCompressionEnabled()) {
                        int compressCount = DirectoryUtility.getFileCount(Context.getCompressSourceFolder().toPath(), 0);
                        if(compressCount == 0) Context.setCompressProcessedCount(0);
                        Context.setCompressedImage(compressCount);
                    }
                    //check the temporary folder
                    /*File tmpProcessFolder = new File(Context.getUploadProcessFolder(), "_@@tmp@@_");
                    if(tmpProcessFolder.exists()) {
                        uploadPendingCount = DirectoryUtility.getFileCount(tmpProcessFolder.toPath(), 0);
                    }
                    uploadPendingCount += DirectoryUtility.getFileCount(Context.getUploadProcessFolder().toPath(), 0);*/
                    int uploadPendingCount = DirectoryUtility.getFileCount(Context.getUploadProcessFolder().toPath(), 0);
                    Context.setUploadPendingCount(uploadPendingCount);

                } catch (Exception e) {

                }
            };
        }
        return runnableMonitorPendingFiles;
    }

    private void clearSavedState() {
        try {
            FileUtils.delete(new File(Context.getCommandWorkingFolder(), "_state"));
        } catch (Exception e) { /* Nothing to print */ }
    }

    private DicomProcessState getMatchedState(String id) {
        switch(id) {
            case "Batch_Compression_001": return this.compressImageState;
            case "Batch_ExtractJSON_001": return this.extractJSONState;
            case "Batch_Modify_001": return this.modifyImageState;
            case "Batch_Move_001": return this.moveImageState;
            case "Batch_Push_001": return this.pushImageState;
        }
        return this.moveImageState;
    }
}
