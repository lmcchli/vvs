/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.event.types;

/**
 * Fire when the user failed to provide timely input
 */
public class MASNoInput extends MASEvent{
    public MASNoInput(Object source) {
        super(source);
    }
}