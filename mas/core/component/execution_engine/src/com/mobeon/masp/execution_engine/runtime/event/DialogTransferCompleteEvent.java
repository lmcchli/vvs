package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.voicexml.runtime.TransferShadowVars;

/**
 * @author David Looberger
 */
public class DialogTransferCompleteEvent implements Event {

    String name;
    TransferShadowVars results;
    String reason;
    String target;
    String targetType;


    public DialogTransferCompleteEvent(String name, TransferShadowVars results, String reason) {
        this.name = name;
        this.results = results;
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public TransferShadowVars getResults() {
        return results;
    }

    public String getReason() {
        return reason;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTarget() {
        return target;
    }

    public String getTargetType() {
        return targetType;
    }

}
