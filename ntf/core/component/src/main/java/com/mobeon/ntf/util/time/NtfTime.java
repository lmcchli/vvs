 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.time;

/****************************************************************
 * NtfTime provides a centralized source for a low-budget version
 * of the current time. NtfTime.now gives you the current time with one-second
 * resolution and is an alternative to currentTimeMillis if you only need
 * approximate timing. Everything is static, so the time starts running as soon
 * as the class is loaded.
 */
public class NtfTime {
    /** the time when NTF was started, in seconds */
    public static final int START_TIME = (int)(System.currentTimeMillis()/1000);
    /** now always contains the current time in seconds */
    static public volatile int now= (int)(System.currentTimeMillis()/1000);

    static {
        new Thread() {
            public void run () {
                setName("NtfTime");
                setPriority(MAX_PRIORITY - 1); //High priority to avoid skipping seconds
                while (true) {
                    now= (int)(System.currentTimeMillis()/1000);
                    try {sleep(1000);} catch(InterruptedException e) {}
                }
            }
        }.start();
    }

    public static void sleepUntil(int t) {
        int sleepTime;
        do {
            sleepTime = t - now;
            if (sleepTime > 1) {
                try {
                    Thread.sleep(sleepTime * 1000L);
                } catch (InterruptedException e) {
                    ;
                }
            }
        } while (sleepTime > 1);
    }

    /****************************************************************
     * main is used to test the class
     */
    /*    public static void main(String args[]) {

        new Thread() {
            public void run() {
                while(true) {
                    try{Thread.sleep(3000);}catch (InterruptedException e) {}
                    System.out.println("\t\t" + now);
                }
            }
        }.start();

        while (true) {
            try{Thread.sleep(5000);}catch (InterruptedException e) {}
            System.out.println(now);
        }
    }
    */
}

