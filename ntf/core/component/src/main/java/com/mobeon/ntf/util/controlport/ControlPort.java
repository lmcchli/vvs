/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.controlport;

import com.mobeon.ntf.util.Logger;

import java.io.*;
import java.net.*;


/**********************************************************************************
 * This class let a user connect via for instance telnet and set / view certain
 parameters and configuration in NTF. Only a single user at a time is allowed to
 connect.
 <B>Start the control port</B>
 The control port start by that the user defines a file given by
 ctrlFilename. The content of the file must be a valid port.

 <B>Stop the control port</B>
 The control port is stopped by either removing the enable file and to access
 the control port or by send the comand disable and the terminate the session by
 the command exit.
 
 <B>Commands supported by the control port</B>
 exit - terminate the session
 disable - remove the enable file
 If the help request is in the classpath, you can type help to get current
 supported commands (that has registered).

 <B>How to register a new request type</B>
 The request must inherit from ActionRequest and be registered in the RequestRegister.

 When the user access the port from outside and gives a request, the control port ask the request
 register for a registered class. The registered class must inherit from
 ActionRequest. The method setParameter is called after the request has been instansiated.
 
 Currently all instansiated control ports have the same enable file. Pretty ugly
 solution but ...

*/


public class ControlPort extends Thread {
    
    private final static Logger log = Logger.getLogger(ControlPort.class);
    /** The name of the enable file*/
    public static String ctrlFilename = "/var/tmp/ntf_ctrl_port";

    protected static final int NO_CONTROL_FILE = -1;
    protected static final int INVALID_CONTROL_PORT = -2;


    protected static final int SLEEP_TIME = 600000;

    protected int port;
    
    protected ServerSocket sSock;
    protected boolean isConnected = false;





    /**********************************************************************************
     * Create a control port using default file as startup file (see ctrlFilename)
    */
    public ControlPort() {
	this(null);
    }


    /**********************************************************************************
     * Create a control port with given start file.
     @param enablingFile the name of the file that shall contain a given port number
    */
    public ControlPort(String enablingFile) {
	if(enablingFile != null)
	    ctrlFilename = enablingFile;
	port = -1;
	setName("ControlPort");
	start();
    }


    public void run() {

        // System.out.println("Connected");
        while (true) {
            try {
                // System.err.println("Inside while true");
                // Check for a valid port number
                boolean hasPortNumber = false;
                while (!hasPortNumber) {
                    int status = getPortFromFile();
                    // System.err.println("Got status " + status);
                    switch (status) {
                        case NO_CONTROL_FILE:
                            // The file did not exist
                            // System.err.println("No file");
                            if (isConnected()) {
                                // System.err.println("Dis");
                                log.logMessage("Control port disconnecting", Logger.L_ERROR);
                                disconnect();
                            }
                            nap();
                            break;
                        case INVALID_CONTROL_PORT:
                            // The file existed but did not contain a valid port number
                            // System.err.println("Found invalid port number");
                            if (isConnected()) {
                                log.logMessage("Control port did not get valid port. Disconnecting.", Logger.L_ERROR);
                                disconnect();
                            }
                            nap();
                            break;
                        default:
                            // System.err.println("Found a port number");
                            int old_port = port;
                            if (status != old_port) {
                                disconnect();
                            }
                            port = status;
                            // System.err.println("Found valid port on " + port);
                            hasPortNumber = true;
                            break;
                    }
                }
                if (!isConnected()) {
                    // System.err.println("Need to connect");
                    if (!connect(port)) {
                        log.logMessage("Control port could not connect to " + port, Logger.L_ERROR);
                        continue;
                    }
                    log.logMessage("Control port start on " + port, Logger.L_VERBOSE);
                }
                try {
                    // System.err.println("Wait for connection");
                    new SessionHandler(sSock.accept());
                } catch (IOException e) {
                    log.logMessage("Control port disconnecting due to following error " + e.getMessage(), Logger.L_ERROR);
                    disconnect();
                } catch (SecurityException se) {
                    log.logMessage("Control port could not attach to port " + port + " due to following error " + se.getMessage(),
                            Logger.L_ERROR);
                    disconnect();
                }
            } catch (Exception ee) {
                System.err.println("Control port error. Message: " + ee);
            }
        }

    }


    /**********************************************************************************
     * Parse the file ctrl_port_enable file for a port number
     @return NO_CONTROL_FILE if the file could not be found,
     INVALID_CONTROL_PORT if the content of the file was not a valid port number
     within 1 an 65535 otherwise the port number is returned.
    */
    protected int getPortFromFile() {

        File ctrlFile = new File(ctrlFilename);
        if (!ctrlFile.exists()) {
            return NO_CONTROL_FILE;
        }
        
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(ctrlFile));
            String line = in.readLine();
            if (line == null)
                return INVALID_CONTROL_PORT;

            int prt = Integer.parseInt(line);
            if (prt <= 0 || prt >= 65536)
                return INVALID_CONTROL_PORT;
            return prt;

        } catch (Exception e) {
            return INVALID_CONTROL_PORT;
        } finally {
            if(in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    System.err.println("Cannot close file: " + e);
                }
        }

    }



    /**********************************************************************************
     * Take a little nap ...
     */
    protected void nap() {
	try {
	    Thread.sleep(SLEEP_TIME);
	}catch(Exception e){}
    }


    /**********************************************************************************
     * Check if the control port os connected or not
     @return true if the control port is conected, false otherwise
    */
    public boolean isConnected() {
	return isConnected;
    }

    /**********************************************************************************
     * Get the port allocated for the control port
     @return the port number that has been allocated or -1 if the port is
     inactive
    */
    public int getAllocatedPort() {
	return port;
    }


    /**********************************************************************************
     * Connects to a specified port.
     @return true if the connect was successful, false otherwise
    */
    protected boolean connect(int port) {
	try {
	    sSock = new ServerSocket(port);
	    isConnected = true;
	}catch(Exception e) {
	    System.err.println("Could not start controlport on port " + port + "Message: "+e);
	    isConnected = false;
	}
	
	return isConnected;
    }


    /**********************************************************************************
     * Disconnect the control port
     */
    protected void disconnect() {
        // System.err.println("Disconnect");
        try {
            if (isConnected())
                sSock.close();
        } catch (Exception e) {
        }
        isConnected = false;
        port = -1;
    }

    /**********************************************************************************
     * Commands that the control port supports. These commands will override
     other commands with same name that is registered.
    */
    public static String getUsage() {
        return "exit - exit the control port\ndisable - disable the control port after current session";
    }



    public static void main(String args[]) {
        try {
            ControlPort p = new ControlPort();
            p.join();
        } catch (Exception eee) {
            System.err.println("Main " + eee);
        }
    }
}


/**********************************************************************************
 * If you would like to have concurrent requests to the control port, let this
 inherit from Thread and start it. If you would like to reuse session handlers
 you must rewrite the ControlPort a bit too.
*/
class SessionHandler {
    
    protected InputStream in;
    protected OutputStream out;
    protected Socket sock;
    
    protected static int TIMEOUT = 600000;
    
    public SessionHandler(Socket sock) throws IOException {
	this.sock = sock;
	this.sock.setSoTimeout(TIMEOUT);
	in = sock.getInputStream();
	out = sock.getOutputStream();
	
	goHandler();
    }


    /**********************************************************************************
     * Handle a session with a single user
     */
    protected void goHandler() {
	try {
	    sendWelcomePrompt();
	    
	    while(true) {
		
		//System.out.println("Read request");
		String req = readRequest();
		
		if(req == null) {
		    //System.out.println("The request was null");
		    continue;
		}
		//System.out.println("Collect parameters for " + req);
		String s[] = separateActionAndParameters(req);
		
		if(checkForExit(s[0])) {
		    close(ControlMessage.getClosePrompt());
		    return;
		}
		else if(checkForDisable(s[0])) {
		    removeEnableFile();
		    write("Control port will be disabled after current session\r\n");
		    continue;
		}
		
		ActionRequest r;
		try {
		    //System.out.println("Load request for " + s[0]);
		    r = loadRequest(s[0], s[1]);
		}catch(ActionException e) {
		    write(ControlMessage.getBadResponsePrompt(e.toString()));
		    continue;
		}catch(BadParametersException bpe) {
		    if(bpe.getMessage() == null)
			write(ControlMessage.getBadResponsePrompt("Expected parameters to " + s[0]));
		    else
			write(ControlMessage.getBadResponsePrompt(bpe.getMessage()));
		    continue;
		}

		if(r == null) {
		    sendUnsupportedRequest(s);
		    continue;
		}
		else {
		    try {
			r.send();
		    
			boolean status = r.status();
			Object message = r.getResponse();
			if(status) {
			    if(message == null)
				write(ControlMessage.getOKResponsePrompt());
			    else
				write(message+"\n"+ControlMessage.getOKResponsePrompt());
			}
			else {
			    if(message == null)
				write(ControlMessage.getBadResponsePrompt());
			    
			    else
				write(message+"\n"+ControlMessage.getBadResponsePrompt()); 
			}
		    }catch(Exception e) {
			write(ControlMessage.getBadResponsePrompt(e.toString()));
		    }
		}
		
	    }
	}catch(java.io.InterruptedIOException timeout) {
	    // Probabel cause is timeout.
	    try {
		close(ControlMessage.getAutoLogoutPrompt(TIMEOUT/1000));
	    }catch(Exception ee) {}
	}catch(Exception e) {
	    // For the time being, this is put to STDOUT but a Logger shall be
	    // used instead.
	    System.out.println("Session got exception " + e);
	    
	}
    }
    

    /**********************************************************************************
     * Remove the file that has enabled the control port.
     @return true if the file was removed, false otherwise
    */
    protected boolean removeEnableFile() {
	try {
	    File f = new File(ControlPort.ctrlFilename);
	    if(f.exists()) {
		return f.delete();
	    }
	}catch(Exception e){}
	return false;
    }



    /**********************************************************************************
     * Load the specific action. The name of the request is <action>Request
     @param action is the request that is loaded
     @param parameters is parameters that is sent to the request instance
     @throws ActionException if the class is no ActionRequest or if the action
     is not found in classpath
    */
    protected ActionRequest loadRequest(String action, String parameters) throws ActionException, BadParametersException {
        try {
            if (action == null || action.length() == 0) {
                // System.out.println("No action defined");
                return null;
            }

            // String requestName =
            // "utilities.ControlPort."+action.substring(0,1).toUpperCase()+action.substring(1).toLowerCase()+"Request";

            String className = RequestRegister.getClassForRequest(action);
            if (className == null)
                throw new ActionException(ActionException.NO_ACTION_REQUEST_CLASS, action + " is not registered.");

            // System.out.println("Load class " + className);
            Object o = java.beans.Beans.instantiate(null, className);
            ActionRequest a = null;
            if (o instanceof ActionRequest) {
                a = (ActionRequest) o;
            } else
                throw new ActionException(ActionException.NO_ACTION_REQUEST_CLASS, action);
           
            a.setParameters(parameters);
            return a;
        } catch (ClassNotFoundException e) {
            throw new ActionException(ActionException.CLASS_NOT_DEFINED, action);
        } catch (IOException ioe) {
            throw new ActionException(ActionException.LOAD_ERROR, action);
        }

    }


    
    protected void sendUnsupportedRequest(String s[]) throws IOException {
        write(ControlMessage.getBadResponsePrompt("Does not support action " + s[0]));
    }
    


    protected boolean checkForExit(String action) {
	if(action.toLowerCase().equals("exit"))
	    return true;
	return false;
    }


    protected boolean checkForDisable(String action) {
	if(action == null)
	    return false;
	if(action.toLowerCase().equals("disable"))
	    return true;
	return false;
    }



    /**********************************************************************************
     * In a string like <action< <parameter 1> <parameter 2> ... this separate
     the string into two separate parts.
     @param req the string to separate
     @return a length 2 string with the action in the 0 element and the
     parameters in the 1 element.
     */
    protected String[] separateActionAndParameters(String req) {
	int indexOfSpace = req.indexOf(" ");
	//System.out.println("Request #"+req+"#");
	String action = req;
	String parameters = null;

	if(indexOfSpace > 0) {
	    if(req.length() >= indexOfSpace)
		parameters = req.substring(indexOfSpace+1);
	    action = req.substring(0, indexOfSpace);
	}
	String ret[] = new String[2];
	ret[0] = action;
	ret[1] = parameters;
	return ret;
	    
    }
    
    
    protected void sendWelcomePrompt() throws java.io.IOException {
	write(ControlMessage.getWelcomePrompt());
    }
    
    protected void sendErrorMessage(String message) throws java.io.IOException {
	write("Error occurred: " + message);
    }
    
    protected void close(String message) {
	try {
	    write(message);
	}catch(Exception e) {}
	closeConnection();
    }

    
    
    protected void closeConnection() {
	try {
	    sock.shutdownInput();
	}catch(Exception e) {}
	try {
	    sock.shutdownOutput();
	}catch(Exception e) {}
	try {
	    in.close();
	}catch(Exception e) {}
	try {
	    out.close();
	}catch(Exception e) {}
	try {
	    sock.close();
	}catch(Exception e) {}
    }
    
    protected void write(String message) throws java.io.IOException {
	out.write(message.getBytes());
	out.flush();
    }
    
    protected String readRequest() throws java.io.IOException {
	byte buf[] = new byte[500];
	
	StringBuffer strBuf = new StringBuffer();
	
	while(true) {
	    int letterCount = in.read(buf);
	    
	    if(letterCount > 0) {
		strBuf.append(new String(buf, 0, letterCount));
		int indexOfLineEnd = strBuf.toString().indexOf("\r\n");
		if(indexOfLineEnd >= 0) {
		    String ret = strBuf.toString().substring(0, indexOfLineEnd);
		    if(ret == null || ret.length() == 0)
			return null;
		    return ret;
		}
	    }
	}
    }
    
    
}


class ControlMessage {
    private static final String WELCOME_PROMPT = "Hello. The control port is under construction and shall be used with precaution.";
    
    private static final String BAD_RESPONSE = "Not completed.";
    
    private static final String OK_RESPONSE = "OK Completed.";
    
    public static String getWelcomePrompt() {
	return WELCOME_PROMPT + "\r\n";
    }

    public static String getBadResponsePrompt() {
	return BAD_RESPONSE + "\r\n";
    }
    
    public static String getBadResponsePrompt(String message) {
	return BAD_RESPONSE + " Message: " + message + "\r\n";
    }

    public static String getOKResponsePrompt() {
	return OK_RESPONSE + "\r\n";
    }

    public static String getClosePrompt() {
	return "Goodbye.\r\n";
    }

    public static String getAutoLogoutPrompt(int timeout) {
	return "Timeout " + timeout + " seconds. Goodbye.\r\n";
    }
}



/**********************************************************************************
 * This class is to indicate that severe errors has occurred when instansiating
 / using action requests
*/
class ActionException extends Exception {

    public static final int NO_ACTION_REQUEST_CLASS = 1;
    public static final int CLASS_NOT_DEFINED = 2;
    public static final int LOAD_ERROR = 3;

    protected int thisExcType;

    public ActionException(int i, String m) {
	super(m);
	thisExcType = i;
    }

    public String getMessage() {
	switch(thisExcType) {
	case NO_ACTION_REQUEST_CLASS:
	    return "Request is no ActionRequest. " + super.getMessage();
	case CLASS_NOT_DEFINED:
	    return "Request class is not found in classpath. " + super.getMessage();
	case LOAD_ERROR:
	    return "Load class error. "+super.getMessage();
	default:
	    return "No detailed information. " + super.getMessage();
	}
    }
}


/**********************************************************************************
 * A class to indicate that parameters that is passed to the request is not what
 is expected
*/
class BadParametersException extends Exception {
    public BadParametersException() {
	super();
    }
    
    public BadParametersException(String s) {
	super(s);
    }

}
