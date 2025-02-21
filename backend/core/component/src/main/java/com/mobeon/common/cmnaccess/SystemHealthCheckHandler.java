package com.mobeon.common.cmnaccess;

import com.abcxyz.messaging.common.interfaces.ComponentHealthChecker.CheckingResult;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.mcd.statuschecker.McdProxyStatusChecker;
import com.abcxyz.messaging.nodestatusmonitor.CheckerLoadingException;
import com.abcxyz.messaging.nodestatusmonitor.ComponentCheckerFactory;
import com.abcxyz.messaging.nodestatusmonitor.ComponentCheckerHandler;
import com.abcxyz.messaging.nodestatusmonitor.ComponentStatusConfig;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;

/**
 * class handles system components status checking required for running traffic
 *
 * @author lmchuzh
 *
 */
public class SystemHealthCheckHandler implements ComponentCheckerHandler {


    private static final String checkers = System.getProperty("abcxyz.moip.componentcheckers", McdProxyStatusChecker.class.getName());

    private static final LogAgent logger = CommonOamManager.getInstance().getLogger();

	@Override
	public void reportStatus(CheckingResult status) {
		if (status == CheckingResult.CHECKING_SUCCESS) {
			CommonMessagingAccess.getInstance().setSystemReady();

			if (logger.isDebugEnabled())
				logger.debug("SystemHealthCheckHandler reportStatus success");
		} else {
			logger.warn("SystemHealthCheckHandler report failed status<" + status.name() + ">");
		}
	}

	@Override
	public void reportStatus(String componentId, CheckingResult status, boolean lastReport) {

		if (logger.isDebugEnabled())
			logger.debug("SystemHealthCheckHandler report status<" + status.name() + ">" + " component<" + componentId + ">" + " last: " + lastReport);

		if (status == CheckingResult.CHECKING_SUCCESS) {
			if (lastReport) {
				CommonMessagingAccess.getInstance().setSystemReady();
			}
		} else {
			logger.warn("SystemHealthCheckHandler report failed status<" + status.name() + ">" + " component<" + componentId);
		}
	}

	void startComponentChecking(OAMManager oam) {

		ComponentCheckerFactory checker = new ComponentCheckerFactory();

		try {
			oam.getConfigManager().setParameter(ComponentStatusConfig.healthCheckerClassNames, checkers);
		} catch (ConfigurationDataException e1) {
		    logger.warn("startComponentChecking "+e1,e1);
		}

		try {

			checker.setDefaultOam(oam);

			checker.start(this);
		} catch (CheckerLoadingException e) {
			CommonMessagingAccess.getInstance().setSystemReady();

			logger.warn("SystemHealthCheckHandler failed loading checkers: " + e.getMessage() + " skip checkings",e);
		}

	}
}
