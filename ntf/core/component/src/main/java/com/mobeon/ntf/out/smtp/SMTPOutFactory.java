package com.mobeon.ntf.out.smtp;

import com.mobeon.ntf.out.mms.MMSCenter;

public class SMTPOutFactory {
	
   private  static SMTPOutFactory instance;
		
	public static SMTPOutFactory getInstance() {
		if (instance == null) {
			instance = new SMTPOutFactory();
		} 
		return instance;
	}

	public SMTPOut createSMTPOut(MMSCenter center) {
		return new SMTPOut(center);
	}

}
