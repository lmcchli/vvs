package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.SessionImpl;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.stream.*;
import com.mobeon.masp.util.component.IComponentManager;
import com.mobeon.masp.util.component.SpringComponentManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Utility {
    private static Utility singletonInstance = null;
    ISession session = new SessionImpl();
    IStreamFactory streamFactory;
    IMediaObjectFactory mediaObjectFactory;
    IEventDispatcher eventDispatcher;
    MediaTranslationManagerFacade mtm = null;

    public static Utility getSingleton() {
        if (singletonInstance == null) singletonInstance = new Utility();
        return singletonInstance;
    }

    private Utility() {
//        session.setSessionLogData("session", "Pillevick");
    }

    public void initialize(String componentConfigXML) throws IllegalArgumentException {
        IComponentManager compManager = null;
//        ClassPathResource classPathResource = new ClassPathResource("TestComponentConfig.xml");
//        XmlBeanFactory bf = new XmlBeanFactory(classPathResource);
//        mtm = (MediaTranslationManagerFacade)bf.getBean("MediaTranslationManager");
        try {
            // Create our context
            FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(componentConfigXML);
            SpringComponentManager.initialApplicationContext(ctx);
            compManager = SpringComponentManager.getInstance();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (!(compManager != null)) throw new IllegalArgumentException();
            mtm = (MediaTranslationManagerFacade)compManager.create("MediaTranslationManager",
                    MediaTranslationManagerFacade.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        streamFactory = MediaTranslationFactory.getInstance().getStreamFactory();
        mediaObjectFactory = MediaTranslationFactory.getInstance().getMediaObjectFactory();
    }

    public MediaTranslationManagerFacade getMediaTranslationManager(ISession session) {
        return mtm;
    }

    /**
     * Creating a MediaObject containing text
     * @return a MediaObject
     * @throws Exception
     * @param text
     * @param mimeTypeString
     */
    IMediaObject createTextMediaObject(String text, String mimeTypeString) throws Exception {
        MimeType mimeType = new MimeType(mimeTypeString);
        MediaProperties mediaProperies = new MediaProperties();
        mediaProperies.setContentType(mimeType);
        IMediaObject mediaObject = mediaObjectFactory.create(text, mediaProperies);
        mediaObject.setImmutable();
        return mediaObject;
    }

    /**
     * Creates an IOutboundMediaStream that corresponds to a stream which calls for text to speech translation.
     * @return an IOutboundMediaStream.
     * @throws com.mobeon.masp.stream.StackException
     */
    IOutboundMediaStream createCallingStream(String host, int port) throws StackException {
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(port);
        cProp.setAudioHost(host);
        cProp.setPTime(20);
        cProp.setMaxPTime(20);
        Object requestId = new Object();
        OutboundMediaStreamImpl output = (OutboundMediaStreamImpl)streamFactory.getOutboundMediaStream();
        output.setRequestId(requestId);
        output.setPlayOption(IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO);
        output.setCursor(0);
        output.setEventDispatcher(eventDispatcher);
        output.create(payloads, cProp);
        return output;
    }

    /**
     * Creates an IOutboundMediaStream that corresponds to a stream which calls for text to speech translation.
     * @return an IOutboundMediaStream.
     * @throws StackException
     */
    IInboundMediaStream createReceivingStream() throws StackException {
        InboundMediaStreamImpl input = (InboundMediaStreamImpl)streamFactory.getInboundMediaStream();
        input.setEventDispatcher(eventDispatcher);
        input.create(getAudioMediaMimeTypes());
        return input;
    }

    ISession getSession() {
        return session;
    }

    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * Creates audio media mime types.
     * @return audio media mime types
     */
    protected MediaMimeTypes getAudioMediaMimeTypes() {
        MimeType mimeType = null;

        try {
            mimeType = new MimeType("audio/pcmu");
        } catch (MimeTypeParseException e) {
            //TODO: fail("Not a valid mime type!");
        }
        return new MediaMimeTypes(mimeType);
    }

    /**
     * Returns a collection of payloads which corresponds to audio media.
     * @return an RTPPayload collection.
     */
    protected Collection<RTPPayload> getAudioMediaPayloads() {
        Collection<RTPPayload> list = new ArrayList<RTPPayload>();
        list.add(new RTPPayload(0, RTPPayload.AUDIO_PCMU, "pcmu", 0, 0, 0, null));
        return list;
    }

    /**
     * Reads a file and returns the contents.
     * @param fileName a file name.
     * @return the file contents.
     */
    protected Object readFile(String fileName) {
        byte[] data = null;
        File input = new File(fileName);
        FileInputStream inputStream;

        try {
            inputStream = new FileInputStream(input);
            int length = (int)input.length();
            data = new byte[length];
            int readBytes = inputStream.read(data, 0, length);
            //TODO: if (readBytes != length) fail("Wrong size on retreived data.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
