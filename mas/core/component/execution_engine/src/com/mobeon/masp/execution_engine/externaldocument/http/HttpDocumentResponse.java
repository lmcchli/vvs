package com.mobeon.masp.execution_engine.externaldocument.http;

import org.dom4j.Document;

/**
 * @author ermmaha
 */
public class HttpDocumentResponse extends HttpResponse {
    private Document document;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
