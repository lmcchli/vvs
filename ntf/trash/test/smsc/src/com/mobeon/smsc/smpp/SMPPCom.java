package com.mobeon.smsc.smpp;


import java.net.Socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InterruptedIOException;
import java.io.EOFException;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.logging.*;
import java.util.Properties;

import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.util.delayline.DelayEventListener;
import com.mobeon.ntf.util.delayline.DelayItem;
import com.mobeon.ntf.util.delayline.Delayer;
import com.mobeon.smsc.config.Config;
import com.mobeon.smsc.interfaces.SmppConstants;
import com.mobeon.smsc.interfaces.TrafficCentral;
import com.mobeon.smsc.smpp.SMSComException;
import com.mobeon.smsc.smpp.in.DeliverSmRespPDU;
import com.mobeon.smsc.smpp.in.EnquireLinkPDU;
import com.mobeon.smsc.smpp.in.ReadBindRequestPDU;
import com.mobeon.smsc.smpp.in.ReadCancelRequestPDU;
import com.mobeon.smsc.smpp.in.ReadSMSRequestPDU;
import com.mobeon.smsc.smpp.out.BindResponsePDU;
import com.mobeon.smsc.smpp.out.CancelResponsePDU;
import com.mobeon.smsc.smpp.out.DeliverSmPDU;
import com.mobeon.smsc.smpp.out.EnquireLinkRespPDU;
import com.mobeon.smsc.smpp.out.SubmitSMSResponsePDU;
import com.mobeon.smsc.smpp.util.SMSPdu;


public class SMPPCom
    implements SmppConstants,
            DelayEventListener {

    /** General log target. All logs events are written to the main logfile, smsc.log*/
    private Logger log = null;

    /** Socket to read and write from and to */
    private Socket smppSock = null;

    /** InputStream to read SMPP data from */
    private InputStream smppIn = null;

    /** OutputStream to write SMPP data to */
    private OutputStream smppOut = null;

    /** ReadBindRequestPDU reads a bind request from an ESME*/
    private ReadBindRequestPDU bindTransmitter = null;
    private ReadBindRequestPDU bindTransceiver = null;

    /** BindResponsePDU writes the bind response to an ESME*/
    private BindResponsePDU bindTransmitterResponse = null;
    private BindResponsePDU bindTransceiverResponse = null;

    private boolean bindAsTransceiver = false;

    /** ReadSMSRequestPDU reads a SMPP request from an ESME*/
    private ReadSMSRequestPDU smsRequestPDU = null;

    /** SubmitSMSResponsePDU writes an SMPP response to an ESME*/
    private SubmitSMSResponsePDU submitSMSResponsePDU = null;

    private ReadCancelRequestPDU cancelRequest = null;
    private CancelResponsePDU cancelResponse = null;

    private EnquireLinkPDU enquireLinkRequest = null;
    private EnquireLinkRespPDU enquireLinkResponse = null;

    private DeliverSmPDU deliverRequest = null;
    private DeliverSmRespPDU deliverResponse = null;

    /** TrafficCentral collects information retrieved from an ESME*/
    private TrafficCentral trafficInfo = null;

    /** SMPPCom.PDUReader is a private class that reads SMPP data sent by the ESME*/
    private SMPPCom.PDUReader reader = null;

    /** timeout is the timelimit in millisec. to wait for requests before the connection is closed.*/
    private int timeout = 1000;

    /** should_live indicates if the SMPPCom.PDUReader should stop read.*/
    private boolean should_live = true;

    /** The name of the connected ESME, set after an bindrequest has been received.*/
    private String esmeName = null;

    /** The name of the connected ESMEs account, set after an bindrequest has been received.*/
    private String accountName = null;
    /** The password for the account */
    private String password = null;

    private Delayer delayer = new Delayer(null, "d", Config.getSms0delay(), this);
    private Logger smpplog;
    private static int dropCount = 0;
    /* Enable bad behavior for testing */
    private static int timeToRespond = 0;
    private static boolean respondTimeThreadStarted = false;

    private static boolean dieAll = false;

    public static void dontRespond(int dur) {
	timeToRespond = dur/5;
    }

    /**
     *Constructor.
     *@param s Socket for the SMPP connection.
     *@param i id of the SMPP connection.
     *@param trafficInfo is the interface to where all traffic
     * information are reported to.
     */
    public SMPPCom(Socket s, int i, TrafficCentral trafficInfo) {
	dieAll = false;
        smppSock = s;
        this.trafficInfo = trafficInfo;
        bindTransmitter = new ReadBindRequestPDU(SMPPCMD_BIND_TRANSMITTER);
        bindTransceiver = new ReadBindRequestPDU(SMPPCMD_BIND_TRANSCEIVER);
        bindTransmitterResponse = new BindResponsePDU(
                SMPPCMD_BIND_TRANSMITTER_RESP);
        bindTransceiverResponse = new BindResponsePDU(
                SMPPCMD_BIND_TRANSCEIVER_RESP);
        smsRequestPDU = new ReadSMSRequestPDU();
        submitSMSResponsePDU = new SubmitSMSResponsePDU();
	cancelRequest = new ReadCancelRequestPDU();
	cancelResponse = new CancelResponsePDU();
        enquireLinkRequest = new EnquireLinkPDU(this);
        enquireLinkResponse = new EnquireLinkRespPDU(this);
        deliverRequest = new DeliverSmPDU();
        deliverResponse = new DeliverSmRespPDU();
        reader = new SMPPCom.PDUReader();
        smpplog = Logger.getLogger("SMSC");
        initSocket();
	startRespondTimeThread();
    }

    private static synchronized void startRespondTimeThread() {
	if (!respondTimeThreadStarted) {
	    respondTimeThreadStarted = true;
	    new Thread() {
		public void run() {
		    while (true) {
			try { sleep(5000); } catch (InterruptedException e) { ; }
			if (timeToRespond > 0) { --timeToRespond; }
		    }
		}
	    }.start();
	}
    }
    
    public static void killAllConnections() {
	dieAll = true;
    }

    /**
     * Creates a logger object used to log information.
     * @param the name of logger to use. name = ESME name.
     */
    private void initLogger(String name) {
        log = Logger.getLogger(name);
    }

    /**
     * initSocket initiates the outputstream and the inputstream from/to the ESME.
     * @return true if the initiation was successful, false otherwise.
     */
    private boolean initSocket() {
        try {
            synchronized (reader) {
                if (smppSock != null) {
                    smppIn = new BufferedInputStream(smppSock.getInputStream());
                    smppOut = new BufferedOutputStream(
                            smppSock.getOutputStream());
                    smppSock.setSoTimeout(timeout);
                    smppSock.setSoLinger(true, 5);
                }
                reader.notifyAll();
            }
            reader.start();
        } catch (IOException e) {
            System.out.println("Initiation failed. " + e);
            return false;
        } catch (Exception e) {
            System.out.println("Unknown error, initiation failed. " + e);
        }
        return true;
    }

    /**
     * closes the SMPP connection to the ESME
     */
    public void disconnect() {
        smpplog.severe("Closing socket for ESME " + esmeName + "_" + accountName);
        if( esmeName != null && accountName != null ) {
            trafficInfo.freeESMEAccount(esmeName, accountName);
        }
        if (smppSock != null) {
            try {
                smppSock.close();
            } catch (IOException e) {}
        }
        synchronized (reader) {
            smppSock = null;
            should_live = false;
            delayer.stopDelayer();
            delayer = null;
            reader.notifyAll();
        }
        reader.releasePDU();
    }

    /**
     * handleBindRequest reads the bindrequest and sends back a bind response to the ESME.
     * The response code is returned from the trafficInfo.bindESMEAccount function.
     */
    private void handleBindRequest(int commandId) throws SMSComException {
        try {
            ReadBindRequestPDU bind;
            BindResponsePDU bindresp;

            if (commandId == SMPPCMD_BIND_TRANSCEIVER) {
                bind = bindTransceiver;
                bindresp = bindTransceiverResponse;
                bindAsTransceiver = true;
            } else {
                bind = bindTransmitter;
                bindresp = bindTransmitterResponse;
                bindAsTransceiver = false;
            }
            readRequest(bind);
            esmeName = bind.getSystemType();
            accountName = bind.getSystemId();
            password = bind.getPassword();
            initLogger(esmeName);
            reader.setName(esmeName + "_" + accountName + "_r");
            System.out.println("Esme=" + esmeName + ", account=" + accountName +
		", password=" + password);
            int command_status = trafficInfo.bindESMEAccount(esmeName,
                    accountName, password);

            writeBuffer(
                    bindresp.getBuffer(esmeName, bind.getSequenceNumber(),
                    command_status));

            switch (command_status) {
            case SMPPSTATUS_ROK:
                if (log.isLoggable(Level.FINE)) {
                    log.fine(
                            "ESME " + esmeName + "_" + accountName
                            + " succesfully bound to SMS-C");
                }
                break;

            case SMPPSTATUS_RTHROTTLED:
                if (log.isLoggable(Level.FINE)) {
                    log.fine(
                            "Could not bind " + esmeName + "_" + accountName
                            + ". Throttled. (0x"
                            + Integer.toHexString(SMPPSTATUS_RTHROTTLED) + ")");
                }
                disconnect();
                break;

            default:
                log.severe(
                        "Could not bind ESME " + esmeName + "_" + accountName
                        + ". Reason: 0x" + Integer.toHexString(command_status));
                disconnect();
                break;
            }
        } catch (IOException e) {
            log.severe("Could not bind ESME correctly. " + e);
            disconnect();
        }
    }

    public void delayCompleted(Object o) {
        sendDeliver((SMSPdu) ((DelayItem) o).getItem());
    }

    private void sendDeliver(SMSPdu pdu) {
        try {
            SMSAddress to = new SMSAddress(pdu.getSourceAddrTon(),
                    pdu.getSourceAddrNpi(), pdu.getSourceAddr());
            SMSAddress from = new SMSAddress(pdu.getDestAddrTon(),
                    pdu.getDestAddrNpi(), pdu.getDestinationAddr());
            SMSMessage msg = new SMSMessage(null, 0);

            msg.setServiceType(pdu.getServiceType());
            writeBuffer(deliverRequest.getBuffer(to, from, msg));
        } catch (Exception e) {
            log.severe(
                    "SMPPCom got unexpected exception " + NtfUtil.stackTrace(e));
        }
    }

    /**
     * Reads a SMS request from the inputstream and sends back a SMPP response.
     * The response code is always set to 0x0000000, OK.
     * The sequence number and the message id is the same as in the SMS request.
     **/
    private void handleSubmitRequest() throws SMSComException {
	boolean dropResponse = false;	

        try {
            SMSPdu p = (SMSPdu) (readRequest(smsRequestPDU));

            if (p != null) {
                p.setAccountName(accountName);
                trafficInfo.logSMSRequest(esmeName, accountName, p);
                if ((p.getRegisteredDelivery() & 0x3) == 1) { // DeliveryReceipt
                    if( bindAsTransceiver ) {
                        delayer.add(new DelayItem(p));
                    } else {
                        submitSMSResponsePDU.setCommandStatus(0x4); 
                    }
                }
            }
            if (Config.getResponseDelay() > 0) {
		int validityPeriod = p.getValidityPeriodSec();
                try {
			if(Config.getResponseDelay() >= validityPeriod)
			{
				log.fine("Response delay >= validity period, drop response.\n");
				dropResponse = true;
			}
			else
			{
				log.fine("Delay response for "+Config.getResponseDelay()+" s.\n");
				Thread.sleep(Config.getResponseDelay()*1000);
			}
                } catch (InterruptedException e) {
                    ;
                }
            }
            
            System.out.println("" + Config.getDropEvery() + " " + dropCount);
            if (dropResponse || (Config.getDropEvery() > 0 && dropCount >= Config.getDropEvery())) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Dropping response");
                }
                dropCount = 0;
            } else {
                int res = trafficInfo.getSubmitPDUResponseCode(esmeName, accountName);
                if (res >= 0) {
                    submitSMSResponsePDU.setCommandStatus(res);
                }
                writeBuffer(
                        submitSMSResponsePDU.getBuffer
                        (smsRequestPDU.getSequenceNumber(),
                        smsRequestPDU.getMessageId()));
                dropCount++;
                submitSMSResponsePDU.setCommandStatus(0);
            }
        } catch (IOException e) {
            log.severe(
                    "Could not send/read SMS delivery request correctly. " + e);
        }
    }

    /**
     **/
    private void handleEnquireLinkRequest() throws SMSComException {
        try {
            readRequest(enquireLinkRequest);
            enquireLinkResponse.setSequenceNumber(
                    enquireLinkRequest.getSequenceNumber());
            int res = trafficInfo.getEnquireLinkResponseCode(esmeName, accountName);
            enquireLinkResponse.setCommandStatus(res);
            writeBuffer(enquireLinkResponse.getBuffer());
        } catch (IOException e) {
            log.severe(
                    "Could not send/read SMS delivery request correctly. " + e);
        }
    }

    /**
     **/
    private void handleCancelRequest() throws SMSComException {
        try {
            readRequest(cancelRequest);

            SMSPdu p = new SMSPdu();
	    p.setAccountName(accountName);
	    p.setShortMessage("CANCEL SM");
	    p.setCommandId(cancelRequest.getCommandId());
	    p.setCommandLength(cancelRequest.getCommandLength());
	    p.setCommandStatus(cancelRequest.getCommandStatus());
	    p.setSequenceNumber(cancelRequest.getSequenceNumber());
	    p.setSourceAddrTon(cancelRequest.getSourceAddrTon());
	    p.setSourceAddrNpi(cancelRequest.getSourceAddrNpi());
	    p.setSourceAddr(cancelRequest.getSourceAddr());
	    p.setDestAddrTon(cancelRequest.getDestAddrTon());
	    p.setDestAddrNpi(cancelRequest.getDestAddrNpi());
	    p.setDestinationAddr(cancelRequest.getDestinationAddr());
	    p.setServiceType(cancelRequest.getServiceType());
	    trafficInfo.logSMSRequest(esmeName, accountName, p);

            cancelResponse.setSequenceNumber(
					     cancelRequest.getSequenceNumber());
            int res = trafficInfo.getCancelResponseCode(esmeName, accountName);
            cancelResponse.setCommandStatus(res);
            writeBuffer(cancelResponse.getBuffer());
        } catch (IOException e) {
            log.severe(
		       "Could not send/read SMS cancel request correctly. " + e);
        }
    }

    /**
     **/
    private void handleDeliveryResponse() throws SMSComException {
        try {
            readRequest(deliverResponse);
        } catch (IOException e) {
            log.severe(
                    "Could not send/read SMS delivery request correctly. " + e);
        }
    }

    /****************************************************************
     * readRequest reads an expected response PDU requests from the ESME. If a
     * PDU other than the expected is received, the connection is logged out and
     * closed and an SMSCom exception is thrown.
     * @param pdu the expected PDU to read (either ReadBindRequestPDU or
     * ReadSMSRequestPDU in this case)
     * @param expectedCommandId the command id of the expected PDU type.
     */
    private Object readRequest(SMPP_PDU pdu) throws SMSComException, IOException {
        Object result = null;
        SMPP_PDU readPDU;

        readPDU = reader.getPDU();
        if (readPDU == null) {
            throw new SMSComException("SMPPCom timeout waiting for response");
        }
        if (readPDU.getCommandId() == pdu.getCommandId()) {

            result = pdu.parseBody(readPDU);
            reader.releasePDU();
        } else {
            disconnect();
            throw new SMSComException(
                    "Expected command id " + pdu.getCommandId() + " not "
                    + readPDU.getCommandId());
        }
        return result;
    }

    /****************************************************************
     * writeBuffer writes a byte array back to the ESME.
     * @param buf the bytes to write.
     */
    private void writeBuffer(byte[] buf) throws IOException {
	if (timeToRespond <= 0) {
	    smpplog.fine("\n<--\n" + NtfUtil.hexDump(buf));
	    smppOut.write(buf);
	    smppOut.flush();
	} else {
	    smpplog.fine("\n<-- suppressed\n" + NtfUtil.hexDump(buf));
	}
    }

    /****************************************************************
     * PDUReader loops forever, toggling between two states; either it waits for
     * an SMPP PDU, or it waits for someone to consume the PDU last read.
     */
    private class PDUReader extends Thread {
        private boolean pduAvailable;
        private SMPP_PDU pdu;
        private SMSComException lastException;

        public PDUReader() {
            super(Thread.currentThread().getName() + "_r");
            pdu = new SMPP_PDU();
        }

        /****************************************************************
         * getPDU waits until a PDU is available. This PDU may be read before or
         * after this function is called, depending on in what order threads are
         * scheduled.
         * @return the read PDU, or null if there was a timeout waiting for the PDU.
         */
        public SMPP_PDU getPDU() throws SMSComException {
            synchronized (pdu) {
                while (!pduAvailable) {
                    try {
                        pdu.wait(timeout);
                        if (!pduAvailable) {
                            return null;
                        }
                    } catch (InterruptedException e) {
                        if (lastException != null) {
                            throw lastException;
                        }
                        return null;
                    }
                }
                return pdu;
            }
        }

        /****************************************************************
         * releasePDU informs the reader thread that the PDU is free for
         * reuse. The reader thread will not do anything until the PDU is
         * released.
         */
        public void releasePDU() {
            synchronized (pdu) {
                pduAvailable = false;
                pdu.notifyAll();
            }
        }

        /****************************************************************
         * waitForPDURelease waits until the PDU is no longer in use. If
         * that takes too long, it releases the PDU anyway and returns.
         */
        private void waitForPDURelease() {
            while (pduAvailable) {
                synchronized (pdu) {
                    try {
                        pdu.wait(timeout);
                    } catch (InterruptedException e) {
                        releasePDU();
                    }
                }
            }
        }

        /****************************************************************
         * handlePDU handles the request from the ESME
         */
        private void handlePDU() throws IOException, SMSComException {
            switch (pdu.getCommandId()) {
            case SMPPCMD_BIND_TRANSMITTER:
            case SMPPCMD_BIND_TRANSCEIVER:
                pduAvailable = true;
                handleBindRequest(pdu.getCommandId());
                break;

            case SMPPCMD_SUBMIT_SM:
                pduAvailable = true;
                handleSubmitRequest();
                break;

            case SMPPCMD_ENQUIRE_LINK:
                pduAvailable = true;
                handleEnquireLinkRequest();
                break;

            case SMPPCMD_CANCEL_SM:
                pduAvailable = true;
                handleCancelRequest();
                break;

            case SMPPCMD_DELIVER_SM_RESP:
                pduAvailable = true;
                handleDeliveryResponse();
                break;

            case SMPPCMD_UNBIND:
                disconnect();
                break;

            default:
                synchronized (pdu) {
                    pduAvailable = true;
                    pdu.notifyAll();
                }
            }
        }

        /****************************************************************
         * run waits for someone else to connect. Then it reads all SMPP PDUs
         * that arrive.
         */
        public void run() {
            while (should_live && !dieAll) {
                try {
                    while (should_live && !dieAll) {
                        waitForPDURelease();
                        synchronized (this) {
                            while (smppSock == null) {
                                wait();
                            }
                            lastException = null;
                            try {
                                if (pdu.read(smppIn)) {
                                    smpplog.fine(
                                            "\n-->\n"
                                                    + NtfUtil.hexDump(
                                                            pdu.getBuffer()));
                                    handlePDU();
                                } 
                            } catch (InterruptedIOException e) {
                                smpplog.severe(
                                        "Could not read data SMPP PDU. "
                                                + NtfUtil.stackTrace(e));
                                disconnect();
                            } catch (EOFException e) {
                                smpplog.severe(
                                        "Could not read data SMPP PDU. "
                                                + NtfUtil.stackTrace(e));
                                disconnect();
                            } catch (IOException e) {
                                smpplog.severe(
                                        "Could not read data SMPP PDU. "
                                                + NtfUtil.stackTrace(e));
                                disconnect();
                            } catch (Exception e) {
                                smpplog.severe(
                                        "Could not read data SMPP PDU. "
                                                + NtfUtil.stackTrace(e));
                                disconnect();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println(
                            "Unexpected exception: " + NtfUtil.stackTrace(e));
                    should_live = false;
                } catch (OutOfMemoryError e) {
                    System.err.println("Out of memory: " + NtfUtil.stackTrace(e));
                    System.exit(0);
                }
            }
        }
    }
}
