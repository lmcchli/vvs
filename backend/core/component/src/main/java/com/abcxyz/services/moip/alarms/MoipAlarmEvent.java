package com.abcxyz.services.moip.alarms;

import com.abcxyz.messaging.common.oam.AlarmEvent;
import com.abcxyz.messaging.common.oam.AlarmSeverity;

public class MoipAlarmEvent implements AlarmEvent {

	private String alarmId;
	private String description;
	private int instance;
	private String senderId;
	private AlarmSeverity severity;
	private String operatorId;

	public MoipAlarmEvent(String alarmId, String description, int instance, String senderId, AlarmSeverity severity, String operatorId){
		this.alarmId = alarmId;
		this.description = description;
		this.instance = instance;
		this.senderId = senderId;
		this.severity = severity;
		this.operatorId = operatorId;
	}

	public MoipAlarmEvent(String alarmId, String description, int instance, String operatorId){
		this.alarmId = alarmId;
		this.description = description;
		this.instance = instance;
		this.operatorId = operatorId;
	}
	
	
	public MoipAlarmEvent(String alarmId){
		this.alarmId = alarmId;
	}

	@Override
	public String getAlarmID() {
		return alarmId;
	}

	public void setAlarmID(String id){
		alarmId = id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description){
		this.description = description;
	}

	@Override
	public int getInstanceNo() {
		return instance;
	}

	public void setInstanceNo(int instanceNo) {
		this.instance = instanceNo;
	}

	@Override
	public String getOperatorID() {
		return operatorId;
	}

	public void setOperatorID(String operatorId){
		this.operatorId = operatorId;
	}

	@Override
	public String getSenderID() {
		return senderId;
	}

	public void setSenderID(String senderId) {
		this.senderId = senderId;
	}

	@Override
	public AlarmSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(AlarmSeverity severity) {
		this.severity = severity;
	}

}
