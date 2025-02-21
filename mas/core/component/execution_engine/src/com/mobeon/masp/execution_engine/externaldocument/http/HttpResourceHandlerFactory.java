package com.mobeon.masp.execution_engine.externaldocument.http;

/**
 * Date: 2007-mar-12
 *
 * @author ermmaha
 */
public class HttpResourceHandlerFactory {
    public IHttpResourceHandler create() {
        return new HttpResourceHandler();
    }
}
