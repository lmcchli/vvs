package com.mobeon.masp.rpcclient;

import com.mobeon.masp.operateandmaintainmanager.OperationalState;

import java.util.Date;
import java.util.Vector;
import java.util.Calendar;
import java.io.Serializable;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */
// implements Serializable {
public class MasMibAttributes implements Serializable {
	public String  masName = "";
    public String  masVersion ="";
    public OperationalState  masOperationalState = OperationalState.DISABLED;
    public String  masAdministrativeState="";
    public Date    masInstallDate = new Date();
    public Long    masCurrentUpTime;
    public Long    masAccumulatedUpTime;
    public long    masReloadConfiguration = ReloadConfiguration.OK.getIndex();
    public Date    masReloadConfigurationTime = new Date();

    public Vector<MasMibProvidedServices>  providedServices;
    public Vector<MasMibConsumedServices>  consumedServices;
    public Vector<MasMibServiceEnabler>  serviceEnablers;
    public Vector<MasMibCommonAlarm> commonAlarms;

    private Date startTime;

	private static final long serialVersionUID = 1L;

	public MasMibAttributes () {
        Calendar cal = Calendar.getInstance();
        this.startTime = cal.getTime();

        providedServices = new Vector<MasMibProvidedServices>();
        consumedServices = new Vector<MasMibConsumedServices>();
        serviceEnablers = new Vector<MasMibServiceEnabler>();
        commonAlarms = new Vector<MasMibCommonAlarm>();
    }

//    public Long masCurrentUpTime(){
//        return  (Calendar.getInstance().getTimeInMillis() - startTime.getTime());
//    }

//    public Long masAccumulatedUpTime(){
//        Calendar cal = Calendar.getInstance();
//        return  (cal.getTimeInMillis() - masInstallDate.getTime());
//        //return Long.getLong("2");
//    }

}

