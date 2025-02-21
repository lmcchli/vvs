package com.mobeon.masp.servicerequestmanager.states;

/**
 * @author mmawi
 */
public interface AdministrativeState {
    public void closeForced();
    public void closeUnforced();
    public void open();
    public void removeSession();
}
