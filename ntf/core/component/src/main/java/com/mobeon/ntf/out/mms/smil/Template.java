package com.mobeon.ntf.out.mms.smil;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.out.mms.smil.SmilContent;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.BodyPart;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.IllegalWriteException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import java.util.Date;
import java.util.Random;
import java.util.zip.ZipFile;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

/**
 * 
 * @deprecated
 *
 */
public class Template {
                
    /* Debug and error log **/
    private final static Logger log = Logger.getLogger(Template.class);               
    /* Container of media objects used for a MMS notification **/
    private ZipFile zipfile = null;    
    /* Container for cached bodyparts**/
    private MimeMultipart mimemultipart = new MimeMultipart("related");    
    /* Cached bodyparts from the zip file. **/
    private BodyPart abodypart[] = null;                      
   /* Cached smildocument, not modified. **/
    private String smildoc = null;
    
    /**
     * Read a zipfile from disk, path to file is based on 
     * language and configuration. <BR><BR>
     * Creates and caches bodyparts based on the content of the zipfile.
     * The created bodyparts is cached in a mimemultipart object wich is 
     * used as a template for every smil based Voice to MMS notification.
     **@param language is user language
     **/
    protected Template(String language, String prefix, String languageExtention, String cosName) {
        createCach(language, prefix, languageExtention, cosName);
    }  
           
    /*
     * Creates a cach from the zipfile. 
     **/
    private void createCach(String language, String prefix, String languageExtention, String cosName){
        try{   
            zipfile = null;
            if( prefix == null ) {
                prefix = "";
            }
            
            if(cosName != null && cosName != "")
            {
            	log.logMessage("Creating a template for " + language + " using " +
                        Config.getPhraseDirectory() + "/" + prefix + language +".zip",
                        log.L_DEBUG);
            	
            	if(languageExtention != null && languageExtention != "")
            	{
	                try{
	                    zipfile = new ZipFile(Config.getPhraseDirectory() + "/" + prefix + languageExtention + "_" + language + "-x-" + cosName + ".zip");
	                }catch(Exception e){
	                	//IOException e
	                    log.logMessage("No template could be found for message type and language "  +
	                        prefix + languageExtention + "_" + language + "-x-" + cosName + " trying to use extention + lang", log.L_ERROR);
	                }
            	}
                if(zipfile == null)
                {
	                try{	
	                	zipfile = new ZipFile(Config.getPhraseDirectory() + "/" + prefix + language + "-x-" + cosName + ".zip");
		            }catch(Exception e){
		                log.logMessage("No template could be found for message type and language "  +
		                    prefix + language + "-x-" + cosName + " trying to use lang", log.L_ERROR);
		            }
                }
                if(zipfile == null)
                {
	                try{
	                	zipfile = new ZipFile(Config.getPhraseDirectory() + "/" + prefix + language + ".zip");
	                }catch(Exception e){
		                log.logMessage("No template could be found for message type and language "  +
		                    prefix + language + " trying to use system default", log.L_ERROR);
		            }
                }
                if(zipfile == null)
                {
	                try{
	                	zipfile = new ZipFile(Config.getPhraseDirectory() + "/" + prefix  + Config.getDefaultLanguage() + ".zip");
	                }catch(Exception e){
		                log.logMessage("No template could be found for message type and language "  +
		                    prefix +  Config.getDefaultLanguage(), log.L_ERROR);
		            }
                }
            }
            else if(languageExtention != null && languageExtention != "")
            {
            	log.logMessage("Creating a template for " + language + " using " +
                        Config.getPhraseDirectory() + "/" + prefix + language +".zip",
                        log.L_DEBUG);
                try{
                    zipfile = new ZipFile(Config.getPhraseDirectory() + "/" + prefix + languageExtention + "_" + language + ".zip");
                }catch(ZipException e){
                    log.logMessage("No template could be found for message type and language "  +
                        prefix + languageExtention + "_" + language + " trying to use lang", log.L_ERROR);
                }
                if(zipfile == null)
                {
	                try{
	                	zipfile = new ZipFile(Config.getPhraseDirectory() + "/" + prefix + language + ".zip");
		            }catch(ZipException e){
		                log.logMessage("No template could be found for message type and language "  +
		                    prefix + language + " trying to use default", log.L_ERROR);
		            }
                }
                if(zipfile == null)
                {
		            try{
		            	zipfile = new ZipFile(Config.getPhraseDirectory() + "/" + prefix  + Config.getDefaultLanguage() + ".zip");
		            }catch(ZipException e){
		                log.logMessage("No template could be found for message type and language "  +
		                    prefix + Config.getDefaultLanguage(), log.L_ERROR);
		            }
                }
            }
            else
            {
            	try{
            		zipfile = new ZipFile(Config.getPhraseDirectory() + "/" + prefix + language + ".zip");
            	}catch(ZipException e){
                    log.logMessage("No template could be found for message type and language "  +
                        prefix + language + " trying to use default", log.L_ERROR);
                }
            	if(zipfile ==  null)
                {
	            	try{
	            		zipfile = new ZipFile(Config.getPhraseDirectory() + "/" + prefix  + Config.getDefaultLanguage() + ".zip");
	            	}catch(ZipException e){
		                log.logMessage("No template could be found for message type and language "  +
		                    prefix + Config.getDefaultLanguage(), log.L_ERROR);
		            }
                }
            }
            
            abodypart = new BodyPart[ zipfile.size() ];
            
            if (Config.getLogLevel() == log.L_DEBUG)            
                log.logMessage("Zip file name:"+zipfile.getName()+". Number of entries in zip file: " + zipfile.size(), log.L_DEBUG);
            
            /* Loop trough every zipentry**/
            Enumeration entries = zipfile.entries();
            for(int j = 0; entries.hasMoreElements(); j++){                
                ZipEntry ze = (ZipEntry) entries.nextElement();
                try{                    
                    /* The smil document is not a static object. 
                     * The document needs to be parsed for every MMS 
                     * notification in order to replace configurable
                     * values within the document.
                     **/
                    if (Config.getLogLevel() == log.L_DEBUG)                            
                            log.logMessage("Reading zip entry: " + ze.getName(), log.L_DEBUG);
                    if( ze.getName().indexOf(".smil") < 0 ){        
                        if (Config.getLogLevel() == log.L_DEBUG)                            
                            log.logMessage("Adding " + ze.getName() + " to template for " + language, log.L_DEBUG);
                        abodypart[j] = new MimeBodyPart();
                        
                        //Could be optimized by putting the data in a ByteArrayDataSource instead
                        FileDataSource fds = new FileDataSource( getFile(ze, language) );
                        abodypart[j].setDataHandler( new DataHandler(fds) );
                        abodypart[j].setHeader( "Content-ID", ze.getName() );
                        abodypart[j].setHeader( "Content-Location", ze.getName() );                        
                        mimemultipart.addBodyPart( abodypart[j] );
                    } else{ 
                    	smildoc = new String(getBytes(ze)); 
                    }
                    
                }catch(MessagingException e){
                    log.logMessage("Could not create bodypart: " + ze.getName() + " : " + e, log.L_ERROR);
                    smildoc = null;
                    mimemultipart = null;
                    abodypart = null;
                    return;
                }
            }
            if( smildoc == null ) {
                log.logMessage("Zipfile " + zipfile.getName() + " is missing a smil file", log.L_ERROR);
            }
        }catch(IOException e){
            log.logMessage("createCache Zip template not found: " + e, log.L_ERROR);
            smildoc = null;
            mimemultipart = null;
            abodypart = null;
            return;                
        }catch(Exception e){
            log.logMessage("createCache. Uknown error: " + e, log.L_ERROR);                                
            smildoc = null;                    
            mimemultipart = null;                    
            abodypart = null;
            return;
        }
    }
    
    /** 
     * Write zipentry to disk. This is neded by javamails FileDataSource and 
     * DataHandler.
     */ 
    private File getFile(ZipEntry ze, String language){
    	try{
    		File f = new File(Config.getTemplateDir() + "/" + language + "_" + ze.getName());
    		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));

    		InputStream is = zipfile.getInputStream(ze);
    		byte[] buf = new byte[4 * 1024];  // Read/write in batches to minimize systemcalls
    		int bytesRead = 0;
    		while ((bytesRead = is.read(buf)) != -1) {
    			bos.write(buf, 0, bytesRead);
    		}
    		is.close();
    		bos.close();
    		return f;
    	}catch(IOException e){
    		log.logMessage("getFile. Could not write zipenty to disk: " + e, log.L_ERROR);
    		return null;
    	}catch(Exception e){                        
    		log.logMessage("getFile. Unknown error. Could not write zipenty to disk: " + e, log.L_ERROR);
    		return null;
    	}
    }
    
    /** 
     * Reads the inputstream from the zipentry and 
     * returns an bytearray with the content of 
     * the InputStream.
     *@return the content of the InputStream
     */     
    private byte[] getBytes(ZipEntry ze){
        try{
            
            //Not the correct way to do it
            //Works for strings probably
            byte abyte[] = new byte[(int) ze.getSize()];
            InputStream is = zipfile.getInputStream(ze);
            is.read(abyte, 0, abyte.length);
            is.close();
            return abyte;
        }catch(IOException e){
            log.logMessage("getBytes. Could not get InputStream from zipentry: " + e, log.L_ERROR);
            return null;
        }        
    }
    
    /**
     * Creates a MimeMultipart object based on the cached mimemultipart, 
     * cached smildocument, transcoded wav file, the NotificationEmail and Component data.
     *@param user information about the user that shall be notified.
     *@param email all information about the notification.
     *@param inbox TextCreator needs it in order to generate a text template.
     *@return a compleate smil based Voice to MMS Message
     */
    protected MimeMultipart getContent(UserInfo user, 
                                 NotificationEmail email, 
                                 UserMailbox inbox){
        try{                
            if ( smildoc == null ) return null;
            if ( mimemultipart == null ) return null;
            
            log.logMessage("Number of saved bodyparts: " + mimemultipart.getCount(), log.L_DEBUG);            
            MimeMultipart mm = new MimeMultipart("related");
            if ( mimemultipart != null && mimemultipart.getCount() > 0){
                for(int i = 0; i < mimemultipart.getCount(); i++){
                    mm.addBodyPart(mimemultipart.getBodyPart(i));                    
                }
            }
            
            SmilContent sc = new SmilContent(user, email, inbox, smildoc, mm);
            return sc.createContent();
            
        }catch(Exception e){
            log.logMessage("Could not create a Smil notification message: " + e, log.L_ERROR);
            return null;
        }
    }                                 
}
