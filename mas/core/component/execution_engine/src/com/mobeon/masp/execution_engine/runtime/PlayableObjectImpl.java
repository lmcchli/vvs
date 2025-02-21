/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.voicexml.runtime.PropertyStack;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.mediaobject.IMediaObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Looberger
 */
public class PlayableObjectImpl implements PlayableObject {
    private List<IMediaObject> media = new ArrayList<IMediaObject>();
    private PlayableObject alternative = null;
    private boolean bargein = true;
    private String bargeintype;
    private String timeout;
    private String lang;
    private String base;
    private String offset;
    private boolean isInputItemChild = false;
    private String markText = null;

    public PlayableObjectImpl() {
    }

    public PlayableObject getAlternative() {
        return alternative;
    }

    public void setAlternative(PlayableObject alternative) {
        this.alternative = alternative;
    }

    public IMediaObject[] getMedia() {
        IMediaObject[] ret = new IMediaObject[media.size()];
        media.toArray(ret);
        return ret;
    }

    public void addMediaObject(IMediaObject mo) {
        media.add(mo);
    }

    public void addMediaObject(List<IMediaObject> mos) {
        media.addAll(mos);
    }

    public void addMediaObject(IMediaObject mediaObjects[]) {
        addMediaObject(Arrays.asList(mediaObjects));
    }

    public boolean isBargein() {
        return bargein;
    }

    public void setBargein(boolean bargein) {
        this.bargein = bargein;
    }

    public boolean isInputItemChild() {
        return isInputItemChild;
    }

    public void setInputItemChild(boolean inputItemChild) {
        isInputItemChild = inputItemChild;
    }

    public String getBargeintype() {
        return bargeintype;
    }

    public String getTimeout() {
        return timeout;
    }

    public String getLang() {
        return lang;
    }

    public String getBase() {
        return base;
    }

    public void retrieveCurrentPropSettings(VXMLExecutionContext ex) {
        PropertyStack properties = ex.getProperties();
        bargein = properties.getProperty("bargein").equals("true") ;
        bargeintype = properties.getProperty("bargeintype");
        timeout = properties.getProperty("timeout");
        lang = properties.getProperty("xml:lang");
        base = properties.getProperty("xml:base");
        offset = properties.getProperty(Constants.PlatformProperties.PLATFORM_AUDIO_OFFSET);
    }

    public String getMarkText() {
        return markText;
    }

    public void setMarkText(String markText) {
        this.markText = markText;
    }

    public String getOffset() {
        return offset;
    }

    public void updateContextProperties(VXMLExecutionContext ex) {
        if (getTimeout() != null)
            ex.getProperties().putProperty(Constants.VoiceXML.TIMEOUT, getTimeout());
            ex.getProperties().putProperty(Constants.VoiceXML.BARGEIN, isBargein() ? "true" : "false");
        if (getBargeintype() != null)
            ex.getProperties().putProperty(Constants.VoiceXML.BARGEINTYPE, getBargeintype());
        if (getLang() != null)
            ex.getProperties().putProperty(Constants.VoiceXML.XMLLANG, getLang());
        if (getBase() != null)
            ex.getProperties().putProperty(Constants.VoiceXML.XMLBASE, getBase());
        ex.getProperties().putProperty(Constants.PlatformProperties.PLATFORM_AUDIO_OFFSET, getOffset());        
    }
}
