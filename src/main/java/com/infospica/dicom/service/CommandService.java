package com.infospica.dicom.service;

import com.infospica.dicom.config.iface.ResetableCommand;
import com.infospica.dicom.process.schedule.task.ExecutableTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommandService {

    private final List<ResetableCommand> resettableCommands;
    private final List<ExecutableTask> executableTasks;


    @Autowired
    public CommandService(final List<ResetableCommand> resettableCommands, final List<ExecutableTask> executableTasks) {
        this.resettableCommands = resettableCommands;
        this.executableTasks = executableTasks;
    }

    public void resetAll() {
        if(this.resettableCommands != null) {
            this.resettableCommands.forEach(ResetableCommand::reset);
        }

        if(this.executableTasks != null) {
            this.executableTasks.forEach(ExecutableTask::resetCommand);
        }
    }
}
