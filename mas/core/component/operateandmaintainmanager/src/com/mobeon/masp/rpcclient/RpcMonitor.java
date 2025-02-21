/* RpcMonitor - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package com.mobeon.masp.rpcclient;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoRead;
import com.mobeon.masp.operateandmaintainmanager.ConnectionStatistics;

import java.io.IOException;
import java.util.HashMap;

public class RpcMonitor implements MonitorOperations
{
    ILogger log;
    private RpcClient client;
    private boolean monitorStarted;
    private boolean connected = false;


    public RpcMonitor(String hostName, Integer port) {
        ILoggerFactory.configureAndWatch("/opt/moip/config/mas/log4j2.xml");
        log = ILoggerFactory.getILogger(RpcMonitor.class);
        log.debug("Create RpcMonitor");
        client = new RpcClient(hostName,port.toString());
        connect(); // is connected ?
    }

    public String startMonitor() throws IOException {
        String result = client.sendCommand("startMonitor", "");
        // store result
        monitorStarted = result.contains("started");
        return result;
    }

    public void stopMonitor() throws IOException {
        client.sendCommand("stopMonitor", "");
        monitorStarted = false;
    }


    public HashMap<String, SessionInfoRead> getMonitorConnectionData() throws IOException {
    HashMap<String, SessionInfoRead> retValue;
    if (monitorStarted) {
        try {
            retValue = client.getMonitorConnectionData();
        } catch (IOException e) {
            monitorStarted = false;
            throw new IOException(e.getMessage() );
            //e.printStackTrace();
        }
    }
    else {
        startMonitor();
        retValue = new HashMap<String, SessionInfoRead>();
    }
    return retValue;
    }

    // monitor is running
    public boolean monitorStarted(){
        return monitorStarted;
    }

    // Connection is established
    public boolean connected() {
        return connected;
    }

    /**
     * @depricated
     * @return
     * @throws IOException
     */
    public ConnectionStatistics getMonitorStatisticData() throws IOException {
        ConnectionStatistics retValue;
        try {
            //retValue = client.getMonitorStatisticData();
            retValue = client.getMonitorStatisticData();
        } catch (IOException e) {
            monitorStarted = false;
            throw new IOException(e.getMessage() );
        }
        return retValue;
    }

    private void connect(){
        // chek if able to communicate with omm
        String strRunning;
        try {
            strRunning = client.sendCommand("running", "");
            if (strRunning.equals("true")) {
                connected = true;
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        connected = false;

    }

}
