/**
 * COPYRIGHT (c) Abcxyz Canada Inc., 2007.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Abcxyz Canada Inc.  The program(s) may be used and/or copied only with the
 * written permission from Abcxyz Canada Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.execution_engine.compiler.ApplicationCompilerImpl;
import com.mobeon.masp.util.Tools;
import com.mobeon.common.logging.ILogger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Mikael Andersson
 */
public class OpsCompiler {

    private static class StdOutLogger implements ILogger {

            public void trace(Object message) {
                Tools.println(message);
            }

            public void trace(Object message, Throwable t) {
                printThrowable(message, t);
            }

            public void debug(Object message) {
                Tools.println(message);
            }

            public void debug(Object message, Throwable t) {
                printThrowable(message, t);
            }

            public void info(Object message) {
                Tools.println(message);
            }

            public void info(Object message, Throwable t) {
                printThrowable(message, t);
            }

            public void warn(Object message) {
                Tools.println(message);
            }

            public void warn(Object message, Throwable t) {
                printThrowable(message, t);
            }

            public void error(Object message) {
                Tools.println(message);
            }

            public void error(Object message, Throwable t) {
                printThrowable(message, t);
            }

            public void fatal(Object message) {
                Tools.println(message);
            }

            public void fatal(Object message, Throwable t) {
                printThrowable(message, t);
            }

        private void printThrowable(Object message, Throwable t) {
            Tools.println(message);
            if(t.getMessage() != null)
                Tools.println("Error description: "+t.getMessage());
        }

        public void registerSessionInfo(String name, Object sessionInfo) {
        }

        public void clearSessionInfo() {
        }

        public boolean isTraceEnabled() {
            return true;
        }

        public boolean isDebugEnabled() {
            return true;
        }

        public boolean isInfoEnabled() {
            return true;
        }

    }

    public static void main(String[] args) {
        ApplicationCompilerImpl c = new ApplicationCompilerImpl();
        ApplicationCompilerImpl.setLog(new StdOutLogger());
        c.setOpsPathURI(".");
        ModuleCollection mc = new ModuleCollection(null, null, null);
        String uriStr = args[0];
        boolean retry;
        do {
            retry = false;
            try {
                URI documentURI = new URI(uriStr);
                Module m = c.compileDocument(documentURI, mc);
                c.dumpCompiledDocument(documentURI, m);
            } catch (URISyntaxException e) {
                printInvalidUriMessage(uriStr, e.getReason());
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("URI is not absolute")) {
                    uriStr = new File("").toURI().resolve(uriStr).toString();
                    retry = true;
                } else {
                    printInvalidUriMessage(uriStr, e.getMessage());
                }
            }
        } while (retry);
    }

    private static void printInvalidUriMessage(String uriStr, String reason) {
        Tools.println("Invalid URI: '" + uriStr + "' i got the following error: " + reason);
    }

}
