package com.mobeon.masp.util.debug;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-feb-01
 * Time: 15:36:48
 * To change this template use File | Settings | File Templates.
 */
public class Tools {

    public static String outerCaller(int i, boolean isDebugEnabled) {
        if (isDebugEnabled) {
            try {
                thrower();
            } catch (Exception e) {
                return e.getStackTrace()[3 + i].toString();
            }
            return "<unknown>";
        } else {
            return "<enable debug to show caller>";
        }
    }


    private static class StackTracerException extends Exception {
    }

    private static void thrower() throws Exception {
        throw new StackTracerException();
    }

    public static Throwable stackTrace() {
        try {
            thrower();
        } catch (Exception e) {
            return e;
        }
        return null;
    }    
}
