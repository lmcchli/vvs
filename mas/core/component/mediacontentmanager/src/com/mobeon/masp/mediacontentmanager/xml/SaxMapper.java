/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.xml;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;


public abstract class SaxMapper<E> extends DefaultHandler {

    /**
     * Returns the object of type E, mapped from the XML.
     *
     * @return Mapped object.
     */
    public abstract E getMappedObject();

    /**
     * Delegates to subclasses to create the tag-tracker
     * network for its' specific XML-format.
     *
     * @return The tracker.
     */
    public abstract TagTracker createTagTrackerNetwork();


    // A stack for the tag trackers to
    // coordinate on.
    //
    private Stack<TagTracker> tagStack = new Stack<TagTracker>();

    /**
     * The mapped XML.
     */
    protected URL parsedXMLUrl = null;
    // The SAX 2 parser...
    protected XMLReader xr;

    // Buffer for collecting data from
    // the "characters" SAX event.
    private CharArrayWriter contents = new CharArrayWriter();

    public SaxMapper() {

        try {
            // Create the XML reader...
            xr = XMLReaderFactory.createXMLReader();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the tag tracker network
        // and initialize the stack with
        // it.
        //
        // This constructor anchors the tag
        // tracking network to the begining
        // of the XML document. ( before the
        // first tag name is located ).
        //
        // By placing it first on the stack
        // all future tag tracking will follow
        // the network anchored by this
        // root tag tracker.
        //
        // The createTagTrackerNetwork() method
        // is abstract.  All sub classes are
        // responsible for reacting to this
        // request with the creation of a
        // tag tracking network that will
        // perform the mapping for the sub class.
        //
        //SaxMapperLog.trace( "Creating the tag tracker network." );
        tagStack.push(createTagTrackerNetwork());
        //SaxMapperLog.trace( "Tag tracker network created." );

    }

    /**
     * Builds an object of type <code>E</code> from the
     * file read.
     *
     * @param url The URL of the xml file.
     * @return The built object.
     * @throws SaxMapperException If the mapping failed, probably
     *                            due to a syntax error in the XML.
     */
    public E fromXML(URL url) throws SaxMapperException {
        parsedXMLUrl = url;
        E object = fromXML(new InputSource(url.toExternalForm()));
        if (object == null) {
            throw new SaxMapperException("Failed to map XML "
                    + parsedXMLUrl
                    + " to object that class "
                    + this.getClass().getName() + " builds.");
        }
        return object;
    }
    /**
     * Builds an object of type <code>E</code> from the
     * file read.
     *
     * @param in The InputStream of the xml file.
     *
     * @return The built object or null if error occurs while
     *         parsing the xml file.
     */
//    public E fromXML(InputStream in) {
//        try {
//            return fromXML(new InputSource(in));
//
//        } catch (Exception e) {
//            return null;
//        }
//    }

    /**
     * Builds an object of type <code>E</code> from the
     * file read.
     *
     * @param in The reader of the xml file.
     * @return The built object or null if error occurs while
     *         parsing the xml file.
     */
//    public E fromXML(Reader in) {
//        try {
//
//            return fromXML(new InputSource(in));
//
//        } catch (Exception e) {
//            return null;
//        }
//    }
    private synchronized E fromXML(InputSource in) throws SaxMapperException {

        // notes,
        // 1.  The calling "fromXML" methods catch
        //     any parsing exceptions.
        // 2.  The method is synchronized to keep
        //     multiple threads from accessing the XML parser
        //     at once.  This is a limitation imposed by SAX.

        // Set the ContentHandler...
        xr.setContentHandler(this);

        // Parse the file...
        //SaxMapperLog.trace( "About to parser XML document." );
        try {
            xr.parse(in);
        } catch (IOException e) {
            throw new SaxMapperException("Failed to parse MediaContentPackage file:"
                    + parsedXMLUrl
                    + "as an IOException was thrown when parsing it."
                    + "The mapping of the resource failed", e);
        } catch (SAXException e) {
            throw new SaxMapperException("Failed to parse MediaContentPackage file:"
                    + parsedXMLUrl
                    + " as a SAXException was thrown when parsing it."
                    + "The mapping of the resource failed", e);
        }
        //SaxMapperLog.trace( "XML document parsing complete." );

        return getMappedObject();
    }

    // Implement the content hander methods that
    // will delegate SAX events to the tag tracker network.

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes attr) throws SAXException {

        // Resetting contents buffer.
        // Assuming that tags either tag content or children, not both.
        // This is usually the case with XML that is representing
        // data strucutures in a programming language independant way.
        // This assumption is not typically valid where XML is being
        // used in the classical text mark up style where tagging
        // is used to style content and several styles may overlap
        // at once.
        contents.reset();

        // delegate the event handling to the tag tracker
        // network.
        TagTracker activeTracker = tagStack.peek();
        activeTracker.startElement(namespaceURI, localName,
                qName, attr, tagStack);


    }

    public void endElement(String namespaceURI,
                           String localName,
                           String qName) throws SAXException {

        // delegate the event handling to the tag tracker
        // network.
        TagTracker activeTracker = tagStack.peek();
        activeTracker.endElement(namespaceURI, localName,
                qName, contents, tagStack);

    }


    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // accumulate the contents into a buffer.
        contents.write(ch, start, length);

    }
}