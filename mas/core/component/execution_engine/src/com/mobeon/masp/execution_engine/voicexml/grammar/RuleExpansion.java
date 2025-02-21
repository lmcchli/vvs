/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;

import java.util.List;
import java.util.ArrayList;



public abstract class RuleExpansion implements Matchable{
    private final ILogger log = ILoggerFactory.getILogger(RuleExpansion.class);

    protected List<Matchable> content = new ArrayList<Matchable>();

    public RuleExpansion() {

    }


}
