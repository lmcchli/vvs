/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.text;

import com.mobeon.ntf.util.Logger;
import java.util.*;


/**
 * TemplateTokenizer chops a string into tokens. A token can be either a
 * tag, i.e. a substring that begins and ends with a delimiter, and has
 * no embedded delimiters, or a substring that is not a tag, and has no
 * embedded delimiters. The delimiter is __ (a sequence of two underscore
 * characters).
 * <P>
 * The template string is parsed from the end, i.e. the last token is
 * returned first.
 * <P>
 * Example: the template <I>You have__a new mail with __SUBJECT__ as
 * subject.</I> will return the tokens<BR><UL>
 * <LI>" as subject."
 * <LI>"__SUBJECT__"
 * <LI>"a new mail with "
 * <LI>"You have__"
 * </UL>
 */
public class TemplateTokenizer {
    static final private Logger legacyLogger = Logger.getLogger(TemplateTokenizer.class); // legacy Solaris style logger..
    /**Delimiter string surrounding template tags*/
    private static final String DELIM = "__";
    /**The template string to tokenize*/
    private String s;
    /**Points at the last character of the template that has
       not yet been processed*/
    private int last;
    
    
    public TemplateTokenizer(String s) {
        legacyLogger.logMessage("TemplateTokenizer for " + s, Logger.L_DEBUG);
        this.s = s;
        last = s.length() - 1;
    }
    
    /**
     * getNext extracts the next token from the template. The tokens in the
     * end of the string are returned first.
     *@return the next token substring, or null if all tokens have already
     * been returned.
     */
    //after is constant in this method and is changed just before the
    //method returns.
    //delimPos points to the delimiter most recently found.
    //tokenStart moves between places where the token is assumed to start.
    public String getNext() {
        int tokenStart = -1; //still -1 later means no start delimiter found
        int delimPos;
        
        if (last < 0) { return null; } //All tokens in s have been returned
        
        delimPos = s.lastIndexOf(DELIM, last - DELIM.length() + 1); //Find the last delimiter
        
        if (delimPos >= 0) { //There is at least one delimiter
            if (delimPos + 1 == last) { //s ends with a delimiter
                //Find the next delimiter before the one found, i.e. the start of a tag
                tokenStart = s.lastIndexOf(DELIM, delimPos - DELIM.length());
            } else { //s has characters after the last delimiter, these
                //are the token to return
                tokenStart = delimPos + 2; //Exclude the delimiter
            }
        }
        if (tokenStart < 0) { tokenStart = 0; } //0 or 1 delimiters found, the
        //token is the entire string
        
        int tokenEnd = last + 1; //The token found always ends at the end of template
        last = tokenStart - 1; //Move end of template before the token found
        return s.substring(tokenStart, tokenEnd);
    }
}
