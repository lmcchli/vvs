/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2010.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.abcxyz.services.moip.ntf.result;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;


/**
 * Basic testing of the resolver.  The intent is to do a quick sanity check that the config
 * file can be loaded and the actions can be extracted from it.
 * 
 * @author lmcyvca
 */
public class NotificationActionResolverTest {
    private NotificationActionResolver resolver;
    
    @Before
    public void setup() throws URISyntaxException, ConfigurationDataException {
        resolver = new NotificationActionResolver();
        File configFile = new File(new URI(getClass().getResource("testErrorCodes.conf").toString()));
        
        resolver.load(configFile.getAbsolutePath());
    }

    @Test
    public void testGetNotificationAction_nullResultCode() {
        NotificationAction defaultAction = resolver.getDefaultNotificationAction();
        
        assertEquals(defaultAction, resolver.getNotificationAction(null));
        assertEquals(defaultAction, resolver.getNotificationAction(new ResultCode(null)));
    }
    
    @Test
    public void testGetNotificationAction_unknownResultCode() {
        NotificationAction defaultAction = resolver.getDefaultNotificationAction();
        
        assertEquals(defaultAction, resolver.getNotificationAction(new ResultCode("")));
        assertEquals(defaultAction, resolver.getNotificationAction(new ResultCode("251")));
    }
    
    @Test
    public void testGetNotificationAction_knownResultCode() {
        assertEquals(NotificationAction.stop, resolver.getNotificationAction(new ResultCode("250")));
        assertEquals(NotificationAction.retry, resolver.getNotificationAction(new ResultCode("260")));
    }
}
