package com.mobeon.masp.util;

import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-jan-11
 * Time: 09:45:03
 * To change this template use File | Settings | File Templates.
 */
public interface Constructor {
    Callable<Object> OBJECT =  new Callable<Object>() {
                public Object call() throws Exception {
                    return new Object();
                }
            };
}
