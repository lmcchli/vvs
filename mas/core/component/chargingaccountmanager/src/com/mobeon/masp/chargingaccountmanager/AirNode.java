package com.mobeon.masp.chargingaccountmanager;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Date: 2007-dec-14
 *
 * @author emahagl
 */
public class AirNode {

    private String host;
    private int port;
    private String uid;
    private String pwd;

    public AirNode(String host, int port, String uid, String pwd) {
        this.host = host;
        this.port = port;
        this.uid = uid;
        this.pwd = pwd;
    }

    public String getUid() {
        return uid;
    }

    public String getPwd() {
        return pwd;
    }

    public URL asURL() {
        try {
            return new URL("http://" + host + ":" + port + "/Air");
        } catch (MalformedURLException e) {
            System.out.println("Exception in asURL " + e);
        }
        return null;
    }
}
