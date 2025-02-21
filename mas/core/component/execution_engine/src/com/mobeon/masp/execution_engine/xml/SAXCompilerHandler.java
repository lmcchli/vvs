package com.mobeon.masp.execution_engine.xml;

import org.xml.sax.Locator;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.DocumentFactory;
import org.dom4j.ElementHandler;


/**
 * A DOM4J {@link SAXContentHandler} implementations that provides access
 * to a SAX document {@linkplain Locator locator} used by the {@link
 * LineNoFactory} when creating elements.
 *
 * @author Mikael Andersson
 */
public class SAXCompilerHandler extends SAXContentHandler {
    Locator locator = null;
    private static CompilerElementFactory factory;

    public SAXCompilerHandler(DocumentFactory documentFactory, ElementHandler elementHandler) {
        super(documentFactory, elementHandler);
    }

    public SAXCompilerHandler(DocumentFactory documentFactory) {
        super(documentFactory);
    }

    public SAXCompilerHandler() {
        super(factory = new CompilerElementFactory());
        factory.setHandler(this);
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public Locator getDocumentLocator() {
        return locator;
    }
}
