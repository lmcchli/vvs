/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package com.mobeon.client.SIP;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-25
 * Time: 10:30:45
 * To change this template use File | Settings | File Templates.
 */
public class UserInterface extends Thread {
    private BufferedReader userinput;
    private SipClientUA SIPClient;
    private boolean terminate;

    public UserInterface(SipClientUA sipClient) {
        this.userinput = null;
        this.SIPClient = sipClient;
        terminate = false;
        this.start();
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    public void run() {
        userinput = new BufferedReader(new InputStreamReader(System.in));

        String message = null;
        try {
            while (!terminate) {
                System.out.println("ENTER DATA TO SEND OR BYE: ");
                try {
                    message = userinput.readLine();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    System.err.println("********** FAILED TO READ USER INPUT");
                }
                if (terminate) {
                    return;
                }
                if (message.equals("BYE")) {
                    SIPClient.sendBye();
                    SIPClient.shutDown();
                    return;
                }
                else {
                    System.out.println("USERINTERFACE: Sending " + message );
                    SIPClient.sendInfo(message);
                }
            }
        }
        catch (Exception e)  {
            e.printStackTrace();
        }
    }
}
