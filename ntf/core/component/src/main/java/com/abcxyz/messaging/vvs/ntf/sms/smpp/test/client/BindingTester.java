package com.abcxyz.messaging.vvs.ntf.sms.smpp.test.client;

import java.util.Iterator;
import java.util.Map;

import com.mobeon.common.sms.SMSClient;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.out.sms.SMSConfigWrapper;
import com.mobeon.ntf.out.sms.SMSOut;

public class BindingTester {
    private SMSClient smsClient = null;

    BindingTester() {
        this.smsClient = SMSClient.get(SMSOut.get(), new SMSConfigWrapper());
    }

    /**
     * Sets the minimum receiver connections that must be established with the smsc.
     * @param minReceiverConnections Specifies the number of minimum connections.
     */
    void setMinReceiverConnections(String minReceiverConnections) {
        //Config.setCfgVar(NotificationConfigConstants.SMS_MIN_RECEIVER_CONN, minReceiverConnections);
        SMPPTestClientLogger
                .writeLogMessageToFile("BindingTester.setMinReceiverConnections(): Set the minimum receiver connections to: "
                        + minReceiverConnections + ".\n");
    }

    /**
     * Sets the maximum receiver connections that can be established with the smsc.
     * @param maxReceiverConnections Specifies the number of maximum connections.
     */
    void setMaxReceiverConnections(String maxReceiverConnections) {
        //Config.setCfgVar(NotificationConfigConstants.SMS_MAX_RECEIVER_CONN, maxReceiverConnections);
        SMPPTestClientLogger
                .writeLogMessageToFile("BindingTester.setMaxReceiverConnections(): Set the maximum receiver connections to: "
                        + maxReceiverConnections + ".\n");
    }

    /**
     * Sets the minimum transmitter/transceiver connections that can be established with the smsc.
     * @param minTransmitterConnections Specifies the minimum number of connections.
     */
    void setMinTransmitterConnections(String minTransmitterConnections) {
        Config.setCfgVar(NotificationConfigConstants.SMS_MIN_CONN, minTransmitterConnections);
        SMPPTestClientLogger
                .writeLogMessageToFile("BindingTester.setMinTransmitterConnections(): Set the minimum transmitter connections to: "
                        + minTransmitterConnections + ".\n");
    }

    /**
     * Sets the maximum transmitter/transceiver connections that can be established with the smsc.
     * @param maxTransmitterConnections Specifies the maximum number of connections.
     */
    void setMaxTransmitterConnections(String maxTransmitterConnections) {
        Config.setCfgVar(NotificationConfigConstants.SMS_MAX_CONN, maxTransmitterConnections);
        SMPPTestClientLogger
                .writeLogMessageToFile("BindingTester.setMaxTransmitterConnections(): Set the maximum transmitter connections to: "
                        + maxTransmitterConnections + ".\n");
    }

    /**
     * Binds to a single SMSC.
     * @param smsc The name of the SMSC to bind to.
     */
    void bindToSMSC(String smsc) {
        Map<String, Map<String, String>> shortMessageInstances = Config
                .getExternalEnablers(NotificationConfigConstants.SHORT_MESSAGE_TABLE);
        Iterator<String> instanceItr = shortMessageInstances.keySet().iterator();
        while (instanceItr.hasNext()) {
            String instanceName = instanceItr.next();
            if (instanceName.equalsIgnoreCase(smsc)) {
                Map<String, String> instanceInfo = shortMessageInstances.get(instanceName);
                instanceInfo.put(NotificationConfigConstants.AVAILABILITY, SMSClient.CONFIGURED_SMSC_AVAILABILITY_AVAILABLE);
            }
        }
        this.smsClient.refreshSmsUnits();
        SMPPTestClientLogger.writeLogMessageToFile("BindingTester.bindToSmsc(): Attempting to bind to the smsc: " + smsc + "\n");
    }

    /**
     * Binds to all SMSCs specified in the ShortMessage.Table.
     */
    void bindToAllSMSCs() {
        Map<String, Map<String, String>> shortMessageInstances = Config
                .getExternalEnablers(NotificationConfigConstants.SHORT_MESSAGE_TABLE);
        Iterator<String> instanceItr = shortMessageInstances.keySet().iterator();
        while (instanceItr.hasNext()) {
            String instanceName = instanceItr.next();
            Map<String, String> instanceInfo = shortMessageInstances.get(instanceName);
            instanceInfo.put(NotificationConfigConstants.AVAILABILITY, SMSClient.CONFIGURED_SMSC_AVAILABILITY_AVAILABLE);
        }
        this.smsClient.refreshSmsUnits();
        SMPPTestClientLogger
                .writeLogMessageToFile("BindingTester.bindToAllSmscs(): Attempting to bind to the SMSCs specified in the ShortMessage.Table.\n");
    }

    /**
     * Unbinds from a single SMSC.
     * @param smsc The name of the SMSC to unbind from.
     */
    void unbindFromSMSC(String smsc) {
        Map<String, Map<String, String>> shortMessageInstances = Config
                .getExternalEnablers(NotificationConfigConstants.SHORT_MESSAGE_TABLE);
        Iterator<String> instanceItr = shortMessageInstances.keySet().iterator();
        while (instanceItr.hasNext()) {
            String instanceName = instanceItr.next();
            if (instanceName.equalsIgnoreCase(smsc)) {
                Map<String, String> instanceInfo = shortMessageInstances.get(instanceName);
                instanceInfo.put(NotificationConfigConstants.AVAILABILITY, SMSClient.CONFIGURED_SMSC_AVAILABILITY_UNAVAILABLE);
                System.out.println("Made unavailable");
            }
        }
        this.smsClient.refreshSmsUnits();
        SMPPTestClientLogger.writeLogMessageToFile("BindingTester.unbindFromSmsc(): Attempting to unbindbind from the smsc: "
                + smsc + "\n");
    }

    /**
     * Unbinds from all SMSCs specified in the ShortMessage.Table.
     */
    void unbindFromAllSMSCs() {
        Map<String, Map<String, String>> shortMessageInstances = Config
                .getExternalEnablers(NotificationConfigConstants.SHORT_MESSAGE_TABLE);
        Iterator<String> instanceItr = shortMessageInstances.keySet().iterator();
        while (instanceItr.hasNext()) {
            String instanceName = instanceItr.next();
            Map<String, String> instanceInfo = shortMessageInstances.get(instanceName);
            instanceInfo.put(NotificationConfigConstants.AVAILABILITY, SMSClient.CONFIGURED_SMSC_AVAILABILITY_UNAVAILABLE);
        }
        this.smsClient.refreshSmsUnits();
        SMPPTestClientLogger
                .writeLogMessageToFile("BindingTester.unbindFromAllSmscs(): Attempting to unbind from the SMSCs specified in the ShortMessage.Table.\n");
    }
}
