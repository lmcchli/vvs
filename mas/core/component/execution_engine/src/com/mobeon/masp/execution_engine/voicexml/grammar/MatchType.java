/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;
/**
 * NO_MATCH means nothing matchers
 * PARTIAL_MATCH means, start to match but does not completet
 * MATCH means that all DTMF sent to the grammar matches
 * FULL_MATCH means that all DTMF sent to the grammar matches AND no more DTFM is valid
 */

public enum MatchType {
    NO_MATCH, PARTIAL_MATCH, MATCH, FULL_MATCH
}
