package com.mobeon.masp.mailbox.mfs;

import jakarta.mail.MessageAware;

import com.abcxyz.messaging.common.message.MSA;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;

import junit.framework.TestCase;

public class GetMessageMfsTest extends TestCase
{
	public GetMessageMfsTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testGetMessage() throws Exception
    {
    	MSA msa = new MSA("5145755377");
    	assertTrue(msa != null);
//    	int nbr = CommonMessagingAccess.countMessages(msa,null);
//    	assertTrue(nbr == 0);
//        assertTrue("logContextB should imply logContextBQ",logContextB.implies(logContextBQ));
//        assertFalse("logContextB should NOT imply logContextA",logContextB.implies(logContextA));
//        assertFalse("logContextA should NOT imply logContextB",logContextA.implies(logContextB));
//        assertFalse("logContextA should NOT imply logContextBQ",logContextA.implies(logContextBQ));
//        assertFalse("logContextBQ should NOT imply logContextB",logContextBQ.implies(logContextB));
//        assertFalse("logContextBQ should NOT imply logContextA",logContextBQ.implies(logContextA));
    }
}
