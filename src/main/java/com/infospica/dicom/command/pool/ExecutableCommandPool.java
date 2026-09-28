package com.infospica.dicom.command.pool;

import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.util.LoggerUtility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ExecutableCommandPool<T> {
    private final BlockingQueue<T> pool;
    private final ReentrantLock lock = new ReentrantLock();
    private int commandCount = 0;
    private final int size = 7;

    protected ExecutableCommandPool() {
        this.pool = new LinkedBlockingQueue<>(size);
        lock.lock();
    }

    public void createPool() {
        if (lock.isLocked()) {
            for (int i = 0; i < size; ++i) {
                pool.add(createCommand());
                commandCount++;
            }
        }
    }

    public T acquire() throws Exception {
        if (!lock.isLocked()) {
            if (lock.tryLock()) {
                try {
                    LoggerUtility.log(ExecutableCommandPool.class, LoggerUtility.LogLevel.INFO, "Acquire method: creating new command.");
                    ++commandCount;
                    return createCommand();
                } finally {
                    if (commandCount < size) lock.unlock();
                }
            }
        }
        return pool.take();
    }

    public void recycle(T command) throws Exception {
        pool.add(command);
    }

    protected void removeAll() {
        pool.clear();
        commandCount = 0;
    }
    protected abstract T createCommand();
}
