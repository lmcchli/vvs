package com.mobeon.masp.mediacontentmanager.qualifier;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import junit.framework.TestCase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Unit tests for class {@link Time12Qualifier}.
 *
 *
 * @author Mats Egland
 */
public class Time12QualifierTest extends TestCase {
    private Time12Qualifier time12Qualifier;
    private DateFormat df = new SimpleDateFormat("HHHH:mm:ss");
    private Date date;
    private static final String NAME = "time";
    private static final String TIME = "23:59:59";

    public void setUp() throws ParseException {
        date = df.parse(TIME);
        time12Qualifier = new Time12Qualifier(NAME, date, IMediaQualifier.Gender.Male );
    }

    /**
     * Test for method
     * {@link com.mobeon.masp.mediacontentmanager.qualifier.Time12Qualifier#getType()}.
     *
     * <pre>
     * 1. Test that type is time12
     *  Condition:
     *  Action:
     *      getType()
     *  Result:
     *      Type is 'Time12'.
     * </pre>
     */
    public void testGetType() {
        IMediaQualifier.QualiferType type = time12Qualifier.getType();
        assertEquals("Type should be " + IMediaQualifier.QualiferType.Time12,
                IMediaQualifier.QualiferType.Time12, type);
    }

    /**
     * Test for method
     * {@link Time12Qualifier#getValueType()}.
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
        Class theClass = time12Qualifier.getValueType();
        assertEquals("VAlue type should be class",
                Date.class, theClass);
    }

    /**
     * Test for method
     * {@link com.mobeon.masp.mediacontentmanager.qualifier.Time12Qualifier#getValue()}.
     */
    public void testGetValue() {
        assertSame("", date, time12Qualifier.getValue());
    }
    /**
     * Test for method
     * {@link Time12Qualifier#getName()}.
     *
     */
    public void testGetName() {

        assertEquals("Name of qualifier should be " + NAME,
                NAME, time12Qualifier.getName());
    }

    
}
