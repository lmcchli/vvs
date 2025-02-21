package com.mobeon.masp.mediacontentmanager.qualifier;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import junit.framework.TestCase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Unit tests for class {@link Time24Qualifier}.
 *
 *
 * @author Mats Egland
 */
public class Time24QualifierTest extends TestCase {
    private Time24Qualifier time24Qualifier;
    private DateFormat df = new SimpleDateFormat("HHHH:mm:ss");
    private Date date;
    private static final String NAME = "time";
    private static final String TIME = "23:59:59";

    public void setUp() throws ParseException {
        date = df.parse(TIME);
        time24Qualifier = new Time24Qualifier(NAME, date, IMediaQualifier.Gender.Male );
    }

    /**
     * Test for method
     * {@link com.mobeon.masp.mediacontentmanager.qualifier.Time24Qualifier#getType()}.
     *
     * <pre>
     * 1. Test that type is time24
     *  Condition:
     *  Action:
     *      getType()
     *  Result:
     *      Type is 'Time24'.
     * </pre>
     */
    public void testGetType() {
        IMediaQualifier.QualiferType type = time24Qualifier.getType();
        assertEquals("Type should be " + IMediaQualifier.QualiferType.Time24,
                IMediaQualifier.QualiferType.Time24, type);
    }

    /**
     * Test for method
     * {@link Time24Qualifier#getValueType()}.
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
        Class theClass = time24Qualifier.getValueType();
        assertEquals("VAlue type should be class",
                Date.class, theClass);
    }

    /**
     * Test for method
     * {@link com.mobeon.masp.mediacontentmanager.qualifier.Time24Qualifier#getValue()}.
     */
    public void testGetValue() {
        assertSame("", date, time24Qualifier.getValue());
    }
    /**
     * Test for method
     * {@link Time24Qualifier#getName()}.
     *
     */
    public void testGetName() {

        assertEquals("Name of qualifier should be " + NAME,
                NAME, time24Qualifier.getName());
    }

    

}
