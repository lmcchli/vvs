package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.execution_engine.runapp.ApplicationBasicTestCase;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.mediaobject.*;
import com.mobeon.masp.stream.*;
import com.mobeon.masp.util.TimeValue;
import com.mobeon.masp.util.TimeValueParser;

import jakarta.activation.MimeType;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An outbound media stream mock object.
 */
public class OutboundMediaStreamMock extends MediaStreamSupportMock implements IOutboundMediaStream, Runnable {

    /**
     * Currently playing media object
     */
    protected volatile MediaObject currentMediaObject = null;
    protected volatile FileMediaObject currentFileMediaObject = null;
    private Callable toInvokeOnPlayFinished;
    private AtomicBoolean interrupted = new AtomicBoolean(false);

    public void setCallSession(ISession callSession) {

    }

    protected volatile AbstractMediaObject currentAbstractMediaObject = null;
    protected volatile Thread currentPlayThread = null;
    protected volatile boolean finish = false;
    /**
     * Currently playing media objects call id
     */
    protected volatile Object currentCallId = null;

    /**
     * Holds the number of played media objects
     */
    protected volatile int numberOfPlayedMediaObjects = 0;

    /**
     * Number of media objects that have been "listened" to !
     */
    protected volatile int numberOfListenedToMediaObjects = 0;

    protected boolean throwIllegalArgumentAtPlay = false;

    private int delayBeforeResponseToPlay;

    public OutboundMediaStreamMock(int delayBeforeResponseToPlay) {
        this.delayBeforeResponseToPlay = delayBeforeResponseToPlay;
    }

    /**
     * The thread that simulates playing of a media object in
     * an outbound stream.
     */
    public void run() {
        PlayFinishedEvent.CAUSE cause = PlayFinishedEvent.CAUSE.PLAY_FINISHED;

        log.info("MOCK: OutboundMediaStream.Thread Started");

        // There was a race condition where the interruption in stop() did not have
        // any effect since the thread it wanted to interrupt was not yet started.
        // Using a flag "interrupted"also should do the job
        if (interrupted.get()) {
            sendPlayFinished(PlayFinishedEvent.CAUSE.PLAY_CANCELLED);
        } else

            // First do we have anything to play ?
            if (currentAbstractMediaObject != null) {

                // Lets wait a while to simulate the play, lets say size divided by the
                // media bitrate (approximate). For now this is 8 kbytes/second, G.771 u-Law.
                // But we hot it up with 4 times the speed !!!
                try {
                    long millis;
                    final MimeType contentType = this.currentAbstractMediaObject.getMediaProperties().getContentType();
                    if (contentType != null && contentType.toString().equals("text/plain")) {
                        StringWriter sw = new StringWriter();
                        InputStream is = this.currentAbstractMediaObject.getInputStream();
                        int c;
                        try {
                            while ((c = is.read()) != -1) sw.write(c);
                        } catch (IOException e) {
                        }
                        String promptContent = sw.toString().trim();
                        TimeValue tv = TimeValueParser.getTime(promptContent);
                        if (tv == null || tv.getTime() == 0) {
                            tv = new TimeValue(promptContent.length() * 50, TimeUnit.MILLISECONDS);
                        }
                        millis = TimeUnit.MILLISECONDS.convert(tv.getTime(), tv.getUnit());
                    } else {
                        millis = this.currentAbstractMediaObject.getSize() / 32;
                    }
                    log.info("MOCK: OutboundMediaStream.Thread Simulating play for " + millis + " milliseconds");
                    if (!finish)
                        Thread.sleep(ApplicationBasicTestCase.scale(millis));
                } catch (InterruptedException e) {
                    cause = PlayFinishedEvent.CAUSE.PLAY_CANCELLED;
                    log.info("MOCK: OutboundMediaStreamMock.Thread PlayFinishedEvent(PLAY_CANCELLED)");
                    log.info("MOCK: OutboundMediaStream.Thread Got interrupted, probably because the application wanted to cancel the play");
                }

                // If we are cancelled
                if (finish)
                    cause = PlayFinishedEvent.CAUSE.PLAY_CANCELLED;

                sendPlayFinished(cause);
                // Increase the number of played media objects
                numberOfPlayedMediaObjects++;
            } else {
                // If we get here the stop() method may have interrupted us now!
                if (interrupted.get()) {
                    sendPlayFinished(PlayFinishedEvent.CAUSE.PLAY_CANCELLED);
                }
            }

        log.info("MOCK: OutboundMediaStreamMock.Thread finished");
    }

    private void sendPlayFinished(PlayFinishedEvent.CAUSE cause) {
        // Send the play finished
        this.currentAbstractMediaObject = null;
        this.currentMediaObject = null;
        this.currentFileMediaObject = null;
        this.currentPlayThread = null;
        Object cid = this.currentCallId;
        this.currentCallId = null;

        // Tell the world we are finished
        log.info("MOCK: OutboundMediaStreamMock.Thread PlayFinishedEvent(" + cause + ")");


        if (delayBeforeResponseToPlay > 0) {
            try {
                log.info("MOCK: OutboundMediaStreamMock.Thread sleeping " + delayBeforeResponseToPlay);

                Thread.sleep(delayBeforeResponseToPlay);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        getEventDispatcher().fireEvent(new PlayFinishedEvent(cid, cause));
        invokeAtPlayFinished();
    }

    /**
     * Waits for the application to isse a play command. Not that this wait increases
     * the numberOfListenedToMediaObjects. If numberOfPlayedMediaObjects are less than
     * or equal to numberOfListenedToMediaObjects, it will wait for the numberOfPlayedMediaObejcts
     * to be greater than numberOfListenedToMediaObjects. It will then increase the number
     * of numberOfListenedToMediaObjects.
     *
     * @param timeout The timoeut of the wait in milliseconds.
     * @return Returns true of the play was issued or false if a timeout occured.
     */
    public boolean waitForPlay(long timeout) {
        long timer = 0L;

        log.info("MOCK: OutboundMediaStreamMock.waitForPlay");
        log.info("MOCK: OutboundMediaStreamMock.waitForPlay timeout " + timeout);
        log.info("MOCK: OutboundMediaStreamMock.waitForPlay for play " + numberOfListenedToMediaObjects + 1);

        // Start the busy wait loop, ugly I know but easy for now.
        while (numberOfListenedToMediaObjects >= numberOfPlayedMediaObjects && timer < timeout) {
            long shortWait = ApplicationBasicTestCase.scale(250);
            timer += shortWait; // Lets wait 250 milliseconds for the play to finish
            try {
                Thread.sleep(shortWait);
            } catch (InterruptedException e) {
                log.info("MOCK: OutboundMediaStreamMock.waitForPlay We got cancelled by someone!");
                return false;
            }
        }

        // Increase the listened to objects if the timeout did not occur.
        if (timeout > timer) {
            numberOfListenedToMediaObjects++;
            log.info("MOCK: OutboundMediaStreamMock.waitForPlay End of play detected for play " + numberOfListenedToMediaObjects);
        } else
            log.info("MOCK: OutboundMediaStreamMock.waitForPlay Timeout for play " + numberOfListenedToMediaObjects);

        // Return with the true story
        return timeout > timer;
    }

    public void create(Collection<RTPPayload> payloads,
                       ConnectionProperties connectionProperties,
                       IInboundMediaStream inboundStream) throws StackException {
        // TODO: Add something here
        log.info("MOCK: OutboundMediaStreamMock.create");
        log.info("MOCK: OutboundMediaStreamMock.create unimplemented");
    }

    public void joined(IInboundMediaStream inboundStream) {

    }

    public void joined() {

    }

    public void setCNAME(String name) {
        //mCNAME = name;
    }


    public boolean isJoined() {
        return false;
    }

    /**
     * Tells that a joined inbound stream has been removed and no more
     * data will arrive from that inbound stream.
     */
    public void unjoined() {
        // TODO: Add something here
        log.info("MOCK: OutboundMediaStreamMock.unjoined");
        log.info("MOCK: OutboundMediaStreamMock.unjoined unimplemented");
    }

    public void translated(IMediaObject mediaObject) throws StackException {
        log.info("MOCK:  OutboundMediaStreamMock.translated");
        log.info("MOCK:  OutboundMediaStreamMock.translated unimplemented");
    }

    /**
     * Sends the translated data to the endpoint.
     * <p/>
     * <strong>Note</strong> that the method {@link #translationDone} must be
     * called when all translated data has been sent.
     *
     * @param mediaObject mediaObject to send.
     */
    public void translationDone(IMediaObject mediaObject) {
        // TODO: Add something here
        log.info("MOCK: OutboundMediaStreamMock.translated");
        log.info("MOCK: OutboundMediaStreamMock.translated unimplemented");
    }

    public void translationFailed(String cause) {

    }

    /**
     * Notifies the outbound stream that no more data will be sent using the
     * {@link #translated}-method.
     * <p/>
     * <strong>Note</strong> that this method should be used together with the
     * <code>translated</code>-method and otherwise should not be called.
     */
    public void translationDone() {
        // TODO: Add something here
        log.info("MOCK: OutboundMediaStreamMock.translationDone");
        log.info("MOCK: OutboundMediaStreamMock.translationDone unimplemented");
    }

    /**
     * Sends the tokens on the RTP stream. DTMF is only supported so far as
     * as DTMF RTP payload.
     *
     * @param tokens The tokens. May not be <code>null</code>.
     * @throws IllegalArgumentException if tokens is <code>null</code>.
     * @throws IllegalStateException    If this method is called before
     *                                  {@link #create}
     *                                  or if <code>delete</code> has been
     *                                  called on this stream.
     * @throws StackException           If some other error occured.
     */
    public void send(ControlToken[] tokens) throws StackException {
        // TODO: Add somehing here
        log.info("MOCK: OutboundMediaStreamMock.send");
        log.info("MOCK: OutboundMediaStreamMock.send unimplemented");
    }

    /**
     * Plays the media object on the stream using the <code>playOption</code>
     * and starts from <code>cursor</code>. For a video media object, the media
     * stream locates the closest intra frame after <code>cursor</code> to
     * start streaming from.
     * <p/>
     * If the media object is the same as a previously stopped media object,
     * any internal data such as packetization is reused.
     *
     * @param callId      Identifies this call. Is included in all events
     *                    originating from this call. May not be
     *                    <code>null</code>.
     * @param mediaObject Media source. May not be <code>null</code>.
     * @param playOption  Tells how the playing shall be done. May not be
     *                    <code>null</code>.
     * @param cursor      Start location in milliseconds in the media object
     *                    for playing. Must be &gt= 0.
     * @throws IllegalStateException         If this method is called before
     *                                       {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes, ConnectionProperties)}
     *                                       or if <code>delete</code> has been
     *                                       called on this stream.
     * @throws IllegalArgumentException      If <code>callId</code>,
     *                                       <code>mediaObject</code> or
     *                                       <code>playOption</code> is
     *                                       <code>null</code>, if
     *                                       <code>cursor</code> is &lt 0
     *                                       or if <code>mediaObject</code>
     *                                       is mutable.
     * @throws UnsupportedOperationException If a new play is issued when the
     *                                       stream is already playing.
     * @throws StackException                If some other error occured.
     */
    public void play(Object callId, IMediaObject mediaObject,
                     PlayOption playOption, long cursor) throws StackException {
        log.info("MOCK: Entering OutboundMediaStreamMock.play");
        log.info("MOCK: OutboundMediaStreamMock.play");
        TestEventGenerator.generateEvent(TestEvent.PLAY_STARTED);
        if (throwIllegalArgumentAtPlay) {
            log.info("MOCK: OutboundMediaStreamMock.play: Throwing in play");
            throw new IllegalArgumentException("Throwing in play() since the test is set up like that...");
        }
        // We can only do one play at a time
        if (currentPlayThread == null) {

            // Check for null object
            if (mediaObject != null) {

                log.info("MOCK: OutboundMediaStreamMock.play PlayOption " + playOption.decimalDef());
                log.info("MOCK: OutboundMediaStreamMock.play Cursor " + cursor);
                if (mediaObject instanceof FileMediaObject) {
                    this.currentFileMediaObject = (FileMediaObject) mediaObject;
                    this.currentAbstractMediaObject = this.currentFileMediaObject;
                }
                if (mediaObject instanceof MediaObject) {
                    this.currentMediaObject = (MediaObject) mediaObject;
                    this.currentAbstractMediaObject = this.currentMediaObject;
                    // if MimeType ==
                    MediaProperties mediaProperties = mediaObject.getMediaProperties();
                    if (mediaProperties != null) {
                        MimeType contentType = mediaProperties.getContentType();
                        if (contentType != null) {
                            String primaryType = contentType.getPrimaryType();
                            if (primaryType != null) {
                                if (primaryType.equals("text")) {
                                    IMediaObjectIterator iter = mediaObject.getNativeAccess().iterator();
                                    ByteBuffer buff = null;
                                    String result = "";
                                    while (iter.hasNext()) {
                                        try {
                                            buff = iter.next();
                                            result += new String(buff.array());
                                        } catch (MediaObjectException e) {
                                            log.debug("Caught MediaObjectException");
                                        }
                                    }
                                    log.info(result);
                                }
                            }
                        }
                    }
                }
                log.info("MOCK: OutboundMediaStreamMock.play Size " + this.currentAbstractMediaObject.getSize());
                this.currentCallId = callId;

                // Simulate the play
                finish = false;
                currentPlayThread = new Thread(this);
                currentPlayThread.start();

            } else {

                log.info("MOCK: OutboundMediaStreamMock.play Immediate Play Finished for NULL object");
                getEventDispatcher().fireEvent(new PlayFinishedEvent(callId, PlayFinishedEvent.CAUSE.PLAY_FINISHED));
                invokeAtPlayFinished();

            }

        } else {
            throw new StackException("Unable to make two plays at the same time");
        }
        log.info("MOCK: OutboundMediaStreamMock.play Playjob started");
    }

    public void play(Object callId, IMediaObject mediaObjects[], PlayOption playOption, long cursor) throws StackException {
        log.info("MOCK: Entering OutboundMediaStreamMock.play");
        if(mediaObjects.length > 1)
            log.info("MOCK: OutboundMediaStreamMock.play (multiple MediaObjects provided)");
        else
            log.info("MOCK: OutboundMediaStreamMock.play");        
        TestEventGenerator.generateEvent(TestEvent.PLAY_STARTED);

        if (throwIllegalArgumentAtPlay) {
            log.info("MOCK: OutboundMediaStreamMock.play: Throwing in play");
            throw new IllegalArgumentException("Throwing in play() since the test is set up like that...");
        }

        // We can only do one play at a time
        if (currentPlayThread == null) {

            log.info("MOCK: OutboundMediaStreamMock.play PlayOption " + playOption.decimalDef());
            log.info("MOCK: OutboundMediaStreamMock.play Cursor " + cursor);

            // Do we have anything to play ?
            if (mediaObjects.length > 0) {

                // Do we have anything to play ?
                if (mediaObjects[0] != null) {
                    if (mediaObjects[0] instanceof FileMediaObject) {
                        this.currentFileMediaObject = (FileMediaObject) mediaObjects[0];
                        this.currentAbstractMediaObject = this.currentFileMediaObject;
                    }
                    if (mediaObjects[0] instanceof MediaObject) {
                        this.currentMediaObject = (MediaObject) mediaObjects[0];
                        this.currentAbstractMediaObject = this.currentMediaObject;
                        if (mediaObjects[0] != null &&
                                mediaObjects[0].getMediaProperties() != null &&
                                mediaObjects[0].getMediaProperties().getContentType() != null &&
                                mediaObjects[0].getMediaProperties().getContentType().getPrimaryType().equals("text")) {
                            IMediaObjectIterator iter = mediaObjects[0].getNativeAccess().iterator();
                            ByteBuffer buff = null;
                            String result = "";
                            Charset charset = Charset.forName("ISO-8859-1");
                            CharsetDecoder decoder = charset.newDecoder();
                            while (iter.hasNext()) {
                                try {
                                    buff = iter.next();
                                    CharBuffer cbuf = decoder.decode(buff);
                                    result += cbuf.toString();
                                } catch (MediaObjectException e) {
                                    log.debug("Caught MediaObjectException");
                                } catch (CharacterCodingException e) {
                                    e.printStackTrace();
                                }
                            }
                            log.info(result);
                        }
                    }
                    log.info("MOCK: OutboundMediaStreamMock.play Size " + this.currentAbstractMediaObject.getSize());
                    this.currentCallId = callId;

                    // Simulate the play
                    finish = false;
                    currentPlayThread = new Thread(this);
                    currentPlayThread.start();
                } else {
                    // Immediate return for arrays of zero length
                    log.info("MOCK: OutboundMediaStreamMock.play Immediate Play Finished for NULL objects in array");
                    getEventDispatcher().fireEvent(new PlayFinishedEvent(callId, PlayFinishedEvent.CAUSE.PLAY_FINISHED));
                    invokeAtPlayFinished();
                }

            } else {
                // Immediate return for arrays of zero length
                log.info("MOCK: OutboundMediaStreamMock.play Immediate Play Finished for zero length arrays");
                getEventDispatcher().fireEvent(new PlayFinishedEvent(callId, PlayFinishedEvent.CAUSE.PLAY_FINISHED));
                invokeAtPlayFinished();
            }

        } else {
            throw new StackException("Unable to make two plays at the same time");
        }
        log.info("MOCK: OutboundMediaStreamMock.play Playjob started");
    }

    private void invokeAtPlayFinished() {
        if (toInvokeOnPlayFinished != null) try {
            toInvokeOnPlayFinished.call();
        } catch (Exception e) {
            log.error("MOCK: OutboundMediaStreamMock Exception when invoking " + toInvokeOnPlayFinished + ":" + e);
        }
    }

    /**
     * Any ongoing streaming is canceled.
     *
     * @throws IllegalStateException If this method is called before
     *                               {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes, ConnectionProperties)}
     *                               or if <code>delete</code> has been
     *                               called on this stream.
     */
    public void cancel() {
        // TODO: Add somehing here !
        log.info("MOCK: OutboundMediaStreamMock.cancel");
        if (this.currentAbstractMediaObject != null) {
            finish = true;
            log.info("MOCK: OutboundMediaStreamMock.cancel: interrupting");
            this.currentPlayThread.interrupt();
        } else {
            log.info("MOCK: OutboundMediaStreamMock.cancel: had no currentAbstractMediaObject");
        }
        this.currentAbstractMediaObject = null;
        this.currentMediaObject = null;
        this.currentFileMediaObject = null;
    }

    public MediaMimeTypes getMediaMimeTypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* Javadoc in interface */
    public long stop(Object callId) throws StackException {
        return stop();
    }

    public long stop() throws StackException {
        // TODO: Add somehing here !
        log.info("MOCK: OutboundMediaStreamMock.cancel");
        if (this.currentAbstractMediaObject != null) {
            finish = true;
            log.info("MOCK: OutboundMediaStreamMock.cancel: interrupting");
            interrupted.set(true);
            final Thread currentPlayThread = this.currentPlayThread;
            if(currentPlayThread != null)
                currentPlayThread.interrupt();
        } else {
            log.info("MOCK: OutboundMediaStreamMock.cancel: had no currentAbstractMediaObject");
        }
        this.currentAbstractMediaObject = null;
        this.currentMediaObject = null;
        this.currentFileMediaObject = null;

        // Just a bogus return value for the cursor !!!
        return 5142L;
    }

    public RTPPayload[] getSupportedPayloads() {
        return new RTPPayload[0];
    }

    public void setThrowIllegalArgumentAtPlay(boolean throwIllegalArgumentAtPlay) {
        this.throwIllegalArgumentAtPlay = throwIllegalArgumentAtPlay;
    }

    public void invokeWhenPlayFinished(Callable callable) {
        this.toInvokeOnPlayFinished = callable;
    }

    public void sendPictureFastUpdate(int i) {
        log.info("MOCK: OutboundMediaStream.sendPictureFastUpdate: " + Integer.toString(i));
    }

    public int getSenderSSRC() {
        log.info("MOCK: OutboundMediaStream.getSenderSSRC");
        return 0;
    }

    public void create(Collection<RTPPayload> collection, ConnectionProperties connectionProperties, RTCPFeedback rtcpFeedback) throws StackException {
        log.info("MOCK: OutboundMediaStream.create with RTCPFeedback");
        create(collection, connectionProperties);
    }

    public boolean usesRTCPPictureFastUpdate() {
        log.info("MOCK: OutboundMediaStream.usesRTCPPictureFastUpdate return false");
        return false;
    }

	public void create(Collection<RTPPayload> payloads,
			ConnectionProperties connectionProperties) throws StackException {
		// TODO Auto-generated method stub
		
	}

	public void create(Collection<RTPPayload> payloads,
			ConnectionProperties connectionProperties,
			RTCPFeedback rtcpFeedback, IInboundMediaStream inboundStream)
			throws StackException {
		// TODO Auto-generated method stub
		
	}
}
