/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wireline;

public interface SS7ResponseHandler {
    public void handleResponse(String code, String id);
}
