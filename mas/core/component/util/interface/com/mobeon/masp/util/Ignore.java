/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;


public class Ignore {
    public static void reflectionException(Exception e) {

    }

    public static void ioException(IOException e) {

    }

    public static void exception(Exception e) {}


    public static void interruptedException(InterruptedException e) {}

    public static void cloneNotSupportedException(CloneNotSupportedException e) {}

    public static void noSuchMethodException(NoSuchMethodException e) {}

    public static void illegaleAccessException(IllegalAccessException e) {
    }

    public static void invocationTargetException(InvocationTargetException e) {
    }

    public static void numberFormatException(NumberFormatException nfe) {
    }

    public static void uriSyntaxException(URISyntaxException e) {
    }


}
