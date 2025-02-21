/**
 * Copyright (c) 2010 Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors;

import com.mobeon.masp.callmanager.SipUtils;
import com.mobeon.masp.callmanager.sip.header.PEarlyMediaHeader;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.util.Ignore;
import com.mobeon.masp.util.Tools;
import org.mozilla.javascript.Undefined;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Pheader extends MirrorBase {

    Map<String, Object> hash = new HashMap<String, Object>();
    private final Connection connection;

    private Callable<Object> pEarlyMedia = new Callable<Object>() {
        public Object call() {
            
            // We removed all special character from headerName in the Session
            // This is kept for backward compatibility with original implementation          
            String headerName = SipUtils.mapInternalSipHeaderName(PEarlyMediaHeader.NAME).toLowerCase(); 
            
            Object pEarlyMedia = connection.getCall().getSession().getData(headerName);
            if (pEarlyMedia != null) {
                return Tools.nullToUndefined(pEarlyMedia);
            }
            return Undefined.instance;
        }
    };

    public Map<String,Object> getHash() {
        return hash;
    }

    public Pheader(Connection connection) {
        this.connection = connection;
        hash.put("pEarlyMedia", pEarlyMedia);
        //hash.put(<other PRIVACY-HEADER name>, <other PRIVACY-HEADER Callable object>);
        Tools.defineToStringMethod(this);
    }

    public String getClassName() {
        return "Pheader";
    }

    public Object jstoString() {
        try {
            // We removed all special character from headerName in the Session
            // This is kept for backward compatibility with original implementation
            String headerName = SipUtils.mapInternalSipHeaderName(PEarlyMediaHeader.NAME).toLowerCase(); 
            
            Object pEarlyMedia = connection.getCall().getSession().getData(headerName);
            
            if (pEarlyMedia != null) {
                return pEarlyMedia;
            }
        } catch (Exception e) {
            Ignore.exception(e);
        }
        return super.toString();
    }
}
