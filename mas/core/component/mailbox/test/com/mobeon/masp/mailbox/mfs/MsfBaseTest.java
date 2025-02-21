package com.mobeon.masp.mailbox.mfs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.jmock.Expectations;

import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.message_sender.IInternetMailSender;
import com.mobeon.masp.mailbox.MailboxBaseTestCase;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.util.content.PageBreakingStringCounter;
import com.mobeon.masp.util.content.PageCounter;

public class MsfBaseTest extends MailboxBaseTestCase
{
    protected IInternetMailSender mockInternetMailSender = mockery.mock(IInternetMailSender.class);
    protected MfsContext mfsContext;
    protected MfsStoreAdapter mfsStoreAdapter;
    static final String ADDITIONAL_PROPERTY = "property1";
    static final String ADDITIONAL_PROPERTY_HEADER_NAME = "property1headername";
    Appender mockAppender;
    protected String msId;
	
    public MsfBaseTest(String name) {
        super(name);
    }

	protected void setUp() throws Exception {
		msId = "0001";		
        setUpMfsContext();
        setUpMfsStoreAdapter(msId);
	}
	
	protected MfsContext getMfsContext() {		
        return mfsContext;
    }
	
	   protected IGroup getMockConfigGroup() {
		   final IGroup mockConfigGroup = mockery.mock(IGroup.class, "mockConfigGroup");
		   try {
				mockery.checking(new Expectations() {{
					   allowing(mockConfigGroup).getGroup("message.additionalproperty");
					   will(returnValue(getAdditionalPropertyGroup()));
				   }});
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	        return mockConfigGroup;
	    }
	   
	    protected void setUpMfsContext() throws MailboxException {
	        MfsContextFactory mfsContextFactory;
			try {
				mfsContextFactory = getMfsContextFactory();
			} catch (Exception e) {
				throw new MailboxException(e.getMessage());
			}
	        mfsContext = mfsContextFactory.create(new MailboxProfile("accountid", "accountpassword", "emailaddress"));
	    }

	    protected void setUpMfsStoreAdapter(String msId) {
	        mfsStoreAdapter = new MfsStoreAdapter(mfsContext,msId);
	    }

	    /**
	     * Sets up a mocked Appender, so that log behavior can be tested
	     */
	    void setUpMockAppender() {
	        mockAppender = mockery.mock(Appender.class);
	        Logger.getRootLogger().addAppender(mockAppender);
	    }

	    /**
	     * Tears down a mocked Appender, so that log behavior no longer is tested
	     */
	    void tearDownMockAppender() {
	        Logger.getRootLogger().removeAppender(mockAppender);
	    }

	    protected MfsContextFactory getMfsContextFactory() throws Exception {
	        MfsContextFactory mfsContextFactory = new MfsContextFactory();
	        mfsContextFactory.setDefaultSessionProperties(new Properties());
	        mfsContextFactory.setPageCounterMap(getPageCounterMap());
	        mfsContextFactory.setConfiguration(getMockConfiguration());
	        mfsContextFactory.setMediaObjectFactory(new MediaObjectFactory());
	        return mfsContextFactory;
	    }

	    private HashMap<String, PageCounter> getPageCounterMap() {
	        HashMap<String, PageCounter> pageCounterMap = new HashMap<String, PageCounter>();
	        pageCounterMap.put("image/tiff", new PageBreakingStringCounter("Fax Image"));
	        return pageCounterMap;
	    }

	    
	    protected IGroup getAdditionalPropertyGroup() throws Exception {
	    	final IGroup additionalPropertyGroup = mockery.mock(IGroup.class, "mockAdditionalPropertyGroup");
	        // The stubs method is used here since BaseConfig.additionalPropertyMap is static so getGroups is only called once
	    	mockery.checking(new Expectations() {{
	    		allowing(additionalPropertyGroup).getString("name");
	    		will(returnValue(ADDITIONAL_PROPERTY));
	    		allowing(additionalPropertyGroup).getString("field");
	    		will(returnValue(ADDITIONAL_PROPERTY_HEADER_NAME));
	    	}});
	    	
	    	return additionalPropertyGroup;
	    }

	    InputStream getVoiceMessageInputStream() {
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        appendVoiceMessage(pw);
	        return new ByteArrayInputStream(sw.toString().getBytes());
	    }

	    void appendVoiceMessage(PrintWriter pw) {
	        pw.println("Content-Type: multipart/voice-message; boundary=\"voicemessageboundary\"");
	        pw.println("Subject: Voice Message From John Doe");
	        pw.println();
	        pw.println("--voicemessageboundary");
	        pw.println("Content-type: audio/wav");
	        pw.println();
	        pw.println();
	        pw.println("--voicemessageboundary--");
	    }

	    protected InputStream getVideoMessageInputStream() {
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        appendVideoMessage(pw);
	        return new ByteArrayInputStream(sw.toString().getBytes());
	    }

	    protected InputStream getFaxMessageInputStream() {
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        appendFaxMessage(pw);
	        return new ByteArrayInputStream(sw.toString().getBytes());
	    }

	    protected InputStream getEmailMessageInputStream() {
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        appendEmailMessage(pw);
	        return new ByteArrayInputStream(sw.toString().getBytes());
	    }

	    protected void appendVideoMessage(PrintWriter pw) {
	        pw.println("Content-Type: multipart/x-video-message; boundary=\"videomessageboundary\"");
	        pw.println();
	        pw.println("--videomessageboundary");
	        pw.println("Content-type: video/quicktime");
	        pw.println();
	        pw.println();
	        pw.println("--videomessageboundary--");
	    }

	    protected void appendFaxMessage(PrintWriter pw) {
	        pw.println("Content-Type: multipart/fax-message; boundary=\"faxmessageboundary\"");
	        pw.println();
	        pw.println("--faxmessageboundary");
	        pw.println("Content-type: image/tiff");
	        pw.println();
	        pw.println();
	        pw.println("--faxmessageboundary--");
	    }

	    protected void appendEmailMessage(PrintWriter pw) {
	        pw.println("Content-Type: text/plain");
	        pw.println();
	        pw.println("body");
	    }
}
