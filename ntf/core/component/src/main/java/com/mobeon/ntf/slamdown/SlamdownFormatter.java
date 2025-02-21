/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.slamdown;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.text.ByteArrayUtils;
import com.mobeon.ntf.text.TemplateBytes;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.text.TextCreator;

/**
 * SlamdownFormatter takes a slamdown info and formats the information into one
 * string for each caller. It can also return a header and a footer which are
 * put first and last in each slamdown information message.
 */
public class SlamdownFormatter {

    private static LogAgent logger = NtfCmnLogger.getLogAgent(SlamdownFormatter.class);
    /**
     * Constructor.
     */
    private SlamdownFormatter() {
    }

    public static SlamdownUserInfo getUserInfo(SlamdownList list) {
        SlamdownUserInfo user = new SlamdownUserInfo();
        user.setCosName(list.getCosName());
        user.setPreferredLanguage(list.getPreferredLanguage());
        user.setNotifNumber(list.getNumber());
        user.setOrigDestinationNumber(list.getOrigDestinationNumber());
        
        return user;
    }
    /**
     * Creates an array with one string of formatted information for each
     * caller.
    *@param list - the slamdown info to be formatted.
    *@return an array with one String for each caller.
    */
    public static String[] formatBody(SlamdownList list) {
        CallerInfo[] callers = list.sortCallers();
        SlamdownUserInfo user = getUserInfo(list);
        
        return formatBody(callers, user);
    }
    
    /**
     * Returns the SlamdownBytePayload requested.
     * @param charsetname The characterset of the current NTF.
     * @param list The list of slamdown information.
     */
    public static SlamdownPayload getSlamdownBytePayload(String charsetname, SlamdownList list)
    {
        return new SlamdownPayload(formatHeaderBytes(charsetname, list), formatBodyBytes(charsetname, list), formatFooterBytes(charsetname, list));
    }
    
    /**
     * Creates an array with one string of formatted information for each
     * caller.
    *@param list - the slamdown info to be formatted.
    *@return an array with one String for each caller.
    */
    public static byte[][] formatBodyBytes(String charsetname, SlamdownList list)
    {
        CallerInfo[] callers = list.sortCallers();
        SlamdownUserInfo user = getUserInfo(list);
        
        return formatBodyBytes(charsetname, callers, user);
    }

	public static String[] formatBody(CallerInfo[] callers,
			SlamdownUserInfo user) {
		String[] result = new String[callers.length];
        
		for (int i = 0; i < result.length; i++) {
		    String line = null;
		    try {
		        line = TextCreator.get().generateText(null, null, user, "slamdownbody", false, callers[i]);
		    } catch(TemplateMessageGenerationException e) { }
		    
		    if( line == null ) {
		        try {
		            user.setPreferredLanguage(Config.getDefaultLanguage());
		            line = TextCreator.get().generateText(null, null, user, "slamdownbody", false, callers[i]);
		        } catch(TemplateMessageGenerationException e) { }
		    }
		    if( line == null ) {
		        try {
		            user.setPreferredLanguage("en");
		            line = TextCreator.get().generateText(null, null, user, "slamdownbody", false, callers[i]);
		        } catch(TemplateMessageGenerationException e) { }
		    }
		    if( line == null ) {
		        line = callers[i].getNumber() + "\n";
		    }
		    logger.debug("Using slamdownbody " + line);
		    result[i] = line;

		}
        return result;
	}
    
    /**
     * Creates an array with one string of formatted information for each
     * caller.
    *@param charsetname String
    *@param callers CallerInfo[]
    *@param user SlamdownUserInfo
    *@return an array with one String for each caller.
    */
    public static byte[][] formatBodyBytes(String charsetname, CallerInfo[] callers, SlamdownUserInfo user)
            {
        byte[][] result = new byte[callers.length][];
        for (int i = 0; i < result.length; i++) {
            byte[] line = null;
            try {
                line = TextCreator.get().generateBytes(charsetname, null, null, user, "slamdownbody", false, callers[i]);
            } catch(TemplateMessageGenerationException e) { }
            
            if( line == null || ByteArrayUtils.arrayEquals(line, TemplateBytes.EMPTY) ) {
                try {
                    user.setPreferredLanguage(Config.getDefaultLanguage());
                    line = TextCreator.get().generateBytes(charsetname, null, null, user, "slamdownbody", false, callers[i]);
                } catch(TemplateMessageGenerationException e) { }
            }
            if( line == null || ByteArrayUtils.arrayEquals(line, TemplateBytes.EMPTY) ) {
                try {
                    user.setPreferredLanguage("en");
                    line = TextCreator.get().generateBytes(charsetname, null, null, user, "slamdownbody", false, callers[i]);
                } catch(TemplateMessageGenerationException e) { }
            }
            if( line == null || ByteArrayUtils.arrayEquals(line, TemplateBytes.EMPTY) ) {
                line = (callers[i].getNumber() + "\n").getBytes();
            }
            logger.debug("Using slamdownbody " + line);
            result[i] = line;
        }
        return result;
            }

    /**
     * Formats a header line for a slamdown info SMS.
     *@param list - the slamdown information, to get the users preferred
     * language.
     *@return an SMS header in the preferred language.
     */
    public static String formatHeader(SlamdownList list) {
        String result = null;
        SlamdownUserInfo user = getUserInfo(list);
        if (list.isSingleCaller()) {
            try {
                result = TextCreator.get().generateText(null, null, user, "slamdownheader1", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if (result == null) {
            try {
                result = TextCreator.get().generateText(null, null, user, "slamdownheader", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if( result == null ) {
            try {
                user.setPreferredLanguage(Config.getDefaultLanguage());
                result = TextCreator.get().generateText(null, null, user, "slamdownheader", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if( result == null ) {
            try {
                user.setPreferredLanguage("en");
                result = TextCreator.get().generateText(null, null, user, "slamdownheader", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }

        if (result == null) {
            result = "Callers:\n";
        }
        return result;
    }
    
    /**
     * Formats a header line for a slamdown info SMS.
     *@param list - the slamdown information, to get the users preferred
     * language.
     *@return an SMS header in the preferred language.
     */
    public static byte[] formatHeaderBytes(String charsetname, SlamdownList list) {
        byte[] result = null;
        SlamdownUserInfo user = getUserInfo(list);
        if (list.isSingleCaller()) {
            try {
                result = TextCreator.get().generateBytes(charsetname, null, null, user, "slamdownheader1", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if (result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY)) {
            try {
                result = TextCreator.get().generateBytes(charsetname, null, null, user, "slamdownheader", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if( result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY) ) {
            try {
                user.setPreferredLanguage(Config.getDefaultLanguage());
                result = TextCreator.get().generateBytes(charsetname, null, null, user, "slamdownheader", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if( result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY) ) {
            try {
                user.setPreferredLanguage("en");
                result = TextCreator.get().generateBytes(charsetname, null, null, user, "slamdownheader", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }

        if (result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY)) {
            result = "Callers:\n".getBytes();
        }
        return result;
    }

    /**
     * Formats a footer line for a slamdown info SMS.
     *@param list - the slamdown information, to get the users preferred
     * language.
     *@return an SMS footer in the preferred language.
     */
    public static String formatFooter(SlamdownList list) {
        String result = null;
        SlamdownUserInfo user = getUserInfo(list);

        try {
            result = TextCreator.get().generateText(null, null, user, "slamdownfooter", false, null);
        } catch(TemplateMessageGenerationException e) { }
        
        if( result == null ) {
            try {
                user.setPreferredLanguage(Config.getDefaultLanguage());
                result = TextCreator.get().generateText(null, null, user, "slamdownfooter", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if( result == null ) {
            try {
                user.setPreferredLanguage("en");
                result = TextCreator.get().generateText(null, null, user, "slamdownfooter", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Formats a footer line for a slamdown info SMS.
     *@param list - the slamdown information, to get the users preferred
     * language.
     *@return an SMS footer in the preferred language.
     */
    public static byte[] formatFooterBytes(String charsetname, SlamdownList list) {
        byte[] result = null;
        SlamdownUserInfo user = getUserInfo(list);

        try {
            result = TextCreator.get().generateBytes(charsetname, null, null, user, "slamdownfooter", false, null);
        } catch(TemplateMessageGenerationException e) { }
        
        if( result == null  || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY)) {
            try {
                user.setPreferredLanguage(Config.getDefaultLanguage());
                result = TextCreator.get().generateBytes(charsetname, null, null, user, "slamdownfooter", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if( result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY) ) {
            try {
                user.setPreferredLanguage("en");
                result = TextCreator.get().generateBytes(charsetname, null, null, user, "slamdownfooter", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if (result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY)) {
            result = "".getBytes();
        }
        return result;
    }

    

}
