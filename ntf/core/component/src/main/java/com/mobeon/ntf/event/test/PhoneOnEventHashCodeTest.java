package com.mobeon.ntf.event.test;

import java.util.concurrent.ConcurrentHashMap;

import com.mobeon.ntf.event.PhoneOnEvent;

public class TestPhoneOnEventHashCode extends extends NtfTestCase {
	
	private static ConcurrentHashMap<Object, String> theMap = new ConcurrentHashMap<Object, String>();
	
	public void testPhoneOnEventSameKey() {
	    Object sourceOne = new Object();
        PhoneOnEvent poeOne = new PhoneOnEvent(sourceOne, "address", PhoneOnEvent.PHONEON_OK, "message");
        
        Object sourceTwo = new Object();
        PhoneOnEvent poeTwo = new PhoneOnEvent(sourceTwo, "address", PhoneOnEvent.PHONEON_OK, "message");
       
        theMap.put(poeOne, "test");
        
        // Key should be the same even if object is not
        assertTrue(theMap.containsKey(poeTwo));
        
	    return;
	}

    public void testPhoneOnEventDifferentKey() {
        Object sourceOne = new Object();
        PhoneOnEvent poeOne = new PhoneOnEvent(sourceOne, "address1", PhoneOnEvent.PHONEON_OK, "message");
        
        Object sourceTwo = new Object();
        PhoneOnEvent poeTwo = new PhoneOnEvent(sourceTwo, "address2", PhoneOnEvent.PHONEON_OK, "message");
       
        theMap.put(poeOne, "test");
        
        // Key should NOT be the same even if object is not
        assertFalse(theMap.containsKey(poeTwo));
        
        return;
    }

}
