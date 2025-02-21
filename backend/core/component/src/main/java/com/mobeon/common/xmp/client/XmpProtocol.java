/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.common.xmp.client;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;

import com.mobeon.common.xmp.XmpErrorHandler;

/**
 * XmpProtocol
 */
public class XmpProtocol {

    private static int validity = XmpClient.get().getValidity();

    /**
     * Parses a String with the XML document for an XMP response and makes an
     * XMPResult object.
     *@param res - the XML document with the response
     *@return an XmlResult with the information parsed from the XML document.
     */
    /* package */ static XmpResult parseResponse(String res, XmpClient client, ArrayList attachments) {
        try {
            if (client.isDebugEnabled()) {
                if( attachments != null ) {
                    client.debug("XmpResponse with " + attachments.size() + " attachments: \n " +  res);
                } else  {
                    client.debug("XmpResponse: \n " +  res);
                }
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //Set a custom error handler to prevent error messages printed to System.out
            builder.setErrorHandler(new XmpErrorHandler(client.getLog()));
            StringReader sr = new StringReader(res);
            InputSource iSrc = new InputSource(sr);
            Document doc = builder.parse(iSrc);

            for (int i = 0; i < doc.getElementsByTagName("xmp-service-response").getLength(); i++) {
                String item;
                int transactionId = 0;
                int statusCode = 0;
                String statusText = null;

                Element xsr = (Element) doc.getElementsByTagName("xmp-service-response").item(i);
                if (xsr.getAttributes().getNamedItem("transaction-id") != null) {
                    item = xsr.getAttributes().getNamedItem("transaction-id").getNodeValue();
                    try {
                        transactionId = Integer.parseInt(item);
                    } catch (NumberFormatException e) {
                        transactionId = 0;
                        client.error("XmpProtocol: bad transaction id: " + item);
                    }
                }

                if (xsr.getElementsByTagName("status-code").getLength() != 0) {
                    item = xsr.getElementsByTagName("status-code").
                        item(0).getFirstChild().getNodeValue();
                    try {
                        statusCode = Integer.parseInt(item);
                    } catch (NumberFormatException e) {
                        transactionId = 0;
                        client.error("XmpProtocol: bad status code: " + item);
                    }
                }
                if (xsr.getElementsByTagName("status-text").getLength() != 0) {
                    statusText = xsr.getElementsByTagName("status-text").
                        item(0).getFirstChild().getNodeValue();
                }

                Properties props = null;
                NodeList parameterNodes = xsr.getElementsByTagName("parameter");
                if( parameterNodes.getLength() > 0 ) {
                    props = new Properties();
                    for( int n=0;n<parameterNodes.getLength();n++ ) {
                        Node node = parameterNodes.item(n);
                        String key = node.getAttributes().getNamedItem("name").getNodeValue();
                        // empty parameter value becomes ""
                        String value = "";
                        if( node.getFirstChild() != null ) {
                            value = node.getFirstChild().getNodeValue();
                        }
                        props.setProperty(key, value );

                    }


                }
                XmpResult result = new XmpResult(transactionId, statusCode, statusText, props, attachments);
                client.debug("Received XMP result: " + result);
                return result;
            }
        } catch (ParserConfigurationException e) {
            client.error("XmpProtocol.parseXmpResponse: Could not parse response: ", e);
        } catch (SAXException e) {
            client.error("XmpProtocol.parseXmpResponse: Could not parse response: ", e);
        } catch (IOException e) {
            client.error("XmpProtocol.parseXmpResponse: Could not parse response: ", e);
        }
        return null;
    }

    public static synchronized String makeEmptyRequest(){
        Document doc= new DocumentImpl();
        Element root;
        Element service;

        root = doc.createElement("xmp-message");
        root.setAttribute("xmlns", "http://www.abcxyz.se/xmp-1.0");
        service = doc.createElement("xmp-service-request");
        service.setAttribute("service-id", "Empty");
        service.setAttribute("transaction-id", "0");
        service.setAttribute("client-id", XmpClient.get().getClientId());
        root.appendChild( service );
        doc.appendChild( root );
        return getDocumentAsString(doc);
    }

    /**
     * Creates a string with an XML document that is an XMP request. This
     * function handles the general XMP parts, and delegates handling of
     * service-specific items to the method addServiceSpecificItems.
     *@param transId - unique identity of this transaction.
     *@param info - service specific information for this request.
     *@param caller - the callers telephone number.
     *@return a String with the XMP part of an XMP request.
     */
    public String makeRequest(int transId, Object info, String caller, String mailbox_id) {
        Document doc = new DocumentImpl();
        Element root;
        Element service;

        root = doc.createElement("xmp-message");
        root.setAttribute("xmlns", "http://www.abcxyz.se/xmp-1.0");
        service = doc.createElement("xmp-service-request");
        service.setAttribute("transaction-id", Integer.toString(transId));
        service.setAttribute("client-id", XmpClient.get().getClientId());
        addElement(doc, service, "validity", Integer.toString(validity));
        addElement(doc, service, "message-report", "true");
        addServiceSpecificItems(doc, service, info, caller, mailbox_id);
        root.appendChild(service);
        doc.appendChild(root);
        return getDocumentAsString(doc);
    }

    /**
     * Creates an xml-string for the specified service and properties.
     * @param transId unique id for the request.
     * @param serviceString The name of service to use. E.g. "PagerNotification"
     * @param props A properties containing parameter names and values.
     * @return An xml-string to be used with XmpClient.
     */
    public static String makeRequest( int transId, String serviceString, Properties props ) {
        return makeRequest(transId, serviceString, props, XmpClient.get().getClientId(), validity, true);
    }

    /**
     * Creates an xml-string for the specified service, clientid and properties.
     *
     * @param transId unique id for the request.
     * @param serviceString The name of service to use. E.g. "PagerNotification"
     * @param props A properties containing parameter names and values.
     * @param clientId The id of the client.
     * @param validity The validity time of the message.
     * @param messageReport Indicates if the client wants an XMP service response.
     * @return An xml-string to be sent.
     */
    public static String makeRequest(int transId, String serviceString, Properties props,
                                     String clientId, int validity, Boolean messageReport) {
        Document doc = new DocumentImpl();
        Element root;
        Element service;

        root = doc.createElement("xmp-message");
        root.setAttribute("xmlns", "http://www.abcxyz.se/xmp-1.0");
        service = doc.createElement("xmp-service-request");
        service.setAttribute("transaction-id", Integer.toString(transId));
        service.setAttribute("client-id", clientId);
        service.setAttribute("service-id", serviceString);
        addElement(doc, service, "validity", Integer.toString(validity));
        addElement(doc, service, "message-report", messageReport.toString());

        // add properties values
        Enumeration parameters = props.keys();

        while( parameters.hasMoreElements() ) {
            String parameterName = (String)parameters.nextElement();
            String parameterValue = props.getProperty( parameterName );
            addParameterElement(doc, service, parameterName, parameterValue );
        }

        root.appendChild(service);
        doc.appendChild(root);
        return getDocumentAsString(doc);
    }

    public static String updateTransId( String request, int oldId, int newId ) {
        String oldString = "transaction-id=\"" + oldId;
        String newString = "transaction-id=\"" + newId;
        return request.replaceAll(oldString, newString );

    }

    /**
     * When creating an XMP request, XmpProtocol calls this method to allow the
     * service-specific subclass to set its special elements and parameters.
     *@param doc - the XML document for the request.
     *@param service - the service part of the document.
     *@param info - the filter information for this particular notification
     *@param caller - the callers telephone number.
     * type.
     */
    protected void addServiceSpecificItems(Document doc, Element service, Object info, String caller, String mailbox_id) {}

    /**
     *
     */
    protected static void addParameterElement(Document doc,
                                              Element service,
                                              String attributeName,
                                              String textValue) {
        Element parameter = doc.createElement("parameter");
        parameter.setAttribute("name",  attributeName);
        parameter.appendChild(doc.createTextNode(textValue));
        service.appendChild(parameter);
    }

    /**
     * Adds an element to a document.
     */
    protected static void addElement(Document doc,
                                     Element service,
                                     String attributeName,
                                     String textValue) {
        Element el = doc.createElement(attributeName);
        el.appendChild(doc.createTextNode(textValue));
        service.appendChild(el);
    }

    /**
     * Creates a string from an XML document.
     */
    protected static String getDocumentAsString(Document doc) {
        StringWriter  stringOut;
        try {
            OutputFormat format     = new OutputFormat(doc);
            stringOut               = new StringWriter();
            XMLSerializer serial    = new XMLSerializer(stringOut, format);
            serial.asDOMSerializer();
            serial.serialize(doc.getDocumentElement());
            return stringOut.toString();
        } catch (IOException e) {
            XmpClient.get().error("XmpProtocol: Internal error. Could not convert XML document to string. " + e.toString() );
            return "";
        }
    }
}
