/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.frontend.rtp;

import com.mobeon.frontend.Stream;
import com.mobeon.frontend.ControlSignal;
import com.mobeon.util.DTMF;
import com.mobeon.util.ErrorCodes;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

public class RTPStream implements Stream {
    static Logger logger = Logger.getLogger("com.mobeon");
    private final int handle;
    private static final String BASE_DIR = "c:/tmp";
    private int localPort;
    private int remotePort;
    private String remoteIpAddress;
    private ControlSignal controlSignalQ;
    private int payloadType = 0; // Default to PCMU/8000
    private boolean interrupted = false;
    private boolean interruptable = true;



    public RTPStream(String ipaddress, int localPort, int remotePort, int payloadType) {
        this.remoteIpAddress = ipaddress;
        this.remotePort = remotePort;
        this.localPort = localPort;
        this.controlSignalQ = null;
        this.payloadType = payloadType;
        handle = nativeRTPStream(this.localPort, this.remotePort,this.remoteIpAddress, payloadType);
    }

    public RTPStream(String ipaddress, ControlSignal controlSignal, int localPort, int remotePort, int payloadType) {
        this.remoteIpAddress =  ipaddress;
        this.controlSignalQ = controlSignal;
        this.localPort = localPort;
        this.remotePort = remotePort;
        this.payloadType = payloadType;
        handle = nativeRTPStream(this.localPort, this.remotePort,this.remoteIpAddress, payloadType);
        String dtmfType = System.getProperty("DTMF");
        if (dtmfType != null && !dtmfType.equals("SIP")) {
            logger.debug("Creating DTMF scanner");
            Thread scannerThread = new Thread(new StreamScanner(this), "DTMFScanner");
            scannerThread.start();
        }
    }

    public RTPStream(int localPort, int remotePort,
			  String destIpAddress, int payloadType) {
        this.remoteIpAddress =  destIpAddress;
        this.controlSignalQ = null;
        this.localPort = localPort;
        this.remotePort = remotePort;
        this.payloadType = payloadType;
        handle = nativeRTPStream(localPort, remotePort, destIpAddress, payloadType);
    }


    public void addControlToken(int token) {
        logger.debug("Queueing DTMF token " + token);
        controlSignalQ.putToken(token);
    }

    private static final native int nativeRTPStream(int localPort, 
						    int remotePort, 
						    String destIpAddress, int payloadType);

    public int sendFile(String filename, boolean interruptable) {
        return nativeSendFile(handle,filename, interruptable);
    }

    public native int nativeSendFile(int handle, String filename, boolean interruptable);
    
    public void autoScanner() {
        nativeAutoScanner(handle);
    }


    public native void nativeAutoScanner(int handle);

    public void endAutoScanner() {
        nativeEndAutoScanner(handle);
    }

    public native void nativeEndAutoScanner(int handle);

    public int receive(String filename, boolean interruptable) {

	    return nativeReceive(handle, filename, interruptable);
    }

    public native int nativeReceive(int handle, String filename, boolean interruptable);

    public boolean scanStream() {
        return nativeScanStream(handle);
    }

    public native boolean nativeScanStream(int handle);

    public void playPrompt(byte[] msg) throws InterruptedException {
        // Not implemented yet
    }

    public void playPrompt(String[] messageFiles, boolean interruptable) throws InterruptedException {
        interrupted = false;
        this.interruptable = interruptable;
        for (int i = 0; i < messageFiles.length; i++) {
            if (!interrupted)
                sendFile(messageFiles[i],interruptable);
        }
    }

    public String recordGetFilename() throws InterruptedException {
        return null; // Not implemented yet
    }

    public String record(boolean interruptable) throws InterruptedException {
    // Create temp. filename
        interrupted = false;
        this.interruptable = interruptable;
        String fname = null;
        try {
            fname = File.createTempFile("rec_",".wav",new File(BASE_DIR)).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
        receive(fname, interruptable);
        return fname;
    }

    public boolean scan() throws InterruptedException {
        return scanStream();
    }

    public void interrupt(boolean force) {
        if (interruptable || force) {
            logger.debug("Interrupting stream");
            interrupted = true;
            nativeInterrupt(handle);
        }
    }

    public native void nativeInterrupt(int handle);

    public void close() {
       // endAutoScanner();
    }

    static {
        try {
            System.loadLibrary("rtplib");
        }
        catch(Exception e) {
            System.err.println("Exception caught when loading library!");
            e.printStackTrace();
        }
    }




    public static void main(String argv[]) {
	System.out.println("In main");

	if (argv.length < 5) {
	    System.err.println("Usage: RTPStream localport remoteport destip filename send|receive");
	    System.exit(ErrorCodes.USER_ERROR);
	}

	int lp = Integer.parseInt(argv[0]);
	int rp = Integer.parseInt(argv[1]);
	String destIp = argv[2];
	String file = argv[3];
	String mode = argv[4];

	System.out.println("LP : " + argv[0] + " RP : " + argv[1] + " IP : " + destIp + " file : " + file + " mode : " + mode);

	System.out.println("Creating proxy...");
	RTPStream rtp = new RTPStream(lp, rp, destIp,0);
	
	if (mode.equals("send")) {
	    int ret = rtp.sendFile(file, true);
	    System.out.println("Received : " + ret);
	}
	else {
	    rtp.receive(file, false);
	}


    }

}

