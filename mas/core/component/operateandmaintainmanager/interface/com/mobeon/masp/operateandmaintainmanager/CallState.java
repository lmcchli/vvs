package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public enum CallState {
    RECORD ("record","r"),
    ALERTING ("alerting","a"),
    PROGRESSING ("progress","p"),
    CONNECTED ("connected","c"),
    FAILED ("failed","f"),
    DISCONNECTED ("disconnected","d"),
    ERROR ("error","e"),
    UNKNOWN ("unknown","");


    private String info;
    private String shortInfo;

    CallState (String info,String shortInfo){
        this.info = info;
        this.shortInfo = shortInfo;
    }

    public String getInfo(){
           return this.info;
       }

       public String getShortInfo(){
           return this.shortInfo;

       }



}
