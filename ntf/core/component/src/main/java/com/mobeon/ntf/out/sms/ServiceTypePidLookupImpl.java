package com.mobeon.ntf.out.sms;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.mobeon.ntf.Config;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.IntfServiceTypePidLookup;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;


public class ServiceTypePidLookupImpl implements IntfServiceTypePidLookup {
    
    private static LogAgent log = NtfCmnLogger.getLogAgent(ServiceTypePidLookupImpl.class);
    
    private static final int PID_BASE=0x41; //the first value of a valid PID see SMPP standard. 

    private HashMap<String,ReplaceClass> contentToClass = null;
    private HashMap<String,Collection<ReplaceClass>> serviceToPos = null;
    private ReplaceClass[] replaceClasses;
    private boolean disableSmscReplace = true;    
 

    private static ServiceTypePidLookupImpl _inst = new ServiceTypePidLookupImpl();
    
    
    public static ServiceTypePidLookupImpl get() {
        if (_inst == null) {
            _inst = new ServiceTypePidLookupImpl();
        }
        return _inst;        
    }

    /**
     * Private constructor, no instances needed.
     */
    private ServiceTypePidLookupImpl() {
        refreshConfig();
    }

    public void  refreshConfig() {
        //this is heavy on parsing to read every time. so keep local copies and refresh as needed.
        disableSmscReplace = Config.isDisableSmscReplace(); 
        contentToClass = new HashMap<String, ReplaceClass>();
        serviceToPos = new HashMap<String, Collection<ReplaceClass>>();
        int tableSize = Config.getReplaceNotifications().length;
        replaceClasses = new ReplaceClass[tableSize];
        String[] rns = Config.getReplaceNotifications();
        for (int i = 0; i < tableSize; i++) {
            ReplaceClass replaceClass = new ReplaceClass(i,rns[i]);
            if (replaceClass.valid == false) {
                continue; //skip invalid entry.
            }
            contentToClass.put(replaceClass.contentType, replaceClass);
            //note: can be more than one position with same serviceType. hence an array.
            Collection<ReplaceClass> classes = serviceToPos.get(replaceClass.serviceType);
            if( classes == null ) {
                //first time and maybe only.
                classes = new ArrayList<ServiceTypePidLookupImpl.ReplaceClass>();
                classes.add(replaceClass);
                serviceToPos.put(replaceClass.serviceType, classes);
            } else {
                //add another item to to the collections based on this serviceType, more than one in the list.
                classes.add(replaceClass);
            }
            
            //add to the position lookup array.
            replaceClasses[i]=replaceClass;
        }
    }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.out.sms.IntfServiceTypePidLookup#isReplace(java.lang.String)
     */
    @Override
    public boolean isReplace(String contentType) {
        if (!disableSmscReplace) {
            return (contentToClass.get(contentType) != null);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.out.sms.IntfServiceTypePidLookup#getServiceType(java.lang.String)
     */
    @Override
    public String getServiceType(String contentType) {
        ReplaceClass rc = contentToClass.get(contentType.toLowerCase());
        if (rc != null ) {
            return rc.serviceType;
        }
        return Config.getSmeServiceType();
    }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.out.sms.IntfServiceTypePidLookup#getPid(java.lang.String)
     */
    @Override
    public int getPid(String contentType) {
        ReplaceClass rc = contentToClass.get(contentType.toLowerCase());
        if (rc != null ) {
            return rc.pid;
        }
        return 0;
    }

    private class ReplaceClass {
        public String serviceType;
        public int pid=0;
        public int position;     
        public boolean valid = true;
        public String contentType; //the name of the lookup value in the table.

        public ReplaceClass(int position, String rns) {
            
            if (rns.length() == 0 )
            {
                log.error("Invalid value in ReplaceNotifications.List for position [" + position + "] value [" + rns + "]");
                valid  = false;
            }
            
            String[] values = rns.split(",");
            
            contentType=values[0].toLowerCase(); //store always as lower case, same as Template.java or lookup can fail in some cases.
            
            if (values.length > 3) {
                log.error("To many fields in ReplaceNotifications.List for position [" + position + "] value [" + rns + "]");
                valid  = false;
            }
            
            
            if (position < 9) {
                serviceType = "VM"+(position+1); //start at 1 due to legacy.
            } else {
                serviceType = "V"+(position+1);
            }
            
            if (values.length > 1) { //if serviceType is set in table value.
                String type=values[1];
                
                int len = type.length();
                if (len > 6) {
                    log.error("ServiceType to long in ReplaceNotifications.List entry for position[" + position + "] value [" + rns + "]");
                    valid = false;
                    return;
                } 
                else if (len > 0) {
                   serviceType=type;
                } else {
                    log.debug("ServiceType set to default for position [" + position + "] value:[" + rns + "] serviceType: " + serviceType +"]");
                }
            }
            if (values.length < 3) {
                pid=position+PID_BASE;
                if (pid>71) {pid=0;} //PID(j) 0=no replace on mobile, PID only supports 7 types (3 bit) 0 indicates off
            } else {
                int replaceTypePos;
                try {
                    replaceTypePos = Integer.valueOf(values[2]);
                } catch (NumberFormatException nfe) {
                    log.error("Replace PID position is not a number for ReplaceNotifications.List entry for position[" + position + "] value [" + rns + "]");
                   valid = false;
                   return;
                }
                if (replaceTypePos >= 0 && replaceTypePos <= 7) {
                    if (replaceTypePos == 0) {
                       pid=0; //turned off.
                    } else {
                        //this starts at 1 so needs -1 as opposed to position above which starts at 0.
                        pid = (replaceTypePos-1)+PID_BASE;
                    }
                    
                } else {
                    log.error("Replace PID position illegal value for ReplaceNotifications.List entry for position[" + position + "] value [" + rns + "]");
                    valid = false;
                    return;
                }
            }

            this.position=position;
            log.debug("Parsed replace.list.value: " + this.toString());
        }
        
        public String toString() {
            StringBuilder result = new StringBuilder();
            
            result.append("ReplaceClass {replaceType [")
            .append(contentType)
            .append("], position [")
            .append(position)
            .append(", PID [0x")
            .append(Integer.toHexString(pid))           
            .append("], ServiceType [")
            .append(serviceType)
            .append("]}");
            
            return result.toString();
        }
    }


    /* (non-Javadoc)
     * @see com.mobeon.ntf.out.sms.IntfServiceTypePidLookup#getPosition(java.lang.String)
     */
    @Override
    public  int getPosition(String replaceTypeName) {
        ReplaceClass rc = contentToClass.get(replaceTypeName);
        if (rc != null ) {
            return rc.position;
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.out.sms.IntfServiceTypePidLookup#getServiceType(int)
     */
    @Override
    public  String getServiceType(int position) {
        if (position >= 0 && position < replaceClasses.length) {
            ReplaceClass rClass = replaceClasses[position];
            if (rClass == null) {return null;}
            return (rClass.serviceType);
        }
        return null;     
    }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.out.sms.IntfServiceTypePidLookup#getPid(int)
     */
    @Override
    public  int getPid(int position) {
        if (position >= 0 && position < replaceClasses.length) {
            ReplaceClass rClass = replaceClasses[position];
            if (rClass == null) {return 0;}
            return rClass.pid;         
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.IntfServiceTypePidLookup#getPositionByServiceType(java.lang.String)
     */
    @Override
    public int[] getPositionByServiceType(String replaceTypeName) {
        int[] result;
        Collection<ReplaceClass> rc = serviceToPos.get(replaceTypeName);
        if (rc != null ) {
            result = new int[rc.size()];
            Iterator<ReplaceClass> iter = rc.iterator();
            int i = 0;
            while (iter.hasNext()) {
                result[i++] = iter.next().position;
            }
            return result;
        } else {
            result = new int[1];
            result[0]=-1;
        }
        return result;
       
    }
}
