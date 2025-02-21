package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.mobeon.common.cmnaccess.TopologyException;
import com.mobeon.masp.callmanager.CallManagerLicensing;
import com.mobeon.masp.callmanager.CallManagerLicensingException;
import com.mobeon.masp.operateandmaintainmanager.OMManager;


public class CallManagerLicensingMock
        implements CallManagerLicensing
{
    // As per needed for Spring Framework
    //private OMManager omManager;

    public CallManagerLicensingMock()
    {
    }

    public void setOmManager(OMManager omManager)
    {
        //this.omManager = omManager;
    }

    public synchronized void init() throws ConfigurationDataException, TopologyException
    {
    }

    public synchronized void refresh()
    {
    }

    public Boolean isLicensingEnabled()
    {
        return false;
    }

    public synchronized void addOneVoiceCall() throws CallManagerLicensingException
    {
        return;
    }

    public synchronized void removeOneVoiceCall()
    {
    }

    public synchronized void addOneVideoCall()  throws CallManagerLicensingException
    {
        return;
    }

    public synchronized void removeOneVideoCall()
    {
    }

}
