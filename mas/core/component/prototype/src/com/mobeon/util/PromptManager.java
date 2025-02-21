/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.util;

import com.mobeon.application.graph.Node;
import com.mobeon.application.graph.PromptNode;
import com.mobeon.executor.Traverser;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: qdalo
 * Date: 2005-feb-11
 * Time: 11:01:31
 * To change this template use File | Settings | File Templates.
 */
public class PromptManager {
    private static Logger logger = Logger.getLogger("mobeon.com");
    private static HashMap promptList;

    public static int ENGLISH;

    static {
        promptList = new HashMap();
        promptList.put("GOODMORNING",new String("sound/goodmorning.wav"));
        promptList.put("WELCOME",new String("sound/UM_1371.wav"));
        promptList.put("YOU_HAVE",new String("sound/UM_3847.wav"));
        promptList.put("NEW_MESSAGES",new String("sound/UM_0336.wav"));

        promptList.put("ONE",new String("sound/one_UL.wav"));
        promptList.put("TWO",new String("sound/two_UL.wav"));
        promptList.put("THREE",new String("sound/three_UL.wav"));
        promptList.put("FOUR",new String("sound/four_UL.wav"));
        promptList.put("FIVE",new String("sound/five_UL.wav"));
        promptList.put("SIX",new String("sound/six_UL.wav"));
        promptList.put("SEVEN",new String("sound/seven_UL.wav"));
        promptList.put("EIGHT",new String("sound/eight_UL.wav"));
        promptList.put("NINE",new String("sound/nine_UL.wav"));
        promptList.put("1",new String("sound/one_UL.wav"));
        promptList.put("2",new String("sound/two_UL.wav"));
        promptList.put("3",new String("sound/three_UL.wav"));
        promptList.put("4",new String("sound/four_UL.wav"));
        promptList.put("5",new String("sound/five_UL.wav"));
        promptList.put("6",new String("sound/six_UL.wav"));
        promptList.put("7",new String("sound/seven_UL.wav"));
        promptList.put("8",new String("sound/eight_UL.wav"));
        promptList.put("9",new String("sound/nine_UL.wav"));

        promptList.put("THANK_YOU",new String("sound/UM_4054.wav"));
        promptList.put("NO_SUCH_MSG",new String("sound/UM_0252.wav"));
        promptList.put("LEAVE_MSG_AFTER_BEEP",new String("sound/UM_0936.wav"));
        promptList.put("LISTEN_TO_YOUR_MSGS",new String("sound/UM_4107.wav"));
        promptList.put("BEEP",new String("sound/UM_4516.wav"));
        promptList.put("NOT_VALID",new String("sound/UM_0246.wav"));
        promptList.put("SILENCE_1SEC",new String("sound/UM_1441.wav"));

    }

    private static PromptManager ourInstance = new PromptManager();

    public static PromptManager getInstance() {
        return ourInstance;
    }

    private PromptManager() {
    }

    public static String getMedia(String resourceId, int encoding)  {
        if (!promptList.containsKey(resourceId))  {
            // Should generate some error
            return (String) promptList.get("BEEP");
        }
        else {
            return (String) promptList.get(resourceId);
        }
    }

    public static String toString(int value)  {
        if (value == 1)
           return "ONE";
        if (value == 2)
           return "TWO";
        if (value == 3)
           return "THREE";
        if (value == 4)
           return "FOUR";
        if (value == 5)
           return "FIVE";
        if (value == 6)
           return "SIX";
        if (value == 7)
           return "SEVEN";
        if (value == 8)
           return "EIGHT";
        if (value == 9)
           return "NINE";

        // Fallback
        return "BEEP" ;

    }

    public static final  Node getPromptToReprompt(Traverser traverser) {

        ArrayList list;
        PromptNode nextNode = null ;
        PromptNode repromptNode = null;

        if ((list = (ArrayList) traverser.getFields().getSymbol("MAS_REPROMPT")) != null) {
            for (Iterator it = list.iterator(); it.hasNext(); ) {
                logger.debug("Looking for reprompts...");
                nextNode = (PromptNode) it.next();
                if (nextNode.getCond().isCond(traverser.getEcmaExecutor())) {
                    if (repromptNode == null) {
                        if (nextNode.getCount() <= traverser.getRepromptCount())  {
                            repromptNode = nextNode;
                            logger.debug("Found reprompt candidate node to use!");
                        }
                    } else {
                        if (nextNode.getCount() <= traverser.getRepromptCount() &&
                                nextNode.getCount() > repromptNode.getCount()) {
                            repromptNode = nextNode;
                            logger.debug("Found reprompt candidate node to use!");
                        }
                    }
                }
            }
            if (repromptNode == null)
                logger.error("No repromptnode found!");
            else
                logger.debug("Reprompt node found!");
        }

        return repromptNode;
    }


}
