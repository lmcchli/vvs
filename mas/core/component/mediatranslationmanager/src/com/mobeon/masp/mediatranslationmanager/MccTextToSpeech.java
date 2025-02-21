package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediatranslationmanager.services.ServiceResponseObserver;
import com.mobeon.masp.mediatranslationmanager.services.ServiceResponseReceiver;
import com.mobeon.masp.servicerequestmanager.ServiceRequest;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.masp.servicerequestmanager.ServiceResponse;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.masp.execution_engine.session.ISession;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

/**
 * Translates text to streamed speech by calling MCC through the SRM ({@link IServiceRequestManager}).
 * This translator is given a stream ({@link IOutboundMediaStream}) and a media object ({@link IMediaObject})
 * which contains a text. The purpose is to translate the text to speech (audio).
 * MccTextToSpeech sends a request ({@link ServiceRequest}) containing the text to the SRM and expects to
 * receive a response ({@link ServiceResponse}) containing audio data. The audio data is inserted into
 * a new media object and sent/returned to the stream through {@link IOutboundMediaStream#translationDone(IMediaObject)}.
 */
public class MccTextToSpeech implements TextToSpeech, ServiceResponseObserver {
    private static ILogger logger = ILoggerFactory.getILogger(MccTextToSpeech.class);

    // Here are some string constants which are related to
    // a TTS request sent to an MCC component.
    public final static String MCC_PARAMETER_LANGUAGE = "language";
    public final static String MCC_PARAMETER_CODEC = "codec";
    public final static String MCC_PARAMETER_MAXAUDIO = "maxaudio";
    public final static String MCC_PARAMETER_VOICE = "voice";
    public final static String MCC_PARAMETER_CHARSET = "charset";
    public final static String MCC_PARAMETER_VOLUME = "volume";
    public final static String MCC_PARAMETER_SPEED = "speed";
    public final static String MCC_PARAMETER_TEXT = "text";
    public final static String MCC_PCMU_CODEC = "G.711/u-law";
    public final static String MCC_PCMA_CODEC = "G.711/a-law";
    public final static int MCC_SERVICE_STATUS_OK = 200;

    // Here are some string constants defining the mime
    // types which are used/known by MccTextToSpeech.
    // NB! All IANA media type strings should be in lower case
    public final static String AUDIO = "audio";
    public final static String TEXT = "text";
    public final static String PLAIN = "plain";
    public final static String SSML = "ssml";
    public final static String HTML = "html";
    public final static String PCMU = "pcmu";
    public final static String PCMA = "pcma";
    public final static String WAV = "wav";
    public final static String TEXT_PLAIN = "text/plain";
    public final static String TEXT_HTML = "text/html";
    public final static String APPLICATION_SSML = "application/ssml";
    public final static String AUDIO_PCMU = "audio/pcmu";
    public final static String AUDIO_PCMA = "audio/pcma";
    public final static String AUDIO_WAV = "audio/wav";

    // A reference to the (Singleton) factory (for creating requests).
    private MediaTranslationFactory factory = MediaTranslationFactory.getInstance();

    // The host name of the MCC
    private String mccHostName = null;
    // The port number of the MCC (to which we are sending requests).
    private int mccPortNumber = -1;
    // The service request manager
    private IServiceRequestManager requestManager = null;
    // Checks the request manager for responses.
    private ServiceResponseReceiver responseReceiver = null;
    // The stream wich called us (receiver of translated media object).
    private IOutboundMediaStream outputStream = null;
    private ISession session;

    /**
     * Default constructor.
     */
    MccTextToSpeech(ISession session) {
        // TODO: ensure that session propagates to SRM
        this.session = session;
        logger.debug("MccTextToSpeech");
        responseReceiver = new ServiceResponseReceiver(this);
    }

    /**
     * Initializes the translator ...
     * Configuration parameters etc.
     * Ensuring that we have all the mandatory information.
     */
    void init() {
        if (mccHostName == null) throw new IllegalStateException("Undefined MCC host name.");
        if (mccPortNumber < 0) throw new IllegalStateException("Undefined MCC port number.");
        if (requestManager == null) throw new
                IllegalStateException("Undefined service request manager");
    }

    /**
     * Initiates the text to speech translation.
     * This method takes the text from the media object and requests the MCC for translation.
     * Once the request is issued a {@link ServiceResponseReceiver} is started and method returns.
     * Translation continues when {@link #receiveServiceResponse(ServiceResponse)} is called.
     * @param mediaObject containts the text to be translated.
     * @param outputStream receiver of the translated media object.
     */
    public void translate(IMediaObject mediaObject, IOutboundMediaStream outputStream) {
        logger.debug("--> translate");
        // TODO: check the configuration and media types
        //       1) Is it possible to translate the media object?
        //         - get media object media type
        //         - get stream media type
        //         - consider media type returned from MCC
        //       2) If all three alternatives does match then it's possible
        //          to translate.
        //       3) Get the speech properties from the media object
        //       4) Are the speech properties compatible to the TTS engine?
        //       5) Get connection properties (service name, host and port)
        // TODO: the actual configuration retrieval should be managaded by the
        //       stream factory. The configuration data should be injected into the
        //       TextToSpeech upon creation.

        // Creating a service request (media object and stream campability is ensured)
        ServiceRequest request;
        try {
            request = createRequest(mediaObject, outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Could not create service request", e);
        }

        // Send request
        logger.debug("Sending XMP request");
        int transactionId = 0;
        transactionId = requestManager.sendRequestAsync(request, mccHostName, mccPortNumber);

        // Remember the stream.
        this.outputStream = outputStream;
        // Set up for receival of response
        responseReceiver.setServiceRequestManager(requestManager);
        responseReceiver.setTransactionId(transactionId);
        // The thread will terminate once a response is received.
        responseReceiver.start();
        logger.debug("<-- translate");
    }

    /**
     * The incoming service response notification method.
     * The media translation continues when this method is called. The received audio data
     * is inserted into a media object and handed over to the stream. This ends the translation.
     * The calling thread ({@link ServiceResponseReceiver}) is supposed to terminate.
     * @param response a response to a service request.
     */
    public void receiveServiceResponse(ServiceResponse response) {
        logger.debug("--> receiveServiceResponse");
        // TODO: can't handle the error with an exception.
        // TODO: an event should be sent to the caller of IOutboundMediaStream.
        if (response.getStatusCode() != MCC_SERVICE_STATUS_OK) {
            throw new RuntimeException("Service error: " + response.getStatusCode()
                    + ":'" + response.getStatusText() + "'");
        }

        // TODO: cannot throw any exception here ...
        // TODO: move the code to an other part of this class ...
        // Creating media type for the translated media object
        MimeType mediaType;
        try {
            mediaType = new MimeType(AUDIO_PCMU);
        } catch (MimeTypeParseException e) {
            throw new RuntimeException("Could not create mime type", e);
        }

        // Retreiving the parameter containing the data/speech
        byte[] audio = (byte[])response.getParameter(AUDIO_WAV);

        // TODO: improve error handling ...
        if (audio == null) throw new RuntimeException("Got null audio from service response");

        // Transferring the speech to an array of direct ByteBuffers in
        // order to create a proper media object.
        List<ByteBuffer> audioBufferList = new LinkedList<ByteBuffer>();
        ByteBuffer audioBuffer = ByteBuffer.allocateDirect(audio.length);
        audioBuffer.put(audio);
        audioBufferList.add(audioBuffer);

        // Calculating media object length in milli seconds.
        // Assuming that sampling frequency is 8kHz hence dividing the buffer length by eight
        // gives us the audio length in milli seconds (length/frequency = n/(n/t) = t*n/n = t).
        int lengthInMillis = audio.length > 0 ? audio.length/8 : 0;

        // Creating a media object
        MediaProperties mediaProperties = new MediaProperties(mediaType);
        mediaProperties.addLengthInUnit(
                MediaLength.LengthUnit.MILLISECONDS, lengthInMillis);
        IMediaObject translatedMediaObject =
                factory.getMediaObjectFactory().create(/*audioBufferList, mediaProperties*/);

        // Returning the translated media to the stream
        // The this will end the thread (translation ends here). Stream
        // will take over.
        outputStream.translationDone(translatedMediaObject);
        logger.debug("<-- receiveServiceResponse");
    }

    /**
     * Performs control actions on an ongoing text to speech translation.
     *
     * @param action an action can be one of "pause", "resume" or "stop".
     * @param data ignored.
     */
    public void control(String action, String data) {
        // TODO: is this really applicable in this case?
        // Play on a stream is aborted by EE. When resumed a new play with an offset is issued.
        // I think that this interface is here only because it is supported by MRCP.
        throw new IllegalAccessError("Control is not supported by TTS over MCC!");
    }

    /**
     * Opens/initalizes a text to speech translation session.
     * The speech will be returned/transmitted through an outbound
     * stream.
     *
     * @param outbound through which the speech is transmitted.
     */
    public void open(IOutboundMediaStream outbound) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Performs text to speech translation.
     * The text contained in the media object is translated to speech
     * and transmitted through the outbound stream from a prior call of
     * the open() method.
     *
     * @param mediaObject containing the text to be translated.
     */
    public void translate(IMediaObject mediaObject) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Closes an open text to speech session.
     * If there is a speech session open, it will be closed.
     */
    public void close() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Service request manager setter.
     * @param requestManager a service request manager.
     */
    public void setRequestManager(IServiceRequestManager requestManager) {
        this.requestManager = requestManager;
    }

    /**
     * MCC hoste name setter (for service requests).
     * @param mccHostName the host name of the MCC.
     */
    public void setMccHostName(String mccHostName) {
        this.mccHostName = mccHostName;
    }

    /**
     * MCC port number setter (for service requests).
     * @param mccPortNumber the port number of the MCC.
     */
    public void setMccPortNumber(int mccPortNumber) {
        this.mccPortNumber = mccPortNumber;
    }

    /**
     * Determines if we are capable of translating a media object or not.
     * @param mediaObject
     * @return true if we can translate the media object and false otherwise.
     */
    private boolean isMediaTypeKnown(IMediaObject mediaObject) {
        logger.debug("isMediaTypeKnown");
        MediaProperties mediaProperties = mediaObject.getMediaProperties();
        String mimeType = mediaProperties.getContentType().getBaseType();

        if (APPLICATION_SSML.equals(mimeType))  return true;
        if (TEXT_PLAIN.equals(mimeType)) return true;
        if (TEXT_HTML.equals(mimeType)) return true;
        return false;
    }

    private ServiceRequest createRequest(IMediaObject mediaObject,
                                          IOutboundMediaStream outputStream) throws IOException {
        logger.debug("--> createRequest()");
        // Check media type
        if (!isMediaTypeKnown(mediaObject)) {
            throw new IllegalArgumentException("Unknown media type");
        }

        // Check codec
        String codec = getCodec(outputStream);
        if (codec == null) throw new IllegalArgumentException("Unknown codec");

        ServiceRequest request = factory.createServiceRequest();
        // TODO: is this really the proper way to get text from a MediaObject?
        int mediaSize = (int)mediaObject.getSize();
        byte[] mediaData = new byte[mediaSize];
        int readBytes = mediaObject.getInputStream().read(mediaData, 0, mediaSize);
        // Who's resposible for the base64 encoding, it must be the ServiceRequest/Response?
        String text = Base64.getEncoder().encodeToString(mediaData);

        request.setServiceId(IServiceName.TEXT_TO_SPEECH);
        request.setParameter(MCC_PARAMETER_CODEC, codec);
        logger.debug("text read bytes: " + readBytes);
        logger.debug("text: [" + new String(mediaData) + "]");
        logger.debug("text in base64 : [" + text + "]");
        request.setParameter(MCC_PARAMETER_TEXT, text);

        logger.debug("<-- createRequest()");
        return request;
    }

    private String getCodec(IOutboundMediaStream stream) {
        logger.debug("getCodec");
        RTPPayload[] payloads = stream.getSupportedPayloads();
        for (RTPPayload payload : payloads) {
            String mimeType = payload.getMimeType().getBaseType();
            if (AUDIO_PCMU.equals(mimeType)) return MCC_PCMU_CODEC;
            if (AUDIO_PCMA.equals(mimeType)) return MCC_PCMA_CODEC;
        }
        logger.debug("No codec ...");
        return null;
    }

}
