/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */

package com.mobeon.event.types;
     
import org.apache.log4j.Logger;

import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-25
 * Time: 18:20:17
 * To change this template use File | Settings | File Templates.
 */
public class MASEvent extends EventObject {
    static Logger logger = Logger.getLogger("com.mobeon");    
    public String type;
    public MASEvent(Object source) {
        super(source);
    }
}
