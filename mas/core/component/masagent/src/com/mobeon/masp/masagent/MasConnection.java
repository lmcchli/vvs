package com.mobeon.masp.masagent;

import com.mobeon.masp.rpcclient.*;
import com.mobeon.common.cmnaccess.oam.MoipFaultManager;
import com.mobeon.common.cmnaccess.oam.CommonAlarmStatus.AlarmStatus;
import com.mobeon.common.configuration.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.OMMConfiguration;
import com.mobeon.masp.operateandmaintainmanager.OperationalState;
import com.mobeon.masp.operateandmaintainmanager.Status;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * User: eperber
 * Date: 2006-feb-10
 * Time: 15:47:56
 */
@SuppressWarnings ({ "EmptyCatchBlock" })
public final class MasConnection {
    private static ILogger log = ILoggerFactory.getILogger(MasConnection.class);

    private static long prevPoll = 0; // System.currentTimeMillis();
    private static final long pollTimeout = 1000;
    private static MasMibAttributes attributes = null;
    private RpcClient rpcClient;
    private static String rpcHost;
    private static int rpcPort;

    public MasConnection() {
    	// FIXME: really should use the local configuration for this
    	// but don't know when OMMConfiguration gets initialized.
    	// Maybe we can move all this code to init: why is the
    	// RPC client initialized here and not after we read config?
	    setRpcHost("localhost"); // OMMConfiguration.getHostName());
	    setRpcPort(8081);
	    setRpcClient(new RpcClient(rpcHost, Integer.toString(rpcPort)));
    }

    public static MasConnection getInstance() {
	return new MasConnection();
    }

    private static OMMConfiguration ommConfig = null;
    public static void init(IConfiguration cfg) {
    	/*
    	 * if (ommConfig == null){
    		ommConfig = new OMMConfiguration(cfg);
    	}
    	 */
    	setRpcHost("localhost");//OMMConfiguration.getHostName());
    	// FIXME: really should use the local configuration for this
    	// but don't know when OMMConfiguration gets initialized.
	    setRpcPort(8081);
	   // IGroup group = cfg.getGroup("operateandmaintainmanager.omm");
    }

    public void setRpcClient(RpcClient rpcClient) {
	this.rpcClient = rpcClient;
    }

    public static void setRpcHost(String rpcHost) {
	MasConnection.rpcHost = rpcHost;
    }

    public static void setRpcPort(int rpcPort) {
	MasConnection.rpcPort = rpcPort;
    }

    /**
     * Gets the MIB values from MAS. The values are requested only if a timeout
     * has expired. The cache is shared by all instances of the MasConnection
     * class.
     *
     * @return An object of the MasMibAttribute class which contains all MIB
     *         values.
     */
    public MasMibAttributes getValues() {
	synchronized (MasConnection.class) {
	    long currentTime = System.currentTimeMillis();
	    if (currentTime - prevPoll >= pollTimeout) {
		// Timeout on the MIB values. Get new.

		try {
		    attributes = (MasMibAttributes) rpcClient.getMibAttributes();
		    if (attributes == null) {
		    	log.debug("MasConnection:getValues() - MIB attributes returned by the rpcClient are null. We'll return mib attributes for mas is down.");
		    	attributes = getMasIsDownAttributes();
		    }
		} catch (IOException e) {
		    // Todo: Verify log level with Torsten.
		    if (log.isDebugEnabled()) {
		    	log.debug("Unable to aquire new MIB attribute values. The mib attributes returned will be for mas is down.");
		    }
		    attributes = getMasIsDownAttributes();
		    return attributes;
		}
		prevPoll = currentTime;
	    }
	}
	return attributes;
    }

	public static Date getMasInstallDate(String masInstallDate) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd:HHmmss");
		Date date = cal.getTime();
		try {
			date = dateFormat.parse(masInstallDate);
		} catch (ParseException e) {
	        log.error("Unable to read masInstallDate: ["+masInstallDate+"]");
		}
		return date;
	}

    private MasMibAttributes getMasIsDownAttributes() {

		MasMibAttributes masIsDownAttributes =  new MasMibAttributes();
		masIsDownAttributes.masName = System.getenv("MAS_HOST");
		masIsDownAttributes.masVersion = System.getenv("MAS_VERSION");
		masIsDownAttributes.masOperationalState = OperationalState.DISABLED;
		masIsDownAttributes.masAdministrativeState = "unlocked";   // TODO Change to use enum
		masIsDownAttributes.masInstallDate = getMasInstallDate(System.getenv("MAS_INSTALL_DATE"));
		masIsDownAttributes.masReloadConfigurationTime = Calendar.getInstance().getTime();
		masIsDownAttributes.masReloadConfiguration = ReloadConfiguration.OK.getIndex();
		masIsDownAttributes.masAccumulatedUpTime = new Long(0);
		masIsDownAttributes.masCurrentUpTime = new Long(0);

		/* set a default masMibProvidedService */
		MasMibProvidedServices masMibProvidedService =  new MasMibProvidedServices();
		masIsDownAttributes.providedServices.add(masMibProvidedService);

		/* set a default consumedService */
		MasMibConsumedServices masMibConsumedService =  new MasMibConsumedServices();
		masIsDownAttributes.consumedServices.add(masMibConsumedService);

		/* set a default masMibServiceEnabler */
		MasMibServiceEnabler masMibServiceEnabler = new MasMibServiceEnabler();
		MasMibConnection masMibConnection = new MasMibConnection();
		masMibServiceEnabler.connections.add(masMibConnection);
		masIsDownAttributes.serviceEnablers.add(masMibServiceEnabler);
		return masIsDownAttributes;
	}

	/**
     * Sends a lock command to MAS.
     */
    public void lock() {
	try {
	    log.info("Sending lock command to MAS.");
	    rpcClient.sendCommand("setAdminState", "locked");
	} catch (IOException e) {
	}
    }

    /**
     * Sends an unlock command to MAS.
     */
    public void unlock() {
	try {
	    log.info("Sending unlock command to MAS.");
	    rpcClient.sendCommand("setAdminState", "unlocked");
	} catch (IOException e) {
	}
    }

    /**
     * Sends a shutdown command to MAS.
     */
    public void shutdown() {
	try {
	    log.info("Sending shutdown command to MAS.");
	    rpcClient.sendCommand("setAdminState", "shutdown");
	} catch (IOException e) {
	}
    }

    /**
     * Sends a reload configuration command to MAS.
     */
    public void reloadConfiguration() {
	try {
	    log.info("Sending reload configuration command to MAS.");
	    rpcClient.sendCommand("reloadConfiguration", "");
	} catch (IOException e) {
	}
    }

    /**
     * Updates the status of a service in MAS.
     *
     * @param serviceName The name of the service.
     * @param status      The current status.
     */
    public void updateStatus(String serviceName, Status status) {
	try {
		log.debug("Sending update status command to MAS.");
	    Vector<Serializable> param = new Vector<Serializable>(2);
	    param.add(serviceName);
	    param.add(XmlRpcEncode.encode(status));
	    rpcClient.sendCommand("updateServiceStatus", param);
	} catch (IOException e) {
		log.debug(e.getMessage());
	}
    }
}
