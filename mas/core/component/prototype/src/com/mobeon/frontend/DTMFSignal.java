/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.frontend;

import com.mobeon.session.SIP.SIPConnection;
import com.mobeon.session.SessionConnection;
import com.mobeon.event.MASEventDispatcher;
import com.mobeon.event.MASEventListener;
import com.mobeon.event.types.MASUserInput;
import com.mobeon.event.types.MASEvent;
import com.mobeon.event.types.MASTimeout;
import com.mobeon.util.DTMF;
import com.mobeon.util.InterruptTimerTask;

import java.io.PrintStream;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: HemPC
 * Date: 2004-dec-13
 * Time: 13:00:24
 * To change this template use File | Settings | File Templates.
 */
public class DTMFSignal implements ControlSignal {
    static Logger logger = Logger.getLogger("com.mobeon");
    private SessionConnection connection;
    private MASEventDispatcher dispatcher;
    private LinkedList messageQ;
    private long maxTimeDifference = 0;
    private boolean stopRunning;

    public DTMFSignal(SessionConnection connection, MASEventDispatcher dispatcher, long timeDifference) {
        this.connection = connection;
        this.dispatcher = dispatcher;
        this.messageQ = new LinkedList();
        this.stopRunning = false;
        maxTimeDifference = timeDifference;
    }

    public String getToken() throws InterruptedException {
        String ret = null;
        Date currentTime = new Date();
        Token token;
        long diff = 0;

        synchronized (messageQ)
        {
            while (messageQ.size() == 0)
                messageQ.wait();
            token = (Token) messageQ.removeLast();
            ret = token.getValue();
            messageQ.notifyAll();
        }
        if ((diff = Math.abs(currentTime.getTime() - token.timedate.getTime())) < maxTimeDifference &&
                messageQ.size() < 2) {
            Thread.sleep(diff); // Sleep in order for tokens to appear within the timeframe
        }
        synchronized (messageQ)
        {
            if (messageQ.size() > 0) {
                Token nextToken = (Token) messageQ.getLast();
                while (Math.abs(token.timedate.getTime() - nextToken.timedate.getTime()) < maxTimeDifference) {
                    nextToken = (Token) messageQ.removeLast();
                    ret += " " + nextToken.getValue();
                    if (messageQ.size() > 0)
                        nextToken = (Token) messageQ.getLast();
                    else
                        break;
                }
            }
        }
        return ret;
    }


    public String getToken(long timeout, int maxTimeDifference) {
        String ret = null;
        Timer timer = new Timer();
        Date currentTime = new Date();
        Token token;
        long diff = 0;

        try {
            if (timeout > 0)
                timer.schedule(new InterruptTimerTask(Thread.currentThread()),timeout);
            synchronized (messageQ)
            {
                while (messageQ.size() == 0) {
                    messageQ.wait();
                }
                token = (Token) messageQ.removeLast();
                ret = token.getValue();
                timer.cancel();
                messageQ.notify();
            }
            if ((diff = Math.abs(currentTime.getTime() - token.timedate.getTime())) < maxTimeDifference &&
                messageQ.size() < 2) {
            Thread.sleep(diff); // Sleep in order for tokens to appear within the timeframe
        }
        synchronized (messageQ)
        {
            if (messageQ.size() > 0) {
                Token nextToken = (Token) messageQ.getLast();
                while (Math.abs(token.timedate.getTime() - nextToken.timedate.getTime()) < maxTimeDifference) {
                    nextToken = (Token) messageQ.removeLast();
                    ret += " " + nextToken.getValue();
                    if (messageQ.size() > 0)
                        nextToken = (Token) messageQ.getLast();
                    else
                        break;
                }
            }
        }
        return ret;
        } catch (InterruptedException e) {
            logger.debug("Timeout waiting for token!");
            timer.cancel();
            messageQ.notifyAll();
        }
        return ret;
    }

    public void putToken(String tok) {
        synchronized (messageQ)
        {
            logger.debug("Queueing token " + tok);
            messageQ.addFirst(new Token(DTMF.getDTMF(tok)));
            messageQ.notify();
            dispatcher.fire(new MASUserInput(this,DTMF.getDTMF(tok)));
        }
    }

    public void putToken(int tok) {
        synchronized (messageQ)
        {
            logger.debug("Queueing token " + tok);
            messageQ.addFirst(new Token(DTMF.getDTMF(tok)));
            messageQ.notify();
            dispatcher.fire(new MASUserInput(this, DTMF.getDTMF(tok)));
        }
    }
    /**
     *
     * @param value
     * @throws Exception
     */
    public void sendToken(String value) throws Exception {
        if (connection instanceof SIPConnection)
            ((SIPConnection)connection).getInfoHandler().sendInfo(value);
        else
            throw new Exception("The SessionConnection type used (" + connection.getClass().toString()  +
                    ") can not send the control token");
    }

    public void setStopRunning(boolean state) {
        stopRunning = state;
    }

    public void clearQueue() {
        logger.debug("Clearing control signal queue");
        synchronized(messageQ) {
            messageQ.clear();
            logger.debug("Cleared!");
            messageQ.notifyAll();
        }
    }

    private class Token {
        private String value;
        private Date timedate;
        public Token(String val) {
            this.value = val;
            timedate = new Date();
        }

        public long diff (Token token) {
            return Math.abs(this.timedate.getTime() - token.timedate.getTime());
        }

        public String getValue() {
            return value;
        }
    }
}
