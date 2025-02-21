/**
 * 
 */
package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.chargingaccountmanager.ChargingAccountException;
import com.mobeon.masp.chargingaccountmanager.IChargingAccountManager;
import com.mobeon.masp.chargingaccountmanager.IChargingAccountRequest;
import com.mobeon.masp.chargingaccountmanager.IChargingAccountResponse;

/**
 * @author lmcraby
 *
 */
public class ChargingAccountManagerMock extends BaseMock implements
		IChargingAccountManager {

	/* (non-Javadoc)
	 * @see com.mobeon.masp.chargingaccountmanager.IChargingAccountManager#createChargingAccountRequest()
	 */
	@Override
	public IChargingAccountRequest createChargingAccountRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.chargingaccountmanager.IChargingAccountManager#getNextClientId()
	 */
	@Override
	public int getNextClientId() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.mobeon.masp.chargingaccountmanager.IChargingAccountManager#sendRequest(com.mobeon.masp.chargingaccountmanager.IChargingAccountRequest, int)
	 */
	@Override
	public IChargingAccountResponse sendRequest(
			IChargingAccountRequest request, int clientId)
			throws ChargingAccountException {
		// TODO Auto-generated method stub
		return null;
	}

}
