package com.mobeon.common.xmp;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import com.mobeon.common.util.logging.ILogger;

/**
 * Custom error handler for {@link org.apache.xerces.parsers.XMLParser}.
 * This is used to prevent error messages printed on System.out, as by the
 * default ErrorHandler.
 *
 * @author mmawi
 */
public class XmpErrorHandler implements ErrorHandler {
    private ILogger log = null;

    public XmpErrorHandler(ILogger log) {
        this.log = log;
    }

    public void setLog(ILogger log) {
        this.log = log;
    }

    public void warning(SAXParseException exception) throws SAXException {
        if(log != null)
            log.info("XML Parser warning: (" + exception.getLineNumber()
                    + ":" + exception.getColumnNumber() + "), "
                    + exception.getMessage());
        throw exception;
    }

    public void error(SAXParseException exception) throws SAXException {
        if(log != null)
            log.info("XML Parser error: (" + exception.getLineNumber()
                    + ":" + exception.getColumnNumber() + "), "
                    + exception.getMessage());
        throw exception;
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        if(log != null)
            log.info("XML Parser fatal error: (" + exception.getLineNumber()
                    + ":" + exception.getColumnNumber() + "), "
                    + exception.getMessage());
        throw exception;
    }
}
