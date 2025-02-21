package com.mobeon.masp.rpcclient;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */


/**
 * User: eperber
 * Date: 2006-apr-07
 * Time: 12:52:11
 */
public enum ReloadConfiguration {
    LOAD(1, "load"),     // Files are being loaded
    OK(2, "ok"),         // Read from disk ok
    NOK(3, "not ok"),    // Read from disk not ok, using backup
    FAILED(4, "failed"); // Read from disk not ok, and no backup was found

    private String info;
    private int index;

    ReloadConfiguration(int index, String info) {
	this.index = index;
	this.info = info;
    }

    public String getInfo() {
	return this.info;
    }

    public int getIndex() {
	return index;
    }

    public static ReloadConfiguration fromInfo(String info) {
	for (ReloadConfiguration state : values()) {
	    if (state.info.equals(info)) {
		return state;
	    }
	}
	throw new IllegalArgumentException(info + " is not part of ReloadConfiguration.");
    }

    public static ReloadConfiguration fromIndex(int index) {
	for (ReloadConfiguration state : values()) {
	    if (state.index == index) {
		return state;
	    }
	}
	throw new IllegalArgumentException(index + " is not an index of ReloadConfiguration.");
    }
}
