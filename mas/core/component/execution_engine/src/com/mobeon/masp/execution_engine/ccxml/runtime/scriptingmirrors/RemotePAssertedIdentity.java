/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors;

import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.util.Ignore;
import com.mobeon.masp.util.Tools;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.mozilla.javascript.Undefined;

public class RemotePAssertedIdentity extends MirrorBase {
    Map<String, Object> hash = new HashMap<String, Object>();
    private final Connection connection;

    private Callable<Object> number = new Callable<Object>() {
        public Object call() {
            CallingParty callingParty = connection.getCall().getCallingParty().getPAssertedIdentityCallingParty();
            if (callingParty != null) {
                return Tools.nullToUndefined(callingParty.getTelephoneNumber());
            }
            return Undefined.instance;
        }
    };

    private Callable<Object> user = new Callable<Object>() {
        public Object call() {
            CallingParty callingParty = connection.getCall().getCallingParty().getPAssertedIdentityCallingParty();
            if (callingParty != null) {
                return Tools.nullToUndefined(callingParty.getSipUser());
            }
            return Undefined.instance;
        }
    };

    private Callable<Object> uri = new Callable<Object>() {
        public Object call() {
            CallingParty callingParty = connection.getCall().getCallingParty().getPAssertedIdentityCallingParty();
            if (callingParty != null) {
                return Tools.nullToUndefined(callingParty.getUri());
            }
            return Undefined.instance;
        }
    };

    private Callable<Object> displayname = new Callable<Object>() {
        public Object call() {
            CallingParty callingParty = connection.getCall().getCallingParty().getPAssertedIdentityCallingParty();
            if (callingParty != null) {
                return Tools.nullToUndefined(callingParty.getFromDisplayName());
            }
            return Undefined.instance;
        }
    };

    public Map<String,Object> getHash() {
        return hash;
    }

    public RemotePAssertedIdentity(Connection connection) {
        this.connection = connection;
        hash.put("number", number);
        hash.put("user", user);
        hash.put("uri", uri);
        hash.put("displayname", displayname);
        Tools.defineToStringMethod(this);
    }

    public String getClassName() {
        return "RemotePAssertedIdentity";
    }

    public Object jstoString() {
        try {
            CallingParty party = connection.getCall().getCallingParty().getPAssertedIdentityCallingParty();
            if (party != null && party.getUri() != null) {
                return party.getUri();
            }
        } catch (Exception e) {
            Ignore.exception(e);
        }
        return super.toString();
    }
}
