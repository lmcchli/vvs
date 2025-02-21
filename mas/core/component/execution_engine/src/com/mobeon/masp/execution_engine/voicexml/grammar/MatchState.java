/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import java.util.Arrays;
import java.util.List;

public class MatchState {
    private List<DTMF> dtmf_tokens;
    private int current;
    // when in item with quantifier 0- is contained in an one-or one needs to check if last match realy was a match
    private boolean lastMatchWasEmpty = false;

    public MatchState(DTMF[] dtmf) {
        this.dtmf_tokens = Arrays.asList(dtmf);
        this.current = 0;
    }

    public MatchState(String dtmf) {
        this.dtmf_tokens = Arrays.asList(stringToDtmf(dtmf));
    }

    /**
     * retures if the last watch was because of 0 quantifier. Resets lastMatchWasTrue to false
     * @return
     */

    public boolean getLastMatchWasEmpty() {
        boolean tmp = this.lastMatchWasEmpty;
        this.lastMatchWasEmpty = false;
        return tmp;
    }

    public void setLastMatchWasEmpty() {
        this.lastMatchWasEmpty = true;
    }

    public void setLastMatchWasNotEmpty() {
        this.lastMatchWasEmpty = false;
    }

    public int getSize() {
        return dtmf_tokens.size() - current;
    }
                                      
    public void consumeAll() {
       current = dtmf_tokens.size();
    }


    public int getCurrent() {
        return this.current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public boolean hasMoreItems() {
        return getSize() > 0;
    }

    public boolean match(DTMF token) {
        if (dtmf_tokens.get(current) == (token)) {
            current++;
            return true;
        } else {
            return false;

        }
    }

    private  DTMF[] stringToDtmf(String str) {

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
}
