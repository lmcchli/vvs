/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.abcxyz.services.moip.userinfo;

import com.abcxyz.services.moip.userinfo.UserInfo;
import java.util.*;

public class NotificationFilter {

    public static final int TRANSPORT_MOBILE = 1;
    public static final int TRANSPORT_FIXED = 2;
    public static final int TRANSPORT_IP = 3;

    private static final FilterPart[] defaultParts;
    static {
        defaultParts= new FilterPart[3];
        defaultParts[0]= new FilterPart("1;n;a;evfm;;;997;;;;;OFF;;");
        defaultParts[1]= new FilterPart("1;y;a;s;SMS,EML;slamdown,slamdown;998;;;;;SLAMDOWN;;");
        defaultParts[2]= new FilterPart("1;y;a;p;SMS,EML;faxprintfail,faxprintfail;999;;;;;FAXPRINTFAIL;;");
    }

    private List /* of DeliveryProfile */ _deliveryProfileList;
    private FilterPart[] parts= null;
    private boolean notifDisabled= false;
    private FilterPart match= defaultParts[0];
    private Calendar lastWhen= null;
    private UserInfo user= null;

    /**
     * Constructor that parses the filter strings and prepares to decide about
     * the users notifications.
     * @param partStrings strings specifying the filter parts.
     * @param disabled true iff notification is disabled.
     * @param user information about the user.
     * @param deliveryProfileStrings the emDeliveryProfile values for the user
     */
    public NotificationFilter(String[] partStrings, boolean disabled, UserInfo user,
        String[] deliveryProfileStrings) {

        _deliveryProfileList = new ArrayList();
        this.user=user;

        notifDisabled= disabled;

        if (disabled) return;

        if (partStrings == null) {
            parts= new FilterPart[3];
            parts[0] = defaultParts[0];
            parts[1] = defaultParts[1];
            parts[2] = defaultParts[2];
        } else {
            parts= new FilterPart[partStrings.length + defaultParts.length - 1];
            //defaultPart[0] used in match algorithm
            int i;
            for (i = 0; i < defaultParts.length - 1; i++) {
                parts[i] = defaultParts[i + 1];
            }
            for (i= 0; i < partStrings.length; i++) {
                parts[i + defaultParts.length - 1]= new FilterPart(partStrings[i]);
            }
            try {
                Arrays.sort(parts); //Sort filters after priority
            } catch (ClassCastException e) {
                //This will never happen, since we only add FilterPart objects
            }
        }

        if(deliveryProfileStrings != null) {
            for(int i = 0; i < deliveryProfileStrings.length; i++) {
                _deliveryProfileList.add(
                    new DeliveryProfile(deliveryProfileStrings[i]));
            }
        }
    }

    /**
     * Tells if notification is completely disabled for this user.
     *@return true iff the user has notification disabled.
     */
    public boolean isNotifDisabled() {
        return notifDisabled;
    }

    /**
     * Gets the numbers that NTF will notify for a given notif type
     * and transport. .
     *No check is done if the user would have been notified or not.
     *@param notifType The name of the notification, e.g. "SMS"
     * @param transport The transport to use, e.g. Mobile
     *@return return the numbers to notify to.
     */
    public String[] getNotifNumbers(String notifType, int transport, String subscriberNumber) {
        if (notifDisabled) { return new String[0]; }

        String[] numbers = getMatchingDeliveryProfileNumbers(notifType, transport);
        if( numbers == null ) {
            String notifNumber = user.getNotifNumber();
            if ( notifNumber != null  && !notifNumber.isEmpty() )
                numbers = new String[] { notifNumber };
            else
                numbers = new String[] {subscriberNumber};
        }
        return numbers;
    }

    /**
     * Returns a printable representation of this filter.
     *@return String with the parts constituting this filter.
     */
    public String toString() {
        if (parts == null) return "{NotificationFilter:}";

        String s= "{NotificationFilter:\n";
        for (int i= 0; i < parts.length - defaultParts.length + 1; i++) {
            s+= "  " + parts[i].toString();
        }
        return s + "}";
    }

    /**
     * @return the numbers in matching delivery profiles, null if no profile
     * or the types does not match. No duplicates numbers are returned.
     */
    public String[] getMatchingDeliveryProfileNumbers(String type, int transportType) {
        if(_deliveryProfileList.isEmpty()) return null;

        if (notifDisabled) { return null; }

        Vector temp = new Vector();

        Iterator it = _deliveryProfileList.iterator();
        while( it.hasNext() ) {
            DeliveryProfile profile = (DeliveryProfile) it.next();
            if( profile != null && profile._notifTypes.contains(type)
                    && profile.matchesTransport(transportType)) {
                if(profile._numbers.size() > 0) {
                    for(int i = 0; i < profile._numbers.size(); i++) {
                        String num = (String) profile._numbers.get(i);
                        if( !temp.contains( num ))
                            temp.add( num );
                    }
                }
            }
        }
        if(temp.isEmpty()) return null;

        return (String[]) temp.toArray(new String[temp.size()]);
    }

    /**
     * FilterPart is a simple class that parses data from the string specifying
     * a filter part and provides a structured way to store this data.
     *
     * Note: this class has a natural ordering that is inconsistent with equals.
     * This means that two FilterParts that are not equal can still give the
     * result 0 when compared. This is intentional and is because we want to
     * sort (i.e. compare) objects based only on the prio field.
     */
    private static class FilterPart implements Comparable {
        private static final String encodedSemicolon= "%3b";
        private static final String encodedPercent= "%25";

        public String name;
        public int prio;
        public boolean active;
        public boolean notify;
        public String time;
        public String depType;
        public Properties contentForType;
        public String from;
        public String subject;
        public boolean urgent;
        public String voiceFaxFrom;
        public String readOnly;


        /**
         * Decodes the %3b and %25 special character sequences used to store
         * semicolon and percent signs in the subject part of a filter string.
         *@param encoded the encoded value from the filter string.
         *@return the decoded value.
         */
        private String murDecode(String encoded) {
            int percentIndex;
            String decoded= encoded;

            while((percentIndex= decoded.indexOf(encodedSemicolon)) >= 0) {
                decoded= decoded.substring(0, percentIndex)
                    + ";"
                    + decoded.substring(percentIndex + 3);
            }
            while((percentIndex= decoded.indexOf(encodedPercent)) >= 0) {
                decoded= decoded.substring(0, percentIndex)
                    + "%"
                    + decoded.substring(percentIndex + 3);
            }
            return decoded;
        }

        /**
         * Constructor that parses the filter strings.
         *@param s the string specifying a filter part.
         */
        public FilterPart(String s) {
            StringTokenizer st = new StringTokenizer(s, ";", true);
            active = getBoolean(st, false);
            notify = getBoolean(st, true);
            time = getString(st, "a");
            depType = getString(st, "evfm");
            contentForType = CommaStringTokenizer.getPropertiesFromLists(getString(st, "SMS"), getString(st, ""));
            prio = getInt(st);
            from = getString(st, null);
            if (from != null) from = from.toLowerCase();
            subject = getString(st, null);
            if (subject != null) subject = murDecode(subject.toLowerCase());
            urgent = getBoolean(st, false);
            voiceFaxFrom = getString(st, null);
            name = getString(st, "name");
            readOnly = getString(st, null);
        }

        /**
         * Compares two FilterPart objects, so an array of such objects can be
         * sorted according to priority.
         *@param o FilterPart to compare to.
         *@return &lt;0 if this FilterPart has smaller prio than o<BR>
         *        &gt;0 if this FilterPart has larger prio than o<BR>
         *        0 if this FilterPart and o have the same prio.
         */
        public int compareTo(Object o) {
            return prio - ((FilterPart)o).prio;
        }

        /**
         * Parses a boolean value from the filter string (y and 1 is true).
         *@param st a StringTokenizer that is eating its way through the string.
         *@param def a default value to return if the field is empty.
         *@return a boolean from the string.
         */
        private boolean getBoolean(StringTokenizer st, boolean def) throws NoSuchElementException {
            String s = getString(st, null);
            if (s == null) {
                return def;
            } else {
                return (s.equalsIgnoreCase("y") ||
                        s.equals("1"));
            }
        }

        /**
         * Parses an integer value from the filter string.
         *@param st a StringTokenizer that is eating its way through the string.
         *@return an integer from the string.
         */
        private int getInt(StringTokenizer st) throws NoSuchElementException {
            String s = getString(st, "0");
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        /**
         * Parses a string value from the filter string;
         *@param st a StringTokenizer that is eating its way through the string.
         *@param def a default value to return if the field is empty.
         *@return a string from the string.
         */
        private String getString(StringTokenizer st, String def) {
            String s;
            try {
                s = st.nextToken();
                if (";".equals(s)) {
                    s = def;
                } else {
                    try {
                        st.nextToken(); //Discard the delimiter
                    } catch (NoSuchElementException e) { ; }
                }
            } catch (NoSuchElementException e) {
                s = def;
            }
            return s;
        }

        /**
         * Returns a printable representation of this FilterPart
         *@return This FilterPart as a String
         */
        public String toString() {
            return "{FilterPart:"
                + " name=" + name
                + ",prio=" + prio
                + ",active=" + active
                + ",notify=" + notify
                + ",time=" + time
                + ",depType=" + depType
                + ",contentForType=" + contentForType
                + ",from=" + from
                + ",subject=" + subject
                + ",urgent=" + urgent
                + ",voiceFaxFrom=" + voiceFaxFrom
                + ",readonly=" + readOnly
                + "}";
        }
    }

   /**
    * Models the Delivery Profile which is used by the Notification Filters.
    * A Profile contains numbers and notification types.
    */
   class DeliveryProfile {

       /**
        * List of numbers (Strings)
        */
       public List _numbers;
       /**
        * notificationtype list (Strings)
        */
       public List _notifTypes;
       /**
        * False if we know the number is fixed, true otherwise
        */
       public boolean _mobile = false;
       public boolean _fixed = false;
       public boolean _ip = false;

       /**
        * Creates a DeliveryProfile from the string in MUR
        */
       public DeliveryProfile(String s) {
           StringTokenizer st = new StringTokenizer(s, ";", true);
           getNumbers(getString(st, ""));
           getNotficationTypes(getString(st, ""));
           getMobileInfo(getString(st, "M"));
       }

       private void getNumbers(String commaString) {
           _numbers = new ArrayList();
           if (commaString.length() == 0) return; //no numbers

           StringTokenizer z = new StringTokenizer(commaString, ",");
           while (z.hasMoreElements()) {
               _numbers.add(z.nextToken());
           }
       }

       private void getNotficationTypes(String commaString) {
           _notifTypes = new ArrayList();
           if (commaString.length() == 0) return; //no types

           StringTokenizer z = new StringTokenizer(commaString, ",");
           while (z.hasMoreElements()) {
               _notifTypes.add(z.nextToken());
           }
       }

       private void getMobileInfo(String info) {
           info = info.toUpperCase();
           if (info.contains("F")) {
               _fixed = true;
           }
           if (info.contains("M")) {
               _mobile = true;
           }
           if (info.contains("I")) {
               _ip = true;
           }


       }

       private boolean matchesTransport( int transportType ) {
           if( transportType == TRANSPORT_MOBILE && _mobile ) {
               return true;
           }
           if( transportType == TRANSPORT_FIXED && _fixed ) {
               return true;
           }
           if( transportType == TRANSPORT_IP && _ip ) {
               return true;
           }
           if( transportType == 0 ) {
               return true;
           }
           return false;
       }

       private String getString(StringTokenizer st, String def) {
           String s;
           try {
               s = st.nextToken();
               if (";".equals(s)) {
                   s = def;
               } else {
                   try {
                       st.nextToken(); //Discard the delimiter
                   } catch (NoSuchElementException e) {
                       ;
                   }
               }
           } catch (NoSuchElementException e) {
               s = def;
           }
           return s;
       }
   }
}
