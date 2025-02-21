/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework;

import java.util.ArrayList;
import java.util.List;

/**
 * The NotifierIncomingSignalResponse class is used by the Notifier plug-in to communicate with the NTF component about 
 * its handling of the incoming notification signal.
 * <p>
 * Possible incoming signal processing scenarios:
 * <table border="2" summary="">
 * <tr> <th>Signal processed by Notifier plug-in</th> <th>NTF component action</th> </tr>
 * <tr> <td>Yes</td> <td>Refer to the Notifier plug-in handling set in this response (see possible incoming signal handling scenarios in table below)</td> </tr>
 * <tr> <td>No</td> <td>Re-send signal to Notifier plug-in later (according to NTF retry schema)</td> </tr>
 * </table> 
 * <p>
 * Possible incoming signal handling scenarios:
 * <table border="2" summary="">
 * <tr> <th>Notifier plug-in handling </th> <th>NTF component action</th> </tr>
 * <tr> <td>Not handling signal</td> <td>Handle all notifications that should be sent for the signal</td> </tr>
 * <tr> <td>Handle signal completely</td> <td>No further processing of signal</td> </tr>
 * <tr> <td>Handle signal for specified notifications</td> <td>Handle any other notifications that should be sent for the signal</td> </tr>
 * </table>
 */
public class NotifierIncomingSignalResponse {
    
    /**
     * The NotifierHandlingAction to be executed by the NTF component as a result of the Notifier plug-in's ability to process the incoming signal.
     */
    private NotifierHandlingActions notifierHandlingAction = null;

    /**
     * The list of NotifierHandlingTypes representing the notification types handled by the Notifier plug-in for the incoming signal.
     */
    private List<NotifierHandlingTypes> list = null;

    /**
     * The NotifierHandlingActions enum contains the possible actions that the NTF component can execute as a result of the Notifier plug-in's ability to process the incoming signal.
     */
    public enum NotifierHandlingActions {

        /**
         * Notifier plug-in has processed the incoming signal; NTF component to refer to list of NotifierHandlingTypes in this response.
         */
        OK      ("notifierHandlingAction OK"),
        /**
         * Notifier plug-in has not processed the incoming signal; NTF component to retry sending the signal to the plug-in.
         */
        RETRY   ("notifierHandlingAction RETRY");

        private String name;

        NotifierHandlingActions(String name) {
            this.name = name;
        }

        /**
         * Gets the name of the enum constant.
         * @return the name of the enum constant
         */
        public String getName() {
            return name;
        }
    }

    /**
     * The NotifierHandlingTypes enum contains the possible types of handling by the Notifier plug-in for the incoming signal.
     * <p>
     * The Notifier plug-in handling of the signal can range from no handling to complete handling.
     */
    public enum NotifierHandlingTypes {

        /**
         * Signal is not handled by Notifier plug-in.
         */
        NONE ("none"),
        /**
         * Signal is handled completely by Notifier plug-in.
         */
        ALL  ("all"),
        /**
         * Signal is handled by Notifier plug-in for the SMS notification.
         */
        SMS  ("sms");

        private String name;

        NotifierHandlingTypes(String name) {
            this.name = name;
        }

        /**
         * Gets the name of the enum constant.
         * @return the name of the enum constant
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Constructs an instance of NotifierIncomingSignalResponse that has NotifierHandlingActions.OK set as the notifierHandlingAction value
     * and an empty list of NotifierHandlingTypes.
     * <p>
     * These default values would indicate to the NTF component that the Notifier plug-in is not handling the incoming signal. 
     */
    public NotifierIncomingSignalResponse() {
        list = new ArrayList<NotifierHandlingTypes>();
        notifierHandlingAction = NotifierHandlingActions.OK;
    }

    /**
     * Sets the action to be executed by the NTF component as a result of the Notifier plug-in's ability to process the incoming signal.
     * @param notifierHandlingAct the action to be executed by the NTF component
     */
    public void setAction(NotifierHandlingActions notifierHandlingAct) {
        this.notifierHandlingAction = notifierHandlingAct;
    }

    /**
     * Determines whether the specified notifierHandlingAct is the set notifierHandlingAction value for this response.
     * @param notifierHandlingAct the action for which to verify
     * @return true if the specified notifierHandlingAct is the set notifierHandlingAction value for this response; false otherwise
     */
    public boolean isAction(NotifierHandlingActions notifierHandlingAct) {
        return this.notifierHandlingAction == notifierHandlingAct;
    }

    /**
     * Adds a NotifierHandlingTypes enum constant to the list of notification types handled by the Notifier plug-in for the incoming signal.
     * <p>
     * Note that this list of NotifierHandlingTypes will be considered by the NTF component only if NotifierHandlingActions.OK 
     * is set as the notifierHandlingAction value in this response.
     * <p>
     * Currently, the available NotifierHandlingTypes enum constants (<code>ALL</code>, <code>NONE</code>, <code>SMS</code>) are mutually exclusive.  
     * Therefore, the NTF component will use only the first NotifierHandlingTypes enum constants in the list.
     * @param notifierHandlingType the NotifierHandlingTypes enum constant to add
     */
    public void addHandlingType(NotifierHandlingTypes notifierHandlingType) {
        list.add(notifierHandlingType);
    }

    /**
     * Gets the list of NotifierHandlingTypes which represents the notification types handled by the Notifier plug-in for the incoming signal.
     * @return the list of NotifierHandlingTypes handled by the Notifier plug-in for the incoming signal
     */
    public List<NotifierHandlingTypes> getHandlingType() {
        return list;
    }

    /**
     * Determines whether the specified notifierHandlingType is in the list of NotifierHandlingTypes handled by the Notifier plug-in for the incoming signal.
     * @param notifierHandlingType the type for which to verify
     * @return true if the specified notifierHandlingType is in the list of NotifierHandlingTypes; false otherwise
     */
    public boolean containsHandlingType(NotifierHandlingTypes notifierHandlingType) {
        return list.contains(notifierHandlingType);
    }

    /**
     * Returns the string representation of this response.
     * This consists of the notifierHandlingAction and list of NotifierHandlingTypes for this response.
     * @return the string representation of this response
     */
    public String toString() {
        return notifierHandlingAction.getName() + ", notificationHandlingType " + list.toString();
    }
}
