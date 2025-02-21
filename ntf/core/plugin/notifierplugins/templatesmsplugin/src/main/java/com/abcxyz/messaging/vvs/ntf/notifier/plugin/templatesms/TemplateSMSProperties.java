/**
 * 
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms;

/**
 * A collection of properties sent in the templateSMS notifier event.
 */
public class TemplateSMSProperties {
	public static final String SendOnlyIfUnread 		= "sendonlyifunreadmessages"; //property to indicate to send if only unread messages are in INBOX.
	public static final String sendMultipleIfRetry 	= "sendmultipleifretry"; //property to indicate if should send new event if retrying last one.
}
