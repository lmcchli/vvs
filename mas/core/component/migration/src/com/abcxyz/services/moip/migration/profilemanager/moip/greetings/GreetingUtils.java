/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip.greetings;

import jakarta.activation.MimeType;

import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;
import com.mobeon.masp.profilemanager.greetings.GreetingType;

/**
 * Utility class for greetings
 *
 * @author mande
 */
public class GreetingUtils {

    public static String getDispositionString(GreetingFormat format) {
        return "inline; " + getDisposition(format);
    }

    public static String getFormatHeader(GreetingFormat format) {
        switch (format) {
            case VOICE: return "voice";
            case VIDEO: return "video";
            default:    return "unknown format";
        }
    }

    public static String getTypeHeader(GreetingSpecification specification) {
        checkArgs(specification);
        switch (specification.getType()) {
            case ALL_CALLS:        return "AllCalls";
            case BUSY:             return "Busy";
            case CDG:              return "CDG" + specification.getSubId() + "#";
            case EXTENDED_ABSENCE: return "Extended_Absence";
            case NO_ANSWER:        return "NoAnswer";
            case OUT_OF_HOURS:     return "OutOfHours";
            case OWN_RECORDED:     return "OwnRecorded";
            case TEMPORARY:        return "Temporary";
            case SPOKEN_NAME:      return "SpokenName";
            default:               return "Unknown GreetingType";
        }
    }


    public static String getSubjectString(GreetingSpecification specification) {
        checkArgs(specification);
        if (specification.getType() == GreetingType.DIST_LIST_SPOKEN_NAME) {
            return getSubjectTypeString(specification);
        } else {
            return getSubjectTypeString(specification) + " " + getSubjectFormatString(specification.getFormat());
        }
    }

    private static String getSubjectTypeString(GreetingSpecification specification) {
        switch (specification.getFormat()) {
            case VOICE: return getVoiceSubjectString(specification);
            case VIDEO: return getVideoSubjectString(specification);
            default:    return "Unknown GreetingFormat";
        }
    }

    private static String getVideoSubjectString(GreetingSpecification specification) {
        switch (specification.getType()) {
            case ALL_CALLS:        return "All Calls";
            case BUSY:             return "Occupied";
            case CDG:              return "This is your greeting for " + specification.getSubId();
            case EXTENDED_ABSENCE: return "Extended Absence";
            case NO_ANSWER:        return "No Answer";
            case OUT_OF_HOURS:     return "Out Of Hours";
            case OWN_RECORDED:     return "OwnRecorded";
            case TEMPORARY:        return "Temporary";
            case SPOKEN_NAME:      return "Spoken Name";
            default:               return "Unknown GreetingType";
        }
    }

    private static String getVoiceSubjectString(GreetingSpecification specification) {
        switch (specification.getType()) {
            case ALL_CALLS:             return "AllCalls";
            case BUSY:                  return "Busy";
            case CDG:                   return "This is your greeting for " + specification.getSubId();
            case EXTENDED_ABSENCE:      return "Extended_Absence";
            case NO_ANSWER:             return "NoAnswer";
            case OUT_OF_HOURS:          return "OutOfHours";
            case OWN_RECORDED:          return "OwnRecorded";
            case TEMPORARY:             return "Temporary";
            case SPOKEN_NAME:           return "SpokenName";
            case DIST_LIST_SPOKEN_NAME: return specification.getSubId();
            default:                    return "Unknown GreetingType";
        }
    }

    private static String getSubjectFormatString(GreetingFormat format) {
        switch (format) {
            case VOICE: return "(voice)";
            case VIDEO: return "(video)";
            default:    return "(unknown format)";
        }
    }

    public static String getGreetingFileName(GreetingSpecification specification, MimeType contentType) {
        checkArgs(specification);
        return getFileName(specification) + getFileExtension(specification.getFormat(), contentType);
    }

    private static String getFileName(GreetingSpecification specification) {
        switch (specification.getType()) {
            case ALL_CALLS:             return "allcalls";
            case BUSY:                  return "busy";
            case CDG:                   return "cdg" + specification.getSubId();
            case EXTENDED_ABSENCE:      return "extendedabsence";
            case NO_ANSWER:             return "noanswer";
            case OUT_OF_HOURS:          return "outofhours";
            case OWN_RECORDED:          return "ownrecorded";
            case TEMPORARY:             return "temporary";
            case SPOKEN_NAME:           return "spokenname";
            case DIST_LIST_SPOKEN_NAME: return "distlistspokenname" + specification.getSubId();
            default:                    return "unknown";
        }
    }


    private static String getFileExtension(GreetingFormat format, MimeType contentType) {
        if (format == GreetingFormat.VOICE) {
            if (contentType != null) {
            	//BaseType() shows the type string without any parameters (before ; if any).
            	//do not use toString as returns the parameters also.
                if (contentType.getBaseType().equalsIgnoreCase("audio/3gpp")) {
                	String codec=contentType.getParameter("codec").toLowerCase();
                	if (codec.contains("sawb")) {
                		//wide band amr
                		//special extension for amr-wb (non-official) makes internal parsing easier
                		//strictly speaking this should be .3gp
                		return ".3gpw";
                	} else {
                		//otherwise assume narrow band,
                		return ".3gp";
                	}
                }
            }
            return ".wav"; //assume wav
        } else {
            if (contentType != null) {
                if (contentType.getBaseType().equalsIgnoreCase("video/3gpp")) { return ".3gp";}
            }
            return ".mov"; //assume mov(quicktime)
        }
    }

    private static String getDisposition(GreetingFormat format) {
        switch (format) {
            case VOICE: return "voice=Greeting-Message";
            case VIDEO: return "video=Greeting-Message";
            default:    return "unknown=Greeting-Message";
        }
    }

    private static void checkArgs(GreetingSpecification specification) {
        switch (specification.getType()) {
            case ALL_CALLS:
            case BUSY:
            case EXTENDED_ABSENCE:
            case NO_ANSWER:
            case OUT_OF_HOURS:
            case OWN_RECORDED:
            case TEMPORARY:
            case SPOKEN_NAME:
                if (specification.getSubId() != null) {
                    throw new IllegalArgumentException("GreetingType " + specification + " should not have subId(s)");
                }
                break;
            case CDG:
            case DIST_LIST_SPOKEN_NAME:
                if (specification.getSubId() == null) {
                    throw new IllegalArgumentException("GreetingType " + specification + " should have a subId");
                }
                break;
            default:
        }
    }
}
