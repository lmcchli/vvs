package com.mobeon.masp.execution_engine.xml;

import org.dom4j.DocumentException;
import org.dom4j.Document;
import com.mobeon.masp.execution_engine.voicexml.VoiceXMLCompilerDispatcher;


/**
 * User: QMIAN
 * Date: 2005-sep-06
 * Time: 15:26:41
 */
public class CompilerDriver {
    public static void main(String [] args) throws DocumentException {


        SAXCompilerReader r = new SAXCompilerReader();

        SAXCompilerHandler contentHandler = r.getContentHandler();

        r.setDocumentFactory(new CompilerElementFactory(contentHandler));

        // TODO: Skapa en riktig instans av application

        Document doc = r.read(
                "<vxml>" +
                    "<form>" +
                        "<field name=\"sample\">" +
                            "<prompt>Tell me what ?</prompt>" +
                        "</field>"+
                    "</form>" +
                "</vxml>");
        VoiceXMLCompilerDispatcher cd = (VoiceXMLCompilerDispatcher)VoiceXMLCompilerDispatcher.getInstance();

        // ApplicationImpl app = Parser.compile(cd,doc);
    }
}
