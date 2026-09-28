package com.infospica.dicom.context;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.util.Date;

@Data
@NoArgsConstructor
public class ExecutionContext {
    private boolean terminate = false;
    private boolean uploadServiceStarted = false;
    private boolean backupCleanProcessScheduled = false;
    private boolean backupCleanStarted = false;

    @Setter(AccessLevel.NONE)
    private ProcessControl processControl = new ProcessControl();

    @Data
    protected static class ProcessControl {
        private ProcessControl() {
            super();
        }
        private Date lastProcessDone;
        private long processIdle;
    }
}
