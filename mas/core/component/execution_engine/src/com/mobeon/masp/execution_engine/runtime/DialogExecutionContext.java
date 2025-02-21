/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.voicexml.runtime.TransferState;

public interface DialogExecutionContext extends ExecutionContext{
    public void setDialog(Dialog dialog);
    public Dialog getDialog();
    public void shutdown(boolean recursive);
    boolean isAlive();
    TransferState getTransferState();
}
