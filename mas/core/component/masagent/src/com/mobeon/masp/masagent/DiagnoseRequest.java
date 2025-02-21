package com.mobeon.masp.masagent;

import com.mobeon.masp.operateandmaintainmanager.DiagnoseService;
import com.mobeon.masp.operateandmaintainmanager.ServiceInstance;
import com.mobeon.masp.operateandmaintainmanager.Status;

/**
 * User: eperber
 * Date: 2006-mar-01
 * Time: 14:33:32
 */
public class DiagnoseRequest extends Thread {
    private final String name;
    private final String host;
    private final long port;
    private final DiagnoseService diagSvc;
    private final ServiceInstance si;
    private final MasConnection masConnection;
    private Status status;

    public DiagnoseRequest(String name,String host,long port, DiagnoseService diagSvc, ServiceInstance si, MasConnection masConnection) {
	this.name = name;
    this.host = host;
    this.port = port;
	this.diagSvc = diagSvc;
	this.si = si;
	this.masConnection = masConnection;
    }

    public void run() {
	status = diagSvc.serviceRequest(si);
	String mapName = name + "-" + host + "-" + port;
        masConnection.updateStatus(mapName, status);
    }

    public Status getStatus() {
	return status;
    }

    public String getServiceName() {
	return name;
    }
    public String getServiceHost() {
	return host;
    }

    public long getServicePort() {
	return port;
    }

}
