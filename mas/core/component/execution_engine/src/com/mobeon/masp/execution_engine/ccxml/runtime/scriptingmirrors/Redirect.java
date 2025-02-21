package com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors;

import com.mobeon.masp.callmanager.CallPartyDefinitions;
import com.mobeon.masp.callmanager.RedirectingParty;
import com.mobeon.masp.execution_engine.ccxml.ConnectionImpl;
import com.mobeon.masp.util.Ignore;
import com.mobeon.masp.util.Tools;
import org.mozilla.javascript.Undefined;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * User: QMIAN
 * Date: 2006-jun-09
 * Time: 11:45:38
 */
public class Redirect extends MirrorBase {
    Map<String, Object> hash = new HashMap<String, Object>();
    private ConnectionImpl connection;

    private Callable<Object> redirectNumber = new Callable<Object>() {
        public Object call() {
            RedirectingParty redirectingParty = connection.getRedirectingParty();
            Object number = Undefined.instance;
            if (redirectingParty != null) {
                number = redirectingParty.getTelephoneNumber();
                if (number == null) {
                    number = Undefined.instance;
                }
            }
            return number;
        }
    };
    private Callable<Object> redirectingUser = new Callable<Object>() {
        public Object call() {
            RedirectingParty redirectingParty = connection.getRedirectingParty();
            Object user = Undefined.instance;
            if (redirectingParty != null) {
                user = redirectingParty.getSipUser();
                if (user == null) {
                    user = Undefined.instance;
                }
            }
            return user;
        }
    };

    private Callable<Object> redirectingPresentationIndicator = new Callable<Object>() {
        public Object call() {
            RedirectingParty redirectingParty = connection.getRedirectingParty();
            Object pi = Undefined.instance;
            if (redirectingParty != null) {
                CallPartyDefinitions.PresentationIndicator p = redirectingParty.getPresentationIndicator();
                if(p == CallPartyDefinitions.PresentationIndicator.ALLOWED){
                    pi = 0;
                } else if(p == CallPartyDefinitions.PresentationIndicator.RESTRICTED){
                    pi = 1;
                }
            }
            return pi;
        }
    };

    private Callable<Object> redirectingReason = new Callable<Object>() {
        public Object call() {
            RedirectingParty redirectingParty = connection.getRedirectingParty();
            Object cause = Undefined.instance;
            if (redirectingParty != null) {
                switch(redirectingParty.getRedirectingReason()){
                    case USER_BUSY:
                        cause = "user busy";
                        break;
                    case NO_REPLY:
                        cause = "no reply";
                        break;
                    case UNCONDITIONAL:
                        cause = "unconditional";
                        break;
                    case DEFLECTION_DURING_ALERTING:
                        cause = "deflection during alerting";
                        break;
                    case DEFLECTION_IMMEDIATE_RESPONSE:
                        cause = "deflection immediate response";
                        break;
                    case MOBILE_SUBSCRIBER_NOT_REACHABLE:
                        cause = "mobile subscriber not reachable";
                        break;
                    default:
                        cause = "unknown";
                }
            }
            return cause;
        }
    };
    private Callable<Object> redirectingUri = new Callable<Object>() {
        public Object call()  {
            RedirectingParty redirectingParty = connection.getRedirectingParty();
            Object uri = Undefined.instance;
            if (redirectingParty != null) {
                uri = redirectingParty.getUri();
                if (uri == null) {
                    uri = Undefined.instance;
                }
            }
            return uri;
        }
    };


    public Map<String,Object> getHash() {
        return hash;
    }

    public Redirect(ConnectionImpl connection) {
        this.connection = connection;
        hash.put("number", redirectNumber);
        hash.put("user", redirectingUser);
        hash.put("pi", redirectingPresentationIndicator);
        hash.put("reason", redirectingReason);
        hash.put("uri", redirectingUri);
        Tools.defineToStringMethod(this);
    }


    public String getClassName() {
        return "Redirect";
    }

    public Object jstoString() {
        try {
            RedirectingParty party = connection.getRedirectingParty();
            if (party != null && party.getUri() != null) {
                return party.getUri();
            }
        } catch (Exception e) {
            Ignore.exception(e);
        }
        return super.toString();
    }
}
