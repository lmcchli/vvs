package com.mobeon.common.cmnaccess;

import java.util.HashMap;
import java.util.Iterator;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.Message;
import com.abcxyz.messaging.mfs.data.MfsFileHandle;

/**
 * class defines message handle for keeping information for further
 * message handling. The class is just a data holder
 *
 * @author lmchuzh
 *
 */
public class MessageHandle {

	public MSA omsa;
	public String omsgid;
	public MSA rmsa;
	public String rmsgid;

	public MfsFileHandle origFileHandle;
	public HashMap<String, MfsFileHandle> attachFileHandles  ;

	public RouteResult routeFound;
	public Container1 c1;
	public Message msg; // Used when in direct delivery mode.

	public int storeTried = 0;
	public void release() {
	    if (origFileHandle != null) {
	        origFileHandle.release();
	        origFileHandle = null;
	    }

	    if (attachFileHandles != null) {
	        Iterator<MfsFileHandle> itr = attachFileHandles.values().iterator();
	        while (itr.hasNext()) {
	            MfsFileHandle handle = itr.next();
	            handle.release();
	            handle = null;
	        }
	        attachFileHandles = null;
	    }
	}
}
