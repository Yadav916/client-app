package com.infospica.dicom.process.schedule.task;

import com.infospica.dicom.pool.AbstractPool;

public abstract class ExecutableTaskPool<T> extends AbstractPool<T> {
    protected ExecutableTaskPool() {
        super(10);
    }
}
