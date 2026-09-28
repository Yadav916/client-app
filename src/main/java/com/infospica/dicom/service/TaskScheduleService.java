package com.infospica.dicom.service;

import com.infospica.dicom.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class TaskScheduleService {

    private final TaskScheduler taskScheduler;
    private Map<Integer, ScheduledFuture<?>> tasksMap = new HashMap<>();

    @Autowired
    public TaskScheduleService(final TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void addTaskToScheduler(int id, Runnable task, Date runningDate) {
        try {
            if (!tasksMap.containsKey(id)) {
                ScheduledFuture<?> scheduledTask = taskScheduler.schedule(task, runningDate);
                tasksMap.put(id, scheduledTask);
            }
        } catch(RuntimeException e) {
            LoggerUtility.log(TaskScheduleService.class, "RuntimeException in addTaskToScheduler: ", e);
        } catch(Exception e) {
            LoggerUtility.log(TaskScheduleService.class, "Exception in addTaskToScheduler: ", e);
        }
    }

    public void addTaskToScheduler(int id, Runnable task, long fixedDelay) {
        try {
            if (!tasksMap.containsKey(id)) {
                ScheduledFuture<?> scheduledTask = taskScheduler.scheduleWithFixedDelay(task, fixedDelay);
                tasksMap.put(id, scheduledTask);
            }
        } catch(RuntimeException e) {
            LoggerUtility.log(TaskScheduleService.class, "RuntimeException in addTaskToScheduler: ", e);
        } catch(Exception e) {
            LoggerUtility.log(TaskScheduleService.class, "Exception in addTaskToScheduler: ", e);
        }
    }

    public void addTaskToScheduler(int id, Runnable task, Duration duration) {
        try {
            if (!tasksMap.containsKey(id)) {
                ScheduledFuture<?> scheduledTask = taskScheduler.scheduleWithFixedDelay(task, duration);
                tasksMap.put(id, scheduledTask);
            }
        } catch(RuntimeException e) {
            LoggerUtility.log(TaskScheduleService.class, "RuntimeException in addTaskToScheduler: ", e);
        } catch(Exception e) {
            LoggerUtility.log(TaskScheduleService.class, "Exception in addTaskToScheduler: ", e);
        }
    }

    public void addTaskToScheduler(int id, Runnable task, CronTrigger cronTrigger) {
        try {
            if (!tasksMap.containsKey(id)) {
                ScheduledFuture<?> scheduledTask = taskScheduler.schedule(task, cronTrigger);
                tasksMap.put(id, scheduledTask);
            }
        } catch(RuntimeException e) {
            LoggerUtility.log(TaskScheduleService.class, "RuntimeException in addTaskToScheduler: ", e);
        } catch(Exception e) {
            LoggerUtility.log(TaskScheduleService.class, "Exception in addTaskToScheduler: ", e);
        }
    }

    /* Cancel and remove the job on demand */
    public void removeTaskFromScheduler(int id) {
        removeTaskFromScheduler(id, true);
    }

    public void removeTaskFromScheduler(int id, boolean interrupt) {
        try {
            ScheduledFuture<?> scheduledTask = tasksMap.get(id);
            if (scheduledTask != null) {
                if (!scheduledTask.isCancelled()) {
                    scheduledTask.cancel(interrupt);
                }
                ScheduledFuture<?> removedSF = tasksMap.remove(id);
                while (!removedSF.isDone()) ;
                LoggerUtility.log(TaskScheduleService.class, LoggerUtility.LogLevel.INFO, "Task complete for id: " + id + " " + removedSF.isDone());
            }
        } catch(RuntimeException e) {
            LoggerUtility.log(TaskScheduleService.class, "RuntimeException in addTaskToScheduler: ", e);
        } catch(Exception e) {
            LoggerUtility.log(TaskScheduleService.class, "Exception in removeTaskFromScheduler: ", e);
        }
    }
    public void completeAndRemoveTaskFromScheduler(int id, boolean cancel) {
        try {
            ScheduledFuture<?> scheduledTask = tasksMap.get(id);
            if (scheduledTask != null) {
                ScheduledFuture<?> removedSF = tasksMap.remove(id);
                if (cancel) removedSF.cancel(true);
                try {
                    removedSF.get();
                } catch (ExecutionException | InterruptedException | CancellationException e) {

                }
                LoggerUtility.log(TaskScheduleService.class, LoggerUtility.LogLevel.INFO, "Completed and removed the task for id: " + id + " " + removedSF.isDone());
            }
        } catch(RuntimeException e) {
            LoggerUtility.log(TaskScheduleService.class, "RuntimeException in addTaskToScheduler: ", e);
        } catch(Exception e) {
            LoggerUtility.log(TaskScheduleService.class, "Exception in completeAndRemoveTaskFromScheduler: ", e);
        }
    }
}
