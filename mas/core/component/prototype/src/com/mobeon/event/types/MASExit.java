/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.event.types;

/**
 * Fire when the user asked to exit, i.e. the MAS shall hang up
 */
public class MASExit extends MASEvent{
    public MASExit(Object source) {
        super(source);
    }
}
