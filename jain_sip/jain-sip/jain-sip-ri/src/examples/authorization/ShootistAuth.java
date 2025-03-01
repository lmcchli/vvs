package examples.authorization;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;
import java.text.ParseException;
import gov.nist.javax.sip.header.ProxyAuthenticate;
import gov.nist.javax.sip.header.SIPHeaderNames;

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme is
 * the guy that gets shot.
 * 
 * @author M. Ranganathan
 * @author Kathleen McCallum
 */

public class ShootistAuth implements SipListener {

	private static SipProvider sipProvider;
	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;
	private static SipStack sipStack;
	private ContactHeader contactHeader;
	private ListeningPoint udpListeningPoint;
	private ClientTransaction inviteTid;
	private Dialog dialog;
	long invco = 1;
        String peerHostPort = "127.0.0.1:5070";
        String transport = "udp";
        String USER_AUTH = "auth";
        String PASS_AUTH = "pass";
        String realm = "nist.gov";

	

	public void processRequest(RequestEvent requestReceivedEvent) {
		Request request = requestReceivedEvent.getRequest();
		ServerTransaction serverTransactionId = requestReceivedEvent
				.getServerTransaction();

		System.out.println("\n\nRequest " + request.getMethod()
				+ " received at " + sipStack.getStackName()
				+ " with server transaction id " + serverTransactionId);

		// We are the UAC so the only request we get is the BYE.
		if (request.getMethod().equals(Request.BYE))
			processBye(request, serverTransactionId);
	}

	public void processBye(Request request,
			ServerTransaction serverTransactionId) {
		try {
			System.out.println("shootist:  got a bye .");
			if (serverTransactionId == null) {
				System.out.println("shootist:  null TID.");
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			System.out.println("Dialog State = " + dialog.getState());
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("shootist:  Sending OK.");
			System.out.println("Dialog State = " + dialog.getState());

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	public void processResponse(ResponseEvent responseReceivedEvent) {
		System.out.println("Got a response");
		Response response = (Response) responseReceivedEvent.getResponse();
		ClientTransaction tid = responseReceivedEvent.getClientTransaction();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

		System.out.println("Response received : Status Code = "
				+ response.getStatusCode() + " " + cseq);
		if (tid == null) {
			System.out.println("Stray response -- dropping ");
			return;
		}
		System.out.println("transaction state is " + tid.getState());
		System.out.println("Dialog = " + tid.getDialog());
		System.out.println("Dialog State is " + tid.getDialog().getState());

		try {
			if (response.getStatusCode() == Response.OK) {
				if (cseq.getMethod().equals(Request.INVITE)) {
					Request ackRequest = dialog.createRequest(Request.ACK);
					System.out.println("Sending ACK");
					dialog.sendAck(ackRequest);
				} else if (cseq.getMethod().equals(Request.CANCEL)) {
					if (dialog.getState() == DialogState.CONFIRMED) {
						// oops cancel went in too late. Need to hang up the dialog.
						System.out.println("Sending BYE -- cancel went in too late !!");
						Request byeRequest = dialog.createRequest(Request.BYE);
						ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
						dialog.sendRequest(ct);
					}
				}
			}else if ( response.getStatusCode()==Response.PROXY_AUTHENTICATION_REQUIRED || 
                                   response.getStatusCode()==Response.UNAUTHORIZED) {
                            URI uriReq = tid.getRequest().getRequestURI();
                            Request authrequest = this.processResponseAuthorization(response, uriReq);
                            
                            inviteTid = sipProvider.getNewClientTransaction(authrequest);
                            inviteTid.sendRequest();
                            System.out.println("INVITE AUTHORIZATION sent:\n"+authrequest);
                            invco++;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

		System.out.println("Transaction Time out");
	}

	public void sendCancel() {
		try {
			System.out.println("Sending cancel");
			Request cancelRequest = inviteTid.createCancel();
			ClientTransaction cancelTid = sipProvider
					.getNewClientTransaction(cancelRequest);
			cancelTid.sendRequest();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

        public Request processResponseAuthorization(Response response, URI uriReq){
            Request requestauth = null;
            try {
                System.out.println("processResponseAuthorization()");
                String schema = ((ProxyAuthenticate)(response.getHeader(SIPHeaderNames.PROXY_AUTHENTICATE))).getScheme();
                String nonce = ((ProxyAuthenticate)(response.getHeader(SIPHeaderNames.PROXY_AUTHENTICATE))).getNonce();
                ProxyAuthorizationHeader proxyAuthheader = headerFactory.createProxyAuthorizationHeader(schema);
                proxyAuthheader.setRealm(realm);
                proxyAuthheader.setNonce(nonce);
                proxyAuthheader.setAlgorithm("MD5");
                proxyAuthheader.setUsername(USER_AUTH);
                proxyAuthheader.setURI(uriReq);
                DigestClientAuthenticationMethod digest=new DigestClientAuthenticationMethod();

                CSeqHeader cseqHeader=(CSeqHeader)response.getHeader(CSeqHeader.NAME);
                //if (cseqHeader.getMethod().equals(Request.INVITE)){
                    String callId = ((CallIdHeader) response.getHeader(CallIdHeader.NAME)).getCallId();
                    requestauth = this.createInvite(callId);

                    digest.initialize(realm,USER_AUTH,uriReq.toString(),nonce,PASS_AUTH,((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod(),null,"MD5");
                    System.out.println("Proxy Response antes de modificarlo : " + proxyAuthheader.getResponse());
                    String respuestaM = digest.generateResponse();
                    proxyAuthheader.setResponse(respuestaM);

                    requestauth.addHeader(proxyAuthheader);
                //}
            }catch (ParseException pa){
                System.out.println("processResponseAuthorization() ParseException:");
                System.out.println(pa.getMessage());
                pa.printStackTrace();
            }catch (Exception ex){
                System.out.println("processResponseAuthorization() Exception:");
                System.out.println(ex.getMessage());
                ex.printStackTrace();
            }
            return requestauth;
        }

        public Request createInvite(String callId)
            throws ParseException, InvalidArgumentException{
                String fromName = "BigGuy";
		String fromSipAddress = "here.com";
		String fromDisplayName = "The Master Blaster";

		String toSipAddress = "there.com";
		String toUser = "LittleGuy";
		String toDisplayName = "The Little Blister";

		// create >From Header
		SipURI fromAddress = addressFactory.createSipURI(fromName,fromSipAddress);

		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName(fromDisplayName);
		FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "12345");

		// create To Header
		SipURI toAddress = addressFactory.createSipURI(toUser, toSipAddress);
		Address toNameAddress = addressFactory.createAddress(toAddress);
		toNameAddress.setDisplayName(toDisplayName);
		ToHeader toHeader = headerFactory.createToHeader(toNameAddress,null);

		// create Request URI
		SipURI requestURI = addressFactory.createSipURI(toUser,peerHostPort);

		// Create ViaHeaders
		ArrayList viaHeaders = new ArrayList();
		ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1", sipProvider.getListeningPoint(transport).getPort(),transport, null);
		// add via headers
		viaHeaders.add(viaHeader);

		// Create ContentTypeHeader
		ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");

		// Create a new CallId header
                CallIdHeader callIdHeader;
                callIdHeader = sipProvider.getNewCallId();
                if (callId.trim().length()>0)
                    callIdHeader.setCallId(callId);

		// Create a new Cseq header
		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(invco,Request.INVITE);

		// Create a new MaxForwardsHeader
		MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

		// Create the request.
		Request request = messageFactory.createRequest(requestURI,
				Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
				toHeader, viaHeaders, maxForwards);
		// Create contact headers
		String host = "127.0.0.1";

		SipURI contactUrl = addressFactory.createSipURI(fromName, host);
		contactUrl.setPort(udpListeningPoint.getPort());

		// Create the contact name address.
		SipURI contactURI = addressFactory.createSipURI(fromName, host);
		contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());

		Address contactAddress = addressFactory.createAddress(contactURI);

		// Add the contact address.
		contactAddress.setDisplayName(fromName);

		contactHeader = headerFactory.createContactHeader(contactAddress);
		request.addHeader(contactHeader);

		String sdpData = "v=0\r\n"
				+ "o=4855 13760799956958020 13760799956958020"
				+ " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
				+ "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
				+ "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
				+ "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
				+ "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
		byte[] contents = sdpData.getBytes();

		request.setContent(contents, contentTypeHeader);

		Header callInfoHeader = headerFactory.createHeader("Call-Info","<http://www.antd.nist.gov>");
		request.addHeader(callInfoHeader);
		
		return request;
        }

	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");

                Properties properties = new Properties();
		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/" + transport);
		properties.setProperty("javax.sip.STACK_NAME", "shootistAuth");
		properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "1048576");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG","shootistAuthdebug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG","shootistAuthlog.txt");
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "16");
		// Drop the client connection after we are done with the transaction.
		properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS","false");

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("createSipStack " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find gov.nist.jain.protocol.ip.sip.SipStackImpl in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(0);
		}
		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			udpListeningPoint = sipStack.createListeningPoint("127.0.0.1",5060, "udp");
			sipProvider = sipStack.createSipProvider(udpListeningPoint);
			ShootistAuth listener = this;
			sipProvider.addSipListener(listener);

		} catch (PeerUnavailableException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Creating Listener Points");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
                        			
		try {
			System.out.println("ShootistAuth Process ");
			Request request = this.createInvite("");
			// Create the client transaction.
			inviteTid = sipProvider.getNewClientTransaction(request);
			// send the request out.
			inviteTid.sendRequest();
			System.out.println("INVITE with no Authorization sent:\n" + request);
			dialog = inviteTid.getDialog();

		} catch (Exception e) {
			System.out.println("Creating call CreateInvite()");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new ShootistAuth().init();

	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException happened for " + exceptionEvent.getHost() + " port = " 
					+ exceptionEvent.getPort());

	}

	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		System.out.println("Transaction terminated event recieved");
	}

	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("dialogTerminatedEvent");

	}
}
