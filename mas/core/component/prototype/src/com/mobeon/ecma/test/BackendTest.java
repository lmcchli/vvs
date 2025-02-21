/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.ecma.test;

import sun.net.www.http.HttpClient;

import java.net.URL;
import java.io.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.backend.TerminalSubscription;
import com.mobeon.backend.exception.NoUserException;
import com.mobeon.application.util.Expression;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-mar-14
 * Time: 11:17:22
 * To change this template use File | Settings | File Templates.
 */
public class BackendTest {
    public static Logger logger = Logger.getLogger("com.mobeon");

    private String scriptFile;
    private String loadedSrc;


    public boolean loadSrc(String URI) {
        String proxy = null;
        int proxyPort = -1;
        StringBuffer buff = new StringBuffer();
        // Todo : Retrieve info about http proxy from somewhere and use that info.
        if (URI.startsWith("http://")) {
            try {
                HttpClient client = new HttpClient(new URL(URI),proxy, proxyPort);
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null){
                    buff.append(line);
                    buff.append("\n");
                }
                loadedSrc =  buff.toString();
            } catch (IOException e) {
                logger.error("Failed to access to URIValidator " + URI, e);
                return false;
            }
        }
        else if (URI.startsWith("file://")) {
            URI = URI.substring(7);
        }
        // Assume we now have a file URIValidator
        try {
            String VXMLDir = System.getProperty("VXML_ROOTDIR");
            if (!URI.startsWith("/") && VXMLDir != null )
                URI = VXMLDir + "/" + URI;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(URI))));
            String line;
            while ((line = reader.readLine()) != null){
                buff.append(line);
                buff.append("\n");
            }
            loadedSrc =  buff.toString();
        } catch (IOException e) {
            logger.error("Failed to access to file " + URI, e);
            return false;
        }
        return true;
    }

    public String getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(String scriptFile) {
        this.scriptFile = scriptFile;
    }

    public static void main(String argv[]) {
        PropertyConfigurator.configure("lib/mobeon.properties");
        BackendTest bt = new BackendTest();
        bt.setScriptFile(argv[0]);
        bt.loadSrc(argv[0]);

        ECMAExecutor ecma = new ECMAExecutor();
        ecma.exec(bt.loadedSrc);
        try {
            ecma.putIntoScope("MAS_A_NUM", "222");
            ecma.putIntoScope("MAS_C_NUM", "111");
            ecma.putIntoScope("terminalsubscription", new TerminalSubscription("111"));
        } catch (NoUserException e) {
            logger.error("Exception caught! ", e);
            return ;
        }
        ecma.exec("attachSubscription();");
        // Integer numMsg = (Integer) ecma.exec("getNumOfVoiceMsg(inbox);") ;
        // int msgCount = numMsg.intValue();
        Expression expression = new Expression("getNumOfVoiceMsg(inbox);");
        ecma.putIntoScope("numMsg", expression.eval(ecma));
        ecma.newScope();
        logger.debug("Number of messages: " + ecma.exec("numMsg"));
        ecma.exec("numMsg = numMsg - 1;");
        logger.debug("Number of messages: " + ecma.exec("numMsg"));
        ecma.leaveScope();
        logger.debug("(Left scope) Number of messages: " + ecma.exec("numMsg"));

        String media = null;

        ecma.exec("for (i = 0; i < getNumOfVoiceMsg(inbox); i++) { print(getVoiceMsgMedia(inbox,i)); };");

        ecma.putIntoScope("aNewMsg", "c:/tmp/goodmorning.wav");
        ecma.exec("storeMsg(aNewMsg);");
    }
}
