/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.util;

import com.mobeon.event.MASEventDispatcher;
import com.mobeon.event.types.MASTimeout;

import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-dec-01
 * Time: 13:35:33
 * To change this template use File | Settings | File Templates.
 */
public class MASTimerTask extends TimerTask{
    private MASEventDispatcher disp;

    public MASTimerTask(MASEventDispatcher disp) {
        this.disp = disp;
    }

    public void run() {
        disp.fire(new MASTimeout(this));
    }
}
