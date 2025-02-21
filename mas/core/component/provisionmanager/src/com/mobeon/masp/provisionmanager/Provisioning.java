/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.provisionmanager;

import com.mobeon.common.configuration.ConfigurationChanged;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.provisionmanager.cai.*;
import com.mobeon.masp.util.executor.ExecutorServiceManager;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Implements the IProvisioning interface. Used to create and delete subscribers in a userdirectory.
 * Has a map of CAIConnectionPool objects keyed by adminUid.
 * <p/>
 * Initialized by the Spring framework at MAS start-up.
 * Contains references to the IConfiguration and ILocateService interfaces.
 *
 * @author ermmaha
 */
public class Provisioning implements IProvisioning, IEventReceiver {
    private static ILogger log = ILoggerFactory.getILogger(Provisioning.class);
    /**
     * For debugging the CAI communication
     */
    private CAIDebug caiDebug = new CAIDebug();
    /**
     * To find the CAI servers
     */
    private ILocateService locateService;
    /**
     * Configuration
     */
    private IConfiguration configuration;
    /**
     * Map of CAIConnectionPool, keyed by adminUid
     */
    private Map<String, CAIConnectionPool> pools = new HashMap<String, CAIConnectionPool>();

    private int lastTransactionId;
    /**
     * Keeps track of the current asynchronous tasks
     */
    private Map<Integer, Future<CommandCaller>> commandTasks =
            Collections.synchronizedMap(new HashMap<Integer, Future<CommandCaller>>());

    /**
     * Initiates the object. Must be called after construction!
     */
    public void init() {
        updateConfiguration();
    }

    public void create(Subscription sub, String adminUid, String adminPwd) throws ProvisioningException {
        if (log.isInfoEnabled()) log.info("create(sub=" + sub + ", adminUid=" + adminUid + ", adminPwd=" + adminPwd + ")");
        CAIConnectionPool pool = getConnectionPool(adminUid, adminPwd);
        CAIConnection conn = null;
        try {
            conn = pool.getConnection();
            doCommand(conn, makeCreateCommand(sub));
        } catch (CAIException e) {
            throw new ProvisioningException(e);
        } catch (IOException e) {
            throw new ProvisioningException(e);
        } finally {
            if (conn != null) pool.returnConnection(conn);
        }
        if (log.isInfoEnabled()) log.info("create(Subscription, String, String) returns void");
    }

    public int createAsync(Subscription sub, String adminUid, String adminPwd) throws ProvisioningException {
        if (log.isInfoEnabled()) log.info("createAsync(sub=" + sub + ", adminUid=" + adminUid + ", adminPwd=" + adminPwd + ")");
        int transactionid = doAsyncCommand(sub, adminUid, adminPwd, CommandMode.CREATE);
        if (log.isInfoEnabled()) log.info("createAsync(Subscription, String, String) returns " + transactionid);
        return transactionid;
    }

    public void create(int transactionid) throws ProvisioningException {
        if (log.isInfoEnabled()) log.info("create(transactionid=" + transactionid + ")");
        waitForAsyncCommand(transactionid);
        if (log.isInfoEnabled()) log.info("create(int) returns void");
    }

    public void delete(Subscription sub, String adminUid, String adminPwd) throws ProvisioningException {
        if (log.isInfoEnabled()) log.info("delete(sub=" + sub + ", adminUid=" + adminUid + ", adminPwd=" + adminPwd + ")");
        CAIConnectionPool pool = getConnectionPool(adminUid, adminPwd);
        CAIConnection conn = null;
        try {
            conn = pool.getConnection();
            doCommand(conn, makeDeleteCommand(sub));
        } catch (CAIException e) {
            throw new ProvisioningException(e);
        } catch (IOException e) {
            throw new ProvisioningException(e);
        } finally {
            if (conn != null) pool.returnConnection(conn);
        }
        if (log.isInfoEnabled()) log.info("delete(Subscription, String, String) returns void");
    }

    public int deleteAsync(Subscription sub, String adminUid, String adminPwd) throws ProvisioningException {
        if (log.isInfoEnabled()) log.info("deleteAsync(sub=" + sub + ", adminUid=" + adminUid + ", adminPwd=" + adminPwd + ")");
        int transactionid = doAsyncCommand(sub, adminUid, adminPwd, CommandMode.DELETE);
        if (log.isInfoEnabled()) log.info("deleteAsync(Subscription, String, String) returns " + transactionid);
        return transactionid;
    }

    public void delete(int transactionid) throws ProvisioningException {
        if (log.isInfoEnabled()) log.info("delete(transactionid=" + transactionid + ")");
        waitForAsyncCommand(transactionid);
        if (log.isInfoEnabled()) log.info("delete(int) returns void");
    }

    public boolean isFinished(int transactionid) {
        if (log.isInfoEnabled()) log.info("isFinished(transactionid=" + transactionid + ")");
        boolean finished = false;
        Future<CommandCaller> task = commandTasks.get(transactionid);
        if (task != null) {
            finished = task.isDone();
        }
        if (log.isInfoEnabled()) log.info("isFinished(int) returns " + finished);
        return finished;
    }

    /**
     * Starts an  asynchronous task by submitting a CommandCaller object with information how to do the command to a
     * ExecutorService. Uses the singleton ExecutorServiceManager to get a ExecutorService which is related to this
     * component.
     *
     * @param sub
     * @param adminUid
     * @param adminPwd
     * @param commandMode
     * @return a new transactionid
     */
    private int doAsyncCommand(Subscription sub, String adminUid, String adminPwd, CommandMode commandMode) {
        int id = getTransactionId();

        ExecutorService executorService = ExecutorServiceManager.getInstance().getExecutorService(Provisioning.class);
        Future<CommandCaller> task = executorService.submit(new CommandCaller(sub, adminUid, adminPwd, commandMode));

        commandTasks.put(id, task);

        return id;
    }

    /**
     * Waits for the asynchronous task to finish (if not finished already).
     *
     * @param transactionid
     * @throws ProvisioningException If some error occurred during the task.
     */
    private void waitForAsyncCommand(int transactionid) throws ProvisioningException {
        Future<CommandCaller> task = commandTasks.get(transactionid);
        if (task == null) {
            throw new ProvisioningException("No asynchronous task defined by " + transactionid + " was found");
        }

        try {
            CommandCaller commandCaller = task.get();
            if (commandCaller.getException() != null) {
                throw commandCaller.getException();
            }
        } catch (InterruptedException e) {
            log.error("Exception in waitForAsyncCommand " + e);
        } catch (ExecutionException e) {
            log.error("Exception in waitForAsyncCommand " + e);
        } finally {
            commandTasks.remove(transactionid);
        }
    }

    private void doCommand(CAIConnection conn, CAICommand caiCommand) throws CAIException, IOException {
        if (log.isDebugEnabled()) log.debug("doCommand(conn=" + conn + ", caiCommand=" + caiCommand + ")");
        CAIException ex;
        int retries = 0;
        do {
            try {
                conn.sendCommand(caiCommand);
                if (log.isDebugEnabled()) log.debug("doCommand(CAIConnection, CAICommand) returns void");
                return;
            } catch (CAIException e) {
                if (log.isDebugEnabled()) log.debug("Exception in doCommand " + e);
                // if no errorcode defined some other error has occurred, don't do any retries
                if (e.getErrorCode() == 0) throw e;
                ex = e;
            }
        } while (retries++ < ProvisioningConfiguration.getInstance().getCommandSendRetries());

        throw ex;
    }

    /**
     * Setter for Provisioning's locateService
     *
     * @param locateService the locateService to use
     */
    public void setServiceLocator(ILocateService locateService) {
        this.locateService = locateService;
    }

    /**
     * Sets the event dispatcher that should be used to receive global events.
     *
     * @param eventDispatcher
     */
    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        eventDispatcher.addEventReceiver(this);
    }

    public void doEvent(Event event) {
        // do nothing
    }

    /**
     * This method is used to receive global events fired by any event dispatcher in the system.
     * <p/>
     * Provision Manager is only interested in the {@link com.mobeon.common.configuration.ConfigurationChanged}
     * event which is used to reload the configuration.
     */
    public void doGlobalEvent(Event event) {
        if (event instanceof ConfigurationChanged) {
            if (log.isDebugEnabled()) {
                log.debug("In doGlobalEvent: ConfigurationChanged event received, reloading configuration");
            }
            ConfigurationChanged configurationChanged = (ConfigurationChanged) event;
            this.configuration = configurationChanged.getConfiguration();
            updateConfiguration();
        }
    }

    /**
     * Setter for Provisioning's configuration
     *
     * @param configuration The configuration to use
     */
    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    private synchronized CAIConnectionPool getConnectionPool(String adminUid, String adminPwd) throws ProvisioningException {
        if (log.isDebugEnabled()) log.debug("getConnectionPool(adminUid=" + adminUid + ", adminPwd=" + adminPwd + ")");
        CAIConnectionPool pool = pools.get(adminUid);
        if (pool == null) {
            pool = createConnectionPool(adminUid, adminPwd);
            pools.put(adminUid, pool);
        } else {
            // Set password in case the provisionmanager client has changed password (e.g. after ConfigurationChanged)
            pool.setPassword(adminPwd);
        }
        if (log.isDebugEnabled()) log.debug("getConnectionPool(String, String) returns " + pool);
        return pool;
    }

    private CAIConnectionPool createConnectionPool(String adminUid, String adminPwd) throws ProvisioningException {
        if (log.isDebugEnabled()) log.debug("createConnectionPool(adminUid=" + adminUid + ", adminPwd=" + adminPwd + ")");
        try {
            IServiceInstance serviceInstance = locateService.locateService(IServiceName.PROVISIONING);
            String host = serviceInstance.getProperty(IServiceInstance.HOSTNAME);
            String portStr = serviceInstance.getProperty(IServiceInstance.PORT);
            
            if ((host == null) || (portStr == null)) {
                log.error("Exception in createConnectionPool host <" + host + "> or port <" + portStr + "> is null" );
                throw new ProvisioningException("Host or port is null");
            }
            
            int port = Integer.parseInt(portStr);

            CAIConnectionPool pool = new CAIConnectionPool(host, port, adminUid, adminPwd);
            if (log.isDebugEnabled()) pool.setCommSpy(caiDebug);

            pool.setMaxSize(ProvisioningConfiguration.getInstance().getConnectionPoolSize());
            pool.setTimeoutLimit(ProvisioningConfiguration.getInstance().getConnectionTimeout());
            pool.setIdleTimeoutLimit(ProvisioningConfiguration.getInstance().getConnectionIdleTimeout());

            if (log.isDebugEnabled()) log.debug("createConnectionPool(String, String) returns " + pool);
            return pool;
        } catch (NoServiceFoundException e) {
        	e.printStackTrace(System.out);
            log.error("Exception in createConnectionPool, not able to locate service: " + IServiceName.PROVISIONING);
            throw new ProvisioningException(e);
        }
        catch (NumberFormatException e) {
        	e.printStackTrace(System.out);
            log.error("Exception in createConnectionPool, port is not numeric for service: " + IServiceName.PROVISIONING);
            throw new ProvisioningException(e);
        }
    }

    private void updateConfiguration() {
        ProvisioningConfiguration.getInstance().setConfiguration(configuration);
        try {
            ProvisioningConfiguration.getInstance().update();
        } catch (ConfigurationException e) {
            log.error("Exception in setupConfiguration " + e);
        }
    }

    /**
     * Utility function to create a CreateCommand from a Subscription object. If an attribute is missing (mailhost) is added
     * from configuration.
     *
     * @param sub
     * @return a new CreateCommand
     * @throws ProvisioningException
     */
    CreateCommand makeCreateCommand(Subscription sub) throws ProvisioningException {
        Map<String, String[]> attrs = sub.getAttributes();
        // This will change the Subscription object for the client also, maybe should be changed someday TODO
        String[] values = attrs.remove(CAISchema.TELEPHONENUMBER);
        if (values == null) throw new ProvisioningException("Attribute " + CAISchema.TELEPHONENUMBER + " is missing in Subscription");

        String[] telephonenumber = values;
        CreateCommand c = new CreateCommand(telephonenumber[0]);

        addMissingAttributes(attrs, c);

        for (String name : attrs.keySet()) {
            values = attrs.get(name);
            c.addAttribute(name, values[0]);
        }
        // Reset telephonenumber attribute (maybe CreateCommand should be "smarter" and take a Subscription as
        // constructor parameter?) TODO
        attrs.put(CAISchema.TELEPHONENUMBER, telephonenumber);
        return c;
    }

    private void addMissingAttributes(Map<String, String[]> attrs, CreateCommand c) throws ProvisioningException {
        if (attrs.get(CAISchema.MAILHOST) == null) {
            String mailHost = ProvisioningConfiguration.getInstance().getDefaultMailhost();
            if (mailHost == null || mailHost.length() == 0) {
                    throw new ProvisioningException("No default mailhost configured,can't create the subscriber.");
            }
            c.addAttribute(CAISchema.MAILHOST, mailHost);
        }
    }

    /**
     * Utility function to create a DeleteCommand from a Subscription object
     *
     * @param sub
     * @return a new DeleteCommand
     * @throws ProvisioningException
     */
    private DeleteCommand makeDeleteCommand(Subscription sub) throws ProvisioningException {
        Map<String, String[]> attrs = sub.getAttributes();
        String[] values = attrs.get(CAISchema.TELEPHONENUMBER);
        if (values == null) throw new ProvisioningException("Attribute " + CAISchema.TELEPHONENUMBER + " is missing in Subscription");

        return new DeleteCommand(values[0]);
    }

    private synchronized int getTransactionId() {
        return lastTransactionId++;
    }

    private enum CommandMode {
        CREATE, DELETE
    }

    /**
     * Contains information how to do a command in the future. Used for the asynchronous function in this component.
     * The call function is executed by a ExecutorService thread.
     */
    class CommandCaller implements Callable<CommandCaller> {
        private Subscription sub;
        private String adminUid;
        private String adminPwd;
        private ProvisioningException ex = null;
        private CommandMode commandMode;

        /**
         * Constructor.
         *
         * @param sub
         * @param adminUid
         * @param adminPwd
         * @param commandMode
         */
        CommandCaller(Subscription sub, String adminUid, String adminPwd, CommandMode commandMode) {
            this.sub = sub;
            this.adminUid = adminUid;
            this.adminPwd = adminPwd;
            this.commandMode = commandMode;
        }

        /**
         * Calls the synchronous version of the create or delete methods on Provisioning
         *
         * @return this
         * @throws Exception
         */
        public CommandCaller call() throws Exception {
            try {
                if (commandMode == CommandMode.CREATE) create(sub, adminUid, adminPwd);
                else if (commandMode == CommandMode.DELETE) delete(sub, adminUid, adminPwd);
            } catch (ProvisioningException e) {
                ex = e;
            }
            return this;
        }

        /**
         * @return an ProvisioningException if something went wrong, null if everything was OK.
         */
        ProvisioningException getException() {
            return ex;
        }
    }

    class CAIDebug implements CAICommSpy {
        public void println(String line) {
            log.debug("> " + line);
        }

        public void readLine(String line) {
            log.debug("< " + line);
        }

        public void debug(String msg) {
            log.debug(msg);
        }
    }
}
