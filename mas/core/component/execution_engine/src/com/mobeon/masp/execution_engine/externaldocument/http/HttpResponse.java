package com.mobeon.masp.execution_engine.externaldocument.http;

/**
 * Base class for data extracted from a Http response.
 *
 * @author ermmaha
 */
public class HttpResponse {
    private int responseCode;
    private long lastModified;
    private long date;
    private long expires;

    /**
     * Retrieves the Http result code
     *
     * @return the code
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Sets the Http result code
     *
     * @param responseCode the code
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Retrieves the Last-Modified header
     *
     * @return value on the header
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Set value from the Last-Modified header
     *
     * @param lastModified value
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Retrieves the Expires header
     *
     * @return value on the header
     */
    public long getExpires() {
        return expires;
    }

    /**
     * Set value from the Expires header
     *
     * @param expires value
     */
    public void setExpires(long expires) {
        this.expires = expires;
    }

    /**
     * Retrieves the Date header
     *
     * @return value on the header
     */
    public long getDate() {
        return date;
    }

    /**
     * Set value from the Date header
     *
     * @param date value
     */
    public void setDate(long date) {
        this.date = date;
    }
}
