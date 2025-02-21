/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.sdp.attributes.SdpMaxPTime;
import com.mobeon.masp.callmanager.sdp.attributes.SdpPTime;
import com.mobeon.masp.callmanager.sdp.attributes.SdpAttributes;
import com.mobeon.masp.callmanager.sdp.attributes.SdpRtpMap;
import com.mobeon.masp.callmanager.sdp.fields.*;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.stream.ConnectionProperties;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.configuration.ConfigurationManagerImpl;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * SdpIntersection Tester.
 *
 * @author Malin Flodin
 */
public class SdpIntersectionTest extends MockObjectTestCase
{
    private static final MimeType AUDIO_PCMU;
    private static final MimeType AUDIO_AMR;
    private static final MimeType VIDEO_H263;
    private static final MimeType VIDEO_MPEG4;

    private static final int DYNAMIC_PCMU = 55;
    private static final int DYNAMIC_H261 = 66;

    private static final int STATIC_PT_PCMU = 0;
    private static final int STATIC_PT_AMR = 97;
    private static final int STATIC_PT_DTMF = 101;
    private static final int STATIC_PT_H261 = 31;
    private static final int STATIC_PT_H263 = 34;
    private static final String STATIC_FMTP_AMR = "mode-set=7; robust-sorting=0";

    private static final int BITRATE_PCMU = 64000;
    private static final int BITRATE_AMR = 12200;
    private static final int BITRATE_DTMF = 0;
    private static final int BITRATE_H263 = 52000;
    private static final int BITRATE_H261 = 52000;


    private static final int SDP_PTIME = 77;
    private static final int SDP_ASBANDWIDTH = 64;
    private static final int SDP_RRBANDWIDTH = 800;
    private static final int SDP_RSBANDWIDTH = 2400;

    private static final int SDP_MAXPTIME = 154;


    private static final String INBOUND_AUDIO_HOST = "inboundAudioHost";
    private static final String INBOUND_VIDEO_HOST = "inboundVideoHost";
    private static final Integer INBOUND_AUDIO_PORT = 23;
    private static final Integer INBOUND_VIDEO_PORT = 32;
    private static final Integer INBOUND_PTIME = 60;
    private static final Integer INBOUND_MAXPTIME = 120;
    private static final Integer CLOCK_RATE = 8000;

    private static final Integer DEFAULT_PTIME = 20;
    private static final Integer DEFAULT_MAXPTIME = 40;

    static {
        try {
            AUDIO_PCMU = new MimeType("audio", "pcmu");
            AUDIO_AMR = new MimeType("audio", "amr");
            VIDEO_H263 = new MimeType("video", "h263");
            VIDEO_MPEG4 = new MimeType("video", "mpeg4");
        } catch (MimeTypeParseException e) {
            // Should not be possible when the arguments are constant strings.
            throw new IllegalStateException("Failed to create MimeType", e);
        }

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    private SdpSessionDescription emptySdpSessionDescription = new SdpSessionDescriptionImpl();
    private CallMediaTypes audioPcmuCallMediaType;
    private CallMediaTypes audioAmrCallMediaType;
    private CallMediaTypes audioAmrVideoMpeg4CallMediaType;
    private Collection<MimeType> mandatoryAudioTypes = new ArrayList<MimeType>();
    private Collection<MimeType> mandatoryVideoTypes = new ArrayList<MimeType>();
    private ConnectionProperties inboundConnectionProperties =
            new ConnectionProperties();

    Mock sessionDescriptionMock = mock(SdpSessionDescription.class);
    Mock audioMdMock = mock(SdpMediaDescription.class);
    Mock unusedAudioMdMock = mock(SdpMediaDescription.class);
    Mock videoMdMock = mock(SdpMediaDescription.class);


    public SdpIntersectionTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        CMUtils.getInstance().setCallManagerLicensing(new CallManagerLicensingMock());

        // Create a configuration manager and read the configuration file
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile(CallManagerTestContants.CALLMANAGER_WITH_SSPS_XML);

        // Initialize configuration now to be able to setup SSPs before CM
        ConfigurationReader.getInstance().setInitialConfiguration(
                cm.getConfiguration());
        ConfigurationReader.getInstance().update();

        ConnectionProperties.updateDefaultPTimes(DEFAULT_PTIME,DEFAULT_MAXPTIME);

        audioPcmuCallMediaType =
                new CallMediaTypes(new MediaMimeTypes(AUDIO_PCMU), null);
        audioAmrCallMediaType =
                new CallMediaTypes(new MediaMimeTypes(AUDIO_AMR), null);
        audioAmrVideoMpeg4CallMediaType =
                new CallMediaTypes(new MediaMimeTypes(AUDIO_AMR, VIDEO_MPEG4), null);

        mandatoryAudioTypes.add(AUDIO_PCMU);
        mandatoryVideoTypes.add(VIDEO_H263);

        // TODO: Add fmtp parameters
        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(STATIC_PT_PCMU, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, BITRATE_PCMU, null));
        rtppayloads.add(new RTPPayload(STATIC_PT_AMR, RTPPayload.AUDIO_AMR, "AMR", 8000, 1, BITRATE_AMR, STATIC_FMTP_AMR));
        rtppayloads.add(new RTPPayload(STATIC_PT_DTMF, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, BITRATE_DTMF, null));
        rtppayloads.add(new RTPPayload(STATIC_PT_H263, RTPPayload.VIDEO_H263, "H263", 90000, 1, BITRATE_H263, null));
        rtppayloads.add(new RTPPayload(STATIC_PT_H261, new MimeType("video/h261"), "H261", 8000, 1, BITRATE_H261, null));
        RTPPayload.updateDefs(rtppayloads);


        setupMockStubs();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that IllegalArgumentException is thrown if session description,
     * mandatory audio or video types, or audioIndex is null.
     * Also verifies that IllegalArgumentException is thrown if videoIndex is
     * not null but callMediaTypes contain no video mime type.
     * @throws Exception if test case fails.
     */
    public void testCreateSdpIntersectionThatFails() throws Exception {
        // Try create an SDP intersection with no session description
        try {
            new SdpIntersection (null, 0, 1, 0, audioAmrVideoMpeg4CallMediaType,
                    mandatoryAudioTypes, mandatoryVideoTypes);
            fail("Exception not thrown when expected");
        } catch (IllegalArgumentException e) {
        }

        // Try create an SDP intersection with no mandatory audio mime types
        try {
            new SdpIntersection (emptySdpSessionDescription, 0, 1, 0,
                    audioAmrVideoMpeg4CallMediaType, null,mandatoryVideoTypes);
            fail("Exception not thrown when expected");
        } catch (IllegalArgumentException e) {
        }

        // Try create an SDP intersection with no mandatory video mime types
        try {
            new SdpIntersection (emptySdpSessionDescription, 0, 1, 0,
                    audioAmrVideoMpeg4CallMediaType, mandatoryAudioTypes, null);
            fail("Exception not thrown when expected");
        } catch (IllegalArgumentException e) {
        }

        // Try create an SDP intersection with no audioIndex
        try {
            new SdpIntersection (emptySdpSessionDescription, null, 1, 0,
                    audioAmrVideoMpeg4CallMediaType, mandatoryAudioTypes,
                    mandatoryVideoTypes);
            fail("Exception not thrown when expected");
        } catch (IllegalArgumentException e) {
        }

        // Try create an SDP intersection with videoIndex != null but no
        // video Mime type in callMediaType
        try {
            new SdpIntersection (emptySdpSessionDescription, 0, 1, 0,
                    audioPcmuCallMediaType, mandatoryAudioTypes,
                    mandatoryVideoTypes);
            fail("Exception not thrown when expected");
        } catch (IllegalArgumentException e) {
        }

        // Try create an SDP intersection with one mandatory mime type that is
        // null in the list of mandatory mime types.
        try {
            Collection<MimeType> nullMandatoryAudioTypes = new ArrayList<MimeType>();
            nullMandatoryAudioTypes.add(null);
            new SdpIntersection (emptySdpSessionDescription, 0, null, 0,
                    audioPcmuCallMediaType, nullMandatoryAudioTypes,
                    mandatoryVideoTypes);
            fail("Exception not thrown when expected");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Verifies that mime types in mandatoryAudioMimeTypes,
     * mandatoryVideoMimeTypes and callMediaType is added to the
     * supportedMimeTypes list.
     * @throws Exception if test case fails.
     */
 /*   public void testCreateSdpIntersection() throws Exception {
        SdpIntersection sdpIntersection;
        Collection<MimeType> mimeTypes;

        // Verify that not video mime types are added if videoIndex is null
        sdpIntersection = new SdpIntersection(
                emptySdpSessionDescription, 0, null, 0,
                audioPcmuCallMediaType, mandatoryAudioTypes,
                mandatoryVideoTypes);
        mimeTypes = sdpIntersection.getSupportedMimeTypes();
        assertEquals(1, mimeTypes.size());
        assertTrue(mimeTypes.contains(AUDIO_PCMU));

        // Verify that not video mime types are added if videoIndex is null
        sdpIntersection = new SdpIntersection(
                emptySdpSessionDescription, 0, null, 0,
                audioAmrCallMediaType, mandatoryAudioTypes,
                mandatoryVideoTypes);
        mimeTypes = sdpIntersection.getSupportedMimeTypes();
        assertEquals(2, mimeTypes.size());
        assertTrue(mimeTypes.contains(AUDIO_PCMU));
        assertTrue(mimeTypes.contains(AUDIO_AMR));

        // Verify that video mime types are added if videoIndex != null
        sdpIntersection = new SdpIntersection(
                emptySdpSessionDescription, 0, 1, 0,
                audioAmrVideoMpeg4CallMediaType, mandatoryAudioTypes,
                mandatoryVideoTypes);
        mimeTypes = sdpIntersection.getSupportedMimeTypes();
        assertEquals(4, mimeTypes.size());
        assertTrue(mimeTypes.contains(AUDIO_PCMU));
        assertTrue(mimeTypes.contains(AUDIO_AMR));
        assertTrue(mimeTypes.contains(VIDEO_H263));
        assertTrue(mimeTypes.contains(VIDEO_MPEG4));
    }*/

    /**
     * Verify that the callMediaTypes can be retrieved.
     * @throws Exception if the test case fails.
     */
    public void testGetCallMediaTypes() throws Exception {
        SdpIntersection sdpIntersection;

        // Verify that the call media type inserted in the constructor is returned
        sdpIntersection = new SdpIntersection(
                emptySdpSessionDescription, 0, null, 0,
                audioPcmuCallMediaType, mandatoryAudioTypes,
                mandatoryVideoTypes);
        assertEquals(audioPcmuCallMediaType, sdpIntersection.getCallMediaTypes());

        sdpIntersection = new SdpIntersection(
                emptySdpSessionDescription, 0, null, 0,
                null, mandatoryAudioTypes,
                mandatoryVideoTypes);
        assertNull(sdpIntersection.getCallMediaTypes());
    }

    /**
     * Verifies that an intersection created with:
     * <ul>
     * <li>videoIndex == null => VOICE</li>
     * <li>videoIndex != null => VIDEO</li>
     * </ul>
     * @throws Exception
     */
    public void testGetCallType() throws Exception {
        SdpIntersection sdpIntersection;

        // VideoIndex == null
        sdpIntersection = new SdpIntersection(
                emptySdpSessionDescription, 0, null, 0,
                null, mandatoryAudioTypes,
                mandatoryVideoTypes);
        assertEquals(CallProperties.CallType.VOICE, sdpIntersection.getCallType());

        // VideoIndex != null
        sdpIntersection = new SdpIntersection(
                emptySdpSessionDescription, 0, 1, 0,
                null, mandatoryAudioTypes,
                mandatoryVideoTypes);
        assertEquals(CallProperties.CallType.VIDEO, sdpIntersection.getCallType());
    }

    /**
     * Verify that the Session Description can be retrieved.
     * @throws Exception if the test case fails.
     */
    public void testGetSessionDescription() throws Exception {
        SdpIntersection sdpIntersection;

        // Verify that the call media type inserted in the constructor is returned
        sdpIntersection = new SdpIntersection(
                emptySdpSessionDescription, 0, null, 0,
                audioPcmuCallMediaType, mandatoryAudioTypes,
                mandatoryVideoTypes);
        assertEquals(emptySdpSessionDescription, sdpIntersection.getSessionDescription());
    }

    /**
     * Verifies that connection properties can be created correctly from the
     * SDP intersection. Verifies that if videoIndex == null, video host and
     * port is not set in connection properties.
     * @throws Exception
     */
    public void testGetConnectionProperties() throws Exception {
        SdpIntersection sdpIntersection;
        ConnectionProperties connectionProperties;

        // videoIndex == null
        sdpIntersection = new SdpIntersection(
                (SdpSessionDescription)sessionDescriptionMock.proxy(),
                0, null, 0, audioPcmuCallMediaType, mandatoryAudioTypes,
                mandatoryVideoTypes);

        connectionProperties = sdpIntersection.getConnectionProperties();
        assertEquals("AudioHost", connectionProperties.getAudioHost());
        assertEquals(1234, connectionProperties.getAudioPort());
        assertEquals(SDP_PTIME, connectionProperties.getPTime());
        assertEquals(SDP_MAXPTIME, connectionProperties.getMaxPTime());
        // TODO: Phase 2! Add test for MTU
        //assertEquals(?, connectionProperties.getMaximumTransmissionUnit());
        assertNull(connectionProperties.getVideoHost());
        assertEquals(-1, connectionProperties.getVideoPort());


        // videoIndex != null
        sdpIntersection = new SdpIntersection(
                (SdpSessionDescription)sessionDescriptionMock.proxy(),
                0, 1, 0, audioAmrVideoMpeg4CallMediaType, mandatoryAudioTypes,
                mandatoryVideoTypes);

        connectionProperties = sdpIntersection.getConnectionProperties();
        assertEquals("VideoHost", connectionProperties.getVideoHost());
        assertEquals(4321, connectionProperties.getVideoPort());
    }

    /**
     * Verifies that connection properties can be created correctly from the
     * SDP intersection if the ptime is not set in the Media Description. The
     * PTime should be set to the configured default value.
     * @throws Exception
     */
    public void testGetConnectionPropertiesWhenPTimeNotSet() throws Exception {
        Mock sessionDescriptionMock = mock(SdpSessionDescription.class);
        Mock audioMdMock = mock(SdpMediaDescription.class);
        Mock videoMdMock = mock(SdpMediaDescription.class);
        ConnectionProperties connectionProperties;
        SdpIntersection sdpIntersection;

        sessionDescriptionMock.stubs().method("getMediaDescription").
                with(eq(0)).will(returnValue(audioMdMock.proxy()));
        sessionDescriptionMock.stubs().method("getMediaDescription").
                with(eq(1)).will(returnValue(videoMdMock.proxy()));

        audioMdMock.stubs().method("getConnection").
                will(returnValue(new SdpConnection("AudioHost")));
        Vector<Integer> audioFormats = new Vector<Integer>();
        audioFormats.add(DYNAMIC_PCMU);
        SdpMedia audioMedia = new SdpMedia(
                SdpMediaType.AUDIO, 1234, 0,
                SdpMediaTransport.RTP_AVP, new SdpMediaFormats(audioFormats));
        audioMdMock.stubs().method("getMedia").will(returnValue(audioMedia));

        SdpAttributes audioAttr = new SdpAttributes();
        audioAttr.addRtpMap(new SdpRtpMap(DYNAMIC_PCMU, "PCMU", 4000, 2));
        audioMdMock.stubs().method("getAttributes").will(returnValue(audioAttr));

        videoMdMock.stubs().method("getConnection").
                will(returnValue(new SdpConnection("VideoHost")));

        Vector<Integer> videoFormats = new Vector<Integer>();
        videoFormats.add(DYNAMIC_H261);
        SdpMedia videoMedia = new SdpMedia(
                SdpMediaType.VIDEO, 4321, 0,
                SdpMediaTransport.RTP_AVP, new SdpMediaFormats(videoFormats));
        videoMdMock.stubs().method("getMedia").will(returnValue(videoMedia));

        SdpAttributes videoAttr = new SdpAttributes();
        videoAttr.addRtpMap(new SdpRtpMap(DYNAMIC_H261, "H261", 4000, 2));
        videoMdMock.stubs().method("getAttributes").will(returnValue(videoAttr));

        // videoIndex == null
        sdpIntersection = new SdpIntersection(
                (SdpSessionDescription)sessionDescriptionMock.proxy(),
                0, null, 0, audioPcmuCallMediaType, mandatoryAudioTypes,
                mandatoryVideoTypes);

        connectionProperties = sdpIntersection.getConnectionProperties();
        assertEquals((int)DEFAULT_PTIME, connectionProperties.getPTime());
        assertEquals((int)DEFAULT_MAXPTIME, connectionProperties.getMaxPTime());
    }

    /**
     * Verifies that for each supported mime type, its corresponding RTP payload
     * (including dynamic payloads) is inserted in the supported payloads that
     * are retrieved using getSupportedRtpPayloads.
     * @throws Exception
     */
    public void testGetSupportedRtpPayloads() throws Exception {

        SdpIntersection sdpIntersection;
        Collection<RTPPayload> rtpPayloads;

        // Verify that no video mime types are added if videoIndex is null
        sdpIntersection = new SdpIntersection(
                (SdpSessionDescription)sessionDescriptionMock.proxy(),
                0, null, 0, audioPcmuCallMediaType, mandatoryAudioTypes,
                mandatoryVideoTypes);
        rtpPayloads = sdpIntersection.getSupportedRtpPayloads();
        assertEquals(1, rtpPayloads.size());
        assertEquals("audio/pcmu",
                ((RTPPayload)rtpPayloads.toArray()[0]).getMimeType().toString());


        // Verify that video mime types are added if videoIndex != null
        sdpIntersection = new SdpIntersection(
                (SdpSessionDescription)sessionDescriptionMock.proxy(),
                0, 1, 0, audioAmrVideoMpeg4CallMediaType, mandatoryAudioTypes,
                mandatoryVideoTypes);
        rtpPayloads = sdpIntersection.getSupportedRtpPayloads();
        assertEquals(3, rtpPayloads.size());

        HashSet<String> mimeTypeSet = new HashSet<String>();
        for (RTPPayload rtpPayload : rtpPayloads) {
            mimeTypeSet.add(rtpPayload.getMimeType().toString());
            if (rtpPayload.getEncoding().equals("PCMU")) {
                assertEquals(DYNAMIC_PCMU, rtpPayload.getPayloadType());
            } else if (rtpPayload.getEncoding().equals("AMR")) {
        	assertEquals(STATIC_PT_AMR, rtpPayload.getPayloadType());
            } else if (rtpPayload.getEncoding().equals("H263")) {
        	assertEquals(STATIC_PT_H263, rtpPayload.getPayloadType());
            }
        }
        assertTrue(mimeTypeSet.contains("audio/amr"));
        assertTrue(mimeTypeSet.contains("audio/pcmu"));
        assertTrue(mimeTypeSet.contains("video/h263"));
    }

    //=========================== Private Methods =========================


    private void setupMockStubs() {
        sessionDescriptionMock = mock(SdpSessionDescription.class);
        audioMdMock = mock(SdpMediaDescription.class);
        unusedAudioMdMock = mock(SdpMediaDescription.class);
        videoMdMock = mock(SdpMediaDescription.class);
        List<SdpMediaDescription> mediaDescriptions = new ArrayList<SdpMediaDescription>();
        mediaDescriptions.add((SdpMediaDescription)audioMdMock.proxy());
        mediaDescriptions.add((SdpMediaDescription)videoMdMock.proxy());
        mediaDescriptions.add((SdpMediaDescription)unusedAudioMdMock.proxy());

        sessionDescriptionMock.stubs().method("getMediaDescription").
                with(eq(0)).will(returnValue(audioMdMock.proxy()));
        sessionDescriptionMock.stubs().method("getMediaDescription").
                with(eq(1)).will(returnValue(videoMdMock.proxy()));
        sessionDescriptionMock.stubs().method("getMediaDescriptions").
                will(returnValue(mediaDescriptions));

        // Create audio media description
        audioMdMock.stubs().method("getConnection").
                will(returnValue(new SdpConnection("AudioHost")));

        Vector<Integer> audioFormats = new Vector<Integer>();
        audioFormats.add(DYNAMIC_PCMU);
        SdpMedia audioMedia = new SdpMedia(
                SdpMediaType.AUDIO, 1234, 0,
                SdpMediaTransport.RTP_AVP, new SdpMediaFormats(audioFormats));
        audioMdMock.stubs().method("getMedia").will(returnValue(audioMedia));

        SdpAttributes audioAttr = new SdpAttributes();
        audioAttr.setPTime(new SdpPTime(SDP_PTIME));
        audioAttr.setMaxPTime(new SdpMaxPTime(SDP_MAXPTIME));
        audioAttr.addRtpMap(new SdpRtpMap(DYNAMIC_PCMU, "PCMU", 4000, 2));
//        SdpFmtp sdpFmtp = new SdpFmtp(97,"octet-align=1");
//        audioAttr.addFmtp(sdpFmtp);
        audioMdMock.stubs().method("getAttributes").will(returnValue(audioAttr));

//        SdpBandwidth sdpBandwidth = new SdpBandwidth(SDP_BANDWIDTH);
//        audioMdMock.stubs().method("getBandwidth").with(eq("AS")).will(returnValue(sdpBandwidth));
//        videoMdMock.stubs().method("getBandwidth").with(eq("AS")).will(returnValue(sdpBandwidth));
        SdpBandwidth asBandwidth = new SdpBandwidth(SDP_ASBANDWIDTH);
        SdpBandwidth rsBandwidth = new SdpBandwidth(SDP_RSBANDWIDTH);
        SdpBandwidth rrBandwidth = new SdpBandwidth(SDP_RRBANDWIDTH);
        audioMdMock.stubs().method("getBandwidth").with(eq("AS")).will(returnValue(asBandwidth));
        videoMdMock.stubs().method("getBandwidth").with(eq("AS")).will(returnValue(asBandwidth));
        audioMdMock.stubs().method("getBandwidth").with(eq("RS")).will(returnValue(rsBandwidth));
        videoMdMock.stubs().method("getBandwidth").with(eq("RS")).will(returnValue(rsBandwidth));
        audioMdMock.stubs().method("getBandwidth").with(eq("RR")).will(returnValue(rrBandwidth));
        videoMdMock.stubs().method("getBandwidth").with(eq("RR")).will(returnValue(rrBandwidth));


        // Create audio media description
        videoMdMock.stubs().method("getConnection").
                will(returnValue(new SdpConnection("VideoHost")));

        Vector<Integer> videoFormats = new Vector<Integer>();
        videoFormats.add(DYNAMIC_H261);
        SdpMedia videoMedia = new SdpMedia(
                SdpMediaType.VIDEO, 4321, 0,
                SdpMediaTransport.RTP_AVP, new SdpMediaFormats(videoFormats));
        videoMdMock.stubs().method("getMedia").will(returnValue(videoMedia));

        SdpAttributes videoAttr = new SdpAttributes();
        videoAttr.addRtpMap(new SdpRtpMap(DYNAMIC_H261, "H261", 4000, 2));
        videoMdMock.stubs().method("getAttributes").will(returnValue(videoAttr));


        unusedAudioMdMock.stubs().
                method("createUnusedMediaDescriptionCopy").
                will(returnValue(unusedAudioMdMock.proxy()));
        inboundConnectionProperties.setAudioHost(INBOUND_AUDIO_HOST);
        inboundConnectionProperties.setAudioPort(INBOUND_AUDIO_PORT);
        inboundConnectionProperties.setPTime(INBOUND_PTIME);
        inboundConnectionProperties.setMaxPTime(INBOUND_MAXPTIME);
        inboundConnectionProperties.setVideoHost(INBOUND_VIDEO_HOST);
        inboundConnectionProperties.setVideoPort(INBOUND_VIDEO_PORT);
    }

}
