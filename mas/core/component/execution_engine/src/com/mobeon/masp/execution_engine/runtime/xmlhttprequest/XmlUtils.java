/*
 * COPYRIGHT Abcxyz Communication Inc. Montreal 2009
 * The copyright to the computer program(s) herein is the property
 * of ABCXYZ Communication Inc. Canada. The program(s) may be used
 * and/or copied only with the written permission from ABCXYZ
 * Communication Inc. or in accordance with the terms and conditions
 * stipulated in the agreement/contact under which the program(s)
 * have been supplied.
 *---------------------------------------------------------------------
 * Created on 22-apr-2009
 */
package com.mobeon.masp.execution_engine.runtime.xmlhttprequest;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.xml.XMLObject;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides some simple Rhino - XMLObject utilities.
 * 
 * @author Bart Guijt
 * @version CVS $Revision: 1.4 $ $Date: 2006/11/13 01:23:51 $
 */
public final class XmlUtils {

    /**
     * Reads and parses the XML from the specified <code>stream</code> and returns
     * a Rhino E4X {@link XMLObject}.
     * 
     * @param cx the Rhino context we're in
     * @param parentScope the scope where the XMLObject is made in
     * @param stream the stream to read
     * @return a newly created XMLObject (never <code>null</code>)
     * @throws IOException if the parsing fails
     */
    public static XMLObject getXml(final Context cx, final Scriptable parentScope, final InputStream stream) throws IOException {
        return getXml(cx, parentScope, stream, null);
    }
    
    /**
     * Reads and parses the XML from the specified <code>stream</code> and returns
     * a Rhino E4X {@link XMLObject}.
     * 
     * @param cx the Rhino context we're in
     * @param parentScope the scope where the XMLObject is made in
     * @param stream the stream to read
     * @param encoding the carachter encoding to apply (e.g. <code>"UTF-8"</code>)
     * @return a newly created XMLObject (never <code>null</code>)
     * @throws IOException if the parsing fails
     */
    public static XMLObject getXml(final Context cx, final Scriptable parentScope, final InputStream stream, final String encoding) throws IOException {
        final XmlOptions options = new XmlOptions();
        if (encoding != null) {
            options.setCharacterEncoding(encoding);
        }
        try {
            final XmlObject responseXml = XmlObject.Factory.parse(stream, options);
            return (XMLObject) cx.newObject(
                    parentScope, "XML", new Object[] {Context.javaToJS(responseXml, parentScope)});
        } catch (XmlException e) {
            final IOException ioe = new IOException("Failed to parse XML: " + e);
            ioe.initCause(e.getCause());
            throw ioe;
        }
    }

    public static XMLObject getXml(final Context cx, final Scriptable parentScope, final Node node) throws IOException {
        XmlObject responseXml;
        try {
            responseXml = XmlObject.Factory.parse(node);
            return (XMLObject) cx.newObject(
                    parentScope, "XML", new Object[] {Context.javaToJS(responseXml, parentScope)});
        } catch (XmlException e) {
            final IOException ioe = new IOException("Failed to convert DOM node to E4X/XMLBeans node: " + e);
            ioe.initCause(e.getCause());
            throw ioe;
        }
    }
    
    public static XMLObject getXml(final Context cx, final Scriptable parentScope, final XmlObject xml) {
        return (XMLObject) cx.newObject(parentScope, "XML", new Object[] {Context.javaToJS(xml, parentScope)});
    }
    
    /**
     * Returns the W3C Dom Node which represents the specified Rhino XMLObject.
     * 
     * @param xml the Rhino XML object
     * @return the W3C Dom Node representing the Rhino XML
     */
    public static Node getDomNode(final XMLObject xml) {
        final Wrapper wrap = (Wrapper) ScriptableObject.callMethod(xml, "getXmlObject", new Object[0]);
        final XmlObject xmlObj = (XmlObject) wrap.unwrap();
        
        return xmlObj.getDomNode();
    }
}
