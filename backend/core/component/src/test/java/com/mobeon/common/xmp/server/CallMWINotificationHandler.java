
package com.mobeon.common.xmp.server;

import com.mobeon.common.xmp.XmpAttachment;


public class CallMWINotificationHandler implements ServiceHandler {
    
    /** Creates a new instance of TestServiceHandler */
    public CallMWINotificationHandler() {
        
    }
    
    public void cancelRequest(String clientId, Integer transactionId) {
    }
    
    public void handleRequest(XmpResponseQueue responseQueue, 
                            String serviceId, 
                            String clientId, 
                            Integer transactionId, 
                            int validity, 
                            org.w3c.dom.Document xmpDoc, 
                            java.util.ArrayList attachments) {
        XmpAnswer answer = new XmpAnswer();
        answer.setTransactionId(transactionId);
        answer.setStatusCode(200);
        answer.setStatusText("Success");
        if( attachments != null) {
            for( int i=0;i<attachments.size();i++ ) {
                answer.addAttachment((XmpAttachment) attachments.get(i));
            }
        }
        
        final IXmpAnswer finalAnswer = answer;
        final XmpResponseQueue finalQueue = responseQueue;
        new Thread() {
            public void run() {
                try {
                    sleep(2000);
                    finalQueue.addResponse(finalAnswer);
                } catch(Exception e) {
                    
                }
            }
        }.start();
        
        
        
    }
    
    /**
     * Gets an instance of ExternalSubscriberInformationHandler.
     *@return a ExternalSubscriberInformationHandler.
     */
    public static com.mobeon.common.xmp.server.ServiceHandler get() {
        return new CallMWINotificationHandler();
    }
}
