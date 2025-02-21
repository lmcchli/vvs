package com.mobeon.smsc.containers;

import com.mobeon.smsc.containers.ESME;


/**
 * Container for a ESME account.
 */
public class Account {

    /*Account password**/
    private String password = null;
    /*Account uid**/
    private String uid = null;
    /*Max number of binded accounts.**/
    private int maxConnections;
    /*Number of threads bounded to this account.**/
    private int connections;
    /*Number of received SMS PDU requests**/
    private int receivedSMSPdu = 0;
    
    /** 
     * Creates a new instance of Account 
     */
    public Account(String uid, String pwd, int maxConnections) {
        this.uid = uid;
        this.password = pwd;
        this.maxConnections = maxConnections;
    }                    
    
    /*@return true if there are free connections for this account**/
    public boolean hasFreeConnections(){        
        /*        if ( connections >= maxConnections ) return false;
                  else*/ return true;
    }
    
    /*@return the uid for this account**/
    public String getName(){ return uid;}        
    /*@return the password for this account**/
    public String getPassword(){ return password;}        
    /*@param name the uid for this account**/
    public void setName(String name){ uid = name;}        
    /*@param pwd the password for this account**/
    public void setPassword(String pwd){ password = pwd;}
    /*Increases number of received SMS PDU requests by one**/
    public void increaseSMSPduRequests(){ receivedSMSPdu++; }
    /*@return number of received SMS PDU requests**/
    public int getSMSPduRequests(){ return receivedSMSPdu; }
    /*@return number of received SMS PDU requests**/
    public void resetSMSPduRequests(){ receivedSMSPdu = 0; }
    /*increases the number of connections used for this account**/
    public void increaseConnection(){ connections++; }    
    /*decrease the number of connections used for this account**/
    public void decreaseConnection(){ if (connections > 0 ) connections--; }
    /*@return the number of used connections for this account**/    
    public int getUsedConnections(){ return connections; }
    /*@return max number of connections that can be allocated for this account**/
    public int getConnections(){ return maxConnections; }       
    /*@param size is the number of connections that can be allocated for this account**/
    public void setConnections(int size){ maxConnections = size; }  
}
