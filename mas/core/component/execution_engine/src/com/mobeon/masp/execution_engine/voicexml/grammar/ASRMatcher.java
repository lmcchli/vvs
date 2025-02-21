/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import static com.mobeon.masp.util.Tools.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.io.XPP3Reader;
import org.dom4j.DocumentException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;


public class ASRMatcher {
    private static ILogger log = ILoggerFactory.getILogger(ASRMatcher.class);
    public static String getUtterance(GrammarScopeNode node, String nlsml) {

        /*
<?xml version='1.0'?>
<result>
<interpretation grammar="session:grammarA" confidence="97">
<input mode="speech">two</input>
<instance>
<SWI_literal>two</SWI_literal>
<SWI_grammarName>session:grammarA</SWI_grammarName>
<SWI_meaning>{SWI_literal:two}</SWI_meaning>
</instance>
</interpretation>
</result>

        */
        GrammarScopeNode initialNode = node;
        List<Interpretation> list = buildInterpretations(nlsml);

        // TODO: implement correct scooping
        ASRGrammar g = (ASRGrammar) node.getGrammar();
        String ret = match(g, list);
        if (ret != null) {
            return ret;
        }
        while (node.getParent() != null) {
            if(node instanceof VirtualRootGrammarScopeNode)
                continue;
            node = node.getParent();
            ret = match((ASRGrammar) node.getGrammar(), list);
            if (ret != null) {
                return ret;
            }

        }
        log.warn("Neither one of "+ func.foldL1().f(func.add(),map(new Fn1<String,Interpretation>()
        {
            public String f(Interpretation interpretation) {
                return interpretation.getGrammar_id();
            }
        },list))+  " interpretations could be matched against "+initialNode.getGrammar().getGrammar_id()+" or any of it's ancestors");
        return null;
    }

    private static String match(ASRGrammar grammar, List<Interpretation> list) {

        for (Interpretation i : list) {
            if (i.getGrammar_id().equals(grammar.getGrammar_id())) {
                if(log.isDebugEnabled()) log.debug("ASR MATCH " + i.getUtterance());
                return i.getUtterance();
            }
        }
        return null;

    }

    private static List<Interpretation> buildInterpretations(String nlsml) {
        XPP3Reader r = new XPP3Reader();
        List<Interpretation> list = new ArrayList<Interpretation>();
        try {
            Document d = r.read(new StringReader(nlsml));

            Element root = d.getRootElement();

            for (Object o : root.content()) {
                Node e = (Node) o;
                if (e.getNodeType() == Node.ELEMENT_NODE) {
                    list.add(new Interpretation((Element) e));
                }
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return list;
    }

    static class Interpretation {
        private String grammar_id = null;
        private String utterance = null;
        private int confidence;

        public Interpretation(Element elem) {
            String s = elem.attributeValue("grammar");
            s = s.substring(s.indexOf(":") + 1);
            this.grammar_id = s;

            this.confidence = Integer.parseInt(elem.attributeValue("confidence"));
            for (Object o : elem.content()) {
                Node e = (Node) o;
                if ("input".equals(e.getName())) {
                    Element element = (Element) e;
                    this.utterance = (String) element.getData();
                }
            }
        }

        public String getGrammar_id() {
            return grammar_id;
        }

        public String getUtterance() {
            return utterance;
        }

        public int getConfidence() {
            return confidence;
        }
    }


}

