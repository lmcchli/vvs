/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * Defines a Control Token that is used to control the endpoint of a stream.
 * <p>
 * A  Control Token is represented by
 * <ul>
 * <li>A decimal value.</li>
 * <li>A volume expressed in dBm0 after dropping the sign.</li>
 * <li>A duration expressed in timestamp units.</li>
 * </ul>
 *
 * @author Jörgen Terner
 */
public class ControlToken {
    /**
     * Enumeration of DTMF Control Tokens defined by their decimal encodings
     * as specified in RFC 2833.
     */
    public enum DTMFToken {
        ZERO (0),
        ONE (1),
        TWO (2),
        THREE (3),
        FOUR (4),
        FIVE (5),
        SIX (6),
        SEVEN (7),
        EIGHT (8),
        NINE (9),
        STAR (10),
        HASH (11),
        A (12),
        B (13),
        C (14),
        D (15),
        FLASH (16),
        SILENCE_BETWEEN_TOKENS (-1);

        private final int digit;

        DTMFToken(int digit) {
            this.digit = digit;
        }

        /**
         * @return The decimal encoding of the DTMF token.
         */
        public int digit() {
            return this.digit;
        }
    };

    /** DTMF event. */
    private DTMFToken mToken;
    /** Duration expressed in timestamp units. */
    private int mDuration;
    /** Volume expressed in dBm0 after dropping the sign. */
    private int mVolume;
    
    /**
     * Creates a new token with the given values.
     * 
     * @param token    DTMF event, may not be <code>null</code>.
     * @param volume   Volume expressed in dBm0 after dropping the sign.
     *                 If set to a negative value, a default value will be 
     *                 used.
     * @param duration Duration expressed in timestamp units. If set to 
     *                 a negative value, a default value will be used.
     *                 
     * @throws IllegalArgumentException If <code>token</code> is 
     *         <code>null</code>.
     */
    public ControlToken(DTMFToken token, int volume, int duration) {
        mToken = token;
        mVolume = volume;
        mDuration = duration;
    }

    /**
     * Gets the token.
     *
     * @return The token, can never be <code>null</code>.
     */
    public DTMFToken getToken() {
        return mToken;
    }

    /**
     * Gets the token decimal encoding as specified in RFC 2833.
     *
     * @return The decimal encoding of the token.
     */
    public int getTokenDigit() {
        return mToken.digit();
    }

    /**
     * Gets the volume of the token expressed in dBm0 after dropping the sign.
     * For DTMF digits and other events representable as tones, this is the
     * power level of the tone. For other events it is always zero.
     * <p>
     * If this is a negative value, the volume is unknown and a standard value
     * will be read from configuration.
     *
     * @return The power level of the token, if representable as a tone,
     *         zero otherwise. If < 0, the value is unknown.
     */
    public int getVolume() {
        return mVolume;
    }

    /**
     * Gets the duration of the token in timestamp units.
     * <p>
     * If this is a negative value, the duration is unknown and a standard value
     * will be read from configuration.
     *
     * @return The duration of the token. If < 0, the value is unknown.
     */
    public int getDuration() {
        return mDuration;
    }
    
    /**
     * Converts a digit to a DTMFToken.
     * 
     * @param digit DTMF digit.
     * 
     * @return The corresponding DTMFToken.
     * 
     * @throws IllegalArgumentException If the digit could not be mapped to a 
     *         DTMFToken.
     */
    static DTMFToken toToken(int digit) {
        switch (digit) {
        case 0: 
            return DTMFToken.ZERO;
        case 1:
            return DTMFToken.ONE;
        case 2:
            return DTMFToken.TWO;
        case 3:
            return DTMFToken.THREE;
        case 4:
            return DTMFToken.FOUR;
        case 5:
            return DTMFToken.FIVE;
        case 6:
            return DTMFToken.SIX;
        case 7:
            return DTMFToken.SEVEN;
        case 8:
            return DTMFToken.EIGHT;
        case 9:
            return DTMFToken.NINE;
        case 10:
            return DTMFToken.STAR;
        case 11:
            return DTMFToken.HASH;
        case 12:
            return DTMFToken.A;
        case 13:
            return DTMFToken.B;
        case 14:
            return DTMFToken.C;
        case 15:
            return DTMFToken.D;
        case 16:
            return DTMFToken.FLASH;
        case -1:
            return DTMFToken.SILENCE_BETWEEN_TOKENS;
        default:
            throw new IllegalArgumentException("Could not map digit " + 
                    digit + " to a DTMF token.");
        }
    }
}