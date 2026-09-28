package com.infospica.dicom.process;

import com.infospica.dicom.command.pool.CompressDicomCommandPool;
import com.infospica.dicom.command.pool.ModifyDicomCommandPool;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class PreparationTask {

    @Autowired
    private ApplicationContext applicationContext;
    private ModifyDicomCommandPool modifyDicomCommandPool;
    private CompressDicomCommandPool compressDicomCommandPool;

    public Mono<File> updateNameAndAddress(File srcFile) {
        try {
            String modifiedFile = this.modifyDicomCommandPool.execute(srcFile);
            LoggerUtility.log(PreparationTask.class, LoggerUtility.LogLevel.DEBUG, modifiedFile + "[Modified]");
            Context.updateModifiedImage();
            return Mono.just(new File(modifiedFile));
        } catch (Exception e) {
            LoggerUtility.log(PreparationTask.class,  LoggerUtility.LogLevel.ERROR, "Error during update: Cause" + e.getMessage());
        }
        return Mono.just(new File(srcFile.getParent(), srcFile.getName().concat("#ERROR")));
    }

    public Mono<File> compressFile(File srcFile) {
        try {
            //compressFile = ((ExecutableCommand)applicationContext.getBean("compressDicomCommand")).execute(srcFile.getAbsolutePath());
            String compressFile = compressDicomCommandPool.execute(srcFile);
            LoggerUtility.log(PreparationTask.class, LoggerUtility.LogLevel.DEBUG, compressFile + "[Compressed]");
            Context.updateCompressedImage();
            return Mono.just(new File(compressFile));
        } catch (Exception e) {
            LoggerUtility.log(PreparationTask.class,  LoggerUtility.LogLevel.ERROR, "Error during compressing the image: Cause" + e.getMessage());
        }
        return Mono.just(new File(srcFile.getParent(), srcFile.getName().concat("#ERROR")));
    }

    @PostConstruct
    public void initCommandPool() {
        this.modifyDicomCommandPool = new ModifyDicomCommandPool(applicationContext);
        this.compressDicomCommandPool = new CompressDicomCommandPool(applicationContext);
    }
}
