package com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors;

import com.mobeon.masp.callmanager.CallPartyDefinitions;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.NumberCompletion;
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
 * Time: 11:46:40
 */
public class Remote extends MirrorBase {
   Map<String, Object> hash = new HashMap<String, Object>();
   private final Connection connection;

   private Callable<Object> number = new Callable<Object>() {
       public Object call() {
           CallingParty callingParty = connection.getCall().getCallingParty();
           if (callingParty != null) {
               return Tools.nullToUndefined(callingParty.getTelephoneNumber());
           }
           return Undefined.instance;
       }
   };
   private Callable<Object> user = new Callable<Object>() {
       public Object call() {
           CallingParty callingParty = connection.getCall().getCallingParty();
           if (callingParty != null) {
               return Tools.nullToUndefined(callingParty.getSipUser());
           }
           return Undefined.instance;
       }
   };
   private Callable<Object> uri = new Callable<Object>() {
       public Object call() {
           CallingParty callingParty = connection.getCall().getCallingParty();
           if (callingParty != null) {
               return Tools.nullToUndefined(callingParty.getUri());
           }
           return Undefined.instance;
       }
   };

   private Callable<Object> callingPresentationIndicator = new Callable<Object>() {
       public Object call() {
           CallingParty callingParty = connection.getCall().getCallingParty();
           Object pi = Undefined.instance;
           if (callingParty != null) {
               CallPartyDefinitions.PresentationIndicator p = callingParty.getPresentationIndicator();
               if(p == CallPartyDefinitions.PresentationIndicator.ALLOWED){
                   pi = 0;
               } else if(p == CallPartyDefinitions.PresentationIndicator.RESTRICTED){
                   pi = 1;
               }
           }
           return pi;
       }
   };

    private Callable<Object> numberComplete = new Callable<Object>() {
        public Object call() {
            CallingParty callingParty = connection.getCall().getCallingParty();
            Object ni = Undefined.instance;
            if (callingParty != null) {
                NumberCompletion numberCompletion = callingParty.getNumberCompletion();
                if(numberCompletion == NumberCompletion.COMPLETE){
                    ni = true;
                } else if(numberCompletion == NumberCompletion.INCOMPLETE){
                    ni = false;
                }
            }
            return ni;
        }
    };

    private Callable<Object> displayname = new Callable<Object>() {
        public Object call() {
            CallingParty callingParty = connection.getCall().getCallingParty();
            if (callingParty != null) {
                return Tools.nullToUndefined(callingParty.getFromDisplayName());
            }
            return Undefined.instance;
        }
    };

    private Callable<Object> remoteFromInstance() {
        return new Callable<Object>() {
            private RemoteFrom remoteFrom = new RemoteFrom(connection);

            public Object call() {
                return remoteFrom;
            }
        };
    }

    private Callable<Object> remotePAssertedIdentityInstance() {
        return new Callable<Object>() {
            private RemotePAssertedIdentity remotePAssertedIdentity = new RemotePAssertedIdentity(connection);

            public Object call() {
                return remotePAssertedIdentity;
            }
        };
    }

    public Map<String,Object> getHash() {
        return hash;
    }

    public Remote(Connection connection) {
        this.connection = connection;

        // ANI selected by CallManager
        hash.put("number", number);
        hash.put("user", user);
        hash.put("uri", uri);
        hash.put("pi", callingPresentationIndicator);
        hash.put("_displayname", displayname);
        hash.put("_numbercomplete", numberComplete);

        // 'A' entites received by CallManager
        hash.put("_from", remoteFromInstance());
        hash.put("_passertedidentity", remotePAssertedIdentityInstance());

        Tools.defineToStringMethod(this);
    }

    public String getClassName() {
        return "Remote";
    }

    public Object jstoString() {
        try {
            CallingParty party = connection.getCall().getCallingParty();
            if (party != null && party.getUri() != null) {
                return party.getUri();
            }
        } catch (Exception e) {
            Ignore.exception(e);
        }
        return super.toString();
    }
}
