package com.mobeon.common.content;

import jakarta.mail.internet.MimeBodyPart;

public class TextContent implements ContentPart {

    private String text = null;
    private String partName = "";
    private String contentType = "text/plain";
    private String charSet = "us-ascii";
    
    public TextContent(String text, String partName) {
        this.text = text;
        this.partName = partName;
    }
    
    public TextContent(String text, String partName, String charSet) {
        this.text = text;
        this.partName = partName;
        this.charSet = charSet;
    }
    
    public String getPartName() {        
        return partName;
    }
    
    public void setContentType(String type) {
        //only set if not empty..
        if (contentType!=null && !contentType.isEmpty()) {
            contentType = type;
        }
    }
   
    public String getContentType() {
       return contentType;
    }
    
    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }    
    
    public String getCharSet() {
        return charSet;
    }
    
    public MimeBodyPart getMimeBodyPart() {
        MimeBodyPart part = new MimeBodyPart();
        try {
            part.setText(text,charSet);            
            part.setFileName(partName);
            part.setHeader("Content-ID", partName);
            part.setHeader("Content-Location", partName);
            if (charSet !=null && !charSet.isEmpty()) {
                part.setHeader("Content-Type", contentType + "; charset=\"" + charSet + "\"");
            } else
            {
                part.setHeader("Content-Type", contentType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return part;
    } 
    
    public byte[] getBytes() {
        return text.getBytes();
    }

}
