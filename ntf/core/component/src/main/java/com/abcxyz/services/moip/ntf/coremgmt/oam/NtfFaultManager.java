package com.abcxyz.services.moip.ntf.coremgmt.oam;


import com.abcxyz.messaging.common.oam.AlarmEvent;
import com.abcxyz.messaging.common.oam.FaultManager;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.alarms.MoipAlarmEvent;
import com.abcxyz.services.moip.alarms.MoipAlarmFactory;
import com.mobeon.common.cmnaccess.oam.MoipFaultManager;
import com.mobeon.common.logging.LogAgentFactory;

/**
 * class as messaging core fault manager plug-in for receiving all alarms and notifications from core components
 *
 * @author lmchuzh
 */
public class NtfFaultManager extends  MoipFaultManager {

	private LogAgent logger = LogAgentFactory.getLogAgent(NtfFaultManager.class);
	private FaultManager faultManager;

	public void setFaultManager(FaultManager faultManager){
		this.faultManager = faultManager;
	}

	@Override
	public void clearAlarm(AlarmEvent alarm) {

		String name = alarm.getAlarmID();
		MoipAlarmEvent moipAlarm = MoipAlarmFactory.getInstance().getAlarm(name);
		faultManager.clearAlarm(moipAlarm);

	}

	@Override
	public void raiseAlarm(AlarmEvent alarm) {

		String name = alarm.getAlarmID();
		MoipAlarmEvent moipAlarm = MoipAlarmFactory.getInstance().getAlarm(name);
		faultManager.raiseAlarm(moipAlarm);
	}

	@Override
	public void raiseAlarm(AlarmEvent alarm, String specificText) {

		logger.info("NtfFaultManager raised alarm: " + alarm.getAlarmID() + " with specific text: " + specificText);
		raiseAlarm(alarm);
	}

}
