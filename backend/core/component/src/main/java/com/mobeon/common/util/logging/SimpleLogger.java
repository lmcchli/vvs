/*
 * StandardOutLogger.java
 *
 * Created on den 30 september 2005, 15:07
 */

package com.mobeon.common.util.logging;

import java.io.*;
import java.util.Date;


public class SimpleLogger implements ILogger  {
    private int level;
    private FileWriter writer;
    
    public static final int L_DEBUG = 5;
    public static final int L_INFO = 4;
    public static final int L_WARN = 3;
    public static final int L_ERROR = 2;
    public static final int L_FATAL = 1;
    
    private static SimpleLogger inst;
    
    public static SimpleLogger getLogger() {
        if( inst == null ) {
            inst = new SimpleLogger();
        }
        return inst;
    }
    
    private SimpleLogger() {
        this.level = L_DEBUG;
        try { 
            writer = new FileWriter("test.log", true);
        } catch(Exception e) {
            // do nothing
        }
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public void setFileName(String fileName) {
        try { 
            writer = new FileWriter(fileName, true);
        } catch(Exception e) {
            System.out.println("Excpetion " + e);
        }
    }
    
    private String getDate() {
        String res = new Date().toString();
        res += " [" + Thread.currentThread().getName() + "]  ";
        return res;
    }
        
    public void debug(Object message) {
        if( level >= L_DEBUG ) {
            try {
                writer.write(getDate() + message.toString() + "\n");
                writer.flush();
            } catch(IOException ioe) {}
        }
    }
    
    public void debug(Object message, Throwable t) {
        if( level >= L_DEBUG ) {
            try {
                writer.write(getDate() +  message.toString()+ ": " + t.toString() + "\n");
                writer.flush();
            } catch(IOException ioe) {}
        }
    }
    
    public void error(Object message) {
        if( level >= L_ERROR ) {
            try {
                writer.write(getDate() +  message.toString() + "\n");
                writer.flush();
            } catch(IOException ioe) {}
        }
    }
    
    public void error(Object message, Throwable t) {
        if( level >= L_ERROR ) {
            try {
                writer.write(getDate() + message.toString()+ ": " + t.toString() + "\n");
                writer.flush();
            } catch(IOException ioe) {}
        }
        
    }
    
    public void fatal(Object message) {
        if( level >= L_FATAL ) {
            try {
                writer.write(getDate() + message.toString() + "\n");
                writer.flush();
            } catch(IOException ioe) {}
        }
    }
    
    public void fatal(Object message, Throwable t) {
        if( level >= L_FATAL ) {
            try {
                writer.write(getDate() + message.toString()+ ": " + t.toString() + "\n");
                writer.flush();
            } catch(IOException ioe) {}
        }
    }
    
    public void info(Object message) {
        if( level >= L_INFO ) {
            try {
                writer.write(getDate() + message.toString() + "\n");
                writer.flush();
            } catch(IOException ioe) {}
        }
    }
    
    public void info(Object message, Throwable t) {
        if( level >= L_INFO ) {
            try {
                writer.write(getDate() + message.toString()+ ": " + t.toString() + "\n");
                writer.flush();
            } catch(IOException ioe) {}
        }
    }
    
    public boolean isDebugEnabled() {
        return level >= L_DEBUG;
    }
    
    public void registerSessionInfo(String name, Object sessionInfo) {
    }
    
    public void warn(Object message) {
        if( level >= L_WARN ) {
            try {
                writer.write(getDate() + message.toString() + "\n");
                writer.flush();
            } catch(IOException ioe) {}
        }
    }
    
    public void warn(Object message, Throwable t) {
        if( level >= L_WARN ) {
            try {
                writer.write(getDate() + message.toString()+ ": " + t.toString() + "\n");
                writer.flush();
            } catch(IOException ioe) {}
        }
    }
    
}
