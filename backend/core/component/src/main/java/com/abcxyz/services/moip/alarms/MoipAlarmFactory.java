package com.abcxyz.services.moip.alarms;

import java.util.HashMap;

public class MoipAlarmFactory {

    static public enum MoipAlarm {

        //Core alarms
        Mfs_NFS_CONNECTION_PROBLEM("MFS.NFS_CONNECTION_PROBLEM", "MFS.NFS_CONNECTION_PROBLEM"),
        Scheduler_EVENTS_MOUNT_POINT_LOST("Scheduler.EVENTS_MOUNT_POINT_LOST", "Scheduler.EVENTS_MOUNT_POINT_LOST"),
        SessionManager_SessionFileAccessProblem("SessionManager.SessionFileAccessProblem", "SessionManager.SessionFileAccessProblem"),
        CDRGen_CannotWriteASCIIFile("CDRGen.CannotWriteASCIIFile", "CDRGen.CannotWriteASCIIFile"),
        CDRGen_CannotWriteASN1File("CDRGen.CannotWriteASN1File", "CDRGen.CannotWriteASN1File"),
        CDRGen_RadiusTransmitProblem("CDRGen.RadiusTransmitProblem","CDRGen.RadiusTransmitProblem"),
        TaskExecutor_QueueFull("TaskExecutor.QueueFull", "TaskExecutor.QueueFull"),
        TaskExecutor_TaskRejectedAlarm("TaskExecutor.TaskRejectedAlarm", "TaskExecutor.TaskRejectedAlarm"),
        MRD_REMOTE_DISPATCHER_PROTOCOL_ERROR("MRD.REMOTE_DISPATCHER_PROTOCOL_ERROR","MRD.REMOTE_DISPATCHER_PROTOCOL_ERROR"),
        LicenseManagerAPI_LicenseLimitExceeded("LicenseManagerAPI.LicenseLimitExceeded","LicenseManagerAPI.LicenseLimitExceeded"),
        LicenseManagerAPI_LicenseCorruptedOrExpired("LicenseManagerAPI.LicenseCorruptedOrExpired", "LicenseManagerAPI.LicenseCorruptedOrExpired"),
        LicenseManagerAPI_LicenseNotAcquired ("LicenseManagerAPI.LicenseNotAcquired","LicenseManagerAPI.LicenseNotAcquired"),
        ScriptEngine_SE_PLUGIN_EXECUTE_EXCEPTION ("ScriptEngine.SE_PLUGIN_EXECUTE_EXCEPTION", "ScriptEngine.SE_PLUGIN_EXECUTE_EXCEPTION"),
        MCDProxyService_McdProxyUnavailableAlarm ("MCDProxyService.McdProxyUnavailableAlarm", "MCDProxyService.McdProxyUnavailableAlarm"),
        CAI3GClient_CASUnavailable ("CAI3GClient.CASUnavailable", "CAI3GClient.CASUnavailable"),
        CDRGen_CannotAuthenticateRadiusAccounting ("CDRGen.CannotAuthenticateRadiusAccounting", "CDRGen.CannotAuthenticateRadiusAccounting"),
        CDRGen_CannotReadASCIIFile ("CDRGen.CannotReadASCIIFile", "CDRGen.CannotReadASCIIFile"),
        CDRGen_CannotReadASN1File ("CDRGen.CannotReadASN1File", "CDRGen.CannotReadASN1File"),
        CDRGen_ConfigurationProblem ("CDRGen.ConfigurationProblem", "CDRGen.ConfigurationProblem"),
        CDRGen_RadiusConnectionProblem ("CDRGen.RadiusConnectionProblem", "CDRGen.RadiusConnectionProblem"),
        DISPATCHER_CONFIG_LOAD_FAIL ("DISPATCHER_CONFIG_LOAD_FAIL", "DISPATCHER_CONFIG_LOAD_FAIL"),
        SS7_Stack_Connection("SS7StackConnection", "SS7StackConnection"),
        SSMG_Communication_Raised("114", "Unable to communicate with SSMG or SS7 stack"),
        SSMG_Communication_Cleared("115", "SSMG or SS7 stack communications is up"),

        //Moip alarms defined in moip faultlist.conf
        COMPONENT_HIGH_LOAD ("HighLoadOnComponent"),
        SMS_CONNECTION("SmsConnection"),
        MMS_CONNECTION("MmsConnection"),
        FAX_CONNECTION("FaxConnection"),
        EMAIL_CONNECTION("EmailConnection"),
        CONFIG_LOADING_FAILED("ConfigLoadingFailed");

        ;

        private String name;
        private String description;

        MoipAlarm(String name) {
            //Take description from faultlist.conf
            this.name = name;
        }

        MoipAlarm(String name, String description) {
            this.description = description;
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public String getName() {
            return name;
        }

    }

    protected static HashMap<String, MoipAlarm> supportedAlarms = new HashMap<String, MoipAlarm>();

    private volatile static MoipAlarmFactory moipAlarmFactory;

    public static MoipAlarmFactory getInstance() {
        if (moipAlarmFactory == null) {
            synchronized(MoipAlarmFactory.class){
                if(moipAlarmFactory == null) {
                    moipAlarmFactory = new MoipAlarmFactory();
                }
            }
        }
        return moipAlarmFactory;
    }


    private MoipAlarmFactory() {
        supportedAlarms.put(MoipAlarm.Mfs_NFS_CONNECTION_PROBLEM.getName(), MoipAlarm.Mfs_NFS_CONNECTION_PROBLEM);
        supportedAlarms.put(MoipAlarm.Scheduler_EVENTS_MOUNT_POINT_LOST.getName(), MoipAlarm.Scheduler_EVENTS_MOUNT_POINT_LOST);
        supportedAlarms.put(MoipAlarm.SessionManager_SessionFileAccessProblem.getName(), MoipAlarm.SessionManager_SessionFileAccessProblem);
        supportedAlarms.put(MoipAlarm.CDRGen_CannotWriteASCIIFile.getName(), MoipAlarm.CDRGen_CannotWriteASCIIFile);
        supportedAlarms.put(MoipAlarm.CDRGen_CannotWriteASN1File.getName(), MoipAlarm.CDRGen_CannotWriteASN1File);
        supportedAlarms.put(MoipAlarm.CDRGen_RadiusTransmitProblem.getName(), MoipAlarm.CDRGen_RadiusTransmitProblem);
        supportedAlarms.put(MoipAlarm.TaskExecutor_QueueFull.getName(), MoipAlarm.TaskExecutor_QueueFull);
        supportedAlarms.put(MoipAlarm.TaskExecutor_TaskRejectedAlarm.getName(), MoipAlarm.TaskExecutor_TaskRejectedAlarm);
        supportedAlarms.put(MoipAlarm.MRD_REMOTE_DISPATCHER_PROTOCOL_ERROR.getName(), MoipAlarm.MRD_REMOTE_DISPATCHER_PROTOCOL_ERROR);
        supportedAlarms.put(MoipAlarm.LicenseManagerAPI_LicenseLimitExceeded.getName(), MoipAlarm.LicenseManagerAPI_LicenseLimitExceeded);
        supportedAlarms.put(MoipAlarm.LicenseManagerAPI_LicenseCorruptedOrExpired.getName(), MoipAlarm.LicenseManagerAPI_LicenseCorruptedOrExpired);
        supportedAlarms.put(MoipAlarm.LicenseManagerAPI_LicenseNotAcquired.getName(), MoipAlarm.LicenseManagerAPI_LicenseNotAcquired);
        supportedAlarms.put(MoipAlarm.ScriptEngine_SE_PLUGIN_EXECUTE_EXCEPTION.getName(), MoipAlarm.ScriptEngine_SE_PLUGIN_EXECUTE_EXCEPTION);
        supportedAlarms.put(MoipAlarm.MCDProxyService_McdProxyUnavailableAlarm.getName(), MoipAlarm.MCDProxyService_McdProxyUnavailableAlarm);
        supportedAlarms.put(MoipAlarm.CAI3GClient_CASUnavailable.getName(), MoipAlarm.CAI3GClient_CASUnavailable);
        supportedAlarms.put(MoipAlarm.CDRGen_CannotAuthenticateRadiusAccounting.getName(), MoipAlarm.CDRGen_CannotAuthenticateRadiusAccounting);
        supportedAlarms.put(MoipAlarm.CDRGen_CannotReadASCIIFile.getName(), MoipAlarm.CDRGen_CannotReadASCIIFile);
        supportedAlarms.put(MoipAlarm.CDRGen_CannotReadASN1File.getName(), MoipAlarm.CDRGen_CannotReadASN1File);
        supportedAlarms.put(MoipAlarm.CDRGen_ConfigurationProblem.getName(), MoipAlarm.CDRGen_ConfigurationProblem);
        supportedAlarms.put(MoipAlarm.CDRGen_RadiusConnectionProblem.getName(), MoipAlarm.CDRGen_RadiusConnectionProblem);
        supportedAlarms.put(MoipAlarm.DISPATCHER_CONFIG_LOAD_FAIL.getName(), MoipAlarm.DISPATCHER_CONFIG_LOAD_FAIL);
        supportedAlarms.put(MoipAlarm.SS7_Stack_Connection.getName(), MoipAlarm.SS7_Stack_Connection);
        supportedAlarms.put(MoipAlarm.SSMG_Communication_Raised.getName(), MoipAlarm.SSMG_Communication_Raised);
        supportedAlarms.put(MoipAlarm.SSMG_Communication_Cleared.getName(), MoipAlarm.SSMG_Communication_Cleared);

        supportedAlarms.put(MoipAlarm.COMPONENT_HIGH_LOAD.getName(), MoipAlarm.COMPONENT_HIGH_LOAD);
        supportedAlarms.put(MoipAlarm.SMS_CONNECTION.getName(), MoipAlarm.SMS_CONNECTION);
        supportedAlarms.put(MoipAlarm.MMS_CONNECTION.getName(), MoipAlarm.MMS_CONNECTION);
        supportedAlarms.put(MoipAlarm.FAX_CONNECTION.getName(), MoipAlarm.FAX_CONNECTION);
        supportedAlarms.put(MoipAlarm.EMAIL_CONNECTION.getName(), MoipAlarm.EMAIL_CONNECTION);
        supportedAlarms.put(MoipAlarm.CONFIG_LOADING_FAILED.getName(), MoipAlarm.CONFIG_LOADING_FAILED);

    }


    public MoipAlarmEvent getAlarm(MoipAlarm moipAlarm) {
        return getAlarm(moipAlarm.getName());
    }

    public MoipAlarmEvent getAlarm(String alarmName) {
        MoipAlarmEvent alarm = null;

        MoipAlarm a = supportedAlarms.get(alarmName);
        if(a != null) {

            alarm = new MoipAlarmEvent(a.getName());
            alarm.setDescription(a.getDescription());
        }else {
            //create a dummy alarm so oe framework does not throw a null pointer exception
            alarm = new MoipAlarmEvent("dummy");
        }

        return alarm;
    }

}
