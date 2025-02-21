package com.mobeon.ntf.out.sip.test;

import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.test.TestUser;
import com.mobeon.ntf.out.sip.test.SIPOutTest;
import com.mobeon.ntf.out.sip.SIPOut;
import com.mobeon.ntf.out.sip.SIPCommandHandler;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.userinfo.SIPFilterInfo;

import java.util.Date;
import java.util.Properties;

/**
 *
 * @author  mnify
 */
public class SIPCommandHandlerTest extends NtfTestCase {

    public SIPCommandHandlerTest(String name) {
        super(name);
    }


    public void testBasic() throws Exception {
        l("testbasic");
        Properties props = new Properties();
        props.put("maxtimehours", "5");
        props.put("waittime", "2");
        props.put("waittime.10-50", "10");
        props.put("waittime.51-100", "60");
        props.put("waittime.101-", "300");

        SIPCommandHandler handler = new SIPCommandHandler(props);
        assertEquals(5, handler.getMaxTimeHours());
        assertEquals(2, handler.getWaitTime(0));
        assertEquals(2, handler.getWaitTime(9));
        assertEquals(10, handler.getWaitTime(10));
        assertEquals(10, handler.getWaitTime(50));
        assertEquals(60, handler.getWaitTime(51));
        assertEquals(60, handler.getWaitTime(100));
        assertEquals(300, handler.getWaitTime(105));
        assertEquals(300, handler.getWaitTime(456548833));


    }


}
