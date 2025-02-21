/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.frontend;

import com.mobeon.event.MASEventDispatcher;
import com.mobeon.frontend.rtp.RTPStream;

import java.util.Hashtable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-19
 * Time: 23:55:18
 * To change this template use File | Settings | File Templates.
 */
public class StreamFactory {
     public static Stream create(String ipaddress, int localPort, int remotePort, int payloadType) {
        try {
            Class cs =  Class.forName("com.mobeon.frontend.rtp.RTPStream");
            Class paramTypes[] = new Class[4];
            paramTypes[0] = String.class;
            paramTypes[1] = int.class;
            paramTypes[2] = int.class;
            paramTypes[3] = int.class;
            Constructor ssCtor = cs.getConstructor(paramTypes);
            // Should find the parameters for the constructor from some
            // init file or similar...
            Object[] params = new Object[4];
            params[0] = ipaddress;
            params[1] = new Integer(localPort);
            params[2] = new Integer(remotePort);
            params[3] = new Integer(payloadType);

            return (Stream) ssCtor.newInstance(params);
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

    public static Stream create(String ipaddress, ControlSignal controlSig, int localPort, int remotePort, int payloadType) {
        try {
            Class cs =  Class.forName("com.mobeon.frontend.rtp.RTPStream");
            Class paramTypes[] = new Class[5];
            paramTypes[0] = String.class;
            paramTypes[1] = ControlSignal.class;
            paramTypes[2] = int.class;
            paramTypes[3] = int.class;
            paramTypes[4] = int.class;
            Constructor ssCtor = cs.getConstructor(paramTypes);
            // Should find the parameters for the constructor from some
            // init file or similar...
            Object[] params = new Object[5];
            params[0] = ipaddress;
            params[1] = controlSig;
            params[2] = new Integer(localPort);
            params[3] = new Integer(remotePort);
            params[4] = new Integer(payloadType);

            return (Stream) ssCtor.newInstance(params);
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
