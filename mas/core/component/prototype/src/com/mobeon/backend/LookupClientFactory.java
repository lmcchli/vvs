/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.backend;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-17
 * Time: 16:18:40
 * To change this template use File | Settings | File Templates.
 */
public class LookupClientFactory {
    public static LookupClient create() {
        try {
            return (LookupClient) Class.forName("com.mobeon.backend.demo.DemoLookupClient").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
