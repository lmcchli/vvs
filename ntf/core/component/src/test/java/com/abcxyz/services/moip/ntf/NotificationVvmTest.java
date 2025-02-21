package com.abcxyz.services.moip.ntf;

/**
 * 
 * 
 * @deprecated since MiO 3.2 after VVM refactor
 * @author 
 */
public class NotificationVvmTest {
/*
    private int responseReceived_OK = 0;
    private static NtfMain ntf;    

    private final String from = "5143457901";
    private final String to = "5143457900";
    private static CommonMessagingAccess commonMessagingAccess = null;
    static private String strDirectoy = "C:\\opt\\moip\\mfs";
    MessageInfo msgInfo = null;
    private static SMSClientStub smsClientStub = null;

    // FIXME This test class needs reviewing.  Does not initialize properly on Linux
    
//    @BeforeClass
    static public void startup() throws Exception {
        String userDir = System.getProperty("user.dir");
        System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
        System.setProperty("ntfHome", userDir + "/test/junit/" );

        // Setup subscriber MCD profile
        McdStub directoryAccess = new McdStub();
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_FILTER, "Name=sms;Active=yes;Notify=yes;ValidTime=Always;Priority=1;CriteriaMsgHighPriority=no;MsgDepositType=Voice;NotifType=SMS,EML;NotifContentSMS=Subject;NotifContentEML=Subject;NotifContentMWI=false;CriteriaTelephoneFrom=");
        directoryAccess.addCosProfileAttribute(DAConstants.ATTR_SERVICES, ProvisioningConstants.SERVICES_VVM + "," + "msgtype_voice" );
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_VVM_ACTIVATED, "yes");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_COS_IDENTITY, "cos:1");
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_DELIVERY_PROFILE, "1111111111;SMS;M");
        directoryAccess.addSubcriberProfileIdentity(URI.create("msid:111112462ffff"));
        directoryAccess.addSubcriberProfileIdentity(URI.create("tel:+5143457900"));
        directoryAccess.addSubscriberProfileAttribute(DAConstants.ATTR_MSG_RETENTION_NEW_VOICE, "10");
        
        CommonTestingSetup.setup();
        System.setProperty("-Dabcxyz.mfs.userdir.create", "true");
        
        CommonMessagingAccessTest.setMcdStub(directoryAccess);
        CommonMessagingAccessTest.setUp();
        
        BasicConfigurator.configure();
        
        smsClientStub = new SMSClientStub();
        SMSOut.setSmsClient(smsClientStub);
        
        // Start NTF
        ntf = new NtfMain();
    	Config.updateCfg();
    }

//    @AfterClass
    static public void tearDown() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CommonMessagingAccessTest.stop();
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testVvm_Deposit() throws Exception {

        // Wait for NTF to boot (30 seconds)
        Thread.sleep(30000);

        ntf.getEventHandler().resetNumberOfNotification();
        smsClientStub.reset(); 
        responseReceived_OK = 0;
        String subscriberNumber = "5143457900";

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Create MFS message (simulating MAS deposit) an inject it to NTF
        storeMfsMessage();

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        SendMessageReq req = getSendMessageReqForMfs();
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(5000);

        // Validating the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(1, responseReceived_OK);

        // Validating the SMSClientStub received only 1 request (the SMS-Info)
        int numberOfRequests = smsClientStub.getNumberOfRequests();
        assertTrue(numberOfRequests == 1);

        // Validating the SMS content request vs template
        String expectedResult = "//VVM:SYNC:ev=NM;id=3446456;c=1;t=v;s=01234567898;dt=02/08/2008 12:53 +0200;l=30";
        System.out.println("expected result:" + expectedResult + " SMS: " + smsClientStub.getExpectedSmsMessage());
        // TODO: assert each token of the SMS message against the values of the MFS message created initially
        // TODO: assertTrue(expectedResult.equalsIgnoreCase(smsClientStub.getExpectedSmsMessage()));
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testVvm_Greeting() throws Exception {
        ntf.getEventHandler().resetNumberOfNotification();
        smsClientStub.reset(); 
        responseReceived_OK = 0;
        String subscriberNumber = "5143457900";

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        SendMessageReq req = getSendMessageReq(subscriberNumber, MoipMessageEntities.SERVICE_TYPE_GREETING_UPDATED);
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(5000);

        // Validating the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(1, responseReceived_OK);

        // Validating the SMSClientStub received only 1 request (the SMS-Info)
        int numberOfRequests = smsClientStub.getNumberOfRequests();
        assertTrue(numberOfRequests == 1);

        // Validating the SMS content request vs template
        String expectedResult = "//VVM:SYNC:ev=GU;c=-1;t=v";
        assertTrue(expectedResult.equalsIgnoreCase(smsClientStub.getExpectedSmsMessage()));
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testVvm_Expiry() throws Exception {
        ntf.getEventHandler().resetNumberOfNotification();
        smsClientStub.reset(); 
        responseReceived_OK = 0;
        String subscriberNumber = "5143457900";

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        SendMessageReq req = getSendMessageReq(subscriberNumber, MoipMessageEntities.SERVICE_TYPE_MSG_EXPIRY);
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(5000);

        // Validating the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(1, responseReceived_OK);

        // Validating the SMSClientStub received only 1 request (the SMS-Info)
        int numberOfRequests = smsClientStub.getNumberOfRequests();
        assertTrue(numberOfRequests == 1);

        // Validating the SMS content request vs template
        String expectedResult = "//VVM:SYNC:ev=MBU;c=-1;t=v";
        assertTrue(expectedResult.equalsIgnoreCase(smsClientStub.getExpectedSmsMessage()));
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testVvm_Logout() throws Exception {
        ntf.getEventHandler().resetNumberOfNotification();
        smsClientStub.reset(); 
        responseReceived_OK = 0;
        String subscriberNumber = "5143457900";

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        SendMessageReq req = getSendMessageReq(subscriberNumber, MoipMessageEntities.SERVICE_TYPE_LOGOUT_SUBSCRIBER);
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(5000);

        // Validating the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(1, responseReceived_OK);

        // Validating the SMSClientStub received only 1 request (the SMS-Info)
        int numberOfRequests = smsClientStub.getNumberOfRequests();
        assertTrue(numberOfRequests == 1);

        // Validating the SMS content request vs template
        String expectedResult = "//VVM:SYNC:ev=MBU;c=-1;t=v";
        assertTrue(expectedResult.equalsIgnoreCase(smsClientStub.getExpectedSmsMessage()));
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testVvm_SmsRetry_EventInCache() throws Exception {
        ntf.getEventHandler().resetNumberOfNotification();
        smsClientStub.reset(); 
        responseReceived_OK = 0;
        String subscriberNumber = "5143457900";

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_RETRY);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        SendMessageReq req = getSendMessageReq(subscriberNumber, MoipMessageEntities.SERVICE_TYPE_LOGOUT_SUBSCRIBER);
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        // Wait for the NotificationHandler to retrieve the event from the queue
        Thread.sleep(5000);

        // Validating the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(1, responseReceived_OK);

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

//        VvmEventKey vvmEventKey = new VvmEventKey(subscriberNumber, VvmEventTypes.VVM_LOGOUT);
//        VvmEventKey retrievedVvmEventKey = VvmHandler.get().retrieveFromCache(vvmEventKey).getKey();
//        assertTrue(retrievedVvmEventKey.equals(vvmEventKey));

        // Sleep as long as the configured value (default config is 1 minute)
        String vvmSmsUnitRetrySchema = Config.getVvmSmsUnitRetrySchema();
        int intervalInMinute = Integer.valueOf(vvmSmsUnitRetrySchema.substring(0, vvmSmsUnitRetrySchema.indexOf(":")));
        System.out.println("Wait for Scheduler to retry in " + intervalInMinute + " minute(s)...");
        Thread.sleep(((intervalInMinute) * 60 * 1000) + (10 * 1000));

        // Validating the SMSClientStub received only 1 request (the SMS-Info)
        int numberOfRequests = smsClientStub.getNumberOfRequests();
        assertTrue(numberOfRequests == 1);

        // Validating the SMS content request vs template
        String expectedResult = "//VVM:SYNC:ev=MBU;c=-1;t=v";
        assertTrue(expectedResult.equalsIgnoreCase(smsClientStub.getExpectedSmsMessage()));
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testVvm_SmsRetry_EventNotInCache() throws Exception {
        ntf.getEventHandler().resetNumberOfNotification();
        smsClientStub.reset(); 
        responseReceived_OK = 0;
        String subscriberNumber = "5143457900";

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_RETRY);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        SendMessageReq req = getSendMessageReq(subscriberNumber, MoipMessageEntities.SERVICE_TYPE_MSG_EXPIRY);
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        // Wait for the NotificationHandler to retrieve the event from the queue
        Thread.sleep(5000);

        // Validating the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(1, responseReceived_OK);

//        // Remove the event from the VvmHandler's cache (simulating NTF rebooting)
//        VvmEventKey vvmEventKey = new VvmEventKey(subscriberNumber, VvmEventTypes.VVM_EXPIRY);
//        VvmHandler.get().removeEventFromCache(vvmEventKey);

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_OK);

        // Sleep as long as the configured value (default config is 1 minute)
        String vvmSmsUnitRetrySchema = Config.getVvmSmsUnitRetrySchema();
        int intervalInMinute = Integer.valueOf(vvmSmsUnitRetrySchema.substring(0, vvmSmsUnitRetrySchema.indexOf(":")));
        System.out.println("Wait for Scheduler to retry in " + intervalInMinute + " minute(s)...");
        Thread.sleep(((intervalInMinute) * 60 * 1000) + (10 * 1000));
        
        // Validating the SMSClientStub received only 1 request (the SMS-Info)
        int numberOfRequests = smsClientStub.getNumberOfRequests();
        assertTrue(numberOfRequests == 1);

        // Validating the SMS content request vs template
        String expectedResult = "//VVM:SYNC:ev=MBU;c=-1;t=v";
        assertTrue(expectedResult.equalsIgnoreCase(smsClientStub.getExpectedSmsMessage()));
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testVvm_SmsFailed() throws Exception {
        ntf.getEventHandler().resetNumberOfNotification();
        smsClientStub.reset(); 
        responseReceived_OK = 0;
        String subscriberNumber = "5143457900";

        // Set the SMS-C response stub to the desired result
        smsClientStub.setSmsUnitResponse(Constants.FEEDBACK_STATUS_FAILED);

        // Inject the NtfEvent into NTF (simulating MRD calling NTF)
        MyListener listener = new MyListener();
        NtfEventHandlerRegistry.registerDefaultListener(listener);
        NtfEventHandlerRegistry.registerDefaultEventReceiver(ntf.getEventHandler());
        SendMessageReq req = getSendMessageReq(subscriberNumber, MoipMessageEntities.SERVICE_TYPE_LOGOUT_SUBSCRIBER);
        NtfMessageService service = new NtfMessageService();
        SendMessageResp resp = service.sendMessage(req);

        Thread.sleep(5000);

        // Validating the response
        assertTrue(resp.reason.value.equalsIgnoreCase(Reason.HAND_OFF_2000));
        assertEquals(service.getNumOfSendMessageReceived(), 1);
        assertEquals(1, responseReceived_OK);

        // Validating the SMSClientStub received only 1 request (the SMS-Info)
        int numberOfRequests = smsClientStub.getNumberOfRequests();
        assertTrue(numberOfRequests == 1);

        // Validating the SMS content request vs template
        String expectedResult = "//VVM:SYNC:ev=MBU;c=-1;t=v";
        assertTrue(expectedResult.equalsIgnoreCase(smsClientStub.getExpectedSmsMessage()));
    }

    class MyListener implements EventSentListener {

        @Override
        public void sendStatus(NtfEvent event, SendStatus status)
        {
            if (status.equals(EventSentListener.SendStatus.OK)) {
                responseReceived_OK++;
            } else {
                System.out.println("VVMMyListener: " + status + event.getEventServiceTypeKey() + event.getEventTypeKey());
            }

            NtfRetryHandling handler = NtfEventHandlerRegistry.getEventHandler(event.getEventServiceTypeKey());
            handler.cancelEvent(event.getReferenceId());
        }
    }

    private SendMessageReq getSendMessageReq(String subscriberNumber, String eventType) {
        SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "im";
        req.destRcptID.value = subscriberNumber;

        req.rMsa.value = "";
        req.rMsgID.value = "";
        req.oMsa.value = "";
        req.oMsgID.value = "";

        req.eventType.value = eventType;
        req.eventID.value = "id";
        req.eventID.value = "myid";
        return req;
    }

    private SendMessageReq getSendMessageReqForMfs() {
        SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "im";
        req.destRcptID.value = to;

        req.rMsa.value = msgInfo.rmsa.toString();
        req.rMsgID.value = msgInfo.rmsgid.toString();
        req.oMsa.value = msgInfo.omsa.toString();
        req.oMsgID.value = msgInfo.omsgid.toString();

        req.eventType.value = NtfEventTypes.DEFAULT_NTF.getName();
        req.eventID.value = "id";

        HashMap<String, String> extra = new HashMap<String, String>();
        extra.put("srv-type", "foo");
        extra.put("BarFoo", "bar");

        req.extraValue = extra;
        req.eventID.value = "myid";
        return req;
    }

    private void storeMfsMessage() throws Exception {

        Calendar now = Calendar.getInstance();
        deleteDirectory(new File(strDirectoy + "\\internal"));

        CommonMessagingAccess.setMcd(new McdStub());
        commonMessagingAccess = CommonMessagingAccess.getInstance();

        final ConfigManager mfsConfig = MfsConfiguration.getInstance();
        mfsConfig.setParameter(MfsConfiguration.MfsRootPath, strDirectoy);
        commonMessagingAccess.reInitializeMfs(mfsConfig);

        // create and store messages for testing
        final Container1 c1_1 = new Container1();
        c1_1.setFrom(from);
        c1_1.setTo(to);
        c1_1.setSubject("subject");
        c1_1.setMsgClass("voice");
        c1_1.setDateTime(now.getTimeInMillis());
        final Container2 c2_1 = new Container2();
        final MsgBodyPart[] c3_1Parts = new MsgBodyPart[1];
        c3_1Parts[0] = new MsgBodyPart();
        final StateAttributes attributes1 = new StateAttributes();
        attributes1.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
        attributes1.setAttribute(Container1.Message_class, c1_1.getMsgClass());
        msgInfo = commonMessagingAccess.storeMessageTest(c1_1, c2_1, c3_1Parts, attributes1);

        final Container1 c1_1a = new Container1();
        c1_1a.setFrom(from);
        c1_1a.setTo(to);
        c1_1a.setSubject("subject");
        c1_1a.setMsgClass("voice");
        c1_1a.setDateTime(now.getTimeInMillis());
        final Container2 c2_1a = new Container2();
        final MsgBodyPart[] c3_1Partsa = new MsgBodyPart[1];
        c3_1Partsa[0] = new MsgBodyPart();
        final StateAttributes attributes1a = new StateAttributes();
        attributes1a.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
        attributes1a.setAttribute(Container1.Message_class, c1_1a.getMsgClass());
        msgInfo = commonMessagingAccess.storeMessageTest(c1_1a, c2_1a, c3_1Partsa, attributes1a);
    }

    private static boolean deleteDirectory(final File path) {
        if(path.exists()) {
            final File[] files = path.listFiles();
            for(int i = 0; i < files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
*/
}
