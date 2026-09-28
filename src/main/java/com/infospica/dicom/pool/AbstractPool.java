package com.infospica.dicom.pool;

import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.util.LoggerUtility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractPool<T> {
    private BlockingQueue<T> pool;
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;
    protected int size = 0;

    public AbstractPool() {
        this(10);
    }
    public AbstractPool(int size) {
        this.size = size;
        this.pool = new LinkedBlockingQueue<>(size);
        lock.lock();
    }

    public void createPool() {
        if (lock.isLocked()) {
            for (int i = 0; i < size; ++i) {
                pool.add(create());
                count++;
            }
        }
    }

    public T acquire() throws Exception {
        if (!lock.isLocked()) {
            if (lock.tryLock()) {
                try {
                    LoggerUtility.log(AbstractPool.class, LoggerUtility.LogLevel.INFO, "Acquire method: creating new instance.");
                    ++count;
                    return create();
                } finally {
                    if (count < size) lock.unlock();
                }
            }
        }
        return pool.take();
    }

    public void recycle(T t) throws Exception {
        pool.add(t);
    }

    protected abstract T create();
}
