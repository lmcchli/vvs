/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess.util;

import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;
import com.mobeon.masp.profilemanager.greetings.GreetingFormat;

/**
 * Utility class. Used to create a GreetingSpecification from the string parameters that comes from the ECMA application.
 * The GreetingSpecification is then used to find correct greeting from the Profile.
 *
 * @author ermmaha
 */
public class GreetingTypeUtil {

    private final static String CDG = "cdg";
    private final static String VOICE = "voice";
    private final static String VIDEO = "video";

    /**
     * Creates a GreetingSpecification object depending on the parameters.
     *
     * @param greetingType
     * @param mediaType
     * @param cdgNumber
     * @return a new GreetingSpecification object
     */
    public static GreetingSpecification getGreetingSpecification(String greetingType, String mediaType, String cdgNumber, String duration) {
        if (greetingType == null) {
            throw new IllegalArgumentException("greetingType can't be null");
        }

        if (mediaType == null) {
            throw new IllegalArgumentException("mediaType can't be null");
        }

        GreetingSpecification greetingSpec = new GreetingSpecification();
        greetingSpec.setType(greetingType);
        greetingSpec.setFormat(getGreetingFormat(mediaType));
        greetingSpec.setDuration(duration);

        if (cdgNumber != null && cdgNumber.length() > 0) {
            if (greetingType.equals(CDG)) {
                greetingSpec.setSubId(cdgNumber);
            }
        }
        return greetingSpec;
    }

    /**
     * Retrieves a GreetingFormat enum. Can be one of GreetingFormat.VOICE or GreetingFormat.VIDEO
     *
     * @param mediaType ("voice" or "video")
     * @return GreetingFormat enum
     */
    public static GreetingFormat getGreetingFormat(String mediaType) {
        GreetingFormat greetingFormat;
        if (mediaType.equals(VOICE)) {
            greetingFormat = GreetingFormat.VOICE;
        } else if (mediaType.equals(VIDEO)) {
            greetingFormat = GreetingFormat.VIDEO;
        } else {
            throw new IllegalArgumentException("Invalid mediaType " + mediaType);
        }
        return greetingFormat;
    }
}
