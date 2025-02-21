/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.compiler.Constants;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Item extends RuleExpansion {
    private static final ILogger log = ILoggerFactory.getILogger(Item.class);


    // Quantificators
    private Integer min;
    private Integer max;


    public Item(Element node, Map<String, Rule> rule_map) {
        List list = node.content();
        setQuantificator(node);
        for (Object o : list) {
            Node n = (Node) o;
            if (n.getNodeType() == Node.TEXT_NODE) {
                String s = n.getText();

                s = s.trim();
                if ("".equals(s)) continue; // ignore whitespaces and new lines
                content.add(new DTMFToken(s));

            } else if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (Constants.SRGS.ITEM.equals(n.getName())) {
                    content.add(new Item((Element) n, rule_map));
                } else if (Constants.SRGS.RULEREF.equals(n.getName())) {
                    content.add(new RuleRef((Element) n, rule_map));
                } else if (Constants.SRGS.ONEOF.equals(n.getName())) {
                    content.add(new OneOf((Element) n, rule_map));

                }
            }


        }

    }

    private void setQuantificator(Element node) {
        String s = node.attributeValue(Constants.SRGS.REPEAT);

        if (s == null || "".equals(s)) {
            min = max = 1;
            return;
        }
        s = s.trim();

        Pattern p1 = Pattern.compile("([0-9]+)-([0-9]+)");
        Pattern p2 = Pattern.compile("-([0-9]+)");
        Pattern p3 = Pattern.compile("([0-9]+)-");
        Pattern p4 = Pattern.compile("([0-9]+)");

        Matcher m = p1.matcher(s);
        if (m.matches()) {
            min = Integer.parseInt(m.group(1));
            max = Integer.parseInt(m.group(2));
            return;
        }

        m = p2.matcher(s);
        if (m.matches()) {
            min = 0;
            max = Integer.parseInt(m.group(1));
            return;
        }

        m = p3.matcher(s);
        if (m.matches()) {
            min = Integer.parseInt(m.group(1));
            max = Integer.MAX_VALUE;
            return;
        }

        m = p4.matcher(s);
        if (m.matches()) {
            min = max = Integer.parseInt(m.group(1));

        }
    }

    public MatchType match(MatchState ms) {
        MatchType mt = com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;
        MatchType prev = com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;

        boolean all_match = true;
        int current = 0;
        int i;

        // no more to match
        if (!ms.hasMoreItems()) {
            if(min.intValue() == 0)
                return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH;
            return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;
        }


        for (i = 0; i < max.intValue() && ms.hasMoreItems(); i++) {

            current = ms.getCurrent();
            prev = mt;
            mt = this.subMatch(ms);

            if (mt != com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH && mt != com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH) {
                all_match = false;
                break;
            }
        }

        if (mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH) {
            return mt;
        }
        // all matched up or we ran out of DTMF tokens
        if (all_match) {
            //  We matched all upto max quantificator
            if (i == max.intValue()) {
                   if(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH)
                        return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH;
                    else
                        return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH;

            }
            if (i >= min) {
                return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH;
            } else {
            }
            return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH;
        }


        // we have a no match
        //If the previus one were a match, backtrack and return match
        if (prev == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH || prev == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH) {
            //were we in a valid interval last time round
            if (i >= min) {
                ms.setCurrent(current);
                return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH;
            }

        }

        // if NO_MATCH when quantificator min is zero, return a MATCH and set LastMatchWasEmpty flag so
        // OneOf can handle it properly
        if (min.intValue() == 0) {
            ms.setCurrent(current);
            ms.setLastMatchWasEmpty();
            return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH;
        }

        return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;

    }

    /**
     *  Match the whole body of Item. All items must match for match. If any child matches or evals
     * to partial match return partial match.
     * Otherwise return no match
     * @param dtmf
     * @return
     */
    private  MatchType subMatch(MatchState dtmf) {
        int current = dtmf.getCurrent(); // min is zeoro we might need to backtrack
        if (this.content.size() == 0)
            return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;


        MatchType prev = com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;
        MatchType mt = com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;

        for (Matchable m : this.content) {

            prev = mt;
            mt = m.match(dtmf);
            dtmf.setLastMatchWasNotEmpty();
            // if child is not any kind of  match we are done. Analyze the result.
            if (mt !=  com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH && mt != com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH ) {
                // if partial match return partial match. All previus children mathces
               if(mt == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH) {
                    return mt;
               // if previus were a match return partial match
               } else if(prev == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH || prev == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.FULL_MATCH) {
                   return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH;
               } else { // prev = no match from initial value
                   return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;
               }
            }
        }

        // all children matched
        // TODO: return mt or full match?
        return mt;
    }

    public MatchType _old_match(MatchState dtmf) {
        int i = 0;
        int current = dtmf.getCurrent(); // min is zeoo we might need to backtrack
        if (this.content.size() == 0)
            return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;

        MatchType res = com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH;

        // iterate up to max the quantificator without any back tracking
        for (i = 0; i < max; i++) {
            // iterate all children, they could partial match and then match in sequence  and eat up dtmf as we go
            for (Matchable m : this.content) {
                // if more tokens to match exists analyze them
                if (dtmf.getSize() > 0) {
                    res = m.match(dtmf);
                    dtmf.setLastMatchWasNotEmpty();

                } else {

                    if (i >= min && (i == max - 1 || !dtmf.hasMoreItems())) {
                        return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH;
                    }
                    return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH;
                }

                // need to hanlde quantificator zero to many, no match is backtrack and return MATCH
                // set the lastMatchWas empty flag incase the parent is one-of who should treat the Match as NO_MATCH
                if (min == 0 && res == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.NO_MATCH) {
                    dtmf.setCurrent(current);
                    dtmf.setLastMatchWasEmpty();
                    return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH;
                }
                // if a child is no match or partial match we are done.
                // if we have a match, take next child in doc order
                if (res != com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH) {
                    return res;
                }


            }
            // if we did not match upto minimum  quantificator but all children up to now are
            // match then return partial match
            if (i < min - 1 && !dtmf.hasMoreItems() && res == com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.MATCH) { // or Partial match maybe?
                return com.mobeon.masp.execution_engine.voicexml.grammar.MatchType.PARTIAL_MATCH;
            }
        }

        // all children match - return MATCH
        return res;
    }




    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
