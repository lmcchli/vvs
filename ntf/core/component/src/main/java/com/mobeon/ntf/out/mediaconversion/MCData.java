/*
 * MediaFile.java
 *
 * Created on den 15 december 2005, 10:53
 */

package com.mobeon.ntf.out.mediaconversion;

import com.mobeon.common.xmp.XmpAttachment;


public class MCData {
    private int status;
    private XmpAttachment attachment;
    private int length = -1;
    
 
    public int getStatus() {
        return status;
    }
    
    public XmpAttachment getAttachment() {
        return attachment;
    }
    
    public int getLength() {
        return length;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public void setAttachment(XmpAttachment att) {
        this.attachment = att;
    }
    
    public void setLength(int length) {
        this.length = length;
    }
}
