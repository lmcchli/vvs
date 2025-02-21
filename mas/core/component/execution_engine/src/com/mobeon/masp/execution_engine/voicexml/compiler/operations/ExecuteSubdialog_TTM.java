/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.values.Pair;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.PromptQueue;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExecuteSubdialog_TTM extends VXMLOperationBase {

    private DebugInfo debugInfo;
    static ILogger logger = ILoggerFactory.getILogger(ExecuteSubdialog_TTM.class);


    public ExecuteSubdialog_TTM(DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        String src = stack.popAsString(ex);
        if (logger.isInfoEnabled()) logger.info("Subdialog to " + src + " requested");

        List<Value> namePairs = stack.popToMark();

        List<Pair> params = null;
        if (namePairs != null && namePairs.size() > 0) {
            params = new ArrayList<Pair>(namePairs.size());
            for (Value value : namePairs) {
                Pair param = (Pair) value.getValue();
                params.add(param);
            }
        }

        if (logger.isDebugEnabled()) logger.debug("Setting engine state to " + ExecutionResult.EVENT_WAIT);
        ex.setExecutionResult(ExecutionResult.EVENT_WAIT);

        List<Value> values = stack.toList();
        String maxage = getMaxage(values, ex);
        String fetchTimeout = getFetchTimeout(values, ex);
        ex.subdialog(src, debugInfo, params, PromptQueue(ex).getPlayableQueue(), maxage, fetchTimeout);
    }

    public String arguments() {
        return "";
    }

    /**
     * Retrieves maxage parameter, either from an attribute or a property
     *
     * @param values
     * @param ex
     * @return the maxage string
     */
    private String getMaxage(List<Value> values, VXMLExecutionContext ex) {
        String maxage = null;
        if (values.size() > 0) {
            maxage = values.get(0).toString();
        }
        if (maxage == null) {
            maxage = ex.getProperty(Constants.VoiceXML.DOCUMENTMAXAGE);
        }
        return maxage;
    }

    /**
     * Retrieves fetchtimeout parameter, either from an attribute or a property
     *
     * @param values
     * @param ex
     * @return the fetchtimeout string
     */
    private String getFetchTimeout(List<Value> values, VXMLExecutionContext ex) {
        String fetchTimeout = null;
        if (values.size() > 1) {
            fetchTimeout = values.get(1).toString();
        }
        if (fetchTimeout == null) {
            fetchTimeout = ex.getProperty(Constants.VoiceXML.FETCHTIMEOUT);
        }
        return fetchTimeout;
    }
}
