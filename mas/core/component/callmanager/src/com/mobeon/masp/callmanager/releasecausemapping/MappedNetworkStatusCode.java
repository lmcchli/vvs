/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.releasecausemapping;

/**
 * This class represents a mapped network status code. It contains the
 * network status code together with the name of the mapping scenario used.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class MappedNetworkStatusCode {

    private final String mappingName;
    private final int networkStatusCode;
    private int q850Priority = Integer.MAX_VALUE;

	public MappedNetworkStatusCode(String mappingName, int networkStatusCode) {
        this.mappingName = mappingName;
        this.networkStatusCode = networkStatusCode;
    }
	
	public MappedNetworkStatusCode(String mappingName, int networkStatusCode, int q850Priority) {
        this.mappingName = mappingName;
        this.networkStatusCode = networkStatusCode;
        this.q850Priority = q850Priority;
    }

    public String getMappingName() {
        return mappingName;
    }

    public int getNetworkStatusCode() {
        return networkStatusCode;
    }
    
    public int getQ850Priority() {
		return q850Priority;
	}

    public String toString() {
        return "MappingName = " + mappingName + ", Network Status Code = " +
                networkStatusCode;
    }
}
