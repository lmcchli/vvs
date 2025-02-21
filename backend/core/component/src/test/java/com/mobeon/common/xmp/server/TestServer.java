
package com.mobeon.common.xmp.server;

import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.util.logging.SimpleLogger;


public class TestServer {

    /** Creates a new instance of TestServer */
    public TestServer(int port) {
        
        HttpServer server = new HttpServer();
        server.setPort(port);
        SimpleLogger log = SimpleLogger.getLogger();
        log.setFileName("server.log");
        server.setLogger(log);
        server.start();

    }
    
    public static void main(String [] args) {
        int port = 9090;
        if( args.length > 0 ) {
            String portStr = args[0];
            try {
                port = Integer.parseInt(portStr);
            } catch(Exception e) {


            }
        }
	XmpHandler.setServiceHandler(IServiceName.MEDIA_CONVERSION, com.mobeon.common.xmp.server.MediaConversionHandler.get());
	XmpHandler.setServiceHandler(IServiceName.OUT_DIAL_NOTIFICATION, com.mobeon.common.xmp.server.OutdialNotificationHandler.get());
	XmpHandler.setServiceHandler(IServiceName.CALL_MWI_NOTIFICATION, com.mobeon.common.xmp.server.CallMWINotificationHandler.get());
	XmpHandler.setServiceHandler(IServiceName.PAGER_NOTIFICATION, com.mobeon.common.xmp.server.PagerNotificationHandler.get());
	XmpHandler.setServiceHandler("ExternalSubscriberInformation", com.mobeon.common.xmp.server.externalsubscriberinformationhandler.get());
	XmpHandler.setServiceHandler("Accounting", com.mobeon.common.xmp.server.accountinghandler.get());
    XmpHandler.setServiceHandler(IServiceName.MWI_NOTIFICATION, com.mobeon.common.xmp.server.MWINotificationHandler.get());

        new TestServer(port);
    }
}
