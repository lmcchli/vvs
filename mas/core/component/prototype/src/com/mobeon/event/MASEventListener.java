/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package com.mobeon.event;

import com.mobeon.event.types.MASEvent;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-25
 * Time: 18:21:41
 * To change this template use File | Settings | File Templates.
 */
public interface MASEventListener extends EventListener {
    public void notify(MASEvent e);
}
