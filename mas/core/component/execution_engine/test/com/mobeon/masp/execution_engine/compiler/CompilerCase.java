/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Case;

import java.util.Arrays;
import java.util.List;

public abstract class CompilerCase extends Case {
    public CompilerCase(String name) {
        super(name);
    }

    //TODO: Maybe change validateOperations somewhat so that you can use it
    //TODO: to validate that a certain subset of a sequence of operations exists.

    public void validateOperations(Product result, Executable ... expected) {
        List<Executable> actual = result.freezeAndGetExecutables();
        compare("Operations",expected,actual);
    }

    public void validateConstructors(Product result, Executable ... expected) {
        List<Executable> actual = result.freezeAndGetConstructors();
        compare("Constructors",expected,actual);
    }

    public void validateDestructors(Product result, Executable ... expected) {
        List<Executable> actual = result.freezeAndGetDestructors();
        compare("Destructors",expected,actual);
    }

    public static void compare(String what,Executable[] expected, List<Executable> actual) {
        if(expected.length != actual.size()) {
            ExecutableBase.StringAccumulator actualBuf = new ExecutableBase.StringAccumulator();
            ExecutableBase.appendMnemonics(actualBuf,actual,0);
            ExecutableBase.StringAccumulator expectedBuf = new ExecutableBase.StringAccumulator();
            ExecutableBase.appendMnemonics(expectedBuf, Arrays.asList(expected),0);
            fail(what+": Different number of operations than expected, expected "+
                    expected.length+" but got "+actual.size()+" !\n" +
                    "\tActual was :\n"+actualBuf+"\n"+
                    "\tExpected was :\n"+ expectedBuf);
        }
        for(int i=0;i<expected.length;i++) {
            if(actual.get(i) == null)
                fail(what+": Element "+i+" in operations was null. This is not allowed");
            if(!expected[i].toMnemonic(0).equals(actual.get(i).toMnemonic(0))) {
                fail(what+": Expected "+expected[i].toMnemonic(0)+" but found "+actual.get(i).toMnemonic(0));
            }
        }
    }
}
