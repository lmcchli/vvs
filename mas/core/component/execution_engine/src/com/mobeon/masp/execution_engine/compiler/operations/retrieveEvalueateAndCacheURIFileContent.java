/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeImpl;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.mozilla.javascript.Script;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

/** Retrieve the content of the file specified byu the URL, and place the content on the value stack
 *
 * @author David Looberger
 */
public class retrieveEvalueateAndCacheURIFileContent extends OperationBase {
    private static final ILogger logger = ILoggerFactory.getILogger(retrieveEvalueateAndCacheURIFileContent.class);
    URI uri;
    DebugInfo dbi;

    public retrieveEvalueateAndCacheURIFileContent(URI uri, DebugInfo d) {
        this.uri = uri;
        this.dbi = d;
    }

    /**
     * @logs.error "Failed to compile and execute the script <uri>" - The ECMA script pointed out by <uri> probably has syntax errors.
     * @param ex
     * @throws InterruptedException
     */
    public void execute(ExecutionContext ex) throws InterruptedException {
        String proxy = null;
        int proxyPort = -1;
        String loadedSrc = null;
        Script cachedScript = ScopeImpl.getScriptFromCache(uri.toString());
        if (cachedScript != null) {
            // Descard any object returned from the execution of the script
            ex.getCurrentScope().exec(cachedScript,uri, 1);
            return;
        }
        // If the script did not yet exist in the cache,
        // Retrieve, compile, store and execute it
        if (uri.getScheme() != null) {
            if (uri.getScheme().equals("file") || uri.getScheme().startsWith("test")) {
                loadedSrc = readDocument(ex, uri);
            }
            else {
                if (logger.isDebugEnabled()) logger.debug("The scheme " + uri.getScheme() + " is not yet supported");
                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH, dbi);
                loadedSrc = "";
            }
        }
        else {
            try {
                URI fileURI = new URI("file:" + uri.getPath());
                loadedSrc = readDocument(ex, fileURI);
            } catch (URISyntaxException e) {
                if (logger.isDebugEnabled()) logger.debug("Illegal URI syntax!", e);
                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH, dbi);
                loadedSrc = "";
            }
        }
        Script compiledScript = ex.getCurrentScope().compileAndCache(loadedSrc, uri.toString());
        // Descard any object returned from the execution of the script
        if (compiledScript != null) {
            ex.getCurrentScope().exec(compiledScript, uri, 1);
        } else {
            logger.error("Failed to compile and execute the script " + uri);
        }
    }

    public String arguments() {
        return uri.toString();
    }

    private String readDocument(ExecutionContext ex, URI doc) {
        if (doc.getScheme().equals("file")) {
            return readFile(ex, doc.getPath());
        }
        else {
            return readURL(ex, doc);
        }
    }

    private String readFile(ExecutionContext ex, String path) {
        StringBuffer buff = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
            String line;
            while ((line = reader.readLine()) != null){
                buff.append(line);
                buff.append("\n");
            }
        } catch (FileNotFoundException e) {
            if (logger.isDebugEnabled()) logger.debug("File was not found : " + uri.getPath());
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH, dbi);
        } catch (IOException e) {
            if (logger.isDebugEnabled()) logger.debug("IO exception caught while reading file " + uri.getPath(), e);
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH, dbi);
        }
        return buff.toString();
    }

    /**
     * @logs.error "Failed to access to <uri> <message>" - The resource pointed out by <uri> could not be read, possibly due to wrong file permissions, the URI does not exist, or similar. <message> should give further information about the problem.
     * @param ex
     * @param uri
     * @return
     */
    private String readURL(ExecutionContext ex, URI uri) {
        if (uri.getScheme().equals("http") || uri.getScheme().equals("test")) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(uri.toURL().openStream()));
                String line;
                StringBuffer buff = new StringBuffer();
                while ((line = reader.readLine()) != null){
                    buff.append(line);
                    buff.append("\n");
                }
                return buff.toString();
            } catch (IOException e) {
                logger.error("Failed to access to " + uri, e);
                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH, dbi);
            }
        } else {
            // Unsupporte schema
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH, dbi);
        }
        return "";
    }
}
