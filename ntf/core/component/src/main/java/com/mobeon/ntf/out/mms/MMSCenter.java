/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.out.mms;

import java.util.Map;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.mail.EmailListHandler;
import com.mobeon.ntf.util.Logger;

/**
 *MMSCenter handles one MMS-C.
 */
public class MMSCenter {
    
    private final static Logger log = Logger.getLogger(MMSCenter.class); 
	private String componentName = null;
    private String host = null;
    private String protocol = null;
    private String uri = null;
    private int port = -1;
    private boolean isOk = false;

    /**
     * Constructor
     * @param name The identifying name of the MMS-C.
     */
    public MMSCenter(String name) {
    	log.logMessage("Constructing MMSCenter " + name, Logger.L_VERBOSE);
    	
    	Map<String, String> multiMediaComponent = null;
    	try {
    	    multiMediaComponent = Config.getExternalEnabler(NotificationConfigConstants.MULTIMEDIA_MESSAGE_TABLE, name);
    	    if (multiMediaComponent != null) {
                componentName = name;
                host = multiMediaComponent.get(NotificationConfigConstants.HOST_NAME);
                protocol = multiMediaComponent.get(NotificationConfigConstants.PROTOCOL);
                uri = multiMediaComponent.get(NotificationConfigConstants.URI);
                port = Integer.parseInt(multiMediaComponent.get(NotificationConfigConstants.PORT));
                isOk = true;
    	    }
    	}
    	catch (NumberFormatException e) {
    		log.logMessage("Port is not numeric for service <MultimediaMessage>  with component name: " + name, Logger.L_ERROR);
    	}
    }

    public boolean isOk() {
    	return isOk;
    }
    
    public String getName() {return componentName;}
    public String getHost() {return host;}
    public String getProtocol() {return protocol;}
    public int getPort() {return port;}
    public String getUri() {return uri;}
  }
