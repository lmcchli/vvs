/*
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

/**
 * A container of CallingParty related information.
 * It extends {@link com.mobeon.masp.callmanager.CallPartyDefinitions}
 * with information specific for a CallingParty.
 *
 * As a container, it provides only setters and getters.
 *
 * This class is thread safe.
 *
 * @author Malin Flodin
 */

public class CallingParty extends CallPartyDefinitions {

    private PresentationIndicator presentationIndicator;
    private NumberCompletion numberCompletion;
    private String fromDisplayName;
    private String fromUser;
    private String pAssertedIdentityDisplayName; // Optional display name for P-Asserted-Identity header in outbound calls
    private String pAssertedIdentitySecondValueDisplayName; // Optional display name for second P-Asserted-Identity header in outbound calls
    private String pAssertedIdentityFirstValue; // Uri for P-Asserted-Identity header in outbound calls
    private String pAssertedIdentitySecondValue; // Uri for second P-Asserted-Identity header in outbound calls

    private CallingParty fromCallingParty = null;
    private CallingParty passertedIdentityCallingParty = null;

    public CallingParty() {
        super();
        presentationIndicator = PresentationIndicator.UNKNOWN;
        numberCompletion = NumberCompletion.UNKNOWN;
        fromDisplayName = null;
        fromUser = null;
        pAssertedIdentityDisplayName = null;
        pAssertedIdentitySecondValueDisplayName = null;
        pAssertedIdentityFirstValue = null;
        pAssertedIdentitySecondValue = null;
    }

    public synchronized PresentationIndicator getPresentationIndicator() {
        return presentationIndicator;
    }

    /**
     * Sets the Presentation Indicator for the party.
     * The Presentation Indicator shows if presentation of the number is
     * allowed, restricted or if the number is unavailable.
     *
     * @param pi The presentation indicator. Possible values are described
     * in {@link
     * com.mobeon.masp.callmanager.CallPartyDefinitions.PresentationIndicator}
     */
    public synchronized void setPresentationIndicator(PresentationIndicator pi) {
        this.presentationIndicator = pi;
    }

    public synchronized NumberCompletion getNumberCompletion() {
        return numberCompletion;
    }

    /**
     * Sets the Number completion for the party.
     * The Number completion shows if the number is complete, incomplete or unknown.
     *
     * @param numberCompletion The number completion. Possible values are described
     * in {@link
     * com.mobeon.masp.callmanager.NumberCompletion}
     */
    public synchronized void setNumberCompletion(NumberCompletion numberCompletion) {
        this.numberCompletion = numberCompletion;
    }

    public String getFromDisplayName() {
        return this.fromDisplayName;
    }

    public void setFromDisplayName(String fromDisplayName) {
        this.fromDisplayName = fromDisplayName;
    }

    public String getFromUser() {
        return this.fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public CallingParty getFromCallingParty() {
        return this.fromCallingParty;
    }

    public void setFromCallingParty(CallingParty fromCallingParty) {
        this.fromCallingParty = fromCallingParty;
    }
    
    public String getPAssertedIdentityDisplayName() {
        return this.pAssertedIdentityDisplayName;
    }

    public void setPAssertedIdentityDisplayName(String pAssertedIdentityDisplayName) {
        this.pAssertedIdentityDisplayName = pAssertedIdentityDisplayName;
    }

    
    public String getPAssertedIdentitySecondValueDisplayName() {
        return pAssertedIdentitySecondValueDisplayName;
    }

    
    public void setPAssertedIdentitySecondValueDisplayName(String pAssertedIdentitySecondValueDisplayName) {
        this.pAssertedIdentitySecondValueDisplayName = pAssertedIdentitySecondValueDisplayName;
    }

    
    public String getPAssertedIdentityFirstValue() {
        return pAssertedIdentityFirstValue;
    }

    
    public void setPAssertedIdentityFirstValue(String pAssertedIdentityFirstValue) {
        this.pAssertedIdentityFirstValue = pAssertedIdentityFirstValue;
    }

    
    public String getPAssertedIdentitySecondValue() {
        return pAssertedIdentitySecondValue;
    }

    
    public void setPAssertedIdentitySecondValue(String pAssertedIdentitySecondValue) {
        this.pAssertedIdentitySecondValue = pAssertedIdentitySecondValue;
    }

    public CallingParty getPAssertedIdentityCallingParty() {
        return this.passertedIdentityCallingParty;
    }

    public void setPAssertedIdentityCallingParty(CallingParty passertedIdentityCallingParty) {
        this.passertedIdentityCallingParty = passertedIdentityCallingParty;
    }

    public String toString() {
        return super.toString() +  ", <PI = " +
                presentationIndicatorToString(getPresentationIndicator()) + ">" +
                ", <NI = " + numberCompletion + ">";
    }
}
