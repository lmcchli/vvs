/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

import com.mobeon.common.commandline.CommandException;
import com.mobeon.common.commandline.CommandLine;
import com.mobeon.common.smscom.charset.Converter;
import com.mobeon.common.smscom.PhoneOnEvent;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSCom;
import com.mobeon.common.smscom.SMSComException;
import com.mobeon.common.smscom.SMSMessage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * SmsSender is used to test the connection to and the capabilities of the SMSC
 * for a newly installed NTF.
 *
 *<H2>Test cases</H2>
 *<UL>
 *<LI>Send a message.
 *<LI>Voice MWI on.
 *<LI>Voice MWI off.
 *<LI>Fax MWI on.
 *<LI>Fax MWI off.
 *<LI>Email MWI on.
 *<LI>Email MWI off.
 *<LI>Other MWI on.
 *<LI>Other MWI off.
 *<LI>Login.
 *<LI>Max concurrent logins.
 *<LI>Replace in SMSC.
 *<LI>Replace in phone.
 *<LI>SMS type 0 delivery receipt.
 *<LI>Character sets.
 *<LI>
 *<LI>
 *</UL>
 */

public class SmsSender
    implements com.mobeon.common.smscom.PhoneOnEventListener,
            com.mobeon.common.smscom.Logger,
            com.mobeon.common.smscom.CommSpy,
            com.mobeon.common.smscom.ConnectionStateListener {
    
    private static final int commandWidth = 35;

    private BufferedReader in;
    private BufferedReader init;
    private PrintStream out = System.out;
    private CommandLine ui;
    private String initFileName = "testsms.rc";

    private SMSCom conn = null;
    private Converter conv = null;
    private int sendCount = 0;

    public SmsSender(String initFileName) {
        this.initFileName = initFileName;
        in = new BufferedReader(new InputStreamReader(System.in));
        ui = new CommandLine(this, in, out, initFileName);
    }

    public static void main(String[] argv) {
        String fn = "testsms.rc";

        if (argv.length > 0) {
            fn = argv[0];
        }
        SmsSender t = new SmsSender(fn);

        t.out.println("Mobeon SMSC test program R10C");
        t.out.println("=============================\n");

        while (true) {
            try {
                t.ui.run();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CommandException e) {
                t.out.println("ERROR: " + e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
    }

    public String validityHelpText = "number of hours the SMS is valid";
    public int validityParameter;
    public int[] validityParameterValues = { 1, 100};

    public String systemidHelpText = "the system id (login) of the SMSC account";
    public String systemidParameter;

    public String passwordHelpText = "the password of the SMSC account";
    public String passwordParameter;

    public String systemtypeHelpText = "the system type of the SMSC account";
    public String systemtypeParameter;

    public String servicetypeHelpText = "the service type for coming messages";
    public String servicetypeParameter;

    public String tonHelpText = "destination type of number";
    public int tonParameter;
    public int[] tonParameterValues = { 1, 255};

    public String npiHelpText = "destination numbering plan indicator"; //
    public int npiParameter;
    public int[] npiParameterValues = { 1, 255};

    public String phoneHelpText = "destination telephone number";
    public String phoneParameter;

    public String srctonHelpText = "source type of number";
    public int srctonParameter;
    public int[] srctonParameterValues = { 1, 255};

    public String srcnpiHelpText = "source numbering plan indicator";
    public int srcnpiParameter;
    public int[] srcnpiParameterValues = { 1, 255};

    public String srcphoneHelpText = "source telephone number";
    public String srcphoneParameter;

    public String hostHelpText = "host name of the SMSC";    
    public String hostParameter;

    public String portHelpText = "port number on the SMSC";
    public int portParameter;
    public int[] portParameterValues = { 0, 65535};

    public String replypathHelpText = "use the reply path feature of the SMSC";
    public boolean replypathParameter;

    public String logHelpText = "show log messages";    
    public boolean logParameter;

    public String spyHelpText = "show protocol packets";
    public boolean spyParameter;

    public String replaceHelpText = "type of replace for the next SMS";
    public String replaceParameter;
    public String[] replaceParameterValues = { "smsc", "phone", "both" };

    public String replacegroupHelpText = "which replace group to use for the next SMS";
    public int replacegroupParameter;
    public int[] replacegroupParameterValues = { 1, 7};

    public String protocolHelpText = "which protocol to talk to the SMSC with";
    public String protocolParameter;
    public String[] protocolParameterValues = { "smpp", "cimd2" };

    public String flashHelpText = "send SMS as flash";
    public boolean flashParameter;

    public String mwiHelpText = "MWI command for the next SMS";
    public String mwiParameter;
    public String[] mwiParameterValues = { "none", "on", "off" };

    public String iconHelpText = "MWI icon for the next SMS";
    public String iconParameter;
    public String[] iconParameterValues = { "voice", "fax", "email", "other" };
    
    public void helpCommand() {
        ui.help();
    }

    public String setHelpText = CommandLine.fixfmt("set <paramname> <value>",
            commandWidth)
            + " - ";
    public void setCommand()  throws CommandException {
        ui.set();
    }
    
    public void parameterWasSet(String p) {
        if ("systemid".equals(p) || "password".equals(p)
                || "systemtype".equals(p) || "host".equals(p)
                || "port".equals(p)) {
            forgetSmsCom();
        }
        
        if ("log".equals(p) && conn != null) {
            conn.setLogger(logParameter ? this : null);
        }
        
        if ("spy".equals(p) && conn != null) {
            conn.setSpy(spyParameter ? this : null);
        }
    }
    
    public String getHelpText = CommandLine.fixfmt("get", commandWidth)
            + " - show all parameters";
    public void getCommand()  throws CommandException {
        ui.get();
    }
    
    public String pollHelpText = CommandLine.fixfmt("poll", commandWidth)
            + " - polls the SMSC connection";
    public void pollCommand() throws CommandException {
        try {
            getSmsCom();
            conn.poll();
        } catch (SMSComException e) {
            throw new CommandException(e.getMessage());
        }
    }
    
    public String sendHelpText = CommandLine.fixfmt("send <message>",
            commandWidth)
            + " - sends a message";
    public void sendCommand() throws CommandException {
        try {
            String msg = ui.getRest();

            getSmsCom();
            SMSMessage m = conv.unicodeToMessage(msg, 140);

            m.setExpiryTimeRelative(1);
            m.setFragmentSize(140);
            if ("both".equals(replaceParameter)) {
                m.setPID(64 + replacegroupParameter);
                m.setReplace(true);
                m.setServiceType("VM" + replacegroupParameter);
            } else if ("smsc".equals(replaceParameter)) {
                m.setReplace(true);
                m.setServiceType("VM" + replacegroupParameter);
            } else if ("phone".equals(replaceParameter)) {
                m.setPID(64 + replacegroupParameter);
            }

            if (flashParameter) {
                m.setDCS(0x10);
            }
            if (!"none".equals(mwiParameter)) {
                int dcs = 0xC0;

                if (msg.length() > 0) {
                    dcs |= 0x10;
                }
                if ("on".equals(mwiParameter)) {
                    dcs |= 0x08;
                }
                if ("fax".equals(iconParameter)) {
                    dcs |= 1;
                } else if ("email".equals(iconParameter)) {
                    dcs |= 2;
                } else if ("other".equals(iconParameter)) {
                    dcs |= 3;
                }
                m.setDCS(dcs);
            }
	    //System.out.println("Sending message " + m);
            conn.sendMessage(
                    new SMSAddress(tonParameter, npiParameter, phoneParameter),
                    new SMSAddress(srctonParameter, srcnpiParameter,
                    srcphoneParameter),
                    m);
            if (++sendCount % 100 == 0) {
                System.out.println("Sent total " + sendCount);
            }
        } catch (SMSComException e) {
            throw new CommandException(e.getMessage());
        }
        
        mwiParameter = "none";
    }
    
    public String cancelHelpText = CommandLine.fixfmt("cancel",
            commandWidth)
            + " - cancel messages with the same service type and to and from address";
    public void cancelCommand() throws CommandException {
        try {
            getSmsCom();
	    conn.sendCancel(
			    new SMSAddress(tonParameter, npiParameter, phoneParameter),
			    new SMSAddress(srctonParameter, srcnpiParameter,
					   srcphoneParameter),
			    servicetypeParameter);
        } catch (SMSComException e) {
            throw new CommandException(e.getMessage());
        }
    }
    
    public String phoneonHelpText = CommandLine.fixfmt("phoneon", commandWidth)
            + " - waits for the phone to be turned on";
    public void phoneonCommand() throws CommandException {
        try {
            getSmsCom();
            conn.setReceiptReceiver(this);
            // conn.requestPhoneOnEvent(new SMSMessage(new byte[0], 0),
            // new SMSAddress(ton, npi, phoneNumber),
            // new SMSAddress(srcTon, srcNpi, sourcePhone),
            // 1,
            // replypath);
        } catch (SMSComException e) {
            throw new CommandException(e.getMessage());
        }
    }
    
    public String closeHelpText = CommandLine.fixfmt("close", commandWidth)
            + " - unbinds from the SMSC";
    public void closeCommand() throws CommandException {
        try {
            getSmsCom();
            conn.close();
        } catch (SMSComException e) {
            throw new CommandException(e.getMessage());
        }
    }
    
    public String quitHelpText = CommandLine.fixfmt("quit", commandWidth)
            + " - Exits the program";
    public void quitCommand() throws CommandException {
        forgetSmsCom();
        System.exit(0);
    }
    
    /**
     * Get a protocol handler for the SMSC protocol.
     */
    private void getSmsCom() throws SMSComException, CommandException {
        Properties props = new Properties();

        if (conn == null) {
            conn = SMSCom.get(protocolParameter, this);
        }
        conn.setHostName(hostParameter);
        conn.setPortNumber(portParameter);
        conn.setUserName(systemidParameter);
        conn.setPassword(passwordParameter);
        conn.setSystemType(systemtypeParameter);
        conn.setTimeout(30 * 1000);
        if (conn != null) {
            conn.setLogger(logParameter ? this : null);
        }
        if (conn != null) {
            conn.setSpy(spyParameter ? this : null);
        }
        
        File charFile = new File(new File("."),
                "charconv." + protocolParameter.toLowerCase());

        if (!charFile.canRead()) {
            charFile = new File(new File("."), "charconv.cfg");
        }
        if (!charFile.canRead()) {
            throw new CommandException(
                    "Could not find character conversion file");
        } else {
            try {
                props.load(
                        new BufferedInputStream(new FileInputStream(charFile)));
            } catch (IOException e) {
                throw new CommandException(e.getMessage());
            }
        }
        conv = Converter.get(props);
    }

    private void forgetSmsCom() {
        if (conn != null) {
            conn.close();
        }
        conn = null;
    }        

    /**
     * Handles a phone on event.
     *@param e - the event object.
     */
    public void phoneOn(PhoneOnEvent e) {
        System.out.println(
                "Phone on event for " + e.getAddress()
                + (e.isOk() ? " OK: " : " failed: ") + e.getMessage());
    }

    public void logString(String msg, int l) {
        System.out.println(
                "LOG " + l + " [" + Thread.currentThread().getName() + "] "
                + msg);
    }

    public boolean ifLog(int level) {
        return true;
    }

    public void toSMSC(byte[] msg) {
        System.out.println("-->" + hexDump(msg));
    }
    
    public void fromSMSC(byte[] msg) {
        System.out.println("<--" + hexDump(msg));
    }

    public void connectionUp(String name) {
        System.out.println("The connection is up: " + name);
    }
    
    public void connectionDown(String name) {
        System.out.println("The connection is down: " + name);
        conn = null;
    }

    public void connectionReset(String name) {
        System.out.println("The connection is reset: " + name);
        conn = null;
    }

    /**
     * Generates a printable string with a "pipe-hex" dump of a byte array.
     *@param ba byte array to dump.
     *@return String with the pipe-hex dump.
     */
    public static String hexDump(byte[] ba) {
        char b;
        int lineByte = 0;
        StringBuffer sb = new StringBuffer();
        StringBuffer printable = new StringBuffer();

        for (int i = 0; i < ba.length; i++) {
            if (lineByte == 0) {
                if (i < 0x1000) {
                    sb.append("0");
                }
                if (i < 0x100) {
                    sb.append("0");
                }
                if (i < 0x10) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(i)).append(" ");
            }
            b = (char) (ba[i] & 0xFF);
            if (Character.isLetterOrDigit(b)) {
                printable.append(b);
            } else {
                printable.append(".");
            }
            if (b < 0x10) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(b));
            ++lineByte;
            if (lineByte == 16) {
                lineByte = 0;
                sb.append("    ").append(printable).append("\n");
                printable = new StringBuffer();
            } else {
                sb.append("|");
            }
        }
        if (lineByte != 0) {
            sb.append("    ").append(printable).append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }
}    
