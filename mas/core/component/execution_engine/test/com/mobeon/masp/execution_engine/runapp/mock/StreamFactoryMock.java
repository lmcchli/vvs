package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.stream.*;
import com.mobeon.masp.mediaobject.ContentTypeMapper;

/**
 * Created by IntelliJ IDEA.
 * User: etomste
 * Date: 2005-dec-25
 * Time: 20:29:51
 * To change this template use File | Settings | File Templates.
 */
public class StreamFactoryMock extends BaseMock implements IStreamFactory {
    private int delayBeforeResponseToPlay;

    /**
     * Create the mock object
     */
    public StreamFactoryMock ()
    {
        super ();
        log.info ("MOCK: StreamFactoryMock.StreamFactoryMock");
    }

    /**
     * Used to map MIME-types to content type and file extension.
     * Needed in all inbound streams.
     */
    private ContentTypeMapper mContentTypeMapper;

    /* Javadoc in interface. */
    public IOutboundMediaStream getOutboundMediaStream() {
        return new OutboundMediaStreamMock(delayBeforeResponseToPlay);
    }

    /* Javadoc in interface. */
    public IInboundMediaStream getInboundMediaStream() {
        InboundMediaStreamMock stream = new InboundMediaStreamMock();
        stream.setContentTypeMapper(mContentTypeMapper);
        return stream;
    }

    /**
     * Sets the mapper used to map MIME-types to content type and file
     * extension. Needed in all inbound streams.
     *
     * @param mapper May not be <code>null</code>.
     *
     * @throws IllegalArgumentException If <code>mapper</code> is
     *         <code>null</code>.
     */
    public void setContentTypeMapper(ContentTypeMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException(
                    "Parameter mapper may not be null");
        }
        mContentTypeMapper = mapper;
    }

    /* Javadoc in interface. */
    public void init() throws StackException {
        log.info ("MOCK: StreamFactoryMock.init is unimplemented!");
        // TODO: Something good here ?
    }

    public void setDelayBeforeResponseToPlay(int delayBeforeResponseToPlay) {
        this.delayBeforeResponseToPlay = delayBeforeResponseToPlay;
    }
}
