/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.url;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * {@link URLStreamHandlerFactory} for unit test purposes.
 * <p/>
 * Supports all common url protocols, and adds a new protocol
 * called <b>test</b>. This protocol works similar to the <code>file</code>
 * protocol, but is designed to work in conjunction with ant so that an url
 * given is relative to the build project root. In MAS this is &lt;vob&gt;\mas\.
 * <p/>
 * To use this factory instead of the Java defaults, call the {@link #initialize} method.
 * @author Mikael Andersson
 */
public class TestHandlerFactory implements URLStreamHandlerFactory {
    protected static boolean initialized = false;

    public URLStreamHandler createURLStreamHandler(String protocol) {
        URLStreamHandler result = null;

        result = subclassURLStreamHandler(protocol);
        if(result != null)
            return result;

        if ("test".equals(protocol)) {
            return new TestStreamHandler();
        } else {
            String handlerPkgs = System.getProperty("java.protocol.handler.pkgs", "sun.net.www.protocol");
            String[] handlerPrefixes = handlerPkgs.split("\\|");
            for (String handlerPrefix : handlerPrefixes) {
                try {
                    Class c = Class.forName(handlerPrefix + "." + protocol + ".Handler");
                    return (URLStreamHandler) c.newInstance();
                } catch (ClassNotFoundException e) {
                    ignoreException(e);
                } catch (InstantiationException e) {
                    ignoreException(e);
                } catch (IllegalAccessException e) {
                    ignoreException(e);
                } catch (ClassCastException e) {
                    ignoreException(e);
                }
            }
        }
        return result;
    }

    protected URLStreamHandler subclassURLStreamHandler(String protocol) {
        return null;
    }

    /**
     * Ignores exceptions caused by reflection.
     * <p>
     * This method only reason to exist is to calm down source code
     * analysers which regard missing exception-handlers as an error.
     * In reflection based code ignoring exceptions and instead relying
     * on whether the result of <code>Class.forName</code> is <code>null</code> or
     * not, is a common and sensible strategy.
     * @param e The exception to ignore
     */
    public void ignoreException(Exception e) {

    }

    /**
     * Initializes URL with a StreamHandlerFactory appropriate for
     * testing purposes.
     */
    public synchronized static void initialize() {
        if (!initialized) {
            initialized = true;
            URL.setURLStreamHandlerFactory(new TestHandlerFactory());
        }
    }
}
