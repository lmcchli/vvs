package com.mobeon.smsc.interfaces;

import com.mobeon.smsc.smpp.util.SMSPdu;

/* This interface is implemented by the ESMEHandler*/
public interface TrafficCentral {
            
    public void addEsmeAndAccount(String esme_name, String loglevel, String cacheSize, String account_name, String password, String connections);
    public void addAccount(String esme_name, String account_name, String password, String connections);
    public int bindESMEAccount(String esme, String accountUid, String password);   
    public void freeESMEAccount(String esmeName, String accountUid);        
    public int getEnquireLinkResponseCode(String esme, String accountUid);
    public int getCancelResponseCode(String esme, String accountUid);
    public int getSubmitPDUResponseCode(String esme, String accountUid);
    public String[] getRegistredESMEs();
    public String[] getRegistredAccounts(String esme_name);
    public int getRate(String esme_name);     
    public int getReceivedSMSRequests(String esme_name);     
    public int getReceivedSMSRequests(String esme_name, String account);
    public String getLoglevel(String esme_name);        
    public int getLogSize(String esme_name);
    public int getNumberOfAccounts(String esme_name);
    public int getMaxConnections(String esme_name, String account_name);
    public int getUsedConnections(String esme_name, String account_name);
    public String[] getCachedSMSAsPlainText(String esmeName);
    public String[] getCachedSMSAsHTML(String esmeName);      
    public String getCacheSummaryHTML(String esmeName);      
    public long getStartTime(String esme_name);
    public String getAccountPassword(String esmeName, String account_name);
    public int getReceivedMwiOn(String esmeName);
    public int getReceivedMwiOff(String esmeName);
    public int getReceivedSmsNull(String esmeName);
    public int getReceivedCancel(String esmeName);
    public int getReceivedSms(String esmeName);
    public void logSMSRequest(String esme_name, String account_name, SMSPdu sms);        
    public void resetSMSList(String esme_name);
    public void resetAccountSMSList(String esme_name, String account_name);
    public void removeAccount(String esmeName, String accountName);
    public void removeEsme(String esme);
    public void setLogSize(String esme_name, int size);        
    public void setLogLevel(String esme_name, String level);    
    public void setMaxConnections(String esme_name, String account_name, int size);        
    public void changeAccountUid(String esme_name, String account_name, String account_uid);    
    public void changeAccountPwd(String esme_name, String account_name, String account_pwd);
    public void setResultCode(String esme_name, int bc, boolean bonce, int sc, boolean sonce, int ec, boolean eonce, int cc, boolean conce);
}
