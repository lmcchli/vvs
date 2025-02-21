package com.mobeon.common.cmnaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.abcxyz.messaging.common.message.CodingFailureException;
import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.MessageStreamingResult;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.MfsConfiguration;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.mobeon.common.cmnaccess.oam.CommonOamManagerTest;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;

/**
 * This is not an automatic test case.
 *
 * This test case will invoke remote MRD after storing a message, to trigger NTF's delivery.
 *
 */
public class MasStubTest
{
    static private CommonMessagingAccess commonMessagingAccess =null;
    static private String strDirectoy = "C:\\opt\\moip\\mfs";

    @BeforeClass
    static public void setUp() throws Exception {
        System.setProperty("abcxyz.mfs.userdir.create", "true");
        System.setProperty("abcxyz.mrd.noAYL", "true");
        CommonMessagingAccess.setMcd(new McdStub());
        //System.setProperty("NtfMrdServiceHost", "172.30.164.100");
        //System.setProperty("abcxyz.messaging.mrd.secResponse", "false");

        CommonOamManagerTest.initOam();

        //deleteDirectory(new File(strDirectoy + "\\internal"));
        //deleteDirectory(new File(strDirectoy + "\\external"));
        //System.setProperty("abcxyz.messaging.mrd.secResponse", "false");

        commonMessagingAccess = CommonMessagingAccess.getInstance();

        Collection<String> configFilenames = new LinkedList<String>();

        String configFilename = "c:/ntf/cfg/backend.xml";
        File backendXml = new File(configFilename);


        configFilenames.add(configFilename);
        IConfiguration configuration;
        try {
            configuration = new ConfigurationImpl(null,configFilenames,false);
            CommonMessagingAccess.getInstance().setConfiguration(configuration);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        ConfigManager mfsConfig = MfsConfiguration.getInstance();
        (new File(strDirectoy)).mkdir();
        mfsConfig.setParameter(MfsConfiguration.MfsRootPath, strDirectoy);
        commonMessagingAccess.reInitializeMfs(mfsConfig);
    }
    @AfterClass
    static public void tearDown() throws Exception {
        System.setProperty("abcxyz.messaging.scheduler.memory", "false");
    }

    @Test
    public void depositeMessage() throws Exception {

        //System.setProperty("NTF_HOST", "172.30.164.100");
        //System.setProperty("NTF_HOST", "localhost");
        //System.setProperty("NTF_PORT", "10500");

        Container1 c1 = new Container1();

        c1.setFrom("4503457900");

        c1.setTo("491721092605");

        c1.setMsgClass("voice");

        c1.setSubject("hello");

        Container2 c2 = new Container2();
        MsgBodyPart[] c3Parts = new MsgBodyPart[1];
        c3Parts[0] =  new MsgBodyPart();
        StateAttributes attributes = new StateAttributes();

        //what to set in state file
        attributes.setAttribute("messagestate", "new");

        int result1 = commonMessagingAccess.storeMessage(c1, c2, c3Parts, attributes);
        assertEquals(MessageStreamingResult.streamingOK, result1);
        Thread.sleep(Integer.MAX_VALUE);


    }


}
