package com.mobeon.masp.rpcclient;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.ConnectionStatistics;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoRead;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

/**
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 * TODO Remember to put license information in RC_MAS.
 */
public class RpcClient {

    private XmlRpcClient client = null;
    private ILogger log;

    public RpcClient(String host, String port) {
        log = ILoggerFactory.getILogger(RpcClient.class);
        try {
            URL url = new URL("http://" + host + ":" + port);
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(url);

            client = new XmlRpcClient();
            client.setConfig(config);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        log.debug("RPC Client initilized [Port:" + port + ", host:" + host + "]");
    }

    synchronized public HashMap<String, SessionInfoRead> getMonitorConnectionData() throws IOException {
        HashMap<String, SessionInfoRead> retValue = null;
        Vector<String> params = new Vector<String>();

        Object obj;

        try {
            obj = client.execute("RPCHandler.getMonitorConnectionData", params);
            retValue = (HashMap<String, SessionInfoRead>) XmlRpcEncode.decode((byte[]) obj);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    synchronized public ConnectionStatistics getMonitorStatisticData() throws IOException {
        Vector<String> params = new Vector<String>();
        ConnectionStatistics conStat = new ConnectionStatistics();
        Object obj;

        try {
            obj = client.execute("RPCHandler.getMonitorConnectionStatistic", params);
            conStat = (ConnectionStatistics) XmlRpcEncode.decode((byte[]) obj);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return conStat;
    }

    /**
     * Send a command to OMM.
     * The return value can be a String or null.
     *
     * @param command
     * @param action
     * @return null or a string
     * @throws IOException
     */
    synchronized public String sendCommand(String command, String action) throws IOException {
        log.debug("Send command cmd:" + command + ":" + action);
        String retValue = null;
        Vector<String> params = new Vector<String>();

        if (!action.equals("")) {
            params.add(action);
        }

        try {
            retValue = (String) client.execute("RPCHandler." + command, params);
        } catch (XmlRpcException e) {
            log.debug(e.getMessage());
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * Send a command to OMM.
     * The return value can be a String or null.
     *
     * @param command
     * @param action
     * @return null or a string
     * @throws IOException
     */
    synchronized public String sendCommand(String command, Vector action) throws IOException {
        String retValue = null;

        log.debug("Send command cmd:" + command + ":" + action);
        try {
            retValue = (String) client.execute("RPCHandler." + command, action);
            log.debug("Command sent");
        } catch (XmlRpcException e) {
            log.debug("Command FAILED");
            log.debug(e.getMessage());
            e.printStackTrace();
        }
        return retValue;
    }

    synchronized public Object getMibAttributes() throws IOException {
        Object mibAttrib;
        Vector<String> params = new Vector<String>();
        Object obj;

        try {
            obj = client.execute("RPCHandler.getMibAttributes", params);
            mibAttrib = XmlRpcEncode.decode((byte[]) obj);
        } catch (XmlRpcException e) {
            log.debug(e.getMessage());
            mibAttrib = null;
        }

        return mibAttrib;
    }

    public void finalize() throws Throwable {
        client = null;
        super.finalize();
    }
}
