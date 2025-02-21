package com.mobeon.masp.monitor;

import com.mobeon.masp.operateandmaintainmanager.FetchTask;

import java.util.TimerTask;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class FetchTimerTask extends TimerTask {
    FetchTask o;
    FetchTimerTask(Object obj) {
             o = (FetchTask)obj;
    }
    public void run() {
        
        o.processData();
    }
}
