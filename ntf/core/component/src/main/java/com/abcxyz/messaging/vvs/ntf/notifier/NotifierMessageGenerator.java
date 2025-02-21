/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.HashMap;
import java.util.Map;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.ANotifierSlamdownCallInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.INotifierMessageGenerator;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.NotifierMessageGenerationException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.NotifierMessageGenerationException.MessageGenerationExceptionCause;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.slamdown.CallerInfo;
import com.mobeon.ntf.text.Phrases;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.text.TextCreator;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.mcd.McdUserInfo;


public class NotifierMessageGenerator implements INotifierMessageGenerator {

    private static LogAgent log = NtfCmnLogger.getLogAgent(NotifierMessageGenerator.class);
    private static NotifierMessageGenerator instance = null;
    
    private NotifierMessageGenerator() {        
    }
    
    public static NotifierMessageGenerator get() {
        if(instance == null) {
            instance = new NotifierMessageGenerator();
        }
        return instance;
    }

    @Override
    public String generateNotificationMessageAsString(String templateName, ANotifierNotificationInfo notificationInfo, ANotifierDatabaseSubscriberProfile subscriberProfile, boolean isFallbackMessageUsed) 
            throws NotifierMessageGenerationException {

        String generatedText = null;
        try {
            UserInfo userInfo = null;
            UserMailbox mailbox = null;
            if(subscriberProfile != null) {
                userInfo = new McdUserInfo(new NotifierDirectoryAccessSubscriber(subscriberProfile));
                String msid = userInfo.getMsid();
                if(msid != null) {
                    MSA msa = new MSA(msid);
                    mailbox = new UserMailbox(msa,
                            userInfo.hasMailType(Constants.NTF_EMAIL),
                            userInfo.hasMailType(Constants.NTF_FAX),
                            userInfo.hasMailType(Constants.NTF_VOICE),
                            userInfo.hasMailType(Constants.NTF_VIDEO));
                }
            }
            generatedText = TextCreator.get().generateText(mailbox, userInfo, templateName, isFallbackMessageUsed, notificationInfo);
            
        } catch (TemplateMessageGenerationException e) {
            String errorMsg = "Notification message generation (with INotifierNotificationInfo) failed using template " + templateName + ": " + e.getMessage();
            log.error(errorMsg);
            throw new NotifierMessageGenerationException(errorMsg, getMessageGenerationExceptionCause(e));
        } catch (Throwable t) {
            String errorMsg = "Notification message generation (with INotifierNotificationInfo) failed: " + t.getMessage();
            if(isFallbackMessageUsed) {
                log.warn("Returning hard-coded fallback message.  " + errorMsg, t);
                generatedText = "New message";
            } else {
                log.error(errorMsg, t);
                throw new NotifierMessageGenerationException(errorMsg);
            }
        }
        return generatedText;
    }
    
    @Override
    public byte[] generateNotificationMessageAsBytes(String charsetName, String templateName, ANotifierNotificationInfo notificationInfo, ANotifierDatabaseSubscriberProfile subscriberProfile, boolean isFallbackMessageUsed) 
            throws NotifierMessageGenerationException {

        byte[] generatedBytes = null;
        try {
            UserInfo userInfo = null;
            UserMailbox mailbox = null;
            if(subscriberProfile != null) {
                userInfo = new McdUserInfo(new NotifierDirectoryAccessSubscriber(subscriberProfile));
                String msid = userInfo.getMsid();
                if(msid != null) {
                    MSA msa = new MSA(msid);
                    mailbox = new UserMailbox(msa,
                            userInfo.hasMailType(Constants.NTF_EMAIL),
                            userInfo.hasMailType(Constants.NTF_FAX),
                            userInfo.hasMailType(Constants.NTF_VOICE),
                            userInfo.hasMailType(Constants.NTF_VIDEO));
                }
            }
            generatedBytes = TextCreator.get().generateBytes(charsetName, mailbox, userInfo, templateName, isFallbackMessageUsed, notificationInfo);
            
        } catch (TemplateMessageGenerationException e) {
            String errorMsg = "Notification message generation (with INotifierNotificationInfo) failed using template " + templateName + " and charset " + charsetName + ": " + e.getMessage();
            log.error(errorMsg);
            throw new NotifierMessageGenerationException(errorMsg, getMessageGenerationExceptionCause(e));
        } catch (Throwable t) {
            String errorMsg = "Notification message generation (with INotifierNotificationInfo) failed: " + t.getMessage();
            if(isFallbackMessageUsed) {
                log.warn("Returning hard-coded fallback message.  " + errorMsg, t);
                generatedBytes = "New message".getBytes();
            } else {
                log.error(errorMsg, t);
                throw new NotifierMessageGenerationException(errorMsg);
            }
        }
        return generatedBytes;
    }

    @Override
    public String generateSlamdownNotificationMessageAsString(String templateName, ANotifierSlamdownCallInfo slamdownCallInfo, ANotifierDatabaseSubscriberProfile subscriberProfile, boolean isFallbackMessageUsed)
            throws NotifierMessageGenerationException {

        String generatedText = null;
        try {
            CallerInfo callerInfo = CallerInfo.create(slamdownCallInfo.getCaller(), true, true);
            callerInfo.voiceSlamdown(slamdownCallInfo.getDate());

            if (slamdownCallInfo.getProperties() != null) {
                Map<String, String> slamdownInfoProperties = new HashMap<String, String>();
                for (Map.Entry<Object, Object> entry: slamdownCallInfo.getProperties().entrySet()) {
                    log.debug("generateSlamdownNotificationMessageAsString: Setting CallerInfo SlamdownInfoProperty: " + (String)entry.getKey() + " Value: " + (String)entry.getValue());
                    slamdownInfoProperties.put((String)entry.getKey(), (String)entry.getValue());
                }
                callerInfo.setSlamdownInfoProperties(slamdownInfoProperties);
            }

            UserInfo userInfo = null;
            UserMailbox mailbox = null;
            if(subscriberProfile != null) {
                userInfo = new McdUserInfo(new NotifierDirectoryAccessSubscriber(subscriberProfile));
                String msid = userInfo.getMsid();
                if(msid != null) {
                    MSA msa = new MSA(msid);
                    mailbox = new UserMailbox(msa,
                            userInfo.hasMailType(Constants.NTF_EMAIL),
                            userInfo.hasMailType(Constants.NTF_FAX),
                            userInfo.hasMailType(Constants.NTF_VOICE),
                            userInfo.hasMailType(Constants.NTF_VIDEO));
                }
            }
            generatedText = TextCreator.get().generateText(mailbox, null, userInfo, templateName, isFallbackMessageUsed, callerInfo);
            
        } catch (TemplateMessageGenerationException e) {
            String errorMsg = "Notification message generation (with INotifierSlamdownCallInfo) failed using template " + templateName + ": " + e.getMessage();
            log.error(errorMsg);
            throw new NotifierMessageGenerationException(errorMsg, getMessageGenerationExceptionCause(e));
        } catch (Throwable t) {
            String errorMsg = "Notification message generation (with INotifierSlamdownCallInfo) failed: " + t.getMessage();
            if(isFallbackMessageUsed) {
                log.warn("Returning hard-coded fallback message.  " + errorMsg);
                generatedText = "New message";
            } else {
                log.error(errorMsg);
                throw new NotifierMessageGenerationException(errorMsg);
            }
        }
        return generatedText;
    }

    @Override
    public byte[] generateSlamdownNotificationMessageAsBytes(String charsetName, String templateName, ANotifierSlamdownCallInfo slamdownCallInfo, ANotifierDatabaseSubscriberProfile subscriberProfile, boolean isFallbackMessageUsed) 
            throws NotifierMessageGenerationException {

        byte[] generatedBytes = null;
        try {
            CallerInfo callerInfo = CallerInfo.create(slamdownCallInfo.getCaller(), true, true);
            callerInfo.voiceSlamdown(slamdownCallInfo.getDate());
            
            if (slamdownCallInfo.getProperties() != null) {
                Map<String, String> slamdownInfoProperties = new HashMap<String, String>();
                for (Map.Entry<Object, Object> entry: slamdownCallInfo.getProperties().entrySet()) {
                    log.debug("generateSlamdownNotificationMessageAsBytes: Setting CallerInfo SlamdownInfoProperty: " + (String)entry.getKey() + " Value: " + (String)entry.getValue());
                    slamdownInfoProperties.put((String)entry.getKey(), (String)entry.getValue());
                }
                callerInfo.setSlamdownInfoProperties(slamdownInfoProperties);
            }

            UserInfo userInfo = null;
            UserMailbox mailbox = null;
            if(subscriberProfile != null) {
                userInfo = new McdUserInfo(new NotifierDirectoryAccessSubscriber(subscriberProfile));
                String msid = userInfo.getMsid();
                if(msid != null) {
                    MSA msa = new MSA(msid);
                    mailbox = new UserMailbox(msa,
                            userInfo.hasMailType(Constants.NTF_EMAIL),
                            userInfo.hasMailType(Constants.NTF_FAX),
                            userInfo.hasMailType(Constants.NTF_VOICE),
                            userInfo.hasMailType(Constants.NTF_VIDEO));
                }
            }
            generatedBytes = TextCreator.get().generateBytes(charsetName, mailbox, null, userInfo, templateName, isFallbackMessageUsed, callerInfo);
            
        } catch (TemplateMessageGenerationException e) {
            String errorMsg = "Notification message generation (with INotifierSlamdownCallInfo) failed using template " + templateName + " and charset " + charsetName + ": " + e.getMessage();
            log.error(errorMsg);
            throw new NotifierMessageGenerationException(errorMsg, getMessageGenerationExceptionCause(e));
        } catch (Throwable t) {
            String errorMsg = "Notification message generation (with INotifierSlamdownCallInfo) failed: " + t.getMessage();
            if(isFallbackMessageUsed) {
                log.warn("Returning hard-coded fallback message.  " + errorMsg);
                generatedBytes = "New message".getBytes();
            } else {
                log.error(errorMsg);
                throw new NotifierMessageGenerationException(errorMsg);
            }
        }
        return generatedBytes;
    }


    public MessageGenerationExceptionCause getMessageGenerationExceptionCause(TemplateMessageGenerationException templateMessageGenerationException) {
        MessageGenerationExceptionCause cause = MessageGenerationExceptionCause.NO_CAUSE_SPECIFIED;
        switch(templateMessageGenerationException.getTemplateExceptionCause()) {
            case CAUSE_PAYLOAD_FILE_DOES_NOT_EXIST:
                cause = MessageGenerationExceptionCause.PAYLOAD_FILE_DOES_NOT_EXIST;
                break;
            case CAUSE_PAYLOAD_FILE_NOT_ACCESSIBLE:
                cause = MessageGenerationExceptionCause.PAYLOAD_FILE_NOT_ACCESSIBLE;
                break;
        }
        return cause;
    }

    public boolean doesCphrTemplateExist(String templateName) {
        return Phrases.isCphrPhraseFound(Config.getDefaultLanguage(), templateName, null);
    }

    public boolean doesCphrTemplateExistforCharSet(String templateName, String charsetName) {
        return Phrases.isCphrPhraseFound(charsetName, templateName, null);
    }

}