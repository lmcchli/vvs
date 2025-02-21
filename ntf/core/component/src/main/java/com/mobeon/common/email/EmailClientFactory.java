package com.mobeon.common.email;

import com.abcxyz.messaging.common.oam.LogAgent;


public class EmailClientFactory {
	private static EmailClientFactory instance;

	public static EmailClientFactory getInstance() {
		if (instance == null) {
			instance = new EmailClientFactory();
		}
		return instance;
	}

    public EmailClient createEmailClient(LogAgent logger, EmailConfig configWrapper) {
        return new EmailClient(logger,configWrapper);
    }
}
