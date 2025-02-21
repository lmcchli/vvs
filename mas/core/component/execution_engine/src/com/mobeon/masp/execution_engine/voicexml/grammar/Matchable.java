/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;
public interface  Matchable {
    public  MatchType match(MatchState dtmf);
}
