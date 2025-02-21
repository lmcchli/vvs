package com.mobeon.masp.operateandmaintainmanager;

import java.util.Vector;
import java.util.Hashtable;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class StatisticMonitorInfo {
    private static Hashtable<String,Vector> lstStatisticInfo;
    private Long time = System.currentTimeMillis();

    public StatisticMonitorInfo(){
        lstStatisticInfo = new Hashtable<String,Vector>();
    }

    //public void put(String pos,String serviceEnablerName,String connectionType,String counter1,String counter2,String counterValue){
    public void put(String pos,String serviceEnablerName,String counterValue){

        Vector<String> cv = new Vector<String>();

        cv.add(time.toString());
        cv.add(pos);            //ServiceEnabler/*/*/*
        cv.add(serviceEnablerName); // Name of service enabler
     //   cv.add(connectionType);     // counter type current / total
 //       cv.add(connectionType);     // connection type voice/video
//        cv.add(counter1);           // Name of counter inbound/outbound/answer/abandond
//        cv.add(counter2);           // Name of counter inbound/outbound
        cv.add(counterValue);       // Counter value

        lstStatisticInfo.put(pos ,cv);

    }

    public Hashtable<String,Vector> getInfo(){
     return lstStatisticInfo;
    }
}
