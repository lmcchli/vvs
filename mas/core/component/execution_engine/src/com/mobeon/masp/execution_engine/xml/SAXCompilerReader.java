package com.mobeon.masp.execution_engine.xml;

import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.DocumentFactory;
import org.xml.sax.*;

/**
 * A DOM4J {@link SAXReader} building trees of {@link CompilerElement} instances.
 * <p>
 * A LineNoElement contains methods to retrieve source line and column associated
 * with a certain element.
 *
 * @author Mikael Andersson
 */
public class SAXCompilerReader extends SAXReader {

    SAXCompilerHandler handler = null;

    protected SAXContentHandler createContentHandler(XMLReader xmlReader) {
        return new SAXCompilerHandler();
    }

    public SAXCompilerHandler getContentHandler() {
        return handler;
    }

    public DocumentFactory getDocumentFactory() {
        return new CompilerElementFactory(handler);
    }

}
