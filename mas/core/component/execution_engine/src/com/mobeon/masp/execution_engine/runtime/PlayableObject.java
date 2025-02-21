package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.PropertyStack;
import com.mobeon.masp.execution_engine.compiler.Constants;

import java.util.List;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Mar 20, 2006
 * Time: 4:48:46 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PlayableObject {

    public void setAlternative(PlayableObject alternative);

    public PlayableObject getAlternative();

    public IMediaObject[] getMedia();

    public void addMediaObject(IMediaObject mo);

    public void addMediaObject(List<IMediaObject> mos);

    public void addMediaObject(IMediaObject mediaObjects[]);

    public boolean isBargein();

    public void setBargein(boolean bargein);

    public boolean isInputItemChild();

    public void setInputItemChild(boolean inputItemChild);

    public String getBargeintype();

    public String getTimeout();

    public String getLang();

    public String getBase();

    public void retrieveCurrentPropSettings(VXMLExecutionContext ex);

    public String getMarkText();

    public void setMarkText(String markText);

    public String getOffset();

    void updateContextProperties(VXMLExecutionContext ex);
}
