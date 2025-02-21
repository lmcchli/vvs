/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.text;

import com.mobeon.ntf.util.Logger;
import java.util.*;


/**
 * TemplateTokenizerCphr chops a string into tokens. A token can be either a
 * tag, i.e. a substring that begins and ends with a delimiter, and has
 * no embedded delimiters, or a substring that is not a tag.
 * 
 * The template string is parsed from the end, i.e. the last token is
 * returned first.
 */
public class TemplateTokenizerCphr {
    static final private Logger legacyLogger = Logger.getLogger(TemplateTokenizerCphr.class); // legacy Solaris style logger..
    /**Delimiter string surrounding template tags*/
    private static final String DELIM = "\n";
    /**The template string to tokenize*/
    private String s;
    /**Points at the last character of the template that has
       not yet been processed*/
    private int last;
       
    public TemplateTokenizerCphr(String s) {
        legacyLogger.logMessage("TemplateTokenizerCphr for " + s, 
                                      Logger.L_DEBUG);
        this.s = s.replaceAll("\n\\(", " \\("); 
        // Line starting with "(" is assumed count tag and 
        // prepended with space for robustness
        this.s = this.s.replaceAll("\n", "");
        if(s != null)
            last = s.length() -1;
        else
            last = -1;
    }

    /**
     * getNext extracts the next token from the template. The tokens in the
     * end of the string are returned first.
     * @param delim - token delimiters are: ( ) " \<white space>
     *@return the next token substring, or null if all tokens have already
     * been returned.
     */
    public String getNext() {

        String strbuf="";
        if (last < 0)
            return null;
        
        char[] revertedBuf=null;
        boolean tagMark = false;
        for(int ix=last;ix>=0;ix--){
            String ch=""+s.charAt(ix);
            if(s.charAt(ix) == '\"') {
                int k = ix-1;
                for(;k>0 && s.charAt(k) != '\"';k--) 
                    strbuf+=s.charAt(k);   
                last=k-2;
                if (strbuf == "") {
                  return ""; // No need to return null when seeing ""
                }
                break; // e.g. token "You have"
            }
            else if(ch.matches("[0-9A-Za-z,.:;\\-\\=\\+]")) { // unicode characters ending with a number were not recognised
                int k = ix;
                for(;k>=0 && s.charAt(k) != ' ' && s.charAt(k) != '\t' && s.charAt(k) != '\"' && s.charAt(k) != '{' ;k--) 
                    strbuf+=s.charAt(k);  
                last=k;
                tagMark = true;
                break; // e.g. token VCOUNT
            }
            else if(s.charAt(ix) == ')')  {
                int k = ix;
                for(;k>0 && s.charAt(k) != '(';k--)
                    strbuf+=s.charAt(k); 
                strbuf+="(";
                last=k-2;

                //check for the &
                if( (last+1 > 0) && (s.charAt(last+1) == '&') )
                {
                    for(k=last+1;k>0 && s.charAt(k) != '(';k--)
                        strbuf+=s.charAt(k);
                    strbuf+="(";
                    last=k-2;
                }

                //check for the & again (we can have two & if three conditions
                if( (last+1 > 0) && (s.charAt(last+1) == '&') )
                {
                    for(k=last+1;k>0 && s.charAt(k) != '(';k--)
                        strbuf+=s.charAt(k);
                    strbuf+="(";
                    last=k-2;
                }


                break; // e.g. token (1,*,*,*)
            }
            else if(s.charAt(ix) == ' ' || s.charAt(ix) == '\t' )
                ; //skip white space
            else if(s.charAt(ix) == '\n')
                ; // skip new line
            else if(s.charAt(ix) == '}')
              ; // end tag
            //else if(s.charAt(ix) == '{')
            //  ; // start tag
            else
              ;
        }
        
        // Revert string buffer
        revertedBuf = new char[strbuf.length()];
        String res = null;
        for(int i=strbuf.length()-1;i>=0;i--) {
            int r = strbuf.length()-i-1;
            revertedBuf[r]=strbuf.charAt(i);
        }
        if (revertedBuf.length > 0) {
          res = new String(revertedBuf);
          if (tagMark) {
            res = "__" + res;
          }
        } else {
          res = null;
        }
        return res;


    }
    
    
    /**
     * getNextLine acctually gets the only line in this tokenizer
     *
     * @return the next whole token string, or null if all tokens have already
     * been returned.
     */
    public String getNextLine() {
      int tokenStart = -1; //still -1 later means no start delimiter found
      int tokendEnd = 0;
      int delimPos = -1;

      String delim = "\n";
      if (last < 0) { 
        return null; 
      } //All tokens in s have been returned
      if (tokenStart < 0) { tokenStart = 0; } //0 or 1 delimiters found, the
      //token is the entire string      
      last = tokenStart - 1; //Move end of template before the token found
      return s;
    }
}
