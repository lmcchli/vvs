/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.backend;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-17
 * Time: 16:37:46
 * To change this template use File | Settings | File Templates.
 */
public class StorageClientFactory {
    public static StorageClient create() {
        try {
            return (StorageClient) Class.forName("com.mobeon.backend.demo.DemoStorageClient").newInstance();
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
