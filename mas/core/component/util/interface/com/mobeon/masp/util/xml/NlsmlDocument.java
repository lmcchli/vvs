/*
 * Copyright (c) 2005, Mobeon. All Rights Reserved.
 */

package com.mobeon.masp.util.xml;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import com.mobeon.masp.util.xml.nlsml.ResultDocument;
import com.mobeon.masp.util.xml.nlsml.InterpretationDocument;

/**
 * This a wrapper for the XML Beans which are generated from the NLSML schema.
 * NlsmlDocument provide means to parse NLSML text/documents.
 */
public class NlsmlDocument {
    private ResultDocument resultDocument = null;

    /**
     * Parses an NLSML XML text.
     * @param nlsmlText the NLSML to be parsed.
     */
    public void parse(String nlsmlText) {
        XmlOptions options = new XmlOptions();
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("", ResultDocument.type.getDocumentElementName().getNamespaceURI());
        options.setLoadSubstituteNamespaces(namespaces);
        options.setDocumentType(ResultDocument.type);
        try {
            resultDocument = ResultDocument.Factory.parse(nlsmlText, options);
        } catch (XmlException e) {
            resultDocument = null;
            e.printStackTrace();
        }
    }

    public String getXmlText() {
        XmlOptions options = new XmlOptions();
        options.setGenerateJavaVersion("1.0");
        options.setSavePrettyPrint();
        OutputStream output = new ByteArrayOutputStream();
        try {
            resultDocument.save(output, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    /**
     * Returns the status of the parse.
     * @return true if the NLSML is properly parsed and false otherwise.
     */
    public boolean isOk() {
        return resultDocument != null;
    }

    /**
     * Returns the input (the "token" parsed by the ASR engine).
     * @return the parsed speech
     */
    public String getInput() {
        ResultDocument.Result result = resultDocument.getResult();
        InterpretationDocument.Interpretation interpretation = result.getInterpretation();
        XmlCursor cursor = interpretation.getInput().newCursor();
        String input = cursor.getTextValue();
        cursor.dispose();
        return input;
    }

    /**
     * Returns the confidence value.
     */
    public int getConfidence() {
        ResultDocument.Result result = resultDocument.getResult();
        InterpretationDocument.Interpretation interpretation = result.getInterpretation();
        return interpretation.getConfidence();
    }
}
