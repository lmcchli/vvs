/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.configuration.moip;

import com.abcxyz.services.moip.migration.configuration.moip.SchemaValidator;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: eperber
 * Date: 2005-dec-16
 * Time: 12:09:57
 */
public final class SchemaValidator {
    private static final ILogger logger = ILoggerFactory.getILogger(SchemaValidator.class);
    private String schemaName;

    public SchemaValidator() {
    }

    public SchemaValidator(String schemaName) {
        this.schemaName = schemaName;
    }

    public boolean validate(String xmlFilename) {
        xmlFilename = new File(xmlFilename).getAbsolutePath();
        String xsdFilename = schemaName;
        if (schemaName == null) {
            xsdFilename = getSchemaFromXML(xmlFilename);
            if (xsdFilename == null) {
                int dotIdx = xmlFilename.lastIndexOf('.');
                if (dotIdx >= 0) {
                    xsdFilename = xmlFilename.substring(0, dotIdx) + ".xsd";
                } else {
                    xsdFilename = xmlFilename + ".xsd";
                }
            }
        }
        return validateWithSchema(xmlFilename, xsdFilename);
    }

    public static boolean validateWithSchema(String xmlFilename, String schemaName) {
        File xmlFile = new File(xmlFilename).getAbsoluteFile();
        xmlFilename = xmlFile.getPath();
        File xsdFile = new File(schemaName);
        if (!xsdFile.isAbsolute()) {
            schemaName = new File(xmlFile.getParent(), schemaName).getAbsolutePath();
            xsdFile = new File(schemaName);
        }
        if (xsdFile.exists()) {
            SchemaFactory sf = SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                Schema s = sf.newSchema(new StreamSource(schemaName));
                Validator v = s.newValidator();
                v.validate(new StreamSource(xmlFilename));
            } catch (SAXParseException e) {
                String message = "Unable to validate configuration file: " + xmlFilename;
                message += " Error at line: " + e.getLineNumber();
                message += ", column: " + e.getColumnNumber();
                //message += " " + e.getMessage();
                logger.error(message, e);
                return false;
            } catch (SAXException e) {
                String message = "Unable to validate configuration file: " + xmlFilename;
                message += " Error message: " + e.toString();
                logger.error(message);
                return false;
            } catch (IOException e) {
                logger.warn("Configuration schema not found for: " + xmlFilename);
                return false;
            }
            return true;
        }
        logger.warn("Configuration schema not found for: " + xmlFilename);
        return false;
    }

    public static String getSchemaFromXML(String xmlFile) {
        File in = new File(xmlFile);

        if (in.exists()) {
            try {
                SAXReader reader = new SAXReader();
                Document xmlDoc = reader.read(in);
                return getSchemaFromXML(xmlDoc);
            } catch (DocumentException e) {
            }
        }
        return null;
    }

    public static String getSchemaFromXML(Document xmlDoc) {
        String schemaName = xmlDoc.getRootElement().attributeValue("noNamespaceSchemaLocation");
//	File schemaFile = new File(schemaName);
        if (!(new File(schemaName).isAbsolute())) {
            File xmlFile;
            try {
                // The documentation is not very clear on the name format.
                // Observed behaviour is that the name is in URL form.
                URL xmlURL = new URL(xmlDoc.getName());
                xmlFile = new File(xmlURL.getPath());
            } catch (MalformedURLException e) {
                // If the URL is malformed in any way, assume it's actually
                // a standard path. Untested fallback, should not happen.
                xmlFile = new File(xmlDoc.getName());
            }
            schemaName = new File(xmlFile.getParent(), schemaName).getAbsolutePath();
        }

        return schemaName;
    }
}
