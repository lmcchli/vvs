package com.mobeon.masp.callmanager.sip;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.sip.Dialog;

import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.callhandling.CallInternal;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

public class SipStackAuditor {

	private final ILogger log = ILoggerFactory.getILogger(getClass());

	private final SipStackWrapper sipStackWrapper;

	public SipStackAuditor(SipStackWrapper sipStackWrapper) {
		this.sipStackWrapper = sipStackWrapper;
	}

	/** 
	 * Start a thread to audit the dialogs and transactions held by the SIP stack.
	 * The stack's dialogs and transactions will be compared with callmanager's internal 
	 * list of dialogs/callIds. Dialogs/transactions that have no counterpart in callmanager's
	 * during several consecutive audits will be removed from the stack. 
	 * 
	 */
	public void start(){

		new Thread("SipStackAuditorThread") {

			public void run() {

				// All units are milliseconds
				final int sleepTime = 60000;
				final long leakedDialogTimer = 120000;
				final long leakedTransactionTimer = ConfigurationReader.getInstance().getConfig().getSipStackLeakedTransactionAuditorTimer();

				if (log.isInfoEnabled())
					log.info("SipStackAuditorThread started.");

				while(true){
					try {
						Thread.sleep(sleepTime);

						Collection<CallInternal> calls =
							CMUtils.getInstance().getCallDispatcher().getAllCalls();

						Set<String> activeCallIDs = new HashSet<String>(calls.size());
						for (CallInternal call : calls) {
							
							try {
								activeCallIDs.add(call.getDialog().getCallId().getCallId());
							} catch(NullPointerException e){
								log.warn("Could not retrieve callId for call=" + call);
							}

						}

						String auditReport = sipStackWrapper.auditStack(activeCallIDs, 
								leakedDialogTimer, leakedTransactionTimer);

						if (log.isDebugEnabled()) {
							log.debug("AuditReport: " + auditReport);
						} else if (log.isInfoEnabled() && auditReport != null) {
							log.info("AuditReport: " + auditReport);
						}

					} catch(InterruptedException e){
						if (log.isDebugEnabled())
							log.debug("SipStackAuditor thread was interrupted, continuing anyway.");
					} catch(Throwable e) {
						log.warn("Exception occured in SipStackAuditor thread. Continuing anyway.", e);
					}
				}
			}
		}.start();
	}
}
