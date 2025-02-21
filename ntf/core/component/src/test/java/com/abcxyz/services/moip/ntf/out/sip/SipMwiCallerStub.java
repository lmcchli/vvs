package com.abcxyz.services.moip.ntf.out.sip;

import java.util.HashMap;

import com.abcxyz.services.moip.ntf.event.SipMwiEvent;
import com.mobeon.common.xmp.client.XmpResult;
import com.mobeon.common.xmp.client.XmpResultHandler;
import com.mobeon.ntf.out.sip.SIPCallListener;
import com.mobeon.ntf.out.sip.SIPInfo;
import com.mobeon.ntf.out.sip.SipMwiCallSpec;
import com.mobeon.ntf.userinfo.UserInfo;

public class SipMwiCallerStub implements SipMwiCallSpec {

	HashMap<String, SipMwiHandler> handlers = new HashMap<String, SipMwiHandler>();

	int callmade = 0;
	
	public void sendCall(SipMwiEvent sipMwiEvent, SIPInfo sipInfo, UserInfo user, SIPCallListener listener) {
	    SipMwiHandler handler = new SipMwiHandler(sipMwiEvent, sipInfo, user, listener );
		handlers.put(sipMwiEvent.getNotificationNumber(), handler);

		handler.send();
		callmade ++;
	}

	public int getCallMade() {
		return callmade;
	}

	public void reset() {
		callmade = 0;
	}

    public void sendOk(String number) {
        SipMwiHandler handler = handlers.get(number);
    	if (handler != null) {
            System.out.println("SipMwiCallerStub SendOk handler");
        	handler.sendOk();
    	} else {
    		System.out.println("SipMwiCallerStub SendOk handler not found");
    	}
    }
    
    public void sendResponse(String number, int code) {
        SipMwiHandler handler = handlers.get(number);
        if (handler != null) {
            System.out.println("SipMwiCallerStub sendWithResponseCode " + code);
            handler.sendResponseCode(code);
        } else {
            System.out.println("SipMwiCallerStub sendWithResponseCode handler not found");
        }
    }
    
    public void sendResponseWithRetryTime(String number, int retryTime) {
        SipMwiHandler handler = handlers.get(number);
        if (handler != null) {
            System.out.println("SipMwiCallerStub sendWithRetryTime " + retryTime);
            handler.sendResponseCodeWithRetryTime(retryTime);
        } else {
            System.out.println("SipMwiCallerStub sendWithRetryTime handler not found");
        }
    }

    private class SipMwiHandler implements XmpResultHandler {
        
        private SipMwiEvent sipMwiEvent;
        private UserInfo user;
        private SIPCallListener listener;

        public SipMwiHandler(SipMwiEvent sipMwiEvent, SIPInfo sipInfo, UserInfo user, SIPCallListener listener) {
            this.sipMwiEvent = sipMwiEvent;
            this.user = user;
            this.listener = listener;
        }

        public void send() {
            System.out.println("SipMwiCallerStub XMP call sent out to " + sipMwiEvent);
        }

        public synchronized void handleResult(XmpResult result) {
            listener.handleResult(sipMwiEvent, user, result.getStatusCode(), 0);
        }

        public void sendOk() {
            listener.handleResult(sipMwiEvent, user, 200, 0);
        }

        public void sendResponseCode(int code) {
            listener.handleResult(sipMwiEvent, user, code, 0);
        }

        public void sendResponseCodeWithRetryTime(int retryTime) {
            listener.handleResult(sipMwiEvent, user, 400, retryTime);
        }
    }
}
