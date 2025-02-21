/*
* Copyright (c) 2005 Mobeon AB. All Rights Reserved.
*/
package com.mobeon.frontend.rtp.util;

import org.apache.log4j.Logger;

import java.util.Vector;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-jan-21
 * Time: 13:13:39
 * To change this template use File | Settings | File Templates.
 */
public class PromptQueue {
    private Logger logger = Logger.getLogger("com.mobeon");
    private LinkedList q;
    private boolean stopRunning = false;

    public PromptQueue() {
        this.q = new LinkedList();
    }

    public void addPrompt(String prompt) {
        synchronized (q)
        {
            if (!stopRunning) {
                logger.debug("Adding " + prompt + " to prompt Q");
                q.addFirst(prompt);
            }
            q.notifyAll();
        }
    }

    public void addPrompt(Vector prompts) {
        synchronized (q)
        {
            if (!stopRunning) {
                for (Iterator it = prompts.iterator(); it.hasNext();) {
                    q.addFirst((String) it.next());
                }
            }
            q.notifyAll();
        }
    }


    public String getPrompt() throws InterruptedException {
        String ret = null;
        synchronized (q)
        {
            while (q.size() == 0 && !stopRunning)
                q.wait();
            try {
                if (!stopRunning)
                    ret = (String) q.removeLast();
                    logger.debug("Retrieving " + ret + " from prompt Q");
            } catch (NoSuchElementException e) {
                ret = null;
            }
            q.notifyAll();
        }
        return ret;
    }

    public int size() {
        int size = 0;
        synchronized (q)
        {
            size = q.size();
            q.notifyAll();
        }
        return size;
    }

    public void stop() {
        synchronized (q)
        {
            stopRunning=true;
        }
    }

}
