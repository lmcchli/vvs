package com.mobeon.masp.execution_engine.externaldocument.http;

import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * @author ermmaha
 */
public class HttpMediaResponse extends HttpResponse {
    private IMediaObject mediaObject;

    public IMediaObject getMediaObject() {
        return mediaObject;
    }

    public void setMediaObject(IMediaObject mediaObject) {
        this.mediaObject = mediaObject;
    }
}
