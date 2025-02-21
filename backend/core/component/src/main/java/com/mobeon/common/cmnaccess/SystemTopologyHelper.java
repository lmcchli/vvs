package com.mobeon.common.cmnaccess;

import com.abcxyz.messaging.oe.common.topology.ComponentInfo;
import com.abcxyz.messaging.oe.impl.bpmanagement.utils.ComponentSAUtils;
import com.abcxyz.messaging.oe.lib.OEManager;


public class SystemTopologyHelper {
	public static String getOpcoName() throws TopologyException {
		ComponentInfo cInfo;
		String aComponentInstanceName;
		String opco = "unknown";
		try {
			aComponentInstanceName = ComponentSAUtils.getInstance().getComponentName();
			cInfo = getComponentInfo(aComponentInstanceName);
			if ( cInfo != null ) {
				opco = cInfo.getOperatorName();
				if (opco == null){
					throw new TopologyException("Could not find operator in topology for component instance " + aComponentInstanceName);
				}
			}
			else {
				throw new TopologyException("Could not find component called " + aComponentInstanceName + " in topology");
			}
		}
		catch (Exception e){
			throw new TopologyException("Unexpected exception: " + e.getMessage() + " received while querying topology");
		}
		return opco;
	}



	public static ComponentInfo getComponentInfo(String aComponentInstanceName) throws TopologyException {
		ComponentInfo cInfo;
		try {
			cInfo = OEManager.getSystemTopologyInfo().getComponentInfo(aComponentInstanceName);
		}
		catch (Exception e){
			throw new TopologyException("Unexpected exception: " + e.getMessage() + " received while querying topology");
		}
		return cInfo;
	}
}
