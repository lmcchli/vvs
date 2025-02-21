/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.externaldocument.ResourceLocator;
import com.mobeon.masp.execution_engine.runtime.PlayableObjectImpl;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.util.Ignore;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * @author David Looberger
 */
public class CreatePlayableObject_TM_P extends VXMLOperationBase {
    private static final ILogger logger = ILoggerFactory.getILogger(CreatePlayableObject_TM_P.class);
    private boolean isChildOfPrompt = false;
    private boolean isInTransfer = false;

    public CreatePlayableObject_TM_P(boolean childOfPrompt, boolean _isInTransfer) {
        isChildOfPrompt = childOfPrompt;
        isInTransfer = _isInTransfer;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        ValueStack value_stack = ex.getValueStack();
        List<Value> values = value_stack.popToMark();

        Value v = values.get(0);
        Object src = v.accept(ex, Visitors.getAsObjectVisitor());

        PlayableObjectImpl alternative = getAlternativePlayableObject(values, ex);
        IMediaObject mediaobject = null;
        PlayableObjectImpl playableObject = new PlayableObjectImpl();
        playableObject.setInputItemChild(isChildOfPrompt);
        playableObject.setAlternative(alternative);

        if (src == null) {
            // TODO: Handle the case when we receive a null object.
            value_stack.pushScriptValue(playableObject);
            return;
        }

        URI relative = null;
        try {
            URI documentURI = ex.getExecutingModule().getDocumentURI();
            if (logger.isDebugEnabled()) logger.debug("Source is of type " + src.getClass());
            if (src instanceof IMediaObject) {
                mediaobject = (IMediaObject) src;
                playableObject.addMediaObject(mediaobject);
            } else if (src.getClass().isArray()) {
                IMediaObject mediaObjects[] = (IMediaObject[]) src;
                if (mediaObjects.length > 0)
                    playableObject.addMediaObject(mediaObjects);
                else {
                    if (logger.isDebugEnabled())
                        logger.debug("The mediaObject array received contained 0 elements, discarding media object array");
                }
            } else if (src instanceof String) {
                relative = documentURI.resolve(src.toString());
                // Determine contenttype based on file extension
                MimeType mt = null;
                if (src.toString().endsWith(".mov")) mt = new MimeType("video/quicktime");
                else
                    mt = new MimeType("audio/wav");

                MediaProperties mp = new MediaProperties(mt);
                if (relative.getScheme().equals("file")) {

                    // If the file does not exist the contents of <audio>
                    // should be played, we just return.
                    if (!fileExists(relative.getPath())) {
                        logFileNotExistError(relative.getPath());
                        return;
                    }

                    mediaobject = ex.getMediaObjectFactory().create(new File(relative.getPath()), mp);
                    playableObject.addMediaObject(mediaobject);
                }
                // ToDO Remove this block ?
                if (mediaobject == null && relative.getScheme().equals("test")) {
                    // if in transfer we should just push empty object on stack and return
                    if (isInTransfer) {
                        value_stack.pushScriptValue(playableObject);
                        return;
                    }

                    InputStream s = null;
                    try {
                        s = relative.toURL().openStream();
                        mediaobject = ex.getMediaObjectFactory().create(s, 1024, mt);
                        playableObject.addMediaObject(mediaobject);
                    } catch (IOException ioe) {
                        if (s != null)
                            try {
                                s.close();
                            } catch (IOException e1) {
                                Ignore.ioException(e1);
                            }

                        throw new IllegalArgumentException("Error opening URL " + relative, ioe);
                    }
                }
                if (mediaobject == null && relative.getScheme().equals("http")) {
                    // if in transfer we should just push empty object on stack and return
                    if (isInTransfer) {
                        value_stack.pushScriptValue(playableObject);
                        return;
                    }

                    ResourceLocator resourceLocator = ex.getResourceLocator();
                    resourceLocator.setMediaObjectFactory(ex.getMediaObjectFactory());
                    String maxage = getMaxage(values, ex);
                    String fetchTimeout = getFetchTimeout(values, ex);
                    mediaobject = resourceLocator.getMedia(src.toString(), maxage, fetchTimeout);
                    if (mediaobject != null) {
                        playableObject.addMediaObject(mediaobject);
                    } else {
                        logger.warn("Could not fetch resource " + src);
                        //TODO here we could play content of the audio element according to
                        // If the audio file cannot be played (e.g. 'src' referencing or 'expr' evaluating to an
                        // invalid URI, a file with an unsupported format, etc), the content of the audio element is played instead."
                        // in the VXML specc
                    }
                }
            } else if (src instanceof NativeArray) {
                doNativeArray(src, playableObject);
            } else {
                // This is something we cannot play, throw an error.badfetch
                if (logger.isInfoEnabled()) logger.info("Unable to play this object");
                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH, DebugInfo.getInstance());
            }

        } catch (IllegalStateException e) {
            if (logger.isDebugEnabled()) logger.debug(e);
        } catch (MediaObjectException e) {
            if (logger.isDebugEnabled()) logger.debug(e);
        } catch (jakarta.activation.MimeTypeParseException e) {
            if (logger.isDebugEnabled()) logger.debug(e);
        } catch (java.lang.IllegalArgumentException e) {
            TestEventGenerator.generateEvent(TestEvent.CREATE_PLAYABLE_OBJECT_TM_P, e);
            if (logger.isDebugEnabled()) logger.debug(e);
        } finally {
            value_stack.pushScriptValue(playableObject);
        }
    }

    private PlayableObjectImpl getAlternativePlayableObject(List<Value> values, VXMLExecutionContext ex) {
        PlayableObjectImpl alternative = null;
        if (values.size() > 3) {
            Value v = values.get(3);
            Object obj = v.accept(ex, Visitors.getAsObjectVisitor());
            if (obj instanceof PlayableObjectImpl) {
                alternative = (PlayableObjectImpl) obj;
            } else if (obj instanceof String) {
                String str = (String) obj;
                MimeType mt = null;
                try {
                    mt = new MimeType("text/plain");
                } catch (MimeTypeParseException e) {
                    if (logger.isDebugEnabled()) logger.debug("Failed to parse Mime type!");
                }

                MediaProperties mp = new MediaProperties(mt);
                IMediaObject textMediaObject = null;
                try {
                    textMediaObject = ex.getMediaObjectFactory().create(str, mp);
                } catch (MediaObjectException e) {
                    if (logger.isDebugEnabled()) logger.debug("Failed to create text MediaObject!" + e);
                }
                alternative = new PlayableObjectImpl();
                alternative.addMediaObject(textMediaObject);
            } else {
                if (logger.isDebugEnabled()) logger.debug("Unknown type used as alternative media");
            }
        }
        return alternative;
    }

    private void doNativeArray(Object src, PlayableObjectImpl playableObject) {
        NativeArray array = (NativeArray) src;
        if (logger.isDebugEnabled()) logger.debug("Number of elements in NativeArray: " + array.getLength());
        for (int index = 0; index < array.getLength(); index++) {
            if (logger.isDebugEnabled()) logger.debug("Unwrapping NativeJavaObject #" + index);
            NativeJavaObject nativeElement = (NativeJavaObject) array.get(index, null);
            Object object = nativeElement.unwrap();
            if (object instanceof IMediaObject) playableObject.addMediaObject((IMediaObject) object);
            else
                logger.warn("Type cast problem in javascript! Object was not IMediaObject: " + object);
        }
        if (logger.isDebugEnabled())
            logger.debug("Number of MediaObjects: " + playableObject.getMedia().length);
    }


    private void logFileNotExistError(String path) {
        if (logger.isInfoEnabled()) logger.info("Requested to play file '" + path +
                "', but it does not exist");
    }

    private boolean fileExists(String path) {
        File f = new File(path);
        return f.exists();
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
        if (values.size() > 2) {
            maxage = values.get(2).toString();
        }
        if (maxage == null) {
            maxage = ex.getProperty("audiomaxage");
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
