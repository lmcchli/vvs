package com.mobeon.smsc.containers;


import java.util.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.util.logging.*;
import java.util.Vector;
import java.util.Date;
import java.util.Enumeration;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.smsc.config.Config;
import com.mobeon.smsc.util.SMSCFormatter;
import com.mobeon.smsc.util.SMSCFilter;
import com.mobeon.smsc.containers.Account;
import com.mobeon.smsc.interfaces.SmppConstants;
import com.mobeon.smsc.smpp.util.SMSPdu;
import com.mobeon.smsc.management.response.ResponseCodeHandler;


/*
 * Container for ESME general information such as loglevel,
 * ESME name, total number of received SMS requests
 * and a cache of ESME accounts.
 **/
public class ESME implements SmppConstants {

    /* The name of this ESME account**/
    private String esme_name = null;

    /* Loglevel for this ESME account**/
    private String loglevel = null;

    /* Number of received SMS requests. numberOfSMS will be reseted when >= logsize**/
    private int numberOfSMS = 0;

    /* Max number of SMS requests to cache.**/
    private int logsize = 0;

    /* Logger**/
    private Logger log = null;

    /* Cache of SMSPdu**/
    private LinkedList cacheOfSMS = null;

    /* Cache of accounts connected to this ESME**/
    private Hashtable accounts = new Hashtable();

    /* Date object, init when a new ESME is created**/
    private Date start = null;

    /** Filewriter object to log all sms requests fo file.*/
    private FileWriter fw = null;
    private ResponseCodeHandler responseHandler = null;

    private int receivedMwiOn = 0;
    private int receivedMwiOff = 0;
    private int receivedSmsNull = 0;
    private int receivedCancel = 0;
    private int receivedSms = 0;
    private int total = 0;
    private Rate rate;

    /**
     * Inits a new ESME with accounts, if there exist any.
     * This constructor is called from the LabHandler.
     * @param esme_name is the name of this ESME.
     * @param loglevel for the logger object.
     * @param logsize is the max number of SMSPdu to cache.
     * @param clients a node list of accounts.
     */
    public ESME(String esme_name,
            String loglevel,
            int logsize,
            Node clients) {
        init(esme_name, loglevel, logsize);
        initAccounts(clients);
    }

    /**
     * Inits a new ESME with accounts, if there exist any.
     * This constructor is called from the ESMEHandler
     * when a new ESME should created.
     * @param esme_name is the name of this ESME.
     * @param loglevel for the logger object.
     * @param logsize is the max number of SMSPdu to cache.
     * @param account_name the name of the account that should be created.
     * @param password is the account password for the account that should be created.
     * @param connections max number of bindings allowed to the account.
     */
    public ESME(String esme_name,
            String loglevel,
            int logsize,
            String account_name,
            String password,
            String connections) {
        init(esme_name, loglevel, logsize);
        addAccount(account_name, password, connections);
        rate = new Rate();
    }

    /**
     * Init code common to the constructors.
     * @param esme_name is the name of this ESME.
     * @param loglevel for the logger object.
     * @param logsize is the max number of SMSPdu to cache.
     */
    private void init(String esme_name,
            String loglevel,
            int logsize) {
        this.esme_name = esme_name;
        this.loglevel = loglevel;
        this.logsize = logsize;
        setLogger();
        cacheOfSMS = new LinkedList();
        responseHandler = new ResponseCodeHandler();
    }

    private String logDirectory(String esme) {
        File logdir = new File("../logs/" + esme);

        if (!logdir.isDirectory()) {
            if (logdir.exists()) {
                logdir = new File(logdir.getPath() + System.currentTimeMillis());
            }
            logdir.mkdir();
        }
        return logdir.getPath() + "/";
    }

    /*
     *Inits the logger object with loglevel
     *A new log file will be created in the logs directory.
     *The logfile name will be set to the value of the esme_name variabel
     **/
    private void setLogger() {
        String[] errors = Config.getErrors();
        String error = null;
        Handler h;

        Logger l = Logger.getLogger(esme_name);

        try {
            fw = new FileWriter(logDirectory(esme_name) + "sms.log", true);
            h = new FileHandler(logDirectory(esme_name) + "bind.%g.log",
                    Config.getLogSize(), Config.getLogCount(), true);
        } catch (IOException e) {
            h = new ConsoleHandler();
            error = "Failed to open log file " + e + "\n";
        }
        h.setFormatter(new SMSCFormatter());
        h.setLevel(Level.ALL);
        h.setFilter(new SMSCFilter(esme_name));

        l.addHandler(h);
        l.setLevel(Config.getLogLevel(getLogLevel()));

        log = Logger.getLogger(esme_name);

        for (int i = 0; i < errors.length; i++) {
            log.severe(errors[i]);
        }
        if (error != null) {
            log.severe(error);
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine(
                    "================ " + esme_name
                    + " started ================================");
        }

    }

    /*
     *Parses the node and creates a new account for every account node contained
     *within the clients node
     **/
    private void initAccounts(Node clients) {
        try {
            NodeList nl = ((Element) clients).getElementsByTagName("account");

            if (nl != null && nl.getLength() != 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    String uid = n.getAttributes().getNamedItem("uid").getNodeValue();
                    String pwd = getElementValue(n, "password");
                    int con = 10;

                    try {
                        con = Integer.parseInt(getElementValue(n, "connections"));
                    } catch (NumberFormatException e) {}
                    if (log.isLoggable(Level.FINE)) {
                        log.fine(
                                "Adding new account for " + esme_name
                                + " { Account: " + uid + " pwd: " + pwd
                                + " Max connections: " + con + " Caching "
                                + logsize + " SMS requests}");
                    }
                    accounts.put(uid, new Account(uid, pwd, con));
                }
            }
        } catch (Exception e) {
            log.severe("Could not parse node. " + e);
        }
    }

    /**
     * Adds a new account to this ESME.
     * @param account_name the name of the account that should be created.
     * @param password is the account password for the account that should be created.
     * @param connections max number of bindings allowed to the account.
     */
    public void addAccount(String account_name,
            String password,
            String connections) {
        if (!accounts.containsKey(account_name)) {
            if (log.isLoggable(Level.FINE)) {
                log.fine(
                        "Adding new account for " + esme_name + " { Account: "
                        + account_name + " pwd: " + password
                        + " Max connections: " + connections + " Caching "
                        + logsize + " SMS requests}");
            }
            accounts.put(account_name,
                    new Account(account_name, password,
                    Integer.parseInt(connections)));
        }
    }

    /**
     *Removes a account from the account cashe
     *@param account_name the account that should be removed
     */
    public void removeAccount(String account_name) {
        if (accounts.containsKey(account_name)) {
            accounts.remove(account_name);
        }
    }

    /**
     * Returns the time the ESME was created
     * If start object should be null a new Date is created and the start time is returned
     * @return the time this ESME was iniated
     */
    public long getStartTime() {
        if (start == null) {
            start = new Date();
        }
        return start.getTime();
    }

    /**
     * Resets the SMSPdu cashe.
     */
    public void restetSMSList() {
        synchronized (cacheOfSMS) {
            start = null;
            cacheOfSMS = new LinkedList();
            numberOfSMS = 0;
            receivedMwiOff = 0;
            receivedMwiOn = 0;
            receivedSms = 0;
            receivedSmsNull = 0;
            Enumeration accountId = accounts.keys();

            for (int i = 0; i < accounts.size(); i++) {
                ((Account) accounts.get((String) accountId.nextElement())).resetSMSPduRequests();
            }
        }
    }

    /**
     * Sets the number of received SMS PDU requests for a specific account to 0.
     *@param accountName the account to reset.
     */
    public void restetAccountSMSList(String accountName) {
        if (accounts.containsKey(accountName)) {
            ((Account) accounts.get(accountName)).resetSMSPduRequests();
        }
    }

    /*
     *Returns the loglevel for this logger
     * @return the loglevel of this logger
     **/
    public String getLogLevel() {
        return loglevel;
    }

    /*
     *Sets the loglevel for this log
     * @param loglevel sets the loglevel for this log
     **/
    public void setLogLevel(String loglevel) {
        this.loglevel = loglevel;
        log.setLevel(Config.getLogLevel(loglevel));
    }

    /*
     * @return the loglevel of this logger
     **/
    public int getLogSize() {
        return logsize;
    }

    /**
     * @param size sets the size of the SMSPdu cashe
     */
    public void setLogSize(int size) {
        this.logsize = size;
    }

    /**
     * Adds a new SMSPdu the SMSPdu cache.
     * If numberOfSMS == logsize the numberOfSMS will be set to 0
     * numberOfSMS is increased.
     * @param sms the SMSPdu to add to the cache
     */
    public synchronized void addSMS(String accountName, SMSPdu sms) {
        if (start == null) {
            start = new Date();
        }
        total++;
        try {

            if (sms.isMwiOn()) {
                receivedMwiOn++;
            } else if (sms.isMwiOff()) {
                receivedMwiOff++;
            } else if (sms.isSmsNull()) {
                receivedSmsNull++;
            } else if (sms.isSms()) {
                receivedSms++;
            } else if (sms.isCancel()) {
		receivedCancel++;
	    }

            cacheOfSMS.addLast(sms);
            fw.write(sms.toString());
            fw.write("\n====================================================\n");
            fw.flush();
            while (cacheOfSMS.size() > getLogSize()) {
                cacheOfSMS.removeFirst();
            }
            if (accounts.containsKey(accountName)) {
                ((Account) accounts.get(accountName)).increaseSMSPduRequests();
            }
        } catch (Exception e) {
            cacheOfSMS = new LinkedList();
            cacheOfSMS.addLast(sms);
        }
    }

    /**
     * Gets the SMSPdu cache as a String[] array
     * where each entry is in a plain text format.
     * @return the SMS PDU cache as a string array.
     */
    public String[] getCachedSMSAsPlainText() {
        int s = cacheOfSMS.size();
        String[] list = new String[s];
        ListIterator li = cacheOfSMS.listIterator(0);
        int i = 0;

        while (li.hasNext()) {
            Object o = li.next();

            if (o != null) {
                list[i++] = o.toString();
            }
        }
        // new SMSPduCache().start();
        return list;
    }

    /**
     * Gets the 10 latest entries in the SMSPdu cache
     * as a String[] array where each entry is in HTML format.
     *@param esmeName the name of the ESME.
     * @return the SMS PDU cache as a string array.
     */
    public String[] getCachedSMSAsHTML() {
        Object[] arr;

        synchronized (this) {
            arr = cacheOfSMS.toArray();
        }
        if (arr.length == 0) {
            return null;
        }

        String[] list = new String[arr.length];

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                list[i] = ((SMSPdu) arr[i]).toHttpString();
            }
        }
        return list;
    }

    /**
     * Gets the latest entries in the SMSPdu cache
     * as a String[] array where each entry is in HTML format.
     *@param esmeName the name of the ESME.
     * @return the SMS PDU cache as a string array.
     */
    public String getCacheSummaryHTML() {
        if (cacheOfSMS.isEmpty()) {
            return "";
        }
        
        String result = SMSPdu.getHttpSummaryHeader();
        ListIterator li = cacheOfSMS.listIterator(cacheOfSMS.size());

        while (li.hasPrevious()) {
            Object o = li.previous();

            if (o != null) {
                result += ((SMSPdu) o).getHttpSummary(esme_name);
            }
        }
        return result;
    }

    /**
     *Gets the number of receiced MWI Off PDU requests
     **/
    public int getReceivedMwiOff() {
        return receivedMwiOff;
    }

    /**
     *Gets the number of receiced MWI PDU requests
     **/
    public int getReceivedMwiOn() {
        return receivedMwiOn;
    }

    /**
     *Gets the number of receiced Null SMS PDU requests
     **/
    public int getReceivedSmsNull() {
        return receivedSmsNull;
    }

    /**
     *Gets the number of receiced cancel SMS PDU requests
     **/
    public int getReceivedCancel() {
        return receivedCancel;
    }

    /**
     *Gets the number of receiced SMS PDU requests
     **/
    public int getReceivedSMS() {
        return receivedSms;
    }

    /**
     * Increases the number of bindings connected to a account
     * within this ESME. If the account has no free connections
     * the SMPP response code SMPPSTATUS_RTHROTTLED is returned.
     * If the account does not exist the SMPP status code SMPPSTATUS_RINVSYSID
     * is returned.
     *@param accountUid the account name
     *@return a SMPP status code
     */
    public int getConnection(String accountUid, String password) {
        Account account = null;
        int command_status = SMPPSTATUS_RINVSYSID;

        if (accounts.containsKey(accountUid)) {
            account = (Account) accounts.get(accountUid);
            if (!account.getPassword().equals(password)) {
                command_status = SMPPSTATUS_RINVPASWD; 
            } else {
                if (account.hasFreeConnections()) {
                    command_status = responseHandler.getBindResponseCode();
                    if (command_status == SMPPSTATUS_ROK) {
                        account.increaseConnection();
                    }
                } else {
                    command_status = SMPPSTATUS_RTHROTTLED;
                }
            }
        }
        return command_status;
    }

    /**
     *Gets a status code to include in the SMS PDU response from a SMS PDU request.
     *@param accountUid is the name of the actual account that sent the SMS PDU request.
     *@return a statuscode on the SMS PDU request
     */
    public int getSubmitPDUResponseCode() {
        return responseHandler.getSubmitResponseCode();
    }

    /**
     *Gets a status code to include in the SMS PDU response from a SMS PDU request.
     *@param accountUid is the name of the actual account that sent the SMS PDU request.
     *@return a statuscode on the SMS PDU request
     */
    public int getEnquireLinkResponseCode() {
        return responseHandler.getEnquireLinkResponseCode();
    }

    /**
     *Gets a status code to include in the SMS PDU response from a SMS PDU request.
     *@param accountUid is the name of the actual account that sent the SMS PDU request.
     *@return a statuscode on the SMS PDU request
     */
    public int getCancelResponseCode() {
        return responseHandler.getCancelResponseCode();
    }

    /*
     *Returns the number of used connections for a specified account.
     *If the account does not exist -1 is returned.
     *@param accountUid the name of the account
     *@return the number of used connection for the accountUid
     **/
    public int getUsedConnection(String accountUid) {
        int used = -1;

        if (accounts.containsKey(accountUid)) {
            used = ((Account) accounts.get(accountUid)).getUsedConnections();
        }
        return used;
    }

    /**
     *Returns the max number of bindings allowed to a specific account.
     *@param accountUid the account name
     *@return max number of bindings to a account
     */
    public int getMaxConnections(String accountUid) {
        int max = -1;

        if (accounts.containsKey(accountUid)) {
            max = ((Account) accounts.get(accountUid)).getConnections();
        }
        return max;
    }

    /**
     *Stes the max number of bindings allowed for a specified account.
     *@param accountUid the account name
     */
    public void setMaxConnections(String accountUid, int size) {
        if (accounts.containsKey(accountUid)) {
            ((Account) accounts.get(accountUid)).setConnections(size);
        }
    }

    /*
     * Decreases the number of bindings for a specific account by one.
     *@param accountUid the account wich should have its connections decreased
     **/
    public void freeConnection(String accountUid) {
        Account account = null;

        if (accounts.containsKey(accountUid)) {
            ((Account) accounts.get(accountUid)).decreaseConnection();
        }
    }

    /*
     *Rteurns the name of this ESME.
     * @return the name of the ESME
     **/
    public String getName() {
        return esme_name;
    }

    /*
     *Returns the password for an account.
     * @return the password for the requested account.
     **/
    public String getAccountPassword(String accountUid) {
        String pwd = "";

        if (accounts.containsKey(accountUid)) {
            pwd = ((Account) accounts.get(accountUid)).getPassword();
        }
        return pwd;
    }

    /*
     *Sets the password for an account.
     **/
    public void setAccountPassword(String accountUid, String newPwd) {
        if (accounts.containsKey(accountUid)) {
            ((Account) accounts.get(accountUid)).setPassword(newPwd);
        }
    }

    /*
     *Sets the password for an account.
     **/
    public void setAccountUid(String accountUid, String newAccountUid) {
        getAccountNames();
        if (accounts.containsKey(accountUid)) {
            Account account = (Account) accounts.get(accountUid);

            account.setName(newAccountUid);
            accounts.remove(accountUid);
            accounts.put(newAccountUid, account);
        }
    }

    public int getRate() {
        return rate == null ? 0 : rate.getRate();
    }

    /**
     * Returns the the total number of received SMS requests for this ESME.
     *@return the total number of received SMS requests
     */
    public int getReceivedSMSRequests() {
        return total;
    }

    /**
     *Gets the number of receiced SMS PDU requests for a specific account
     *@param accountName the account to get information from.
     **/
    public int getReceivedSMSRequests(String accountName) {
        int num = 0;

        if (accounts.containsKey(accountName)) {
            num = ((Account) accounts.get(accountName)).getSMSPduRequests();
        }
        return num;
    }

    /**
     * Returns the the total number of accounts for this ESME.
     *@return the the total number of accounts for this ESME
     */
    public int getNumberOfAccounts() {
        return accounts.size();
    }

    public void setResultCode(int bc, boolean bonce, int sc, boolean sonce, int ec, boolean eonce, int cc, boolean conce) {
        if (responseHandler != null) {
            responseHandler.setResultCode(bc, bonce, sc, sonce, ec, eonce, cc, conce);
        }
    }

    /**
     * Returns all account names registred for this ESME.
     *@return a String array with all registred accounts for this ESME.
     */
    public String[] getAccountNames() {
        Enumeration names = accounts.keys();
        String[] nameId = new String[accounts.size()];

        for (int i = 0; i < accounts.size(); i++) {
            nameId[i] = (String) names.nextElement();
        }
        return nameId;

    }

    private String getElementValue(Node root, String element) {
        String value = null;

        try {
            NodeList nl = ((Element) root).getElementsByTagName(element);

            if (nl != null && nl.getLength() != 0) {
                value = nl.item(0).getFirstChild().getNodeValue();
            }
            return value;
        } catch (Exception e) {
            System.out.println(e);
            return value;
        }
    }

    private class SMSPduCache extends Thread {
        
        SMSPduCache() {}
        
        public void run() {
            try {
                for (int i = 0; i < cacheOfSMS.size(); i++) {// if(cacheOfSMS[i] != null){
                    // fw.write(cacheOfSMS[i].toString());
                    // fw.write("\n====================================================\n");
                    // }
                }
                fw.flush();
            } catch (Exception e) {
                System.err.println(
                        "Unexpected exception: " + NtfUtil.stackTrace(e));
                try {
                    if (fw != null) {
                        fw.close();
                    }
                    fw = new FileWriter(logDirectory(esme_name) + "sms.log");
                } catch (IOException ioe) {
                    System.err.println("Could not close file: " + ioe);
                }
                System.err.println("Could not write to file: " + e);
            }
        }
    }
    

    public class Rate extends Thread {
        private int last = 0;
        private long lastTime = 0;
        private int r = 0;
        
        public Rate() {
            start();
        }

        public int getRate() {
            return r;
        }

        public void run() {
            while (true) {
                try {
                    try {
                        sleep(60);
                    } catch (InterruptedException e) {
                        ;
                    }
                    long now = System.currentTimeMillis();
                    int t = total;

                    r = (int) (1000L * (t - last) / (now - lastTime));
                    last = t;
                    lastTime = now;
                } catch (Exception e) {
                    System.err.println("Unexpected exception " + e);
                }
            }
        }
    }
}
