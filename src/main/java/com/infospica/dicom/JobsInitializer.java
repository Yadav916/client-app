package com.infospica.dicom;

import com.infospica.dicom.command.handler.StreamHandlerExecutor;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.context.ExecutionContext;
import com.infospica.dicom.context.FolderContext;
import com.infospica.dicom.process.DicomUploadProcessor;
import com.infospica.dicom.process.analytics.AnalyticalContext;
import com.infospica.dicom.process.analytics.monitor.AnalyticsUpdater;
import com.infospica.dicom.process.batch.BatchDicomFileProcessor;
import com.infospica.dicom.process.batch.BatchDicomUploadProcessor;
import com.infospica.dicom.process.schedule.DicomProcessScheduler;
import com.infospica.dicom.service.TaskScheduleService;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@DependsOn({"dicomUploaderConfig"})
public class JobsInitializer implements DisposableBean {

    private final TaskScheduleService scheduleService;

    private final DicomUploadProcessor processor;
    private final BatchDicomFileProcessor batchFileProcessor;
    private final BatchDicomUploadProcessor batchUploadProcessor;

    @Autowired
    public JobsInitializer(final TaskScheduleService scheduleService,
                           final DicomUploadProcessor processor, final DicomProcessScheduler processScheduler,
                           final BatchDicomFileProcessor batchFileProcessor, final BatchDicomUploadProcessor batchUploadProcessor) {
        this.scheduleService = scheduleService;
        this.processor = processor;
        this.batchFileProcessor = batchFileProcessor;
        this.batchUploadProcessor = batchUploadProcessor;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    public void startScheduleService() {
        Context.setFolderContext(new FolderContext());
        Context.setAnalyticalContext(new AnalyticalContext());
        Context.setExecutionContext(new ExecutionContext());

        if(!Context.isConfigurationRequired()) {
            //start the server
            if(Context.canStartSCPService()) {
                startStoreSCP();
            }

            if (!Context.isManualStart() && Context.canStartUploadService()) {
                startUploadService();
            }

            startBackupCleanService();
            cleanBackupOnStartup();
        }

        //enable the notifiers
        this.scheduleService.addTaskToScheduler(3, () -> {
            AnalyticsUpdater.getInstance().notifyAllMonitors();
        }, Duration.of(2, ChronoUnit.SECONDS));
    }

    public void startUploadService() {
        if(Context.isBatchPreference()) {
            this.scheduleService.addTaskToScheduler(454, batchFileProcessor.start(), new Date());
            this.scheduleService.addTaskToScheduler(457, batchUploadProcessor.start(), new Date());

            /*if(Context.isAnonymizeEnabled()) {
                this.scheduleService.addTaskToScheduler(122, batchFileProcessor.monitorAnonymizedFolder(), Duration.of(200, ChronoUnit.MILLIS));
            }
            if(Context.isCompressionEnabled()) {
                this.scheduleService.addTaskToScheduler(123, batchFileProcessor.monitorCompressedFolder(), Duration.of(200, ChronoUnit.MILLIS));
            }*/

            //this.scheduleService.addTaskToScheduler(125, batchProcessor.monitorUploadProcess(), Duration.of(200, ChronoUnit.MILLIS));
            this.scheduleService.addTaskToScheduler(2, batchFileProcessor.processFiles(), Duration.of(5, ChronoUnit.SECONDS));
            this.scheduleService.addTaskToScheduler(4, batchUploadProcessor.uploadFiles(), Duration.of(5, ChronoUnit.SECONDS));
            //this.scheduleService.addTaskToScheduler(5, batchUploadProcessor.monitorAndResume(), Duration.of(1, ChronoUnit.SECONDS));
        }
        if (Context.isPingEnabled()) {
            int interval = Context.getPingInterval();
            this.scheduleService.addTaskToScheduler(56, processor.pingRemoteServer(), Duration.of(interval, ChronoUnit.SECONDS));
        } else {
            LoggerUtility.log(JobsInitializer.class, LoggerUtility.LogLevel.INFO, "Server ping is disabled.");
            this.scheduleService.addTaskToScheduler(56, processor.pingRemoteServer(), new Date());
            this.scheduleService.completeAndRemoveTaskFromScheduler(56, false); //complete and stop
        }
        //monitor pending files count
        this.scheduleService.addTaskToScheduler(1100, batchFileProcessor.monitorFileCount(), Duration.of(200, ChronoUnit.MILLIS));
        Context.setUploadServiceStarted(true);
    }

    public void stopUploadService() {
        if(Context.isBatchPreference()) {
            this.scheduleService.addTaskToScheduler(455, batchFileProcessor.stop(), new Date());
            this.scheduleService.addTaskToScheduler(456, batchUploadProcessor.stop(), new Date());
            this.scheduleService.completeAndRemoveTaskFromScheduler(455, false); //complete the stop
            this.scheduleService.completeAndRemoveTaskFromScheduler(456, false); //complete the stop

            /*this.scheduleService.removeTaskFromScheduler(122);
            this.scheduleService.removeTaskFromScheduler(123);*/

            this.scheduleService.removeTaskFromScheduler(125);
            this.scheduleService.completeAndRemoveTaskFromScheduler(2, true); //complete and stop
            this.scheduleService.completeAndRemoveTaskFromScheduler(4, true); //complete and stop
            //this.scheduleService.completeAndRemoveTaskFromScheduler(5, true); //complete and stop
            this.scheduleService.removeTaskFromScheduler(454);
            this.scheduleService.removeTaskFromScheduler(457);
            this.scheduleService.removeTaskFromScheduler(56);
        }
        this.scheduleService.removeTaskFromScheduler(1100);

        Context.updateSCUEchoStatus(false);
        Context.setUploadServiceStarted(false);
    }

    public void startStoreSCP() {
        this.scheduleService.addTaskToScheduler(1, processor.startServer(), new Date());
        this.scheduleService.completeAndRemoveTaskFromScheduler(1, false);
        if (Context.isPingEnabled()) {
            int interval = Context.getPingInterval();
            this.scheduleService.addTaskToScheduler(55, processor.pingSCPServer(), Duration.of(interval, ChronoUnit.SECONDS));
        } else {
            //new SingleTimeMonitorUtility<Boolean>(processor.getScpService()).doMonitor().withMonitorUntil(true, 6000);
            this.scheduleService.addTaskToScheduler(55, processor.pingSCPServer(), new Date());
            this.scheduleService.completeAndRemoveTaskFromScheduler(55, false); //complete and stop
        }
    }

    public void stopStoreSCP() {
        this.scheduleService.addTaskToScheduler(2222, processor.stopServer(), new Date());
        this.scheduleService.completeAndRemoveTaskFromScheduler(2222, false);
        this.scheduleService.removeTaskFromScheduler(55);
        this.scheduleService.removeTaskFromScheduler(1);
        Context.updateSCPEchoStatus(false);
    }

    public void startBackupCleanService() {
        LoggerUtility.log(JobsInitializer.class, LoggerUtility.LogLevel.INFO, "Inside backup clean process service start.");
        if(Context.isBackupCleanProcessScheduled()) {
            LoggerUtility.log(JobsInitializer.class, LoggerUtility.LogLevel.INFO, "Backup clean scheduled already, stopping...");
            stopBackupCleanService();
        }
        if(Context.shallCleanBackup()) {
            //String cronExpression = "0 */" + Context.getStoreSCUConfigurationProperties().getImageCleanupMinute() + " * * * MON-SUN";
            //LoggerUtility.log(JobsInitializer.class, LoggerUtility.LogLevel.INFO, "Considering the expression: " + cronExpression);
            //this.scheduleService.addTaskToScheduler(58, processor.cleanBackup(), new CronTrigger(cronExpression));
            int delayMillis = Context.getStoreSCUConfigurationProperties().getImageCleanupMinute() * 60 * 1000;
            this.scheduleService.addTaskToScheduler(58, processor.cleanBackup(), delayMillis);
            Context.setBackupCleanProcessScheduled(true);
        }
        cleanBackupOnStartup();
    }

    public void cleanBackupOnStartup() {
        if(Context.shallCleanImmediately()) {
            this.scheduleService.addTaskToScheduler(589, processor.cleanAllBackup(), new Date());
        }
    }

    public void stopBackupCleanService() {
        this.scheduleService.removeTaskFromScheduler(58);
        this.scheduleService.removeTaskFromScheduler(589);
        Context.setBackupCleanProcessScheduled(false);
    }

    public void releaseResources() {
        StreamHandlerExecutor.shutdown();
    }

    @Override
    public void destroy() throws Exception {
        stopStoreSCP();
        stopBackupCleanService();
        stopUploadService();
        releaseResources();
        LoggerUtility.log(JobsInitializer.class, LoggerUtility.LogLevel.INFO, "Terminated all services.");
    }
}
