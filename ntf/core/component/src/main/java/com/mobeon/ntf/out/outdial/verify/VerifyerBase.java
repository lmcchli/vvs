package com.mobeon.ntf.out.outdial.verify;

/**
 * This interface describes the base for all outdial verifyers.
 */
public interface VerifyerBase {

    // Reporting levels

    static final int LEVEL_ERROR  = 0;
    static final int LEVEL_HIGH   = 10;
    static final int LEVEL_MEDIUM = 20;
    static final int LEVEL_LOW    = 30;
    static final int LEVEL_ALL    = 100000;

    /**
     * Do a validation on the given level.
     */
    public void validate(int level);

}
