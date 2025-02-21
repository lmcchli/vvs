package com.mobeon.masp.mediacontentmanager.qualifier;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import junit.framework.TestCase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Unit tests for class {@link WeekdayQualifier}.
 *
 *
 * @author Mats Egland
 */
public class WeekdayQualifierTest extends TestCase {
    private WeekdayQualifier weekdayQualifier;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private Date date;
    private static final String NAME = "weekday";
    public void setUp() throws ParseException {
        date = df.parse("1999-12-30");
        weekdayQualifier = new WeekdayQualifier(NAME, date, IMediaQualifier.Gender.Male );
    }

    /**
     * Test for method
     * {@link com.mobeon.masp.mediacontentmanager.qualifier.WeekdayQualifier#getType()}.
     *
     * <pre>
     * 1. Test that type is weekday
     *  Condition:
     *  Action:
     *      getType()
     *  Result:
     *      Type is 'Weekday'.
     * </pre>
     */
    public void testGetType() {
        IMediaQualifier.QualiferType type = weekdayQualifier.getType();
        assertEquals("Type should be " + IMediaQualifier.QualiferType.WeekDay,
                IMediaQualifier.QualiferType.WeekDay, type);
    }

    /**
     * Test for method
     * {@link WeekdayQualifier#getValueType()}.
     *
     * <pre>
     * 1. Test that type is Date
     *  Condition:
     *  Action:
     *      getValueType()
     *  Result:
     *      Type is Date.
     * </pre>
     */
    public void testGetValueType() {
        Class theClass = weekdayQualifier.getValueType();
        assertEquals("VAlue type should be class",
                Date.class, theClass);
    }

    /**
     * Test for method
     * {@link com.mobeon.masp.mediacontentmanager.qualifier.WeekdayQualifier#getValue()}.
     */
    public void testGetValue() {
        assertSame("", date, weekdayQualifier.getValue());
    }
    /**
     * Test for method
     * {@link WeekdayQualifier#getName()}.
     *
     */
    public void testGetName() {

        assertEquals("Name of qualifier should be " + NAME,
                NAME, weekdayQualifier.getName());
    }

}
