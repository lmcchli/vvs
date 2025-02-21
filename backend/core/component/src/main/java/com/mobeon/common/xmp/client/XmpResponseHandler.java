/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.common.xmp.client;

import java.util.ArrayList;
/**
 * Specifies a class that can handle XMP responses.
 */
public interface XmpResponseHandler {
    /**
     * Handle an XMP response.
     *@param result XML document specifying the result.
     */
    public void handleResponse(String result, XmpUnit unit, ArrayList attachments);
    
    public void sendFailed(XmpUnit unit, int transId);
}
