/* **********************************************************************
 * Copyright (c) ABCXYZ 2009. All Rights Reserved.
 * Reproduction in whole or in part is prohibited without the
 * written consent of the copyright owner.
 *
 * ABCXYZ MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ABCXYZ SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * **********************************************************************/

package com.abcxyz.services.moip.ntf.coremgmt;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.impl.OamManagerDefaultImpl;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.IdGenerationException;
import com.abcxyz.messaging.mrd.operation.SendMessageReq;
import com.abcxyz.messaging.scheduler.SchedulerConfigMgr;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventGenerator;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;

/**
 */
public class NtfEventTest {

    @BeforeClass
    static public void setup() {
        BasicConfigurator.configure();
    }

    @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")    
    @Test
    public void testMsgId() throws IdGenerationException, ConfigurationDataException {
        final OAMManager mrdOam = new OamManagerDefaultImpl();
        mrdOam.getConfigManager().setParameter(SchedulerConfigMgr.SchedulerID, "22");
        CommonOamManager.getInstance().setMrdOam(mrdOam);
        NtfCmnManager.resetOamManager();

        final MSA omsa =
                MFSFactory.getMSA("calll-id1" + System.currentTimeMillis(),
                        true);
        final String omsgid =
                MFSFactory.getOmsgid("123456" + System.currentTimeMillis(),
                        "IM");
        final MSA rmsa =
                MFSFactory.getMSA("calll-id2" + System.currentTimeMillis(),
                        true);
        final String rmsgid =
                MFSFactory.getRmsgid("123456" + System.currentTimeMillis());

        final String id =
                "tn0/20060720-19h30/120_" + omsgid + "-" + rmsgid
                        + "-0.Notif;rcv=mas-rcpid-" + rmsa.toString() + ";snd="
                        + omsa.toString() + ";srv-type=slamdown";

        final NtfEvent event = NtfEventGenerator.generateEvent(id);

        assertTrue(event.getMessageId().endsWith("22"));
    }

    @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")    
    @Test
    public void testEventSetFromSchedulerEvent() {
        final String id =
                "tn0/20060720-19h30/120_omsgid-rmsgid-0.Notif;rcv=mas-rcpid-rmsa;snd=omsa;srv-type=slamdown";

        final NtfEvent event = NtfEventGenerator.generateEvent(id);

        final MessageInfo info = event.getMsgInfo();
        assertTrue(info.omsa.getId().equalsIgnoreCase("omsa"));
        assertTrue(info.rmsa.getId().equalsIgnoreCase("rmsa"));
        assertTrue(info.omsgid.equalsIgnoreCase("omsgid"));
        assertTrue(info.rmsgid.equalsIgnoreCase("rmsgid"));

        final Properties props = event.getEventProperties();
        assertTrue(props.getProperty("srv-type").equalsIgnoreCase("slamdown"));

        assertTrue(event.getEventServiceTypeKey().equalsIgnoreCase(NtfEventTypes.DEFAULT_NTF.getName()));
    }

    @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")    
    @Test
    public void testEventSetFromServiceScheduling() {
        final String id =
                "tn0/20060720-19h30/120_omsgidrmsgid-0.Notif;rcv=mas-rcpid-rmsa;snd=omsa;srv-type=slamdown;omsa=omsa;rmsa=rmsa;omsg=omsgid;rmsg=rmsgid";

        final NtfEvent event = NtfEventGenerator.generateEvent(id);

        final MessageInfo info = event.getMsgInfo();
        assertTrue(info.omsa.getId().equalsIgnoreCase("omsa"));
        assertTrue(info.rmsa.getId().equalsIgnoreCase("rmsa"));
        assertTrue(info.omsgid.equalsIgnoreCase("omsgid"));
        assertTrue(info.rmsgid.equalsIgnoreCase("rmsgid"));

        final Properties props = event.getPersistentProperties();
        System.out.println(props.getProperty("omsa"));
        System.out.println(props.getProperty("rmsa"));
        System.out.println(props.getProperty("omsg"));
        System.out.println(props.getProperty("rmsg"));

        assertTrue(props.getProperty("omsa").equalsIgnoreCase("omsa"));
        assertTrue(props.getProperty("rmsa").equalsIgnoreCase("rmsa"));
        assertTrue(props.getProperty("omsg").equalsIgnoreCase("omsgid"));
        assertTrue(props.getProperty("rmsg").equalsIgnoreCase("rmsgid"));

        assertTrue(event.getEventServiceTypeKey().equalsIgnoreCase(NtfEventTypes.DEFAULT_NTF.getName()));
    }

    @Test
    public void testEventSetFromMessage() {

        final MessageInfo msgInfo = new MessageInfo();
        msgInfo.omsa = new MSA("omsa");
        msgInfo.omsgid = "omsgid";
        msgInfo.rmsa = new MSA("rmsa");
        msgInfo.rmsgid = "rmsgid";

        final Properties props = new Properties();
        props.put("key1", "val1");
        props.put("key2", "val2");

        final String id = "myid";
        final NtfEvent event = new NtfEvent("key", msgInfo, props, id);
        final String uid = event.calculateEventUid("0");

        assertTrue(uid.equalsIgnoreCase("omsgid-rmsgid-0"));
        assertTrue(event.getReferenceId() != null);

        assertTrue(event.getEventServiceTypeKey().equalsIgnoreCase("key"));
    }

    @Test
    public void testEventFromSendMessageReq() {
        final SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "im";
        req.destRcptID.value = "tel:+123456";
        req.rMsa.value = "msid:637jd";
        req.rMsgID.value = "29dkjd";
        req.oMsa.value = "eid:1234";
        req.oMsgID.value = "dkdfdfd";
        req.eventType.value = "Deliv";
        req.eventID.value = "id";

        final HashMap<String, String> extra = new HashMap<String, String>();
        extra.put("srv-type", "foo");
        extra.put("BarFoo", "bar");

        req.extraValue = extra;
        req.eventID.value = "myid";

        // construct NTF event, the event is created based on message type
        final MessageInfo msgInfo =
                new MessageInfo(new MSA(req.oMsa.value),
                        new MSA(req.rMsa.value), req.oMsgID.value,
                        req.rMsgID.value);

        // convert to NTF event
        final NtfEvent event =
                NtfEventGenerator.generateEvent("me", msgInfo, req.extraValue,
                        req.eventID.value);

        final Properties props = event.getEventProperties();

        assertTrue(props.getProperty("type").equalsIgnoreCase("foo"));
        assertTrue(props.getProperty("BarFoo").equalsIgnoreCase("bar"));

        assertTrue(event.getReferenceId().equalsIgnoreCase("myid"));

        assertTrue(event.getEventServiceTypeKey().equalsIgnoreCase("me"));
    }

}