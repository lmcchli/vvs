/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.*;
import com.mobeon.masp.execution_engine.runtime.PlayableObjectImpl;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.Redirector;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.MediaObjectException;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.util.Collections;
import java.util.List;

/**
 * @author David Looberger
 */
public class QueuePlayavbleObjects_TM extends VXMLOperationBase {
    private static final ILogger logger = ILoggerFactory.getILogger(QueuePlayavbleObjects_TM.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        List<Value> values = ex.getValueStack().popToMark();
        Value bVal = ex.getValueStack().pop();
        Boolean isInputItemChild = (Boolean) bVal.accept(ex, Visitors.getAsBooleanVisitor());
        String message = "";
        boolean concatenatingMode = false;
        Collections.reverse(values);
        for (int i = 0; i < values.size(); i++) {
            Value v = values.get(i);
            Object obj = v.accept(ex, Visitors.getAsObjectVisitor());
            if (obj instanceof PlayableObjectImpl) {
                if (concatenatingMode && message.length() > 0) {
                    addTextMediaObject(ex, message, isInputItemChild);
                    concatenatingMode = false;
                    message = "";
                }
                PlayableObjectImpl playableObject = (PlayableObjectImpl) obj;
                if (isInputItemChild) {
                    playableObject.setInputItemChild(true);
                }
                PromptQueue(ex).addPlayableToQueue(playableObject);
            } else if (obj instanceof String) {
                message += (String) obj;
                concatenatingMode = true;
            } else {
                message += obj.toString();
                concatenatingMode = true;
            }
        }
        if (message != null && message.length() > 0)
            addTextMediaObject(ex, message, isInputItemChild);
    }

    private void addTextMediaObject(VXMLExecutionContext ex, String message, Boolean isInputItemChild) {
        MimeType mt = null;
        try {
            mt = new MimeType("text/plain");
        } catch (MimeTypeParseException e) {
            if (logger.isDebugEnabled()) logger.debug("Failed to parse Mime type!");
        }

        MediaProperties mp = new MediaProperties(mt);
        IMediaObject textMediaObject = null;
        try {
            textMediaObject = ex.getMediaObjectFactory().create(message, mp);
        } catch (MediaObjectException e) {
            if (logger.isDebugEnabled()) logger.debug("Failed to create text MediaObject!" + e);
        }
        PlayableObjectImpl playableObject = new PlayableObjectImpl();
        playableObject.addMediaObject(textMediaObject);
        if (isInputItemChild) {
            playableObject.setInputItemChild(true);
        }
        PromptQueue(ex).addPlayableToQueue(playableObject);
    }

    public String arguments() {
        return "";
    }
}
