package com.infospica.dicom.command.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class StreamHandlerExecutor {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(7, new StreamHandlerThreadFactory());

    private StreamHandlerExecutor() { super(); }

    public static void executeTask(Runnable task) {
        executorService.execute(() -> {
            try {
                // Execute the task
                task.run();
            } finally {
                // Release the thread back to the global pool
            }
        });
    }

    public static void shutdown() {
        if(!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private static class StreamHandlerThreadFactory implements ThreadFactory {
        private final String threadNamePrefix = "Thread-StreamHandler";

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName(threadNamePrefix + "-" + thread.getId());
            return thread;
        }
    }
}
