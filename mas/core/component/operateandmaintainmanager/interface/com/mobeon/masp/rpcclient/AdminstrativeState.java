package com.mobeon.masp.rpcclient;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public enum AdminstrativeState {
    UNLOCKED(0, "unlocked"),
    LOCKED(1, "locked"),
    SHUTDOWN(2, "shutdown");

    private String info;
    private int index;

    AdminstrativeState(int index, String info) {
	this.index = index;
	this.info = info;
    }

    public String getInfo() {
	return this.info;
    }

    public int getIndex() {
	return index;
    }

    public static AdminstrativeState fromInfo(String info) {
	for (AdminstrativeState state : values()) {
	    if(state.getInfo().equals(info)) {
		return state;
	    }
	}
	throw new IllegalArgumentException(info + " is not part of AdministrativeState.");
    }

    public static AdminstrativeState fromIndex(int index) {
	for (AdminstrativeState state : values()) {
	    if(state.getIndex() == index) {
		return state;
	    }
	}
	throw new IllegalArgumentException(index + " is not an index of AdministrativeState.");
    }
}