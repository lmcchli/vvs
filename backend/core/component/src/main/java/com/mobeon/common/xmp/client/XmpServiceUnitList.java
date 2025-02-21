/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.xmp.client;

import java.util.*;

/**
 * XmpServiceUnitList keeps track of all units that support a particular service.
 */
public class XmpServiceUnitList {
    private XmpClient client = null;

    private String service;
    private Vector<XmpUnit> localUnits;
    private Vector<XmpUnit> nonLocalUnits;
    private Vector<XmpUnit> serviceUnavailableUnits;
    private int nextUnit;

    /**
     * Constructor.
     *@param service - the service this class shall handle.
     */
    XmpServiceUnitList(String service) {
        client = XmpClient.get();
        this.service = service;
        localUnits = new Vector<XmpUnit>();
        nonLocalUnits = new Vector<XmpUnit>();
        
        serviceUnavailableUnits = new Vector<XmpUnit>();
        nextUnit = 0;
    }

    /**
     * Adds a unit that handles this serviec.
     *@param unit - the new unit.
     */
    void addUnit(XmpUnit unit) {
        insertUnit(unit);
    }
    
    private void insertUnit(XmpUnit unit) {
        client.debug("Adding unit " + unit.getHost() + ":" + unit.getPort() + " from service " + service );
        if (unit.isLocal()) {
            localUnits.add(unit);
        } else {
            nonLocalUnits.add(unit);
        }
    }

    /**
     * Removes a unit that used to handle this service.
     *@param unit - the removed unit.
     */
    void removeUnit(XmpUnit unit) {
        client.debug("Removing unit " + unit.getHost() + ":" + unit.getPort() + " from service " + service );
        boolean dummy = localUnits.remove(unit)
            || nonLocalUnits.remove(unit)
            || serviceUnavailableUnits.remove(unit);
    }
    
    /**
     * Tells that a service has become unavailable.
     * Sleeps 5 minutes and then set status ok
     */
    void serviceUnavailable(XmpUnit unit) {
        if( serviceUnavailableUnits.contains(unit) ) {
                return;
        }
	client.debug( "Banning " + unit.toString() );
        
        client.setStatus(false, service, unit.getName(service) );
        if( unit.isLocal() ) {
            if( localUnits.remove(unit) ) {
                serviceUnavailableUnits.add(unit);
            }
        } else {
            if( nonLocalUnits.remove(unit) ) {
                serviceUnavailableUnits.add(unit);
            }
        }
        client.debug("Unavailable units: " + serviceUnavailableUnits.toString());
      
    }
    
    /**
     * Makes a unt available again after has been in service unavailable
     */
    void serviceAvailable(XmpUnit unit) {
        
        if( serviceUnavailableUnits.remove( unit ) ) {
            if( unit.isLocal() ) {
                localUnits.add(unit);
            } else {
                nonLocalUnits.add(unit);
            }
        }
        client.setStatus(true, service, unit.getName(service));
        unit.setUnitAvailable(true);
    }
      
    /**
     *corrects the list of local and nonlocal units,
     */
    void updateLocals() {
        for( int i=0;i<localUnits.size();i++ ) {
            XmpUnit unit = (XmpUnit) localUnits.get(i);
            if( !unit.isLocal() ) {
                client.debug("Making unit " + unit.getHost() + ":" + unit.getPort() + 
                    "non local for service " + service );
                localUnits.remove(i);
                nonLocalUnits.add(unit);
            }
        }
        
        for( int i=0;i<nonLocalUnits.size();i++ ) {
            XmpUnit unit = (XmpUnit) nonLocalUnits.get(i);
            if( unit.isLocal() ) {
                client.debug("Making unit " + unit.getHost() + ":" + unit.getPort() + 
                    "local for service " + service );
                nonLocalUnits.remove(i);
                localUnits.add(unit);
            }
        }
    }
    
    /**
     *@return a vector with all units handling this service.
     */
    Vector<XmpUnit> getUnits() {
        Vector<XmpUnit> all = new Vector<XmpUnit>();
        all.addAll(localUnits);
        all.addAll(nonLocalUnits);
        all.addAll(serviceUnavailableUnits);
        return all;
    }
            
    /**
     * Finds the best unit that can handle this service.
     * First priority is to use a local unit and second priority is to spread
     * requests evenly among the units. Thus the algorithm selects local units
     * in a round-robin fashion, but if no local units are available it does the
     * same with non-local units.
     *@return the selected XMP unit.
     */
    synchronized XmpUnit selectUnit() {
        XmpUnit u = null;
        while (localUnits.size() > 0) {
            if (nextUnit >= localUnits.size()) {
                nextUnit = 0;
            }
            u = (XmpUnit) localUnits.elementAt(nextUnit);
            if (u.isAvailable()) {
                client.debug("XmpServiceUnitList selecting local unit " + nextUnit);
                ++nextUnit;
                return u;
            } else {
                client.debug("XmpServiceUnitList setting local unit " + nextUnit + " unavailable");
                serviceUnavailable(u);
                
            }
        }
        while (nonLocalUnits.size() > 0) {
            if (nextUnit >= nonLocalUnits.size()) {
                nextUnit = 0;
            }
            u = (XmpUnit) nonLocalUnits.elementAt(nextUnit);
            if (u.isAvailable()) {
                client.debug("XmpServiceUnitList selecting nonlocal unit " + nextUnit);
                ++nextUnit;
                return u;
            } else {
                client.debug("XmpServiceUnitList setting nonlocal unit " + nextUnit + " unavailable");
                serviceUnavailable(u);
                
            }
        }
        return null;
    }
    
    Vector<XmpUnit> getLocalUnits() {
        return localUnits;
    }

    Vector<XmpUnit> getNonLocalUnits() {
        return nonLocalUnits;
    }

    Vector<XmpUnit> getUnAvailableUnits() {
        return serviceUnavailableUnits;
    }
    
    boolean hasUnit(String host, int port) {
        Vector<XmpUnit> units = getUnits();
        for( int i=0;i<units.size();i++ ) {
            XmpUnit unit = units.get(i);
            if(unit.getHost().equals(host) && unit.getPort() == port) {
                return true;
            }
        }
        return false;
    }
    
    public String toString() {
        int i;
        XmpUnit u;
        StringBuffer sb = new StringBuffer();
        sb.append("{XmpServiceUnitList service=").append(service);
        sb.append(" nextUnit=").append(nextUnit);

        sb.append(" local={");
        for (i = 0; i < localUnits.size(); i++) {
            u = localUnits.elementAt(i);
            sb.append(u.getHost()).append(":").append(u.getPort()).append(" ");
        }

        sb.append("} nonlocal={");
        for (i = 0; i < nonLocalUnits.size(); i++) {
            u = nonLocalUnits.elementAt(i);
            sb.append(u.getHost()).append(":").append(u.getPort()).append(" ");
        }

        sb.append("} unavailable={");
        for (i = 0; i < serviceUnavailableUnits.size(); i++) {
            u = serviceUnavailableUnits.elementAt(i);
            sb.append(u.getHost()).append(":").append(u.getPort()).append(" ");
        }

        return sb.toString() + "}}";
    }
}
