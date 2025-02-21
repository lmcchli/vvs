/**
 * CmpTool.java
 *
 * Simple command-line tool for tests of UDP communication, like using telnet
 * for TCP.
 * Written for use with CMP.
 */

import java.util.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.io.*;

public class CmpTool extends Thread {
    
    private static final String getreq = "Type=Get\n\n";
    private static final String startreq = "Type=Set\n\nstart=true\n";
    private static final int ANY = -99;

    private DatagramSocket sock = null;
    private boolean updateSend = false;
    private InetAddress sendAddress = null;
    private int sendPort = 0;
    
    /**
     * Constructor for CmpTool, creates datagram socket
     */
    public CmpTool(String send, String recv) throws Exception {
        int port;
        String adr;
        adr = getAddress(recv);
        port = getPort(recv);
        if ("any".equals(adr)) {
            if (ANY == port) {
                sock = new DatagramSocket();
            } else {
                sock = new DatagramSocket(port);
            }
        } else {
            sock = new DatagramSocket(port, InetAddress.getByName(adr));
        }
        
        adr = getAddress(send);
        port = getPort(send);
        if ("any".equals(adr)) {
            updateSend = true;
        } else {
            sendAddress = InetAddress.getByName(adr);
            sendPort = port;
        }
    }
    
    private int getPort(String adr) throws Exception {
        String s = adr.substring(adr.indexOf(":") + 1);
        if ("any".equals(s)) {
            return ANY;
        } else {
            return Integer.parseInt(s);
        }
    }

    private String getAddress(String adr) throws Exception {
        return (adr.substring(0, adr.indexOf(":")));
    }

    /**
     * Sends a CMP request to the last host and port.
     *@param request - the request to send.
     *@return upon a successfully sent request, it returns 0.
     * Otherwise it returns -1.
     */
    private void sendRequest(String request) throws Exception {
        if (sendAddress == null) { return; }
        
        request = "" + request.length() + "\n" + request;
        DatagramPacket packet =
            new DatagramPacket(request.getBytes(), request.length(), sendAddress, sendPort);
        sock.send(packet);
    }
    
    /**
     * Sends a Start event
     *@param index - index of the started managed object
     *@param name - name of the started managed object
     *@return upon a successfully sent Start event, it returns 0.
     * Otherwise it returns -1.
     */
    public void sendStart(int index, String name) throws Exception {
        sendRequest("Type=Start\nIndex=" + index + "\nName=" + name);
    }
    
    /**
     * Sends a Start event
     *@param index - index of the started managed object
     *@param intstanceIndex - The index of the service instance
     *@param name - name of the started managed object
     *@return upon a successfully sent Start event, it returns 0.
     * Otherwise it returns -1.
     * /
    public int sendInstanceStart(int index, int instanceIndex, String name) {
        StringBuffer tmp = new StringBuffer();
        StringBuffer buff = new StringBuffer();
        tmp.append("Type=Start\n");
        tmp.append("Index="); tmp.append(index); tmp.append("\n");
        tmp.append("InstanceIndex="); tmp.append(instanceIndex); tmp.append("\n");
        tmp.append("Name=" + name + "\n");
        buff.append(tmp.length()); buff.append("\n");
        buff.append(tmp.toString());
        try {
            LOG.logMessage("Sending instance start message " + buff, LOG.L_DEBUG );
            DatagramPacket packet = new DatagramPacket(buff.toString().getBytes(),
            buff.length(),
            d_latestHost,
            d_latestPort);
            c_udpSocket.send(packet);
        }
        catch (IOException ioe) {LOG.logMessage(ioe.getMessage(), LOG.L_ERROR); return -1; }
        return 0;
    }
*/    
    
    /**
     * Listen to datagram socket and sort data into CMPData objects
     * @return ArrayList with CMPData objects
     */
    public void listenToEvent() throws Exception {
        byte[] buff = new byte[16384];
        String strPacket = null;
        DatagramPacket packet = new DatagramPacket(buff, 0, buff.length);
        sock.receive(packet);
        int len = packet.getLength();
        byte[] tmp = new byte[len];
        System.arraycopy(packet.getData(), 0, tmp, 0, len);
        if (updateSend) {
            sendPort = packet.getPort();
            sendAddress = packet.getAddress();
        }
        System.out.println("================>\n" + new String(tmp, 0, tmp.length, "UTF-8"));
    }
    
    public void closeSocket() {
        if (sock != null)
            sock.close();
    }

    public boolean predefinedRequest(String line) throws Exception {
        if ("startreq".equals(line)) {
            sendRequest(startreq);
        } else if ("getreq".equals(line)) {
            sendRequest(getreq);
        } else if ("startev".equals(line)) {
            sendStart(0, "test");
        } else {
            return false;
        }
        return true;
    }

    public void run() {
        try {
            while (true) {
                listenToEvent();
            }
        } catch (Exception e) {
            System.err.println("Exception reading: " + e);
        }
    }

    private static void help() {
        System.out.println("CmpTool is a tool to test UDP communication. It is developed to test\n"
                           + "NTFs CMP protocol. When you press enter, the line is sent to the other\n"
                           + "end. If you need to send multiple lines in one request, you type \"req\" as the\n"
                           + "first line and \"endreq\" as the last line. The lines between will be sent\n"
                           + "as one request. You can also type \"start\" which will send a CMP start request,\n"
                           + "or \"get\" which will send a CMP get event. Each request is preceded by a line\n"
                           + "with the length (as CMP specifies).\n\n");
        System.out.println("Usage:\n"
                           + "java -cp . CmpTool sendhost:sendport receivehost:receiveport\n"
                           + "\n"
                           + "A host or port can be replaced by the word \"any\":\n"
                           + "Send: any:any means always send back to the source of the last packet.\n"
                           + "Receive: any:any means receive from the broadcast address on a system-selected port.\n"
                           + "         any:port means receive from the specified port on the broadcast address.\n"
                           + "         host:any means receive from a system-selected port on the broadcast address.\n");
    }

    public static void main(String[] args) {
        try {
            boolean buildingRequest = false;
            String request = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            boolean serv = false;
            
            if (args.length != 2) {
                help();
                System.exit(0);
            }
            
            CmpTool s = new CmpTool(args[0], args[1]);
            s.start();
            
            System.out.println("NTF CMP tool v1.0");

            String line;
            while (true) {
                System.out.print("" + s.sock.getLocalAddress() + ":" + s.sock.getLocalPort()
                                 + "|" + s.sendAddress + ":" + s.sendPort + ">");
                line = in.readLine();
                if (buildingRequest) {
                    if ("endreq".equals(line)) {
                        buildingRequest = false;
                        s.sendRequest(request);
                        request = "";
                    } else {
                        request = request + line + "\n";
                    }
                } else {
                    if ("quit".equals(line)
                        || "qui".equals(line)
                        || "q".equals(line)
                        || "exit".equals(line)) {
                        System.exit(0);
                    } else if ("req".equals(line)) {
                        buildingRequest = true;
                    } else if (!s.predefinedRequest(line)) {
                        s.sendRequest(line);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }
}

