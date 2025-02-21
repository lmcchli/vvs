/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.util.Ignore;

import java.util.HashMap;
import java.util.Map;

public class Header extends MirrorBase {
   
    private final Connection connection;

    public Header(Connection connection) {
        this.connection = connection;
       
    }

    public String getClassName() {
        return "Header";
    }

    public Object jstoString() {
        try {
            String headerString = getHash().toString();
            if (headerString != null) {
                return headerString;
            }
        } catch (Exception e) {
            Ignore.exception(e);
        }
        return super.toString();
    }

    @Override
    public Map<String, Object> getHash() {
        Map<String, Object> hash = new HashMap<String, Object>();
        Map<String, Object> originalMap = connection.getCall().getSession().getMap();

        for(Map.Entry<String, Object> entry : originalMap.entrySet()) {

            String key = entry.getKey();

            key = key.replace('-', '_');

            hash.put(key, entry.getValue());

        }
        return hash;
    }
        

}
