/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.xmp.server;

import com.mobeon.common.util.M3Utils;
import com.mobeon.common.xmp.XmpAttachment;

import java.io.IOException;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.SocketException;
import java.util.*;
import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.Header;
import jakarta.mail.MessagingException;


/**
 * Handles one connection and HTTP traffic on it.
 */
public class HttpHandler
		extends
		Thread
		implements
		HttpGetHandler,
		HttpResponder {
	/**
	 * HTTP headers fo use in responses
	 */
	private static final String HTTP_HEADER = "HTTP/1.1 200 OK\r\ncontent-type: text/html\r\n";
	/**
	 * Start of HTML pages
	 */
	private static final String HTML_START =
			"<HTML><HEAD><TITLE>M3 XmpServer</TITLE></HEAD><BODY><H1>M3 XmpServer</H1>";
	/**
	 * End of HTML pages
	 */
	private static final String HTML_END = "</BODY></HTML>";

	private static HttpGetHandler getHandler = null;

	/**
	 * Socket for HTTP communication
	 */
	private Socket socket = null;
	/**
	 * Writer of HTTP messages
	 */
	private DataOutputStream output = null;
	/**
	 * XMP requests handler
	 */
	private XmpHandler xmphandler = null;

	private HttpServer server = null;
	/**
	 * session identifier
	 */
	private static int id = 0;
	private boolean keepalive = true;

	// 5 min connection timeout
	private int connectionTimeout = 300000;
	private long connectionTime;
  
        // Never wait for content-lenght byte more than 2 seconds
        private int connectionWait = 2000;
 
	/**
	 * Constructor.
	 *
	 * @param s Socket for the HTTP connection, with timeout already.
	 */
	public HttpHandler(Socket s, HttpServer server) {
		super("HttpHandler-" + ++id);
		socket = s;
		try {
			socket.setSoTimeout(connectionTimeout);
		} catch (java.net.SocketException e) {
			// Ignore
		}
		this.server = server;
		connectionTime = System.currentTimeMillis();
		xmphandler = new XmpHandler(this, id);
		xmphandler.start();
		if (id >= 99999) id = 0;
	}

	/**
	 * Run method that reads HTTP requests and passes XMP requests on to the XMP
	 * handler and forwards <I>get</I> and <I>head</I> requests to special
	 * handler functions in this class.
	 */
	public void run() {

		server.debug("Handling new HTTP connection.");
		InputStream in;
		try {
			output = new DataOutputStream(socket.getOutputStream());
			in = new BufferedInputStream(socket.getInputStream()); // Wrapped for efficiency
		} catch (Exception e) {
			server.error("Failed to create inputstream from socket, " + e.toString());
			return;
		}


		while (isConnected()) {
			try {
				InternetHeaders respHeaders = new InternetHeaders(in);
				boolean multipart = false;
				String get = null;
				String head = null;

				int headerCount = 0;
				Enumeration headerNames = respHeaders.getAllHeaders();
				while (headerNames.hasMoreElements()) {

					headerCount++;
					Header header = (Header)headerNames.nextElement();
					if (headerCount == 1) {
						if (header.getValue().startsWith("GET")) {
							get = header.getValue().substring(3).trim();
						} else if (header.getValue().startsWith("HEAD")) {
							head = header.getValue().substring(4).trim();
						} else if (!header.getValue().startsWith("POST")) {
							server.error("Invalid request: " + header.getValue());
							disconnect();
							return;
						}
					}
					if (header.getValue().toLowerCase().indexOf("multipart/related") != -1) {
						multipart = true;
					}
					if (header.getName().toLowerCase().equals("connection")) {
						if (header.getValue().toLowerCase().indexOf("close") != -1) {
							keepalive = false;
						}
					}
					if (keepalive) {
						connectionTime = System.currentTimeMillis();
					}
				}
				if (headerCount == 0) {
					disconnect();
				} else if (get != null) {
					if (getHandler == null) {
						handleGet(get, this);
					} else {
						getHandler.handleGet(get, this);
					}
				} else if (head != null) {
					handleHead(head);
				} else if (multipart) {
					readMultipart(respHeaders, in);
				} else {
					readSingle(respHeaders, in);
				}

			} catch (MessagingException e) {
				Exception ex = e.getNextException();
				if (ex != null) {
					if (ex instanceof SocketTimeoutException ||
							ex instanceof SocketException) {
						server.info("Connection timed out.");
						disconnect();
					} else {
						server.error("Connection problem: ", e);
						disconnect();
					}
				} else {
					server.error("Connection problem: ", e);
					disconnect();
				}
			} catch (IOException e) {
				server.error("Connection problem " + e);
				disconnect();
			} catch (Exception e) {
				server.error("Unknown exception: " + M3Utils.stackTrace(e).toString());
				disconnect();
			}
			if (!keepalive) {
				server.info("Finished reading request.");
				break;
			}
		}
		//disconnect();
		//server.debug("Client disconnected");
	}

	/**
	 * reads a single xmp request.
	 *
	 * @param headers the headers for the request.
	 * @param in	  the inputstream from the socket
	 */
	private void readSingle(InternetHeaders headers, InputStream in) throws Exception {
		int contentLen = -1;
		String[] contentLength = headers.getHeader("Content-Length");
		if (contentLength != null) {
			try {
				contentLen = Integer.parseInt(contentLength[0]);
			}
			catch (NumberFormatException ignore) {
				// Ignore
			}
		}
		if (contentLen > 0) {
			//InputStreamReader reader = new InputStreamReader(in);
			byte[] data = new byte[contentLen];
			int bytesRead = 0;
			int position = 0;
			long startTime = System.currentTimeMillis() + connectionWait;
			long endTime = System.currentTimeMillis();
			while (bytesRead < data.length && endTime <= startTime) {
				int currentBytes;
				currentBytes = in.read(data, position, data.length - bytesRead); // Will block 5 min...
				position += currentBytes;
				bytesRead += currentBytes;
				endTime = System.currentTimeMillis();
			}
                        // In case we don't get everything in 2 secs, then? No use passing half-baked
                        // XMP docs to xmphandler, right?
                        if (bytesRead < contentLen) {
                          server.error("XMP-request incomplete, discarding " + bytesRead 
                                       + " bytes (Content-length is " + contentLen + " bytes).");
                        } else {
                          xmphandler.xmpRequestHandler(new String(data).toCharArray(), null);
                        }
		}
	}

	/**
	 * Reads a multipart request. The Xmp-request and all attachments are
	 * parsed from the inputstream
	 *
	 * @param headers headers for the surrounding request.
	 * @param in	  inputstream from the socket
	 */
	private void readMultipart(InternetHeaders headers, InputStream in) throws Exception {
		String boundary = null;
		boolean end = false;
		int contentCount = 0;
		char [] xmpData = new char[0];
		ArrayList<XmpAttachment> attachments = new ArrayList<XmpAttachment>();
		String[] contentType = headers.getHeader("Content-Type");
		if (contentType != null) {
			String line = contentType[0];
			int bix = line.indexOf("boundary=");
			if (bix != -1) {
				boundary = line.substring(bix + 9);
			}
		}
		server.debug("Reading multipart with boundary " + boundary);
		if (boundary != null) {
			// time limited later
			long startTime = System.currentTimeMillis() + connectionWait;
			long endTime = System.currentTimeMillis();
			while (!end && endTime <= startTime) {
				try {
					InternetHeaders ih = new InternetHeaders(in);
					Enumeration headerStrings = ih.getAllHeaderLines();

					while (headerStrings.hasMoreElements()) {
						String line = (String)headerStrings.nextElement();
						if (line.indexOf(boundary + "--") != -1) {
							end = true;
							break;
						}
					}
					int contentLen = -1;
					String[] contentLength = ih.getHeader("Content-Length");
					if (contentLength != null) {
						try {
							contentLen = Integer.parseInt(contentLength[0]);
						}
						catch (NumberFormatException ignore) {
							// Ignore
						}
					}
					if (contentLen > 0) {
						String attachmentContentType = "unknown";
						String[] attachmentContentTypeLines = ih.getHeader("Content-Type");
						if (attachmentContentTypeLines != null && attachmentContentTypeLines.length > 0) {
							attachmentContentType = attachmentContentTypeLines[0];
						}
						byte[] data = new byte[contentLen];
						int bytesRead = 0;
						int position = 0;
						while (bytesRead < data.length) {
							int currentBytes = in.read(data, position, data.length - bytesRead);
							position += currentBytes;
							bytesRead += currentBytes;
						}
						if (contentCount == 0) {
							xmpData = new String(data).toCharArray();
						} else {
							XmpAttachment att = new XmpAttachment(data, attachmentContentType);
							attachments.add(att);

						}
						contentCount++;
					}
				} catch (MessagingException me) {
					if (me.toString().indexOf("SocketTimeoutException") == -1) {
						throw me;
					}
				} catch (SocketTimeoutException ste) {
					// do nothing read again.
				} catch (Exception e) {
					throw e;
				}
				endTime = System.currentTimeMillis();
			}
		}
		xmphandler.xmpRequestHandler(xmpData, attachments);

	}


	/**
	 * Handles HTTP <I>head</I> requests.
	 *
	 * @param req - the unprocessed part of the request string.
	 */
	private void handleHead(String req) {
		server.debug("handling HEAD request");

		try {
			httpResponseHandler(HTTP_HEADER);
		} catch (IOException e) {
			//ignore
		}
	}


	/**
	 * Set custom HTTP GET handler.
	 */
	public static void setHttpGetHandler(HttpGetHandler getHandler) {
		HttpHandler.getHandler = getHandler;
	}

	/**
	 * Handle  HTTP get request.
	 *
	 * @param req	   String with the request information.
	 * @param responder the object that forwards the respose to the client.
	 */
	public void handleGet(String req, HttpResponder responder) {

		server.debug("handling GET request");

		if (req.toLowerCase().startsWith("/info")) {
			responder.respond(HTTP_HEADER, HTML_START + ComponentInfo.htmlReport() + HTML_END);
		} else {
			responder.respond(HTTP_HEADER, HTML_START + "<H3>XmpServer</H3>" + HTML_END);
		}
	}


	/**
	 * Responds to HTTP requests by adding the content-length to the body and
	 * returning it to the client.
	 *
	 * @param headers - the headers of the response, so far.
	 * @param body	- the body of the response
	 */
	public void respond(String headers, String body) {
		try {
			httpResponseHandler(headers + "content-length: " + body.length() + "\r\n\r\n" + body);
		} catch (IOException e) {
			//ignore
		}
	}


	/**
	 * Returns an HTTP response to the client
	 *
	 * @param httpResponse the response.
	 */
	/*package*/
	synchronized void httpResponseHandler(String httpResponse) throws IOException {
		if (isConnected()) {
			server.debug("Sending HTTP response:\n" + httpResponse);

			try {
				output.writeBytes(httpResponse);
				output.flush();

				if (!keepalive) {
					server.debug("Disconnecting.");
					disconnect();
				}
			} catch (IOException ioe) {
				server.error("Failed to send response, " + ioe.toString());
				disconnect();
				throw ioe;
			} catch (Exception e) {
				server.error("Unknown exception sending response: " + M3Utils.stackTrace(e).toString());
				disconnect();
				throw new IOException("Socket error.");
			}
		} else {
			server.debug("Not sending HTTP response (closed):\n" + httpResponse);
			throw new IOException("Socket error.");
		}
	}

	/**
	 * Returns an HTTP response to the client with attachments
	 *
	 * @param xmpResponse the xmp response.
	 * @param attachments list of XmpAttachments
	 */
	/*package*/
	synchronized void httpResponseHandler(String xmpResponse, ArrayList attachments)
			throws IOException {

		if (isConnected()) {
			try {

				StringBuffer buffer = new StringBuffer();
				buffer.append("--MIME_boundery\r\n"
						+ "Content-Type: text/xml; charset=utf-8\r\n"
						+ "Content-Length: ").append(xmpResponse.length()).append("\r\n\r\n");
				buffer.append(xmpResponse);
				int attachmentSize = 0;
				StringBuffer [] attachmentsBuffer = new StringBuffer[attachments.size()];
				for (int i = 0; i < attachments.size(); i++) {
					attachmentsBuffer[i] = new StringBuffer();
					XmpAttachment attachment = (XmpAttachment)attachments.get(i);
					if (attachment.getSize() > -1) {
						attachmentsBuffer[i].append("\r\n\r\n--MIME_boundery\r\n"
								+ "Content-Type: ").append(attachment.getContentType()).append(" ; charset=utf-8\r\n"
								+ "Content-Length: ").append(attachment.getSize()).append("\r\n\r\n");
						attachmentSize += attachment.getSize() + attachmentsBuffer[i].length();
					}
				}

				String endLine = "\r\n\r\n--MIME_boundery--\r\n\r\n";
				server.debug("sending multi: " + xmpResponse);
				output.writeBytes("HTTP/1.1 200 OK\r\n"
						+ "Content-Type: Multipart/Related; boundary=MIME_boundery\r\n"
						+ "Content-Length: " + (buffer.length() + attachmentSize + endLine.length()) + "\r\n\r\n");

				output.writeBytes(buffer.toString());

				for (int i = 0; i < attachments.size(); i++) {
					XmpAttachment attachment = (XmpAttachment)attachments.get(i);
					InputStream reader = attachment.getInputStream();
					if (reader != null) {
						byte [] dataArray = new byte[4096];
						int bytesRead;
						output.writeBytes(attachmentsBuffer[i].toString());
						while ((bytesRead = reader.read(dataArray)) > 0) {
							output.write(dataArray, 0, bytesRead);
						}
					}
				}
				output.writeBytes(endLine);
				output.flush();

				if (!keepalive) {
					server.debug("Disconnecting.");
					disconnect();
				}

			} catch (IOException ioe) {
				server.error("Failed to send response, " + ioe.toString());
				disconnect();
				throw ioe;
			} catch (Exception e) {
				server.error("Unknown exception sending response: " + M3Utils.stackTrace(e).toString());
				disconnect();
				throw new IOException("Socket error.");
			}
		} else {
			server.debug("Not sending multipart XMP response (closed):\n" + xmpResponse);
			throw new IOException("Socket error.");
		}
	}

	/**
	 * Check if session is alive
	 *
	 * @return true if still connected, false otherwise.
	 */
	public synchronized boolean isConnected() {
		if (keepalive) {
			long now = System.currentTimeMillis();
			if (connectionTime + connectionTimeout > now) {
				return socket != null;
			} else {
				server.debug("Connection timed out.");
				disconnect();
				return false;
			}
		}
		return socket != null;
	}

	/*package*/ HttpServer getServer() {
		return server;
	}

	/**
	 * Method that shutdown connections.
	 */
	private void disconnect() {
		try {
			if (socket != null) socket.close();
		} catch (Exception e) {
			//ignore
		}
		output = null;
		socket = null;
	}

}
