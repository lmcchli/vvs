package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public enum OperationalState {
    ENABLED ("Enabled"),
    DISABLED ("Disabeld"),
    UNKNOWN ("Unknown");

    String info;
    OperationalState(String info){
        this.info = info;
    }

    public String getInfo(){
        return this.info;
    }
}
