package com.abcxyz.services.moip.ntf.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mrd.data.MessageContext;
import com.abcxyz.services.moip.ntf.coremgmt.EventSentListener;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;

/**
 *
 * class for generating a NTF event
 *
 * @author lmchuzh
 *
 */
public class NtfEventGenerator
{

    /*static HashMap<String, Class> eventsClass = new HashMap<String, Class>();

    static {
        eventsClass.put(MessageEntities.SERVICE_TYPE_NORMAL, NtfEvent.class);
    }*/


    public static NtfEvent generateEvent(String serviceType,
            MessageInfo msgInfo, HashMap<String, String> properties, String refId) {


        Properties props = getProperties(properties);

        //if service type is not the default one, check if need to create as a specific event
        NtfEvent event = new NtfEvent(serviceType, msgInfo, props, refId);

        EventSentListener listener = NtfEventHandlerRegistry.getEventSentListener(serviceType);
        event.setSentListener(listener);

        return event;
    }

    public static NtfEvent generateEvent(String serviceType,
            MessageInfo msgInfo, Properties properties, String refId) {

        //if service type is not the default one, check if need to create as a specific event
        NtfEvent event = new NtfEvent(serviceType, msgInfo, properties, refId);

        EventSentListener listener = NtfEventHandlerRegistry.getEventSentListener(serviceType);
        event.setSentListener(listener);

        return event;
    }


    public static NtfEvent generateEvent(String eventId) {

        NtfEvent event = new NtfEvent (eventId);

        EventSentListener listener = NtfEventHandlerRegistry.getEventSentListener(event.getEventServiceTypeKey());
        event.setSentListener(listener);

        return event;
    }

    public static Properties getProperties(HashMap<String, String> values) {
        Properties properties = new Properties();

        Iterator<String> keys = values.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith(MessageContext.SERVICE_PROPERTY_PREFIX)) {
                properties.put(key.substring(MessageContext.SERVICE_PROPERTY_PREFIX.length()), values.get(key));
            } else {
                properties.put(key, values.get(key));
            }
        }

        return properties;

    }
}
