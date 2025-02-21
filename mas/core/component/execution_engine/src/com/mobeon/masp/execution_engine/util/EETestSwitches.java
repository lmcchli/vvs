package com.mobeon.masp.execution_engine.util;

import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.util.test.MASTestSwitches;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-feb-01
 * Time: 15:19:13
 * To change this template use File | Settings | File Templates.
 */
public class EETestSwitches {
    private static Id<Product> firstId;
    public static Id<Product> toProductTestId(Id<Product> id) {

        if (MASTestSwitches.isCompilerTesting()) {
            if (firstId == null)
                firstId = id;
            return firstId;
        } else {
            return id;
        }
    }

}
