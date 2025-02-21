
package com.mobeon.common.xmp.server;

import com.mobeon.common.xmp.XmpAttachment;
import com.mobeon.common.util.logging.SimpleLogger;
import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class accountinghandler implements ServiceHandler {

    /** Creates a new instance of TestServiceHandler */
    public accountinghandler() {
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
        
    	SimpleLogger event_logger = SimpleLogger.getLogger();
    	// event_logger.setFileName("xmp_events.log");
    	XmpAnswer answer = new XmpAnswer();
        answer.setTransactionId(transactionId);
        String user = getParameterValue("user", xmpDoc);
        if( user != null && user.length() > 0 ) {
        	String file_to_read = user.substring(user.length()-3, user.length() );
        	try {
        		event_logger.info("ClientId=" + clientId + " TransID=" + transactionId.toString() + ": Trying to open responsefile " + file_to_read +".txt");
        		BufferedReader statuscode_file = new BufferedReader(new FileReader(file_to_read + ".txt"));
        		answer.setStatusCode( Integer.parseInt(file_to_read) );
        		answer.setStatusText( statuscode_file.readLine() );
        		event_logger.info("ClientId=" + clientId + " TransID=" + transactionId.toString() + ": StatusCode=" + file_to_read + "(using response file " + file_to_read + ".txt)");
        		String templine;
        		String retparams="";
        		while ( (templine = statuscode_file.readLine()) != null ) {
        			String[] parameters = templine.split("=");
        			answer.addParameter(parameters[0],parameters[1]);
        			retparams += ( parameters[0] + "=" + parameters[1] + ", " );
        		}
        		event_logger.info("ClientId=" + clientId + " TransID=" + transactionId.toString() + ": Return parameters: " + retparams);
        	} catch (Exception ex) {
            	answer.setStatusCode(200);
            	answer.setStatusText("Success");
            	answer.addParameter("money", "1");
            	answer.addParameter("currency", "SEK");
            	event_logger.info("ClientId=" + clientId + " TransID=" + transactionId.toString() + ": StatusCode=200 StatusText=Success (default respons. No file found for StatusCode " + file_to_read + ")");
            	event_logger.info("ClientId=" + clientId + " TransID=" + transactionId.toString() + ": Return parameters: null");
            	event_logger.debug(ex.toString());
        	}
        }
        else {
        	answer.setStatusCode(200);
        	answer.setStatusText("Success");
        	event_logger.info("ClientId=" + clientId + " TransID=" + transactionId.toString() + ": StatusCode=200 StatusText=Success (parameter user=null)");
        	event_logger.info("ClientId=" + clientId + " TransID=" + transactionId.toString() + ": Return parameters: null");
        }
        
        String paramlist="";
        org.w3c.dom.NodeList nl = xmpDoc.getElementsByTagName("parameter");
        if ((nl.getLength() != 0) && (nl != null)) {
            for (int i = 0; i < nl.getLength(); i++) {
                try {
                    org.w3c.dom.Node n = nl.item(i);
                    if (n != null && n.getAttributes() != null) {
                        for (int x = 0; x < n.getAttributes().getLength(); x++) {
                            String attrName = n.getAttributes().item(x).getNodeName().trim();
                            String attrValue = n.getAttributes().getNamedItem(attrName).getNodeValue().trim();
                            String attrData = n.getFirstChild().getNodeValue().trim();
                            paramlist += (attrValue + "=" + attrData + ", ");
                        }
                    }
                } catch (Exception e) {
                    
                }
            }
        }
        event_logger.info("ClientId=" + clientId + " TransID=" + transactionId.toString() + ": Incomming parameters: " + paramlist);
        
        if( attachments != null) {
            for( int i=0;i<attachments.size();i++ ) {
                answer.addAttachment((XmpAttachment) attachments.get(i));
            }
        }
/*
        synchronized(this) {
            try {
                wait(40000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
*/
        responseQueue.addResponse(answer);
    }
    
    /**
     * Gets an instance of ExternalSubscriberInformationHandler.
     *@return a ExternalSubscriberInformationHandler.
     */
    public static com.mobeon.common.xmp.server.ServiceHandler get() {
        return new accountinghandler();
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
}
