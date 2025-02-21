
package com.abcxyz.services.moip.migration.configuration.moip;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;

import com.abcxyz.services.moip.migration.configuration.moip.Utilities;


import java.io.IOException;
import java.io.StringReader;
import java.io.File;
import java.nio.channels.FileChannel;
import java.io.FileOutputStream;
import java.io.FileInputStream;

/**
 * This class modifies XML code in a easy-to-use way.  The intended
 * use is for modifying XML configuration files.
 *
 * The normal usage is like this;
 *
 *  - Create a XmlModifier
 *  - Call modifiers, e.g. "setattr()" for all inteded modifications.
 *  - Call "transform()".
 *
 * The last two items may be repeated without creating a new
 * XmlModifier object, since the "transform()" resets the XmlModifier.
 */
public class XmlModifier {

    private static final String xslprelude =
    "<xsl:stylesheet version=\"1.0\"" +
    "  xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
    "  <xsl:output method=\"xml\" indent=\"yes\" encoding=\"ISO-8859-1\"/>" +
    "  <xsl:template match=\"*\">" +
    "    <xsl:copy><xsl:copy-of select=\"@*\"/>" +
    "       <xsl:apply-templates/></xsl:copy>" +
    "  </xsl:template>";
    private static final String xslpostlude =
    "</xsl:stylesheet>";
    private StringBuffer xslbuffer;
    public String backupSuffix = ".save";

    private class XmlModifierErrorListener implements ErrorListener {
        XmlModifierErrorListener(){}
        public void error(TransformerException exception)
            throws TransformerException {
            throw exception;
        }
        public void fatalError(TransformerException exception)
            throws TransformerException {
            throw exception;
        }
        public void warning(TransformerException exception) 
            throws TransformerException {}
    }

    /**
     * Create a general XmlModifier.
     */
    public XmlModifier() {
        xslbuffer = new StringBuffer(xslprelude);
    }

    /**
     * Set attributes for the node(s) selected by the XPath. The
     * attributes for a particular node must be set only once per call
     * to "transform()".
     */
    public void setattr(String xPath, String... attrs) {
        xslbuffer.append("<xsl:template match=\'");
        xslbuffer.append(xPath);
        xslbuffer.append("\'><xsl:copy><xsl:copy-of select=\"@*\"/>");
        for (String attr : attrs) {
            int indexOfEquals = attr.indexOf('=');
            if (indexOfEquals > 0) {
                xslbuffer.append("<xsl:attribute name=\"");
                xslbuffer.append(attr.substring(0, indexOfEquals));
                xslbuffer.append("\">");
                xslbuffer.append(attr.substring(indexOfEquals+1));
                xslbuffer.append("</xsl:attribute>");
            } else {
                xslbuffer.append("<xsl:attribute name=\"");
                xslbuffer.append(attr);
                xslbuffer.append("\"></xsl:attribute>");
            }
        }
        xslbuffer.append("<xsl:apply-templates/></xsl:copy></xsl:template>");
    }

    /**
     * General transform.
     * @param source The XML source code.
     * @param result The transformed XML code.
     */
    public void transform(StreamSource source, StreamResult result) 
        throws TransformerException {
        xslbuffer.append(xslpostlude);
        if (false) System.out.println(xslbuffer);
        TransformerFactory transFactory = SAXTransformerFactory.newInstance();
        transFactory.setErrorListener(new XmlModifierErrorListener());
        Transformer trans = transFactory.newTransformer(
            new StreamSource(new StringReader(xslbuffer.toString())));
        trans.setErrorListener(new XmlModifierErrorListener());
        trans.transform(source, result);
        xslbuffer = new StringBuffer(xslprelude);
    }

    /**
     * Transform a (configuration-)file. A backup of the file will be
     * saved with a ".save" extension (any old ".save" file is deleted).
     *
     * The original file is modified, i.e. the transformed file will
     * be the same file (inode) as the original. This seems important
     * for "log4j".
     *
     * Prerequisite: Both the file and the containing directory must
     * be writable.
     *
     * @param cfgfile The file to be transformed.
     */
    public void transform(String cfgfile)
        throws IOException, TransformerException {
        String backupPath = cfgfile + backupSuffix;
        Utilities.copyFile(cfgfile, backupPath);
        FileInputStream src = new FileInputStream(backupPath);
        FileOutputStream dst = new FileOutputStream(cfgfile);
        transform(new StreamSource(src), new StreamResult(dst));
        dst.close();
        src.close();
    }

}
