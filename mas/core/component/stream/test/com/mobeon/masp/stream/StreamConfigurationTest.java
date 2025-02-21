/**
 * Copyright (c) 2007 Abcxyz AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import java.util.List;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import com.mobeon.common.configuration.ConfigurationManagerImpl;

import junit.framework.TestCase;

/**
 * @author ehakwik
 *
 */
public class StreamConfigurationTest extends TestCase {

    private StreamConfiguration mConfig;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile("stream/test/com/mobeon/masp/stream/cfg/stream.conf");
        mConfig = StreamConfiguration.getInstance();
        mConfig.setInitialConfiguration(cm.getConfiguration());
        try {
            mConfig.update();
        }
        catch (Exception e) {
            System.err.println("Exception: " + e);
        }
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link com.mobeon.masp.stream.StreamConfiguration#getRTPPayloadDefs()}.
	 */
	public void testGetRTPPayloadDefs() {
		RTPPayload p1 = RTPPayload.get(RTPPayload.AUDIO_PCMU);
		assertEquals("Subtype", "pcmu", p1.getSubType());
		assertEquals("Encoding", "PCMU", p1.getEncoding());
		assertEquals("ClockRate", 8000, p1.getClockRate());
		assertEquals("BitRate", 64000, p1.getBitrate());
		
		
		assertEquals("RS:", 101, p1.getBwSender());
		assertEquals("minRS:", 102, p1.getMinSender());
		assertEquals("maxRS:", 103, p1.getMaxSender());
		assertEquals("RR:", 104, p1.getBwReceiver());
		assertEquals("minRR:", 105, p1.getMinReceiver());
		assertEquals("maxRR:", 106, p1.getMaxReceiver());
		
		RTPPayload p2 = RTPPayload.get(RTPPayload.AUDIO_AMR);
		assertEquals("Subtype", "amr", p2.getSubType());
		assertEquals("Encoding", "AMR", p2.getEncoding());
		assertEquals("ClockRate", 8000, p2.getClockRate());
		assertEquals("BitRate", 12200, p2.getBitrate());
		assertEquals("mediaFormatParameters", "mode-set=7; octet-align=1", p2.getMediaFormatParameters());
		/*
		assertEquals("RS:", 99, p2.getBwSender());
		assertEquals("minRS:", -1, p2.getMinSender());
		assertEquals("maxRS:", -1, p2.getMaxSender());
		assertEquals("RR:", -1, p2.getBwReceiver());
		assertEquals("minRR:", -1, p2.getMinReceiver());
		assertEquals("maxRR:", -1, p2.getMaxReceiver());
		*/
		
	}
	
	public void testSupportedContentType() {
		MimeType audio = null;
		MimeType quicktime = null;
		MimeType video3gpp = null;
		MimeType audio3gpp = null;
		try {
			audio = new MimeType("audio/wav");
			quicktime = new MimeType("video/quicktime");
			video3gpp = new MimeType("video/3gpp");
			audio3gpp = new MimeType("audio/3gpp");

		} catch (MimeTypeParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue(isSupported(audio));
		assertTrue(isSupported(quicktime));
		assertTrue(isSupported(video3gpp));
		assertTrue(isSupported(audio3gpp));

	}

	public void testNonSupportedContentType() {
		MimeType blah = null;
		try {
			blah = new MimeType("blah/blah");
		} catch (MimeTypeParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse(isSupported(blah));

	}
	
	public void testOtherParams() {
		assertEquals("portPoolBase", 23000, mConfig.getPortPoolBase());
		assertEquals("PortPoolSize", 1440,mConfig.getPortPoolSize());
		assertEquals("movFileVersion",1 ,mConfig.getMovFileVersion());
		assertEquals("SyncCallMaxWaitTime",120 ,mConfig.getSyncCallMaxWaitTime());
		assertEquals("SendPacketsAhead", 40,mConfig.getSendPacketsAhead());
		assertEquals("ExpireTimeout", 40000,mConfig.getExpireTimeout());
		assertEquals("AbandonedStreamDetectedTimeout", 32000,mConfig.getAbandonedStreamDetectedTimeout());
		assertEquals("SendersControlFraction", 0.4f ,mConfig.getSendersControlFraction());
		assertEquals("AudioReplaceWithSilence", 10 ,mConfig.getAudioReplaceWithSilence());
		assertEquals("dispatchDtmfOnKeyDown", true,mConfig.isDispatchDTMFOnKeyDown());
		assertEquals("MaximumTransmissionUnit", 2000,mConfig.getMaximumTransmissionUnit());
		assertEquals("AudioSkip", 0,mConfig.getAudioSkip());
		assertEquals("MaxWaitForIFrameTimeout", 2000,mConfig.getMaxWaitForIFrameTimeout());
		assertEquals("Skew", 0,mConfig.getSkew());
		assertEquals("SkewMethodIntRep", 0,mConfig.getSkewMethodIntRep());
		assertEquals("DefaultPTime", 40,mConfig.getDefaultPTime());
		assertEquals("DefaultMaxPTime", 40,mConfig.getDefaultMaxPTime());
		assertEquals("OutputProcessors", 4,mConfig.getOutputProcessors() );
		assertEquals("InputProcessors(", 16,mConfig.getInputProcessors() );
		assertEquals("SilenceDetectionMode",0 ,mConfig.getSilenceDetectionMode() );
		assertEquals("SilenceThreshold", 0,mConfig.getSilenceThreshold());
		assertEquals("InitialSilenceFrames", 40,mConfig.getInitialSilenceFrames() );
		assertEquals("SignalDeadband",10 ,mConfig.getSignalDeadband() );
		assertEquals("SilenceDeadband", 150,mConfig.getSilenceDeadband() );
		assertEquals("DetectionFrames",10 ,mConfig.getDetectionFrames() );
		assertEquals("SilenceDetectionDebugLevel", 0,mConfig.getSilenceDetectionDebugLevel());
		assertEquals("LocalHostName", "localhost",mConfig.getLocalHostName() );
		
		
				
	}

	private boolean isSupported(MimeType contentType) {
		List<MimeType> supportedTypes = mConfig.getSupportedContentTypes();

		for (MimeType type : supportedTypes) {
			if (type.match(contentType)) {
				return true;
			}
		}
		return false;
	}
	


}