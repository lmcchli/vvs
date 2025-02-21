package com.mobeon.smsc.interfaces;

/* This interface is implemented by the ESMEHandler*/
public interface ConfigurationManagement {
    public void addEsmeAndAccount(String esmeName, String loglevel, String cacheSize, String accountName, String password, String connections);
    public void addAccount(String esmeName, String accountName, String password, String connections);
    public void changeEsmeLogSettings(String esmeName, String nodeToAdd, String content);
    public void changeAccountSettings(String esmeName, String accountName, String nodeToAdd, String content);  
    public void removeAccount(String esmeName, String accountName);
    public void removeEsme(String esme);
}
