package com.mobeon.ntf.out.mms.smil;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.out.mms.smil.Template;
import com.mobeon.ntf.out.pager.XmpNotificationResultHandler;
import com.mobeon.ntf.mail.NotificationEmail;

import jakarta.mail.internet.MimeMultipart;

import java.util.Hashtable;

/**
 * @deprecated
 *
 */
public class ContentCreator implements Constants {
    
    private static ContentCreator instance = null;
    private static Hashtable /* of smil template references, keyed by language */ cach = null;
    /* Debug and error log **/
    private final static Logger log = Logger.getLogger(ContentCreator.class);             
    
    static { 
        cach = new Hashtable();
    }
    
    /** Constructor */
    protected ContentCreator() { 
    }
         
    /**
     * Accessor for the single instance of ContentCreator
     *@return a instance of SmilCreator.
     */
    public static ContentCreator get() {
        if (instance == null) {
            instance = new ContentCreator();
        }
        return instance;
    }
    
    
    /**
     * Return a Message object containg the content used for a smil notification.
     *@param email - all information about the notification.
     *@param user - information about the user that shall be notified.
     *@return null or a complete Message object containing all mimeparts that should be sent to the MMS-C, 
     *including the smildocument.
     */
    public MimeMultipart getContent(UserInfo user, 
                              NotificationEmail email, 
                              UserMailbox inbox) {                                                
        
        if(Config.getLogLevel() == log.L_DEBUG){        
            log.logMessage("Generating a smiltemplate for " + user.getMail(), log.L_DEBUG);        
        }        
        
        MimeMultipart mmp = getTemplate(user, email, inbox);
        
        if (mmp == null) { return null; }
        return mmp;
    }
         
    /**
     * Creates a Message containing
     * the content (body and content-type) used in 
     * a smil notification<BR><BR>
     * There is a template for every language. 
     * Every template is cached and stored in 
     * a Hashtable, keyed by a users preferred language. 
     * If the language does not
     * exist in the cach, a SmilContent is created and cached.<BR><BR>
     *@param email - all information about the notification.
     *@param user - information about the user that shall be notified.
     *@return null or a complete Message object.
     */
    private MimeMultipart getTemplate(UserInfo user, 
                                NotificationEmail email, 
                                UserMailbox inbox) {
        Template t = null;
        String prefix = "";
        if(email.getDepositType() == depositType.VIDEO ) {
            prefix = "video-";
        }
        if ( cach.containsKey(prefix + user.getPreferredLanguage()) ){                                    
            if(Config.getLogLevel() == log.L_DEBUG)            
                log.logMessage("SmilTemplate found in cache.", log.L_DEBUG);                                
            t = (Template)cach.get(prefix + user.getPreferredLanguage());
        }
        else{   
            synchronized(this){
                if ( cach.containsKey(prefix + user.getPreferredLanguage()) ){
                    t = (Template)cach.get(prefix + user.getPreferredLanguage());                    
                }
                else{
                    if(Config.getLogLevel() == log.L_DEBUG)
                        log.logMessage("SmilTemplate not found in cache.", log.L_DEBUG);
                    t = new Template(user.getPreferredLanguage(), prefix, user.getBrand(), user.getCosName());
                    if ( t == null ) return null;
                    cach.put(prefix + user.getPreferredLanguage(), t);
                }
            }
        }
        return t.getContent(user, email, inbox);
    }   
}
