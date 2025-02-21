/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.servicerequestmanager.ServiceRequest;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.masp.servicerequestmanager.ServiceResponse;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.RTPPayload;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.io.ByteArrayInputStream;
import java.util.Collection;

/**
 * This class tests Text to Speech over MCC.
 * MCC access is performed through service requests over the Service Request Manager (SRM).
 * The SRM functionality is mocked.
 */
public class MccTextToSpeechTest extends MockObjectTestCase {
    private MediaTranslationManagerFacade mtm = null;
    private Mock mockMediaObjectFactory;
    private Mock mockOutboundStream;
    private Mock mockInputMediaObject;
    private Mock mockOutputMediaObject;
//    private Mock mockServiceRequestFactory;
    private Mock mockServiceRequestManager;
    private Mock mockServiceRequest;
    private Mock mockServiceResponse;
    protected Mock mockEventDispatcher;
    protected static final String HOST_BRAGE = "150.132.5.213";
    protected static final String HOST_PC = "10.16.2.25";
    protected static final String REMOTE_HOST = HOST_BRAGE; // "0.0.0.0";
    protected static final int REMOTE_AUDIO_PORT = 23000;
    // This is the spoken text
    String text = "one";
    ByteArrayInputStream input = new ByteArrayInputStream(text.getBytes());
    // Now we are ready to call translate, but first we must set up the expectations upon
    // the mocked service request manager:

    /**
     * Test cases set up.
     */
    public void setUpDisabled() {
        Utility.getSingleton().initialize("test/TestMCCComponentConfig.xml");
        mtm = Utility.getSingleton().getMediaTranslationManager(Utility.getSingleton().getSession());

        mockMediaObjectFactory = mock(IMediaObjectFactory.class);

        mockInputMediaObject = mock(IMediaObject.class);
        mockOutputMediaObject = mock(IMediaObject.class);

        mockOutboundStream = mock(IOutboundMediaStream.class);

        // Mock a service request ...
        mockServiceRequest = mock(ServiceRequest.class);

        // Mock a service response ...
        mockServiceResponse = mock(ServiceResponse.class);

        // Mock a service request factory
//        mockServiceRequestFactory = mock(IServiceRequestFactory.class);

        // Mock the service request manager ...
        mockServiceRequestManager = mock(IServiceRequestManager.class);

        mockEventDispatcher = mock(IEventDispatcher.class);

        // Start translate
        // Create request (media object, output stream)

        // 1) A service request should be created
//        mockServiceRequestFactory.expects(once()).method("create")
//                .withNoArguments()
//                .will(returnValue(mockServiceRequest.proxy()));

        // 2) Data should be added to the request
        mockServiceRequest.expects(once()).method("setServiceId")
                .with(eq(MccTextToSpeech.MCC_SERVICE_ID));
        mockServiceRequest.expects(once()).method("setParameter")
                .with(eq(MccTextToSpeech.MCC_PARAMETER_CODEC), eq(MccTextToSpeech.MCC_PCMU_CODEC));
        mockServiceRequest.expects(once()).method("setParameter")
                .with(eq(MccTextToSpeech.MCC_PARAMETER_TEXT), eq(Base64.encode(text.getBytes())));
        // 3) The request should be sent through the SRM
        mockServiceRequestManager.expects(once()).method("sendRequestAsync")
                .with(eq(mockServiceRequest.proxy()), eq(null), eq(-1))
                .will(returnValue(4711));
        // 4) The response (to the request) should be received
        mockServiceRequestManager.expects(once()).method("receiveResponse")
                .with(eq(4711))
                .will(returnValue(mockServiceResponse.proxy()));
        // 5) The status code of the response should be retrieved
        mockServiceResponse.expects(once()).method("getStatusCode")
                .withNoArguments()
                .will(returnValue(MccTextToSpeech.MCC_SERVICE_STATUS_OK));

        // Media object stubs
        // 1) Set up getMediaProperties()
        MediaProperties mediaPropeties =
                new MediaProperties(MediaTranslationFactory.getInstance().getSsmlMimeType());
        mockInputMediaObject.expects(once()).method("getMediaProperties")
                .withNoArguments()
                .will(returnValue(mediaPropeties));

        // 2) Set up get media object size
        mockInputMediaObject.expects(once()).method("getSize")
                .withNoArguments()
                .will(returnValue((long)text.length()));

        // 3) Set up getInputStream()
        mockInputMediaObject.expects(once()).method("getInputStream")
                .withNoArguments()
                .will(returnValue(input));
        //    The status text is optional.
        mockServiceResponse.stubs().method("getStatusText")
                .withNoArguments()
                .will(returnValue("Ok"));
        // 6) The received audio data should be retreived
        mockServiceResponse.expects(once()).method("getParameter")
                .with(eq(MccTextToSpeech.AUDIO_WAV))
                .will(returnValue(Utility.getSingleton().readFile("test/one.au")));


        mockMediaObjectFactory.expects(once()).method("create")
                .withAnyArguments()
                .will(returnValue(mockOutputMediaObject.proxy()));

        mockOutboundStream.expects(once()).method("translationDone")
                .with(isA(IMediaObject.class));

//        // 7) When play has completed an event should be issued
//        mockEventDispatcher.expects(once()).method("fireEvent")
//                .with(isA(PlayFinishedEvent.class));


        // ...
        Collection<RTPPayload> payloadCollection =
                Utility.getSingleton().getAudioMediaPayloads();
        assertTrue(payloadCollection.size() > 0);
        for (RTPPayload payload : payloadCollection) {
            assertNotNull(payload);
        }
        RTPPayload[] payloads = new RTPPayload[payloadCollection.size()];
        payloadCollection.toArray(payloads);
        for (RTPPayload payload : payloads) {
            assertNotNull(payload);
        }
        mockOutboundStream.expects(once()).method("getSupportedPayloads")
                .withNoArguments()
                .will(returnValue(payloads));

        mtm.setMediaObjectFactory((IMediaObjectFactory)mockMediaObjectFactory.proxy());
        // Injecting SRM functionality into MTM
//        mtm.setServiceRequestFactory((IServiceRequestFactory)mockServiceRequestFactory.proxy());
        mtm.setServiceRequestManager((IServiceRequestManager)mockServiceRequestManager.proxy());
    }

    /**
     * Testing the translation of text to speech
     * @throws Exception
     */
    public void testTranslate() throws Exception {
//       // The translator
//        TextToSpeech translator = mtm.getTextToSpeech();
//        assertNotNull("Should not be null pointer", translator);
//        assertTrue("Type must be MccTextToSpeech", translator instanceof MccTextToSpeech);
//
//        // Ok, now that the expectations are defined we can perform the translation
//        translator.translate((IMediaObject)mockInputMediaObject.proxy(),
//                (IOutboundMediaStream)mockOutboundStream.proxy());
//
//        // TODO: look this over ...
//        // Pending on the ServiceResponseReceiver
//        Thread.sleep(10);
//
//        // So, if we did not receive any errors from the mock expectations, we are safe and sound!
    }
}
