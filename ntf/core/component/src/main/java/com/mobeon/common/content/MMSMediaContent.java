package com.mobeon.common.content;

import java.util.ArrayList;
import java.util.List;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;


public class MMSMediaContent {    
    private String smil = null; //optional smil
    private List<ContentPart> parts = new ArrayList<ContentPart>(); //list of optional parts (that are referenced from the smil)     
            
    private BinaryContent message = null; //content of voice, video or fax message
    
    public MMSMediaContent() {}
    
    public boolean hasSmil() {
        return (smil != null)? true: false;
    }
    
    public void setSmil(String smil) {
        this.smil = smil;
    }
    
    public String getSmil() {
        return smil;
    }
    
    public MimeBodyPart getSmilAsMimeBodyPart() {
        try {
            MimeBodyPart smilPart = new MimeBodyPart();                
            smilPart.setContent(smil, "application/smil");                
            smilPart.setContentID("notification.smil");
            smilPart.setFileName("notification.smil");
            return smilPart;
        } catch (MessagingException e){
            e.printStackTrace(); //should not happen on new part!
        }
        return null;
    }
    
    public void addOptionalPart(ContentPart part) {
        parts.add(part);
    }
    
    public void addPartIfDefinedInSmil(String smilTemplateArg, String text, String fileName, String charSet ) {
        if (smil != null && smil.contains(smilTemplateArg)) {
            smil = smil.replace(smilTemplateArg, fileName);
            parts.add(new TextContent(text, fileName,charSet));            
        }        
    }
    
    public void replaceInSmil(String smilTemplateArg, String text) {
        if (smil != null && smil.contains(smilTemplateArg)) {
            smil = smil.replace(smilTemplateArg, text);                    
        }        
    }    
    
    public List<ContentPart> getOptionalParts() {
        return parts;
    }
    
    public void setMessageContent(BinaryContent message) {
        this.message = message;
    }
    
    public BinaryContent getMessageContent() {
        return message;
    }
    
    public MimeMultipart getOptionalPartAsMimeMultiPart() {
        MimeMultipart multiPart = new MimeMultipart();
        try {           
            if(hasSmil()) {
                multiPart.addBodyPart(getSmilAsMimeBodyPart());
                for(ContentPart p : getOptionalParts()) {
                    multiPart.addBodyPart(p.getMimeBodyPart());            
                }
            }
        } catch(MessagingException e) {
            e.printStackTrace();
        }        
        return multiPart;
    }
    
    public MimeMultipart getAllContentsAsMimeMultiPart() {
        MimeMultipart multiPart = new MimeMultipart();
        try {           
            if(hasSmil()) {
                multiPart.addBodyPart(getSmilAsMimeBodyPart());
                for(ContentPart p : getOptionalParts()) {
                    multiPart.addBodyPart(p.getMimeBodyPart());            
                }
            }
            //add message
            multiPart.addBodyPart(message.getMimeBodyPart());
        } catch(MessagingException e) {
            e.printStackTrace();
        }        
        return multiPart;
    }
    
    public long getSizeOfAllContents() {
        long totalSize = 0;

        if (hasSmil()) {
            totalSize += getSmil().length();
            
            for(ContentPart p : getOptionalParts()) {
                if( p.getBytes() != null ) {
                    totalSize += p.getBytes().length;
                }
            }
        }
        if (message.getBytes() != null) {
            totalSize += message.getBytes().length; 
        }

        return totalSize;
    }

}
