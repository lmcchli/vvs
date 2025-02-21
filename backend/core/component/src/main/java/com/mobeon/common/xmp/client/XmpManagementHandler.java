/*
 * XmpManagementHandler.java
 *
 * Created on den 29 november 2005, 14:22
 */

package com.mobeon.common.xmp.client;

public interface XmpManagementHandler {
    
    public void statusUp(String service, String instanceName);
    
    public void statusDown(String service, String instanceName);
    
    public void sendOk(String service);
    
    public void sendFailed(String service);
}
