package com.mobeon.smsc.containers;

import org.w3c.dom.Node;
import java.util.Hashtable;
import java.util.logging.*;
import java.util.Enumeration;
import java.util.Date;
import com.mobeon.smsc.containers.ESME;
import com.mobeon.smsc.smpp.util.SMSPdu;
import com.mobeon.smsc.interfaces.TrafficCentral;
import com.mobeon.smsc.interfaces.ConfigurationManagement;
import com.mobeon.smsc.interfaces.SmppConstants;

public class ESMEHandler implements TrafficCentral, 
                                    SmppConstants {
    
    /**A static instance of this class*/
    private static ESMEHandler instance = null;
    /**A static Hashtable for all registred ESMEs in the the system. Key is the ESME name and the value is a ESME object*/
    private static Hashtable cache = null;
    /**Logger to log, logs in the main log file(smsc.log)*/
    private Logger log;
    /**Handler to write the configuration changes to disk*/
    private static ConfigurationManagement updateConfig = null;
    
    /**Inits the cache of ESMEs*/
    static{
        cache = new Hashtable();
    }
    
    /**Constructor wich inits the logger*/
    public ESMEHandler(){
        log = Logger.getLogger("SMSC");

    }
    
    /**
     *@return an ESMEHandler instance
     */
    public static ESMEHandler get() {
        if (instance == null) {
            instance = new ESMEHandler();
        }
        return instance;
    }
    
    /**
     *Sets the updateConfig variable
     *@param updateConfig is a handle to write configuration changes
     *to disk
     */
    public void setConfigurationManagement(ConfigurationManagement updateConfig){                
        this.updateConfig = updateConfig;
    }
    
    /**
     *Adds a new ESME and zero or more accounts depending on the content of the 
     *account node.
     *@param esmeName the name of ESME to add
     *@param loglevel the log level for ESME to add
     *@param logsize the size of SMS cache
     *@param accounts is a node containing all accounts registred for the ESME
     */
    public void addESME(String esmeName,
                        String loglevel,
                        int logsize,
                        Node accounts){                             
        cache.put(esmeName, new ESME(esmeName, loglevel, logsize, accounts));        
    }        
    
    /**
     *Binds a ESME account to the SMS-C. If the cache does not
     *contain the ESME name a the status code SMPPSTATUS_RINVSYSID
     *is returned. Otherwise a status code determined by the ESME 
     *class is returned. The returned status code is determined 
     *on the number of free connections.
     *@param esmeName the name of the ESME wich the account is registred under.
     *@param accountUid is the name of the actual account to bind to.
     *@param password is the password to the account.
     *@return a staus code depending on the outcome of the bind request.
     */
    public int bindESMEAccount(String esmeName, String accountUid, String password){
        int command_status = SMPPSTATUS_RINVSYSID;
        if ( cache.containsKey(esmeName) ){
            ESME esme = (ESME)cache.get(esmeName);
            command_status =  esme.getConnection(accountUid, password);
        }
        return command_status;
    }
    
    /**
     *Unbinds a account thread from the ESME account
     *@param esmeName is the name of the ESME wich the account is registred under.
     *@param accountUid is the name of the actual account to unbind from.
     */
    public void freeESMEAccount(String esmeName, String accountUid){         
        if ( cache.containsKey(esmeName) ){        
            ESME esme = (ESME)cache.get(esmeName);
            esme.freeConnection(accountUid);
        }
    }
    
    /**
     *Gets a status code to include in the SMS PDU response from a SMS PDU request.
     *@param esmeName is the name of the ESME wich the account is registred under.
     *@param accountUid is the name of the actual account that sent the SMS PDU request.
     *@retrun a statuscode on the SMS PDU request
     */
    public int getSubmitPDUResponseCode(String esmeName, String accountUid){
        int command_status = SMPPSTATUS_ROK;
        if ( cache.containsKey(esmeName) ){   
            ESME esme = (ESME)cache.get(esmeName);
            command_status = esme.getSubmitPDUResponseCode();
        }        
        return command_status;
    }
    
    /**
     *Gets a status code to include in the SMS PDU response from a SMS PDU request.
     *@param esmeName is the name of the ESME wich the account is registred under.
     *@param accountUid is the name of the actual account that sent the SMS PDU request.
     *@retrun a statuscode on the SMS PDU request
     */
    public int getEnquireLinkResponseCode(String esmeName, String accountUid){
        int command_status = SMPPSTATUS_ROK;
        if ( cache.containsKey(esmeName) ){   
            ESME esme = (ESME)cache.get(esmeName);
            command_status = esme.getEnquireLinkResponseCode();
        }        
        return command_status;
    }
    
    /**
     *Gets a status code to include in the SMS PDU response from a SMS PDU request.
     *@param esmeName is the name of the ESME wich the account is registred under.
     *@param accountUid is the name of the actual account that sent the SMS PDU request.
     *@retrun a statuscode on the SMS PDU request
     */
    public int getCancelResponseCode(String esmeName, String accountUid){
        int command_status = SMPPSTATUS_ROK;
        if ( cache.containsKey(esmeName) ){   
            ESME esme = (ESME)cache.get(esmeName);
            command_status = esme.getCancelResponseCode();
        }        
        return command_status;
    }
    
    public void setResultCode(String esmeName, int bc, boolean bonce, int sc, boolean sonce, int ec, boolean eonce, int cc, boolean conce) {
        ESME esme = (ESME)cache.get(esmeName);
        if (esme != null) {
            esme.setResultCode(bc, bonce, sc, sonce, ec, eonce, cc, conce);
        }
    }

    /**
     *Cache a SMS PDU request
     *@param esmeName is the name of the ESME that should cache the SMS PDU request.
     *@param sms is the SMS PDU request to cache.
     */    
    public void logSMSRequest(String esmeName, String accountName, SMSPdu sms){                
        if ( cache.containsKey(esmeName) ){                     
            ESME esme = (ESME)cache.get(esmeName);            
            esme.addSMS(accountName, sms);            
            log.info("E: " + esmeName +
                     " A: " + accountName +
                     " A Tot: " + esme.getReceivedSMSRequests(accountName) + 
                     " E tot: " + esme.getReceivedSMSRequests());                                          
        }
    }
    
    /**
     *Returns a String array of all registred accounts under a ESME.
     *@param esmeName gets all registred accounts under a specific ESME.
     *@return a String arrya of all accounts registred under a ESME
     */
    public String[] getRegistredAccounts(String esmeName){
        String[] accounts = null;
        if ( cache.containsKey(esmeName) ){                
            accounts = ((ESME)cache.get(esmeName)).getAccountNames();                    
        }           
        return accounts;
    }
    
     
    /*Returns the password for an account.
     *@return the password for the requested account.
     **/
    public String getAccountPassword(String esmeName, String accountName){
        String pwd = "";
        if ( cache.containsKey(esmeName) )  
            pwd = ((ESME)cache.get(esmeName)).getAccountPassword(accountName);      
        return pwd;        
    }
    
    /**
     **Returns a String array of all registred ESME names.
     *@return a String array of all registred ESME names.
     */
    public String[] getRegistredESMEs(){
        Enumeration names = cache.keys();
        String [] nameId = new String[cache.size()];
        for( int i = 0; i < cache.size(); i++ ){            
            nameId[i] = (String)names.nextElement();
        }
        return nameId;
    }
                
    /**
     *Gets the number of registred accounts for a ESME
     *@param esmeName the name of the ESME
     *@return numbe of accounts
     */
    public int getNumberOfAccounts(String esmeName){         
        int num = -1;
        if ( cache.containsKey(esmeName) ){            
            ESME esme = (ESME)cache.get(esmeName);            
            num = esme.getNumberOfAccounts();
        }
        return num;
    }
    
    /**
     *Gets the number of received SMS PDU requests for a ESME
     *@param esmeName the name of the ESME
     *@return number received SMS PDU request
     */
    public int getRate(String esmeName){         
        int num = 0;
        if ( cache.containsKey(esmeName) ){            
            ESME esme = (ESME)cache.get(esmeName);            
            num = esme.getRate();
        }
        return num;
    }
         
    /**
     *Gets the number of received SMS PDU requests for a ESME
     *@param esmeName the name of the ESME
     *@return number received SMS PDU request
     */
    public int getReceivedSMSRequests(String esmeName){         
        int num = 0;
        if ( cache.containsKey(esmeName) ){            
            ESME esme = (ESME)cache.get(esmeName);            
            num = esme.getReceivedSMSRequests();
        }
        return num;
    }
         
    /**
     *Gets the number of receiced SMS PDU requests for a specific account
     *@param accountName the account to get information from.
     **/
    public int getReceivedSMSRequests(String esmeName, String accountName){
        int num = 0;
        if ( cache.containsKey(esmeName) ){
            num = ((ESME)cache.get(esmeName)).getReceivedSMSRequests(accountName);
        }
        return num;
    }
    
        /**
     *Gets the number of receiced Mwi Off PDU requests for a specific ESME
     *@param esmeName the ESME to get the information from.
     **/
    public int getReceivedMwiOff(String esmeName){
        int num = 0;
        if ( cache.containsKey(esmeName) ){
            num = ((ESME)cache.get(esmeName)).getReceivedMwiOff();
        }
        return num;
    }
                
    /**
     *Gets the number of receiced Mwi PDU requests for a specific ESME
     *@param esmeName the ESME to get the information from.
     **/
    public int getReceivedMwiOn(String esmeName){
        int num = 0;
        if ( cache.containsKey(esmeName) ){
            num = ((ESME)cache.get(esmeName)).getReceivedMwiOn();
        }
        return num;
    }
    
    /**
     *Gets the number of receiced null SMS PDU requests for a specific ESME
     *@param esmeName the ESME to get the information from.
     **/
    public int getReceivedSmsNull(String esmeName){
        int num = 0;
        if ( cache.containsKey(esmeName) ){
            num = ((ESME)cache.get(esmeName)).getReceivedSmsNull();
        }
        return num;
    }
    
    /**
     *Gets the number of receiced cancel SMS PDU requests for a specific ESME
     *@param esmeName the ESME to get the information from.
     **/
    public int getReceivedCancel(String esmeName){
        int num = 0;
        if ( cache.containsKey(esmeName) ){
            num = ((ESME)cache.get(esmeName)).getReceivedCancel();
        }
        return num;
    }
    
            /**
     *Gets the number of receiced Mwi Off PDU requests for a specific ESME
     *@param esmeName the ESME to get the information from.
     **/
    public int getReceivedSms(String esmeName){
        int num = 0;
        if ( cache.containsKey(esmeName) ){
            num = ((ESME)cache.get(esmeName)).getReceivedSMS();
        }
        return num;
    }
    
    /**
     *Gets the loglevel for a ESME
     *@param esmeName the name of the ESME
     *@return the loglevel
     */
    public String getLoglevel(String esmeName){         
        String level = "";
        if ( cache.containsKey(esmeName) ){            
            ESME esme = (ESME)cache.get(esmeName);            
            level = esme.getLogLevel();
        }
        return level;
    }
         
    /**
     *Sets the loglevel for a ESME
     *@param esmeName the name of the ESME to update
     *@param level the new loglevel for a ESME
     */
    public void setLogLevel(String esmeName, String level){               
        if ( cache.containsKey(esmeName) && 
            !getLoglevel(esmeName).equalsIgnoreCase(level) &&
            level.length() != 0){            
            ESME esme = (ESME)cache.get(esmeName);            
            esme.setLogLevel(level);            
            updateConfig.changeEsmeLogSettings(esmeName, "level", level);
        }
    }
         
    /**
     *Gets the size of the SMS PDU cache
     *@param esmeName the name of the ESME
     *@return size of SMS PDU cache 
     */
    public int getLogSize(String esmeName){         
        int size = -1;
        if ( cache.containsKey(esmeName) ){            
            ESME esme = (ESME)cache.get(esmeName);            
            size = esme.getLogSize();            
        }
        return size;
    }
    
    /**
     *Sets the size of the SMS PDU cache     
     *@param esmeName the name of the ESME
     *@param size the new size of the SMS PDU cache. If ESME does not exist
     *-1 is returned
     */
    public void setLogSize(String esmeName, int size){               
        if ( cache.containsKey(esmeName) && getLogSize(esmeName) != size ){            
            ESME esme = (ESME)cache.get(esmeName);            
            esme.setLogSize(size);
            updateConfig.changeEsmeLogSettings(esmeName, "size", Integer.toString(size));
        }
    }
    
    /**
     *Gets the the number of account threads bound to a ESME account
     *@param esmeName the ESME name that holds the requested account.
     *@param accountName the account name.
     *@return number of account threads bound to the account. If the ESME does not exist -1 is returned.
     */
     public int getUsedConnections(String esmeName, String accountName){         
        int size = -1;
        if ( cache.containsKey(esmeName) ){            
            ESME esme = (ESME)cache.get(esmeName);            
            size = esme.getUsedConnection(accountName);
        }
        return size;
    }          
     
     /**
      *Gets max number of account threads that can be bound to a ESME account
      *@param esmeName the name of the ESME.
      *@param accountName the account name.
      *@return max number of accounts threads that could be bound to a account.
      */
     public int getMaxConnections(String esmeName, String accountName){         
        int size = -1;
        if ( cache.containsKey(esmeName) ){            
            ESME esme = (ESME)cache.get(esmeName);            
            size = esme.getMaxConnections(accountName);
        }
        return size;
    }
     
     /**
      *Sets max number of account threads that could be bount to a ESME account
      *@param esmeName the name of the ESME.
      *@param accountName the account name.
      *@param size new max size
      */
     public void setMaxConnections(String esmeName, String accountName, int size){                         
        if ( cache.containsKey(esmeName) && getMaxConnections(esmeName, accountName) != size ){            
            ESME esme = (ESME)cache.get(esmeName);   
            esme.setMaxConnections(accountName, size);
            updateConfig.changeAccountSettings(esmeName, accountName, "connections", Integer.toString(size));
        }
     }
              
     public void changeAccountUid(String esmeName, String accountName, String accountUid){         
         if ( cache.containsKey(esmeName) ){            
            ESME esme = (ESME)cache.get(esmeName);   
            esme.setAccountUid(accountName, accountUid);
            updateConfig.changeAccountSettings(esmeName, accountName, "uid", accountUid);
        }
     }
      
     public void changeAccountPwd(String esmeName, String accountName, String accountPwd){         
         if ( cache.containsKey(esmeName) ){            
            ESME esme = (ESME)cache.get(esmeName);   
            esme.setAccountPassword(accountName, accountPwd);
            updateConfig.changeAccountSettings(esmeName, accountName, "password", accountPwd);
        }
     }
        
     /**
      * Gets the 10 latest entries in the SMSPdu cache 
      * as a String[] array where each entry is in HTML format.
      *@param esmeName the name of the ESME.
      * @return the SMS PDU cache as a string array.
      */
     public String[] getCachedSMSAsHTML(String esmeName){
         String[] list = null;
         if ( cache.containsKey(esmeName) ){
             ESME esme = (ESME)cache.get(esmeName);
             list = esme.getCachedSMSAsHTML();
         }
         return list;
     }
    
     /**
      * Gets the 10 latest entries in the SMSPdu cache 
      * as a String[] array where each entry is in HTML format.
      *@param esmeName the name of the ESME.
      * @return the SMS PDU cache as a string array.
      */
     public String getCacheSummaryHTML(String esmeName){
         if ( cache.containsKey(esmeName) ){
             ESME esme = (ESME)cache.get(esmeName);
             return esme.getCacheSummaryHTML();
         }
         return "";
     }
    
    /**
      * Gets the SMSPdu cache as a String[] array
      * where each entry is in a plain text format.
      * @return the SMS PDU cache as a string array.
      */
     public String [] getCachedSMSAsPlainText(String esmeName){
         String[] list = null;
         if ( cache.containsKey(esmeName) ){
             ESME esme = (ESME)cache.get(esmeName);
             list = esme.getCachedSMSAsPlainText();
         }
         return list;
     }
     
     /**
      *Adds a new ESME and a new accout to the ESME cache and
      *writes the changes to disk.
      *If the ESME exist the ESME will not be added to cache.
      *@param esmeName the name of the ESME to be added
      *@param loglev loglevel for the ESME
      *@param cacheSize the size of the SMS PDU cache
      *@param accountName the name of the new account
      *@param password password for the account
      *@param connections number of account threads that could be bound to the account.
      */
     public void addEsmeAndAccount(String esmeName, 
                                   String loglevel, 
                                   String cacheSize, 
                                   String accountName, 
                                   String password, 
                                   String connections){
         if ( !cache.containsKey(esmeName) ){
             cache.put(esmeName, new ESME(esmeName, loglevel, Integer.parseInt(cacheSize), accountName, password, connections));  
             updateConfig.addEsmeAndAccount(esmeName, loglevel, cacheSize, accountName, password, connections);
         }
     }     
     
     /**
      *Removes a registred ESME and its accounts from the cache and disk.
      *@param esmeName the ESME to remove.
      */
     public void removeEsme(String esmeName){
         if ( cache.containsKey(esmeName) ){
             cache.remove(esmeName);  
             updateConfig.removeEsme(esmeName);
         }
     }  
     
     /**
      *Adds a new account to an existing ESME. If the ESME does not 
      *exist nothing will be added to the cache.
      *@param esmeName the name of the ESME
      *@param accountName the account name to add.
      *@param password for the account
      *@param connections max number of account threads bound to an account
      */
     public void addAccount(String esmeName, 
                            String accountName, 
                            String password, 
                            String connections){
         if ( cache.containsKey(esmeName) ){
             ((ESME)cache.get(esmeName)).addAccount(accountName, password, connections); 
             updateConfig.addAccount(esmeName, accountName, password, connections);
         }
     }
             
     /**
      *Removes a account registration from the ESME.
      *@param esmeName the name of the ESME to update.
      *@param accountName the account to remove.
      */
     public void removeAccount(String esmeName, String accountName){
         if ( cache.containsKey(esmeName) ){
             ((ESME)cache.get(esmeName)).removeAccount(accountName);
             updateConfig.removeAccount(esmeName, accountName);
         }
     }  
     
     /**
      *Gets the time a ESME was started.
      *@return the start time in millisec. of the ESME.
      */
     public long getStartTime(String esmeName){                
         if ( cache.containsKey(esmeName) ){
             return ((ESME)cache.get(esmeName)).getStartTime();                 
         }
         return new Date().getTime();
    }
     
     /**
      *Resets the SMS PDU cache for an ESME
      *@param esmeName the name of the ESME to reset.
      */
     public void resetSMSList(String esmeName){
         if ( cache.containsKey(esmeName) ){
             ((ESME)cache.get(esmeName)).restetSMSList();                 
         }
     }
     
         
     /**                         
      * Sets the number of received SMS PDU requests for a specific account to 0. 
     *@param accountName the account to reset.
     */
    public void resetAccountSMSList(String esmeName, String accountName){
        if ( cache.containsKey(esmeName) ) {
            ((ESME)cache.get(esmeName)).restetAccountSMSList(accountName);
        }
    }
}
