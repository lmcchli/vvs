In order to test and activate this plug-in you need to copy the Main.vxml file to the
/opt/moip/mas/applications/vva.001 folder, replacing the existing one.  This will allow the call flow to send updateSMS messages towards ntf and the template plug-in.

The following config needs to be added to enable the plug-in + the events to be send from mas.ntf.

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
 
In the backend config you need to enable the the updateSMS event:
 
/opt/moip/config/backend/trafficevents.conf
 
                <TrafficEvents.Table>
 
                        <trafficEvent>updateSMS
                                <enabled>true</enabled>
                                <type>mfs</type>
                        </trafficEvent>
						
			<trafficEvent>
                                <enabled>true</enabled>
                                <type>mfs</type>
                        </trafficEvent>
 
                </TrafficEvents.Table>
				
by pressing 1-3 on the call flow it will send updateSms with different flag combinations.

Pressing 4 sends a message using the c template, the c template is nolt configured in the the template config as default, instead it will create an event from the default defined in the table. This will have a similar effect to sending a count type voice mail notification.

In theory any defined phrase in the .cphr files can be sent.  But currently payload (tag) and others are not supported and phone on functionaltity are not supported, to be added at a later date.

To send the phrase (template) you have to add it the the TrafficEvents.table in backend, or it will be blocked, and makes use of the phrase defined in the .cphr files.

