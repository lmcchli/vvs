/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.greetings;

import jakarta.activation.MimeType;

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

    /**
     * Returns the proper filename (without a path) based on the provided {@code specification} and {@code contentType}.<p>
     * It uses the :
     * <ul>
     * <li>GreetingSpecification.GreetingType
     * <li>GreetingSpecification.GreetingFormat
     * <li>MimeType
     * </ul>
     * 
     * For example : allcalls_voice.3gp<p>
     * 
     * @param specification
     * @param contentType
     * @return the filename (without a path) 
     */
    public static String getGreetingFileName(GreetingSpecification specification, MimeType contentType) {
        checkArgs(specification);
        return getFileName(specification) + getFileExtension(specification.getFormat(), contentType);
    }

    /**
     * Returns the proper filename (without a path and without an extension) based on the provided {@code specification}.<p>
     * It uses the :
     * <ul>
     * <li>GreetingSpecification.GreetingType
     * <li>GreetingSpecification.GreetingFormat
     * </ul>
     * 
     * For example : allcalls_voice<p>

     * @param specification
     * @return  the filename (without a path and without and extension)
     */
    protected static String getFileName(GreetingSpecification specification) {
        String filename = null;
        switch (specification.getType()) {
            case ALL_CALLS:             filename = "allcalls"; break;
            case BUSY:                  filename = "busy"; break;
            case CDG:                   filename = "cdg" + specification.getSubId(); break;
            case EXTENDED_ABSENCE:      filename = "extendedabsence"; break;
            case NO_ANSWER:             filename = "noanswer"; break;
            case OUT_OF_HOURS:          filename = "outofhours"; break;
            case OWN_RECORDED:          filename = "ownrecorded"; break;
            case TEMPORARY:             filename = "temporary"; break;
            case SPOKEN_NAME:           filename = "spokenname"; break;
            case DIST_LIST_SPOKEN_NAME: filename = "distlistspokenname" + specification.getSubId(); break;
            default:                    filename = "unknown"; break;
        }
        switch (specification.getFormat()) {
            case VOICE: filename += "_voice"; break;
            case VIDEO: filename += "_video"; break;
            default:    filename += "_unknown format"; break;
        }
        return filename;
    }

    //TODO really this should use the mapping table from stream.conf to map the file extension.
    //for now it is hard coded, if many codecs in the future this should be changed.
    protected static String getFileExtension(GreetingFormat format, MimeType contentType) {
        if (format == GreetingFormat.VOICE) {
            if (contentType != null) {
            	//BaseType() shows the type string without any parameters (before ; if any).
            	//do not use toString as returns the parameters also.
                if (contentType.getBaseType().equalsIgnoreCase("audio/3gpp")) {
                	String codec=contentType.getParameter("codec").toLowerCase();
                	//contains as there may be more than one codec i.e. for video might have video and audio codec.
                	//could split by comma and compare each one but not really needed.
                	if (codec != null && codec.contains("sawb")) {
                		//wide band amr uses sawb codec.
                		//special extension for amr-wb (non-official) makes internal parsing easier
                		//strictly speaking this should be .3gp
                		return ".3gpw";
                	} else {
                		//could be samr for nb but now we only support samr, sawb.
                		//no codec indicates narrow band for backward compatibiliy.
                		//otherwise assume narrow band,
                		return ".3gp";
                	}
                }
            }
            return ".wav";
        } else {
            if (contentType != null) {
                if (contentType.getBaseType().equalsIgnoreCase("video/3gpp")) return ".3gp";
            }
            return ".mov";
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
