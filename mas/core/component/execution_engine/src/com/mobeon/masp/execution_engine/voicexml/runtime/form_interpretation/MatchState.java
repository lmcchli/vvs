package com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-jan-18
 * Time: 15:46:34
 * To change this template use File | Settings | File Templates.
 */
public class MatchState {

    boolean hasOutstandingWakeupEvent = false;
    private String savedUtterance;
    private String idForWakeupEvent;

    public boolean hasOutstandingWakeupEvent() {
        return hasOutstandingWakeupEvent;
    }

    public void setHasOutstandingWakeupEvent(boolean hasOutstandingWakeupEvent) {
        this.hasOutstandingWakeupEvent = hasOutstandingWakeupEvent;
    }

    public void reset() {
        savedUtterance = null;
        hasOutstandingWakeupEvent = false;
    }

    public String getSavedUtterance() {
        return savedUtterance;
    }

    public void setSavedUtterance(String utterance) {
        this.savedUtterance = utterance;
    }

    public void appendToSavedUtterance(String s) {
        if (this.savedUtterance == null) {
            this.savedUtterance = s;
        } else {
            this.savedUtterance += s;
        }
    }

    public String getIdForWakeupEvent() {
        return idForWakeupEvent;
    }

    public void setIdForWakeupEvent(String id) {
        idForWakeupEvent = id;
    }

}
