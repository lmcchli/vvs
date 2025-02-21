package com.mobeon.masp.chargingaccountmanager;

import java.io.*;
import java.net.Socket;

/**
 * Date: 2008-jan-30
 *
 * @author emahagl
 */
public class XmlRpcSender {

    private String host;
    private int port;
    private String fileName;

    public XmlRpcSender(String host, int port, String fileName) {
        this.host = host;
        this.port = port;
        this.fileName = fileName;
    }

    public void runTest() {

        byte[] data = loadFile();

        Socket socket = null;

        try {
            socket = new Socket(host, port);
            OutputStream out = socket.getOutputStream();
            String toSend = "POST /Air HTTP/1.1\r\n";
            out.write(toSend.getBytes());

            toSend = "Content-Type: text/xml\r\n";
            out.write(toSend.getBytes());

            toSend = "User-Agent: IVR/3.1/1.0\r\n";
            out.write(toSend.getBytes());

            toSend = "Authorization: Basic dXNlcjp1c2Vy\r\n"; // user:user
            out.write(toSend.getBytes());

            toSend = "Content-Length: " + data.length + "\r\n";
            out.write(toSend.getBytes());

            toSend = "Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\r\n";
            out.write(toSend.getBytes());

            toSend = "Connection: Close\r\n\r\n";
            out.write(toSend.getBytes());

            InputStream in = socket.getInputStream();

            out.write(data);
            out.flush();

            byte[] buf = new byte[4 * 1024];  // 4K buffer
            int bytesRead = 0;
            while ((bytesRead = in.read(buf)) != -1) {
                System.out.print(new String(buf, 0, bytesRead));
            }

            out.close();
            in.close();
        } catch (IOException e) {
            System.out.println("Exception in runTest" + e);
            die();
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                }
                catch (IOException ignore) {
                }
            }
        }
    }

    private byte[] loadFile() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
            FileInputStream input = new FileInputStream(fileName);
            int ch;
            byte[] tmp = new byte[1024];
            while ((ch = input.read(tmp)) != -1) {
                os.write(tmp, 0, ch);
            }
            return os.toByteArray();
        } catch (FileNotFoundException e) {
            System.out.println("Exception in loadFile" + e);
            die();
        } catch (IOException e) {
            System.out.println("Exception in loadFile" + e);
            die();
        }
        return null;
    }

    public static void die() {
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length != 3) usage();

        String host = args[0];

        int port = 0;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            usage();
        }

        String fileName = args[2];

        XmlRpcSender xmlRpcTester = new XmlRpcSender(host, port, fileName);
        xmlRpcTester.runTest();
    }

    private static void usage() {
        System.out.println("Usage: com.mobeon.masp.chargingaccountmanager.XmlRpcSender <host> <port> <filename>");
        die();
    }
}
