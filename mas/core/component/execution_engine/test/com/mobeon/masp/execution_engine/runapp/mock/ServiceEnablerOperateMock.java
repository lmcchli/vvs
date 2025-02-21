package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerOperate;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 11, 2006
 * Time: 10:44:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceEnablerOperateMock extends BaseMock implements ServiceEnablerOperate {

    private String service;
    private String host;
    private int port;

    public ServiceEnablerOperateMock(String service, String host, int port){
        this.service = service;
        this.host = host;
        this.port = port;
    }
    public void unlock() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void lock() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void open() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close(boolean forced) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateThreshold(int highWaterMark, int lowWaterMark, int threshold) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getProtocol() {
        return "unkown";
    }
}
