package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public enum CallActivity {

    IDLE ("idle","i"),
    PLAY ("play","p"),
    RECORD ("record","r"),
    UNKNOWN ("unknown","");

    private String info;
    private String shortInfo;

    CallActivity (String info,String shortInfo){
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
