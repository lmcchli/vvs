template notifier plugin.
=========================

This plug-in is a generic plug-in for ntf, that allows a mas call flow designed to send notification events to NTF based on the NTF templates .cphr (phrases).
It was initially designed to allow sending of an update sms message from the call flow, when the subscriber still has messages in his mail box and hangs-up or drops the call, A reminder can be sent to
inform the subscriber they still have unread messages.

NOTE: Due to changes in the notifier framework, this plug-in is only supported at 3.3 CP03 + level, in order for it to work in earlier versions, you would need to backport the notifier framework and ntf changes that support it.
These changwes include hooks into the ntf stat managment interface to contorl shutdown and lock, and also hooks into the Phrases.java to check if a template (phrase) exists from the plug-in.

However the plug-in can be used to send any defined phrase from the call flow to ntf.  Where they be new or old.

Currenly the plug-in does not support phone on fuctions or payload type functions, this can be added in a later release as needed.  The payload is used to send a predifened sms from the call flow, text generated from the call flow as either binary or text.

The phone on would allow the call flow designer to wait for phone-on using ntf's confgiured phone on type to regulate the sending of notifications, similar to slamdown fucntionality.

The plug-in is loosly based upon the sample given in the notifier framework sdk, but has more specific functionality and is a little similar to some of the functionality in the KDDI notifier plug-in.

These notifier events corrospond to a particualr template name in the .cphr files located in /opt/moip/ntf/config/templates

The event is then formated according to the template, these can be existing or new templates.

To send the event from mas call flow(vxml) you use the libmas function System_SendTrafficEvent.

for example:

     <assign name="DNIS" expr="PhoneNumber_GetAnalyzedNumber(System_GetConfig('vva.numberanalysis', 'callednumberrule'), Call_GetDnis(), '')" />
 
<script>System_SendTrafficEvent ('updateSMS','callednumber='+DNIS+',sendonlyifunreadmessages=FALSE,sendmultipleifretry=FALSE')</script>

You could add the following to the template sms configuration file (there by default), but can be customised for retries disabling of COS, etc..

The config files are:

/opt/moip/config/ntf/cfg/templateSmsPlugin.conf

xsd:

/opt/moip/config/ntf/cfg/templateSmsPlugin.xsd

        <templateSms.Table>
	            <!-- template Event to send an update SMS  -->
		    <templateEvent>updateSMS
		    	<cphrType>mailboxcphr</cphrType>
			<notificationNumberType>delivery_profile</notificationNumberType>
			<mdrName>updateSMS</mdrName>
  		    </templateEvent>


        </templateSms.Table>
		
note: there is also a default config in the templatSms table, any event (phrase/template) not defined in the table will have a type defined based on the default if recieved by the plug-in and exists in the en.cphr

This allows to send any defined phrase from the mas call flow or other, to be sent via the plug-in.  However would need to be added to the backend trafficevent.conf fro the event to be allowed to be sent from mas. See below:

this would reusklt in an UpdateSMS phrase beeing send for the subscriber, by default would be: "You still have unread messages"

to turn on the plug-in you need to add the following config:

To enable the feature you need to add the plugin class path to the /opt/moip/config/ntf/cfg/notification.conf :
 
        <!-- This table loads all Notifier plug-ins in the order of the table, the LegacyPlugin,
             is the original plug-in framework which had to be a set class path.
 
             You can choose to load multiple plug-in or just one. The first one
             to register to handle an event will be the plug-in that handles that event,
             others plug-ins cannot handle the same event i.e. first come first served..
 
             NOTE: if no table or an empty table is specified NTF will attempt to load the
             legacy plug-in class, this is for backward compatibility.  If you want the
             legacy and a one or more new plug-ins to be loaded you must specify the
             legacy plug-in in the table along with your new plug-in(s).
        -->
 
        <NotifierPlugin.Table>
             <notifierPlugInName>templateSMS
                  <class>com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateSmsPlugin</class>
             </notifierPlugInName>
        </NotifierPlugin.Table>
 
 NOTE, by default the kddi legacy plug-in is enabled, if you define a table in ntf, it will use only the define plug-in (templateSms) to enable both kddi and ntf plug-in you would need to add the class path of
 the kddi plug-in to the table.  An example can be seen in the default notification.conf, which should at least bui in /home/messaging/templates/.... or under the ntf component in git. For example:
 
         <NotifierPlugin.Table>
             <notifierPlugInName>LegacyPlugin
                  <class>com.abcxyz.messaging.vvs.ntf.notifier.plugin.custom.NotifierPlugin</class>
             </notifierPlugInName>
		</NotifierPlugin.Table>
 
In the backend config you need to enable the the updateSMS event:
 
/opt/moip/config/backend/trafficevents.conf
 
                <TrafficEvents.Table>
 
                        <trafficEvent>updateSMS
                                <enabled>true</enabled>
                                <type>mfs</type>
                        </trafficEvent>
 
                </TrafficEvents.Table>
		
	NOTE: add any other template names you want to send from the call flow in the trafficevents.conf
