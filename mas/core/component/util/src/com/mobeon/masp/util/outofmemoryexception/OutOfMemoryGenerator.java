package com.mobeon.masp.util.outofmemoryexception;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Sep 13, 2006
 * Time: 10:36:53 AM
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class is able to quickly generate an OutOfmemoryError. Known uses in MAS
 * is to be be used in conjunction with -XX:+HeapDumpOnOutOfMemoryError JVM flag.
 */
public class OutOfMemoryGenerator {

    static ILogger logger = ILoggerFactory.getILogger(OutOfMemoryGenerator.class);

    static int throwingTime;

    public void init(){
        if(throwingTime > 0){
            new Thread() {

                public void run() {
                    try {
                        logger.debug("OutOfMemoryGenerator is active and will sleep for: "+throwingTime);
                        Thread.sleep(throwingTime);
                        logger.debug("OutOfMemoryGenerator is awake. Starting to allocate memory.");
                        List<String> list = new ArrayList<String>();
                        boolean done = false;
                        int size = 100000;
                        char[] arr = new char[size];
                        for (int i=0;i<arr.length;i++){
                            arr[i] = 'h';
                        }
                        int i = 0;
                        while(! done){
                            i++;
                            char[] arr2 = new char[size];
                            System.arraycopy(arr, 0, arr2, 0, arr.length);
                            list.add(new String(arr2));
                            list.size();
                            if(i % 100 == 0){
                                logger.debug("OutOfMemoryGenerator has made "+i+" allocations");
                            }
                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch(OutOfMemoryError e){
                        logger.debug("OutOfMemoryGenerator caught OutOfMemoryError: Its job is now done.");
                    }

                }
            }.start();

        }
    }

    public void setThrowingTime(final int throwTime){
        throwingTime = throwTime;
    }
}
