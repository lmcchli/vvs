/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.event.types;

/**
 * Fire when the user asks to cancel the current prompt
 */
public class MASCancel extends MASEvent{
    public MASCancel(Object source) {
        super(source);
    }
}
