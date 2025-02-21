/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import java.util.Observer;
import java.util.Observable;
import java.io.IOException;
import java.io.*;
import java.util.StringTokenizer;
import java.util.*;

public class LoadRegulation implements Observer {

    private ILogger log;
    private OperateMAS operateMas;
    
    LoadRegulation(){
       log = ILoggerFactory.getILogger(LoadRegulation.class);	
    }
    
    public void setOperate(OperateMAS opMas) {
    	operateMas = opMas;
    }

    
    public void update(Observable o, Object arg) {
        String reason = (String)arg;

        if (reason.equals("serviceenabler:init") ) {
            // Service enabler inilized. set threshold for this service enabler.
            // this will be done everytime the service enabler is unlocked.
            ServiceEnabler se = (ServiceEnabler)o;
            Integer serviceEnablerInitialThreshold;
            Integer serviceEnablerHighWaterMark;
            Integer serviceEnablerLowWaterMark;
            serviceEnablerInitialThreshold = OMMConfiguration.getProvidedServiceInitialThreshold(se.getProtocol());
            serviceEnablerHighWaterMark  = OMMConfiguration.getserviceEnablerHighWaterMark(se.getProtocol());
            serviceEnablerLowWaterMark = OMMConfiguration.getserviceEnablerLowWaterMark(se.getProtocol());


            try {
                se.setThreshold(serviceEnablerHighWaterMark,serviceEnablerLowWaterMark,serviceEnablerInitialThreshold);
            } catch (Exception e) {
                e.printStackTrace();  // TODO.. log
            }
        }
    }
}
