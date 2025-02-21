/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.XPP3Reader;
import org.xmlpull.v1.XmlPullParserException;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.masp.execution_engine.xml.XPP3CompilerReader;
import com.mobeon.masp.execution_engine.Case;

import java.io.StringReader;
import java.io.IOException;

public abstract class GrammarCase extends Case {
    public GrammarCase(String name) {
        super(name);
    }

    protected CompilerElement readDocument(String document) {
            Document doc = null;
            XPP3Reader reader = new XPP3CompilerReader();

            try {
                doc = reader.read(new StringReader(document));
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return (CompilerElement) doc.getRootElement();
        }

    protected DTMF[] stringToDtmf(String str) {
        if ("".equals(str)) {
            return new DTMF[0];
        }
        str = str.trim();
        str = str.replaceAll("\\s", "");
        String [] dtmf_tokens = str.split("(?<!^)(?!$)"); // belive it or not but this splits on empty string
        DTMF [] item = new DTMF[dtmf_tokens.length];
        for (int j = 0; j < dtmf_tokens.length; j++) {
            item[j] = DTMF.getDTMFValue(dtmf_tokens[j]);
        }
        return item;
    }

    protected void uniqeDTMFTest(DTMF [] tokens, Matchable matchable) {
        MatchState ms;
        for (int i = 0; i < tokens.length - 1; i++) {
            DTMF [] sub = subList(tokens, i + 1);
            ms = new MatchState(sub);
            assertTrue(matchable.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH);
        }
        ms = new MatchState(tokens);
        assertTrue(matchable.match(ms) == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH);

    }

    private DTMF [] subList(DTMF[]  list, int i) {
        DTMF [] ret = new DTMF[i];
        for (int k = 0; k < i; k++) {
            ret[k] = list[k];
        }
        return ret;
    }

}
