/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.application.vxml.datatypes.Duration;
import com.mobeon.ecma.ECMAExecutor;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

import sun.net.www.http.HttpClient;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-22
 * Time: 18:22:14
 * To change this template use File | Settings | File Templates.
 */
public class ScriptNode extends Node {
    private String src = null;
    private String loadedSrc = null;
    private String body = null;
    private String charachterSet = null;
    private Node next = null;

    private String fetchHint = null;
    private Duration fetchTimeOut = null;
    private int maxAge = 0;
    private int maxStale = 0;

    public ScriptNode() {
    }

    public ScriptNode(String src) {
        this.src = src;
    }

    public Node execute(Traverser traverser) {
        logger.debug("Executing");
        ECMAExecutor ecmaExecutor = traverser.getEcmaExecutor();

        if (loadedSrc != null) {
            logger.debug("SCRIPT SOURCE: " + src);
            ecmaExecutor.exec(loadedSrc);
        }
        else if (loadedSrc == null && src != null) {
            logger.error("Loading of script source failed during compilation! URL : " + src);
        }
        else if (body != null) {
            logger.debug("SCRIPT: " + body);
            ecmaExecutor.exec(body);
        }
        else {
            logger.error("No script to execute!");
        }
        return next;
    }

     public Node load(Traverser traverser) {
        logger.debug("Loading");
        ECMAExecutor ecmaExecutor = traverser.getEcmaExecutor();

        if (loadedSrc != null) {
            logger.debug("SCRIPT SOURCE: " + src);
            ecmaExecutor.exec(loadedSrc);
        }
        else if (loadedSrc == null && src != null) {
            logger.error("Loading of script source failed during compilation! URL : " + src);
        }
        else if (body != null) {
            logger.debug("SCRIPT: " + body);
            ecmaExecutor.exec(body);
        }
        else {
            logger.error("No script to execute!");
        }
        return null;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getCharachterSet() {
        return charachterSet;
    }

    public void setCharachterSet(String charachterSet) {
        this.charachterSet = charachterSet;
    }

    public String getFetchHint() {
        return fetchHint;
    }

    public void setFetchHint(String fetchHint) {
        this.fetchHint = fetchHint;
    }

    public Duration getFetchTimeOut() {
        return fetchTimeOut;
    }

    public void setFetchTimeOut(Duration fetchTimeOut) {
        this.fetchTimeOut = fetchTimeOut;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getMaxStale() {
        return maxStale;
    }

    public void setMaxStale(int maxStale) {
        this.maxStale = maxStale;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node child) {
        this.next = child;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean loadSrc(String URI) {
        String proxy = null;
        int proxyPort = -1;
        StringBuffer buff = new StringBuffer();
        // Todo : Retrieve info about http proxy from somewhere and use that info.
        if (URI.startsWith("http://")) {
            try {
                HttpClient client = new HttpClient(new URL(URI),proxy, proxyPort);
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null){
                    buff.append(line);
                    buff.append("\n");
                }
                loadedSrc =  buff.toString();
            } catch (IOException e) {
                logger.error("Failed to access to URIValidator " + URI, e);
                return false;
            }
        }
        else if (URI.startsWith("file://")) {
            URI = URI.substring(7);
        }
        // Assume we now have a file URIValidator
        try {
            String VXMLDir = System.getProperty("VXML_ROOTDIR");
            if (!URI.startsWith("/") && VXMLDir != null )
                URI = VXMLDir + "/" + URI;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(URI))));
            String line;
            while ((line = reader.readLine()) != null){
                buff.append(line);
                buff.append("\n");
            }
            loadedSrc =  buff.toString();
        } catch (IOException e) {
            logger.error("Failed to access to file " + URI, e);
            return false;
        }
        return true;
    }
}
