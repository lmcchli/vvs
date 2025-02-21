/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail;

import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.mobeon.ntf.util.GroupedProperties;
import com.mobeon.ntf.util.Logger;
import java.util.*;


/**
 * SystemNotification knows about special emails that are really notifications
 * from the MoIP system to users. A system notification has a unique name, and
 * is identified by a special combination of values in mail headers. The coupling
 * between header values and system notification name is completely
 * configurable.
 *
 * All matching is case-insensitive.
 */
public class SystemNotification {
    /*
     * SystemNotification was designed to use little resources since every mail
     * must be checked. Thus, as much work as possible is put in the
     * constructor; the configuration files are parsed and some time is spend
     * once on building efficient data structures. The key is the use of
     * allHeaders which is a fixed array with all headers needed to identify any
     * of the System Notifications. The email is asked for these headers and the
     * values are (must be) returned in another array of the same size, with
     * each value at the same index as the corresponding header.
     *
     * The MailMatch for a particular system notification contains the indexes
     * and required values of the headers it needs, and can quickly find the
     * real values.
     */
    

    /* matchHeaders contains a list of all headers that is needed by any of the
     * mail matches */
    private final static Logger log = Logger.getLogger(SystemNotification.class);
    String[] matchHeaders= null;
    /* matches is a list of mail matches for all system notifications. */
    private MailMatch[] matches= null;



    /**
     * Creates a new SystemNotification specified by the contents of a
     * GroupedProperties.
     *@param gprops a GroupedProperties that define system notifications and the
     * header values that identify them.
     */
    public SystemNotification(GroupedProperties gprops) {
	int matchNo= 0;
	Vector headersSoFar= new Vector();
	String group;
	
	matches= new MailMatch[gprops.size()];
	for (Enumeration e= gprops.groupNames(); e.hasMoreElements() ;) {
	    group= (String)(e.nextElement());
	    matches[matchNo++]= new MailMatch(group, getMatches(gprops.getProperties(group), headersSoFar));
	}

	matchHeaders= new String[headersSoFar.size()];
	headersSoFar.toArray(matchHeaders);

	log.logMessage("Created SystemNotification: " + this, log.L_VERBOSE);
    }


    /**
     * getSystemNotificationName matches an email against the header signatures
     * of all defined system notifications.
     *@param mail the email to check
     *@return the name of the first matching system notification or null if
     * none was found.
     */
    public String getSystemNotificationName(NotificationEmail mail) throws MsgStoreException {
	int i;
	String[] headerValues= mail.getHeaders(matchHeaders);
	for (i= 0; i < headerValues.length; i++) {
	    if (headerValues[i]!= null) {
		headerValues[i]= headerValues[i].toLowerCase();
	    }
	}

	for (i= 0; i < matches.length; i++) {
	    if (matches[i].match(headerValues)) {
		return matches[i].sysNotifName;
	    }
	}
	return null;
    }


    /**
     *@return a printable representation of all system notifications.
     */
    public String toString() {
	int i;
	String s= "{SystemNotification\n";
	for (i= 0; i < matchHeaders.length; i++) {
	    s+= "\t" + matchHeaders[i] + "\n";
	}
	for (i= 0; i < matches.length; i++) {
	    s+= matches[i];
	}
	return s + "}\n";
    }


    /*
     * getMatches produces an array of HeaderMatch from the Properties.
     *@param props properties with header name-value pairs.
     *@param headersSoFar Vector with all unique headers found in any system
     * notification so far.
     */
    private HeaderMatch[] getMatches(Properties props, Vector headersSoFar) {
	HeaderMatch[] matches= new HeaderMatch[props.size()];
	int ix= 0;
	String header;
	
	for (Enumeration e= props.propertyNames(); e.hasMoreElements();) {
	    header= (String)(e.nextElement());
	    if (!headersSoFar.contains(header.toLowerCase())) {
		headersSoFar.add(header.toLowerCase());
	    }
	    
	    matches[ix++]= new HeaderMatch(headersSoFar.indexOf(header.toLowerCase()),
					   props.getProperty(header).toLowerCase());
	}

	return matches;
    }




    /*
     * HeaderMatch holds the name and the required (substring) value for one mail
     * header in the set of headers that identifies a system notification.
     * condition. The name is represented as an integer that is an index into an
     * array of header names.
     */
    private class HeaderMatch {
	
	public int headerNo;
	public String headerValue;
	
	HeaderMatch(int n, String v) {
	    headerNo= n;
	    headerValue= v;
	}
    }

	

    /*
     * MailMatch represents the set of header matches that together identify one
     * particular system notification.
     */
    private class MailMatch {
	
	public String sysNotifName= null;
	public HeaderMatch[] matches= null;
	
	/*
	 *@param n unique name of the system notification
	 *@param m list of matches required for the system notification with the
	 *name n.
	 */
	public MailMatch(String n, HeaderMatch[] m) {
	    sysNotifName= n;
	    matches= m;
	}

	/*
	 * match checks if this mail match matches the header values from an
	 * email.
	 *@param headerValues list of header values.
	 *@return true iff all my match values match the corresponding value in
	 * val.
	 */
	public boolean match(String[] headerValues) {
	    String val;
	    for (int i= 0; i < matches.length; i++) {
		val= headerValues[matches[i].headerNo];
		if (val == null || val.indexOf(matches[i].headerValue) < 0) {
		    return false;
		}
	    }
	    return true;
	}

	/**
	 *
	 */
	public String toString() {
	    String s= "\t{MailMatch " + sysNotifName + ":";
	    for (int i= 0; i < matches.length; i++) {
		s+= "\t" + matches[i].headerNo + ":" + matches[i].headerValue;
	    }
	    return s + "}\n";
	}
    }
}
