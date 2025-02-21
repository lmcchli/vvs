/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.common.xmp.client;

/**
 * Specifies a class that can handle XMP results.
 */
public interface XmpResultHandler {
    /**
     * Handle an XMP result.
     *@param result XMLResult with result from XmpServer.
     */
    public void handleResult(XmpResult result);

    
}

