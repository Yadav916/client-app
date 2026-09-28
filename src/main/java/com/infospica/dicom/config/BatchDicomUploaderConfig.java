package com.infospica.dicom.config;

import com.infospica.dicom.command.batch.BatchCompressFileCommand;
import com.infospica.dicom.command.batch.BatchModifyDicomCommand;
import com.infospica.dicom.command.batch.BatchUploadFileCommand;
import com.infospica.dicom.command.pool.ToolkitExecutorPool;
import com.infospica.dicom.process.batch.BatchDicomFileProcessor;
import com.infospica.dicom.process.batch.BatchDicomUploadProcessor;
import com.infospica.dicom.process.state.batch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchDicomUploaderConfig {


    @Bean(name = "batchModifyDicomCommand")
    public BatchModifyDicomCommand batchModifyDicomCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new BatchModifyDicomCommand(toolkitExecutorPool);
    }

    @Bean(name = "batchCompressFileCommand")
    public BatchCompressFileCommand batchCompressFileCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new BatchCompressFileCommand(toolkitExecutorPool);
    }

    @Bean(name = "batchUploadFileCommand")
    public BatchUploadFileCommand batchUploadFileCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new BatchUploadFileCommand(toolkitExecutorPool);
    }

    @Bean
    public BatchDicomFileProcessor batchFileProcessor(@Qualifier("_batchMoveImageState") MoveImageState moveImageState,
                                                        @Qualifier("_batchExtractJSONState") ExtractJSONState extractFromXMLState, @Qualifier("_batchModifyImageState") ModifyImageState modifyImageState,
                                                        @Qualifier("_batchCompressImageState") CompressImageState compressImageState,
                                                        @Qualifier("_batchPushImageState") PushImageState pushImageState,
                                                        @Qualifier("_batchUploadImageState") UploadImageState uploadImageState,
                                                        @Qualifier("_batchBackupState") BackupState backupState, @Qualifier("_batchProcessStopState") ProcessStopState processStopState) {
        return new BatchDicomFileProcessor(moveImageState, extractFromXMLState, modifyImageState, compressImageState, pushImageState, processStopState);
    }

    @Bean
    public BatchDicomUploadProcessor batchUploadProcessor(@Qualifier("_batchUploadImageState") UploadImageState uploadImageState,
                                                        @Qualifier("_batchBackupState") BackupState backupState,
                                                          @Qualifier("_batchUploadSuspendState") UploadSuspendState uploadSuspendState,
                                                          @Qualifier("_batchUploadStopState") UploadStopState uploadStopState) {
        return new BatchDicomUploadProcessor(uploadImageState, backupState, uploadSuspendState, uploadStopState);
    }
}
