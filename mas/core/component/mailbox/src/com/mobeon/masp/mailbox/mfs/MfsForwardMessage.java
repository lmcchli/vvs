/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.mfs;

import java.util.List;

import com.mobeon.masp.mailbox.IMessageContent;
import com.mobeon.masp.mailbox.MailboxException;

/**
 * @author QHAST
 */
public class MfsForwardMessage extends MfsStorableMessage {

    MfsForwardMessage(MfsContext context, MfsMessageAdapter originalMessage) throws MailboxException {
//        super(context,orignalMessage);
    	super(context,null);
    	List<IMessageContent> list;
		list = originalMessage.getContent();

    	for(IMessageContent mc: list) {
    		this.content.add(mc);
    	}
    }

    /* Don't think we need to overide
    @Override
    protected Multipart createMultipart() throws MessagingException {
        Multipart result = super.createMultipart();
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(orignalMessage,"message/rfc822");
        result.addBodyPart(messageBodyPart);
        return result;
    }
     */
    
}
