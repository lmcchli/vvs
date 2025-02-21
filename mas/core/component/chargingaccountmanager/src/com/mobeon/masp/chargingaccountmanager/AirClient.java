package com.mobeon.masp.chargingaccountmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.*;

import java.net.ConnectException;
import java.net.URL;
import java.util.List;

/**
 * Handles the traffic towards an AIR server.
 *
 * @author emahagl
 */
public class AirClient implements IAir {
    private static ILogger log = ILoggerFactory.getILogger(AirClient.class);
    private static long disabledWaitTime = 5 * 60 * 1000;

    private URL url;
    private boolean available = true;
    private XmlRpcClient client;
    private long disabledTime;
    private int maxRetries = 0;
    private int timeoutValue = 10 * 1000; // 10 sec

    /**
     * Constructor, creates a new XmlRpcClient.
     *
     * @param url
     * @param uid
     * @param pwd
     */
    public AirClient(URL url, String uid, String pwd) {
        this.url = url;

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(url);

        if (uid != null && pwd != null) {
            config.setBasicUserName(uid);
            config.setBasicPassword(pwd);
        }

        client = new XmlRpcClient();
        client.setConfig(config);

        // Implement a new XmlRpcTransportFactory and set it into the client.
        XmlRpcTransportFactory xmlRpcTransportFactory = new XmlRpcTransportFactory() {
            public XmlRpcTransport getTransport() {
                return new MasXmlRpcHttpTransport(client);
            }
        };
        client.setTransportFactory(xmlRpcTransportFactory);
        // Set our own type factory for date types
        client.setTypeFactory(new DateTypeFactory(client));
    }

    /**
     * Executes a xmlrpc call.
     *
     * @param requestName
     * @param params
     * @return the result from the xmlrpc call
     * @throws XmlRpcException
     */
    public Object execute(String requestName, List params) throws XmlRpcException {
        if (log.isDebugEnabled()) {
            log.debug("Executing a xmlrpc call to " + url);
        }

        TimingOutCallback callback = new TimingOutCallback(timeoutValue); // Reason to use 3.1 instead of 1.2

        int retries = 0;
        XmlRpcException ex;
        do {
            try {
                client.executeAsync(requestName, params, callback);
                Object result = callback.waitForResponse();
                available = true;
                return result;
            } catch (XmlRpcException e) {
                log.error("Exception in execute, url=" + url + " " + e);
                ex = e;
            } catch (Throwable e) {
                log.error("Exception in execute, url=" + url + " " + e);
                throw new XmlRpcException(e.getMessage());
            }
        } while (retries++ < maxRetries);

        handleException(ex);

        throw ex;
    }

    /**
     * Looks at the exception and decides if it should be handled in a special way.
     *
     * @param e
     */
    private void handleException(XmlRpcException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            if (cause instanceof ConnectException) {
                setUnavailable();
            }
        }

        if (e instanceof TimingOutCallback.TimeoutException) {
            setUnavailable();
        }
    }

    private void setUnavailable() {
        if (log.isDebugEnabled()) {
            log.debug("AirClient with url=" + url + " is set to unavailable");
        }
        available = false;
        disabledTime = System.currentTimeMillis();
    }

    public boolean isAvailable() {
        if (System.currentTimeMillis() - disabledTime > disabledWaitTime) {
            if (disabledTime > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("AirClient with url=" + url + " was unavailable, testing it again");
                }
            }
            available = true;
            disabledTime = 0;
        }
        return available;
    }
}

/**
 * Extends default transport class just to set User-Agent header.
 */
class MasXmlRpcHttpTransport extends XmlRpcSunHttpTransport {
    private static String USER_AGENT = "IVR/3.1/1.0";

    MasXmlRpcHttpTransport(XmlRpcClient client) {
        super(client);
    }

    protected String getUserAgent() {
        return USER_AGENT;
    }
}


