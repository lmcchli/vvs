package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public enum CallDirection {
    INBOUND ("incoming","in","Incoming"),
    OUTBOUND ("outgoing","out","Outgoing"),
    UNKNOWN ("unknown","","Unknown");

    private String info;
    private String shortInfo;
    private String counterName;

    CallDirection (String info,String shortInfo,String counterName){
        this.info = info;
        this.shortInfo = shortInfo;
        this.counterName=counterName;
    }

    public String getInfo(){
        return this.info;
    }

    public String getShortInfo(){
        return this.shortInfo;

    }
    public String getCounterName(){
        return this.counterName;

    }


}
