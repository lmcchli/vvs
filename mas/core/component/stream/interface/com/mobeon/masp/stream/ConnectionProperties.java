/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * Properties necessary to establish a connection to an endpoint.
 *
 * @author Jörgen Terner
 */
public class ConnectionProperties {
    public final int UNDEFINED_PORTNUMBER = -1;

    /** Ip-adress to remote endpoint for audio traffic. */
    private String audioHost;

    /** Ip-adress to remote endpoint for video traffic. */
    private String videoHost;

    /** 
     * Portnumber for RTP-traffic over the audio stream.
     * The corresponding RTCP port is assumed to be this portnumber + 1.
     */
    private int audioPort = UNDEFINED_PORTNUMBER;

    /**
     * Portnumber for RTP-traffic over the video stream.
     * If only audio is sent/received over a stream, this port is always
     * undefined. The corresponding RTCP port is assumed to be this 
     * portnumber + 1.
     */
    private int videoPort = UNDEFINED_PORTNUMBER;
    
    /** 
     * The length of time in milliseconds represented by the media 
     * in a packet.
     */
    private int pTime;

    private static volatile int defaultPTime = 40;
    private static volatile int defaultMaxPTime = 40;
    
    /**
     * Maximum amount of media that can be encapsulated in each packet (milliseconds)
     */
    private int maxPTime;

    /** MTU for the stream connection. */
    private int maximumTransmissionUnit;

    public String getAudioHost() {
        return audioHost;
    }

    public void setAudioHost(String host) {
        audioHost = host;
    }

    public String getVideoHost() {
        return videoHost;
    }

    public void setVideoHost(String host) {
        videoHost = host;
    }

    public int getAudioPort() {
        return audioPort;
    }

    public void setAudioPort(int port) {
        audioPort = port;
    }

    public int getVideoPort() {
        return videoPort;
    }

    public void setVideoPort(int port) {
        videoPort = port;
    }

    public int getMaximumTransmissionUnit() {
        return maximumTransmissionUnit;
    }

    public void setMaximumTransmissionUnit(int maximumTransmissionUnit) {
        this.maximumTransmissionUnit = maximumTransmissionUnit;
    }
    
    public int getPTime() {
        return pTime;
    }
    
    public void setPTime(int pTime) {
        this.pTime = pTime;
    }

    public int getMaxPTime() {
        return maxPTime;
    }

    public void setMaxPTime(int maxPTime) {
        this.maxPTime = maxPTime;
    }

    /**
     * Get the configured default value to use for ptime.
     * (Note that the default value of maxptime should be equal to ptime)
     * @return default ptime value
     */
    public static int getDefaultPTime() {
        return ConnectionProperties.defaultPTime;
    }
    
    /**
     * Get the configured default value to use for ptime.
     * (Note that the default value of maxptime should be equal to ptime)
     * @return default ptime value
     */
    public static int getDefaultMaxPTime() {
        return ConnectionProperties.defaultMaxPTime;
    }

    
    /**
     * Updates the value of the static default PTime. 
     * This method should only be called when the configuration has been updated. 
     * @param defaultPTime The new default ptime value to be used.
     */
    public static void updateDefaultPTimes(int defaultPTime, int defaultMaxPTime) {
	ConnectionProperties.defaultPTime = defaultPTime;
	ConnectionProperties.defaultMaxPTime = defaultMaxPTime;
    }
    

    public String toString() {
        String string = "";
        string += "AudioHost: [" + audioHost + "]:" + audioPort;
        string += ", ";
        string += "VideoHost: [" + videoHost + "]:" + videoPort;
        string += ", ";
        string += "pTime: " + pTime;
        string += ", ";
        string += "maxPTime: " + maxPTime;
        string += ", ";
        string += "MTU: " + maximumTransmissionUnit;
        return string;
    }
}
