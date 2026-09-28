package com.infospica.dicom.process.state.batch;

import com.infospica.dicom.Constants;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.DicomProcessState;
import com.infospica.dicom.process.MoveTask;
import com.infospica.dicom.process.batch.BatchDicomFileProcessor;
import com.infospica.dicom.process.batch.BatchDicomUploadProcessor;
import com.infospica.dicom.process.batch.UploadTask;
import com.infospica.dicom.process.state.AbstractImageState;
import com.infospica.dicom.process.state.batch.response.upload.FileScanErrorFilter;
import com.infospica.dicom.process.state.batch.response.upload.UploadErrorFilter;
import com.infospica.dicom.process.state.batch.response.upload.UploadResponseParser;
import com.infospica.dicom.service.SCUService;
import com.infospica.dicom.util.EchoServer;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component("_batchUploadImageState")
public class UploadImageState extends AbstractImageState {

    private final MoveTask moveTask;
    private final UploadTask uploadTask;
    private final SCUService scuService;
    private BatchDicomUploadProcessor processor;

    private Set<File> fileSet = new HashSet<>();
    private Map<String, Object[]> batchProcessStatus = new HashMap<>();

    private final UploadResponseParser uploadResponseParser = new UploadResponseParser();
    private final FileScanErrorFilter fileScanErrorFilter = new FileScanErrorFilter(uploadResponseParser);
    private UploadErrorFilter uploadErrorFilter = new UploadErrorFilter(uploadResponseParser);

    @Autowired
    public UploadImageState(final MoveTask moveTask, final UploadTask uploadTask, final SCUService scuService) {
        super("Batch_Upload_001");
        this.moveTask = moveTask;
        this.uploadTask = uploadTask;
        this.scuService = scuService;

        //set the filters
        fileScanErrorFilter.setNextFilter(uploadErrorFilter);
    }

    public void setProcessor(BatchDicomUploadProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void moveImages() {
        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Already moved the images");
    }

    @Override
    public void extractJsonFromImages() {
        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Already extracted the images");
    }

    @Override
    public void modifyImages() {
        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Already modified the images");
    }

    @Override
    public void compressImages() {
        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Already compressed the images");
    }

    @Override
    public void pushImages() {

    }

    @Override
    public void uploadImages() {
        //LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Start uploading the images");
        //LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Considering the source folder: " + Context.getUploadProcessFolder());

        Context.createMandatoryFolderIfMissing(Context.getUploadProcessFolder()); //create upload process folder if missing
        Context.createMandatoryFolderIfMissing(Context.getUploadBackupFolder()); //create upload backup folder if missing
        //Context.createMandatoryFolderIfMissing(Context.getScuProcessMetadataFolder()); //create scu metadata store folder

        AtomicReference<DicomProcessState> stateAtomicReference = new AtomicReference<>(processor.getBackupState());
        if(Context.getUploadProcessFolder() != null && Context.getUploadProcessFolder().exists()) {
            try {
                //reset the count
                //Context.setUploadPendingCount(fileSet.size());
                Context.setGoodImage(0);
                Context.setBadImage(0);
                Context.setProcessedImage(0);

                //create a temporary folder inside the upload process folder; if not exists
                File tmpProcessFolder = new File(Context.getUploadProcessFolder(), "_@@tmp@@_");
                FileUtility.makeDirectories(tmpProcessFolder);

                fileSet.clear(); //clear all before starting
                renameErrorFiles(Context.getUploadProcessFolder()); // process error files in main todicom folder
                renameErrorFiles(tmpProcessFolder); // this happens if there happen any error during upload and the application stopped.
                moveFilesToProcessFolder(moveTask, tmpProcessFolder, Context.getUploadProcessFolder(), null);

                // normal proceeding
                Collection<File> existingFiles = FileUtils.listFiles(Context.getUploadProcessFolder(),
                        new NotFileFilter(new OrFileFilter(HiddenFileFilter.HIDDEN)), TrueFileFilter.INSTANCE);
                fileSet.addAll(existingFiles);

                fileSet.removeIf(f -> f.getName().contains("#ERROR")); //skip the error files (including #ERROR#1, #ERROR#2, etc.)
                if (fileSet.isEmpty()) {
                    return;
                }

                File backupFolder = new File(Context.getUploadBackupFolder(), Constants._FOLDER_FORMAT.format(new Date()));
                List<File> failedFiles = new ArrayList<>();
                while(!fileSet.isEmpty() && Context.isUploadServiceStarted()) {
                    int toProcessImageCount = Context.getStoreSCUConfigurationProperties().getMaxFilesPerUpload();
                    if (toProcessImageCount > fileSet.size()) toProcessImageCount = fileSet.size();
                    String batchIdentifier = prepareFilesToUpload(fileSet, toProcessImageCount, tmpProcessFolder);

                    setProcessStatus(batchIdentifier, toProcessImageCount, true);

                    String response = uploadTask.uploadFile(tmpProcessFolder);
                    LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, response);

                    if (response == null) { //means error; should continue
                        Collection<File> tmpFailedFiles = FileUtils.listFiles(tmpProcessFolder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
                        tmpFailedFiles.stream()
                                .map(f -> new File(f.getParentFile(), f.getName().concat("#ERROR")))
                                .collect(Collectors.toCollection(() -> failedFiles));
                        tmpFailedFiles.clear(); //can clear the failed files
                    } else {
                        uploadResponseParser.setResponse(response);
                        fileScanErrorFilter.applyFilter();
                        uploadResponseParser.getFileList().stream()
                                .map(fs1 -> new File(fs1.getParentFile(), fs1.getName().concat("#ERROR")))
                                .collect(Collectors.toCollection(() -> failedFiles));
                        uploadResponseParser.reset();
                    }
                    int failedFileCount = failedFiles.size();
                    if (failedFileCount > 0) {
                        Scheduler scheduler = Schedulers.boundedElastic();
                        Flux.fromStream(failedFiles.stream()).flatMap(f1 -> {
                                if (!f1.exists()) { //if exists means error
                                    try {
                                        Context.updateProcessedImage();
                                        File destFile = new File(f1.getParentFile(), f1.getName());
                                        return Mono.just(FileUtility.moveAsErrorFile(f1, destFile, "#ERROR"));
                                    } catch (IOException ioe) {
                                        LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.ERROR, "Error during moving the upload failed image: " + f1.getAbsolutePath());
                                    }
                                }
                                return Mono.empty();
                            }).publishOn(scheduler)
                            .doOnComplete(() -> {
                                LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Uploaded the images");
                            }).blockLast();
                        if (!scheduler.isDisposed()) {
                            scheduler.disposeGracefully();
                        }
                        updateProcessStatusOnBatchFailure(batchIdentifier, failedFileCount); // update batch on failure
                    } //if (failedFileCount > 0) {

                    LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Considering the backup folder: " + backupFolder.getAbsolutePath());
                    backupUploadedFiles(backupFolder, tmpProcessFolder);

                    renameErrorFiles(tmpProcessFolder);
                    moveErrorFiles(Context.getUploadProcessFolder(), tmpProcessFolder);

                    Context.updateBadImage(failedFileCount);
                    //Context.updateUploadPendingCount(failedFileCount);

                    //clear the list
                    failedFiles.clear();
                } //while(!fileSet.isEmpty()) {

                LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "*** Completed uploading and moving to next state.");
            } catch (Exception e) {
                LoggerUtility.log(UploadImageState.class, "*** Uploading with error and moving to next state.", e);
            } finally {
                fileSet.clear();
            }
            if(hasAllBatchFailed()) {
                LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "All batches in this process have failed. Therefore, we are suspending the process temporarily.");
            }
            batchProcessStatus.clear();
            processor.setState(stateAtomicReference.getAndSet(null));
        }
    }

    @Override
    public void backupImages() {

    }

    private void setProcessStatus(String batchIdentifier, int toProcessImageCount, boolean status) {
        //note of the batch count and status
        batchProcessStatus.put(batchIdentifier, new Object[] {toProcessImageCount, status});
    }

    private void updateProcessStatusOnBatchFailure(String batchIdentifier, int failedImageCount) {
        int toProcessImageCount = (int)batchProcessStatus.get(batchIdentifier)[0];
        if(toProcessImageCount == failedImageCount) {
            batchProcessStatus.computeIfPresent(batchIdentifier, (String k, Object[] v) -> {
                v[1] = false;
                return v;
            });
        }
    }

    private boolean hasAllBatchFailed() {
        return !(batchProcessStatus.values().stream().filter(o -> ((Boolean)o[1]) == true).count() > 0);
    }

    private String prepareFilesToUpload(Set<File> fileSet, int toProcessImageCount, File tmpProcessFolder) {
        Iterator<File> fileIterator = fileSet.iterator();
        for(int processedImageCount = 1; processedImageCount <= toProcessImageCount; processedImageCount++) {
            File fileToUpload = fileIterator.next();
            String relativePath = FileUtility.getRelativePathFrom(fileToUpload.getAbsolutePath(), Context.getUploadProcessFolder().getAbsolutePath());
            try {
                FileUtils.moveFile(fileToUpload, new File(tmpProcessFolder, relativePath));
            } catch (IOException e) {
                e.printStackTrace();
                LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.ERROR,"Error moving file to temporary upload process folder. Cause: " + e.getMessage());
            }
            fileIterator.remove(); // remove the file
        }
        return UUID.randomUUID().toString(); // batch identifier
    }

    private void moveErrorFiles(File uploadProcessFolder, File tmpUploadProcessFolder) {
        if(!tmpUploadProcessFolder.exists()) return;
        Collection<File> files = FileUtils.listFiles(tmpUploadProcessFolder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        files.stream().forEach(file -> {
            String relativePath = FileUtility.getRelativePathFrom(file.getAbsolutePath(), tmpUploadProcessFolder.getAbsolutePath());
            try {
                FileUtils.moveFile(file, new File(uploadProcessFolder, relativePath));
            } catch (IOException e) {
                LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.ERROR,"Error moving error file from temporary upload process to main. Cause: " + e.getMessage());
            }
        });
    }

    private void backupUploadedFiles(File backupFolder, File tmpUploadProcessFolder) {
        if(!Context.shallCleanImmediately()) FileUtility.makeDirectories(backupFolder);

        //create backup of success files
        Collection<File> files = FileUtils.listFiles(tmpUploadProcessFolder,
                new NotFileFilter(
                        new OrFileFilter(new SuffixFileFilter("#ERROR"))
                ), TrueFileFilter.INSTANCE);
        if(files.isEmpty()) return;

        Scheduler scheduler1 = Schedulers.boundedElastic();
        Scheduler scheduler2 = Schedulers.boundedElastic();

        Flux.fromStream(files.stream())
            .parallel()
            .runOn(scheduler1)
            .flatMap(f -> {
                try {
                    Context.updateProcessedImage();
                    //Context.updateUploadPendingCount();
                    Context.updateGoodImage();

                    if(!Context.shallCleanImmediately()) {
                        String relativePath = FileUtility.getRelativePathFrom(f.getAbsolutePath(), tmpUploadProcessFolder.getAbsolutePath());
                        File backupFile = new File(backupFolder, relativePath);
                        FileUtils.moveFile(f, backupFile);
                        return Mono.just(backupFile);
                    }
                    FileUtils.deleteQuietly(f);
                    return Mono.just(f);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.ERROR, "Error during backup uploaded image: " + f.getAbsolutePath());
                }
                return Mono.empty();
            }).sequential()
            .publishOn(scheduler2)
            .doOnComplete(() -> {
                LoggerUtility.log(UploadImageState.class, LoggerUtility.LogLevel.INFO, "Uploaded the images");
            }).doOnError(throwable -> {
                LoggerUtility.log(UploadImageState.class, "Error during uploading: ", throwable);
            }).blockLast();

        if(!scheduler1.isDisposed()) scheduler1.disposeGracefully();
        if(!scheduler2.isDisposed()) scheduler2.disposeGracefully();
    }

}
