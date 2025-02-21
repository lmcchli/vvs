/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class DemonstrateHashcodeDuplicates {
    static Set<String> dupset = new HashSet<String>();
    static String[] buffer = new String[100000];
    static List<Object> longList = new ArrayList<Object>();

    public static void main(String[] args) {
        for(int i=0;i<100000;i++) {
            final Object o = new Object();
            longList.add(o);
            dupset.add(o.toString());
        }
        long iterations = 0;
        for(;;) {
            Object o = new Object();
            longList.add(o);
            String os = o.toString();
            buffer[o.hashCode() % buffer.length] = os;
            iterations++;
            if(dupset.contains(os))
                System.out.println("Caught two identical Objects "+os+" after "+iterations+" iterations !");
            if(iterations > 100000000)
                throw new RuntimeException("Test finished !");
        }
    }
}
