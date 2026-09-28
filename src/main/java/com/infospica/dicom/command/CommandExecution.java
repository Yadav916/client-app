/**
 * Licensed to the Cirakas Consulting Pvt Ltd under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.cirakas.com/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.infospica.dicom.command;

import com.infospica.dicom.Constants;
import com.infospica.dicom.command.exception.CommandExecutionException;
import com.infospica.dicom.command.executor.CommandExecutorProvider;
import com.infospica.dicom.command.pool.ExecutableCommandPool;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.exception.ModificationException;
import com.infospica.dicom.util.LoggerUtility;
import org.apache.commons.exec.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 *   @author arun.vs
 */
public abstract class CommandExecution {
    private ExecutableCommandPool<CommandExecutorProvider> executorProvider;

    protected CommandExecution(ExecutableCommandPool<CommandExecutorProvider> executorProvider) {
        this.executorProvider = executorProvider;
    }

    public int executeUnstoppableCommand(CommandLine cmdLine, boolean waitFor, long timeOut, Consumer<String> consumer) throws CommandExecutionException {
        int returnValue = 0;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(stdout);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        DefaultExecutor exec = new DefaultExecutor();
        exec.setExitValue(0);
        exec.setWorkingDirectory(Context.getCommandWorkingFolder());
        exec.setStreamHandler(streamHandler);
        streamHandler.setStopTimeout(1000); //5 seconds
        streamHandler.start();
        try {
            LoggerUtility.log(CommandExecution.class, LoggerUtility.LogLevel.DEBUG,"Command ==> " + cmdLine);
            exec.execute(cmdLine, resultHandler);
        } catch(IOException io) {
            io.printStackTrace();
        }
        try {
            if(waitFor) {
                if(timeOut > 0) resultHandler.waitFor(timeOut);
                else resultHandler.waitFor(20000L); //default wait time; 20sec
                try {
                    returnValue = resultHandler.getExitValue();
                } catch(IllegalStateException is) {}
            }
            if(consumer != null) {
                consumer.accept(stdout.toString());
            }
            return returnValue;
        } catch(InterruptedException e) {
            LoggerUtility.log(CommandExecution.class, LoggerUtility.LogLevel.ERROR, e.getMessage());
        } finally {
            try {
                stdout.close();
                streamHandler.stop();
            } catch (IOException ioe) { }
        }
        throw new CommandExecutionException("Command execution error");
    }

    public int executeCommand(CommandLine cmdLine, boolean waitFor, long timeOut, Consumer<String> consumer) throws CommandExecutionException {

        int returnValue = 0;
        CommandExecutorProvider commandExecutorProvider = null;
        try {
            commandExecutorProvider = executorProvider.acquire();
            commandExecutorProvider.applyWorkingDirectory(Context.getCommandWorkingFolder());
            LoggerUtility.log(CommandExecution.class, LoggerUtility.LogLevel.DEBUG,"Command ==> " + cmdLine+
                    ", and applies to " + commandExecutorProvider.getIdentifier());
            commandExecutorProvider.executeCommand(cmdLine, timeOut);
            returnValue = commandExecutorProvider.getExitValue();
            if(consumer != null) {
                consumer.accept(commandExecutorProvider.getOutputString());
            }
            return returnValue;
        } catch(Exception io) {
            LoggerUtility.log(CommandExecution.class, LoggerUtility.LogLevel.ERROR, io.getMessage());
        } finally {
            commandExecutorProvider.release();
            try {
                executorProvider.recycle(commandExecutorProvider);
            } catch (Exception e) {
            }
        }
        throw new CommandExecutionException("Command execution error");
    }

    public int executeCommand(CommandLine cmdLine) throws CommandExecutionException {
        return executeCommand(cmdLine, true, 0, null);
    }

    public String executeCommandWithResult(CommandLine cmdLine) throws CommandExecutionException {
        CommandExecutorProvider commandExecutorProvider = null;
        try {
            commandExecutorProvider = executorProvider.acquire();
            commandExecutorProvider.applyWorkingDirectory(Context.getCommandWorkingFolder());
            LoggerUtility.log(CommandExecution.class, LoggerUtility.LogLevel.DEBUG,"Command ==> " + cmdLine +
                    ", and applies to " + commandExecutorProvider.getIdentifier());
            return commandExecutorProvider.executeCommand(cmdLine);
        } catch(Exception io) {
            LoggerUtility.log(CommandExecution.class, LoggerUtility.LogLevel.ERROR, io.getMessage());
        } finally {
            commandExecutorProvider.release();
            try {
                executorProvider.recycle(commandExecutorProvider);
            } catch (Exception e) { }
        }
        throw new CommandExecutionException("Internal execution error");
    }

    public String getExecutableCommand(String command) {
        File executableCommand = new File(Context.getCommandFolder(), command + Constants.COMMAND_EXTENSION);
        return executableCommand.getAbsolutePath();
    }

    public String storeCommandAsFile(String fileName, StringBuilder contents) throws IOException {
        File commandFile = new File(Context.getCommandWorkingFolder(), fileName + Constants.SCRIPT_COMMAND_EXTENSION);
        if(SystemUtils.IS_OS_WINDOWS) {
            contents.insert(0,"@echo off\r\n\r\n");
        } else {
            contents.insert(0,"#!/bin/bash\n");
        }
        FileUtils.writeStringToFile(commandFile, contents.toString(), "UTF-8");
        commandFile.setExecutable(true);
        return commandFile.getAbsolutePath();
    }
}
