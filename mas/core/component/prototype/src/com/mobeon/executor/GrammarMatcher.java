/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor;

import com.mobeon.event.types.MASNoMatch;

import java.util.Iterator;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-28
 * Time: 14:06:23
 * To change this template use File | Settings | File Templates.
 */
public class GrammarMatcher {
    ArrayList rules = new ArrayList();
    public static Logger logger = Logger.getLogger("com.mobeon");


    public GrammarMatcher(ArrayList rules) {
        this.rules.addAll(rules);
    }

    /**
      * Returns null if the matched token is not found in the grammar
      * @param token
      * @param traverser
     * @return
      */
     public String match(String token, Traverser traverser) {
         logger.debug("Matching " + token + "...");
         for (Iterator it = rules.iterator(); it.hasNext(); ){
             if (token.equals((String) it.next()))
                 return token;
         }
         // If we got here we should fire an NOMATCH
        logger.debug("No match, fire NOMATCH");
        traverser.getDispatcher().fire(new MASNoMatch(this));
        return null;
     }

}
