/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package com.mobeon.client.SIP;
import com.mobeon.util.ErrorCodes;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;
import java.text.ParseException;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * This class is a UAC template. SipClientUA is the guy that shoots and shootme
 * is the guy that gets shot.
 *
 *@author M. Ranganathan
 */

public class SipClientUA implements SipListener {

	private static SipProvider tcpProvider;
	private static SipProvider udpProvider;
	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;
	private static SipStack sipStack;
	private int reInviteCount;
	private ContactHeader contactHeader;
	private ListeningPoint tcpListeningPoint;
	private ListeningPoint udpListeningPoint;
	private int counter;
    private Dialog _dialog;
    private UserInterface ui;

	protected ClientTransaction inviteTid;

	protected static final String usageString =
		"java "
			+ "examples.shootist.SipClientUA \n"
			+ ">>>> is your class path set to the root?";

	private static void usage() {
		System.out.println(usageString);
		System.exit(ErrorCodes.SUCCESS);

	}
	public void shutDown() {
		try {

            ui.setTerminate(true);
            ui.interrupt();
            ui = null;
            try {
            Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
			System.out.println("nulling reference");
			sipStack.deleteListeningPoint(tcpListeningPoint);
			sipStack.deleteListeningPoint(udpListeningPoint);
			// This will close down the stack and exit all threads
			tcpProvider.removeSipListener(this);
			udpProvider.removeSipListener(this);
			while (true) {
			  try {
			      sipStack.deleteSipProvider(udpProvider);
			      sipStack.deleteSipProvider(tcpProvider);
			      break;
			    } catch (ObjectInUseException  ex)  {
			        try {  
					Thread.sleep(2000);
			     	} catch (InterruptedException e) {
					continue;
			     	}
			   }
			}
			sipStack = null;
			tcpProvider = null;
			udpProvider = null;
			this.inviteTid = null;
			this.contactHeader = null;
			addressFactory = null;
			headerFactory = null;
			messageFactory = null;
			this.udpListeningPoint = null;
			this.tcpListeningPoint = null;
			this.reInviteCount = 0;
			System.gc();
		} catch (Exception ex) { ex.printStackTrace(); }
	}
	

	public void processRequest(RequestEvent requestReceivedEvent) {
		Request request = requestReceivedEvent.getRequest();
		ServerTransaction serverTransactionId =
			requestReceivedEvent.getServerTransaction();

		System.out.println(
			"\n\nRequest "
				+ request.getMethod()
				+ " received at "
				+ sipStack.getStackName()
				+ " with server transaction id "
				+ serverTransactionId);

		// We are the UAC so the only request we get is the BYE.
		if (request.getMethod().equals(Request.BYE))
			processBye(request, serverTransactionId);

	}
    public void sendInfo(String msg) {
        if (this._dialog != null)  {
            Request infoRequest = null;
            msg = "Signal=" + msg + "\r\nDuration=250\r\n";
            try {
                infoRequest = _dialog.createRequest(Request.INFO);
                ContentTypeHeader cth = headerFactory.createContentTypeHeader("application", "dtmf-relay");
                infoRequest.setContent(msg, cth);
            } catch (SipException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.err.println("********** FAILED TO CREATE infoRequest");
                return;
            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.err.println("********** FAILED TO PARSE EXPRESSION");
                return;

            }
            ClientTransaction ct = null;
            try {
                ct = udpProvider.getNewClientTransaction(infoRequest);
            } catch (TransactionUnavailableException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                try {
                    ct = tcpProvider.getNewClientTransaction(infoRequest);
                } catch (TransactionUnavailableException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    System.err.println("********** NO TCP PROVIDER AVAILABLE, BAILING OUT");
                }
            }
            try {
                _dialog.sendRequest(ct);
                System.out.println("SipClientUA: INFO request sent");
            } catch (SipException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.err.println("********* FAILED TO SEND REQUEST");
            }

        }
        else {
            System.err.println("******** NO DIALOG, CAN NOT SEND INFO REQUEST");
        }
    }

        public void sendBye() {
        if (this._dialog != null)  {
            Request byeRequest = null;
            try {
                byeRequest = this._dialog.createRequest(Request.BYE);
            } catch (SipException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return;
            }
            ClientTransaction tr = null;
            try {
                tr = udpProvider.getNewClientTransaction(byeRequest);
            } catch (TransactionUnavailableException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return;
            }
            try {
                this._dialog.sendRequest(tr);
            } catch (SipException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return;
            }
        }
        else {
            System.err.println("******** NO DIALOG, CAN NOT SEND INFO REQUEST");
        }
    }

	public void processBye(
		Request request,
		ServerTransaction serverTransactionId) {
		try {
			System.out.println("shootist:  got a bye .");
			if (serverTransactionId == null) {
				System.out.println("shootist:  null TID.");
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			System.out.println("Dialog State = " + dialog.getState());
			Response response = messageFactory.createResponse
						(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("shootist:  Sending OK.");
			System.out.println("Dialog State = " + dialog.getState());

			// so that the finalization method will run 
			// and exit all resources.
			this.shutDown();

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(ErrorCodes.SUCCESS);
		}
	}

	public void processResponse(ResponseEvent responseReceivedEvent) {
		System.out.println("Got a response");
		Response response = (Response) responseReceivedEvent.getResponse();
		Transaction tid = responseReceivedEvent.getClientTransaction();

		System.out.println(
			"Response received with client transaction id "
				+ tid
				+ ":\n"
				+ response.getStatusCode());
		if (tid == null) {
			System.out.println("Stray response -- dropping ");
			return;
		}
		System.out.println("transaction state is " + tid.getState());
		System.out.println("Dialog = " + tid.getDialog());
		System.out.println("Dialog State is " + tid.getDialog().getState());

		try {
			if (response.getStatusCode() == Response.OK
				&& ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
					.getMethod()
					.equals(
					Request.INVITE)) {
				Dialog dialog = tid.getDialog();
				Request ackRequest = dialog.createRequest(Request.ACK);
				System.out.println("Sending ACK");
                if (this._dialog == null)
                    this._dialog = dialog;

				dialog.sendAck(ackRequest);
                System.out.println("ACK sent");
                if (this.ui == null)
                    this.ui = new UserInterface(this);
                
				// Send a Re INVITE but this time force it
				// to use UDP as the transport. Else, it will
				// Use whatever transport was used to create
				// the dialog.
                /*
				if (reInviteCount == 0) {
				    Request inviteRequest =
					dialog.createRequest(Request.INVITE);
				    ((SipURI)inviteRequest.getRequestURI()).removeParameter("transport");
				    ((ViaHeader)inviteRequest.getHeader(ViaHeader.NAME)).setTransport("udp");
				    inviteRequest.addHeader(contactHeader);
				    try {Thread.sleep(100); } catch (Exception ex) {}
				    ClientTransaction ct =
					udpProvider.getNewClientTransaction(inviteRequest);
                    System.out.println("Sending RE-INVITE");
				    dialog.sendRequest(ct);
				    reInviteCount ++;
				}
                */

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(ErrorCodes.SUCCESS);
		}

	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

		System.out.println("Transaction Time out" );
	}

	public void init(int port, String peerAddr, boolean isAdmin, String ipaddress) throws UnknownHostException {
        _dialog = null;
        ui = null;
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist"); // Identification of the SipStack vendor
		Properties properties = new Properties();
		// If you want to try TCP transport change the following to tcp
		String transport = "udp";
		String peerHostPort = InetAddress.getLocalHost().getHostAddress() + ":5060";
		properties.setProperty("javax.sip.IP_ADDRESS", InetAddress.getLocalHost().getHostAddress());
		/*
        properties.setProperty(
			"javax.sip.OUTBOUND_PROXY",
			peerAddr + "/" + transport);
		properties.setProperty(
			"javax.sip.ROUTER_PATH",
			"com.mobeon.client.SIP.MyRouter");
        */
		properties.setProperty("javax.sip.STACK_NAME", "NISTv1.2");
		properties.setProperty("javax.sip.RETRANSMISSION_FILTER",
				"ON");

		// The following properties are specific to nist-sip
		// and are not necessarily part of any other jain-sip
		// implementation.
		// You can set a max message size for tcp transport to
		// guard against denial of service attack.
		properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE",
					"1048576");
		properties.setProperty(
			"gov.nist.javax.sip.DEBUG_LOG",
			"SipClientUA.log");
		properties.setProperty(
			"gov.nist.javax.sip.SERVER_LOG",
			"SipClientUAServer.log");

		// Drop the client connection after we are done with the transaction.
		properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
		// Set to 0 in your production code for max speed.
		// You need  16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "16");

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("createSipStack " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(0);
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
            System.err.println("Binding to port " + port);
			udpListeningPoint = sipStack.createListeningPoint
							(port, "udp");
			udpProvider = sipStack.createSipProvider
							(udpListeningPoint);
			SipClientUA listener = this;
			udpProvider.addSipListener(listener);




			tcpListeningPoint = sipStack.createListeningPoint
								(port, "tcp");
			tcpProvider = sipStack.createSipProvider(tcpListeningPoint);
			tcpProvider.addSipListener(listener);

			SipProvider sipProvider = transport.equalsIgnoreCase("udp")?
					udpProvider: tcpProvider;

			String fromName = "BigGuy";
			String fromSipAddress = "here.com";
			String fromDisplayName = "The Master Blaster";

			String toSipAddress = "there.com";
			String toUser = "LittleGuy";
			String toDisplayName = "The Little Blister";

			// create >From Header

			SipURI fromAddress = null;
            Address fromNameAddress = null;
            if (!isAdmin) {
			    fromAddress = addressFactory.createSipURI(fromName, fromSipAddress);
                fromNameAddress = addressFactory.createAddress(fromAddress);
                fromNameAddress.setDisplayName(fromDisplayName);
            }
            else {
                fromAddress = addressFactory.createSipURI(toUser, toSipAddress);
                fromNameAddress = addressFactory.createAddress(fromAddress);
                fromNameAddress.setDisplayName(toDisplayName);
            }
			FromHeader fromHeader =
				headerFactory.createFromHeader(fromNameAddress, "12345");

			// create To Header
			SipURI toAddress =
				addressFactory.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader =
				headerFactory.createToHeader(toNameAddress, null);

			// create Request URIValidator
			SipURI requestURI =
				addressFactory.createSipURI(toUser, peerHostPort);

			// Create ViaHeaders

			ArrayList viaHeaders = new ArrayList();
			ViaHeader viaHeader =
				headerFactory.createViaHeader(
					sipStack.getIPAddress(),
					sipProvider.getListeningPoint().getPort(),
					transport,
					null);


			// add via headers
			viaHeaders.add(viaHeader);

			// Create ContentTypeHeader
			ContentTypeHeader contentTypeHeader =
				headerFactory.createContentTypeHeader("application", "sdp");

			// Create a new CallId header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();

			// Create a new Cseq header
			CSeqHeader cSeqHeader =
				headerFactory.createCSeqHeader(1, Request.INVITE);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards =
				headerFactory.createMaxForwardsHeader(70);

			// Create the request.
			Request request =
				messageFactory.createRequest(
					requestURI,
					Request.INVITE,
					callIdHeader,
					cSeqHeader,
					fromHeader,
					toHeader,
					viaHeaders,
					maxForwards);
			// Create contact headers
			String host = sipStack.getIPAddress();

			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(tcpListeningPoint.getPort());

			// Create the contact name address.
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(sipProvider.getListeningPoint().getPort());

			Address contactAddress = addressFactory.createAddress(contactURI);

			// Add the contact address.
			contactAddress.setDisplayName(fromName);

			contactHeader =
				headerFactory.createContactHeader(contactAddress);
			request.addHeader(contactHeader);

			// Add the extension header.
			Header extensionHeader =
				headerFactory.createHeader("My-Header", "my header value");
			request.addHeader(extensionHeader);

            if (ipaddress == null) {
                ipaddress = "192.168.1.69";
            }
			String sdpData =
				"v=0\r\n"
					+ "o=4855 13760799956958020 13760799956958020"
					+ " IN IP4 " + ipaddress +"\r\n"
					+ "s=mysession session\r\n"
					+ "p=+46 8 52018010\r\n"
					+ "c=IN IP4  " + ipaddress + "\r\n"
					+ "t=0 0\r\n"
					+ "m=audio 11110 RTP/AVP 0 4 18\r\n"
					+ "a=rtpmap:0 G711/8000\r\n"
					+ "a=rtpmap:4 G723/8000\r\n"
					+ "a=rtpmap:18 G729A/8000\r\n"
					+ "a=ptime:20\r\n";

			byte[]  contents = sdpData.getBytes();
			request.setContent(contents, contentTypeHeader);

			extensionHeader =
				headerFactory.createHeader(
					"My-Other-Header",
					"my new header value ");
			request.addHeader(extensionHeader);

			Header callInfoHeader =
				headerFactory.createHeader(
					"Call-Info",
					"<http://www.antd.nist.gov>");
			request.addHeader(callInfoHeader);


			// Create the client transaction.
			listener.inviteTid = sipProvider.getNewClientTransaction(request);

			// send the request out.
			listener.inviteTid.sendRequest();

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}
	}

	public static void main(String args[]) {
        int port = 0;
        boolean isAdmin = false;
        String streamIpAddress = null;
        String peerAddr = null;
        if (args[0] != null)
            port = new Integer(args[0]).intValue();
         if (args.length == 2)
            peerAddr = args[1];
        if (args.length == 3)
            streamIpAddress = args[2];
        if (args.length == 4 && args[3].equals("admin"))
            isAdmin = true;



        try {
            new SipClientUA().init(port, peerAddr, isAdmin, streamIpAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
