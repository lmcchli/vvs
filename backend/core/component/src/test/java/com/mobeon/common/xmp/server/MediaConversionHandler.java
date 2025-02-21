
package com.mobeon.common.xmp.server;

import com.mobeon.common.xmp.XmpAttachment;
import java.io.FileInputStream;
import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class MediaConversionHandler implements ServiceHandler {
    
    /** Creates a new instance of TestServiceHandler */
    public MediaConversionHandler() {
        
    }
    
    public void cancelRequest(String clientId, Integer transactionId) {
    }
    
    public void handleRequest(XmpResponseQueue responseQueue, 
                            String serviceId, 
                            String clientId, 
                            Integer transactionId, 
                            int validity, 
                            Document xmpDoc, 
                            java.util.ArrayList attachments) {
        XmpAnswer answer = new XmpAnswer();
        answer.setTransactionId(transactionId);
        String toFormat = getParameterValue("toFormat", xmpDoc);
        if(toFormat == null || toFormat.length() <= 0 ) {
            answer.setStatusCode(502);
            answer.setStatusText("toFormat not present");
        } else if(toFormat.indexOf("amr") != -1 ) {
            XmpAttachment att = loadVoiceFile("test.amr");
            if( att != null ) {
                answer.setStatusCode(200);
                answer.setStatusText("Success");
                answer.addParameter("length", "3");
                answer.addAttachment(att);
            } else {
                answer.setStatusCode(551);
                answer.setStatusText("couldn't find test.amr");
            }
        
        
        } else if(toFormat.indexOf("3gp") != -1 ){
            XmpAttachment att = loadVideoFile("test.3gp");
            if( att != null ) {
                answer.setStatusCode(200);
                answer.setStatusText("Success");
                answer.addParameter("length", "3");
                answer.addAttachment(att);
            } else {
                answer.setStatusCode(551);
                answer.setStatusText("couldn't find test.3gp");
            }

        
        } else {
            answer.setStatusCode(502);
            answer.setStatusText("unknown toFormat " + toFormat);
        }
        
        
        responseQueue.addResponse(answer);
        
    }
    
    /**
     * Gets an instance of ExternalSubscriberInformationHandler.
     *@return a ExternalSubscriberInformationHandler.
     */
    public static com.mobeon.common.xmp.server.ServiceHandler get() {
        return new MediaConversionHandler();
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
   
    private XmpAttachment loadVoiceFile(String path) {
        try {
            FileInputStream in = new FileInputStream(path);
            XmpAttachment att = new XmpAttachment(in, "audio/amr");
            return att;
        } catch (Exception e) {
            return null;
        }
    }
    
    private XmpAttachment loadVideoFile(String path) {
        try {
            FileInputStream in = new FileInputStream(path);
            XmpAttachment att = new XmpAttachment(in, "video/3gp");
            return att;
        } catch (Exception e) {
            return null;
        }
    }
}
