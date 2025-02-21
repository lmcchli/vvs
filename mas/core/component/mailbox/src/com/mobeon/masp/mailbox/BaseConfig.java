/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.configuration.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author QHAST
 */
public class BaseConfig {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(BaseConfig.class);

    private static AtomicReference<Map<String,String>> additionalPropertyMap = new AtomicReference<Map<String,String>>();

    protected BaseConfig() {
    }

    protected void init(IGroup configuration) throws MailboxException {

            if(additionalPropertyMap.get() == null) {
                List<IGroup> additionalpropertyGroups = null;
                try {
                    additionalpropertyGroups = configuration.getGroups("message.additionalproperty");
                } catch(UnknownGroupException e) {
                    LOGGER.debug("\""+e.getGroupName()+"\" groups is unknown or not found!");
                }
                HashMap<String,String> newMap = new HashMap<String,String>();

                try {
                    if(additionalpropertyGroups != null) {
                        for(IGroup group: additionalpropertyGroups) {
                            String name = group.getString("name");
                            String field = group.getString("field");
                            if(name != null && name.length()>0 && field!= null) {
                                newMap.put(name.toLowerCase(),field.length()==0?name:field);
                            } else {
                                LOGGER.warn("Additional property config name=\""+name+"\" with value=\""+field+"\" is not valid and is ignored!");
                            }
                        }
                    }
                } catch (UnknownParameterException e) {
                    throw new MailboxException("Unable to find \""+e.getGroupName()+"."+e.getParameterName()+"\"",e);
                }
                additionalPropertyMap.set(newMap);
                if(LOGGER.isDebugEnabled()) LOGGER.debug("additionalPropertyMap="+ additionalPropertyMap.get());
            }
    }

    public Map<String, String> getAdditionalPropertyMap() {
        return additionalPropertyMap.get();
    }

}
