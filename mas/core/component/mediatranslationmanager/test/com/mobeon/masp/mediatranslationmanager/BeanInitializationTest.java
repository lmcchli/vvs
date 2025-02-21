/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import org.jmock.MockObjectTestCase;

public class BeanInitializationTest extends MockObjectTestCase {
    protected MediaTranslationManagerFacade mtm = null;

    public void setUp() {
        Utility.getSingleton().initialize("test/TestComponentConfig.xml");
        mtm = Utility.getSingleton().getMediaTranslationManager(Utility.getSingleton().getSession());
        assertNotNull(mtm);
    }

    public void testFacadeAccess() {
        MediaTranslationFactory factory = MediaTranslationFactory.getInstance();

//        assertNotNull("Get Media Object Factory", factory.getMediaObjectFactory());
//        assertNotNull("Get Stream Factory", factory.getStreamFactory());

//        assertNotNull("Get Service Locator", mtm.getServiceLocator());
    }
}
