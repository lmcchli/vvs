package com.mobeon.common.content;

import jakarta.mail.internet.MimeBodyPart;

public interface ContentPart {
    
    public String getPartName(); //file name
    public String getContentType(); // return the mime type
    
    public MimeBodyPart getMimeBodyPart(); // can be converted to a MimeBodyPart
    public byte[] getBytes();

}
