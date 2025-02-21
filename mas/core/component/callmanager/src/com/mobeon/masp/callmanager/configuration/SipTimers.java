/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.IGroup;

/**
 * Contains values for the SIP timers specified in RFC 3261 that is
 * configurable in the SIP stack.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class SipTimers implements ConfigConstants{

    private static final ILogger LOG = ILoggerFactory.getILogger(SipTimers.class);
    private Integer t2 = null;
    private Integer t4 = null;
    private Integer timerB = null;
    private Integer timerC = null;
    private Integer timerD = null;
    private Integer timerF = null;
    private Integer timerH = null;
    private Integer timerJ = null;
    
    // Default value constants
    public static final int DEFAULT_T2 = 8;
    public static final int DEFAULT_T4 = 10;
    public static final int DEFAULT_TIMER_B = 6;
    public static final int DEFAULT_TIMER_C = 360;
    public static final int DEFAULT_TIMER_D = 64;
    public static final int DEFAULT_TIMER_F = 18;
    public static final int DEFAULT_TIMER_H = 64;
    public static final int DEFAULT_TIMER_J = 64;


    public Integer getT2() {
        return t2;
    }

    public void setT2(Integer t2) {
        this.t2 = t2;
    }

    public Integer getT4() {
        return t4;
    }

    public void setT4(Integer t4) {
        this.t4 = t4;
    }

    public Integer getTimerB() {
        return timerB;
    }

    public void setTimerB(Integer timerB) {
        this.timerB = timerB;
    }

    public Integer getTimerC() {
        return timerC;
    }

    public void setTimerC(Integer timerC) {
        this.timerC = timerC;
    }

    public Integer getTimerD() {
        return timerD;
    }

    public void setTimerD(Integer timerD) {
        this.timerD = timerD;
    }

    public Integer getTimerF() {
        return timerF;
    }

    public void setTimerF(Integer timerF) {
        this.timerF = timerF;
    }

    public Integer getTimerH() {
        return timerH;
    }

    public void setTimerH(Integer timerH) {
        this.timerH = timerH;
    }

    public Integer getTimerJ() {
        return timerJ;
    }

    public void setTimerJ(Integer timerJ) {
        this.timerJ = timerJ;
    }

    /**
     * Parses the configuration for sip timers and returns a
     * {@link SipTimers}.
     * @param sipTimersGroup
     * @return Null is returned if the configuration could not be parsed.
     */
    public static SipTimers parseSipTimers(IGroup sipTimersGroup) {

        SipTimers sipTimers = new SipTimers();

        try {
            sipTimers.setT2(sipTimersGroup.getInteger(SIP_TIMER_T2));
            sipTimers.setT4(sipTimersGroup.getInteger(SIP_TIMER_T4));
            sipTimers.setTimerB(sipTimersGroup.getInteger(SIP_TIMER_B));
            sipTimers.setTimerC(sipTimersGroup.getInteger(SIP_TIMER_C));
            sipTimers.setTimerD(sipTimersGroup.getInteger(SIP_TIMER_D));
            sipTimers.setTimerF(sipTimersGroup.getInteger(SIP_TIMER_F));
            sipTimers.setTimerH(sipTimersGroup.getInteger(SIP_TIMER_H));
            sipTimers.setTimerJ(sipTimersGroup.getInteger(SIP_TIMER_J));

        } catch (Exception e) {
            LOG.warn("Error when retrieving SIP timers from " +
                    "configuration. Default timer values are used " +
                    "instead. Error: " + e.getMessage());

            return null;
        }

        return sipTimers;
    }

    /**
     * Returns a default set of SIP timers.
     * @return A {@link SipTimers} instance is returned containing the default
     * timer values.
     */
    public static SipTimers getDefaultSipTimers() {
        SipTimers sipTimers = new SipTimers();

        sipTimers.setT2(DEFAULT_T2);
        sipTimers.setT4(DEFAULT_T4);
        sipTimers.setTimerB(DEFAULT_TIMER_B);
        sipTimers.setTimerC(DEFAULT_TIMER_C);
        sipTimers.setTimerD(DEFAULT_TIMER_D);
        sipTimers.setTimerF(DEFAULT_TIMER_F);
        sipTimers.setTimerH(DEFAULT_TIMER_H);
        sipTimers.setTimerJ(DEFAULT_TIMER_J);

        return sipTimers;
    }



    //=========================== Private methods =========================

    /**
     * Private constructor. The only way to create a
     * {@link SipTimers} is to parse a configuration using the
     * {@link SipTimers#parseSipTimers(com.mobeon.common.configuration.IGroup)}.
     */
    private SipTimers() {
    }

}
