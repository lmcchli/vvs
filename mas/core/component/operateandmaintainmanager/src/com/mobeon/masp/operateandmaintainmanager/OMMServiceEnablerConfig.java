package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class OMMServiceEnablerConfig {
    private String protocol;
    private Integer threshold;
    private Integer highWaterMark;
    private Integer lowWaterMark;

    public OMMServiceEnablerConfig(String protocol, Integer threshold, Integer highWaterMark, Integer lowWaterMark) {
        this.protocol = protocol;
        this.threshold = threshold;
        this.highWaterMark = highWaterMark;
        this.lowWaterMark = lowWaterMark;
    }

    public Integer getLowWaterMark() {
        return lowWaterMark;
    }

    public void setLowWaterMark(Integer lowWaterMark) {
        this.lowWaterMark = lowWaterMark;
    }


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getHighWaterMark() {
        return highWaterMark;
    }

    public void setHighWaterMark(Integer highWaterMark) {
        this.highWaterMark = highWaterMark;
    }

}
