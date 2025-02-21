package com.abcxyz.messaging.vvs.ntf.sms.smpp.test.client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SMPPTestClientLogger {
    private static FileWriter fw = null;
    private static BufferedWriter bw = null;

    private static void openFile() {
        try {
            fw = new FileWriter("/opt/moip/logs/ntf/smppclient.log", true);
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            // TODO - Add log message
            e.printStackTrace();
        }
    }

    private static void closeFile() {
        try {
            bw.close();
        } catch (IOException e) {
            // TODO - Add log message
            e.printStackTrace();
        }
    }

    static void writeLogMessageToFile(String message) {
        try {
            openFile();
            bw.write(message);
            closeFile();
        } catch (IOException e) {
            // TODO - Add log message
            e.printStackTrace();
        }
    }

    static void clearLogs() {
        try {
            new FileWriter("/opt/moip/logs/ntf/smppclient.log");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
