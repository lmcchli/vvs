package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.wrapper.Call;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.stream.RecordingProperties;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Dec 1, 2005
 * Time: 3:46:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class Record_P extends VXMLOperationBase {
    private static final ILogger logger = ILoggerFactory.getILogger(Record_P.class);
    private String type;

    public Record_P(String type) {
        this.type = type;
    }

    public String arguments() {
        return classToMnemonic(getClass());
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ex.getEventProcessor().setEnabled(true); // events were disabled in operations running just before this one

        IMediaObjectFactory factory = ex.getMediaObjectFactory();
        Call call = ex.getCall();
        if (factory == null || call == null) {
            badfetch(ex, "IMediaObjectFactory or Stream was null");
            return;
        }

        IMediaObject mediaObject = factory.create();
        ValueStack valueStack = ex.getValueStack();
        String finalSilence = valueStack.pop().toString();
        String maxtime = valueStack.pop().toString();
        Long mtime = null;
        if (maxtime != null) {
            mtime = Tools.parseCSS2Time(maxtime);
        }
        if (mtime == null) { // TODO: use system parameter value
            if (logger.isDebugEnabled()) logger.debug("Using fallback value for maxtime : 60s ");
            mtime = 1000*60L;
        } 
        
        
        Long finalsilenceValue = null;
        boolean isFinalSilence = false;
        if (finalSilence != null) {
            finalsilenceValue = Tools.parseCSS2Time(finalSilence);
        }
        if (finalsilenceValue==null) {
            if (logger.isDebugEnabled()) logger.debug("Stream will use default value of finalSilence");
        }
        else {
             isFinalSilence = true;
        }

        String timeout=ex.getProperties().getProperty("timeout");
        Long timeoutValue = null;
        boolean isTimeout = false;
        if ( timeout != null) {
           timeoutValue=Tools.parseCSS2Time(timeout);
        }

        if (timeoutValue==null) {
        } else {
          isTimeout = true;
        }
       
        // Verify that the record shall start, and is not inhibited by e.g. DTMF during prompting
        final FIAState state = ex.getFIAState();
        if (state.inhibitRecording()) {
            ex.setExecutionResult(ExecutionResult.DEFAULT);
            ex.getFIAState().setInhibitRecording(false);
            if (logger.isDebugEnabled()) logger.debug("Recording inhibited");
            return;
        }

        RecordingProperties properties = new RecordingProperties();
        // TODO: Use the tag attribute(s)

        if (logger.isDebugEnabled()) logger.debug("Setting maxtime to " + mtime + " ms");
        properties.setMaxRecordingDuration((int)(long)mtime);
        if (isFinalSilence){
            properties.setMaxSilence((int)(long)finalsilenceValue);
            logger.debug("Setting finalSilence to " + finalsilenceValue + " ms");
        }

        if(isTimeout) {
           properties.setTimeout((int)(long)timeoutValue);
           logger.debug("Setting timeout to" + timeoutValue + " ms");
        }

        properties.setWaitForRecordToFinish(false);
        properties.setRecordingType(findRecordingType(ex, type));
        

        valueStack.pushScriptValue(mediaObject);
        state.markRecordStart("5s");
        TestEventGenerator.generateEvent(TestEvent.RECORD_STARTING);
        ex.getCurrentConnection().record();
        TestEventGenerator.generateEvent(TestEvent.RECORD_STARTED,ex);
        if (! call.record(mediaObject, properties)) {
            ex.getCurrentConnection().stopRecording();
        } else {
            ex.waitForEvents();
        }
    }

    private RecordingProperties.RecordingType findRecordingType(VXMLExecutionContext ex, String type) {
        if (type != null) {
            return stringToRecordingType(type);
        } else {
            String typeProperty = ex.getProperty(Constants.PlatformProperties.PLATFORM_RECORD_TYPE);
            return stringToRecordingType(typeProperty);
        }
    }

    private RecordingProperties.RecordingType stringToRecordingType(String type) {
        if (type == null) {
            return RecordingProperties.RecordingType.UNKNOWN;
        } else if (type.equals("audio/*")) {
            return RecordingProperties.RecordingType.AUDIO;
        } else if (type.equals("video/*")) {
            return RecordingProperties.RecordingType.VIDEO;
        } else {
            return RecordingProperties.RecordingType.UNKNOWN;
        }
    }

    /**
     * @param context
     * @param reason
     * @logs.error "IMediaObjectFactory or Stream was null" - required data structures were unexpectedly null.
     */
    private void badfetch(ExecutionContext context,
                          String reason) {
        logger.error(reason);
        context.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH,
                reason, DebugInfo.getInstance());
    }
}
