/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.ShadowVarBase;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.stream.RecordFinishedEvent;

/**
 * @author David Looberger
 */
public class AssignRecordShadowvars_T extends VXMLOperationBase {
    private static final ILogger logger = ILoggerFactory.getILogger(AssignRecordShadowvars_T.class);

    public AssignRecordShadowvars_T() {
        super();
    }

    /**
     * @logs.error "Failed to retrieve old shadow variable" - The old shadow variable could not be retrieved.
     * @param ex
     * @throws InterruptedException
     */
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        Object value = stack.pop().toObject(ex);
        if (ex.getFIAState().getNextItem() == null) {
            if (logger.isDebugEnabled()) logger.debug("No item selected by FIA! ");
            return;
        }
        String formItemName = ex.getFIAState().getNextItem().name;
        if (formItemName != null) {
            IMediaObject mediaObj = (IMediaObject) value;
            Event event = ex.getEventEntry().getEvent();
            Event related;
            if (event != null
                && event instanceof SimpleEvent
                && (related = ((SimpleEvent)event).getRelated()) != null
                && related instanceof RecordFinishedEvent) {
                RecordFinishedEvent finishedEvent = (RecordFinishedEvent)related;

                Scope scope = ex.getCurrentScope();
                ShadowVarBase shadow = new ShadowVarBase();
                String shadowName = formItemName + "$";
                if (! scope.isDeclaredInExactlyThisScope(shadowName)) {
                    scope.declareReadOnlyVariable(shadowName, shadow);
                } else {
                    // Retrieve the old shadow var
                    Object o = scope.evaluate(shadowName);
                    if(o == scope.getUndefined()){
                        scope.setValue(shadowName, shadow);
                    } else {
                        if(o == null || ! (o instanceof ShadowVarBase)){
                            errorSemantic(ex, "Failed to retrieve old shadow variable");
                            return;
                        }
                        shadow = (ShadowVarBase) o;
                    }
                }

                setDuration(ex, mediaObj, shadow);
                setSize(mediaObj, shadow);
                if (ex.getUtterance() != null) {
                    ex.getCurrentScope().evaluate(shadowName + ".termchar = '" + ex.getUtterance() + "'" );
                    // reset to null so re-record will not have previous utterance
                    ex.setUtterance(null);
                }
                if (finishedEvent.getCause() == RecordFinishedEvent.CAUSE.MAX_RECORDING_DURATION_REACHED)
                    ex.getCurrentScope().evaluate(shadowName + ".maxtime = true" );
                else
                    ex.getCurrentScope().evaluate(shadowName + ".maxtime = false" );

            }
        }
    }

    private void setDuration(VXMLExecutionContext ex, IMediaObject mediaObject, ShadowVarBase shadowVars) {
        MediaProperties mediaProperties = mediaObject.getMediaProperties();
        if(mediaProperties == null){
            errorSemantic(ex, "MediaProperties was null");
        } else {
            // We only understand length in milliseconds.
            if(mediaProperties.hasLengthInUnit(MediaLength.LengthUnit.MILLISECONDS)){
                shadowVars.put("duration",
                        shadowVars,
                        mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
            } else {
                errorSemantic(ex, "Length of mediaproperties was not in milliseconds");
            }
        }
    }

    private void setSize(IMediaObject mediaObject, ShadowVarBase shadowVars) {
        shadowVars.put("size", shadowVars, mediaObject.getSize());
    }

    void errorSemantic(VXMLExecutionContext ex, String message){
        logger.error(message);
        ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC, message, DebugInfo.getInstance());
    }

    public String arguments() {
        return "";
    }
}
