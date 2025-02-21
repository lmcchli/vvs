package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.cancel;

import java.util.Iterator;
import java.util.Vector;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.ANotifierCancelRequestProcessor;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.CancelFeedBack;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.ICancelRequest;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.INotifierDatabaseAccess;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.NotifierDatabaseException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile.NotificationType;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.SMSAddressInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateSmsPlugin;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.database.NotifierDatabaseHelper;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierConfig;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.CancelInfo;

public class CancelHandler extends ANotifierCancelRequestProcessor  {
	
	private INotifierDatabaseAccess dbAccess;
	
	private static CancelHandler _inst = null;
	private static INotifierLogger log = null;
		
	private boolean NTFCancelEnabled = false;

	private boolean plugInUsingCancel = false;
	private int destinationNpi;
	private int destinationTon;
	private Vector<PlugInCancelInfo> cancelInfoList;
	
	public static CancelHandler get() {
		if (_inst == null ) {
			_inst = new CancelHandler();
		}
		return _inst;
	}

	
	private CancelHandler() {
		
		if(log == null) {
			log = TemplateSmsPlugin.getLoggerFactory().getLogger(CancelHandler.class);
		}
		
		refreshConfig();
		
		TemplateSmsPlugin.GetCancelEventRegister().register(this);		
		dbAccess = TemplateSmsPlugin.getDatabaseAccess();
		
	}

	@Override
	public CancelFeedBack process(ICancelRequest cancelRequest) {
		if (!NTFCancelEnabled  || !plugInUsingCancel) {return null;}
		String subscriber = cancelRequest.getSubscriberNumber();

		if (cancelInfoList.isEmpty()) {
			return null;
		}

		try {

			ANotifierDatabaseSubscriberProfile profile = dbAccess.getSubscriberProfile(subscriber);
			String[] numbers = profile.getSubscriberNotificationNumbers(NotificationType.SMS, null);
			if (numbers.length < 0) {
				return null;
			}

			CancelFeedBack feedBack = new CancelFeedBack();	
			String cosName = NotifierDatabaseHelper.getCosName(profile);

			Iterator<PlugInCancelInfo> iter = cancelInfoList.iterator();
			while (iter.hasNext()) {				
				//Cancel Information per template that has cancel enabled.
				PlugInCancelInfo info = iter.next();
				
				String content = info.getCphrContentName();
				SMSAddressInfo source = NotifierConfig.getSourceAddress(content, cosName);		
				log.debug("process: adding cancelInfo for CPHR content: [" +  content + "] Subscriber: " + subscriber );

				for (int i = 0;i < numbers.length;i++) {
					//destination number for each enabled template		
					SMSAddressInfo destination = new SMSAddressInfo(destinationTon, destinationNpi, numbers[i]);					
					CancelInfo cancelInfo =  new CancelInfo(source, destination, info.getServiceType() );
					feedBack.addCancelInfo(cancelInfo);
					log.debug("process: added cancelInfo [" + cancelInfo.toString() + "]" );
				}
			}
			return feedBack;
		} catch (NotifierDatabaseException e) {
			log.warn("process: Unable to read profile for " + subscriber + " Cannot cancel ");
			return null;
		}
	}

	public void refreshConfig() {
		//first check if globally enabled.
		NTFCancelEnabled = NotifierConfig.isCancelEnabled();
		if (NTFCancelEnabled == false) {return;}
		
		//Now for the plug-in configuration.
		plugInUsingCancel = TemplateType.isPlugInUsingCancel();
		
		if (plugInUsingCancel == false) {return;} //if no templateType has cancel enabled..
		
		//load the fixed destination NPI and type of number.
		destinationNpi = NotifierConfig.getNumberingPlanIndicator();
		destinationTon = NotifierConfig.getTypeOfNumber();;

		//Create a list of templateType (content) that have cancel enabled with the information we
		//need to cancel so we don't need to do this on every cancel request.
		
		cancelInfoList = new Vector<PlugInCancelInfo>();
		
		//iterate over all non-default content, only explicitly defined can use cancel, or
		//we would only know about them when first received at run-time.
		Iterator<TemplateType> iter = TemplateType.iteratorNonDefault();
		
		//generate a basic list to save time at each Cancel call.
		while (iter.hasNext()) {
			TemplateType type = iter.next();
			if (type.isCancelEnabled()) {
				String serviceType = type.getServiceType();
				String content =  type.getCphr_template_name();
				PlugInCancelInfo cancelInfo = new PlugInCancelInfo(content, serviceType);
				cancelInfoList.add(cancelInfo);			
			}
		}
					
	}

}
