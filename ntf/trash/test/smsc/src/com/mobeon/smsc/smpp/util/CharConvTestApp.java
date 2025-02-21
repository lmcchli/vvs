/**
 * Copyright (c) 2006 Mobeon AB
 * All Rights Reserved
 *
 * Test application for CharsetConverter class
 */
package com.mobeon.smsc.smpp.util;

import com.mobeon.smsc.smpp.util.CharsetConverter;

class CharConvTestApp {
    public static void main (String args[]) {

        CharsetConverter converter = new CharsetConverter();
	String gsmStr = "";
        String isoStr = ""
             + "@"      + "\u00A3" + "$"      + "¥"      + "è"      + "é"      + "ù"      + "ì"
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
             + "x"      + "y"      + "z"      + "ä"      + "ö"      + "ñ"      + "ü"      + "à"

             + "^" + "{" + "}" + "\\" + "[" + "~" + "]" + "|";

	if(args.length!=0) {
		isoStr = "";

		for(int i=0;i<args.length;i++) {
			isoStr = isoStr + args[i];
		}
	}
	else {
		System.out.println("\nNo argument(s), using default ISO string.\n");
	}

	gsmStr = converter.iso2Gsm(isoStr);

	System.out.println("\nOriginal string repr (ISO)   : " + isoStr);
	System.out.println("\n->Converted string repr (GSM): " + gsmStr);
	System.out.println("\n->Converted string repr (ISO): " + converter.gsm2Iso(gsmStr));
    }
}
