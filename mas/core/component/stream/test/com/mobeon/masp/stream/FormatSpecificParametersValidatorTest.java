package com.mobeon.masp.stream;

import junit.framework.TestCase;

public class FormatSpecificParametersValidatorTest extends TestCase {

    public void testFormatSpecificParametersValidator() {
	
	// All these should match!
	String[] refFmtps = {
		"",
		"octet-align=1; mode-set=1,2,3,5; crc=0",
		" crc=1 ; octet-align=0",
		"mode-set=1,2,3;interleaving=1",
		"robust-sorting=0",
		"",
		"channels=1",
		"channels=1",
		"",
        "mode-set=1,2,3,4"		
	};
	String[] testFmtps = {
		"",
		"octet-align=1;mode-set=1,2,3,5; robust-sorting=0",
		"robust-sorting=0;crc=1",
		"interleaving=1;mode-set=1,2,3",
		"robust-sorting=0;foo=7",
		"channels=1",
		"crc=0",
		"channels=1",
		"mode-set=1,2,3,4",
        ""		
	};
	
	for (int i = 0; i < refFmtps.length ;i++) {
	    FormatSpecificParametersValidator validator = 
		new FormatSpecificParametersValidator(refFmtps[i]);
	    
	    assertTrue(testFmtps[i], validator.validateFormatParameters(testFmtps[i]));
	    
	}
	
    }

    public void testFormatSpecificParametersValidatorNeg() {
	
	// All these should match!
	String[] refFmtps = {
		"octet-align=1",
		"octet-align=0",
		"octet-align=1",
		"",

		"robust-sorting=1",
		"robust-sorting=0",
		"robust-sorting=1",
		"",
		
		"crc=1",
		"crc=0",
		"crc=1",
		"",
		
		"interleaving=1",
		"interleaving=0",
		"interleaving=1",
		"",

		"channels=1",
		"channels=2",
		"channels=2",
		"",

		"mode-set=1,2,3,4,5",
		"mode-set=1"
	};
	String[] testFmtps = {
		"octet-align=0",
		"octet-align=1",
		"",
		"octet-align=1",

		"robust-sorting=0",
		"robust-sorting=1",
		"",
		"robust-sorting=1",
		
		"crc=0",
		"crc=1",
		"",
		"crc=1",
		
		"interleaving=0",
		"interleaving=1",
		"",
		"interleaving=1",

		"channels=2",
		"channels=1",
		"",
		"channels=2",

		"mode-set=1,2,3,4",
		"mode-set=1,2",
	};
	
	for (int i = 0; i < refFmtps.length ;i++) {
	    FormatSpecificParametersValidator validator = 
		new FormatSpecificParametersValidator(refFmtps[i]);
	    
	    assertFalse(validator.validateFormatParameters(testFmtps[i]));
	    
	}
	
    }

}
