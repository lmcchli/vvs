package com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors;

import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.util.Ignore;
import com.mobeon.masp.util.Tools;
import org.mozilla.javascript.Undefined;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * User: QMIAN
 * Date: 2006-jun-09
 * Time: 11:47:17
 */
public class Local extends MirrorBase {
    Map<String, Object> hash = new HashMap<String, Object>();
    private final Connection connection;

    private Callable<Object> number = new Callable<Object>() {
        public Object call() {
            CalledParty calledParty = connection.getCall().getCalledParty();
            if (calledParty != null) {
                return Tools.nullToUndefined(calledParty.getTelephoneNumber());
            }
            return Undefined.instance;
        }
    };
    private Callable<Object> user = new Callable<Object>() {
        public Object call() {
            CalledParty calledParty = connection.getCall().getCalledParty();
            if (calledParty != null) {
                return Tools.nullToUndefined(calledParty.getSipUser());
            }
            return Undefined.instance;
        }
    };
    private Callable<Object> uri = new Callable<Object>() {
        public Object call() {
            CalledParty calledParty = connection.getCall().getCalledParty();
            if (calledParty != null) {
                return Tools.nullToUndefined(calledParty.getUri());
            }
            return Undefined.instance;
        }
    };

    public Map<String,Object> getHash() {
        return hash;
    }

    public Local(Connection connection) {
        this.connection = connection;
        hash.put("number", number);
        hash.put("user", user);
        hash.put("uri", uri);
        Tools.defineToStringMethod(this);
    }

    public String getClassName() {
        return "Local";
    }

    public Object jstoString() {
        try {
            CalledParty party = connection.getCall().getCalledParty();
            if (party != null && party.getUri() != null) {
                return party.getUri();
            }
        } catch (Exception e) {
            Ignore.exception(e);
        }
        return super.toString();
    }


}
