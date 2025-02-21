package com.mobeon.common.trafficeventsender;

import java.util.HashMap;
import java.util.Map;

import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.mdr.MdrEvent;
import com.mobeon.common.cmnaccess.SystemTopologyHelper;
import com.mobeon.common.cmnaccess.TopologyException;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

public class TrafficEventMdrHandler {
    private static ILogger log = ILoggerFactory.getILogger(TrafficEventMdrHandler.class);

    /**
     * Sends a TrafficEvent via the Radius interface.
     *
     * @param trafficEvent
     * @throws TrafficEventSenderException if some error with the event
     */
    public static void sendTrafficEvent(TrafficEvent trafficEvent) throws TrafficEventSenderException {
        if (log.isDebugEnabled()) log.debug("In sendTrafficEvent: sending trafficevent " + trafficEvent.getName());

        try {
            MdrEvent mdrEvent = makeMdrEvent(trafficEvent);
            mdrEvent.write();
        } catch (Exception e) {
            log.error("sendTrafficEvent " +e,e);
        }
    }

    /**
     * Loops through the properties in the TrafficEvent and creates a RadiusEvent. The properties names are mapped to
     * a method in RadiusEvent.
     *
     * @param trafficEvent
     * @return
     * @throws TrafficEventSenderException
     */
    private static MdrEvent makeMdrEvent(TrafficEvent trafficEvent) throws TrafficEventSenderException {

        Map<String, RadiusConfigurationAttribute> radiusMethodMap = TrafficEventSenderConfiguration.getInstance().
                getRadiusConfiguration().getRadiusAttributeMap();
        MdrEvent mdrEvent = new MdrEvent();
        String nasIdentifierPrefix = MoipMessageEntities.MESSAGE_SERVICE_MAS;
        HashMap<String, String> prMap = trafficEvent.getProperties();
        
        for (String name : prMap.keySet()) {
        	RadiusConfigurationAttribute attr = radiusMethodMap.get(name);
        	
            if (attr != null) {
                String value = prMap.get(name);
                int attrnum = getRadiusAttributeNumber(attr);
                
                if (attrnum == MdrEvent.NAS_IDENTIFIER) {
                    //no need to set: will automatically be fetched from topology
                    //nasIdentifierPrefix = value;
                } else if (attrnum == MdrEvent.USER_NAME) {
                    if (value != null && !value.isEmpty()) {
                        mdrEvent.setUserName(value);
                    }
                } else {
                    mdrEvent.setAttribute(getRadiusAttributeNumber(attr), value);
                }
            } else {
                throw new TrafficEventSenderException("Invalid property " + name + " found in event " +
                        trafficEvent.getName());
            }
        }
        
        setNasIdentifier(mdrEvent, nasIdentifierPrefix);
        setOpcoIdentification(mdrEvent);
        return mdrEvent;
    }

    private static int getRadiusAttributeNumber(RadiusConfigurationAttribute attr) {
        if (attr.getAttributeType() == RadiusConfigurationAttribute.AttributeType.VENDOR) return attr.getNumber() + 1000;

        return attr.getNumber();
    }


    private static void setNasIdentifier(MdrEvent mdrEvent, String applicationName) {
    	String ni = CommonOamManager.getInstance().getLocalInstanceNameFromTopology(applicationName);
        mdrEvent.setNasIdentifier(ni);
    }

    private static void setOpcoIdentification(MdrEvent mdrEvent) {
		String opco = "unknown";

		try {
			opco = SystemTopologyHelper.getOpcoName();
		}
		catch (TopologyException e) {
			if (log.isDebugEnabled()) log.debug("In setOpcoIdentification: Got exception " + e.getMessage() + " while getting operator from topology");
		}

		mdrEvent.setOpcoId(opco);
	}

}
