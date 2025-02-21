/*
* COPYRIGHT Abcxyz Communication Inc. Montreal 2009
* The copyright to the computer program(s) herein is the property
* of ABCXYZ Communication Inc. Canada. The program(s) may be used
* and/or copied only with the written permission from ABCXYZ
* Communication Inc. or in accordance with the terms and conditions
* stipulated in the agreement/contact under which the program(s)
* have been supplied.
*---------------------------------------------------------------------
* Created on 21-Apr-2009
*/
package com.mobeon.masp.operateandmaintainmanager;

import com.abcxyz.messaging.common.oam.AlarmEvent;
import com.abcxyz.messaging.common.oam.FaultManager;
import com.abcxyz.services.moip.alarms.MoipAlarmEvent;
import com.abcxyz.services.moip.alarms.MoipAlarmFactory;
import com.mobeon.common.cmnaccess.oam.MoipFaultManager;


/**
 * MasFaultManager controls alarm status for the MAS component.
 * <p>
 * It is called to raise and clear alarms of MAS. Use the <code>clearAlarm</code> and
 * <code>raiseAlarm</code> methods to control the status of alarms.
 * </p>
 *
 * @author egeobli
 * @see {@link MoipFaultManager}
 */
public class MasFaultManager extends MoipFaultManager {

	private FaultManager faultManager;

	public MasFaultManager() {
	}

	public void setFaultManager(FaultManager faultManager) {
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

		logger.info("MasFaultManager raised alarm: " + alarm.getAlarmID() + " with specific text: " + specificText);
		raiseAlarm(alarm);
	}
}
