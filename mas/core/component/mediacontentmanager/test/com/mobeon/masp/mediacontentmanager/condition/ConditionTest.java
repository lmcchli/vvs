/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.condition;

import junit.framework.TestCase;

/**
 * Unit tests for the class {@link Condition}.
 *
 * todo more tests
 * @author Mats Egland
 */
public class ConditionTest extends TestCase {

    /**
     * todo
     */
    public void testGetCondition() {

        // test that the original condition string is untouched
        Condition cond = new Condition("numberOfNew > 1");
        String condString = cond.getCondition();
        assertEquals("Condition string is not what specified", "numberOfNew > 1", condString );
        String replacedString = condString.replaceAll("numberOfNew", "1");
        assertEquals("Condition string is not what specified", "numberOfNew > 1", condString );
        assertEquals("Condition string is not what specified", "1 > 1", replacedString);
    }
}
