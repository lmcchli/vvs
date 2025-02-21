/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

import com.mobeon.common.commandline.CommandException;
import com.mobeon.common.commandline.CommandLine;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.util.IndentPrinter;


/**
 * EventSender is used to send ESS events to test ESS event consumers.
 */
public class EventSender {
    
    private static final int commandWidth = 35;
    
    private BufferedReader in;
    private BufferedReader init;
    private PrintStream out = System.out;
    private CommandLine ui;
    private String initFileName;
    
    public EventSender(String initFileName) {
        this.initFileName = initFileName;
        in = new BufferedReader(new InputStreamReader(System.in));
        ui = new CommandLine(this, in, out, initFileName);
    }
    
    public static void main(String[] argv) {
        String fn = "eventsender.rc";
        
        if (argv.length > 0) {
            fn = argv[0];
        }
        EventSender t = new EventSender(fn);
        
        t.out.println("ESS EventSender test program");
        t.out.println("============================\n");
        
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
    
    public String mailhostHelpText = "name of the mailhost (partition for this event)";
    public String mailhostParameter;
    
    public String esshostHelpText = "name of the event broker host";
    public String esshostParameter;
    
    public String interfacetypeHelpText = "the type of interface a session was established through";
    public String interfacetypeParameter = "tui";
    public String[] interfacetypeParameterValues = { "tui", "vui" };
    
    public String sessionidHelpText = "the session id";
    public String sessionidParameter = "1";

    public String rateHelpText = "positive value: number of events per second, negative value: number of seconds per event";
    public int rateParameter = 5;
    public int[] rateParameterValues = { -1000, 1000 };
 
    public String countHelpText = "the number of events to send";
    public int countParameter = 1;
    public int[] countParameterValues = { 0, 100000 };
 
    
    public void helpCommand() {
        ui.help();
    }
    
    public String setHelpText = CommandLine.fixfmt("set <paramname> <value>",
                                                   commandWidth)
        + " - set a parameter to a new value";
    public void setCommand()  throws CommandException {
        ui.set();
    }
    
    public void parameterWasSet(String p) {
        ;
    }
    
    public String getHelpText = CommandLine.fixfmt("get [paramname]", commandWidth)
        + " - show all parameters (truncates long values) or one selected parameter";
    public void getCommand()  throws CommandException {
        ui.get();
    }
    
    public String sleepHelpText = CommandLine.fixfmt("sleep <seconds>", commandWidth)
        + " - sleep for a while";
    public void sleepCommand() throws CommandException {
        try {
            Thread.sleep(1000 * Integer.parseInt(ui.getRest().trim()));
        } catch (NumberFormatException ignore) { ; 
        } catch (InterruptedException ignore) { ; }
    }
    
    public String loginHelpText = CommandLine.fixfmt("login <phone>[..<phone>]", commandWidth)
        + " - sends one or more login (event.interface.session.create) events";
    public void loginCommand() throws CommandException {
        sendEvent("event.interface.session.create", ui.getRest());
    }
    
    public String logoutHelpText = CommandLine.fixfmt("logout <phone>[..<phone>]", commandWidth)
        + " - sends one or more logout (event.interface.session.delete) events";
    public void logoutCommand() throws CommandException {
        sendEvent("event.interface.session.delete", ui.getRest());
    }
    
    public String statsHelpText = CommandLine.fixfmt("stats", commandWidth)
        + " - dumps the Active MQ connection stats";
    public void statsCommand() throws CommandException {
        if (connection == null) { connectEss(); }
        ActiveMQConnection c = (ActiveMQConnection) connection;
        c.getConnectionStats().dump(new IndentPrinter());
    }
    
    public String quitHelpText = CommandLine.fixfmt("quit", commandWidth)
        + " - Exits the program";
    public void quitCommand() throws CommandException {
        try {
            connection.close();
        } catch (Throwable ignore) {}
        System.exit(0);
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
    
    private Destination destination;
    private int messageCount = 1;
    private int startNumber = 33222087;
    private boolean verbose = true;
    private int messageSize = 1024;
    private long timeToLive;
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private String subject = "event";
    private boolean login = true;
    private boolean help = false;
    private Session session;
    private MessageProducer producer;
    private Connection connection = null;
    
    private void connectEss() {
        try {
            url = "tcp://" + esshostParameter + ":61616";
            System.out.println("Connecting to URL: " + url);
            // Create the connection.
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password,
                                                                                        url);
            connection = connectionFactory.createConnection();
            connection.start();
            
            // Create the session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createTopic(subject);
            
            // Create the producer.
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            if (timeToLive != 0)
                producer.setTimeToLive(timeToLive);
            
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        } 
    }
    
    protected void sendEvent(String eventType, String phone) throws CommandException {
        if (connection == null) { connectEss(); }
        Range r = new Range(phone);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < countParameter; i++) {
            try {
                MapMessage message = session.createMapMessage();
                
                message.setStringProperty("msgtype", eventType);
                message.setStringProperty("partition", "ms:" + mailhostParameter);
                message.setStringProperty("objectid", "telephonenumber:" + r.next());
                producer.send(message);
                long sleepTime;
                if (rateParameter > 0) {
                    sleepTime = startTime + 1000L * i / rateParameter - System.currentTimeMillis();
                } else {
                    sleepTime = startTime - 1000L * i * rateParameter - System.currentTimeMillis();
                }
                if (sleepTime > 0) {
                    try { Thread.sleep(sleepTime); } catch (InterruptedException ignore) { ; }
                }
            } catch (Exception e) {
                throw new CommandException("" + e);
            }
        }
    }

    private class Range {
        private int first;
        private int last;
        private int cur;

        public Range(String r) {
            first = 1;
            last = 9;
            try {
                int dots = r.indexOf("..");
                if (dots < 0) {
                    first = Integer.parseInt(r);
                    last = first;
                } else {
                    first = Integer.parseInt(r.substring(0, dots).trim());
                    last = Integer.parseInt(r.substring(dots + 2, r.length()).trim());
                }
            } catch (Exception ignore) { ; }
            cur = first;
        }

        public int next() {
            if (cur > last) {
                cur = first;
            }
            return cur++;
        }
    }
}    
