/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.PlayableObjectImpl;
import com.mobeon.masp.execution_engine.runtime.PlayableObjectPlayer;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * @author David Looberger
 */
public class SetAndPlayPlayingObject extends VXMLOperationBase {
    PlayableObjectImpl playable;
    public SetAndPlayPlayingObject(PlayableObjectImpl obj) {
        super();
        this.playable = obj;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if (playable != null)
            PlayableObjectPlayer.play(playable, ex);
    }

    public String arguments() {
        return "";
    }
}
