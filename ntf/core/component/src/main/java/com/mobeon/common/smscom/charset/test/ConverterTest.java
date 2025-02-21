/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.charset.test;

import com.mobeon.common.smscom.charset.Converter;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.util.NtfUtil;
import java.io.StringBufferInputStream;
import java.util.*;
import junit.framework.*;

/**
 * Test of Converter, NTFs default character converter
 */
public class ConverterTest extends NtfTestCase {

    String config =
        "DataCodingScheme=0\n"
        + "Converter=Converter\n"
        + "#Convert unsupported characters to reverse question-marks.\n"
        + "0000-FFFF=60\n"
        + "#Just chop characters that are the same as ASCII\n"
        + "000A,000D,0020-007A=Chop\n"
        + "\n"
        + "0060=60\n"
        + "#Currency symbols and accented characters\n"
        + "0040=00\n"
        + "00A3=01\n"
        + "0024=02\n"
        + "00A5=03\n"
        + "00E8=04\n"
        + "00E9=05\n"
        + "00F9=06\n"
        + "00EC=07\n"
        + "00F2=08\n"
        + "00C7=09\n"
        + "00A4=24\n"
        + "20AC=1B65\n"
        + "# 0A is line feed\n"
        + "00D8=0B\n"
        + "00F8=0C\n"
        + "# 0C is form feed\n"
        + "00C5=0E\n"
        + "00E5=0F\n"
        + "#Greek characters\n"
        + "0394=10\n"
        + "005F=11\n"
        + "03A6=12\n"
        + "0393=13\n"
        + "039B=14\n"
        + "03A9=15\n"
        + "03A0=16\n"
        + "03A8=17\n"
        + "03A3=18\n"
        + "0398=19\n"
        + "039E=1A\n"
        + "# 1B is escape to extension table\n"
        + "# Misc. european characters\n"
        + "00C6=1C\n"
        + "00E6=1D\n"
        + "00DF=1E\n"
        + "00C9=1F\n"
        + "00A1=40\n"
        + "00C4=5B\n"
        + "00D6=5C\n"
        + "00D1=5D\n"
        + "00DC=5E\n"
        + "00A7=5F\n"
        + "00BF=60\n"
        + "00E4=7B\n"
        + "00F6=7C\n"
        + "00F1=7D\n"
        + "00FC=7E\n"
        + "00E0=7F\n"
        + "#Brackets etc.\n"
        + "005E=1B14\n"
        + "007B=1B28\n"
        + "007D=1B29\n"
        + "005C=1B2F\n"
        + "005B=1B3C\n"
        + "007E=1B3D\n"
        + "005D=1B3E\n"
        + "007C=1B40\n"
        + "6666=Chop\n"
        + "7777=Keep\n"
        + "8888=Delete\n";

    Converter conv;

    public ConverterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        Properties p = new Properties();
        p.load(new StringBufferInputStream(config));

        conv = Converter.get(p);
    }

    int posChar(byte b) {
        return b >= 0
            ? b
            : 256 + b;
    }

    /*
     * Test that find finds an entry if it exists, that it finds the correct
     * entry, and that it does not find non-existing entries.
     */
    public void testGsmDefault() throws Exception {
        l("testGsmDefault");

        int i;
        byte[] converted = null;
        byte[] expected = {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
            0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x20, 0x1c, 0x1d, 0x1e, 0x1f,
            0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f,
            0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f,
            0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f,
            0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f,
            0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f,
        };


        converted = conv.unicodeToBytes
            //These strings are all characters in the GSM Default character set.
            //YOU MUST USE iso-latin-1 TO EDIT THIS FILE. The \ u00xx things are
            //characters not in iso-latin-1, mainly greek characters.
            (""
             + "@"      + "£"      + "$"      + "¥"      + "è"      + "é"      + "ù"      + "ì"
             + "\u00F2" + "\u00C7" + "\n"     + "\u00D8" + "\u00F8" + "\r"     + "Å"      + "å"

             + "\u0394" + "_"      + "\u03A6" + "\u0393" + "\u039B" + "\u03A9" + "\u03A0" + "\u03A8"
             + "\u03A3" + "\u0398" + "\u039E" + " "      + "Æ"      + "æ"      + "ß"      + "É"

             + " "      + "!"      + "\""     + "#"      + "¤"      + "%"      + "&"      + "'"
             + "("      + ")"      + "*"      + "+"      + ","      + "-"      + "."      + "/"

             + "0"      + "1"      + "2"      + "3"      + "4"      + "5"      + "6"      + "7"
             + "8"      + "9"      + ":"      + ";"      + "<"      + "="      + ">"      + "?"

             + "¡"      + "A"      + "B"      + "C"      + "D"      + "E"      + "F"      + "G"
             + "H"      + "I"      + "J"      + "K"      + "L"      + "M"      + "N"      + "O"

             + "P"      + "Q"      + "R"      + "S"      + "T"      + "U"      + "V"      + "W"
             + "X"      + "Y"      + "Z"      + "Ä"      + "Ö"      + "Ñ"      + "Ü"      + "§"

             + "¿"      + "a"      + "b"      + "c"      + "d"      + "e"      + "f"      + "g"
             + "h"      + "i"      + "j"      + "k"      + "l"      + "m"      + "n"      + "o"

             + "p"      + "q"      + "r"      + "s"      + "t"      + "u"      + "v"      + "w"
             + "x"      + "y"      + "z"      + "ä"      + "ö"      + "ñ"      + "ü"      + "à",
             0);

        assertEquals(expected.length, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }
    }

    public void testGsmExtension1() throws Exception {
        l("testGsmExtension1");

        int i;
        byte[] converted = null;
        byte[] expected = {
            0x1B, 0x14, 0x1B, 0x28, 0x1B, 0x29, 0x1B, 0x2F, 0x1B, 0x3C, 0x1B, 0x3D, 0x1B, 0x3E, 0x1B, 0x40,
            0x1B, 0x65
        };


        converted = conv.unicodeToBytes
            //These strings are all characters in the GSM Default character set.
            //YOU MUST USE iso-latin-1 TO EDIT THIS FILE. The u00xx things are
            //characters not in iso-latin-1, mainly greek characters.
            (""
             + "^"      + "{"      + "}"      + "\\"      + "["      + "~"      + "]"      + "|"
             + "\u20AC",
             0);

        assertEquals(expected.length, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }
    }

    public void testCommands() throws Exception {
        l("testCommands");

        int i;
        byte[] converted = null;
        byte[] expected = {
            0x66, 0x77, 0x77,
        };


        converted = conv.unicodeToBytes
            //These strings are all characters in the GSM Default character set.
            //YOU MUST USE iso-latin-1 TO EDIT THIS FILE. The u00xx things are
            //characters not in iso-latin-1, mainly greek characters.
            (""
             + "\u6666"      + "\u7777"      + "\u8888",
             0);

        assertEquals(expected.length, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }
    }

    public void testLength() throws Exception {
        l("testLength");

        int i;
        byte[] converted = null;
        byte[] expected = {
            0x30, 0x31, 0x32, 0x33, 0x34,
        };


        converted = conv.unicodeToBytes
            //These strings are all characters in the GSM Default character set.
            //YOU MUST USE iso-latin-1 TO EDIT THIS FILE. The u00xx things are
            //characters not in iso-latin-1, mainly greek characters.
            ("0123456789", 5);

        assertEquals(expected.length, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }
    }

    public void testSignWrapAround() throws Exception {
        l("testSignWrapAround");

        String config = "DataCodingScheme=0\n"
            + "Converter=Converter\n"
            + "7FFF-8001=55\n";

        Properties p = new Properties();
        p.load(new StringBufferInputStream(config));
        conv = Converter.get(p);

        assertEquals(0x00, posChar(conv.unicodeToBytes("\u0000", 0)[0]));
        assertEquals(0xFE, posChar(conv.unicodeToBytes("\u7FFE", 0)[0]));
        assertEquals(0x55, posChar(conv.unicodeToBytes("\u7FFF", 0)[0]));
        assertEquals(0x55, posChar(conv.unicodeToBytes("\u8000", 0)[0]));
        assertEquals(0x55, posChar(conv.unicodeToBytes("\u8001", 0)[0]));
        assertEquals(0x02, posChar(conv.unicodeToBytes("\u8002", 0)[0]));
        assertEquals(0xFF, posChar(conv.unicodeToBytes("\uFFFF", 0)[0]));
    }

    public void testUcs2() throws Exception {
        l("testUcs2");
        byte[] converted = null;

        String config = "DataCodingScheme=0\n"
            + "Converter=Converter\n"
            + "0000-FFFF=UCS2\n"
            + "0000,FFFF=55\n"
            + "0001=delete\n";

        Properties p = new Properties();
        p.load(new StringBufferInputStream(config));
        conv = Converter.get(p);
        converted = conv.unicodeToBytes("\u0000\u0001\uFFFF", 0);
        assertEquals(2, converted.length);
        assertEquals(0x55, posChar(converted[0]));
        assertEquals(0x55, posChar(converted[1]));

        converted = conv.unicodeToBytes("\u0000\u0001\u1000", 0);
        assertEquals(4, converted.length);
        assertEquals(0x00, posChar(converted[0]));
        assertEquals(0x00, posChar(converted[1]));
        assertEquals(0x10, posChar(converted[2]));
        assertEquals(0x00, posChar(converted[3]));
    }

    public void testUcs2Length() throws Exception {
        l("testUcs2Length");
        byte[] converted = null;

        String config = "DataCodingScheme=0\n"
            + "Converter=Converter\n"
            + "0000-FFFF=UCS2\n"
            + "0030-0039=Chop\n";

        Properties p = new Properties();
        p.load(new StringBufferInputStream(config));
        conv = Converter.get(p);
        converted = conv.unicodeToBytes("0123456789", 4);
        assertEquals(4, converted.length);
        assertEquals(0x30, posChar(converted[0]));
        assertEquals(0x33, posChar(converted[3]));

        converted = conv.unicodeToBytes("0x123456789", 4);
        assertEquals(4, converted.length);
        assertEquals(0x00, posChar(converted[0]));
        assertEquals(0x30, posChar(converted[1]));
        assertEquals(0x00, posChar(converted[2]));
        assertEquals(0x78, posChar(converted[3]));
    }

    public void testUnPacked() throws Exception {
        l("testUnPacked");

        int i;
        byte[] converted = null;
        String config = "DataCodingScheme=0\n"
            + "Converter=Converter\n"
            + "0000-FFFF=Chop\n";

        Properties p = new Properties();
        p.load(new StringBufferInputStream(config));
        conv = Converter.get(p);

        converted = conv.unicodeToBytes("", 0);
        assertEquals(0, converted.length);

        byte[] expected = {0x42, 0x41, 0x44, 0x43, 0x46, 0x45, 0x48, 0x47,
                           0x4A, 0x49, 0x4C, 0x4B, 0x4E, 0x4D, 0x50, 0x4F,
                           0x51};
        converted = conv.unicodeToBytes("BADCFEHGJILKNMPOQ", 0);
        assertEquals(expected.length, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }
    }

    public void testPacked() throws Exception {
        l("testPacked");

        int i;
        byte[] converted = null;
        Properties p = new Properties();
        String config = "DataCodingScheme=0\n"
            + "Converter=Converter\n"
            + "Pack=yes\n"
            + "0000-FFFF=Chop\n";

        p.load(new StringBufferInputStream(config));
        log.logMessage("" + p);
        conv = Converter.get(p);
        log.logMessage("" + conv);

        converted = conv.unicodeToBytes("", 0);
        assertEquals(0, converted.length);

        //1000001
        //100001 0
        //10000 11
        //1000 100
        //100 0101
        //10 00110
        //1 000111
        // 1001000
        //1001001
        //100101 0
        //10010 11
        //1001 100
        //100 1101
        //10 01110x
        //1 001111
        // 1010000
        //1010001
        byte[] expected = {0x41, (byte) 0xE1, (byte) 0x90, 0x58, 0x34, 0x1E, (byte) 0x91, 0x49,
                    (byte) 0xE5, (byte) 0x92, (byte) 0xD9, 0x74, 0x3E, (byte) 0xA1, 0x51,};
        converted = conv.unicodeToBytes("ABCDEFGHIJKLMNOPQ", 0);
        assertEquals(expected.length, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }
    }

    public void testExactlyPacked() throws Exception {
        l("testExactlyPacked");

        int i;
        byte[] converted = null;
        Properties p = new Properties();
        String config = "DataCodingScheme=0\n"
            + "Converter=Converter\n"
            + "Pack=yes\n"
            + "0000-FFFF=Chop\n";

        p.load(new StringBufferInputStream(config));
        log.logMessage("" + p);
        conv = Converter.get(p);
        log.logMessage("" + conv);

        converted = conv.unicodeToBytes("", 0);
        assertEquals(0, converted.length);

        //1000001
        //100001 0
        //10000 11
        //1000 100
        //100 0101
        //10 00110
        //1 000111
        // 1001000
        byte[] expected = {0x41, (byte) 0xE1, (byte) 0x90, 0x58, 0x34, 0x1E, (byte) 0x91,};
        converted = conv.unicodeToBytes("ABCDEFGH", 0);
        assertEquals(expected.length, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }
    }

    public void testPackedLength() throws Exception {
        l("testPackedLength");

        int i;
        byte[] converted = null;
        Properties p = new Properties();
        String config = "DataCodingScheme=0\n"
            + "Converter=Converter\n"
            + "Pack=yes\n"
            + "0000-FFFF=Chop\n";

        p.load(new StringBufferInputStream(config));
        log.logMessage("" + p);
        conv = Converter.get(p);
        log.logMessage("" + conv);

        byte[] expected = new byte[7];
        int size = 0;

        //  6666666666 5555555555 4444444444 3333333333 2222222222 1111111111 0000000000
        //. 1001 000.1 0001 11.10 0011 0.100 0101. 1000 100.1 0000 11.10 0001 0.100 0001
        converted = conv.unicodeToBytes("ABCDEFGHIJKLMNOPQ", 1);
        expected[0] = 0x41;
        assertEquals(1, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }

        converted = conv.unicodeToBytes("ABCDEFGHIJKLMNOPQ", 2);
        expected[1] = 0x21;
        assertEquals(2, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }

        converted = conv.unicodeToBytes("ABCDEFGHIJKLMNOPQ", 3);
        expected[1] = (byte) 0xE1;
        expected[2] = 0x10;
        assertEquals(3, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }

        converted = conv.unicodeToBytes("ABCDEFGHIJKLMNOPQ", 4);
        expected[2] = (byte)0x90;
        expected[3] = 0x08;
        assertEquals(4, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }

        converted = conv.unicodeToBytes("ABCDEFGHIJKLMNOPQ", 5);
        expected[3] = 0x58;
        expected[4] = 0x04;
        assertEquals(5, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }

        converted = conv.unicodeToBytes("ABCDEFGHIJKLMNOPQ", 6);
        expected[4] = 0x34;
        expected[5] = 0x02;
        assertEquals(6, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }

        converted = conv.unicodeToBytes("ABCDEFGHIJKLMNOPQ", 7);
        expected[5] = (byte)0x1E;
        expected[6] = (byte) 0x91;
        assertEquals(7, converted.length);
        for (i = 0; i < converted.length; i++) {
            assertEquals(i * 1000 + posChar(expected[i]), i * 1000 + posChar(converted[i]));
        }
    }
}
