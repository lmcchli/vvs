/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wireline;

import com.mobeon.ntf.Config;
import com.mobeon.common.externalcomponentregister.IServiceInstance;

import java.util.*;
import com.mobeon.common.radius.LocalHost;

/**
 * <pre>
 * Used to select a server according to the "MER-algoritm"
 * Which is explained here:
 *
 * XMP Server selection algorithm
 * ------------------------------
 * This algorithm states how NTF hosts shall find the correct XMP
 * server from the list of servers in MCR. This algorithm provides
 * scalability and high availability of the outdial service within the
 * system.
 *
 * Note that the randomness in this algorithm is crucial. It is not
 * accepted that the client selects the same XMP server each time the
 * algorithm is started.
 *
 * Variables:
 *  ----------
 * The list of XMP servers in MCR is organized in a flat list and must be
 * divided into two groups:
 *
 * Group1:  The set of MER servers that is located on the same subnet
 *          as the MER client. See description below.
 *
 * Group2:  The rest of the XMP servers. These are not located on the
 *          same subnet as the NTF host.
 *
 * State:   Each XMP server must have a corresponding state variable that
 *          can have the following values: "available" or "unavailable"
 *
 * Algorithm:
 * ----------
 *
 *  Step 1:  Update Group1 and Group2 using the content of MCR.
 *
 *  Step 2:  Set all state variables to "available".
 *
 *  Step 3:  Randomly select a XMP server from Group1 that has
 *           state="available".
 *
 *           If the MER server does respond, continue to use this MER
 *           server until it stops responding, then set
 *           state="unavailable" for this server, update Group1 and
 *           Group2 using the content of MCR and goto Step 3.
 *
 *           If the XMP server does not respond, then set
 *           state="unavailable" for this server and goto Step 3.
 *
 *           If there are no MER server left in Group1 with
 *           state="available" then goto step 4.
 *
 *  Step 4:  Randomly select a XMP server from Group2 that has
 *           state="available".
 *
 *           If the XMP server does respond, continue to use this XMP
 *           server for a configurable amount of time (default 5 minutes).
 *           Then goto Step 1.
 *
 *           If the XMP server does not respond, then set
 *           state="unavailable" for this server and goto Step 4.
 *
 *           If there are no XMP server left in Group 2 with
 *           state="available" then wait for a configurable amount of
 *           time (default 1 minute), then goto step 1.
 *
 *
 *
 * How to know if a XMP server is on the same subnet as yourself:
 * --------------------------------------------------------------
 * 1. Let OwnIP be the IP-address of your own host.
 * 2. Let OwnNetmask be the netmask of your own host.
 * 3. Let NTFIP be the IP-address of the NTF instance's host.
 * 4. Let OwnNet be (OwnIP & OwnNetmask). (Do a bit-and.)
 * 5. Let MERNet be (MERIP & OwnNetmask). (Do a bit-and.)
 * 6. If OwnNet is equal to NTFNet, you are on the same LAN subnet.
 * </pre>
 */
public class SS7GatewaySorter{
    
    private Vector<IServiceInstance> _localGroup = new Vector<IServiceInstance>();
    private Vector<IServiceInstance> _nonlocalGroup = new Vector<IServiceInstance>();
    private boolean _onLocalList=true;
    private int _currentEntryInList=0;
    private LocalHost hostUtil;
    
    public SS7GatewaySorter(){
        hostUtil=LocalHost.getInstance();
        hostUtil.setLocalHostNetMask(Config.getNetmask());
    }
    
    /**
     * Updating both lists with new server entries
     *
     * @param clients List of new server entries
     */
    public void updateClients(List<IServiceInstance> clients){
        
        if(clients==null || clients.isEmpty()) return;
        
        //Updating (Step 1)
        
        _localGroup = new Vector<IServiceInstance>();
        _nonlocalGroup = new Vector<IServiceInstance>();
        LocalHost hostUtil=LocalHost.getInstance();
        for(IServiceInstance entry : clients){	
        	if(isLocal(entry)){
        		_localGroup.add(entry);
        	}
        	else{
        		_nonlocalGroup.add(entry);
        	}
        }
        _onLocalList=true;
        //All avialable (Step 2)
    }
    
    /**
     * Checks if a component is local. A component is local if it is in the same
     * logical zone as NTF. If the logical zone is missing, it is local if NTF and
     * the component are on the same network.
     *@param comp - the component that shall be checked.
     *@return true if the component is local to this NTF.
     */
    public boolean isLocal(IServiceInstance instance) {
    	String instLogicalZone = instance.getProperty(IServiceInstance.LOGICALZONE);
        if (Config.getLogicalZone() != null
            && (! "".equals(Config.getLogicalZone()))
            && instLogicalZone != null
            && (! "".equals(instLogicalZone))) {
            return Config.getLogicalZone().equals(instLogicalZone);
        } else {
            return hostUtil.isInSubnetByName(instance.getProperty(IServiceInstance.HOSTNAME));
        }
    }

    /** Get all local xmp hosts*/
    public Vector<IServiceInstance> getLocalHosts(){        
        return _localGroup;
    }
    /** Get all nonlocal xmp hosts*/
    public Vector<IServiceInstance> getNonLocalHosts(){
        return _nonlocalGroup;
    }
    
    
    /**
     * Selects a host according to the Algoritm written by the MER
     * component
     *
     * @param remove If current entry is to be removed from the list
     * @return String that is the selected server
     */
    public IServiceInstance selectClient(boolean remove){
        if(_localGroup.isEmpty() && _nonlocalGroup.isEmpty()) return null;
        
        if(_onLocalList){
            if(remove) _localGroup.removeElementAt(_currentEntryInList);
            int noOfLocals=_localGroup.size();
            if(noOfLocals>0){
                //Random selection (Step 3)
                _currentEntryInList = randomize(noOfLocals);
                return _localGroup.get(_currentEntryInList);                
            }
            else{
                //No XMP servers on local group goto step 4
                _onLocalList=false;
                remove=false;//Can't remove if we are changing to nonlocal-list
            }
        }
        if(!_onLocalList){
            if(remove) _nonlocalGroup.removeElementAt(_currentEntryInList);
            int noOfNonLocals=_nonlocalGroup.size();
            if(noOfNonLocals>0){
                //Random selection of non-locals (Step 4)
                _currentEntryInList=randomize(noOfNonLocals);
                _onLocalList=false;
                return _nonlocalGroup.get(_currentEntryInList);
            }
            else{
                //Step 1 again
                return null;//Must refresh lists with new servers
            }
        }
        return null;//To make the compiler happy
    }
    
    /**
     * If the chosen XMP server is on the local subnet
     *
     * @return true if on same subnet, false othervise
     */
    public boolean isNonLocal(){
        return !_onLocalList;
    }
    
    private int randomize(int max){
        int r=new Random().nextInt();
        if(r<0) r=0-r;
        return r % max;
    }
    
}
