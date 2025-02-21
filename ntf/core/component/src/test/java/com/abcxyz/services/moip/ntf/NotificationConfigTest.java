package com.abcxyz.services.moip.ntf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.mobeon.common.cmnaccess.CommonMessagingAccessTest;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSComDataException;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants.hlrFailAction;
import com.mobeon.ntf.NtfMain;

public class NotificationConfigTest {

    private static NtfMain ntf;

    @BeforeClass
    static public void startup() throws Exception {

        String userDir = System.getProperty("user.dir");
        System.setProperty("componentservicesconfig", userDir + "/../ipms_sys2/backend/cfg/componentservices.cfg");
        System.setProperty("ntfHome", userDir + "/test/junit/" );

        CommonMessagingAccessTest.setUp();

        BasicConfigurator.configure();

        // Start NTF
        ntf = new NtfMain();
        Config.updateCfg();
    }

    @AfterClass
    static public void tearDown() {
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testConfig() throws Exception {
        String key;

        HashMap<String, String> ntfConfigBefore = getOriginalNtfConfiguration();
        System.out.println("ntfConfigBefore HashMap size: " + ntfConfigBefore.size());

        Iterator<String> it = ntfConfigBefore.keySet().iterator();
        while (it.hasNext()) {
            key = (String)it.next();
            String value = ntfConfigBefore.get(key);
            System.out.println("command:" + key + " value:" + value);
        }

// does not exist anymore
//      assertString(Config.getMcrInstanceName(), ntfConfigBefore.get("getMcrInstanceName()"));//ntf1@EV001A4B5D01FA");
//      assertString(Config.getConfigFileName().equals(ntfConfigBefore.get("getConfigFileName()"));//instance_template/cfg/notification.cfg");
//      assertString(Config.getHost().equals(ntfConfigBefore.get("getHost()"));//EV001A4B5D01FA");
//      assertString(Config.getXmpErrorCodeFile().equals(ntfConfigBefore.get("getXmpErrorCodeFile()"));//instance_template/cfg/XmpErrorCodes.cfg");
//      assertBoolean(Config.isReplace(), ntfConfigBefore.get("isReplace()"));//false");

// different result
//      one has config the other has instance_template
//      assertString(Config.getInstallDir().equals(ntfConfigBefore.get("getInstallDir()"));//instance_template");
//      used by outdial to get the outdial-default.cfg file
//      assertString(Config.getNtfHome(), ntfConfigBefore.get("getNtfHome()"));//instance_template");
//      assertString(Config.getDataDirectory(), ntfConfigBefore.get("getDataDirectory()"));//instance_template/data");
//      assertString(Config.getPhraseDirectory(), ntfConfigBefore.get("getPhraseDirectory()"));//instance_template/templates");

        assertString(Config.getMMSVersion(), ntfConfigBefore.get("getMMSVersion()"));// ");
        assertString(Config.getSmeServiceTypeForMwi(), ntfConfigBefore.get("getSmeServiceTypeForMwi()"));//VMN");
        assertBoolean(Config.isSlamdownTimeOfLastCall(), ntfConfigBefore.get("isSlamdownTimeOfLastCall()"));//true");
        assertBoolean(Config.isSlamdownOldestFirst(), ntfConfigBefore.get("isSlamdownOldestFirst()"));//true");
        assertStringArray(Config.getReplaceNotifications(), ntfConfigBefore.get("getReplaceNotifications()"));//"c, mailquotaexceeded");
        assertStringArray(Config.getAllowedSmsc(), ntfConfigBefore.get("getAllowedSmsc()"));//"");
        assertString(Config.getDefaultDateFormat(), ntfConfigBefore.get("getDefaultDateFormat()"));//yyyy/mm/dd");
        assertString(Config.getDefaultLanguage(), ntfConfigBefore.get("getDefaultLanguage()"));//en");
        assertString(Config.getDefaultNotificationFilter(), ntfConfigBefore.get("getDefaultNotificationFilter()"));//1;n;a;evfm;;;997;;;;;OFF;;");
        assertString(Config.getDefaultNotificationFilter2(), ntfConfigBefore.get("getDefaultNotificationFilter2()"));//1;y;a;s;SMS,EML;slamdown,slamdown;998;;;;;SLAMDOWN;;");
        assertString(Config.getDefaultTimeFormat(), ntfConfigBefore.get("getDefaultTimeFormat()"));//24");
        assertString(Config.getLogicalZone(), ntfConfigBefore.get("getLogicalZone()"));//unspecified");
        assertString(Config.getMMSPostMaster(), ntfConfigBefore.get("getMMSPostMaster()"));//");
        assertString(Config.getMMSSystemDomain(), ntfConfigBefore.get("getMMSSystemDomain()"));//abcxyz.com");
        assertString(Config.getNetmask(), ntfConfigBefore.get("getNetmask()"));//255.255.255.0");
    //    assertString(Config.getNtfHostFqdn(), ntfConfigBefore.get("getNtfHostFqdn()"));//EV001A4B5D01FA");
        assertString(Config.getNumberToMessagingSystem(), ntfConfigBefore.get("getNumberToMessagingSystem()"));//133");
        assertString(Config.getNumberToMessagingSystemForCallMwi(), ntfConfigBefore.get("getNumberToMessagingSystemForCallMwi()"));//133");
        assertString(Config.getPathToSnmpScript(), ntfConfigBefore.get("getPathToSnmpScript()"));///opt/moip/snmp/scripts");
        assertString(Config.getQuotaTemplate(), ntfConfigBefore.get("getQuotaTemplate()"));//mailquotaexceeded");
        assertString(Config.getSlamdownTruncatedNumberIndication(), ntfConfigBefore.get("getSlamdownTruncatedNumberIndication()"));//*");
        assertString(Config.getMcnTruncatedNumberIndication(), ntfConfigBefore.get("getMcnTruncatedNumberIndication()"));//*");
        assertString(Config.getSmeServiceType(), ntfConfigBefore.get("getSmeServiceType()"));//VMN");
        assertString(Config.getTemplateDir(), ntfConfigBefore.get("getTemplateDir()"));///opt/moip/config/ntf/templates");
        assertString(Config.getSnmpAgentAddress().toString(), ntfConfigBefore.get("getSnmpAgentAddress()"));///127.0.0.1");
        assertString(Config.getSmscErrorAction(), ntfConfigBefore.get("getSmscErrorAction()"));//handle");
        assertString(Config.getVersion(), ntfConfigBefore.get("getVersion()"));//unknown");
        assertString(Config.getWapPushPasswd(), ntfConfigBefore.get("getWapPushPasswd()"));//NoDefault");
        assertString(Config.getWapPushRetrievalHost(), ntfConfigBefore.get("getWapPushRetrievalHost()"));//");
        assertString(Config.getWapPushUrlSuffix(), ntfConfigBefore.get("getWapPushUrlSuffix()"));///wap_push_dir/wap_push_appl");
        assertString(Config.getWapPushUserName(), ntfConfigBefore.get("getWapPushUserName()"));//NoDefault");

        assertBoolean(Config.getDoOutdial(), ntfConfigBefore.get("getDoOutdial()"));//true");
        assertInt(Config.getOutdialQueueSize(), ntfConfigBefore.get("getOutdialQueueSize()"));//1000");


        assertInt(Config.getOutdialWorkers(), ntfConfigBefore.get("getOutdialWorkers()"));//30");
        assertBoolean(Config.getDoSipMwi(), ntfConfigBefore.get("getDoSipMwi()"));//true");
        assertInt(Config.getSlamdownQueueSize(), ntfConfigBefore.get("getSlamdownQueueSize()"));//1000");
        assertInt(Config.getSlamdownWorkers(), ntfConfigBefore.get("getSlamdownWorkers()"));//10");
        assertString(Config.getSlamdownMcnSmsUnitRetrySchema(), ntfConfigBefore.get("getSlamdownMcnSmsUnitRetrySchema()"));//1:try=3 CONTINUE");
        assertInt(Config.getSlamdownMcnSmsUnitExpireTimeInMin(), ntfConfigBefore.get("getSlamdownMcnSmsUnitExpireTimeInMin()"));//4");
        assertString(Config.getSlamdownMcnSmsType0RetrySchema(), ntfConfigBefore.get("getSlamdownMcnSmsType0RetrySchema()"));//1440:try=3 CONTINUE");
        assertInt(Config.getSlamdownMcnSmsType0ExpireTimeInMin(), ntfConfigBefore.get("getSlamdownMcnSmsType0ExpireTimeInMin()"));//4321");
        assertString(Config.getSlamdownMcnSmsInfoRetrySchema(), ntfConfigBefore.get("getSlamdownMcnSmsInfoRetrySchema()"));//1:try=3 CONTINUE");
        assertInt(Config.getSlamdownMcnSmsInfoExpireTimeInMin(), ntfConfigBefore.get("getSlamdownMcnSmsInfoExpireTimeInMin()"));//4");
        assertBoolean(Config.getDoSmsType0Slamdown(), ntfConfigBefore.get("getDoSmsType0Slamdown()"));//true");
        assertBoolean(Config.getDoSmsType0Mcn(), ntfConfigBefore.get("getDoSmsType0Mcn()"));//true");
        assertBoolean(Config.getDoSmsType0Outdial(), ntfConfigBefore.get("getDoSmsType0Outdial()"));//true");
        assertInt(Config.getSipMwiQueueSize(), ntfConfigBefore.get("getSipMwiQueueSize()"));//1000");
        assertInt(Config.getSipMwiWorkers(), ntfConfigBefore.get("getSipMwiWorkers()"));//10");
        assertString(Config.getSipMwiNotifRetrySchema(), ntfConfigBefore.get("getSipMwiNotifRetrySchema()"));//20:try=100 5 CONTINUE");
        assertLong(Config.getSipMwiNotifExpireTimeInMin(), ntfConfigBefore.get("getSipMwiNotifExpireTimeInMin()"));//4320");
        assertBoolean(Config.isAlternativeFlashDcs(), ntfConfigBefore.get("isAlternativeFlashDcs()"));//false");
        assertBoolean(Config.isCancelSmsAtRetrieval(), ntfConfigBefore.get("isCancelSmsAtRetrieval()"));//false");
        assertBoolean(Config.isCheckQuota(), ntfConfigBefore.get("isCheckQuota()"));//false");
        assertBoolean(Config.isCheckTerminalCapability(), ntfConfigBefore.get("isCheckTerminalCapability()"));//false");
        assertBoolean(Config.isDefaultUserHasMwi(), ntfConfigBefore.get("isDefaultUserHasMwi()"));//false");
        assertBoolean(Config.isDefaultUserHasFlash(), ntfConfigBefore.get("isDefaultUserHasFlash()"));//true");
        assertBoolean(Config.isDefaultUserHasReplace(), ntfConfigBefore.get("isDefaultUserHasReplace()"));//true");
        assertBoolean(Config.isDiscardWhenQuota(), ntfConfigBefore.get("isDiscardWhenQuota()"));//true");

        assertBoolean(Config.isDiscardSmsWhenCountIs0(), ntfConfigBefore.get("isDiscardSmsWhenCountIs0()"));//false");
        assertBoolean(Config.isKeepSmscConnections(), ntfConfigBefore.get("isKeepSmscConnections()"));//false");
        assertBoolean(Config.isDisableSmscReplace(), ntfConfigBefore.get("isDisableSmscReplace()"));//false");
        assertBoolean(Config.isSendUpdateAfterRetrieval(), ntfConfigBefore.get("isSendUpdateAfterRetrieval()"));//false");
        assertBoolean(Config.isSendUpdateAfterTerminalChange(), ntfConfigBefore.get("isSendUpdateAfterTerminalChange()"));//false");
        assertBoolean(Config.isSetReplyPath(), ntfConfigBefore.get("isSetReplyPath()"));//false");
        assertBoolean(Config.isSlamdownMcnNotificationWhenPhoneOnExpiry(), ntfConfigBefore.get("isSlamdownMcnNotificationWhenPhoneOnExpiry()"));//true");
        assertBoolean(Config.isMcnOldestFirst(), ntfConfigBefore.get("isMcnOldestFirst()"));//true");
        assertBoolean(Config.isMcnTimeOfLastCall(), ntfConfigBefore.get("isMcnTimeOfLastCall()"));//true");
        assertBoolean(Config.isWarnWhenQuota(), ntfConfigBefore.get("isWarnWhenQuota()"));//true");
        assertBoolean(Config.isUnreadMessageReminder(), ntfConfigBefore.get("isUnreadMessageReminder()"));//false");
        assertBoolean(Config.isUnreadMessageReminderFlash(), ntfConfigBefore.get("isUnreadMessageReminderFlash()"));//false");
        assertBoolean(Config.shouldUseMMSPostmaster(), ntfConfigBefore.get("shouldUseMMSPostmaster()"));//false");
        assertBoolean(Config.shouldUseSmil(), ntfConfigBefore.get("shouldUseSmil()"));//true");
        assertBoolean(Config.shouldUseCallerInEventDescription(), ntfConfigBefore.get("shouldUseCallerInEventDescription()"));//false");
        assertBoolean(Config.isBearingNetworkGsm(), ntfConfigBefore.get("isBearingNetworkGsm()"));//true");
        assertBoolean(Config.isBearingNetworkCdma2000(), ntfConfigBefore.get("isBearingNetworkCdma2000()"));//false");
        assertBoolean(Config.isBearingNetworkPstn(), ntfConfigBefore.get("isBearingNetworkPstn()"));//false");
        assertBoolean(Config.isMwiOffCheckCount(), ntfConfigBefore.get("isMwiOffCheckCount()"));//false");
        assertBoolean(Config.isSmsHandlerLoadBalanced(), ntfConfigBefore.get("isSmsHandlerLoadBalanced()"));//false");
        assertBoolean(Config.isSplitMwiAndSms(), ntfConfigBefore.get("isSplitMwiAndSms()"));//false");
        assertBoolean(Config.isAutoforwardedMessagesOn(), ntfConfigBefore.get("isAutoforwardedMessagesOn()"));//false");


//Insert test for AutoForwardEmail feature

        Boolean  param;
                param = Config.isEmailForwardTranscodeAudioOn();
                System.out.println("isEmailForwardTranscodeAudioOn() " + param );

                param = Config.isEmailForwardTranscodeVideoOn();
                System.out.println("isEmailForwardTranscodeVideoOn() " + param );

        long   value;

       		value = Config.getEmailForwardMaximumSize();
                System.out.println("getEmailForwardMaximumSize() " + value );


//End of Insert test for AutoForwardEmail feature


        assertInt(Config.getCallMwiCaller(), ntfConfigBefore.get("getCallMwiCaller()"));//0");
        assertHLrFailAction(Config.getHLRRoamingFailureAction(), ntfConfigBefore.get("getHLRRoamingFailureAction()"));//RETRY"); 
        assertInt(Config.getImapTimeout(), ntfConfigBefore.get("getImapTimeout()"));//5000");
        assertInt(Config.getInternalQueueSize(), ntfConfigBefore.get("getInternalQueueSize()"));//256");
        assertInt(Config.getLogLevel(), ntfConfigBefore.get("getLogLevel()"));//1");
        assertInt(Config.getLogSize(), ntfConfigBefore.get("getLogSize()"));//10000000");
        assertInt(Config.getMMSMaxConnection(), ntfConfigBefore.get("getMMSMaxConnection()"));//10");
        assertInt(Config.getMaxTimeBeforeExpunge(), ntfConfigBefore.get("getMaxTimeBeforeExpunge()"));//300");
        assertInt(Config.getMaxXmpConnections(), ntfConfigBefore.get("getMaxXmpConnections()"));//3");
        assertInt(Config.getMMSMaxVideoLength(), ntfConfigBefore.get("getMMSMaxVideoLength()"));//-1");
        assertInt(Config.getNotifThreads(), ntfConfigBefore.get("getNotifThreads()"));//10");
        assertInt(Config.getNumberOfSms(), ntfConfigBefore.get("getNumberOfSms()"));//5");
        assertInt(Config.getNumberingPlanIndicator(), ntfConfigBefore.get("getNumberingPlanIndicator()"));//1");
        assertInt(Config.getPagerPauseTime(), ntfConfigBefore.get("getPagerPauseTime()"));//1000");
        assertInt(Config.getSlamdownMaxCallers(), ntfConfigBefore.get("getSlamdownMaxCallers()"));//0");
        assertInt(Config.getSlamdownMaxCallsPerCaller(), ntfConfigBefore.get("getSlamdownMaxCallsPerCaller()"));//0");
        assertInt(Config.getMcnMaxCallers(), ntfConfigBefore.get("getMcnMaxCallers()"));//0");
        assertInt(Config.getMcnMaxCallsPerCaller(), ntfConfigBefore.get("getMcnMaxCallsPerCaller()"));//0");
        assertString(Config.getMcnLanguage(), ntfConfigBefore.get("getMcnLanguage()"));//en");
        assertInt(Config.getSlamdownMaxDigitsInNumber(), ntfConfigBefore.get("getSlamdownMaxDigitsInNumber()"));//0");
        assertInt(Config.getMcnMaxDigitsInNumber(), ntfConfigBefore.get("getMcnMaxDigitsInNumber()"));//0");
        assertInt(Config.getSmeSourceNpi(), ntfConfigBefore.get("getSmeSourceNpi()"));//0");
        assertInt(Config.getSmeSourceTon(), ntfConfigBefore.get("getSmeSourceTon()"));//0");
        assertInt(Config.getSmppVersion(), ntfConfigBefore.get("getSmppVersion()"));//52");
        assertInt(Config.getSmsMaxConn(), ntfConfigBefore.get("getSmsMaxConn()"));//20");
        assertInt(Config.getSmsStringLength(), ntfConfigBefore.get("getSmsStringLength()"));//140");
        assertInt(Config.getSmsPriority(), ntfConfigBefore.get("getSmsPriority()"));//0");
        assertInt(Config.getSmscPollInterval(), ntfConfigBefore.get("getSmscPollInterval()"));//60");
        assertInt(Config.getSmsQueueSize(), ntfConfigBefore.get("getSmsQueueSize()"));//20");
        assertInt(Config.getSmscTimeout(), ntfConfigBefore.get("getSmscTimeout()"));//30");
        assertInt(Config.getSlamdownConn(), ntfConfigBefore.get("getSlamdownConn()"));//10");
        assertInt(Config.getTypeOfNumber(), ntfConfigBefore.get("getTypeOfNumber()"));//1");
        assertInt(Config.getUnreadMessageReminderInterval(), ntfConfigBefore.get("getUnreadMessageReminderInterval()"));//86400");
        assertInt(Config.getUnreadMessageReminderMaxTimes(), ntfConfigBefore.get("getUnreadMessageReminderMaxTimes()"));//3");
        assertInt(Config.getValidity_flash(), ntfConfigBefore.get("getValidity_flash()"));//-1");

//      current is 24 and old is -1 (old is wrong)
//        assertInt(Config.getValidity_smsType0(), ntfConfigBefore.get("getValidity_smsType0()"));//-1");
        assertInt(Config.getValidity_mwiOn(), ntfConfigBefore.get("getValidity_mwiOn()"));//-1");
        assertInt(Config.getValidity_mwiOff(), ntfConfigBefore.get("getValidity_mwiOff()"));//-1");
        assertInt(Config.getValidity_mailQuotaExceeded(), ntfConfigBefore.get("getValidity_mailQuotaExceeded()"));//-1");
        assertInt(Config.getValidity_temporaryGreetingOnReminder(), ntfConfigBefore.get("getValidity_temporaryGreetingOnReminder()"));//-1");
        assertInt(Config.getValidity_voicemailOffReminder(), ntfConfigBefore.get("getValidity_voicemailOffReminder()"));//-1");
        assertInt(Config.getValidity_cfuOnReminder(), ntfConfigBefore.get("getValidity_cfuOnReminder()"));//-1");
        assertInt(Config.getValidity_slamdown(), ntfConfigBefore.get("getValidity_slamdown()"));//-1");
        assertInt(Config.getValidity_mcn(), ntfConfigBefore.get("getValidity_mcn()"));//-1");
        assertInt(Config.getJournalRefreshInterval(), ntfConfigBefore.get("getJournalRefreshInterval()"));//1800");
        assertInt(Config.getXmpPollInterval(), ntfConfigBefore.get("getXmpPollInterval()"));//90");
        assertInt(Config.getXmpTimeout(), ntfConfigBefore.get("getXmpTimeout()"));//30");
        assertInt(Config.getXmpValidity(), ntfConfigBefore.get("getXmpValidity()"));//90");
        assertInt(Config.getXmpRefreshTime(), ntfConfigBefore.get("getXmpRefreshTime()"));//30");
        assertString(Config.getMmscUser(), ntfConfigBefore.get("getMmscUser()"));//NoDefault");
        assertString(Config.getMmscVaspId(), ntfConfigBefore.get("getMmscVaspId()"));//NTF");

//      current is empty and previous was getting the localhost var.
//        assertString(Config.getMmscVasId(), ntfConfigBefore.get("getMmscVasId()"));//ntf@142.133.116.118");
        assertInt(Config.getSnmpAgentPort(), ntfConfigBefore.get("getSnmpAgentPort()"));//18001");
        assertInt(Config.getSnmpAgentTimeout(), ntfConfigBefore.get("getSnmpAgentTimeout()"));//10");
        assertInt(Config.getLoginFileValidityPeriod(), ntfConfigBefore.get("getLoginFileValidityPeriod()"));//360");
        assertString(Config.getNotifRetrySchema(), ntfConfigBefore.get("getNotifRetrySchema()"));//5 5 60 240 CONTINUE");
        assertLong(Config.getNotifExpireTimeInMin(), ntfConfigBefore.get("getNotifExpireTimeInMin()"));//4320");
        assertString(Config.getNtfEventsRootPath(), ntfConfigBefore.get("getNtfEventsRootPath()"));///opt/moip/events/ntf");
        assertInt(Config.getServiceListenerCorePoolSize(), ntfConfigBefore.get("getServiceListenerCorePoolSize()"));//10");
        assertInt(Config.getServiceListenerMaxPoolSize(), ntfConfigBefore.get("getServiceListenerMaxPoolSize()"));//100");
        assertInt(Config.getCallMwiCaller(), ntfConfigBefore.get("getCallMwiCaller(String name)"));//0");

//      String array
//        assertInt(Config.getSmppErrorCodesIgnored()[0], ntfConfigBefore.get("getSmppErrorCodesIgnored()"));//[I@201f9");
        assertSMSAddress(Config.getSmeSourceAddress(), ntfConfigBefore.get("getSmeSourceAddress()"));//{SMSAddress ,0,0}");

        assertString(Config.getSmscBackup("smsc"), ntfConfigBefore.get("getSmscBackup(\"smsc\")"));//null");

        assertSMSAddress(Config.getSourceAddress("cfuonreminder"), ntfConfigBefore.get("getSourceAddress(\"cfuonreminder\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("email"), ntfConfigBefore.get("getSourceAddress(\"email\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("fax"), ntfConfigBefore.get("getSourceAddress(\"fax\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("flash"), ntfConfigBefore.get("getSourceAddress(\"flash\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("mailquotaexceeded"), ntfConfigBefore.get("getSourceAddress(\"mailquotaexceeded\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("mcn"), ntfConfigBefore.get("getSourceAddress(\"mcn\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("mwioff"), ntfConfigBefore.get("getSourceAddress(\"mwioff\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("mwion"), ntfConfigBefore.get("getSourceAddress(\"mwion\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("slamdown"), ntfConfigBefore.get("getSourceAddress(\"slamdown\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("smstype0"), ntfConfigBefore.get("getSourceAddress(\"smstype0\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("temporarygreetingonreminde"), ntfConfigBefore.get("getSourceAddress(\"temporarygreetingonreminder\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("unreadmessagereminder"), ntfConfigBefore.get("getSourceAddress(\"unreadmessagereminder\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("updatesms"), ntfConfigBefore.get("getSourceAddress(\"updatesms\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("video"), ntfConfigBefore.get("getSourceAddress(\"video\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("voice"), ntfConfigBefore.get("getSourceAddress(\"voice\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("voicemailoffreminder"), ntfConfigBefore.get("getSourceAddress(\"voicemailoffreminder\")"));//{SMSAddress ,0,0}");
        assertSMSAddress(Config.getSourceAddress("vvm"), ntfConfigBefore.get("getSourceAddress(\"vvm\")"));//{SMSAddress ,0,0}");

        assertInt(Config.getValidity("cfuonreminder"), ntfConfigBefore.get("getValidity(\"cfuonreminder\")"));//-1");
        assertInt(Config.getValidity("flash"), ntfConfigBefore.get("getValidity(\"flash\")"));//-1");
        assertInt(Config.getValidity("mailquotaexceeded"), ntfConfigBefore.get("getValidity(\"mailquotaexceeded\")"));//-1");
        assertInt(Config.getValidity("mcn"), ntfConfigBefore.get("getValidity(\"mcn\")"));//-1");
        assertInt(Config.getValidity("mwioff"), ntfConfigBefore.get("getValidity(\"mwioff\")"));//-1");
        assertInt(Config.getValidity("mwion"), ntfConfigBefore.get("getValidity(\"mwion\")"));//-1");
        assertInt(Config.getValidity("slamdown"), ntfConfigBefore.get("getValidity(\"slamdown\")"));//-1");

//      current is 24 and the previous is -1 (and the previous is wrong)
//        assertInt(Config.getValidity("smstype0"), ntfConfigBefore.get("getValidity(\"smstype0\")"));//-1");
        assertInt(Config.getValidity("temporarygreetingonreminder"), ntfConfigBefore.get("getValidity(\"temporarygreetingonreminder\")"));//-1");
        assertInt(Config.getValidity("unreadmessagereminder"), ntfConfigBefore.get("getValidity(\"unreadmessagereminder\")"));//-1");
        assertInt(Config.getValidity("updatesms"), ntfConfigBefore.get("getValidity(\"updatesms\")"));//-1");
        assertInt(Config.getValidity("voicemailoffreminder"), ntfConfigBefore.get("getValidity(\"voicemailoffreminder\")"));//-1");
        assertInt(Config.getValidity("vvm"), ntfConfigBefore.get("getValidity(\"vvm\")"));// -1");

        assertInt(Config.getValidity_cfuOnReminder(), ntfConfigBefore.get("getValidity_cfuOnReminder()"));//-1");
        assertInt(Config.getValidity_flash(), ntfConfigBefore.get("getValidity_flash()"));//-1");
        assertInt(Config.getValidity_mailQuotaExceeded(), ntfConfigBefore.get("getValidity_mailQuotaExceeded()"));//-1");
        assertInt(Config.getValidity_mcn(), ntfConfigBefore.get("getValidity_mcn()"));//-1");
        assertInt(Config.getValidity_mwiOff(), ntfConfigBefore.get("getValidity_mwiOff()"));//-1");
        assertInt(Config.getValidity_mwiOn(), ntfConfigBefore.get("getValidity_mwiOn()"));//-1");
        assertInt(Config.getValidity_slamdown(), ntfConfigBefore.get("getValidity_slamdown()"));//-1");

//      current is 24 and the previous is -1 (and the previous is wrong)
//        assertInt(Config.getValidity_smsType0(), ntfConfigBefore.get("getValidity_smsType0()"));//-1");
        assertInt(Config.getValidity_temporaryGreetingOnReminder(), ntfConfigBefore.get("getValidity_temporaryGreetingOnReminder()"));//-1");
        assertInt(Config.getValidity_voicemailOffReminder(), ntfConfigBefore.get("getValidity_voicemailOffReminder()"));//-1");

        assertInt(Config.getVvmQueueSize(), ntfConfigBefore.get("getVvmQueueSize()"));//1000");
        assertInt(Config.getVvmWorkers(), ntfConfigBefore.get("getVvmWorkers()"));//10");
        assertInt(Config.getVvmSourcePort(), ntfConfigBefore.get("getVvmSourcePort()"));//0");
        assertInt(Config.getVvmDestinationPort(), ntfConfigBefore.get("getVvmDestinationPort()"));//5709");
        assertString(Config.getVvmSmsUnitRetrySchema(), ntfConfigBefore.get("getVvmSmsUnitRetrySchema()"));//1:try=3 CONTINUE");
        assertInt(Config.getVvmSmsUnitExpireTimeInMin(), ntfConfigBefore.get("getVvmSmsUnitExpireTimeInMin()"));//4");
        assertInt(Config.getValidity_vvm(), ntfConfigBefore.get("getValidity_vvm()"));//-1");
        assertInt(Config.getValidity_vvm(), ntfConfigBefore.get("getValidity_vvm()"));//-1");

        assertString(Config.getFallbackSipMwi(), ntfConfigBefore.get("getFallbackSipMwi()"));
        assertString(Config.getFallbackSms(), ntfConfigBefore.get("getFallbackSms()"));
    }


    private HashMap<String, String> getOriginalNtfConfiguration() {
        HashMap<String, String> ntfConfigBefore = new HashMap<String, String>();

        ntfConfigBefore.put("getAllowedSmsc()", "");
        ntfConfigBefore.put("getConfigFileName()", "instance_template/cfg/notification.cfg");
        ntfConfigBefore.put("getDefaultDateFormat()", "yyyy/mm/dd");
        ntfConfigBefore.put("getDefaultLanguage()", "en");
        ntfConfigBefore.put("getDefaultNotificationFilter()", "1;n;a;evfm;;;997;;;;;OFF;;");
        ntfConfigBefore.put("getDefaultNotificationFilter2()", "1;y;a;s;SMS,EML;slamdown,slamdown;998;;;;;SLAMDOWN;;");
        ntfConfigBefore.put("getDefaultTimeFormat()", "24");
        ntfConfigBefore.put("getEsiFailureAllowTypes()", "");
        ntfConfigBefore.put("getHLRRoamingFailureAction()","RETRY");
        ntfConfigBefore.put("getHlrRoamFailureAllowList()","");
        ntfConfigBefore.put("getFaxPrintIp()", "");
        ntfConfigBefore.put("getHost()", "EV001A4B5D01FA");
        ntfConfigBefore.put("getInstallDir()", "instance_template");
        ntfConfigBefore.put("getLogicalZone()", "unspecified");
        ntfConfigBefore.put("getMMSPostMaster()", "");
        ntfConfigBefore.put("getMMSVersion()", "");
        ntfConfigBefore.put("getMcrInstanceName()", "ntf1@EV001A4B5D01FA");
        ntfConfigBefore.put("getMMSSystemDomain()", "abcxyz.com");
        ntfConfigBefore.put("getNetmask()", "255.255.255.0");
        ntfConfigBefore.put("getNtfHome()", "instance_template");
        ntfConfigBefore.put("getNtfHostFqdn()", "EV001A4B5D01FA");
        ntfConfigBefore.put("getNumberToMessagingSystem()", "133");
        ntfConfigBefore.put("getNumberToMessagingSystemForCallMwi()", "133");
        ntfConfigBefore.put("getDataDirectory()", "instance_template/data");
        ntfConfigBefore.put("getPathToSnmpScript()", "/opt/moip/snmp/scripts");
        ntfConfigBefore.put("getPhraseDirectory()", "instance_template/templates");
        ntfConfigBefore.put("getQuotaTemplate()", "mailquotaexceeded");
        ntfConfigBefore.put("getReplaceNotifications()", "c, mailquotaexceeded");
        ntfConfigBefore.put("getSlamdownTruncatedNumberIndication()", "*");
        ntfConfigBefore.put("getMcnTruncatedNumberIndication()", "*");
        ntfConfigBefore.put("getSmeServiceType()", "VMN");
        ntfConfigBefore.put("getSmeServiceTypeForMwi()", "VMN");
        ntfConfigBefore.put("getTempDir()", "/opt/moip/config/ntf/templates");
        ntfConfigBefore.put("getSmscErrorAction()", "handle");
        ntfConfigBefore.put("getSnmpAgentAddress()", "/127.0.0.1");
        ntfConfigBefore.put("getVersion()", "unknown");
        ntfConfigBefore.put("getWapPushPasswd()", "NoDefault");
        ntfConfigBefore.put("getWapPushRetrievalHost()", "");
        ntfConfigBefore.put("getWapPushUrlSuffix()", "/wap_push_dir/wap_push_appl");
        ntfConfigBefore.put("getWapPushUserName()", "NoDefault");
        ntfConfigBefore.put("getXmpErrorCodeFile()", "instance_template/cfg/XmpErrorCodes.cfg");
        ntfConfigBefore.put("getDoOutdial()", "true");
        ntfConfigBefore.put("getOutdialQueueSize()", "1000");
        ntfConfigBefore.put("getOutdialWorkers()", "30");
        ntfConfigBefore.put("getDoSipMwi()", "true");
        ntfConfigBefore.put("getSlamdownQueueSize()", "1000");
        ntfConfigBefore.put("getSlamdownWorkers()", "10");
        ntfConfigBefore.put("getSlamdownMcnSmsUnitRetrySchema()", "1:try=3 CONTINUE");
        ntfConfigBefore.put("getSlamdownMcnSmsUnitExpireTimeInMin()", "4");
        ntfConfigBefore.put("getSlamdownMcnSmsType0RetrySchema()", "1440:try=3 CONTINUE");
        ntfConfigBefore.put("getSlamdownMcnSmsType0ExpireTimeInMin()", "4321");
        ntfConfigBefore.put("getSlamdownMcnSmsInfoRetrySchema()", "1:try=3 CONTINUE");
        ntfConfigBefore.put("getSlamdownMcnSmsInfoExpireTimeInMin()", "4");
        ntfConfigBefore.put("getDoSmsType0Slamdown()", "true");
        ntfConfigBefore.put("getDoSmsType0Mcn()", "true");
        ntfConfigBefore.put("getDoSmsType0Outdial()", "true");
        ntfConfigBefore.put("getSipMwiQueueSize()", "1000");
        ntfConfigBefore.put("getSipMwiWorkers()", "10");
        ntfConfigBefore.put("getSipMwiNotifRetrySchema()", "20:try=100 5 CONTINUE");
        ntfConfigBefore.put("getSipMwiNotifExpireTimeInMin()", "4320");
        ntfConfigBefore.put("getVvmQueueSize()", "1000");
        ntfConfigBefore.put("getVvmWorkers()", "10");
        ntfConfigBefore.put("getVvmSourcePort()", "0");
        ntfConfigBefore.put("getVvmDestinationPort()", "5709");
        ntfConfigBefore.put("getVvmSmsUnitRetrySchema()", "1:try=3 CONTINUE");
        ntfConfigBefore.put("getVvmSmsUnitExpireTimeInMin()", "4");
        ntfConfigBefore.put("isAlternativeFlashDcs()", "false");
        ntfConfigBefore.put("isCancelSmsAtRetrieval()", "false");
        ntfConfigBefore.put("isCheckQuota()", "false");
        ntfConfigBefore.put("isCheckTerminalCapability()", "false");
        ntfConfigBefore.put("isDefaultUserHasMwi()", "false");
        ntfConfigBefore.put("isDefaultUserHasFlash()", "true");
        ntfConfigBefore.put("isDefaultUserHasReplace()", "true");
        ntfConfigBefore.put("isDiscardWhenQuota()", "true");
        ntfConfigBefore.put("isDiscardSmsWhenCountIs0()", "false");
        ntfConfigBefore.put("isEsiSystem()", "false");
        ntfConfigBefore.put("isKeepSmscConnections()", "false");
        ntfConfigBefore.put("isDisableSmscReplace()", "false");
        ntfConfigBefore.put("isSendUpdateAfterRetrieval()", "false");
        ntfConfigBefore.put("isSendUpdateAfterTerminalChange()", "false");
        ntfConfigBefore.put("isSetReplyPath()", "false");
        ntfConfigBefore.put("isSlamdownOldestFirst()", "true");
        ntfConfigBefore.put("isSlamdownTimeOfLastCall()", "true");
        ntfConfigBefore.put("isSlamdownMcnNotificationWhenPhoneOnExpiry()", "true");
        ntfConfigBefore.put("isMcnOldestFirst()", "true");
        ntfConfigBefore.put("isMcnTimeOfLastCall()", "true");
        ntfConfigBefore.put("isWarnWhenQuota()", "true");
        ntfConfigBefore.put("isReplace()", "false");
        ntfConfigBefore.put("isUnreadMessageReminder()", "false");
        ntfConfigBefore.put("isUnreadMessageReminderFlash()", "false");
        ntfConfigBefore.put("shouldUseMMSPostmaster()", "false");
        ntfConfigBefore.put("shouldUseSmil()", "true");
        ntfConfigBefore.put("shouldUseCallerInEventDescription()", "false");
        ntfConfigBefore.put("isBearingNetworkGsm()", "true");
        ntfConfigBefore.put("isBearingNetworkCdma2000()", "false");
        ntfConfigBefore.put("isBearingNetworkPstn()", "false");
        ntfConfigBefore.put("isMwiOffCheckCount()", "false");
        ntfConfigBefore.put("isSmppBindTransceiver()", "true");
        ntfConfigBefore.put("isSmsHandlerLoadBalanced()", "false");
        ntfConfigBefore.put("isSplitMwiAndSms()", "false");
        ntfConfigBefore.put("isAutoforwardedMessagesOn()", "false");
        ntfConfigBefore.put("getCallMwiCaller()", "0");
        ntfConfigBefore.put("getEsiFailureAction()", "1");
        ntfConfigBefore.put("getFaxPrintANumberPrefixSize()", "0");
        ntfConfigBefore.put("getFaxPrintSepSize()", "0");
        ntfConfigBefore.put("getFaxProvisioningPrefixSize()", "0");
        ntfConfigBefore.put("getImapTimeout()", "5000");
        ntfConfigBefore.put("getInternalQueueSize()", "256");
        ntfConfigBefore.put("getLogLevel()", "1");
        ntfConfigBefore.put("getLogSize()", "10000000");
        ntfConfigBefore.put("getMMSMaxConnection()", "10");
        ntfConfigBefore.put("getMaxTimeBeforeExpunge()", "300");
        ntfConfigBefore.put("getMaxXmpConnections()", "3");
        ntfConfigBefore.put("getMMSMaxVideoLength()", "-1");
        ntfConfigBefore.put("getNotifThreads()", "10");
        ntfConfigBefore.put("getNumberOfSms()", "5");
        ntfConfigBefore.put("getNumberingPlanIndicator()", "1");
        ntfConfigBefore.put("getPagerPauseTime()", "1000");
        ntfConfigBefore.put("getSlamdownMaxCallers()", "0");
        ntfConfigBefore.put("getSlamdownMaxCallsPerCaller()", "0");
        ntfConfigBefore.put("getMcnMaxCallers()", "0");
        ntfConfigBefore.put("getMcnMaxCallsPerCaller()", "0");
        ntfConfigBefore.put("getMcnLanguage()", "en");
        ntfConfigBefore.put("getSlamdownMaxDigitsInNumber()", "0");
        ntfConfigBefore.put("getMcnMaxDigitsInNumber()", "0");
        ntfConfigBefore.put("getSmeSourceNpi()", "0");
        ntfConfigBefore.put("getSmeSourceTon()", "0");
        ntfConfigBefore.put("getSmppVersion()", "52");
        ntfConfigBefore.put("getSmsMaxConn()", "20");
        ntfConfigBefore.put("getSmsStringLength()", "140");
        ntfConfigBefore.put("getSmsPriority()", "0");
        ntfConfigBefore.put("getSmscPollInterval()", "60");
        ntfConfigBefore.put("getSmsQueueSize()", "20");
        ntfConfigBefore.put("getSmscTimeout()", "30");
        ntfConfigBefore.put("getSlamdownConn()", "10");
        ntfConfigBefore.put("getTypeOfNumber()", "1");
        ntfConfigBefore.put("getUnreadMessageReminderInterval()", "86400");
        ntfConfigBefore.put("getUnreadMessageReminderMaxTimes()", "3");
        ntfConfigBefore.put("getVeryOldMessage()", "259200");
        ntfConfigBefore.put("getValidity_flash()", "-1");
        ntfConfigBefore.put("getValidity_smsType0()", "-1");
        ntfConfigBefore.put("getValidity_mwiOn()", "-1");
        ntfConfigBefore.put("getValidity_mwiOff()", "-1");
        ntfConfigBefore.put("getValidity_mailQuotaExceeded()", "-1");
        ntfConfigBefore.put("getValidity_temporaryGreetingOnReminder()", "-1");
        ntfConfigBefore.put("getValidity_voicemailOffReminder()", "-1");
        ntfConfigBefore.put("getValidity_cfuOnReminder()", "-1");
        ntfConfigBefore.put("getValidity_slamdown()", "-1");
        ntfConfigBefore.put("getValidity_mcn()", "-1");
        ntfConfigBefore.put("getValidity_vvm()", "-1");
        ntfConfigBefore.put("getJournalRefreshInterval()", "1800");
        ntfConfigBefore.put("getXmpPollInterval()", "90");
        ntfConfigBefore.put("getXmpTimeout()", "30");
        ntfConfigBefore.put("getXmpValidity()", "90");
        ntfConfigBefore.put("getXmpRefreshTime()", "30");
        ntfConfigBefore.put("getSmppErrorCodesIgnored()", "[I@201f9");
        ntfConfigBefore.put("getDelayAutoPrintFaxSMS()", "3");
        ntfConfigBefore.put("getSmeSourceAddress()", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getMmscUser()", "NoDefault");
        ntfConfigBefore.put("getMmscVaspId()", "NTF");
        ntfConfigBefore.put("getMmscVasId()", "ntf@142.133.116.118");
        ntfConfigBefore.put("getSnmpAgentPort()", "18001");
        ntfConfigBefore.put("getSnmpAgentTimeout()", "10");
        ntfConfigBefore.put("getSmscBackup(\"smsc\")", "null");
        ntfConfigBefore.put("getLoginFileValidityPeriod()", "360");
        ntfConfigBefore.put("getNotifRetrySchema()", "5 5 60 240 CONTINUE");
        ntfConfigBefore.put("getNotifExpireTimeInMin()", "4320");
        ntfConfigBefore.put("getNtfEventsRootPath()", "/opt/moip/events/ntf");
        ntfConfigBefore.put("getServiceListenerCorePoolSize()", "10");
        ntfConfigBefore.put("getServiceListenerMaxPoolSize()", "100");
        ntfConfigBefore.put("getCallMwiCaller(String name)", "0");

        ntfConfigBefore.put("getSourceAddress(\"cfuonreminder\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"email\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"fax\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"flash\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"mailquotaexceeded\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"mcn\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"mwioff\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"mwion\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"slamdown\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"smstype0\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"temporarygreetingonreminder\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"unreadmessagereminder\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"updatesms\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"video\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"voice\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"voicemailoffreminder\")", "{SMSAddress ,0,0}");
        ntfConfigBefore.put("getSourceAddress(\"vvm\")", "{SMSAddress ,0,0}");

        ntfConfigBefore.put("getValidity(\"cfuonreminder\")", "-1");
        ntfConfigBefore.put("getValidity(\"flash\")", "-1");
        ntfConfigBefore.put("getValidity(\"mailquotaexceeded\")", "-1");
        ntfConfigBefore.put("getValidity(\"mcn\")", "-1");
        ntfConfigBefore.put("getValidity(\"mwioff\")", "-1");
        ntfConfigBefore.put("getValidity(\"mwion\")", "-1");
        ntfConfigBefore.put("getValidity(\"slamdown\")", "-1");
        ntfConfigBefore.put("getValidity(\"smstype0\")", "-1");
        ntfConfigBefore.put("getValidity(\"temporarygreetingonreminder\")", "-1");
        ntfConfigBefore.put("getValidity(\"unreadmessagereminder\")", "-1");
        ntfConfigBefore.put("getValidity(\"updatesms\")", "-1");
        ntfConfigBefore.put("getValidity(\"voicemailoffreminder\")", "-1");
        ntfConfigBefore.put("getValidity(\"vvm\")", "-1");

        ntfConfigBefore.put("getValidity_cfuOnReminder()", "-1");
        ntfConfigBefore.put("getValidity_flash()", "-1");
        ntfConfigBefore.put("getValidity_mailQuotaExceeded()", "-1");
        ntfConfigBefore.put("getValidity_mcn()", "-1");
        ntfConfigBefore.put("getValidity_mwiOff()", "-1");
        ntfConfigBefore.put("getValidity_mwiOn()", "-1");
        ntfConfigBefore.put("getValidity_slamdown()", "-1");
        ntfConfigBefore.put("getValidity_smsType0()", "-1");
        ntfConfigBefore.put("getValidity_temporaryGreetingOnReminder()", "-1");
        ntfConfigBefore.put("getValidity_voicemailOffReminder()", "-1");
        ntfConfigBefore.put("getValidity_vvm()", "-1");

        ntfConfigBefore.put("getFallbackSms()", "outdial");
        ntfConfigBefore.put("getFallbackSmsContent()", "h");
        ntfConfigBefore.put("getFallbackSipMwi()", "outdial");

        return ntfConfigBefore;
    }

    private void assertBoolean(boolean currentResult, String previousResult) {
        System.out.println("currentResult: " + currentResult + " previousResult: " + previousResult);
        if (previousResult.equalsIgnoreCase("true") && currentResult) {
            return;
        } else if (previousResult.equalsIgnoreCase("false") && (currentResult == false)) {
            return;
        } else {
            System.out.println("CurrentResult " + currentResult + " is not equal to previousResult " + previousResult);
            assertTrue(1==0);
        }
    }

    private void assertString(String currentResult, String previousResult) {
        System.out.println("currentResult: " + currentResult + " previousResult: " + previousResult);
        if (currentResult == null && previousResult.equals("null")) {
            // OK, both are empty
        } else {
            assertTrue(currentResult.equals(previousResult));
        }
    }

    private void assertStringArray(String[] currentResult, String previousResult) {
        System.out.println("currentResult: " + currentResult + " previousResult: " + previousResult);

        List<String> previousList = new ArrayList<String>();
        StringTokenizer sToken = new StringTokenizer(previousResult, ",");
        while (sToken.hasMoreTokens()) {
            previousList.add(sToken.nextToken().trim());
        }

        for (String curResult : currentResult) {
            assertTrue(previousList.contains(curResult));
        }

        assertTrue(currentResult.length == previousList.size());
    }

    private void assertInt(int currentResult, String previousResult) {
        System.out.println("currentResult: " + currentResult + " previousResult: " + previousResult);
        assertTrue(currentResult == Integer.parseInt(previousResult));
    }
    
    
    private void assertHLrFailAction(hlrFailAction currentResult, String previousResult ) {
        System.out.println("currentResult: " + currentResult + " previousResult: " + previousResult);
        assertTrue(currentResult == hlrFailAction.valueOf(previousResult));
        
    }

    private void assertLong(long currentResult, String previousResult) {
        System.out.println("currentResult: " + currentResult + " previousResult: " + previousResult);
        assertTrue(currentResult == Long.parseLong(previousResult));
    }

    private void assertSMSAddress(SMSAddress currentResult, String previousResult) {
        System.out.println("currentResult: " + currentResult + " previousResult: " + previousResult);
        try {
            SMSAddress previousResultSMSAddress = new SMSAddress(previousResult);
            assertTrue(currentResult.equals(previousResultSMSAddress));
        } catch (SMSComDataException scde) {
            ;
        }
    }
}
