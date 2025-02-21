/*
 * Copyright(c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging;

import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.util.List;
import java.util.LinkedList;

/**
 * A Log4J filter, using the session information stored by the Log4JLogger class
 * in order to decide if a log entry shall be forwarded in the logging chain or not.
 *
 * @author David Looberger
 */
public class SessionFilter extends Filter {
    class MdcItem {
        String name;
        String value;

        public MdcItem(String name, String value) {
            this.name = name;
            this.value = value;
        }

        boolean equals(String name, String value) {
            return this.name.equals(name) && this.value.equals(value);
        }
    }

    static List<MdcItem> mdcItems = new LinkedList<MdcItem>();

    public SessionFilter() {
        mdcItems.clear();
    }

    /**
     *
     * @param event
     * @return Indication for the filter chain in Log4J whether the log item should be dropped or not.
     * Should be NEUTRAL, ACCEPT or DENY.
     */
    public int decide(LoggingEvent event) {
        int result = NEUTRAL; // If no session id is associated with the log item,
        Level level = event.getLevel();
        if (level == Level.ERROR || level == Level.FATAL ) // Always pass through error and fatal events
            return result;

        if (!acceptMdcEntry(event)) {
            result = DENY;
        } else result = ACCEPT;
        return result;
    }

    public boolean acceptMdcEntry(LoggingEvent event) {
        boolean nameMatch = false;
        if (mdcItems.size() == 0) {
            return true;
        }
        for (MdcItem mdcItem : mdcItems) {
            String value = (String)event.getMDC(mdcItem.name);
            if (value != null) {
                nameMatch = true;
                if (value.equals(mdcItem.value)) {
                    // Found a matching value
                    return true;
                }
            }
        }
        return !nameMatch;
    }

    public void setTraceItem(String traceItem) {
        traceItem = traceItem.trim();
        if (traceItem.length() > 0 && traceItem.contains(":")) {
            int separator = traceItem.indexOf(':');
            String name = traceItem.substring(0,separator);
            String value = traceItem.substring(separator+1);
            mdcItems.add(new MdcItem(name, value));
        }
    }
}
