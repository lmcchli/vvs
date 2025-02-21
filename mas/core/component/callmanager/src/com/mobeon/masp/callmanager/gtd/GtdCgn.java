package com.mobeon.masp.callmanager.gtd;

import com.mobeon.masp.callmanager.NumberCompletion;

/**
 * This class represents the "CGN" (calling number) field of a GTD.
 */
public class GtdCgn {
    private NumberCompletion numberCompletion = NumberCompletion.UNKNOWN;

    /**
     * Gets the numberCompletion.
     * @return the number completion.
     */
    public NumberCompletion getNumberCompletion() {
        return numberCompletion;
    }

    /**
     * Sets the numberCompletion.
     */
    public void setNumberCompletion(NumberCompletion numberCompletion) {
        this.numberCompletion = numberCompletion;
    }

    public String toString(){
        return "NumberCompletion: "+numberCompletion;
    }
}
