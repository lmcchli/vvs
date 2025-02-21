package com.mobeon.masp.rpcclient;

//import org.apache.xmlrpc.Base64;
//import org.apache.commons.codec.binary.Base64

import java.io.*;

//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class XmlRpcEncode {

    public static byte[] encode(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
  //          byte[] bytearray = baos.toByteArray();
            return baos.toByteArray();
            //return Base64.encode(bytearray);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object decode(byte[] array) {
        //byte[] array;
        //array = Base64.decode(array_in);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(array);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }



}
