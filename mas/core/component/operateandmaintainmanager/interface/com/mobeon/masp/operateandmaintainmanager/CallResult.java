package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public enum CallResult {
    CONNECTED ("connected","conn","Connected"),
    FAILED ("failed","fail","Failed"),
    ABANDONED ("abandoned disconnected","abdi","AbandonedDisconnected"),
    ABANDONED_REJECTED ("abandoned rejected","abre","AbandonedRejected"),
    FARENDDISCONNECTED ("farenddisconnected","fdis","FarendDisconnected"),
    NEARENDDISCONNECTED ("nearenddisconnected","ndis","NearendDisconnected"),
    DROPPEDENTITIES ("droppedentities","dpe","DroppedEntities"),
    ERROR ("error","err","Error");

    private String info;
    private String shortInfo;
    private String counterName;


    CallResult (String info,String shortInfo,String counterName){
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
