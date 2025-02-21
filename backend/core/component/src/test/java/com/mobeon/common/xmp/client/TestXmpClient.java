/*
 * TestClient.java
 *
 * Created on den 12 december 2005, 10:45
 */

package com.mobeon.common.xmp.client;

import com.mobeon.common.xmp.*;
import com.mobeon.common.util.logging.SimpleLogger;
import com.mobeon.common.util.logging.ILogger;
import com.mobeon.common.commandline.CommandException;
import com.mobeon.common.commandline.CommandLine;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.ServiceInstanceImpl;

import java.io.*;
import java.util.*;

/**
 *
 * @author  MNIFY
 */
public class TestXmpClient implements XmpResultHandler {
    private static final int commandWidth = 35;

    private XmpClient client;

    private BufferedReader in;
    private BufferedReader init;
    private PrintStream out = System.out;
    private CommandLine ui;
    private String initFileName = "testsms.rc";
    public ILogger logger;

    private TreeSet transactions = new TreeSet();
    private LinkedList results = new LinkedList();
    private HashMap servers; // service name, host-port list

    private Properties props = new Properties();

    public int portParameter = 8080;
    public String portHelpText = "The port of the next server to add/remove";
    private Properties attachmentFiles = null; //Filenames and content-types
    private Enumeration attachmentWalker = null;

    public String hostNameParameter;
    public String hostNameHelpText = "The hostname of the next server to add/remove";

    public int sendCountParameter = 1;
    public String sendCountHelpText = "the number of messages to send the next time";

    public String attachmentDirectoryParameter = "none";
    public String attachmentDirectoryHelpText = "directory for attachments to XMP requests, none means no attachment.";

    public String serviceParameter;
    public String serviceHelpText = "the service to add servers to or the service to send requests to";

    public String mcrHostParameter = "mcrhost";
    public String mcrHostHelpText = "hostname of the componentRegister";

    public boolean useComponentRegisterParameter = false;
    public String useComponentRegisterHelpText = "use component register or not";

    public String clientIdParameter = "";
    public String clientIdHelpText = "The clientId to use for the requests";

    /** Creates a new instance of TestClient */
    public TestXmpClient(String fileName)  {
        client = XmpClient.get();
        SimpleLogger sLogger = SimpleLogger.getLogger();
        servers = new HashMap();

        sLogger.setFileName("client.log");
        logger = sLogger;
        client.setLogger(logger);

        in = new BufferedReader(new InputStreamReader(System.in));
        ui = new CommandLine(this, in, out, fileName);
    }

    public void getCommand()  throws CommandException {
        ui.get();
    }

    public void quitCommand() throws CommandException {
        System.exit(0);
    }

    public void setCommand()  throws CommandException {
        ui.set();
    }

    public void helpCommand()  throws CommandException {
        ui.help();
    }

    public void parameterWasSet(String p) throws CommandException {
        ui.message(">>>" + p);
        if ("attachmentDirectory".equals(p)) {
            initAttachments();
        }
    }

    public void addServerCommand() throws CommandException {
        logger.debug("Setting clientId to " + clientIdParameter);
        client.setClientId(clientIdParameter);
        String service = serviceParameter;

        List<IServiceInstance> compList = new ArrayList<IServiceInstance>();
        IServiceInstance inst = new ServiceInstanceImpl(serviceParameter);
        inst.setProperty(IServiceInstance.PORT, Integer.toString(portParameter));
        inst.setProperty(IServiceInstance.HOSTNAME, hostNameParameter);
        compList.add(inst);
        
        servers.put(service, compList);
        client.setComponents(service,compList);
    }

    public void listServerCommand() throws CommandException {
        ArrayList lists = new ArrayList(servers.values());
        Iterator iterator = lists.iterator();
        while( iterator.hasNext() ) {
            ArrayList list = (ArrayList) iterator.next();
            for( int i=0;i<list.size();i++ ) {
            	IServiceInstance tc = (IServiceInstance) list.get(i);
                ui.message(tc.getServiceName() + "\t" + tc.getProperty(IServiceInstance.HOSTNAME) +
                    ":" + tc.getProperty(IServiceInstance.PORT) );
            }
        }
    }

    public String addParameterHelpText = CommandLine.fixfmt("addParameter <name> <value>", commandWidth)
            + " - add parameter with name (first word) and value (rest of line)";
    public void addParameterCommand() throws CommandException {
        props.setProperty(ui.getWord(), ui.getRest());
    }

    public void listParameterCommand() throws CommandException {
        props.list(out);
    }

    public void clearParameterCommand() throws CommandException {
        props.clear();
    }

    public void refreshCommand() throws CommandException {
        client.refreshStatus();
    }

    public void sendCommand() throws CommandException {
        final int count = sendCountParameter;
        final String service = serviceParameter;
        final XmpResultHandler handler = this;
        final Properties fp = props;
        new Thread() {
           public void run() {
               for( int i=0;i<count;i++) {
                   int transId = client.nextTransId();
                   transactions.add(new Integer(transId));
                   String request = XmpProtocol.makeRequest(transId, service, fp);
                   boolean result = true;
                   XmpAttachment[] att = getAttachment();
                   if (null == att) {
                       result = client.sendRequest(transId, request, service, handler);
                   } else {
                       result = client.sendRequest(transId, request, service, handler, att);
                   }
                   if( !result ) {
                       logger.warn("Failed to send request " + transId);
                       transactions.remove(new Integer(transId));
                   }
               }
           }
        }.start();
    }

    public void sendToComponentCommand() throws CommandException {
        final int count = sendCountParameter;
        final String service = serviceParameter;
        final String hostName = hostNameParameter;
        final int port = portParameter;
        final XmpResultHandler handler = this;
        final Properties fp = props;        
        
        final IServiceInstance inst = new ServiceInstanceImpl(service);
        
        if (hostName != null && port > -1) {
            inst.setProperty(IServiceInstance.PORT, Integer.toString(port));
            inst.setProperty(IServiceInstance.HOSTNAME, hostName);
        } else {
            logger.warn("Failed to create component. " +
                "Make sure hostname and port is set.");
            return;
        }
        
        new Thread() {
           public void run() {
               for( int i=0;i<count;i++) {
                   int transId = client.nextTransId();
                   transactions.add(new Integer(transId));
                   String request = XmpProtocol.makeRequest(transId, service, fp);
                   boolean result = true;
                   XmpAttachment[] att = getAttachment();
                   if (null == att) {
                       result = client.sendRequestToComponent(transId, request, service, handler, inst);
                   } else {
                       result = client.sendRequestToComponent(transId, request, service, handler, inst, att);
                   }
                   if( !result ) {
                       logger.warn("Failed to send request " + transId);
                       transactions.remove(new Integer(transId));
                   }
               }
           }
        }.start();
    }
    public void listTransactionsCommand() throws CommandException {
        Iterator iter = transactions.iterator();
        while( iter.hasNext() ) {
            Integer i = (Integer) iter.next();
            ui.message("" + i);
        }

    }

    public void examineCommand() throws CommandException {
        synchronized(results) {
            Iterator iter = results.iterator();
            while(iter.hasNext()) {
                XmpResult res = (XmpResult) iter.next();
                ui.message(res.toString());
            }
            results.clear();
        }
    }

    public void handleResult(XmpResult result) {
        Integer integer = new Integer(result.getTransactionId());
        transactions.remove(integer);
        synchronized(results) {
            results.add(result);
        }
        logger.debug("Got result " + result.toString() );
    }

    private XmpAttachment[] getAttachment() {
        XmpAttachment[] result = null;
        try {
            if (null != attachmentFiles && !attachmentFiles.isEmpty()) {
                ui.message("Getting one attachment of " + attachmentFiles.size());
                result = new XmpAttachment[1];
                if (!attachmentWalker.hasMoreElements()) {
                    attachmentWalker = attachmentFiles.propertyNames();
                }
                String key = (String) attachmentWalker.nextElement();
                try {
                    result[0] = new XmpAttachment(new FileInputStream(attachmentDirectoryParameter
                                                                      + "/" + key),
                                                  attachmentFiles.getProperty(key));
                    ui.message("Got attachment " + key + " of type " + attachmentFiles.getProperty(key)); 
                } catch (IOException e) {
                    ui.message("Could not open attachment " + attachmentDirectoryParameter + "/" + key);
                }
            }
        } catch (Exception e) {
            ui.message("EXC" + e);
        }
        
        return result;
    }

    private void initAttachments() throws CommandException {
        try {
        ui.message("Initializing attachments"); 
        attachmentFiles = null;
        if ("none".equals(attachmentDirectoryParameter)) {
            return;
        }

        File attachmentDir = new File(attachmentDirectoryParameter);
        if (!attachmentDir.isDirectory()) {
            throw new CommandException("Can not find the directory " + attachmentDirectoryParameter);
        }

        Properties typeMap = new Properties();
        try {
            typeMap.load(new BufferedInputStream(new FileInputStream(attachmentDirectoryParameter + "/typemap")));
        } catch (IOException e) {
            ui.message("Could not load " + attachmentDirectoryParameter + "/typemap");
            ;
        }
        File[] files = attachmentDir.listFiles();
        ui.message("files " + files.length);
        attachmentFiles = new Properties();
        for (int i = 0; i < files.length; i++) {
            ui.message("" + i + " " + files[i].getName());
            if (!files[i].isDirectory()) {
                String name = files[i].getName();
                if (!"typemap".equals(name)) {
                    int dotpos = name.lastIndexOf(".");
                    String ext = "";
                    if (dotpos >= 0) {
                        ext = name.substring(dotpos + 1);
                    }
                    attachmentFiles.setProperty(name, typeMap.getProperty(ext, "text/plain"));
                    ui.message("Added attachment " + name + " " + attachmentFiles.getProperty(name)); 
                    ui.message("Setting one attachment of " + attachmentFiles.size());
                }
            }
        }
        attachmentWalker = attachmentFiles.propertyNames();
        }catch (Exception e) {
            ui.message("EXC" + e);
        }
    }
    
    public static void main(String args[]) {
        String fileName = "xmpclient.rc";
        if( args.length > 0 && args[0].length() > 0 ) {
            fileName = args[0];
        }
        TestXmpClient client = new TestXmpClient(fileName);

        while (true) {
            try {
                client.ui.run();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CommandException e) {
                client.ui.message("" + e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
