package com.abcxyz.services.moip.common.directoryaccess;

import java.net.URI;
import java.util.List;

import com.abcxyz.messaging.common.mcd.KeyValues;
import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.OamFacade;
import com.abcxyz.messaging.common.oam.impl.ExtendableConfigAgent;
import com.abcxyz.messaging.mcd.proxy.MCDProxyService;
import com.abcxyz.messaging.mcd.proxy.MCDProxyServiceFactory;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

public class DirectoryUpdater extends DirectoryAccess implements IDirectoryUpdater{
	protected MCDProxyService  cai3gclient;
	protected static DirectoryUpdater directoryUpdater = null;
	private static final ILogger logg = ILoggerFactory.getILogger(DirectoryUpdater.class);
    private static final HostedServiceLogger log = new HostedServiceLogger(logg);


	public static DirectoryUpdater getInstance() {
		if (directoryUpdater == null) {
			synchronized(DirectoryUpdater.class){
				if(directoryUpdater == null) {
					directoryUpdater = new DirectoryUpdater();
				}
			}
		}
		return directoryUpdater;
	}

	protected void initializeCai3g(){
        try {
        	CommonOamManager oamManager = CommonOamManager.getInstance();
        	OAMManager mcdOam = oamManager.getMcdOam();
        	
        	//Using a new OamFacade to be sure that we don't have a race condition with the one that init MCDClient.
        	OamFacade cai3gOam = new OamFacade();
        	cai3gOam.setConfigManager(new ExtendableConfigAgent(mcdOam.getConfigManager()));
        	cai3gOam.setFaultManager(mcdOam.getFaultManager());
        	cai3gOam.setLogAgent(mcdOam.getLogAgent());
        	cai3gOam.setLoggingManager(mcdOam.getLoggingManager());
        	cai3gOam.setPerformanceManager(mcdOam.getPerformanceManager());
        	cai3gOam.setProfilerAgent(mcdOam.getProfilerAgent());
        	cai3gOam.setStateManager(mcdOam.getStateManager());
        	cai3gOam.setTrafficLogAgent(mcdOam.getTrafficLogAgent());
        	
        	ConfigManager mcdConfiguration = cai3gOam.getConfigManager();
        	String oldProxyClass = mcdConfiguration.getParameter(MCDConstants.CONFIG_SERVICEPROXYCLASS);
        	if ((oldProxyClass == null) || (oldProxyClass.length()==0)){
        		oldProxyClass = MCDProxyServiceFactory.DEFAULT_SERVICE_IMPL;
        	}
        	mcdConfiguration.setParameter(MCDConstants.CONFIG_SERVICEPROXYCLASS, "com.abcxyz.messaging.provisioningagent.cai3gclient.CAI3GProvisionerService");
        	cai3gclient = MCDProxyServiceFactory.getMCDProxyService(cai3gOam);
        	// Now that we have initialized, put back the old one
        	mcdConfiguration.setParameter(MCDConstants.CONFIG_SERVICEPROXYCLASS, oldProxyClass);
        }
        catch (Exception e){
            log.error("DirectoryUpdater::initializeCai3g Exception: "+e.getMessage(),e);
            cai3gclient = null;
        }
    }

	public Profile lookup(String profileClass, URI uri) {
		return super.lookup(profileClass,uri);
	}


	public void createProfile(String profileClass, URI keyId, Profile entity) throws DirectoryAccessException {

		if (log.isDebugEnabled()) {
    		log.debug("DirectoryUpdater.createProfile, profileClass=" + profileClass + ", keyId=" + keyId.toASCIIString() + ", profile=" + entity.toString());
        }

		try {
			if (cai3gclient == null) initializeCai3g();
			if (log.isDebugEnabled()) {
	    		log.debug("DirectoryUpdater.createProfile, call createProfile");
	        }
			cai3gclient.createProfile(profileClass, keyId, entity);
		}
		catch (Exception e){

            log.error("DirectoryUpdater::initializeCai3g Exception: "+e,e);
            cai3gclient = null;
            throw new DirectoryAccessException("Got exception " + e.toString() + " while creating profile " + keyId.toString());
		}
	}
	
	public void autoprovisionProfile(String profileClass, URI keyId, Profile entity) throws DirectoryAccessException {
		if (log.isDebugEnabled()) {
    		log.debug("DirectoryUpdater.autoprovisionProfile, profileClass=" +
		              profileClass + ", keyId=" + keyId.toASCIIString() + ", profile=" + entity.toString());
        }

		try {
			if (cai3gclient == null) initializeCai3g();
			if (log.isDebugEnabled()) {
	    		log.debug("DirectoryUpdater.autoprovisionProfile, call autoprovisionProfile");
	        }
			cai3gclient.autoprovisionProfile(profileClass, keyId, entity);
		}
		catch (Exception e){

            log.error("DirectoryUpdater::initializeCai3g Exception: "+e,e);
            cai3gclient = null;
            throw new DirectoryAccessException("Got exception " + e.toString() + " while autoprovisioning profile " + keyId.toString());
		}
		
	}

	public void deleteProfile(String profileClass, URI keyId) throws DirectoryAccessException {
		if (log.isDebugEnabled()) {
    		log.debug("deleteProfile, profileClass=" + profileClass + ", keyId=" + keyId.toASCIIString());
        }
		try {
			if (cai3gclient == null) initializeCai3g();
			cai3gclient.deleteProfile(profileClass, keyId);
		}
		catch (Exception e){
            log.error("DirectoryUpdater::initializeCai3g Exception: "+e,e);
            cai3gclient = null;
            throw new DirectoryAccessException("Got exception " + e.getMessage() + " while deleting profile " + keyId.toString());
		}
	}


	public void updateSubscriber(IDirectoryAccessSubscriber subscriber, String attrName, String[] values) throws DirectoryAccessException {
		try {
			if (cai3gclient == null) initializeCai3g();

			KeyValues kv = new KeyValues(attrName, values);
			Modification mod = new Modification(Modification.Operation.REPLACE, kv);
			String msidValue = subscriber.getSubscriberIdentity("msid");
			if (!msidValue.startsWith("msid:")){
				msidValue = "msid:" + msidValue;
			}
			Modification mods[] = new Modification[1];
			cai3gclient.updateProfileAttributes("subscriber", new URI(msidValue), mods);
		}
		catch (Exception e) {
            log.error("DirectoryUpdater::initializeCai3g Exception: "+e,e);
            cai3gclient= null;
            throw new DirectoryAccessException("Got exception " + e.getMessage() + " while updating subcriber ");
		}
	}

	public void updateProfile(String profileClass, URI keyId, List<Modification> mods) throws DirectoryAccessException {

		if (log.isDebugEnabled()) {
    		log.debug("DirectoryUpdater.updateProfile, profileClass=" + profileClass + ", keyId=" + keyId.toASCIIString() + ", mods=" + mods.toString());
        }
		try {
			if (cai3gclient == null) initializeCai3g();
			Modification modArray[] = new Modification[mods.size()];
			modArray = mods.toArray(modArray);
			cai3gclient.updateProfileAttributes(profileClass, keyId, modArray);
		}
		catch (Exception e) {
            log.error("DirectoryUpdater::initializeCai3g Exception: "+e,e);
			cai3gclient= null;
            throw new DirectoryAccessException("Got exception " + e.getMessage() + " while updating profile "+ keyId.toString());

		}
	}


}
