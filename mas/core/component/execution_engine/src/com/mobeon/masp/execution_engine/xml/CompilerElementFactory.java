package com.mobeon.masp.execution_engine.xml;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmlpull.v1.XmlPullParserException;


/**
 * A DOM4J {@link DocumentFactory} implementation creating
 * {@link CompilerElement} instances.
 * 
 * @author Mikael Andersson
 */
public class CompilerElementFactory extends DocumentFactory {
    private LocatorAdapter locator;

    public CompilerElementFactory() {
    }

    private interface LocatorAdapter {
        public int getColumn();
        public int getLine();
    }
    public class SAXAdapter implements LocatorAdapter{
        private SAXCompilerHandler handler;

        public SAXAdapter(SAXCompilerHandler handler) {
            this.handler = handler;
        }

        public int getColumn() {
            return handler.getDocumentLocator().getColumnNumber();
        }

        public int getLine() {
            return handler.getDocumentLocator().getLineNumber();
        }
    }

    public class XPP3Adapter implements LocatorAdapter {
        private XPP3CompilerReader handler;

        public XPP3Adapter(XPP3CompilerReader handler) {
            this.handler = handler;
        }

        public int getColumn() {
            try {
                return handler.getXPPParser().getColumnNumber();
            } catch (XmlPullParserException e) {
            }
            return 0;
        }

        public int getLine() {
            try {
                return handler.getXPPParser().getLineNumber();
            } catch (XmlPullParserException e) {
            }
            return 0;
        }
    }

    public CompilerElementFactory(SAXCompilerHandler handler) {
        this.locator = new SAXAdapter(handler);
    }

    public CompilerElementFactory(XPP3CompilerReader handler) {
        this.locator = new XPP3Adapter(handler);

    }

    public Element createElement(QName qname) {
        if (locator != null) {
            return new CompilerElement(this,
                                       qname,
                                       locator.getLine(),
                                       locator.getColumn());
        } else {
            return new CompilerElement(this,qname,0,0);
        }
    }

    public void setHandler(SAXCompilerHandler handler) {
        this.locator= new SAXAdapter(handler);
    }
}
