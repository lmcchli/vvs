
package com.mobeon.common.xmp.server;

import com.mobeon.common.xmp.XmpAttachment;


public class OutdialNotificationHandler implements ServiceHandler {
    
    /** Creates a new instance of TestServiceHandler */
    public OutdialNotificationHandler() {
        
    }
    
    public void cancelRequest(String cleintId, Integer transactionId) {
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
        answer.addParameter("filetype", "mp3");
        answer.addParameter("newpar", "value");
        if( attachments != null) {
            for( int i=0;i<attachments.size();i++ ) {
                answer.addAttachment((XmpAttachment) attachments.get(i));
            }
        }
        responseQueue.addResponse(answer);
        
    }
    
    /**
     * Gets an instance of ExternalSubscriberInformationHandler.
     *@return a ExternalSubscriberInformationHandler.
     */
    public static com.mobeon.common.xmp.server.ServiceHandler get() {
        return new OutdialNotificationHandler();
    }
}
