/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef CONTROLTOKEN_H_
#define CONTROLTOKEN_H_

#include <base_std.h>
#include <config.h> // For att __EXPORT ska vara definierad (Pointer.h)
#include <ccrtp/rtp.h>
#include "jni.h"

/**
 * Wrapper class for a Java ControlToken that hides the JNI details.
 * 
 * @author Jorgen Terner
 */
class ControlToken
{
private:
    ControlToken(ControlToken& rhs);
    ControlToken& operator=(const ControlToken& rhs);

    /** The DTMF digit as defined in RFC 2833. */
    int mDigit;
    /** Duration expressed in timestamp units. */
    int mDuration;
    /** Volume expressed in dBm0 after dropping the sign. */
    int mVolume;

public:
    /** Digit value representing silence between tokens. */
    static const int SILENCE_BETWEEN_TOKENS;
    static const char* CONTROLTOKEN_CLASSNAME;
    /**
     * Creates a new ControlToken and reads necessary info from the given
     * Java object.
     * 
     * @param controlToken Java object containing the token info.
     * @param env          Reference to Java environment.
     */
    ControlToken(jobject controlToken, JNIEnv* env);

    /**
     * Creates a new ControlToken.
     * 
     * @param digit    The DTMF digit as defined in RFC 2833.
     * @param volume   Volume expressed in dBm0 after dropping the sign.
     * @param duration Duration expressed in timestamp units.
     */
    ControlToken(int digit, int volume, int duration, JNIEnv* env);

    /**
     * Destructor.
     */
    ~ControlToken();

    /**
     * @return The DTMF digit as defined in RFC 2833.
     */
    int getDigit() const;

    /**
     * Gets the volume of the token expressed in dBm0 after dropping the sign.
     * For DTMF digits and other events representable as tones, this is the
     * power level of the tone. For other events it is always zero.
     *
     * @return The power level of the token, if representable as a tone,
     *         zero otherwise.
     */
    int getVolume() const;

    /**
     * Gets the duration of the token in timestamp units.
     *
     * @return The duration of the token.
     */
    int getDuration() const;

    /**
     * @param Latest duration information for this event.
     */
    void setDuration(int duration);
};
#endif /*CONTROLTOKEN_H_*/
