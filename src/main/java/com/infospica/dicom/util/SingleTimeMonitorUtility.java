package com.infospica.dicom.util;

import com.infospica.dicom.service.Monitorable;
import org.apache.commons.exec.ExecuteException;

public class SingleTimeMonitorUtility<T> {

    private final Monitorable<T> monitorable;
    private Thread monitorThread;
    private boolean finished = false;
    private T value = null;
    private MonitorTask monitorTask;

    public SingleTimeMonitorUtility(final Monitorable monitorable) {
        this.monitorable = monitorable;
    }

    public SingleTimeMonitorUtility doMonitor() {
        this.monitorTask = new MonitorTask();
        monitorThread = new Thread(this.monitorTask);
        monitorThread.setDaemon(true);
        monitorThread.start();
        return this;
    }

    public void withMonitorUntil(T value, int timeout) {
        this.value = value;
        stopMonitor(timeout);
    }

    private void stopMonitor(int timeout) {
        if(monitorThread != null) {
            try {
                long timeToWait = timeout + 1000L; //add 1 more second
                long startTime = System.currentTimeMillis();
                monitorThread.join(timeToWait);
                if (System.currentTimeMillis() >= startTime + timeToWait) {
                    LoggerUtility.log(SingleTimeMonitorUtility.class, LoggerUtility.LogLevel.DEBUG, "Timeout happened.");
                    this.monitorTask.stop();
                }
            } catch (InterruptedException ie) {
                monitorThread.interrupt();
            }
        }
    }

    protected class MonitorTask implements Runnable {

        private boolean finished = false;
        private boolean stop = false;

        @Override
        public void run() {
            synchronized(this) {
                this.finished = false;
            }
            while(!this.stop) {
                T t = monitorable.isRunning();
                if(t instanceof Boolean) {
                    if(((Boolean) t).booleanValue() != ((Boolean) value).booleanValue()) {
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException e) { }
                        continue;
                    }
                }
                synchronized(this) {
                    this.finished = true;
                    this.notifyAll();
                    break;
                }
            }
        }

        protected void stop() {
            this.stop = true;
        }
    }
}
