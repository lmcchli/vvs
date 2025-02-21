/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.event.types;

/**
 * Fire when the user provided some data, e.g. DTMF tones
 * Carries the data provided bu the user.
 */
public class MASUserInput extends MASEvent{
    private String data;
    public MASUserInput(Object source) {
        super(source);
    }

    public MASUserInput(Object source, String data) {
        super(source);
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
