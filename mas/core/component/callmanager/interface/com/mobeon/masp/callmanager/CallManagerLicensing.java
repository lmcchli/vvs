/*
 * @(#)CallManagerLicensing 1.0 21/01/2011
 *
 * COPYRIGHT (C) ABCXYZ INTERNET APPLICATIONS INC.
 *
 * THIS SOFTWARE IS FURNISHED UNDER A LICENSE ONLY AND IS
 * PROPRIETARY TO ABCXYZ INTERNET APPLICATIONS INC. IT MAY NOT BE COPIED
 * EXCEPT WITH THE PRIOR WRITTEN PERMISSION OF ABCXYZ INTERNET APPLICATIONS
 * INC.  ANY COPY MUST INCLUDE THE ABOVE COPYRIGHT NOTICE AS
 * WELL AS THIS PARAGRAPH.  THIS SOFTWARE OR ANY OTHER COPIES
 * THEREOF, MAY NOT BE PROVIDED OR OTHERWISE MADE AVAILABLE
 * TO ANY OTHER PERSON OR ENTITY.
 * TITLE TO AND OWNERSHIP OF THIS SOFTWARE SHALL AT ALL
 * TIMES REMAIN WITH ABCXYZ INTERNET APPLICATIONS INC.
 *
 *
 */
package com.mobeon.masp.callmanager;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.udp.InitializationException;
import com.mobeon.common.cmnaccess.TopologyException;
import com.mobeon.masp.operateandmaintainmanager.OMManager;

public interface CallManagerLicensing {

    public void setOmManager(OMManager omManager);

    public void init() throws ConfigurationDataException, TopologyException, InitializationException;

    public void refresh();

    public Boolean isLicensingEnabled();

    public void addOneVoiceCall()throws CallManagerLicensingException;

    public void removeOneVoiceCall();

    public void addOneVideoCall()throws CallManagerLicensingException;

    public void removeOneVideoCall();

}



