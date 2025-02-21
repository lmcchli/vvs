package com.abcxyz.services.moip.mdr;

import java.util.Collection;
import java.util.LinkedList;

import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.mdr.MdrEvent;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class MdrEventTest extends TestCase {

	private IConfiguration configuration;
	private CommonMessagingAccess msgAccess = CommonMessagingAccess.getInstance();

    public MdrEventTest() {
        super();
    }

    public void setUp() throws Exception {
        super.setUp();

        Collection<String> configFilenames = new LinkedList<String>();
	    String curDir = System.getProperty("user.dir");
	    String masFile = "";
	    String backendFile = "";
	    if (curDir.endsWith("backend") == false ) {
	    	backendFile = curDir +  "/../ipms_sys2/backend/cfg/backend.conf";
            masFile = curDir + "/../mas/cfg/mas.xml";
	    }
	    else {
	    	backendFile = curDir +  "/cfg/backend.conf";
	    	masFile = curDir.substring(0, curDir.indexOf("ipms_sys")) + "mas/cfg/mas.xml";
	    }

		configFilenames.add(masFile);
		configFilenames.add(backendFile);

		try{
			configuration = new ConfigurationImpl(null,configFilenames,false);
		}catch(Exception e) {
			System.out.println("Config could not be read");
			System.exit(0);
		}

		msgAccess.setConfiguration(MoipMessageEntities.MESSAGE_SERVICE_MAS, configuration);
		msgAccess.initConfig();
    }

    public void tearDown() throws Exception {
        super.tearDown();

    }

    public void testMasMdr(){
    	MdrEvent ev;

		for (int i = 0; i < 11; i++) {
			ev = new MdrEvent();

			//Orig from FD02
			//(4)un=3645/(4)cg=3645/ty=12/it=4/et=1/(8)id=49f0ebc7/pt=7/to=20090206T162647Z/ot=2/(5)ed=LogIn/(9)ms=session_1/

			//(4)un=3645/(9)ms=session_1/(2)ty=12/(4)cg=3645/(8)id=e1dc1054/(1)et=1/(1)pt=7/to=1234206457/(1)ot=2/(5)ed=LogIn/
			ev.setAttribute(1, i);
			ev.setAttribute(31, 3645);
			ev.setAttribute(50, "session_1");
			ev.setAttribute(1000, 303);
			ev.setAttribute(1001, 1);

			ev.setSASPortType(7);
			ev.setObjectType(2);
			ev.setEventDescription("LogIn");

			//ev.set

			/*		ev.setCalledStationId("calledStationId");
		ev.setGroupOwnership(5);
		ev.setSSSIdentifier("SSSidentifier");*/

			ev.write();
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {

			}
		}
    }

    public static Test suite() {
        return new TestSuite(MdrEventTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}

