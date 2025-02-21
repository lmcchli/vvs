
package com.mobeon.common.xmp.server;

import com.mobeon.common.xmp.XmpAttachment;

import java.util.Properties;
import java.util.Enumeration;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;


public class externalsubscriberinformationhandler implements ServiceHandler {
    private final String serviceName = "externalsubscriberinformation";
    ServerConfig config;

    /** Creates a new instance of TestServiceHandler */
    public externalsubscriberinformationhandler() {
        config = ServerConfig.get();
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
        String number = getParameterValue("msisdn", xmpDoc);
        System.out.println("Received ESI Request for " + number);
        int code = config.getCode(number, serviceName);
        Properties params = config.getParams(number, serviceName);



        answer.setTransactionId(transactionId);
        answer.setStatusCode(code);
        answer.setStatusText("return text");
        if( params != null ) {
            Enumeration keys = params.keys();
            while( keys.hasMoreElements() ) {
                String key = (String) keys.nextElement();
                String value = params.getProperty(key);
                answer.addParameter(key, value);
            }
        }
        int replyTime = config.getReplyTime(number, serviceName);
        if( replyTime > 0 ) {
            try {
                Thread.sleep(replyTime*1000);
            } catch(Exception e) {}

        }
        if( replyTime >= 0 ) {
            // no answer for retry time < 0
            responseQueue.addResponse(answer);
        }

    }

    /**
     * Extracts value of element "parameter" from a Document
     *@param name - name of parameter.
     *@param xmpDoc - the XMP request.
     */
    private String getParameterValue(String name, Document xmpDoc) {
        NodeList nl = xmpDoc.getElementsByTagName("parameter");

        if ((nl.getLength() != 0) && (nl != null)) {
            for (int i = 0; i < nl.getLength(); i++) {
                try {
                    Node n = nl.item(i);
                    if (n != null && n.getAttributes() != null) {
                        for (int x = 0; x < n.getAttributes().getLength(); x++) {
                            String attrName = n.getAttributes().item(x).getNodeName().trim();
                            String attrValue = n.getAttributes().getNamedItem(attrName).getNodeValue().trim();
                            if (attrValue != null && attrValue.equalsIgnoreCase(name)) {
                                return n.getFirstChild().getNodeValue().trim();
                            }

                        }
                    }
                } catch (Exception e) {

                }
            }
        }
        return "";
    }




    
    /**
     * Gets an instance of ExternalSubscriberInformationHandler.
     *@return a ExternalSubscriberInformationHandler.
     */
    public static com.mobeon.common.xmp.server.ServiceHandler get() {
        return new externalsubscriberinformationhandler();
    }
}
