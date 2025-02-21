package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * Address Tester.
 *
 * @author qhast
 */
public class AddressTest extends TestCase
{
    private Address defaultConstructedAddress;

    public AddressTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        defaultConstructedAddress = new Address();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetSetCommonName() throws Exception
    {
        assertEquals(null,defaultConstructedAddress.getCommonName());
        defaultConstructedAddress.setCommonName("nisse");
        assertEquals("nisse",defaultConstructedAddress.getCommonName());
    }

    public void testGetSetPhoneNumber() throws Exception
    {
        assertEquals(null,defaultConstructedAddress.getPhoneNumber());
        defaultConstructedAddress.setPhoneNumber("0123456789");
        assertEquals("0123456789",defaultConstructedAddress.getPhoneNumber());
    }

    public void testGetSetEmailAddress() throws Exception
    {
        assertEquals(null,defaultConstructedAddress.getEmailAddress());
        defaultConstructedAddress.setEmailAddress("nisse@mobeon.com");
        assertEquals("nisse@mobeon.com",defaultConstructedAddress.getEmailAddress());
    }

    public void testStringRepresentation() throws Exception
    {
        assertEquals("\"nisse (0123456789)\" <nisse@mobeon.com>", new Address("nisse","0123456789","nisse@mobeon.com").stringRepresentation());

        assertEquals("0123456789 <nisse@mobeon.com>", new Address("","0123456789","nisse@mobeon.com").stringRepresentation());
        assertEquals("0123456789 <nisse@mobeon.com>", new Address(null,"0123456789","nisse@mobeon.com").stringRepresentation());

        assertEquals("nisse <nisse@mobeon.com>", new Address("nisse","","nisse@mobeon.com").stringRepresentation());
        assertEquals("nisse <nisse@mobeon.com>", new Address("nisse",null,"nisse@mobeon.com").stringRepresentation());

        assertEquals("\"nisse (0123456789)\" <>", new Address("nisse","0123456789","").stringRepresentation());
        assertEquals("\"nisse (0123456789)\" <>", new Address("nisse","0123456789",null).stringRepresentation());
        assertEquals("\"nisse (0123456789)\" <>", new Address("nisse","0123456789","nisse").stringRepresentation());

        assertEquals("nisse <>", new Address("nisse","","").stringRepresentation());
        assertEquals("0123456789 <>", new Address("","0123456789","").stringRepresentation());
        assertEquals("<>", new Address("","","").stringRepresentation());

    }

    public void testToString() throws Exception
    {
        assertEquals("null (null) <null>",defaultConstructedAddress.toString());

        defaultConstructedAddress.setCommonName("nisse");
        assertEquals("nisse (null) <null>",defaultConstructedAddress.toString());

        defaultConstructedAddress.setPhoneNumber("0123456789");
        assertEquals("nisse (0123456789) <null>",defaultConstructedAddress.toString());

        defaultConstructedAddress.setEmailAddress("nisse@mobeon.com");
        assertEquals("nisse (0123456789) <nisse@mobeon.com>",defaultConstructedAddress.toString());
    }

    public void testValidCommonNamePatterns() throws Exception {
        assertTrue(Address.COMMON_NAME_PATTERN.matcher("Hakan Stolt").matches());
        assertTrue(Address.COMMON_NAME_PATTERN.matcher("Håkan Stolt").matches());
        assertTrue(Address.COMMON_NAME_PATTERN.matcher("Håkan Alfred Stolt").matches());
    }

    public void testInvalidCommonNamePatterns() throws Exception {
        assertFalse(Address.COMMON_NAME_PATTERN.matcher(" hakan stolt").matches());
        assertFalse(Address.COMMON_NAME_PATTERN.matcher("hakan\"stolt").matches());
    }

    public void testValidEmailAddressPatterns() throws Exception {
        assertTrue(Address.EMAILADDRESS_PATTERN.matcher("hakan.stolt@mobeon.com").matches());
        assertTrue(Address.EMAILADDRESS_PATTERN.matcher("qhast@mobeon.com").matches());
        assertTrue(Address.EMAILADDRESS_PATTERN.matcher("qwerty_t@mobeon.com").matches());
    }

    public void testInvalidEmailAddressPatterns() throws Exception {
        assertFalse(Address.EMAILADDRESS_PATTERN.matcher("hakan.stolt").matches());
        assertFalse(Address.EMAILADDRESS_PATTERN.matcher("qhast-mobeon.com").matches());
        assertFalse(Address.EMAILADDRESS_PATTERN.matcher("unspecified-domain").matches());
        assertFalse(Address.EMAILADDRESS_PATTERN.matcher("07012345678").matches());
        assertFalse(Address.EMAILADDRESS_PATTERN.matcher("+07012345678").matches());
    }

    public void testValidPhoneNumberPatterns() throws Exception {
        assertTrue(Address.PHONE_NUMBER_PATTERN.matcher("07012345678").matches());
        assertTrue(Address.PHONE_NUMBER_PATTERN.matcher("+467012345678").matches());
    }

    public void testInvalidPhoneNumberPatterns() throws Exception {
        assertFalse(Address.PHONE_NUMBER_PATTERN.matcher("070-12345678").matches());
        assertFalse(Address.PHONE_NUMBER_PATTERN.matcher("070 12345678").matches());
        assertFalse(Address.PHONE_NUMBER_PATTERN.matcher("+46 70 12345678").matches());
        assertFalse(Address.PHONE_NUMBER_PATTERN.matcher("nisse@mobeon.com").matches());
        assertFalse(Address.PHONE_NUMBER_PATTERN.matcher("+").matches());
    }

    public void testValidSenderAddressPatterns() throws Exception {
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("\"Hakan Stolt (070123456789)\" <hakan.stolt@mobeon.com>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("\"Hakan Stolt (+4670123456789)\" <hakan.stolt@mobeon.com>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("\"Hakan Stolt\" <hakan.stolt@mobeon.com>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("Hakan Stolt <hakan.stolt@mobeon.com>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("Hakan Stolt <>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("Håkan Alfred Stolt <hakan.stolt@mobeon.com>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("\"Håkan Alfred Stolt (070123456789)\" <hakan.stolt@mobeon.com>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("070123456789 <hakan.stolt@mobeon.com>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("070123456789 <>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("+4670123456789 <>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("\"070123456789\" <hakan.stolt@mobeon.com>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("+4670123456789 <hakan.stolt@mobeon.com>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("\"+4670123456789\" <hakan.stolt@mobeon.com>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("Number Withheld <>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("Unknown Caller <>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("<>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("123 123 <tomas@mvas.su.erm.abcxyz.se>").matches());
        assertTrue(Address.SENDER_ADDRESS_PATTERN.matcher("<tomas@mvas.su.erm.abcxyz.se>").matches());
    }

    public void testInvalidSenderAddressPatterns() throws Exception {
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher(" hakan stolt").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("hakan\"stolt").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("Hakan Stolt").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("Håkan Stolt").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("Håkan Alfred Stolt").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("Hakan Stolt (070123456789) <hakan.stolt@mobeon.com>").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("\"Hakan Stolt\" <hakan.stolt>").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("Hakan (123456789) Stolt <hakan.stolt@mobeon.com>").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("\"Håkan\" Alfred Stolt <hakan.stolt@mobeon.com>").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("\"Håkan Alfred Stolt\" (070123456789) <hakan.stolt@mobeon.com>").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("(070123456789) <hakan.stolt@mobeon.com>").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("\"(070123456789)\" <hakan.stolt@mobeon.com>").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("+(070123456789) <>").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("\"Hakan Stolt (070123456789) <hakan.stolt@mobeon.com>\"").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("").matches());
        assertFalse(Address.SENDER_ADDRESS_PATTERN.matcher("Tomas A <tomas@lab.mobeon.com> (1022)").matches());
    }


    public void testParseInvalidAddresses() throws Exception {

        try {
            Address.parse("");
            fail("Calling Address.parse() with an invalid string null argument should throw AddressParseException!");
        } catch (AddressParseException e) {
            //OK
            assertEquals("",e.getInvalidAddressString());
        }

        try {
            Address.parse("Håkan Alfred Stolt");
            fail("Calling Address.parse() with an invalid string null argument should throw AddressParseException!");
        } catch (AddressParseException e) {
            //OK
            assertEquals("Håkan Alfred Stolt",e.getInvalidAddressString());
        }

        try {
            Address.parse(null);
            fail("Calling Address.parse() with a null argument should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

    }


    public void testParseValidAddresses() throws Exception {
        Address address = Address.parse("\"Hakan Stolt (070123456789)\" <hakan.stolt@mobeon.com>");
        assertEquals("Common name is wrong!","Hakan Stolt",address.getCommonName());
        assertEquals("Phone number is wrong!","070123456789",address.getPhoneNumber());
        assertEquals("Email address is wrong!","hakan.stolt@mobeon.com",address.getEmailAddress());

        address = Address.parse("Hakan Stolt <hakan.stolt@mobeon.com>");
        assertEquals("Common name is wrong!","Hakan Stolt",address.getCommonName());
        assertEquals("Phone number is wrong!",null,address.getPhoneNumber());
        assertEquals("Email address is wrong!","hakan.stolt@mobeon.com",address.getEmailAddress());

        address = Address.parse("\"Hakan Stolt\" <hakan.stolt@mobeon.com>");
        assertEquals("Common name is wrong!","Hakan Stolt",address.getCommonName());
        assertEquals("Phone number is wrong!",null,address.getPhoneNumber());
        assertEquals("Email address is wrong!","hakan.stolt@mobeon.com",address.getEmailAddress());

        address = Address.parse("Hakan Stolt <>");
        assertEquals("Common name is wrong!","Hakan Stolt",address.getCommonName());
        assertEquals("Phone number is wrong!",null,address.getPhoneNumber());
        assertEquals("Email address is wrong!",null,address.getEmailAddress());

        address = Address.parse("12345678 <hakan.stolt@mobeon.com>");
        assertEquals("Common name is wrong!",null,address.getCommonName());
        assertEquals("Phone number is wrong!","12345678",address.getPhoneNumber());
        assertEquals("Email address is wrong!","hakan.stolt@mobeon.com",address.getEmailAddress());

        address = Address.parse("+12345678 <hakan.stolt@mobeon.com>");
        assertEquals("Common name is wrong!",null,address.getCommonName());
        assertEquals("Phone number is wrong!","+12345678",address.getPhoneNumber());
        assertEquals("Email address is wrong!","hakan.stolt@mobeon.com",address.getEmailAddress());

        address = Address.parse("1234 5678 <hakan.stolt@mobeon.com>");
        assertEquals("Common name is wrong!","1234 5678",address.getCommonName());
        assertEquals("Phone number is wrong!",null,address.getPhoneNumber());
        assertEquals("Email address is wrong!","hakan.stolt@mobeon.com",address.getEmailAddress());

    }


    public static Test suite()
    {
        return new TestSuite(AddressTest.class);
    }
}
