package com.mobeon.masp.callmanager.gtd;

import com.mobeon.masp.callmanager.NumberCompletion;
import com.mobeon.masp.callmanager.RedirectingParty;

/**
 * This class represents a GTD. There are getters and setters for the
 * implemented parts of the GTD.
 */
public class GtdDescription {

    GtdCgn gtdCgn;
    GtdRni gtdRni;

    /**
     * Returns the number completion of the GTD.
     * @return the number completion. Willl return NumberCompletion.UNKNOWN
     * if no GtdCgn is set in this object.
     */
    public NumberCompletion getCallingPartyCompletion() {
        if(gtdCgn == null) return NumberCompletion.UNKNOWN;
        return gtdCgn.getNumberCompletion();
    }

     /**
     * Returns the Redirecting Reason of the GTD.
     * @return the Redirecting Reason. Willl return RedirectingParty.RedirectingReason.UNKNOWN
     * if no GtdRni is set in this object.
     */
    public RedirectingParty.RedirectingReason getRedirectingPartyRedirectingReason() {
        if(gtdRni == null) return RedirectingParty.RedirectingReason.UNKNOWN;
        return gtdRni.getRedirectingReason();
    }

    /**
     * Sets the GtdCGN
     * @param gtdCgn
     */
    public void setGtdCgn(GtdCgn gtdCgn) {
        this.gtdCgn = gtdCgn;
    }

    /**
     * Sets the GtdRNI
     * @param gtdRni
     */
    public void setGtdRni(GtdRni gtdRni) {
        this.gtdRni = gtdRni;
    }

    public String toString() {
        return "GtdDescription: <GTD_CNG: "+gtdCgn+"> <GTD_RNI: "+gtdRni+">";
    }

    //public String toStringRni() {
    //    return "GtdDescription: <GTD_RNI: "+gtdRni+">";
    //}
}
