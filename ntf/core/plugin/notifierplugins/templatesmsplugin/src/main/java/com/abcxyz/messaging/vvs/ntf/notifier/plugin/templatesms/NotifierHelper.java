/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.INotifierDatabaseAccess;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.NotifierDatabaseException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.INotifierMfsEventManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierProfiler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.NotifierTypeState;

public class NotifierHelper {

    private static INotifierLogger log = TemplateSmsPlugin.getLoggerFactory().getLogger(NotifierHelper.class);
    private static INotifierMfsEventManager notifierMfsEventManager = TemplateSmsPlugin.getMfsEventManager();
    private static INotifierDatabaseAccess notifierDatabaseAccess = TemplateSmsPlugin.getDatabaseAccess();
    private static INotifierProfiler notifierProfiler = TemplateSmsPlugin.getProfiler();


    
  /** Filter for default  file filter */
  public static final FileFilter NOTIFIER_DEFAULT_FILE_FILTER = new FileFilter() {
      @Override
      public boolean accept(File file) {

    	  // put code here to filter files. if need be !
    	  
    	  return true;
      }
  };
   
     
    /**
     * Returns an array of private files for a given notification number as parsed by a file filter.
     * @param notificationNumber notification number for which files will be selected
     * @param filter FileFilter
     * @return an array of File objects
     */
    public static File[] getEventFiles(String notificationNumber, FileFilter filter) {
        return notifierMfsEventManager.getEventFiles(notificationNumber, filter);
    }

        
    
    /**
     * Returns a list of private files for a given notification number as parsed by a file filter.
     * @param notificationNumber notification number for which files will be selected
     * @param filter FileFilter
     * @return List of File objects
     */
    public static List<File> getEventFileList(String notificationNumber, FileFilter filter) {

        List<File> fileList = new ArrayList<File>();

        File[] files = notifierMfsEventManager.getEventFiles(notificationNumber, filter);
        
        if (files == null) {
            return fileList;
        }
        
        for(File file : files) {
            fileList.add(file);
        }

        return fileList;
    }


    public static NotifierTypeState getNotifierTypeStateFromEventId(String storedEventId) {
        NotifierTypeState notifierTypeState = NotifierTypeState.STATE_INITIAL;
        if (storedEventId == null || storedEventId.isEmpty()) {
            log.debug("Invalid event id for event " + storedEventId + ", will return state " + notifierTypeState.getName());
            return notifierTypeState;
        }

        String serviceName = TemplateSmsPlugin.getUtil().getEventServiceName(storedEventId);
        if(serviceName == null) {
            log.debug("Invalid event id for event " + storedEventId);
            return notifierTypeState;
        }

        String state = serviceName.substring(serviceName.indexOf(TemplateType.SERVICE_NAME_SEPARATOR)+1); 
        if (state.equalsIgnoreCase(NotifierTypeState.STATE_INITIAL.getName())) {
            notifierTypeState = NotifierTypeState.STATE_INITIAL;
        } else if (state.equalsIgnoreCase(NotifierTypeState.STATE_SENDING.getName())) {
            notifierTypeState = NotifierTypeState.STATE_SENDING;
        }  else {
            log.debug("Invalid event id received: " + storedEventId);
        }
        return notifierTypeState;
    }
    
    public static boolean removePayloadFile(String notificationNumber, Properties eventProperties) {
        boolean isRemoved = true;
        String payloadFileName = eventProperties.getProperty(NotifierConstants.MESSAGE_PAYLOAD_FILE_PROPERTY);
        if(payloadFileName != null) {
            isRemoved = notifierMfsEventManager.removeFile(notificationNumber, payloadFileName);
        }
        log.debug("Removed payload file " + payloadFileName + " for " + notificationNumber + ": " + isRemoved);
        return isRemoved;
    }
    
    /**
     * Retrieves the appropriate subscriber profile given the notifier type and event properties.
     * The subscriber profile can be from the MiO database or another source.
     * In this sample plug-in, the profile is retrieved from the MiO database only.
     * @param subscriberNumber the telephone number of the subscriber.
     * @param notifierTypeName the name of the notifier type to be sent out.
     * @param eventProperties the properties containing the information about the notification to be sent out.
     * @return ANotifierDatabaseSubscriberProfile object containing the subscriber's information and preferences.
     * @throws NotifierDatabaseException if an error is encountered while performing a database lookup.
     */
    public static ANotifierDatabaseSubscriberProfile getSubscriberProfile(String subscriberNumber, String notifierTypeName, Properties eventProperties) throws NotifierDatabaseException {
        ANotifierDatabaseSubscriberProfile subscriberProfile = null;

        log.debug("Performing database lookup for: " + subscriberNumber + " to send " + notifierTypeName);
        subscriberProfile = notifierDatabaseAccess.getSubscriberProfile(subscriberNumber);
        return subscriberProfile;
    }
    
    public static TemplateType getNotifierType(Properties eventProperties) {
        TemplateType notifierType = null;
        if(eventProperties != null && eventProperties.getProperty(NotifierConstants.NOTIFIER_TYPE_NAME_PROPERTY) != null) {
            notifierType = TemplateType.getTemplateType(eventProperties.getProperty(NotifierConstants.NOTIFIER_TYPE_NAME_PROPERTY).toLowerCase());
        }
        return notifierType;
    }
    
    public static void profilerCheckPoint(String checkPoint) {
        Object perf = null;
        try {
            perf = notifierProfiler.enterProfilerPoint(checkPoint);
        } finally {
            notifierProfiler.exitProfilerPoint(perf);
        }
    }
    
    
}
