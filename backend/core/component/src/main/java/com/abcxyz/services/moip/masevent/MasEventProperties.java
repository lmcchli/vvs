package com.abcxyz.services.moip.masevent;

/**
 * ENUM lists event properties that can be stored in scheduler event
 *
 */
public enum MasEventProperties {

	RECIPIENT_ID("rcp"),
	SENDER_ID("snd"),
	OMSA("omsa"),
	RMSA("rmsa"),
	OMSGID("omsg"),
	RMSGID("rmsg"),
	;

	private String name;

	MasEventProperties(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
