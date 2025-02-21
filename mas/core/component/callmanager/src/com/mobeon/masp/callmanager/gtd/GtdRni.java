package com.mobeon.masp.callmanager.gtd;

import com.mobeon.masp.callmanager.RedirectingParty;

/**
 * This class represents the "RNI" (Redirection Information) field of a GTD.
 */
public class GtdRni {
    private RedirectingParty.RedirectingReason redirectingReason = RedirectingParty.RedirectingReason.UNKNOWN;

    /**
     * Gets the numberCompletion.
     * @return the number completion.
     */
    public RedirectingParty.RedirectingReason getRedirectingReason() {
        return redirectingReason;
    }

    /**
     * Sets the numberCompletion.
     */
    public void setRedirectingReason(RedirectingParty.RedirectingReason redirectingReason) {
        this.redirectingReason = redirectingReason;
    }


    public String toString(){
        return "RedirectingReason: "+redirectingReason;
    }
}