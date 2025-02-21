package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public enum CallType {
    VOICE ("voice","voice","Voice"),
    VIDEO ("video","video","Video"),
    SERVICE_REQUEST ("service_request","service_req","Service"),
    VOICE_VIDEO_UNKNOWN ("unknown","","Unknown");

    private String info;
    private String shortInfo;
    private String counterName;

    CallType (String info,String shortInfo,String counterName){
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