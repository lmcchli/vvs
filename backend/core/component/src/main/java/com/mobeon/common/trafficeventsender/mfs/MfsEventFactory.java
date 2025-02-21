package com.mobeon.common.trafficeventsender.mfs;

import com.abcxyz.messaging.mfs.MsgStoreServerFactory;
import com.abcxyz.messaging.mfs.MsgStoreServerFactory.TYPE;

public class MfsEventFactory {
	
	public static MfsEventManager getMfsEvenManager() {
		return getMfsEventManager(null);
	}
	
	public static MfsEventManager getMfsEventManager(MsgStoreServerFactory.TYPE mfsType) {
		MsgStoreServerFactory.TYPE now;
        if (mfsType != null) {
            // Override the configured type.
            now = mfsType;
        } else {
            now = MsgStoreServerFactory.getConfiguredType();
        }
        
        if (now == TYPE.NOSQL) {
            return new MfsEventManagerNoSQL();
        } else {
        	return new MfsEventManager();
        }
	}

}
