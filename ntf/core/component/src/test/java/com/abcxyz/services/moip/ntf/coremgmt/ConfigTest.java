package com.abcxyz.services.moip.ntf.coremgmt;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.mobeon.ntf.Config;

public class ConfigTest
{

    @BeforeClass
    static public void startup() {
        BasicConfigurator.configure();
    }


    @AfterClass
    static public void tearDown() {
    }

    @Test
    public void testDefaultProperties() throws ConfigurationDataException {
    	Config.loadCfg();

        assertTrue (Config.getNotifRetrySchema().equalsIgnoreCase("5 5 60 240 CONTINUE"));
        assertTrue (Config.getNotifExpireTimeInMin() == 10080);

    }
    @Test
    public void testLoadProperties() {
        String curDir = System.getProperty("user.dir");
        if (curDir.endsWith("ntf")) {
            curDir +=  "/test/junit/com/abcxyz/services/moip/ntf/coremgmt/MyConfig.cfg";
        } else if (curDir.endsWith("llv")) {
            curDir +=  "ntf/test/junit/com/abcxyz/services/moip/ntf/coremgmt/MyConfig.cfg";
        }

        /*if (!curDir.endsWith("coremgmt")) {
            curDir +=  "/test/junit/com/abcxyz/services/moip/ntf/coremgmt/MyConfig.cfg";
        }*/

        Config.updateCfg(curDir);

        assertTrue (Config.getNotifRetrySchema().equalsIgnoreCase("5:try=3 STOP"));
        assertTrue (Config.getNotifExpireTimeInMin() == 180);
        assertTrue (Config.getServiceListenerCorePoolSize() == 2);
        assertTrue (Config.getServiceListenerMaxPoolSize() == 5);
    }
}
