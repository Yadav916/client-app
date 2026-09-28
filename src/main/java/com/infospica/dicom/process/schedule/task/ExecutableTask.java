package com.infospica.dicom.process.schedule.task;

import reactor.core.publisher.Mono;

import java.io.File;

public interface ExecutableTask<T> {
    Mono<T> doProcess(File srcFile) throws Exception;
    T doProcess(File srcFile, boolean flag) throws Exception;
    void resetCommand();
}
