/*
 * Copyright (c) 2005, Mobeon. All Rights Reserved.
 */

package com.mobeon.masp.util.xml;

import com.mobeon.masp.util.xml.ssml.Sentence;
import com.mobeon.masp.util.xml.ssml.Speak;
import com.mobeon.masp.util.xml.ssml.SpeakDocument;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import javax.xml.namespace.QName;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Provides XML parser and generater (marshal/unmarshal) for SSML documents.
 * The SsmlDocument also provide means to access "properties" and data in the SSML.
 * The porpose is to simplify the SSML handling (parse and generate) by adding
 * (through) the interface constraints upon the structure of the SSML document.
 * Since we will only use a subset of the SSML schema we should also put restrictions
 * to the flexibility of the SSML schema.
 */
public class SsmlDocument {
    private static ILogger logger = ILoggerFactory.getILogger(SsmlDocument.class);
    public static final String LANGUAGE = "language";
    public static final String VOICE = "voice";
    public static final String SPEED = "speed";
    public static final String VOLUME = "volume";
    public static MimeType SSML_MIME_TYPE;
    private SpeakDocument speakDoc = null;
    private QName sentenceQName = null;

    /**
     * Default constructor.
     */
    public SsmlDocument() {
        logger.debug("--> SsmlDocument()");
        // Due to the complex schema we need to handle the naming manually.
        sentenceQName = new QName(Sentence.type.getName().getNamespaceURI(),
                "s", Sentence.type.getName().getPrefix());
        logger.debug("<-- SsmlDocument()");
        try {
            SSML_MIME_TYPE = new MimeType("application/ssml");
        } catch (MimeTypeParseException e) {
            // This should not happen since the texts above are already validated.
            logger.error("Unknown mime type! " + e);
        }
    }
    /**
     * Parses XML text into an internal representation.
     * @param xmlText the parsed text.
     */
    public void parse(String xmlText) {
        logger.debug("--> parse() : [" + xmlText + "]");
        XmlOptions options = new XmlOptions();
        Map<String, String> namespaces = new HashMap<String, String>();

        namespaces.put("", SpeakDocument.type.getDocumentElementName().getNamespaceURI());
        options.setLoadSubstituteNamespaces(namespaces);
        options.setDocumentType(SpeakDocument.type);
        try {
            speakDoc = SpeakDocument.Factory.parse(xmlText, options);
        } catch (XmlException e) {
            e.printStackTrace();
        }
        logger.debug("<-- parse()");
    }

    /**
     * Creates an empty SSML.
     */
    public void initialize() {
        logger.debug("--> initialize()");
        speakDoc = SpeakDocument.Factory.newInstance();
        Speak speak = speakDoc.addNewSpeak();
        speak.setVersion("1.0");
        logger.debug("<-- initialize()");
    }

    /**
     * Generates an XML text from the internal representation.
     * @return the generated XML text.
     */
    public String getXmlText() {
        logger.debug("--> getXmlText()");
        XmlOptions options = new XmlOptions();
        options.setGenerateJavaVersion("1.0");
        options.setSavePrettyPrint();
        OutputStream output = new ByteArrayOutputStream();
        try {
            speakDoc.save(output, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("<-- getXmlText()");
        return output.toString();
    }

    /**
     * Parameter value setter.
     * @param name the parameter name.
     * @param value the parameter value.
     */
    public void setParameter(String name, String value) {
        if (LANGUAGE.equalsIgnoreCase(name)) {
            speakDoc.getSpeak().setLang(value);
        } else if (VOICE.equalsIgnoreCase(name)) {
        } else if (SPEED.equalsIgnoreCase(name)) {
        } else if (VOLUME.equalsIgnoreCase(name)) {
        } else {
            throw new IllegalArgumentException("Unknown parameter: '" + name + "'");
        }
    }

    /**
     * Parameter value getter.
     * @param name the name of the parameter.
     * @return the parameter value.
     */
    public String getParameter(String name) {
        String value;

        if (LANGUAGE.equalsIgnoreCase(name)) {
            value = speakDoc.getSpeak().getLang();
        } else if (VOICE.equalsIgnoreCase(name)) {
            value = "unknown";
        } else if (SPEED.equalsIgnoreCase(name)) {
            value = "unknown";
        } else if (VOLUME.equalsIgnoreCase(name)) {
            value = "unknown";
        } else {
            throw new IllegalArgumentException("Unknown parameter: '" + name + "'");
        }
        return value;
    }

    /**
     * Appends a sentence to speech.
     * @param sentence the appended sentence.
     */
    public void addSentence(String sentence) {
        logger.debug("--> addSentence() : [" + sentence + "]");
        XmlObject ssmlSentence = speakDoc.getSpeak().addNewStruct();
        XmlCursor cursor = ssmlSentence.newCursor();
        cursor.setName(sentenceQName);
        cursor.setTextValue(sentence);
        cursor.dispose();
        logger.debug("<-- addSentence()");
    }

    /**
     * Appends a list of sentences to the speech.
     * @param sentences the appended sentences.
     */
    public void addSentences(String ... sentences) {
        logger.debug("--> addSentences()");
        for (String sentence : sentences) addSentence(sentence);
        logger.debug("<-- addSentences()");
    }

    /**
     * Getter for the speech text
     * @return a list of sentences.
     */
    public Collection<String> getSentences() {
        Collection<String> sentences = new Vector<String>();
        XmlCursor cursor = speakDoc.getSpeak().newCursor();
        cursor.toFirstChild();
        do {
            if (cursor.getObject() instanceof Sentence) {
                sentences.add(cursor.getTextValue());
            }
        } while (cursor.toNextSibling());
        cursor.dispose();
        return sentences;
    }
}
