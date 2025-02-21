package com.mobeon.masp.execution_engine.externaldocument.http;

import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;

import java.net.URI;

/**
 * Interface for fetching external resources via HTTP
 *
 * @author ermmaha
 */
public interface IHttpResourceHandler {
    /**
     * Fetch a CCXML/VXML document via HTTP
     *
     * @param documentURI
     * @return object containing the result, null if some error occurred
     */
    public HttpDocumentResponse requestDocument(URI documentURI);

    /**
     * Fetch a MediaObject resource via HTTP
     *
     * @param documentURI
     * @param mediaObjectFactory
     * @return object containing the result, null if some error occurred
     */
    public HttpMediaResponse requestMedia(URI documentURI, IMediaObjectFactory mediaObjectFactory);

    /**
     * Set If-Modified-Since header in the request.
     *
     * @param ifmodifiedsince
     */
    public void setIfModifiedSince(long ifmodifiedsince);

    /**
     * Set connect timeout.
     *
     * @param connectTimeout
     */
    public void setConnectTimeout(int connectTimeout);

    /**
     * Set read timeout.
     * @param readTimeout
     */
    public void setReadTimeout(int readTimeout);
}
