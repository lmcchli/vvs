package com.mobeon.common.cmnaccess;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container3;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.message.MfsConstants;
import com.abcxyz.messaging.mfs.message.MfsMimeMultiPart;
import com.abcxyz.messaging.mfs.util.ByteArrayDataSource;

/**
 * This class provides utilities for formating content of message
 * @author lmctdvo
 *
 */

public class ContentFormatUtil {
    private static final String UTF8 = "UTF-8";
    static public final String MULTIPART_TYPE = "multipart/mixed";
    
    /**
     * This function extract the charset from the specified content-type
     * @param contentType
     * @return
     */
    static public String getCharset(String contentType) {
        String charset = UTF8;
        
        int index = contentType.indexOf("charset");
        if (index >= 0) {
            int equalSign = contentType.substring(index).indexOf("=");
            if (equalSign >= 0) {
                int end = contentType.substring(index + equalSign).indexOf(";");
                if (end < 0) {                   
                    end = contentType.length();
                }
                else {
                    end += index + equalSign;
                }
                charset = contentType.substring(index + equalSign + 1, end);
                charset = charset.trim();
                charset = FormatUtil.removeQuote(charset);
            }
        }       
        return charset;
    }
 
   /**
    * This function convert a byte stream into a string with UTF-8 charset
    * @param bytes
    * @param contentType
    * @return converted string
    * @throws UnsupportedEncodingException
    */
   static public String convertToUtf8(byte[] bytes, String contentType) throws UnsupportedEncodingException {
        
        String origStr =  new String(bytes, ContentFormatUtil.getCharset(contentType));
        byte[] utf8Bytes = origStr.getBytes(UTF8);
        String utf8Str = new String(utf8Bytes, UTF8);
        return utf8Str;
    }
   
   /**
    * Build MIME body part from C3 part
    * @param part - C3 part
    * @return MIME body part
    * @throws MessagingException
    */
   static public MimeBodyPart buildMimeBodyPart(MsgBodyPart part) throws MessagingException
   { 
	   MimeBodyPart mimePart;
	   
	   HashMap<String, String> headers = part.getPartHeaders().getAll();
	   
	   InternetHeaders iHeaders = new InternetHeaders();
	   for(String o : headers.keySet())
	   {
		   iHeaders.addHeader(o, part.getPartHeader(o));
	   }
	   
	   if (part.isExternal()) {
	       headers = part.getExternalPartHeaders().getAll();
	       for(String o : headers.keySet())
	       {
	           iHeaders.addHeader(o, part.getExternalPartHeaders().getValue(o));
	       }
	   }   
	   
	   mimePart = new MimeBodyPart(iHeaders,part.getContent());
	   mimePart.setHeader(MfsConstants.CONTENT_TYPE, part.getContentType());
	   
	   if (part.getPartHeader(MfsConstants.CONTENT_TRANSFER_ENCODING) != null) {
		   mimePart.setHeader(MfsConstants.CONTENT_TRANSFER_ENCODING, part.getPartHeader(MfsConstants.CONTENT_TRANSFER_ENCODING));
	   }
	   return mimePart;
   }
   
   static public MimeMessage buildMimeMessage(Container1 c1, Container3 c3) throws MessagingException {
	  
	   MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
	   importC1Headers(message,c1);	   	   
	   Vector<MsgBodyPart> parts = c3.getContents();
	  
	   MimeMultipart mpart = new MimeMultipart("voice-message");	   	   
	   
	   MimeBodyPart mimePart;
	   for(MsgBodyPart part: parts) {
		  
		   mimePart = buildMimeBodyPart(part);  
		   mpart.addBodyPart(mimePart);
		   message.setContent(mpart);
	   }
	   message.setHeader("Status", "RO");// Not sure if we need this header ...
	   return message;
   }
             
   private static void importC1Headers(MimeMessage mimeMessage, Container1 c1) throws MessagingException {
       HashMap<String, String> c1Headers = c1.getAll();
       Iterator<String> keys = c1Headers.keySet().iterator();
       String key;
       String val;
       
       while (keys.hasNext()) {
           key = keys.next();
           val = c1Headers.get(key);
           mimeMessage.setHeader(key, val);           
       }

   }
   
   /**
    * Check if body part is a MIME multi-part mix
    * @param c3Part
    * @return true if it is.
    */
   static public boolean isMultiPart(MsgBodyPart c3Part) {
       
       if (c3Part.getContentType().toLowerCase().indexOf(MULTIPART_TYPE) < 0) {
           return false;
       }
       else {
           return true;
       }
   }
 
   /**
    * Split MIME multi-part message into separate message body parts as per MFS SEC
    * @param c3Part
    * @param logger
    * @return list of splitted parts
    */
   static public Vector<MsgBodyPart> splitMimeMultiPart(MsgBodyPart c3Part, LogAgent logger)  {
       ByteArrayDataSource source = null;
       MfsMimeMultiPart mfsParts     = null;
       Vector<MsgBodyPart> parts = new Vector<MsgBodyPart>();
       
       logger.debug("splitMimeMultiPart()");
       try {
           if (c3Part.getContentType().toLowerCase().indexOf(MULTIPART_TYPE) < 0) {
               // Part is already in proper format
               parts.add(c3Part);
           }
           else {
               // Part needs splitting
               source = new ByteArrayDataSource(c3Part.getContent(), c3Part.getContentType());               
               mfsParts  = new MfsMimeMultiPart(source);
               int numOfParts = mfsParts.getCount();
               for(int i = 0; i < numOfParts; i++) {
                   if (mfsParts.getBodyPart(i).getInputStream().available() <= 0 ) {
                       logger.debug("Part " + i + " is empty. Drop it.");                                                    
                   }
                   else  {
                     InputStream in  = mfsParts.getBodyPart(i).getDataHandler().getDataSource().getInputStream();
                     ByteArrayDataSource byteArray;                
                     if (mfsParts.getBodyPart(i).getContentType().toLowerCase().indexOf("charset") < 0 ) {
                         byteArray = new ByteArrayDataSource(in,mfsParts.getBodyPart(i).getContentType() + "; charset=utf-8");
                     }
                     else {
                         byteArray = new ByteArrayDataSource(in,mfsParts.getBodyPart(i).getContentType());
                     }
                     boolean isLastPart = c3Part.isLastPart() && (i== numOfParts -1);
                     MsgBodyPart newPart = new MsgBodyPart(mfsParts.getBodyPart(i).getContentType(), byteArray.getContents(), isLastPart);  
                     if (mfsParts.getBodyPart(i).getHeader(MfsConstants.CONTENT_TRANSFER_ENCODING) != null) {
                         newPart.setPartHeader(MfsConstants.CONTENT_TRANSFER_ENCODING, mfsParts.getBodyPart(i).getHeader(MfsConstants.CONTENT_TRANSFER_ENCODING)[0]);
                     }
                     parts.add(newPart);
                   }              
               }                     
           }
           logger.debug("splitMimeMultiPart() return " + parts.size() + " parts.");
           return parts;
       } catch (MessagingException e) {
           logger.error("Failed splitting multiparts " + e,e);
       } catch (IOException e) {
           logger.error("Failed splitting multiparts " + e,e);
       }
       return null;              
   }
}
