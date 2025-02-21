/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.frontend.Stream;
import com.mobeon.util.InterruptTimerTask;
import com.mobeon.util.MASTimerTask;

import java.util.ArrayList;
import java.util.Timer;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-mar-07
 * Time: 14:54:06
 * To change this template use File | Settings | File Templates.
 */
public class RecordMsgNode extends Node {
    private Node next = null;
    private int maxtimeout;
    private int finalSilence = 600000;
    private String varName = null;
    private ArrayList mimeType = new ArrayList();
    private boolean dtmfterm = true;


    public Node execute(Traverser traverser) {
        // todo: Should use the mime type to deduce codecs etc.
        logger.debug("Executing");
        Stream instream = traverser.getInputStream();
        String msgFile = null;
        Timer t = new Timer();
        logger.debug("Staring timer...");
        t.schedule(new MASTimerTask(traverser.getDispatcher()),maxtimeout);
        try {
            if (instream != null) {
                msgFile = instream.record(dtmfterm);
                traverser.getEcmaExecutor().putIntoScope(varName, msgFile);
            }
            t.cancel();
        } catch (InterruptedException e) {
            t.cancel();
        }
        return next;
    }

    public void setNext(Node child) {
        this.next = child;
    }

    public Node getNext() {
        return next;
    }

    public int getMaxtimeout() {
        return maxtimeout;
    }

    public void setMaxtimeout(int maxtimeout) {
        this.maxtimeout = maxtimeout;
    }

    public int getFinalSilence() {
        return finalSilence;
    }

    public void setFinalSilence(int finalSilence) {
        this.finalSilence = finalSilence;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public ArrayList getMimeType() {
        return mimeType;
    }

    public void setMimeType(ArrayList mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isDtmfterm() {
        return dtmfterm;
    }

    public void setDtmfterm(boolean dtmfterm) {
        this.dtmfterm = dtmfterm;
    }
}
