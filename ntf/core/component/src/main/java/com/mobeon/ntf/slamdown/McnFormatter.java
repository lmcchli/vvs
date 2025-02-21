/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.slamdown;

import java.util.Hashtable;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.text.ByteArrayUtils;
import com.mobeon.ntf.text.TemplateBytes;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.text.TextCreator;

/**
 * McFormatter takes a MCN info and formats it in string.
 * It can also return a header and a footer which are put
 * first and last in each MCN information message.
 */
public class McnFormatter {
    
    private static LogAgent logger = NtfCmnLogger.getLogAgent(McnFormatter.class);
    
    /**
     * Defines the existing templates for MCN SMSs.
     * Note that the template names are related to the templates in the file
     * en.cphr. They have to match exactly the templates in this file.
     * @author egeobli
     */
    public enum Template {
        MCN("mcn"),
        MCN_SUBSCRIBED("mcnsubscribed");
        
        private String name;
        
        private Template(String name) {
            this.name = name;
        }
        
        public String toString() {
            return name;
        }
    }
    
    
    private static Hashtable<Template, McnFormatter> formatters = new Hashtable<Template, McnFormatter>();
    
    private Template template;
    
    /**
     * Constructor.
     */
    private McnFormatter(Template template) {
        this.template = template;
    }

    /**
     * Returns an SMSs formatter given the specified template.
     * @param template Template selector.
     * @return Instance of McnFormatter using the specified template.
     */
    public static McnFormatter getInstance(Template template) {
        McnFormatter instance = formatters.get(template);
        if (instance == null) {
            instance = new McnFormatter(template);
            formatters.put(template, instance);
        }
        return instance;
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
     * Creates an array with one string of formatted information for each caller.
     * @param list Mcn info to be formatted.
     * @return array with one String for each caller.
     */
    public String[] formatBody(SlamdownList list) {
        CallerInfo[] callers = list.sortCallers();
        SlamdownUserInfo user = getUserInfo(list);
        return formatBody(callers, user);
    }

	public String[] formatBody(CallerInfo[] callers, SlamdownUserInfo user) {
		String[] result = new String[callers.length];
        
        for (int i = 0; i < result.length; i++) {
            String line = null;
            try {
                line = TextCreator.get().generateText(null, null, user, template + "body", false, callers[i]);
            } catch(TemplateMessageGenerationException e) { }
            
            if( line == null ) {
                try {
                    user.setPreferredLanguage(Config.getDefaultLanguage());
                    line = TextCreator.get().generateText(null, null, user, template + "body", false, callers[i]);
                } catch(TemplateMessageGenerationException e) { }
            }
            if( line == null ) {
                try {
                    user.setPreferredLanguage("en");
                    line = TextCreator.get().generateText(null, null, user, template + "body", false, callers[i]);
                } catch(TemplateMessageGenerationException e) { }
            }
            if( line == null ) {
                line = callers[i].getNumber() + "\n";
            }
           logger.debug("Using " + template + "body " + line);
            result[i] = line;
        }
        return result;
	}

    /**
     * Formats a header line for a Mcn info SMS.
     * @param list Mcn info to be formatted.
     * @return SMS header in the preferred language.
     */
	public String formatHeader(SlamdownList list) {
	    String result = null;
	    SlamdownUserInfo user = getUserInfo(list);

	    if (list.isSingleCaller()) {
	        try {
	            result = TextCreator.get().generateText(null, null, user, template + "header1", false, null);
	        } catch(TemplateMessageGenerationException e) { }
	    }
	    if (result == null) {
	        try {
	            result = TextCreator.get().generateText(null, null, user, template + "header", false, null);
	        } catch(TemplateMessageGenerationException e) { }
	    }
	    if( result == null ) {
	        try {
	            user.setPreferredLanguage(Config.getDefaultLanguage());
	            result = TextCreator.get().generateText(null, null, user, template + "header", false, null);
	        } catch(TemplateMessageGenerationException e) { }
	    }
	    if( result == null ) {
	        try {
	            user.setPreferredLanguage("en");
	            result = TextCreator.get().generateText(null, null, user, template + "header", false, null);
	        } catch(TemplateMessageGenerationException e) { }
	    }
	    if (result == null) {
	        result = "Callers:\n";
	    }
	    return result;
	}

    /**
     * Formats a footer line for a Mcn info SMS.
     * @param list Mcn info to be formatted.
     * @return SMS footer in the preferred language.
     */
    public String formatFooter(SlamdownList list) {
        String result = null;
        SlamdownUserInfo user = getUserInfo(list);
        try {
            result = TextCreator.get().generateText(null, null, user, template + "footer", false, null);
        } catch(TemplateMessageGenerationException e) { }
        
        if( result == null ) {
            try {
                user.setPreferredLanguage(Config.getDefaultLanguage());
                result = TextCreator.get().generateText(null, null, user, template + "footer", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if( result == null ) {
            try {
                user.setPreferredLanguage("en");
                result = TextCreator.get().generateText(null, null, user, template + "footer", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Returns the SlamdownBytePayload requested.
     * @param charsetname The characterset of the current NTF.
     * @param list The list of mcn information.
     */
    public SlamdownPayload getMcnBytePayload(String charsetname, SlamdownList list) {
        return new SlamdownPayload(formatHeaderBytes(charsetname, list), formatBodyBytes(charsetname, list), formatFooterBytes(charsetname, list));
    }
    
    private byte[] formatHeaderBytes(String charsetname, SlamdownList list) {
        byte[] result = null;
        SlamdownUserInfo user = getUserInfo(list);
        if (list.isSingleCaller()) {
            try {
                result = TextCreator.get().generateBytes(charsetname, null, null, user, template + "header1", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if (result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY)) {
            try {
                result = TextCreator.get().generateBytes(charsetname, null, null, user, template + "header", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if( result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY) ) {
            try {
                user.setPreferredLanguage(Config.getDefaultLanguage());
                result = TextCreator.get().generateBytes(charsetname, null, null, user, template + "header", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if( result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY) ) {
            try {
                user.setPreferredLanguage("en");
                result = TextCreator.get().generateBytes(charsetname, null, null, user, template + "header", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }

        if (result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY)) {
            result = "Callers:\n".getBytes();
        }
        return result;
    }
    
    private byte[][] formatBodyBytes(String charsetname, SlamdownList list)
    {
        CallerInfo[] callers = list.sortCallers();
        SlamdownUserInfo user = getUserInfo(list);
        return formatBodyBytes(charsetname, callers, user);
    }

    private byte[][] formatBodyBytes(String charsetname, CallerInfo[] callers, SlamdownUserInfo user)
    {
        byte[][] result = new byte[callers.length][];
        for (int i = 0; i < result.length; i++) {
            byte[] line = null;
            try {
                line = TextCreator.get().generateBytes(charsetname, null, null, user, template + "body", false, callers[i]);
            } catch(TemplateMessageGenerationException e) { }
            
            if( line == null || ByteArrayUtils.arrayEquals(line, TemplateBytes.EMPTY) ) {
                try {
                    user.setPreferredLanguage(Config.getDefaultLanguage());
                    line = TextCreator.get().generateBytes(charsetname, null, null, user, template + "body", false, callers[i]);
                } catch(TemplateMessageGenerationException e) { }
            }
            if( line == null || ByteArrayUtils.arrayEquals(line, TemplateBytes.EMPTY) ) {
                try {
                    user.setPreferredLanguage("en");
                    line = TextCreator.get().generateBytes(charsetname, null, null, user, template + "body", false, callers[i]);
                } catch(TemplateMessageGenerationException e) { }
            }
            if( line == null || ByteArrayUtils.arrayEquals(line, TemplateBytes.EMPTY) ) {
                line = (callers[i].getNumber() + "\n").getBytes();
            }
            logger.debug("Using " + template + "body " + line);
            result[i] = line;
        }
        return result;
    }
    
    private byte[] formatFooterBytes(String charsetname, SlamdownList list) {
        byte[] result = null;
        SlamdownUserInfo user = getUserInfo(list);

        try {
            result = TextCreator.get().generateBytes(charsetname, null, null, user, template + "footer", false, null);
        } catch(TemplateMessageGenerationException e) { }
        
        if( result == null  || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY)) {
            try {
                user.setPreferredLanguage(Config.getDefaultLanguage());
                result = TextCreator.get().generateBytes(charsetname, null, null, user, template + "footer", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if( result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY) ) {
            try {
                user.setPreferredLanguage("en");
                result = TextCreator.get().generateBytes(charsetname, null, null, user, template + "footer", false, null);
            } catch(TemplateMessageGenerationException e) { }
        }
        if (result == null || ByteArrayUtils.arrayEquals(result, TemplateBytes.EMPTY)) {
            result = "".getBytes();
        }
        return result;
    }
    
}
