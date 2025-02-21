/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.xmp.server;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Information about the component, counters etc.
 */
public class ComponentInfo {
    /** All named counters known by this program. */
    private static Hashtable /* of Counter keyed by String */ counters = new Hashtable();

    /**
     * Get a counter by name, creating if necessary.
     *@param name the counter name.
     *@return the Counter.
     */
    public static Counter getCounter(String name) {
        Counter c;
        c = (Counter) (counters.get(name));
        if (c == null) {
            c = new Counter();
            counters.put(name, c);
        }
        return c;
    }

    /**
     * Report all component information as an HTML table.
     *@return A string with all component info.
     */
    public static String htmlReport() {
        StringBuffer sb = new StringBuffer();
        sb.append("<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=3>\n");
        for (Enumeration e = counters.keys(); e.hasMoreElements();) {
            String key = (String) (e.nextElement());
            sb.append("<TR><TD>").append(key).append("</TD><TD>");
            sb.append(((Counter) (counters.get(key))).getValue());
            sb.append("</TD></TR>").append("\n");
        }
        sb.append("</TABLE>\n");
        return sb.toString();
    }

    /**
     * Report all component information.
     *@return a string with all component information.
     */
    public static String report() {
        StringBuffer sb = new StringBuffer();
        for (Enumeration e = counters.keys(); e.hasMoreElements();) {
            sb.append(" ");
            String key = (String) (e.nextElement());
            sb.append(key).append("=");
            sb.append(((Counter) (counters.get(key))).getValue());
        }
        sb.append("\n");
        return sb.toString();
    }
}
