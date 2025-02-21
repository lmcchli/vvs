package com.mobeon.masp.execution_engine.platformaccess;

import java.util.Properties;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.ssmg.interfaces.AlertSCHandler;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;


/**
 * As a user of the ss7 stack, we can potentially receive
 * alert sc invokes even though they were requested by NTF.
 * We need to inform NTF that the phone is back on.
 * @author lmcdwng
 *
 */

public class AlertScPhoneOnHandler implements AlertSCHandler {
	private static AlertScPhoneOnHandler instance = new AlertScPhoneOnHandler();
	private LogAgent log;

	public AlertScPhoneOnHandler(){
		log = CommonOamManager.getInstance().getSs7Oam().getLogAgent();
	}

	public static AlertScPhoneOnHandler getInstance() {
        if (instance == null) {
            instance = new AlertScPhoneOnHandler();
        }
        return instance;
    }

	@Override
	public void handleResult(String address) {

		if(log.isDebugEnabled()){
			log.debug("AlertScPhoneOnHandler.handleResult: received alert sc for : " + address);
		}

		MessageInfo msgInfo = new MessageInfo();
		MSA msa = CommonMessagingAccess.getInstance().getMsid(address);
		msgInfo.rmsa = msa;
        Properties properties = new Properties();
        CommonMessagingAccess.getInstance().notifyNtf(EventTypes.ALERT_SC, msgInfo, address, properties);

		if(log.isDebugEnabled()){
			log.debug("AlertScPhoneOnHandler.handleResult: received alert sc for : " + address);
		}
	}
}
