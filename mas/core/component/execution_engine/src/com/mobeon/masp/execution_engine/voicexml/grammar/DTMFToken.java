/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * A CDATA part of a SRGS grammar. A space separated List of DTMF
 */
public class DTMFToken implements Matchable {
    private final ILogger log = ILoggerFactory.getILogger(DTMFToken.class);
    private DTMF [] dtmf_seq;


    private DTMF[] stringToDtmf(String str) {
        str = str.trim();
        str = str.replaceAll("\\s", "");
        String [] dtmf_tokens = str.split("(?<!^)(?!$)"); // belive it or not but this splits on empty string
        DTMF [] item = new DTMF[dtmf_tokens.length];
        for (int j = 0; j < dtmf_tokens.length; j++) {
            item[j] = DTMF.getDTMFValue(dtmf_tokens[j]);
        }
        return item;
    }

    public DTMFToken(String dtmf) {
        dtmf_seq = stringToDtmf(dtmf);
    }

    public DTMF[] getDtmf_seq() {
        return dtmf_seq;
    }


    /**
     * if this matches the beginning of arg dtmf , return MATCH
     * if arg dtmf is shorter then this but sub matches, return partial match
     * otherwise return NO_MATCH
     * @param dtmf
     * @return
     */
    public MatchType match(MatchState dtmf) {

        int size = dtmf.getSize();
        int i;
        if (size == 0) {
            return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;
        }

        // which is shorter
        int nof_iters = Math.min(this.dtmf_seq.length ,dtmf.getSize());
        for (i = 0; i < nof_iters; i++) {
            if (!dtmf.match(this.dtmf_seq[i])) {
                return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;
            }
        }



        // we did match the rule but have more incoming dtmf to match
        if(i == this.dtmf_seq.length)
                   return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH;
        else
            return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH;

    }

}
