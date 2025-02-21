/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.condition;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediacontentmanager.IMediaQualifierFactory;
import com.mobeon.masp.mediacontentmanager.MediaQualifierException;
import com.mobeon.masp.mediaobject.MultiThreadedTeztCase;
import com.mobeon.masp.mediacontentmanager.qualifier.*;
import org.apache.log4j.xml.DOMConfigurator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Unit tests for class {@link RhinoInterpreter}.
 *
 * @author Mats Egland
 *         <p/>
 *         todo this class needs more tests.
 */
public class RhinoInterpreterTest extends MultiThreadedTeztCase {
    /**
     * The tested interpreter.
     */
    private ConditionInterpreter interpreter;
    private IMediaQualifierFactory mediaQualifierFactory;

    public void setUp() {
        interpreter = RhinoInterpreter.getInstance();
        mediaQualifierFactory = new MediaQualifierFactory();
    }

    /**
     * Test for method
     * {@link RhinoInterpreter#interpretCondition(Condition, com.mobeon.masp.mediacontentmanager.IMediaQualifier[])}
     * with expressions 'true' and 'false'.
     */
    public void testTrueFalse() throws ConditionInterpreterException {
        // true
        Condition cond = new Condition("true");
        boolean result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("true should evaluate to true", result);

        // false
        cond = new Condition("false");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("false should evaluate to false", result);

    }

    /**
     * Tests that the method
     * {@link RhinoInterpreter#interpretCondition(Condition, com.mobeon.masp.mediacontentmanager.IMediaQualifier[])}
     * throws ConditionInterpreterException when bad expression.
     */
    public void testException() {
        Condition cond = new Condition("apa");
        try {
            boolean result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
            fail("ConditionInterpreterException should be thrown");
        } catch (ConditionInterpreterException e) {/**/}
        cond = new Condition("a < 1");
        try {
            boolean result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
            fail("ConditionInterpreterException should be thrown");
        } catch (ConditionInterpreterException e) {/**/}
        cond = new Condition("1 && 1");
        try {
            boolean result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
            fail("ConditionInterpreterException should be thrown");
        } catch (ConditionInterpreterException e) {/**/}
    }

    /**
     * Tests for method
     * {@link RhinoInterpreter#interpretCondition(Condition, com.mobeon.masp.mediacontentmanager.IMediaQualifier[])}
     * with expressions containing simple integers but no qualifiers.
     * <p/>
     * <p/>
     * <pre>
     * 1. true
     * 2. false
     * 3. >
     * 4. >=
     * 5. <
     * 6. <=
     * 7. &&
     * 8. ||
     * 9. Use of ()
     * 10. Complex expressions
     * </pre>
     */
    public void testInterpretCondition_Integer_Without_Qualifiers() throws ConditionInterpreterException {
        // 1. true - should return true
        Condition cond = new Condition("true");
        boolean result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("true should return true", result);

        // 2. false - should return true
        cond = new Condition("false");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("false should return false", result);

        // 3. greater than tests
        cond = new Condition("1 > 2");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("1 > 2 should return false", result);
        cond = new Condition("1+2 > 3-1");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("1+2 > 3-1", result);
        cond = new Condition("1+2 > 4-1");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("1+2 > 4-1", result);

        // 4. >=
        cond = new Condition("1 >= 2");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("1 >= 2 should return false", result);
        cond = new Condition("1 >= 1");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("1 >= 2 should return true", result);
        cond = new Condition("3 >= 2");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("3 >= 2 should return true", result);
        cond = new Condition("1+4 >= 6-1");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("1+4 >= 6-1", result);
        cond = new Condition("1+4 >= 8-2");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("1+4 >= 8-2", result);

        // 5.
        cond = new Condition("1 < 2");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("1 < 2 should return true", result);
        cond = new Condition("2 < 1");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("2 < 1 should return false", result);
        cond = new Condition("5-6 < 5-5");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("5-6 < 5-5 should return true", result);
        cond = new Condition("5-5 < 5-5");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("5-5 < 5-5 should return false", result);

        // 6.
        cond = new Condition("2 <= 1");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("2 <= 1 should return false", result);
        cond = new Condition("2 <= 2");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("2 <= 2 should return true", result);

        // 7.
        cond = new Condition("1 < 2 && 2 > 1");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("1 < 2 && 2 > 1 return true", result);
        cond = new Condition("1 < 2 && 2 < 1");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("1 < 2 && 2 < 1 should return false", result);

        // 8.
        cond = new Condition("1 < 2 || 2 < 1");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("1 < 2 || 2 < 1 should return true", result);
        cond = new Condition("1 > 2 || 2 < 1");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("1 > 2 || 2 < 1 should return false", result);

        // 9.
        cond = new Condition("(1 < 2)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("(1 < 2) should return true", result);
        cond = new Condition("(1 < 2) || (2 < 1)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("(1 < 2) || (2 < 1) should return true", result);
        cond = new Condition("(1 > 2) || (2 < 1)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("(1 > 2) || (2 < 1) should return false", result);
        cond = new Condition("(1 > 2 || 2 < 1)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("(1 > 2 || 2 < 1) should return false", result);
        cond = new Condition("(2 + 2) > 2");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("(2 + 2) > 2 should return true", result);
        cond = new Condition("((1 + 2) > 2) && (1 == 1) || ((1 == 1) && (1 < 0))");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("((1 + 2) > 2) && (1 == 1) || ((1 == 1) && (1 < 0)) should return true", result);

        // 10.
        cond = new Condition("(1 < 2 && 1 < 0) || (2 > 1)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("(1 < 2 && 1 < 0) || (2 > 1) should return true", result);
        cond = new Condition("(1 < 2 && 1 < 0) || (2 < 1)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("(1 < 2 && 1 < 0) || (2 < 1) should return true", result);
        cond = new Condition("1 == 0 || (1 < 2 && 1 < 0) || (2 < 1)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("1 == 0 || (1 < 2 && 1 < 0) || (2 < 1) should return false", result);
        cond = new Condition("1 == 1 || (1 < 2 && 1 < 0) || (2 < 1)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("1 == 1 || (1 < 2 && 1 < 0) || (2 < 1) should return true", result);
        cond = new Condition("1-1 == 0+5 || (1+1 < 2 && 1 < 0) || (2+8 == 20-10)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("1-1 == 0+5 || (1+1 < 2 && 1 < 0) || (2+8 == 20-10) should return true", result);
        cond = new Condition("1-1 == 0+5 || (1+1 < 2 && 1 < 0) || (2+8 != 20-10)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("1-1 == 0+5 || (1+1 < 2 && 1 < 0) || (2+8 != 20-10) should return false", result);
        cond = new Condition("(1-1 == 0+5 && (1+1 < 2 && 1 < 0)) || (2+8 == 20-10)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("(1-1 == 0+5 && (1+1 < 2 && 1 < 0)) || (2+8 == 20-10) should return false", result);
        cond = new Condition("(1+4 == 0+5 && (1+1 <= 2 && 1 < 2)) || (2+8 != 20-10)");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("(1+4 == 0+5 && (1+1 <= 2 && 1 < 2)) || (2+8 != 20-10) should return true", result);
    }

    /**
     * Tests for method
     * {@link RhinoInterpreter#interpretCondition(Condition, com.mobeon.masp.mediacontentmanager.IMediaQualifier[])}
     * with expressions containing simple integers and qualifiers.
     * <p/>
     * <p/>
     * <pre>
     * 1. true
     * 2. false
     * 3. >
     * 4. >=
     * 5. <
     * 6. <=
     * 7. &&
     * 8. ||
     * 9. Use of ()
     * 10. Complex expressions
     * </pre>
     */
    public void testInterpretCondition_Integer_With_Qualifiers() throws ConditionInterpreterException, MediaQualifierException {
        // 1. true - should return true
        Condition cond = new Condition("true");
        boolean result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("true should return true", result);

        // 2. false - should return true
        cond = new Condition("false");
        result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertFalse("false should return false", result);

        // 3. >
        NumberQualifier nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "1",
                NumberQualifier.Gender.Male);
        cond = new Condition("numberOfNew > 2");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertFalse("1 > 2 should return false", result);
        cond = new Condition("numberOfNew>2");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertFalse("1 > 2 should return false", result);
        cond = new Condition(" numberOfNew> 2");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertFalse("1 > 2 should return false", result);
        cond = new Condition("numberOfNew + 2 > numberOfOld-1");

        nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfOld",
                "2",
                NumberQualifier.Gender.Male);
        NumberQualifier nQ2 = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "1",
                NumberQualifier.Gender.Male);
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ2, nQ});
        assertTrue("1+2 > 3-1", result);
        cond = new Condition("numberOfNew + 2 > numberOfOld-1");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ2, nQ});
        assertTrue("1+2 > 2-1", result);

        // 4. >=
        nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "2",
                NumberQualifier.Gender.Male);
        cond = new Condition("numberOfNew >= 3");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertFalse("2 >= 3 should return false", result);
        cond = new Condition("2 >= numberOfNew");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertTrue("2 >= 2 should return true", result);
        nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "3",
                NumberQualifier.Gender.Male);
        cond = new Condition("numberOfNew >= 2");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertTrue("3 >= 2 should return true", result);
        cond = new Condition("numberOfNew+4 >= 8-1");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertTrue("numberOfNew+4 >= 8-1", result);
        cond = new Condition("numberOfNew+4 >= 10-3");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertTrue("3+4 >= 10-3", result);

        // 5. <
        nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "1",
                NumberQualifier.Gender.Male);
        cond = new Condition("numberOfNew < 2");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertTrue("1 < 2 should return true", result);
        cond = new Condition("2 < numberOfNew");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertFalse("2 < 1 should return false", result);
        nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "5",
                NumberQualifier.Gender.Male);
        cond = new Condition("numberOfNew-6 < numberOfNew-5");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertTrue("5-6 < 5-5 should return true", result);
        cond = new Condition("numberOfNew-5 < 5-numberOfNew");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertFalse("5-5 < 5-5 should return false", result);

        // 6. <=
        nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "1",
                NumberQualifier.Gender.Male);
        cond = new Condition("2 <= numberOfNew");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertFalse("2 <= 1 should return false", result);
        nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "2",
                NumberQualifier.Gender.Male);
        cond = new Condition("numberOfNew <= 2");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertTrue("2 <= 2 should return true", result);

        // == tests
        nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "10",
                NumberQualifier.Gender.Male);
        cond = new Condition("numberOfNew == 10");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertTrue("10 == 10 should return true", result);

        // 7.
        nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "1",
                NumberQualifier.Gender.Male);
        cond = new Condition("numberOfNew < 2 && 2 > numberOfNew");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertTrue("numberOfNew < 2 && 2 > numberOfNew return true", result);
        cond = new Condition("numberOfNew < 2 && 2 < numberOfNew");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertFalse("1 < 2 && 2 < 1 should return false", result);

        // 8.
        cond = new Condition("numberOfNew < 2 || 2 < numberOfNew");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertTrue("1 < 2 || 2 < 1 should return true", result);
        cond = new Condition("numberOfNew > 2 || 2 < numberOfNew");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertFalse("1 > 2 || 2 < 1 should return false", result);

        // 9.
        nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "1",
                NumberQualifier.Gender.Male);
        nQ2 = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfOld",
                "2",
                NumberQualifier.Gender.Male);
        cond = new Condition("(numberOfNew < 2)");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ});
        assertTrue("(1 < 2) should return true", result);
        cond = new Condition("(numberOfNew < 2) || (numberOfOld < 1)");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertTrue("(numberOfNew < numberOfOld) || (2 < numberOfNew) should return true", result);
        cond = new Condition("(numberOfNew > 2) || (numberOfOld < 1)");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertFalse("(1 > 2) || (2 < 1) should return false", result);
        cond = new Condition("(numberOfNew > 2 || numberOfOld < 1)");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertFalse("(1 > 2 || 2 < 1) should return false", result);
        cond = new Condition("(numberOfOld + 2) > numberOfOld");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertTrue("(2 + 2) > 2 should return true", result);
        cond = new Condition("numberOfNew+2 > 2 && numberOfNew == 2 || numberOfOld != numberOfNew");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertTrue("\"numberOfNew+2 > 2 && numberOfNew == 2 || numberOfOld != numberOfNew\" should return true", result);

        // 10.
        nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "1",
                NumberQualifier.Gender.Male);
        nQ2 = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfOld",
                "2",
                NumberQualifier.Gender.Male);
        cond = new Condition("(numberOfNew < 2 && 1 < 0) || (numberOfOld > 1)");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertTrue("(1 < 2 && 1 < 0) || (2 > 1) should return true", result);
        cond = new Condition("(numberOfNew < 2 && 1 < 0) || (numberOfOld < 1)");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertFalse("(1 < 2 && 1 < 0) || (2 < 1) should return true", result);
        cond = new Condition("numberOfNew == 0 || (1 < numberOfOld && 1 < 0) || (numberOfOld < 1)");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertFalse("1 == 0 || (1 < 2 && 1 < 0) || (2 < 1) should return false", result);
        cond = new Condition("numberOfNew == 1 || (1 < numberOfOld && 1 < 0) || (2 < numberOfNew)");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertTrue("1 == 1 || (1 < 2 && 1 < 0) || (2 < 1) should return true", result);

        nQ2 = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfOld",
                "3",
                NumberQualifier.Gender.Male);
        cond = new Condition("numberOfOld == 3");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertTrue("3==3 should return true", result);
        nQ2 = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfOld",
                "2",
                NumberQualifier.Gender.Male);
        cond = new Condition("numberOfOld+2 == 4");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertTrue("2==2 should return true", result);
        cond = new Condition("numberOfNew-1 == 0+5 || (numberOfNew+1 < numberOfOld && 1 < 0) " +
                "|| (numberOfOld+8 != 20-10)");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertFalse("1-1 == 0+5 || (1+1 < 2 && 1 < 0) || (2+8 != 20-10) should return false", result);
        cond = new Condition("(1-numberOfNew == 0+5 && (numberOfNew+1 < numberOfOld && 1 < 0)) " +
                "|| (numberOfOld+8 == 20-10)");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertTrue("(1-1 == 0+5 && (1+1 < 2 && 1 < 0)) || (2+8 == 20-10) should return false", result);
        cond = new Condition("(numberOfNew+4 == 0+5 && (1+numberOfNew <= 2 && 1 < numberOfOld)) " +
                "|| (numberOfOld+8 != 20-10)");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
        assertTrue("(1+4 == 0+5 && (1+1 <= 2 && 1 < 2)) || (2+8 != 20-10) should return true", result);
    }

    /**
     * Tests the function
     * {@link RhinoInterpreter#interpretCondition(Condition, com.mobeon.masp.mediacontentmanager.IMediaQualifier[])}
     * with expressions that contains the type DateDM.
     */
    public void testInterpretCondition_DateDM() throws ConditionInterpreterException, ParseException, MediaQualifierException {
        // greater than
        Condition cond = new Condition("DateDM('2006-01-10') > DateDM('2006-01-09')");
        boolean result = interpreter.interpretCondition(cond, new IMediaQualifier[]{});
        assertTrue("2006-01-10 > 2006-01-09 should return true", result);


        DateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd");
        DateDMQualifier dQ = (DateDMQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.DateDM,
                "myDateDM",
                "2006-01-10", null);
        cond = new Condition("myDateDM > DateDM('2006-01-09')");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{dQ});
        assertTrue("2006-01-10 > 2006-01-09 should return true", result);

        dQ = (DateDMQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.DateDM,
                "myDateDM",
                "2006-01-10", null);
        cond = new Condition("myDateDM > DateDM('2006-01-09')");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{dQ});
        assertTrue("2006-01-10 > 2006-01-09 should return true", result);

        // less than
        dQ = (DateDMQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.DateDM,
                "date",
                "2006-08-10", null);
        cond = new Condition("date < DateDM('2006-10-09')");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{dQ});
        assertTrue("2006-08-10 < 2006-10-09 should return true", result);


    }

    /**
     * Tests the function
     * {@link RhinoInterpreter#interpretCondition(Condition, com.mobeon.masp.mediacontentmanager.IMediaQualifier[])}
     * with expressions that contains the type Weekday.
     */
    public void testInterpretCondition_Weekday() throws ConditionInterpreterException, ParseException, MediaQualifierException {
        // greater than
        WeekdayQualifier wQ = (WeekdayQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.WeekDay,
                "date",
                "2006-01-10", null);


        Condition cond = new Condition("date > WeekDay('2006-01-09')");
        boolean result = interpreter.interpretCondition(cond, new IMediaQualifier[]{wQ});
        assertTrue("2006-01-10 > 2006-01-09 should return true", result);

        // equals
//        cond = new Condition("date == WeekDay('2006-01-10')");
//        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{wQ});
//        assertTrue("date == WeekDay('2006-01-10') should return true", result);

        // less than
        wQ = (WeekdayQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.WeekDay,
                "date",
                "2006-08-10", null);
        cond = new Condition("date < WeekDay('2006-10-09')");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{wQ});
        assertTrue("2006-08-10 < 2006-10-09 should return true", result);


    }

    /**
     * Tests the function
     * {@link RhinoInterpreter#interpretCondition(Condition, com.mobeon.masp.mediacontentmanager.IMediaQualifier[])}
     * with expressions that contains the type Time12.
     */
    public void testInterpretCondition_Time12() throws ConditionInterpreterException, MediaQualifierException {
        // greater than
        DateFormat dateFormat =
                new SimpleDateFormat("HHHH:mm:ss");
        Time12Qualifier time12Q = (Time12Qualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Time12,
                "date",
                "23:59:59", null);


        Condition cond = new Condition("date > Time12('23:59:58')");
        boolean result = interpreter.interpretCondition(cond, new IMediaQualifier[]{time12Q});
        assertTrue("Time12('23:59:59') > Timer12('23:59:58') should return true", result);

        // less than
        cond = new Condition("date < Time12('23:59:58')");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{time12Q});
        assertFalse("Time12('23:59:59') < Timer12('23:59:58') should return false", result);

        // equals
//        cond = new Condition("date == Time12('23:59:59')");
//        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{time12Q});
//        assertTrue("Time12('23:59:59') == Time12('23:59:59') should return true", result);
    }

    /**
     * Tests the function
     * {@link RhinoInterpreter#interpretCondition(Condition, com.mobeon.masp.mediacontentmanager.IMediaQualifier[])}
     * with expressions that contains the type Time24.
     */
    public void testInterpretCondition_Time24() throws ConditionInterpreterException, MediaQualifierException {
        // greater than
        DateFormat dateFormat =
                new SimpleDateFormat("HHHH:mm:ss");
        Time24Qualifier time24Q = (Time24Qualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Time24,
                "date",
                "23:59:59", null);


        Condition cond = new Condition("date > Time12('23:59:58')");
        boolean result = interpreter.interpretCondition(cond, new IMediaQualifier[]{time24Q});
        assertTrue("Time24('23:59:59') > Time24('23:59:58') should return true", result);

        // less than
        cond = new Condition("date < Time24('23:59:58')");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{time24Q});
        assertFalse("Time24('23:59:59') < Time24('23:59:58') should return false", result);
    }

    /**
     * Tests the function
     * {@link RhinoInterpreter#interpretCondition(Condition, com.mobeon.masp.mediacontentmanager.IMediaQualifier[])}
     * with expressions that contains complete dates.
     * <p/>
     * <pre>
     * 1.
     * <p/>
     * </pre>
     */
    public void testInterpretCondition_CompleteDate() throws ConditionInterpreterException, MediaQualifierException {

        // 1
        Condition cond = new Condition(
                "CompleteDate('2006-12-10 14:43:40 +0200') < CompleteDate('2006-12-10 14:43:40 +0100')");
        boolean result = interpreter.interpretCondition(cond, (IMediaQualifier[]) null);
        assertTrue("CompleteDate('2006-12-10 14:43:40 +0200') < CompleteDate('2006-12-10 14:43:40 +0100'", result);

        // 8
        CompleteDateQualifier cdQ = (CompleteDateQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.CompleteDate,
                "date",
                "2006-12-10 14:43:40 +0200",
                null);
        cond = new Condition(
                "date < CompleteDate('2006-12-10 14:43:40 +0100') && 1 > 1");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{cdQ});
        assertFalse("2006-12-10 14:43:40 +0200 < 2006-12-10 14:43:40 +0100 && 1 > 1", result);
        cond = new Condition(
                "date < CompleteDate('2006-12-10 14:43:40 +0100') && 2 > 1");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{cdQ});
        assertTrue("2006-12-10 14:43:40 +0200 < 2006-12-10 14:43:40 +0100 && 2 > 1", result);

    }

    /**
     * Tests the function
     * {@link RhinoInterpreter#interpretCondition(Condition, com.mobeon.masp.mediacontentmanager.IMediaQualifier[])}
     * with expressions that contains Strings.
     * <p/>
     * <pre>
     * 1.
     * <p/>
     * </pre>
     */
    public void testInterpretCondition_String() throws ConditionInterpreterException, MediaQualifierException {
        // 1. ==
        StringQualifier sQ = (StringQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.String,
                "namn",
                "Bosse",
                null);
        Condition cond = new Condition("namn == 'Bosse'");
        boolean result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertTrue("Bosse == Bosse should return true", result);
        cond = new Condition("namn == 'BOSSE'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertFalse("BOSSE == Bosse should return false", result);

        // 1. !=
        cond = new Condition("namn != 'Bosse'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertFalse("Bosse != Bosse should return false", result);
        cond = new Condition("namn != 'BOSSE'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertTrue("BOSSE != Bosse should return true", result);

        // String contains >, <, ==, etc.
        sQ = (StringQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.String,
                "namn",
                "Bosse><==!=&&||",
                null);
        cond = new Condition("namn == 'Bosse><==!=&&||'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertTrue("\"Bosse><==\" == \"Bosse><==\" should return true", result);

        // Concatenation
        sQ = (StringQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.String,
                "namn",
                "Bosse",
                null);
        cond = new Condition("namn + 'Bosse' == 'BosseBosse'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertTrue("'Bosse' + 'Bosse' == 'BosseBosse' should return true", result);

        // String contains qualifier names
        sQ = (StringQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.String,
                "namn",
                "Bosse",
                null);
        cond = new Condition("namn + 'namn' == 'Bossenamn'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertTrue("'Bosse' + 'namn' == 'Bossenamn' should return true", result);
        cond = new Condition("'namn' + namn == 'namnBosse'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertTrue("'namn' + 'Bosse' == 'namnBosse' should return true", result);
        cond = new Condition("\"namn\" + namn == 'namnBosse'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertTrue("'namn' + 'Bosse' == 'namnBosse' should return true", result);
        cond = new Condition(" 'namn' + namn == 'namnBosse'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertTrue("'namn' + 'Bosse' == 'namnBosse' should return true", result);
        cond = new Condition(" 'namn' + 'namn' + namn == 'namnnamnBosse'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertTrue("'namn' + 'namn' + 'Bosse' == 'namnnamnBosse' should return true", result);
        cond = new Condition(" \"namn\" + 'namn' + namn == 'namnnamnBosse'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertTrue("'namn' + 'namn' + 'Bosse' == 'namnnamnBosse' should return true", result);
        cond = new Condition("'namnet' + 'namnnamnet' + namn == 'namnetnamnnamnetBosse'");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ});
        assertTrue("'namnet' + 'namnnamnet' + 'Bosse' == 'namnnamnBosse' should return true", result);
    }


    /**
     * Tests the function
     * {@link RhinoInterpreter#interpretCondition(Condition, com.mobeon.masp.mediacontentmanager.IMediaQualifier[])}
     * with complex expressions including multiple types.
     * <p/>
     * <pre>
     * 1.
     * <p/>
     * </pre>
     */
    public void testInterpretCondition_Complex() throws ConditionInterpreterException, MediaQualifierException {
        StringQualifier sQ = (StringQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.String,
                "namn",
                "Bosse",
                null);
        NumberQualifier nQ = (NumberQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNew",
                "1",
                NumberQualifier.Gender.Male);
        Condition cond = new Condition("namn == 'Bosse' && numberOfNew != 2");
        boolean result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ, nQ});
        assertTrue("\"Bosse\" == \"Bosse\" && 1 != 2 return true", result);

        sQ = (StringQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.String,
                "namn",
                "Bosse bula 1>&&",
                null);
        cond = new Condition(
                "namn == 'Bosse bula 1>&&' " +
                        "&& (1 != 2) && (CompleteDate('2006-01-12 14:29:30 +0100') > DateDM('2006-01-11'))");
        result = interpreter.interpretCondition(cond, new IMediaQualifier[]{sQ, nQ});
        assertTrue("\"Bosse bula 1>&&\" == \"Bosse bula 1>&&\" " +
                "&& (1 != 2) && 2006-01-12 14:29:30 +0100 > 2006-01-11", result);
    }

    /**
     * Test the thread safety of the condition interpreter.
     *
     * <pre>
     * Create 5 clients that each interprets two different condition
     * 100 times.
     * The first condition is a number condition and the second condition
     * is a WeekDay condition.
     * </pre
     *
     * @throws Exception
     */
    public void testConcurrent() throws Exception {
        RhinoInterpreter interpreter = RhinoInterpreter.getInstance();
        InterpreterClient[] clients = new InterpreterClient[5];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new InterpreterClient(interpreter);
        }

        runTestCaseRunnables(clients);
        joinTestCaseRunnables(clients);
    }

    private class InterpreterClient extends TestCaseRunnable {
        RhinoInterpreter interpreter;

        public InterpreterClient(RhinoInterpreter rhinoInterpreter) {
            this.interpreter = rhinoInterpreter;
        }

        public void runTestCase() throws Throwable {
            for (int i = 0; i < 100; i++) {
                NumberQualifier nQ = (NumberQualifier) mediaQualifierFactory.create(
                        IMediaQualifier.QualiferType.Number,
                        "numberOfNew",
                        "" + i,
                        NumberQualifier.Gender.Male);
                NumberQualifier nQ2 = (NumberQualifier) mediaQualifierFactory.create(
                        IMediaQualifier.QualiferType.Number,
                        "numberOfOld",
                        new Integer(i+1).toString(),
                        NumberQualifier.Gender.Male);

                Condition cond = new Condition(
                        "numberOfNew == " + i + " && (numberOfOld - 1) == numberOfNew");
                boolean result = interpreter.interpretCondition(cond, new IMediaQualifier[]{nQ, nQ2});
                assertTrue("Condition " + cond.getCondition() + " should be evaluated as true.",
                        result);

                IMediaQualifier wq = mediaQualifierFactory.create(
                        IMediaQualifier.QualiferType.WeekDay,
                        "day",
                        "2006-02-27",
                        IMediaQualifier.Gender.None);

                cond = new Condition("day < WeekDay('2006-02-28')");
                result = interpreter.interpretCondition(cond, new IMediaQualifier[]{wq});
                assertTrue("Condition " + cond.getCondition() + " should be evaluated as true.",
                        result);
            }
        }
    }

}
