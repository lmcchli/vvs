/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.masp.stream.*;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.masp.mediaobject.ContentTypeMapper;
import com.mobeon.masp.logging.ILogger;
import com.mobeon.masp.logging.ILoggerFactory;
import com.mobeon.common.configuration.*;

import java.util.Collection;
import java.util.ArrayList;

public class StreamUtility {
    private static ILogger logger = ILoggerFactory.getILogger(StreamUtility.class);
    private static StreamUtility singletonInstance = null;
    private StreamFactoryImpl streamFactory;
    private IEventDispatcher eventDispatcher = null;

    public static StreamUtility getInstance() {
        if (singletonInstance == null) singletonInstance = new StreamUtility();
        return singletonInstance;
    }

    public void initialize(MediaTranslationManager mtm) {
        // Initializing the stream factory
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile("../cfg/mas.xml");
        streamFactory = new StreamFactoryImpl();
        ContentTypeMapperImpl contentTypeMapper = new ContentTypeMapperImpl();
        contentTypeMapper.setConfiguration(cm.getConfiguration());
        contentTypeMapper.init();
        streamFactory.setContentTypeMapper(contentTypeMapper);
//        cm.setConfigFile("../cfg/stream.xml");
        try {
            streamFactory.setConfiguration(cm.getConfiguration());
        } catch (GroupCardinalityException e) {
            e.printStackTrace();
        } catch (UnknownGroupException e) {
            e.printStackTrace();
        } catch (ParameterTypeException e) {
            e.printStackTrace();
        } catch (MissingConfigurationFileException e) {
            e.printStackTrace();
        } catch (ConfigurationLoadException e) {
            e.printStackTrace();
        } catch (javax.naming.ConfigurationException e) {
            e.printStackTrace();
        }

        streamFactory.setMediaTranslationManager(mtm);
        streamFactory.init();
    }

    public IOutboundMediaStream createCallingStream(String host, int port) throws StackException {
        Collection<RTPPayload> payloads = getAudioMediaPayloads();
        ConnectionProperties cProp = new ConnectionProperties();
        cProp.setAudioPort(port);
        cProp.setAudioHost(host);
        cProp.setPTime(20);
        Object requestId = new Object();
        OutboundMediaStreamImpl output = (OutboundMediaStreamImpl)streamFactory.getOutboundMediaStream();
        output.setRequestId(requestId);
        output.setPlayOption(IOutboundMediaStream.PlayOption.WAIT_FOR_AUDIO);
        output.setCursor(0);
        output.setEventDispatcher(eventDispatcher);
        output.create(payloads, cProp);
        return output;
    }

    public IInboundMediaStream createInboundStream() throws StackException {
        MediaTranslationFactory factory = MediaTranslationFactory.getInstance();
        MediaMimeTypes mimeType = new MediaMimeTypes(factory.getPcmuMimeType());
        InboundMediaStreamImpl inbound = (InboundMediaStreamImpl)streamFactory.getInboundMediaStream();
        inbound.setEventDispatcher(eventDispatcher);
        inbound.create(mimeType);
        return inbound;
    }

    /**
     * Returns a collection of payloads which corresponds to audio media.
     * @return an RTPPayload collection.
     */
    protected Collection<RTPPayload> getAudioMediaPayloads() {
        Collection<RTPPayload> list = new ArrayList<RTPPayload>();
        list.add(RTPPayload.get(RTPPayload.AUDIO_AMR));
        list.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));
        return list;
    }

    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public IStreamFactory getStreamFactory() {
        return streamFactory;
    }
}
