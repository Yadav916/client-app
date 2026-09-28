package com.infospica.dicom.process.state.batch;

import com.infospica.dicom.JobsInitializer;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.MoveTask;
import com.infospica.dicom.process.batch.BatchDicomFileProcessor;
import com.infospica.dicom.process.state.AbstractImageState;
import com.infospica.dicom.service.CentreStatusService;
import com.infospica.dicom.util.LoggerUtility;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

@Component("_batchMoveImageState")
public class MoveImageState extends AbstractImageState {

    private final MoveTask moveTask;
    private final CentreStatusService centreStatusService;
    private final ApplicationContext applicationContext;
    private BatchDicomFileProcessor processor;

    @Autowired
    public MoveImageState(final MoveTask moveTask, final CentreStatusService centreStatusService, final ApplicationContext applicationContext) {
        super("Batch_Move_001");
        this.moveTask = moveTask;
        this.centreStatusService = centreStatusService;
        this.applicationContext = applicationContext;
    }

    public void setProcessor(BatchDicomFileProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        // Check centre status before moving any images
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "Checking centre status before moving images");
        boolean isCentreActive = centreStatusService.updateCentreStatus();
        
        if (!isCentreActive) {
            LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.WARN,
                "Centre is not active - Stopping upload service");
            // Show message to user
            try {
                JOptionPane.showMessageDialog(null, 
                    "Centre is not active. Upload service stopped.", 
                    "eCScribe PACS", 
                    JOptionPane.WARNING_MESSAGE);
            } catch (Exception ignored) {
            }
            
            try {
                JobsInitializer jobsInitializer = applicationContext.getBean(JobsInitializer.class);
                jobsInitializer.stopUploadService();
                LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, 
                    "Upload service stopped due to inactive centre status");
            } catch (Exception e) {
                LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.ERROR, 
                    "Error while stopping upload service: " + e.getMessage());
            }
            Context.setUploadServiceStarted(false);
            return;
        }
        
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "Centre is active, proceeding with moving files");
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getMachineDestinationFolder());

        //create missing folder if any; this requirement is unique as the user may clear the intermediate folders without stopping the process.
        Context.createMandatoryFolderIfMissing(Context.getProcessSourceFolder());
        Context.createMandatoryFolderIfMissing(Context.getMachineDestinationFolder());

        if(Context.getMachineDestinationFolder() != null && Context.getMachineDestinationFolder().exists()) {
            Collection<File> files = FileUtils.listFiles(Context.getMachineDestinationFolder(),
                    new NotFileFilter(new OrFileFilter(
                            new SuffixFileFilter(".part"), HiddenFileFilter.HIDDEN)
                    ), TrueFileFilter.INSTANCE);

            if (files.size() == 0) {
                processor.setState(processor.getMoveImageState());
                Context.setLastProcessDone(new Date());
                return;
            }

            AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getExtractJSONState());
            int fileCount = moveFilesToProcessFolder(moveTask, Context.getMachineDestinationFolder(), Context.getProcessSourceFolder());
            if (fileCount < 0) { // -99 is error
                if(fileCount == -777) { //means interrupted; don't know how many files get transferred during the process.
                    processor.forceToState(stateAtomicReference.getAndSet(null));
                    return;
                }
                stateAtomicReference.set(processor.getMoveImageState());
            }
            if(files != null) {
                files.clear(); //deallocate all
            }
            Context.setLastProcessDone(null); //reset the process done time
            processor.setState(stateAtomicReference.getAndSet(null));
        }
    }

    @Override
    public void extractJsonFromImages() {
        if(!Context.isJsonExtractionEnabled()) return;
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "No images are extracted");

    }

    @Override
    public void modifyImages() {
        if(!Context.isAnonymizeEnabled()) return;
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to modify");
    }

    @Override
    public void compressImages() {
        if(!Context.isCompressionEnabled()) return;
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to compress");
    }

    @Override
    public void pushImages() {
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to move to server");
    }

    @Override
    public void uploadImages() {
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to upload");
    }
    @Override
    public void backupImages() {
        LoggerUtility.log(MoveImageState.class, LoggerUtility.LogLevel.INFO, "No images are found to backup");
    }
}
