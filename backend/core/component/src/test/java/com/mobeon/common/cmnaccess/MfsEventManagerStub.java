package com.mobeon.common.cmnaccess;

import java.io.File;
import java.io.FileFilter;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import com.mobeon.common.trafficeventsender.MfsConfiguration;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;

public class MfsEventManagerStub implements IMfsEventManager{

	private static TrafficEvent[] trafficEvents;
	
	@Override
	public void createLoginFile(String telephoneNumber)
			throws TrafficEventSenderException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean loginFileExists(String telephoneNumber) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean loginFileExistsAndValidDate(String telephoneNumber, int time) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public void createEmptyFile(String telephoneNumber, String fileName)
            throws TrafficEventSenderException {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void createPropertiesFile(String telephoneNumber, String prefix, Map<String, String> properties) throws TrafficEventSenderException {
    }

    @Override
    public boolean isInternal(String phoneNumber) {
        MfsEventManager mfsEventManager = new MfsEventManager();
        return mfsEventManager.isInternal(phoneNumber);
    }

    @Override
    public String getFileNameByExtension(String telephoneNumber, String eventName, String extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getFilesNameStartingWith(String telephoneNumber, final String startingWith) {
        MfsEventManager mfsEventManager = new MfsEventManager();
        return mfsEventManager.getFilesNameStartingWith(telephoneNumber, startingWith);
    }

    public String[] getFilePathsNameStartingWith(String telephoneNumber, final String startingWith) {
        MfsEventManager mfsEventManager = new MfsEventManager();
        return mfsEventManager.getFilePathsNameStartingWith(telephoneNumber, startingWith);
    }

    @Override
    public String[] getEventFileNames(String telephoneNumber, FileFilter fileFilter, boolean fullPath) {
        MfsEventManager mfsEventManager = new MfsEventManager();
        return mfsEventManager.getEventFileNames(telephoneNumber, fileFilter, fullPath);
    }

    @Override
    public File[] getEventFiles(String telephoneNumber, FileFilter fileFilter) {
        MfsEventManager mfsEventManager = new MfsEventManager();
        return mfsEventManager.getEventFiles(telephoneNumber, fileFilter);
    }
    
    @Override
    public boolean fileExists(String telephoneNumber, String fileName, boolean internal) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean fileExists(String telephoneNumber, String fileName, int interval, boolean internal) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeFile(String telephoneNumber, String fileName, boolean internal)
            throws TrafficEventSenderException {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean removeFile(String absoluteFilePath)
            throws TrafficEventSenderException {
        // TODO Auto-generated method stub
        return true;
    }

	@Override
    public boolean removeFile(String telephoneNumber, String fileName)
            throws TrafficEventSenderException {
        // TODO Auto-generated method stub
        return true;
    }

	@Override
	public void removeLoginFile(String telephoneNumber)
			throws TrafficEventSenderException {
		// TODO Auto-generated method stub
		
	}

    @Override
    public TrafficEvent[] retrieveEvents(String filePath, boolean internal)
            throws TrafficEventSenderException {
        return trafficEvents;
    }

    @Override
    public TrafficEvent[] retrieveEvents(String phoneNumber, String fileName) throws TrafficEventSenderException {
        return trafficEvents;
    }
    
    @Override
    public TrafficEvent[] retrieveEvents(String phoneNumber, String name, boolean internal)
            throws TrafficEventSenderException {
        return trafficEvents;
    }
	
	public void setTrafficEvents(TrafficEvent[] events){
		trafficEvents = events;
	}
	

	@Override
	public void storeEvent(String telephoneNumber, TrafficEvent event)
			throws TrafficEventSenderException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void storeVvaSms(String telephoneNumber, TrafficEvent event)
			throws TrafficEventSenderException {
		// TODO Auto-generated method stub
		
	}
	
    @Override
    public void storeAutoUnlockPinLockout(String telephoneNumber, TrafficEvent event) throws TrafficEventSenderException {
        // TODO Auto-generated method stub
        
    }

	@Override
	public void updateConfiguration(MfsConfiguration mfsConfiguration) {
		// TODO Auto-generated method stub

	}


	@Override
	public String[] getEventFiles(String telephoneNumber, String eventName)
			throws TrafficEventSenderException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getOutdialEvents(String number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getProperties(String telephoneNumber, String fileName) {
		// TODO Auto-generated method stub
	    return null;
	}

	@Override
	public void storeProperties(String telephoneNumber, String fileName, Properties props)
			throws TrafficEventSenderException {
		// TODO Auto-generated method stub
		
	}
    @Override
    public String[] getSendStatusEventFiles(String telephoneNumber, final String eventName, String order) throws TrafficEventSenderException {
        return null;
    }

    @Override
    public boolean isStorageOperationsAvailable(String originator, String recipient) {
        return true;
    }

    @Override
    public long acquireLockFile(String telephoneNumber, String fileName, int validityPeriodInSeconds, boolean internal) throws TrafficEventSenderException {
        return 0L;
    }

    @Override
    public long acquireLockFile(String telephoneNumber, String fileName, int validityPeriodInSeconds) throws TrafficEventSenderException {
        return 0L;
    }

    @Override
    public void releaseLockFile(String telephoneNumber, String fileName, long lockId, boolean internal) throws TrafficEventSenderException {
        return;
    }

    @Override
    public void releaseLockFile(String telephoneNumber, String fileName, long lockId) throws TrafficEventSenderException {
        return;
    }

	@Override
	public long getLastModified(File file) {
		return 0L;
	}

	@Override
	public Reader retrieveEventsAsReader(File file) throws TrafficEventSenderException {
		return null;
	}

	@Override
	public boolean renameFile(File orig, File dest) throws TrafficEventSenderException {
		return false;
	}
}
