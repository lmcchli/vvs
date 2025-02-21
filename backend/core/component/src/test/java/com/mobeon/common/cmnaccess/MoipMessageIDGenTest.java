package com.mobeon.common.cmnaccess;

import org.junit.Test;
import static org.junit.Assert.*;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.IdGenerationException;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageIDGen;
import org.junit.Ignore;

// @TODO: Fix this test!!
@Ignore
public class MoipMessageIDGenTest {
	@Test
	public void testShortIDGen() throws IdGenerationException{
		MessageInfo msgInfo = new MessageInfo();
		msgInfo.omsa = new MSA(MFSFactory.getMsid("5143457900"));
		msgInfo.rmsa = new MSA(MFSFactory.getMsid("4506781234"));
		msgInfo.omsgid = "789010";
		msgInfo.rmsgid = "123456";
		String id = MoipMessageIDGen.getShortRecipientMessageID(msgInfo, "100");

		assertTrue(id.startsWith("r"));
		assertTrue(id.endsWith("100"));

		String id1 = MoipMessageIDGen.getRecipientMessageID(msgInfo, "100");
		assertTrue(id1.startsWith("r"));
		assertTrue(id1.endsWith("100"));
		assertTrue(id1.equalsIgnoreCase(id));

		id = MoipMessageIDGen.getShortSenderMessageID(msgInfo, "100");
		assertTrue(id.startsWith("s"));
		assertTrue(id.endsWith("100"));

		id1 = MoipMessageIDGen.getSenderMessageID(msgInfo, "100");
		assertTrue(id1.startsWith("s"));
		assertTrue(id1.endsWith("100"));
		assertTrue(id1.equalsIgnoreCase(id));

	}

	@Test
	public void testIDGen() throws IdGenerationException{
		MessageInfo msgInfo = new MessageInfo();
		msgInfo.omsa = new MSA(MFSFactory.getEid("5143457900"), false);
		msgInfo.rmsa = new MSA(MFSFactory.getEid("4506781234"), false);
		msgInfo.omsgid = "789010";
		msgInfo.rmsgid = "123456";

		String id1 = MoipMessageIDGen.getRecipientMessageID(msgInfo, "100");
		assertTrue(id1.startsWith("r"));
		assertTrue(id1.endsWith("100"));

		id1 = MoipMessageIDGen.getSenderMessageID(msgInfo, "100");
		assertTrue(id1.startsWith("s"));
		assertTrue(id1.endsWith("100"));

	}
}
