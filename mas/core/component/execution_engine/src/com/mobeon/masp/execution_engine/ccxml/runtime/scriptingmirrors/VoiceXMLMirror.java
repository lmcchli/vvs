package com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors;

import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.RedirectingParty;
import com.mobeon.masp.execution_engine.ccxml.ConnectionImpl;
import com.mobeon.masp.util.Tools;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Undefined;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * User: QMIAN
 * Date: 2006-jun-09
 * Time: 11:56:41
 */
public class VoiceXMLMirror extends MirrorBase {

    private final Map<String, Object> hash = new HashMap<String, Object>();
    private final ConnectionImpl connection;

    public VoiceXMLMirror(ConnectionImpl
            connection) {
        this.connection = connection;
        Remote remote = new Remote(connection);
        Local local = new Local(connection);

        hash.put("remote", remote);
        hash.put("redirect", redirectInstance());
        hash.put("local", local);
        hash.put("calltype", callTypeInstance());
        hash.put("_totalinboundbitrate", totalInboundBitRateInstance());
        hash.put("_header", HeaderInstance());
    }

    private Callable<Object> callTypeInstance() {
        return new Callable<Object>() {
            public Object call() {
                CallProperties.CallType ct = connection.getCall().getCallType();
                if (ct == CallProperties.CallType.VOICE) {
                    return "voice";
                } else if (ct == CallProperties.CallType.VIDEO) {
                    return "video";
                }
                return Undefined.instance;
            }
        };
    }

    private Callable<Object> totalInboundBitRateInstance() {
        return new Callable<Object>() {
            public Object call() {
                return connection.getCall().getInboundBitRate();
            }
        };
    }

    private Callable<Object> redirectInstance() {
        return new Callable<Object>() {
            private NativeArray instance = null;
            Redirect redirect = new Redirect(connection);


            public Object call() {
                Object uri = Undefined.instance;

                RedirectingParty redirectingParty = connection.getRedirectingParty();
                if (redirectingParty != null) {
                    uri = Tools.nullToUndefined(redirectingParty.getUri());
                }
                if (uri == Undefined.instance) {
                    return Undefined.instance;
                } else {
                    return redirectInstance();
                }
            }

            private Object redirectInstance() {
                if (instance == null) instance = new NativeArray(new Object[]{redirect});
                return instance;
            }
        };
    }
    
    private Callable<Object> HeaderInstance() {
        return new Callable<Object>() {
            private Header header = new Header(connection);

            public Object call() {
                return header;
            }
        };
    }



    public String getClassName
            () {
        return "Connection";
    }

    public Map<String, Object> getHash
            () {
        return hash;
    }
}
