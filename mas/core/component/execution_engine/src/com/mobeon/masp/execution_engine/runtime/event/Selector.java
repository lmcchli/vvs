/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import java.util.Map;

public abstract class Selector {
    protected Kind kind = Kind.EXACT;

    //TODO: Replace with some kind of cache, as it is it can grow out of bounds !!!

    protected String prefix = null;
    protected String wildcard;
    private int dots = 0;

    public abstract Selector create(String event);

    public abstract Map<String, Selector> getSelectors();

    protected Selector parseEvent(String event) {
        Selector result = getSelectors().get(event);
        if (result == null) {
            result = getSelectors().get(event.trim());
            if (result != null) {
                getSelectors().put(event, result);
            } else {
                result = create(event);
                getSelectors().put(event, result);
            }
        }
        return result;
    }


    enum Kind {
        PREFIX_MAYBE_REGEX,PREFIX_AND_REGEX,EXACT,REGEX }

    public boolean match(String event) {
        switch (kind) {
            case EXACT:
                if (event.equals(prefix))
                    return true;
                break;
            case PREFIX_MAYBE_REGEX:
                if(event.length() == prefix.length() && prefix.equals(event))
                    return true;
                if (event.startsWith(prefix)) {
                    String suffix = event.substring(prefix.length());
                    if(suffix.matches(wildcard))
                        return true;
            }
                break;
            case PREFIX_AND_REGEX:
                if(event.length() == prefix.length() && prefix.equals(event))
                    return false;
                if (event.startsWith(prefix)) {
                    String suffix = event.substring(prefix.length());
                    if(suffix.matches(wildcard))
                        return true;
            }
                break;
            case REGEX:
                if (event.matches(wildcard))
                    return true;
                break;
            default:
        }
        return false;
    }

    protected Selector() {};

    protected String toJavaRegex(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            //Most common metachar
            if (c == '.') {
                sb.append("\\.");
                dots++;
                continue;
            }
            //Simple pattern, convert to a non-greedy re matchh
            if (c == '*') {
                sb.append(".*?");
                continue;
            }
            //Escape other metachars
            if ("([{\\^$|)?+".indexOf(c) >= 0) {
                sb.append("\\");
                sb.append(c);
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public Kind getKind() {
        return kind;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getWildcard() {
        return wildcard;
    }


    public String toString() {
        return "Selector{" +
        "kind=" + kind +
        ", prefix='" + prefix + '\'' +
        ", wildcard='" + wildcard + '\'' +
        '}';
    }

    public boolean equals(Object r) {
        if (r == null || !(r instanceof Selector))
            return false;
        Selector rSel = (Selector) r;
        if (rSel.getPrefix().equals(prefix)
        && rSel.getKind() == kind
        && rSel.getWildcard().equals(rSel.getWildcard()))
            return true;
        else
            return false;
    }

}
