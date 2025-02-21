/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.frontend;

import com.mobeon.backend.StorageClient;
import com.mobeon.event.MASEventDispatcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;


/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-19
 * Time: 23:54:00
 * To change this template use File | Settings | File Templates.
 */
public class ControlSignalFactory {
    public static ControlSignal create(MASEventDispatcher disp, Hashtable session) {
        try {
            Class cs =  Class.forName("com.mobeon.frontend.rtp.DTMFSignal");
            Class paramTypes[] = new Class[2];
            paramTypes[0] = Hashtable.class;
            paramTypes[1] = MASEventDispatcher.class;
            Constructor ssCtor = cs.getConstructor(paramTypes);
            // Should find the parameters for the constructor from some
            // init file or similar...
            Object[] params = new Object[2];
            params[0] = session;
            params[1] = disp;
            return (ControlSignal) ssCtor.newInstance(params);
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
