package com.infospica.dicom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infospica.dicom.Constants;
import com.infospica.dicom.command.EchoServerCommand;
import com.infospica.dicom.command.*;
import com.infospica.dicom.command.pool.ToolkitExecutorPool;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.process.*;
import com.infospica.dicom.process.schedule.DefaultScheduleProcessManager;
import com.infospica.dicom.process.schedule.DicomProcessScheduler;
import com.infospica.dicom.process.schedule.ScheduleProcessManager;
import com.infospica.dicom.process.schedule.task.CompressTaskPool;
import com.infospica.dicom.process.state.*;
import com.infospica.dicom.service.SCPService;
import com.infospica.dicom.service.SCUService;
import com.infospica.dicom.util.ContentEncoderDecoderUtility;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Configuration
@ComponentScan("com.infospica.dicom.config")
public class DicomUploaderConfig {

    @Autowired
    private Environment environment;

    @Bean(name = "scpStartCommand")
    public SCPStartCommand scpStartCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new SCPStartCommand(toolkitExecutorPool);
    }

    @Bean(name = "scpStopCommand")
    public SCPStopCommand scpStopCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new SCPStopCommand(toolkitExecutorPool);
    }

    @Bean(name = "moveFileCommand")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MoveFileCommand moveFileCommand() {
        return new MoveFileCommand();
    }

    @Bean(name = "scuEchoCommand")
    public EchoServerCommand echoServerCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new EchoServerCommand(toolkitExecutorPool);
    }

    @Bean(name = "scpEchoCommand")
    public EchoSCPCommand scpEchoServerCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new EchoSCPCommand(toolkitExecutorPool);
    }

    @Bean(name = Constants.MODIFY_COMMAND_BEAN_NAME)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ModifyDicomCommand modifyDicomCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new ModifyDicomCommand(toolkitExecutorPool);
    }

    @Bean(name = Constants.COMPRESS_COMMAND_BEAN_NAME)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CompressFileCommand compressDicomCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new CompressFileCommand(toolkitExecutorPool);
    }

    @Bean(name = Constants.EXTRACT_COMMAND_BEAN_NAME)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ExtractDicomCommand extractDicomCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new ExtractDicomCommand(toolkitExecutorPool);
    }

    @Bean(name = Constants.UPLOAD_COMMAND_BEAN_NAME)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public UploadFileCommand uploadDicomCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new UploadFileCommand(toolkitExecutorPool);
    }

    @Bean(name = Constants.SIMPLE_UPLOAD_COMMAND_BEAN_NAME)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SimpleUploadFileCommand simpleUploadDicomCommand(@Autowired ToolkitExecutorPool toolkitExecutorPool) {
        return new SimpleUploadFileCommand(toolkitExecutorPool);
    }

    @Bean
    public DicomUploadProcessor uploadProcessor(SCPService scpService, SCUService scuService, MoveImageState moveImageState,
                                                ExtractJSONState extractFromXMLState, ModifyImageState modifyImageState,
                                                CompressImageState compressImageState, UploadImageState uploadImageState,
                                                BackupState backupState) {
        return new DicomUploadProcessor(scpService, scuService, moveImageState, extractFromXMLState, modifyImageState, compressImageState, uploadImageState, backupState);
    }

    @Bean(destroyMethod = "shutdownGracefully")
    public DefaultScheduleProcessManager scheduleProcessManager(final CompressTaskPool compressTaskPool, final MoveTask moveTask, final PreparationTask preparationTask, final ExtractTask extractTask, final UploadTask uploadTask) {
        //return new DefaultScheduleProcessManager(preparationTask, extractTask, uploadTask);
        return new DefaultScheduleProcessManager(compressTaskPool, moveTask, preparationTask, extractTask, uploadTask);
    }

    @Bean
    public DicomProcessScheduler dicomProcessScheduler(ScheduleProcessManager processManager) {
        return new DicomProcessScheduler(processManager);
    }

    @Bean
    ExitCodeExceptionMapper exitCodeExceptionMapper() {
        return exception ->  {
            if(exception.getCause() instanceof RuntimeException) {
                return 999;
            }
            return 1;
        };
    }

    @Bean(destroyMethod = "terminate")
    TerminateBean terminateBean() {
        return new TerminateBean();
    }

    private StartupConfigurationProperties startupConfig() throws Exception {
        File startupConfigFile = new File(environment.getProperty("loader.path"), "startup_props.conf");
        String startupConfigFileContent = FileUtils.readFileToString(startupConfigFile, "utf-8");
        String decodedContent = ContentEncoderDecoderUtility.getDecryptedContent(startupConfigFileContent);
        ObjectMapper mapper = new ObjectMapper();
        StartupConfigurationProperties configurationProperties = mapper.readValue(decodedContent, StartupConfigurationProperties.class);
        configurationProperties.setFilePath(startupConfigFile);

        try {
            String systemLogFolder = environment.getProperty("log.folder");
            if (systemLogFolder == null || systemLogFolder.trim().isEmpty()) {
                systemLogFolder = System.getProperty("java.io.tmpdir");
            }
            configurationProperties.setSystemLogFolder(new File(systemLogFolder).getCanonicalFile());
        } catch(IOException ioe) {
            throw new RuntimeException("Unable to proceed without setting the log folder");
        }
        return configurationProperties;
    }

    private StoreSCPConfigurationProperties scpConfig() throws Exception {
        File scpConfigFile = new File(environment.getProperty("loader.path"), "store_scp_props.conf");
        String scpConfigFileContent = FileUtils.readFileToString(scpConfigFile, "utf-8");
        String decodedContent = ContentEncoderDecoderUtility.getDecryptedContent(scpConfigFileContent);
        ObjectMapper mapper = new ObjectMapper();
        StoreSCPConfigurationProperties configurationProperties = mapper.readValue(decodedContent, StoreSCPConfigurationProperties.class);
        configurationProperties.setFilePath(scpConfigFile);
        configurationProperties.setCommandWorkingFolder(environment.getProperty("work.dir"));
        return configurationProperties;
    }

    private StoreSCUConfigurationProperties scuConfig() throws Exception {
        File scuConfigFile = new File(environment.getProperty("loader.path"), "store_scu_props.conf");
        String scuConfigFileContent = FileUtils.readFileToString(scuConfigFile, "utf-8");
        String decodedContent = ContentEncoderDecoderUtility.getDecryptedContent(scuConfigFileContent);
        ObjectMapper mapper = new ObjectMapper();
        StoreSCUConfigurationProperties configurationProperties = mapper.readValue(decodedContent, StoreSCUConfigurationProperties.class);
        configurationProperties.setFilePath(scuConfigFile);
        return configurationProperties;
    }

    @PostConstruct
    protected void loadConfigurationProperties() {
        try {
            Context.setStartupConfigurationProperties(startupConfig());
            Context.setStoreSCPConfigurationProperties(scpConfig());
            Context.setStoreSCUConfigurationProperties(scuConfig());

            //serialize the default configuration and save it for the first time

        } catch (Exception ioe) {
            LoggerUtility.log(DicomUploaderConfig.class, "Error while loading configuration properties", ioe);
            throw new RuntimeException("Unable to proceed without loading the configuration properties");
        }
    }
}
