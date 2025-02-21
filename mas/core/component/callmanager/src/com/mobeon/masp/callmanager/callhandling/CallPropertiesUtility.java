/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.CallPartyDefinitions;
import com.mobeon.masp.callmanager.sip.SipConstants;

/**
 * Utility class containing static helper methods related to call properties.
 * @author Malin Flodin
 */
public class CallPropertiesUtility {

    /**
     * Asserts that the given call properties are set correctly according to
     * description in {@link com.mobeon.masp.callmanager.CallProperties}.
     * @param callProperties
     * @throws IllegalArgumentException if call properties is null, or if a
     * mandatory part of the call properties is not set.
     */
    public static void assertCallPropertiesSetCorrectly(CallProperties callProperties) {
        if (callProperties == null) {
            throw new IllegalArgumentException(
                    "Call properties must not be null");
        } else {
            assertCalledPartySetCorrectly(callProperties.getCalledParty());
            assertCallingPartySetCorrectly(callProperties.getCallingParty());
            assertMaxCallDurationBeforeConnected(
                    callProperties.getMaxDurationBeforeConnected());
        }
    }

    /**
     * Asserts that the given call properties contains a correct called party.
     * @param callProperties
     * @throws IllegalArgumentException if the call properties is null, or if
     * the called party is not set.
     */
    public static void assertCalledPartySetCorrectly(CallProperties callProperties) {
        if (callProperties == null) {
            throw new IllegalArgumentException(
                    "Call properties must not be null");
        } else {
            assertCalledPartySetCorrectly(callProperties.getCalledParty());
        }
    }

    /**
     * Asserts that the given call properties contains a correct calling party.
     * @param callProperties
     * @throws IllegalArgumentException if the call properties is null, or if
     * the calling party is not set.
     */
    public static void assertCallingPartySetCorrectly(CallProperties callProperties) {
        if (callProperties == null) {
            throw new IllegalArgumentException(
                    "Call properties must not be null");
        } else {
            assertCallingPartySetCorrectly(callProperties.getCallingParty());
        }
    }

    /**
     * Asserts that the given call properties contains a correct call duration
     * before connected value.
     * @param callProperties
     * @throws IllegalArgumentException if the call properties is null, or if
     * the max call duration before connected value is not set.
     */
    public static void assertMaxCallDurationBeforeConnected(CallProperties callProperties) {
        if (callProperties == null) {
            throw new IllegalArgumentException(
                    "Call properties must not be null");
        } else {
            assertMaxCallDurationBeforeConnected(
                    callProperties.getMaxDurationBeforeConnected());
        }
    }

    private static void assertMaxCallDurationBeforeConnected(
            int maxDurationBeforeConnected) {
        if (maxDurationBeforeConnected <=0) {
            throw new IllegalArgumentException("Max call duration before " +
                    "connected must be set.");
        }
    }

    private static void assertCalledPartySetCorrectly(CalledParty calledParty) {
        if (calledParty == null) {
            throw new IllegalArgumentException("Called party must not be null.");
        } else if (callPartyIsEmtpy(calledParty))  {
            throw new IllegalArgumentException("At least one representation " +
                    "of the Called party must be set.");
        }
    }

    private static void assertCallingPartySetCorrectly(CallingParty callingParty) {
        if (callingParty == null) {
            throw new IllegalArgumentException("Calling party must not be null.");
        } else if (callPartyIsEmtpy(callingParty))  {
            throw new IllegalArgumentException("At least one representation " +
                    "of the Calling party must be set.");
        }
    }

    private static boolean callPartyIsEmtpy (CallPartyDefinitions callParty) {
        return ((callParty.getUri() == null || callParty.getUri().equals("")) &&
                (callParty.getSipUser() == null || callParty.getSipUser().equals("")) &&
                (callParty.getTelephoneNumber() == null || callParty.getTelephoneNumber().equals("")));
    }

    public static void assertPortSetCorrectly(CallProperties callProperties) {
        if(callProperties.isOutboundCallServerPortSet()){
            int outboundCallServerPort = callProperties.getOutboundCallServerPort();
            if(outboundCallServerPort < 0 || outboundCallServerPort > SipConstants.MAX_PORT)
             throw new IllegalArgumentException("Outbound call server port can not be "
                     +outboundCallServerPort);
        }
    }
}
