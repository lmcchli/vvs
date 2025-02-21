/*
 * Copyright(c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.logging;

import org.apache.log4j.spi.LoggingEvent;

import java.util.LinkedList;
import java.util.List;

/**
 * A file watch dog class responsible for watching the trace session configuration file.
 * Reads the contents of the sessions.cfg file, containing session IDs.
 * The file is periodiacally checked for changes. If changed, the contents is re-read.
 *
 * @author David Looberger
 */
public class TraceSessions {
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

    // Change to ConcurrentHashMap once all are onboard the Java 5 train...
    // protected static ConcurrentHashMap sessionIds = new ConcurrentHashMap();
    static List<MdcItem> mdcItems = new LinkedList<MdcItem>();
//     protected static HashMap sessionIds = new HashMap();

    /**
     * If the session config file has changed since the last visit, re-read its contents, reseting the list
     * of currently traced sessions.
     */
    protected void addTraceItem(String traceItem) {
        traceItem = traceItem.trim();
        if (traceItem.length() > 0 && traceItem.contains(":")) {
            int separator = traceItem.indexOf(':');
            String name = traceItem.substring(0, separator);
            String value = traceItem.substring(separator + 1);
            mdcItems.add(new MdcItem(name, value));
        }
    }

    public void clear() {
        mdcItems.clear();
    }

//    /**
//     * Check whether a specific session is listed among the sessions to trace.
//     * @param id
//     * @return true if the session shall be traced, false otherwise
//     */
//    public boolean containsSessionId(String id) {
//        return sessionIds.containsKey(id);
//    }

    public boolean acceptMdcEntry(LoggingEvent event) {
        boolean nameMatch = false;
        if (mdcItems.size() == 0) {
            return true;
        }
        for (MdcItem mdcItem : mdcItems) {
            String value = (String) event.getMDC(mdcItem.name);
            if (value != null) {
                nameMatch = true;
                if (value.equals(mdcItem.value)) {
                    // Found a matching value
                    return true;
                }
            }
        }
        return false || !nameMatch;
    }
}
