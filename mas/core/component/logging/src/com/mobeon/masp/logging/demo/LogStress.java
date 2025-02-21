/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.logging.demo;

import com.mobeon.masp.logging.ILogger;
import com.mobeon.masp.logging.ILoggerFactory;

import java.util.Date;
import java.util.Random;
import java.text.SimpleDateFormat;


/**
 * Demo class used to assess the time spent using different logging schemas
 *
 * @author David Looberger
 */
public class LogStress {
    private static ILogger logger = ILoggerFactory.getILogger(LogStress.class);

    public static void main(String[] argv) {
        // Log using the naive method by calling logger.error(...) with a simple string
        // and there after call logger.debug(...) with the same string, but having the
        // debug level filtered out.
        int loopcount = 100000;
        Random rand = new Random();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String timestamp = "";


        timestamp = format.format(new Date());
        System.out.println(timestamp + " Starting naive logging with logging enabled (" + loopcount + " entries)");
        System.out.println("Using:            logger.fatal(\"Test \" + \"message \" + \" with \" + \" some random  \" + rand.nextInt(100000)+ \" concatenation\");");
        for (int i = 0; i < loopcount; i++) {
            logger.fatal("Test " + "message " + " with " + " some random  " + rand.nextInt(100000)+ " concatenation");
        }
        timestamp = format.format(new Date());
        System.out.println(timestamp + " End logging\n\n");


        timestamp = format.format(new Date());
        System.out.println(timestamp + " Starting naive, but using stingbuf, logging with logging enabled (" + loopcount + " entries)");
        System.out.println("Using:             logger.fatal(new StringBuffer().append(\"Test \").append(\"message \").append(\" with \").append(\" some random  \").append(rand.nextInt(100000)).append(\" concatenation\").toString());");
        for (int i = 0; i < loopcount; i++) {
            logger.fatal(new StringBuffer().append("Test ").append("message ").append(" with ").append(" some random  ").append(rand.nextInt(100000)).append(" concatenation").toString());
        }
        timestamp = format.format(new Date());
        System.out.println(timestamp + " End logging\n\n");



        timestamp = format.format(new Date());
        System.out.println(timestamp + " Starting naive loggin, no concatenation, with logging enabled (" + loopcount + " entries)");
        System.out.println("Using:             logger.fatal(rand.nextInt(100000));");
        for (int i = 0; i < loopcount; i++) {
            logger.fatal(rand.nextInt(100000));
        }
        timestamp = format.format(new Date());
        System.out.println(timestamp + " End logging\n\n");




        // Logging disabled
        System.out.println("Logging when loglevel is disabled\n\n");


        timestamp = format.format(new Date());
        System.out.println(timestamp + " Starting naive logging with logging disabled (" + loopcount + " entries)");
        System.out.println("Using:            logger.error(\"Test \" + \"message \" + \" with \" + \" some random  \" + rand.nextInt(100000)+ \" concatenation\");");
        for (int i = 0; i < loopcount; i++) {
            logger.error("Test " + "message " + " with " + " some random  " + rand.nextInt(100000)+ " concatenation");
        }
        timestamp = format.format(new Date());
        System.out.println(timestamp + " End logging\n\n");



        timestamp = format.format(new Date());
        System.out.println(timestamp + " Starting naive, but using stingbuf, logging with logging disabled (" + loopcount + " entries)");
        System.out.println("Using:             logger.error(new StringBuffer().append(\"Test \").append(\"message \").append(\" with \").append(\" some random  \").append(rand.nextInt(100000)).append(\" concatenation\").toString());");
        for (int i = 0; i < loopcount; i++) {
            logger.error(new StringBuffer().append("Test ").append("message ").append(" with ").append(" some random  ").append(rand.nextInt(100000)).append(" concatenation").toString());
        }
        timestamp = format.format(new Date());
        System.out.println(timestamp + " End logging\n\n");




        timestamp = format.format(new Date());
        System.out.println(timestamp + " Starting logging, no concatenation, with logging disabled (" + loopcount + " entries)");
        System.out.println("Using:            logger.error(rand.nextInt(100000));");
        for (int i = 0; i < loopcount; i++) {
            logger.error(rand.nextInt(100000));
        }
        timestamp = format.format(new Date());
        System.out.println(timestamp + " End logging\n\n");


        timestamp = format.format(new Date());
        System.out.println(timestamp + " Starting loop (if (false) ...), without concatenation and logging disabled (" + loopcount + " entries)");
        System.out.println("Using:  if (false) {\n" +
                "                logger.error(rand.nextInt(100000));\n" +
                "            }");
        for (int i = 0; i < loopcount; i++) {
            if (false) {
                logger.error(rand.nextInt(100000));
            }
        }
        timestamp = format.format(new Date());
        System.out.println(timestamp + " End logging\n\n");
    }
}
