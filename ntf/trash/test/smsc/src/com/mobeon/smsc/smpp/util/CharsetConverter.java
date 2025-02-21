/**
 * Copyright (c) 2006 Mobeon AB
 * All Rights Reserved
 *
 * UCS2 <-> GSM 7-bit charset conversion class
 */

package com.mobeon.smsc.smpp.util;

public class CharsetConverter {
        private boolean nextByteExtended = false;
	private static char gsm2iso[] = {

	0x0040, 0x00a3, 0x0024, 0x00a5, 0x00e8, 0x00e9, 0x00f9, 0x00ec,
	0x00f2, 0x00c7, 0x000a, 0x00d8, 0x00f8, 0x000d, 0x00c5, 0x00e5,
	0x0394, 0x005f, 0x03a6, 0x0393, 0x039b, 0x03a9, 0x03a0, 0x03a8,
	0x03a3, 0x0398, 0x039e, ' ',    0x00c6, 0x00e6, 0x00df, 0x00c9,
	0x0020, 0x0021, 0x0022, 0x0023, 0x00a4, 0x0025, 0x0026, 0x0027,
	0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
	0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037,
	0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f,
	0x00a1, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047,
	0x0048, 0x0049, 0x004a, 0x004b, 0x004c, 0x004d, 0x004e, 0x004f, 
	0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057,
	0x0058, 0x0059, 0x005a, 0x00c4, 0x00d6, 0x00d1, 0x00dc, 0x00a7,
	0x00bf, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067,
	0x0068, 0x0069, 0x006a, 0x006b, 0x006c, 0x006d, 0x006e, 0x006f,
	0x0070, 0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077,
	0x0078, 0x0079, 0x007a, 0x00e4, 0x00f6, 0x00f1, 0x00fc, 0x00e0
	};

	private static char iso2gsm[] = {

        0x00, ' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' ,
	' ' , ' ' , '\n', ' ' , ' ' , '\r', ' ' , ' ' ,
	' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' ,
	' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' ,
	' ' , '!' , '"' , '#' , 0x02, '%' , '&' , '\'',
	'(' , ')' , '*' , '+' , ',' , '-' , '.' , '/' ,
	'0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' ,
	'8' , '9' , ':' , ';' , '<' , '=' , '>' , '?' ,
	
        0x00, 'A' , 'B' , 'C' , 'D' , 'E' , 'F' , 'G' ,
	'H' , 'I' , 'J' , 'K' , 'L' , 'M' , 'N' , 'O' ,
	'P' , 'Q' , 'R' , 'S' , 'T' , 'U' , 'V' , 'W' ,
	'X' , 'Y' , 'Z' , 0x3c, 0x2f, 0x3e, 0x14, 0x11,
	' ' , 'a' , 'b' , 'c' , 'd' , 'e' , 'f' , 'g' ,
	'h' , 'i' , 'j' , 'k' , 'l' , 'm' , 'n' , 'o' ,
	'p' , 'q' , 'r' , 's' , 't' , 'u' , 'v' , 'w' ,
	'x' , 'y' , 'z' , 0x28, 0x40, 0x29, 0x3d, ' ' ,
	
        ' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' ,
	' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' ,
	' ' , ' ' , ' ' , 0x13, 0x10, ' ' , ' ' , ' ' ,
	0x19, ' ' , ' ' , 0x14, ' ' , ' ' , 0x1a, ' ' ,
	0x16, 0x40, ' ' , 0x01, 0x24, 0x03, 0x12, 0x5F,
	0x17, 0x15, ' ' , ' ' , ' ' , ' ' , ' ' , ' ' ,
	' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' ,
	' ' , ' ' , ' ' , ' ' , ' ' , ' ' , ' ' , 0x60,
	
        ' ' , ' ' , ' ' , ' ' , 0x5B, 0x0E, 0x1C, 0x09,
	' ' , 0x1F, ' ' , ' ' , ' ' , ' ' , ' ' , ' ' ,
	' ' , 0x5D, ' ' , ' ' , ' ' , ' ' , 0x5C, ' ' ,
	0x0B, ' ' , ' ' , ' ' , 0x5E, ' ' , ' ' , 0x1E,
	0x7F, ' ' , ' ' , ' ' , 0x7B, 0x0F, 0x1D, ' ' ,
	0x04, 0x05, ' ' , ' ' , 0x07, ' ' , ' ' , ' ' ,
	' ' , 0x7D, 0x08, ' ' , ' ' , ' ' , 0x7C, ' ' ,
	0x0C, 0x06, ' ' , ' ' , 0x7E, ' ' , ' ' , 0xFF
	};

        private static char gsmExtended2iso[] = {
          ' ',' ',' ',' ',' ',' ',' ',' ',
          ' ',' ',' ',' ',' ',' ',' ',' ',
          ' ',' ',' ',' ', '^',' ',' ',' ',
          ' ',' ',' ',' ',' ',' ',' ',' ',
          ' ',' ',' ',' ',' ',' ',' ',' ',
          '{','}',' ',' ',' ',' ',' ','\\',
          ' ',' ',' ',' ',' ',' ',' ',' ',
          ' ',' ',' ',' ','[','~',']',' ',
          
          '|',' ',' ',' ',' ',' ',' ',' ',
          ' ',' ',' ',' ',' ',' ',' ',' ',
          ' ',' ',' ',' ',' ',' ',' ',' ',
          ' ',' ',' ',' ',' ',' ',' ',' ',
          ' ',' ',' ',' ',' ',' ',' ',' ',
          ' ',' ',' ',' ',' ',' ',' ',' ',
          ' ',' ',' ',' ',' ',' ',' ',' ',
          ' ',' ',' ',' ',' ',' ',' ',' '
        };


	private char[] tmp;

  private boolean isExtendedChar(char c) {
    return (c == '[' ||
            c == '~' ||
            c == ']' ||
            c == '^' ||
            c == '{' ||
            c == '}' ||
            c == '\\' ||
            c == '|');
  }
  
	// GSM char to ISO char
	public char gsm2Iso(char gsmChar) {
          if ( (gsmChar&0x7F) == 0x1b) {
            nextByteExtended = true;
            return 0x0010; // let this denote an empty char
          } else if (nextByteExtended) {
            //System.out.println("XXX Extendend high order byte: " + (int)((gsmChar&0x7F)));
            nextByteExtended = false;
            return gsmExtended2iso[(gsmChar&0x7F)];
          } else {
            return gsm2iso[gsmChar&0x7F];
          }
	}

	// ISO char to GSM char
	public char iso2Gsm(char isoChar) {
          // Sigma and pound has same low order byte (163), 0x03A3 -> 0x18 and 0x00A3 -> 0x01
          if (isoChar == 0x03A3) {
            return 0x18;
          }
          return iso2gsm[isoChar&0xFF];
	}

	// ISO char[] to GSM char[]
	public char[] iso2Gsm(char[] isoChars) {
          tmp = new char [isoChars.length*2];
          // Ensures len is 2*iso to start with
          int offset = 0;
          try
            {
              for(int i=0;i<isoChars.length;i++) {
                if (isExtendedChar(isoChars[i])) {
                  tmp[i+offset] = 0x1b;
                  offset++;
                  tmp[i+offset] = iso2Gsm(isoChars[i]);
                } else {
                  tmp[i+offset] = iso2Gsm(isoChars[i]);
                }
              }
            }
          catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("ERROR:iso2Gsm: " + e.getMessage());
          }
          char [] result = new char[isoChars.length+offset];
          System.arraycopy(tmp, 0, result, 0, isoChars.length+offset); 
          return result;
	}

	// GSM char[] to ISO char[]
	public char[] gsm2Iso(char[] gsmChars) {
		tmp = gsmChars; 	// Ensures len
                int offset = 0;
		try {
                  for(int i=0;i<gsmChars.length;i++) {
                    char c = gsm2Iso(gsmChars[i]);
                    if (c != 0x0010) {
                      tmp[i-offset]=c;
                    } else {
                      offset++;
                    }
                  }
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("ERROR:gsm2Iso: " + e.getMessage());
		}
                char [] res = new char[gsmChars.length - offset];
                System.arraycopy(tmp, 0, res, 0, gsmChars.length-offset); 
		return res;
	}

	// GSM String to ISO String
	public String gsm2Iso(String gsmString)
	{
		return (new String(gsm2Iso(gsmString.toCharArray())));
	}

        // ISO String to GSM String
        public String iso2Gsm(String isoString)
        {
                return (new String(iso2Gsm(isoString.toCharArray())));
        }
}
