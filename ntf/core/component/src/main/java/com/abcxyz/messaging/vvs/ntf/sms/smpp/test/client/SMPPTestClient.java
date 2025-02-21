package com.abcxyz.messaging.vvs.ntf.sms.smpp.test.client;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.util.SystemPropertyHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.generic.NotifierHandler;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.NtfCmnManager;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.logging.LogAgentFactory;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.util.Logger;


public class SMPPTestClient{
    private Logger logger = null;
    private SMSOut smsOut = null;
    private NotifierHandler notifierHandler = null;
    private static boolean isGuiEnabled = true;
    private DataSmTester shortMessageTester = null;
    private CharsetTester charsetTester = null;
    private BindingTester bindingTester = null;
    private SMPPTestClientGuiBuilder smppTestClientGuiBuilder = null;
    static{
        SystemPropertyHandler.setProperty("abcxyz.services.messaging.productPrefix", "moip");
        SystemPropertyHandler.setProperty("management_config_backup_root", "");
        SystemPropertyHandler.setProperty("ObjectId", "20");
        SystemPropertyHandler.setProperty("BaseOid", ".1.3.6.1.4.1.193.91.4");
        SystemPropertyHandler.setProperty("RootOid", ".1.3.6.1.4.1.193.91.");
        SystemPropertyHandler.setProperty("mms_root", "/opt/moip");
    }
    
    SMPPTestClient(){ 
        String log4jPath = Config.getNtfHome() + "/cfg/log4j.xml";
        if (new File(log4jPath).exists()) {
            LogAgentFactory.configureAndWatch(log4jPath);
        }
        this.logger = Logger.getLogger(); 
        try{
            Config.loadCfg();
        }catch(ConfigurationDataException e){
           this.logger.logMessage(e.getMessage(),0x03);
        }
        this.notifierHandler = NotifierHandler.get();
        this.smsOut = SMSOut.get();
        this.shortMessageTester = new DataSmTester();
        this.charsetTester = new CharsetTester();
        this.bindingTester = new BindingTester();
        this.charsetTester = new CharsetTester();
        this.smppTestClientGuiBuilder = new SMPPTestClientGuiBuilder(this.shortMessageTester, this.charsetTester, this.bindingTester);
        initializeBackendToUseCommonMessagingAccess();
        Collection<String> configFilenames = new LinkedList<String>();
        String configFilename = "/opt/moip/config/backend/backend.conf";
        configFilenames.add(configFilename);
        IConfiguration configuration;
        try {
            configuration = new ConfigurationImpl(null,configFilenames,false);
            CommonMessagingAccess.getInstance().setConfiguration(configuration);
            CommonMessagingAccess.getInstance().setServiceName(MoipMessageEntities.MESSAGE_SERVICE_NTF);
            CommonMessagingAccess.getInstance().init();
        } catch (ConfigurationException e) {
            logger.logMessage("NtfMain: loadBackendOam, exception: "+e);
            System.exit(0);
        }
        
        SMPPTestClientLogger.clearLogs();
    }
    
    private void initializeBackendToUseCommonMessagingAccess(){
        logger.logMessage("Intializing common messaging access",3);
        Collection<String> configFilenames = new LinkedList<String>();
        String configFilename = "/opt/moip/config/backend/backend.conf";
        configFilenames.add(configFilename);
        IConfiguration configuration;
        try {
            configuration = new ConfigurationImpl(null,configFilenames,false);
            CommonMessagingAccess.getInstance().setConfiguration(configuration);
            CommonMessagingAccess.getInstance().setServiceName(MoipMessageEntities.MESSAGE_SERVICE_NTF);
            CommonMessagingAccess.getInstance().init();
        } catch (ConfigurationException e) {
            logger.logMessage("NtfMain: loadBackendOam, exception: "+e);
            System.exit(0);
        }
    }
    
    public static void main(String[] args){
        SMPPTestClient smppTestClient = new SMPPTestClient();
        if(isGuiEnabled){
            smppTestClient.smppTestClientGuiBuilder.createGui();
        }else{
            smppTestClient.smsOut.connectSmsUnits();
            smppTestClient.shortMessageTester.runTests();
            smppTestClient.charsetTester.runTests();
        }
        while(true){
            try{
                Thread.sleep(30000);
            }catch(InterruptedException e){
                
            }
        }
    }
}
