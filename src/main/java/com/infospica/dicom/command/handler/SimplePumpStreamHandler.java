package com.infospica.dicom.command.handler;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.InputStreamPumper;
import org.apache.commons.exec.util.DebugUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;

public class SimplePumpStreamHandler implements ExecuteStreamHandler {
    private static final long STOP_TIMEOUT_ADDITION = 2000L;
    private Runnable outputRunnable;
    private Runnable inputRunnable;
    private Runnable errorRunnable;

    private final OutputStream out;
    private final OutputStream err;
    private final InputStream input;
    private InputStreamPumper inputStreamPumper;
    private long stopTimeout;
    private IOException caught;

    public SimplePumpStreamHandler() {
        this(System.out, System.err);
    }

    public SimplePumpStreamHandler(OutputStream outAndErr) {
        this(outAndErr, outAndErr);
    }

    public SimplePumpStreamHandler(OutputStream out, OutputStream err) {
        this(out, err, (InputStream)null);
    }

    public SimplePumpStreamHandler(OutputStream out, OutputStream err, InputStream input) {
        this.caught = null;
        this.out = out;
        this.err = err;
        this.input = input;
    }

    public void setStopTimeout(long timeout) {
        this.stopTimeout = timeout;
    }

    public void setProcessOutputStream(InputStream is) {
        if (this.out != null) {
            this.createProcessOutputPump(is, this.out);
        }

    }

    public void setProcessErrorStream(InputStream is) {
        if (this.err != null) {
            this.createProcessErrorPump(is, this.err);
        }

    }

    public void setProcessInputStream(OutputStream os) {
        if (this.input != null) {
            if (this.input == System.in) {
                this.inputRunnable = this.createSystemInPump(this.input, os);
            } else {
                this.inputRunnable = this.createPump(this.input, os, true);
            }
        } else {
            try {
                os.close();
            } catch (IOException var4) {
                String msg = "Got exception while closing output stream";
                DebugUtils.handleException("Got exception while closing output stream", var4);
            }
        }

    }

    public void start() {
        if (this.outputRunnable != null) {
            StreamHandlerExecutor.executeTask(this.outputRunnable);
        }

        if (this.errorRunnable != null) {
            StreamHandlerExecutor.executeTask(this.errorRunnable);
        }

        if (this.inputRunnable != null) {
            StreamHandlerExecutor.executeTask(this.inputRunnable);
        }

    }

    public void stop() throws IOException {
        if (this.inputStreamPumper != null) {
            this.inputStreamPumper.stopProcessing();
        }

        String msg;
        if (this.err != null && this.err != this.out) {
            try {
                this.err.flush();
            } catch (IOException var4) {
                msg = "Got exception while flushing the error stream : " + var4.getMessage();
                DebugUtils.handleException(msg, var4);
            }
        }

        if (this.out != null) {
            try {
                this.out.flush();
            } catch (IOException var3) {
                msg = "Got exception while flushing the output stream";
                DebugUtils.handleException("Got exception while flushing the output stream", var3);
            }
        }

        if (this.caught != null) {
            throw this.caught;
        }
    }

    protected OutputStream getErr() {
        return this.err;
    }

    protected OutputStream getOut() {
        return this.out;
    }

    protected void createProcessOutputPump(InputStream is, OutputStream os) {
        this.outputRunnable = this.createPump(is, os);
    }

    protected void createProcessErrorPump(InputStream is, OutputStream os) {
        this.errorRunnable = this.createPump(is, os);
    }

    protected Runnable createPump(InputStream is, OutputStream os) {
        boolean closeWhenExhausted = os instanceof PipedOutputStream;
        return this.createPump(is, os, closeWhenExhausted);
    }

    protected Runnable createPump(InputStream is, OutputStream os, boolean closeWhenExhausted) {
        return new StreamPumper(is, os, closeWhenExhausted);
    }

    private Runnable createSystemInPump(InputStream is, OutputStream os) {
        this.inputStreamPumper = new InputStreamPumper(is, os);
        return this.inputStreamPumper;
    }
}
