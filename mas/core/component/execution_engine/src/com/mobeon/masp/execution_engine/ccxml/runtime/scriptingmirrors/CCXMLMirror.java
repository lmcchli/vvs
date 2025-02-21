package com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors;

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
 * Time: 12:02:50
 */
public class CCXMLMirror extends MirrorBase {
    private final Map<String, Object> hash = new HashMap<String, Object>();

    private final ConnectionImpl connection;

    public CCXMLMirror(ConnectionImpl connection) {
        this.connection = connection;
        hash.put("remote", remoteInstance());
        hash.put("local", localInstance());
        hash.put("redirect", redirectInstance());
        hash.put("_pHeader", pHeaderInstance());
        hash.put("_header", HeaderInstance());
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

    private Callable<Object> remoteInstance() {
        return new Callable<Object>() {
            private Remote remote = new Remote(connection);

            public Object call() {
                return remote;
            }
        };
    }

    private Callable<Object> localInstance() {
        return new Callable<Object>() {
            private Local local = new Local(connection);

            public Object call() {
                return local;
            }
        };
    }

    private Callable<Object> pHeaderInstance() {
        return new Callable<Object>() {
            private Pheader pHeader = new Pheader(connection);

            public Object call() {
                return pHeader;
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

    public String getClassName() {
        return "Connection";
    }


    public Map<String,Object> getHash() {
        return hash;
    }

}
