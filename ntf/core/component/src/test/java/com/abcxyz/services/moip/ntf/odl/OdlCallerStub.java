package com.abcxyz.services.moip.ntf.odl;


import java.util.HashMap;

import com.mobeon.common.xmp.client.XmpResult;
import com.mobeon.common.xmp.client.XmpResultHandler;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.out.outdial.OdlCallListener;
import com.mobeon.ntf.out.outdial.OdlCallSpec;
import com.mobeon.ntf.userinfo.UserInfo;

public class OdlCallerStub implements Constants, OdlCallSpec {

	HashMap<String, OdlHandler> handlers = new HashMap<String, OdlHandler>();

	int callmade = 0;
	@Override
	public void sendCall(String subscriberNumber, String notificationNumber, UserInfo user, OdlCallListener listener) {
		OdlHandler handler = new OdlHandler(subscriberNumber, notificationNumber, user, listener );
		handlers.put(notificationNumber, handler);

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
    	OdlHandler handler = handlers.get(number);
    	if (handler != null) {
        	handler.sendOk();
    	} else {
    		System.out.println("OdlCallerStib SendOk handler not found");
    	}
    }
    public void sendResponse(String number, int code) {
    	OdlHandler handler = handlers.get(number);
    	if (handler != null) {
        	handler.sendWithResponseCode(code);
    	} else {
    		System.out.println("OdlCallerStib sendWithResponseCode handler not found");
    	}
    }


    private class OdlHandler implements XmpResultHandler {
        private String subscriberNumber;
        private String notificationNumber;
        private UserInfo user;
        private OdlCallListener listener;

        public OdlHandler(String subscriberNumber, String notificationNumber, UserInfo user, OdlCallListener listener) {
            this.subscriberNumber = subscriberNumber;
            this.notificationNumber = notificationNumber;
            this.user = user;
            this.listener = listener;
        }

        public void send() {

            System.out.println("OdlCaller (stub) sending XMP request to MAS for " + subscriberNumber + " : " + notificationNumber);
            // get esi data
           /* try {
                //sleep 5, send OK
                Thread.sleep(5000);
                listener.handleResult(number, user, OdlConstants.EVENT_CODE_COMPLETED);

                if( user.isNotifTypeDisabledOnUser(NTF_ODL) == 1 ) {
                    listener.handleResult(number,user, OdlConstants.EVENT_CODE_NOTIFDISABLED);
                    return;
                } else if( user.isNotifTypeDisabledOnUser(NTF_ODL) > 1 ) {
                    listener.handleResult(number,user,OdlConstants.EVENT_CODE_LOCATION_FAILURE);
                    return;
                }

                int cfu = user.getDivertAll();
                if( cfu == 1 ) {
                    listener.handleResult(number, user, OdlConstants.EVENT_CODE_CFU_ON);
                    return;
                }
                if( cfu == 2 ) {
                    listener.handleResult(number, user, OdlConstants.EVENT_CODE_CFU_FAILURE);
                    return;
                }


                if (number == "12345") {
                    listener.handleResult(number,user,OdlConstants.EVENT_CODE_LIMIT_EXCEEDED);
                    return;
                }

                //sleep 5, send OK
                Thread.sleep(5000);
                listener.handleResult(number, user, OdlConstants.EVENT_CODE_COMPLETED);

            } catch(Exception e) {
                Logger.getLogger().logMessage("Exception in OdlCaller " + NtfUtil.stackTrace(e).toString(), Logger.L_ERROR );
                listener.handleResult(number, user, OdlConstants.EVENT_CODE_INTERRUPTED);
            }*/

        }

        public synchronized void handleResult(XmpResult result) {
            listener.handleResult(subscriberNumber, notificationNumber, user, result.getStatusCode());
        }

        public void sendOk() {
            try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            listener.handleResult(subscriberNumber, notificationNumber, user, 200);
        }

        public void sendWithResponseCode(int code) {
            try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            listener.handleResult(subscriberNumber, notificationNumber, user, code);
        }

    }
}
