package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.execution_engine.runapp.ApplicationBasicTestCase;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.mediaobject.*;
import com.mobeon.masp.stream.*;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Collection;

/**
 * The mock object for the inbound media stream.
 */
public class InboundMediaStreamMock extends MediaStreamSupportMock implements IInboundMediaStream, IMediaStream, Runnable {

    /**
     * Currently recording media object
     */
    protected final AtomicReference<MediaObject> currentMediaObject = new AtomicReference<MediaObject>();
    protected final AtomicReference<FileMediaObject> currentFileMediaObject = new AtomicReference<FileMediaObject>();
    protected final AtomicReference<AbstractMediaObject> currentAbstractMediaObject = new AtomicReference<AbstractMediaObject>();
    protected final AtomicReference<Thread> currentRecordThread = new AtomicReference<Thread>();
    protected final AtomicReference<RecordingProperties> currentProperties = new AtomicReference<RecordingProperties>();
    protected final AtomicBoolean finish = new AtomicBoolean(false);
    protected final Semaphore abortSemaphore = new Semaphore(0);
    /**
     * This holds information on how we want the record to behave.
     */
    protected final AtomicLong recordLength = new AtomicLong();

    protected final AtomicReference<RecordFinishedEvent.CAUSE> recordFinishedEvent = new AtomicReference<RecordFinishedEvent.CAUSE>();
    protected final AtomicReference<RecordFailedEvent.CAUSE> recordFailedEvent = new AtomicReference<RecordFailedEvent.CAUSE>();

    /**
     * The current call that is doing recording
     */
    protected final AtomicReference<Object> currentCallId = new AtomicReference<Object>();
    private final AtomicReference<CallManagerMock.EventType> responseToRecord = new AtomicReference<CallManagerMock.EventType>();
    private final AtomicReference<Callable> toInvokeWhenRecord = new AtomicReference<Callable>();

    /**
     * The thread that simulates recording of a media object in
     * an inbound stream.
     */
    public void run() {
        log.info("MOCK: InboundMediaStream.Thread Started");

        // First do we have anything to record ?
        final AbstractMediaObject abstractMediaObject = currentAbstractMediaObject.get();
        if (abstractMediaObject != null) {

            final MediaObject mediaObject = currentMediaObject.get();
            final long recLength = recordLength.get();
            if (mediaObject != null) {
                MediaProperties mediaProperties = mediaObject.getMediaProperties();


                if (mediaProperties != null) {
                    mediaProperties.addLengthInUnit(MediaLength.LengthUnit.MILLISECONDS, recLength);
                }
            }

            // Wait the alloted time before firing the event
            final RecordingProperties recordingProperties = currentProperties.get();
            try {
                log.info("MOCK: InboundMediaStream.Thread Simulating record for " + recLength + " milliseconds");

                if (!finish.get()) {
                    long timeToWait = recLength;
                    if (recordingProperties.getMaxRecordingDuration() < timeToWait)
                        timeToWait = recordingProperties.getMaxRecordingDuration();
                    abortSemaphore.tryAcquire(ApplicationBasicTestCase.scale(timeToWait), TimeUnit.MILLISECONDS);
                    log.info("InboundMediaStream.Thread Wait ended");
                }
            } catch (InterruptedException e) {

                ByteBuffer buffer = null;
// TODO: Is the allocation correct now when getMaxRecDur is in ms???
                if (recordingProperties.getMaxRecordingDuration() < recLength)
                    buffer = ByteBuffer.allocateDirect((int) recordingProperties.getMaxRecordingDuration() * 8 / 1000);
                else
                    buffer = ByteBuffer.allocateDirect((int) recLength * 8 / 1000);

                mediaObject.getNativeAccess().append(buffer);

                // Clean up before we exit
                this.currentAbstractMediaObject.set(null);
                this.currentMediaObject.set(null);
                this.currentFileMediaObject.set(null);
                this.currentRecordThread.set(null);
                this.currentProperties.set(null);
                Object cid = this.currentCallId.get();
                this.currentCallId.set(null);

                // Send away the event
                log.info("MOCK: InboundMediaStreamMock.Thread RecordFinishedEvent (RECORD_STOPPED)");
                log.info("MOCK: InboundMediaStreamMock.Thread Got interrupted, probably beucase the application wanted to stop the record");
                getEventDispatcher().fireEvent(new RecordFinishedEvent(cid, RecordFinishedEvent.CAUSE.RECORDING_STOPPED));
                return;
            }

         // TODO: Is the allocation correct now when getMaxRecDur is in ms???
            ByteBuffer buffer = null;
            buffer = ByteBuffer.allocateDirect(recordingProperties.getMaxRecordingDuration() * 8 / 1000);

            mediaObject.getNativeAccess().append(buffer);

            // Clean up before we exit
            this.currentAbstractMediaObject.set(null);
            this.currentMediaObject.set(null);
            this.currentFileMediaObject.set(null);
            this.currentRecordThread.set(null);
            this.currentProperties.set(null);
            Object cid = this.currentCallId.get();
            this.currentCallId.set(null);

            // Send away the event
            final RecordFinishedEvent.CAUSE finishedCause = recordFinishedEvent.get();
            if (!finish.get()) {
                final RecordFailedEvent.CAUSE failedCause = recordFailedEvent.get();
                if (failedCause != null) {
                    log.info("MOCK: InboundMediaStreamMock.Thread Fired " + failedCause);
                    getEventDispatcher().fireEvent(new RecordFailedEvent(cid, failedCause, "As specified to do!"));
                }
                if (finishedCause != null) {
                    log.info("MOCK: InboundMediaStreamMock.Thread Fired " + finishedCause);
                    getEventDispatcher().fireEvent(new RecordFinishedEvent(cid, finishedCause));
                } else {
                    //We must always send _something_
                    log.info("MOCK: InboundMediaStreamMock.Thread Fired " + finishedCause);
                    getEventDispatcher().fireEvent(new RecordFinishedEvent(cid, RecordFinishedEvent.CAUSE.RECORDING_STOPPED));
                }
            } else {

                log.info("MOCK: InboundMediaStreamMock.Thread Fired " + finishedCause);
                getEventDispatcher().fireEvent(new RecordFinishedEvent(cid, RecordFinishedEvent.CAUSE.RECORDING_STOPPED));

            }
        } else {
            log.info("MOCK: InboundMediaStreamMock.Thread No mediaobject to save the recording in !");
        }

        log.info("MOCK: InboundMediaStreamMock.Thread finished");
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
        log.info("MOCK: InboundMediaStreamMock.cancel");
        if (this.currentAbstractMediaObject.get() != null) {
            finish.set(true);
            abortSemaphore.release();
        }

        // Just a bogus return value for the cursor !!!
        return 5142L;
    }

    /**
     * Sets the record finished characterstics, i.e. the next record will succeed,
     * with the following cause.
     *
     * @param length
     * @param cause
     */

    public void setRecordFinished(long length, RecordFinishedEvent.CAUSE cause) {
        this.recordLength.set(length);
        this.recordFinishedEvent.set(cause);
        this.recordFailedEvent.set(null);
    }

    /**
     * Sets the record finished characterstics, i.e. the next record will fail,
     * with the following cause.
     *
     * @param length
     * @param cause
     */
    public void setRecordFailed(long length, RecordFailedEvent.CAUSE cause) {
        this.recordLength .set(length);
        this.recordFinishedEvent.set(null);
        this.recordFailedEvent.set(cause);
    }

    /**
     * Creates an inbound media stream.
     *
     * @param mediaMimeTypes Describes the media of the stream.
     * @throws IllegalArgumentException If <code>mediaMimeTypes</code> is
     *                                  <code>null</code>.
     * @throws IllegalStateException    If this method has already been called
     *                                  for this stream instance or if the
     *                                  stream has been deleted.
     * @throws com.mobeon.masp.stream.CreateSessionException
     *                                  If the local session could not be
     *                                  created.
     * @throws com.mobeon.masp.stream.StackException
     *                                  If some other error occured.
     */
    public void create(MediaMimeTypes mediaMimeTypes) throws StackException {
        // TODO: Add something nice here
        log.info("MOCK: InboundMediaStream.create");
        log.info("MOCK: InboundMediaStream.create unimplemented");
    }


    public void create(VideoFastUpdater videoFastUpdater, MediaMimeTypes mediaMimeTypes) throws StackException {
        // TODO: Add something nice here
        log.info("MOCK: InboundMediaStream.create");
        log.info("MOCK: InboundMediaStream.create unimplemented");
    }

    public void create(Collection<RTPPayload> rtpPayloads) throws StackException {
        // TODO: Add something nice here
        log.info("MOCK: InboundMediaStream.create");
        log.info("MOCK: InboundMediaStream.create unimplemented");
    }

    public void create(VideoFastUpdater videoFastUpdater, Collection<RTPPayload> rtpPayloads) throws StackException {
        // TODO: Add something nice here
        log.info("MOCK: InboundMediaStream.create");
        log.info("MOCK: InboundMediaStream.create unimplemented");
    }

    /**
     * Joins this inbound stream with the given outbound stream. This causes
     * the media that comes on the inbound stream to be redirected to the
     * outbound stream.
     *
     * @param outboundStream The outbound stream.
     * @throws IllegalArgumentException If <code>outboundStream</code>
     *                                  is <code>null</code>.
     */
    public void join(IOutboundMediaStream outboundStream) {
        // TODO: Add something nice here
        log.info("MOCK: InboundMediaStream.join");
        log.info("MOCK: InboundMediaStream.join unimplemented");
    }

    /**
     * Joins this inbound stream with the given outbound stream. This causes
     * the media that comes on the inbound stream to be redirected to the
     * outbound stream.
     *
     * @param handleDtmfAtInbound   True if DTMF events should be issued by
     *                              (this) InboundStream.
     * @param outboundStream        The outbound stream.
     * @param forwardDtmfToOutbound True if DTMF should be forwarded
     *                              to outboundStream
     * @throws IllegalArgumentException If <code>outboundStream</code>
     *                                  is <code>null</code>.
     */
    public void join(boolean handleDtmfAtInbound,
                     IOutboundMediaStream outboundStream,
                     boolean forwardDtmfToOutbound) {
        // TODO: Add something nice here
        log.info("MOCK: InboundMediaStream.join");
        log.info("MOCK: InboundMediaStream.join unimplemented");
    }

    public void setCNAME(String name) {
        //mCNAME = name;
    }

    public void setCallSession(ISession callSession) {
        log.info("MOCK: InboundMediaStream.setCallSession unimplemented");
    }

    /**
     * Removes the redirection of the inbound stream to the given outbound
     * stream. When unjoin is done, all data received so far from the endpoint
     * of the inbound stream is sent to the outbound stream and the outbound
     * streams <code>unjoined()</code>-function is called.
     *
     * @param outboundStream The outbound stream.
     * @throws IllegalArgumentException If <code>outboundStream</code>
     *                                  is <code>null</code>.
     */
    public void unjoin(IOutboundMediaStream outboundStream) {
        // TODO: Add something nice here
        log.info("MOCK: InboundMediaStream.unjoin");
        log.info("MOCK: InboundMediaStream.unjoin unimplemented");
    }

    /**
     * Records the media that arrives on the inbound stream and stores it in
     * the provided media object.
     *
     * @param callId      Identifies this call. Is included in all events
     *                    originating from this call. May not be
     *                    <code>null</code>.
     * @param mediaObject Destination for incoming media.
     * @param properties  Tells how the recording is to be done.
     * @throws IllegalStateException         If this method is called before
     *                                       {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes)}
     *                                       or if <code>delete</code> has been
     *                                       called on this stream.
     * @throws IllegalArgumentException      If <code>callId</code>,
     *                                       <code>mediaObject</code> or
     *                                       <code>properties</code> is
     *                                       <code>null</code> or if
     *                                       <code>mediaObject</code> is
     *                                       immutable.
     * @throws UnsupportedOperationException If a new record is issued when the
     *                                       stream is already recording.
     * @throws com.mobeon.masp.stream.StackException
     *                                       If some other error occured.
     */
    public void record(Object callId, IMediaObject mediaObject,
                       RecordingProperties properties) throws StackException {
        // TODO: Add something nice here
        log.info("MOCK: InboundMediaStreamMock.record");
        log.info("MOCK: InboundMediaStreamMock.record Callid " + callId.toString());
        log.info("MOCK: InboundMediaStreamMock.record MaxRecordingDuration " + properties.getMaxRecordingDuration());
        log.info("MOCK: InboundMediaStreamMock.record MaxSilence " + properties.getMaxSilence());
        log.info("MOCK: InboundMediaStreamMock.record MaxWaitBeforeRecord " + properties.getMaxWaitBeforeRecord());
        log.info("MOCK: InboundMediaStreamMock.record MinRecordingDuration " + properties.getMinRecordingDuration());
        log.info("MOCK: InboundMediaStreamMock.record SilenceDetectionForStart " + properties.isSilenceDetectionForStart());
        log.info("MOCK: InboundMediaStreamMock.record SilenceDetectionForStop " + properties.isSilenceDetectionForStop());
        log.info("MOCK: InboundMediaStreamMock.record WaitForRecordToFinish " + properties.isWaitForRecordToFinish());
        log.info("MOCK: InboundMediaStreamMock.record type:" + properties.getRecordingType());

        if (responseToRecord.get() == CallManagerMock.EventType.RECORD_FAILED) {
            log.info("MOCK: InboundMediaStreamMock.record Throwing recordFailed");
            getEventDispatcher().fireEvent(new RecordFailedEvent(callId,
                    RecordFailedEvent.CAUSE.EXCEPTION, "Sending record failed since the test is setup like that"));
            return;
        }

        // Check if we are recording, if so fail !
        if (currentRecordThread.get() != null) {
            log.info("MOCK: InboundMediaStreamMock.record Unable to make rwo records at the same time!");
            throw new StackException("Unable to make two plays at the same time");
        } else {
            if (toInvokeWhenRecord.get() != null) {
                try {
                    toInvokeWhenRecord.get().call();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            finish.set(false);

            // Save the current recording
            if (mediaObject instanceof FileMediaObject) {
                this.currentFileMediaObject.set((FileMediaObject) mediaObject);
                this.currentAbstractMediaObject.set((AbstractMediaObject) this.currentFileMediaObject.get());
            }
            if (mediaObject instanceof MediaObject) {
                this.currentMediaObject.set((MediaObject) mediaObject);
                this.currentAbstractMediaObject.set(this.currentMediaObject.get());
            }

            this.currentCallId.set(callId);
            this.currentProperties.set(properties);

            // Should we return immediately or not ?
            if (properties.isWaitForRecordToFinish()) {

                // Okey, we should return when the recording has finished.
                log.info("MOCK: InboundMediaStreamMock.record Recordjob started");
                this.currentRecordThread.set(new Thread(this));
                this.currentRecordThread.get().start();
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    log.info("MOCK: InboundMediaStreamMock.record interrupted");
                }
                log.info("MOCK: InboundMediaStreamMock.record Recordjob finished");

            } else {

                // Okey we should return when the recording has started and
                // not wait for finish.
                log.info("MOCK: InboundMediaStreamMock.record Recordjob started");
                this.currentRecordThread.set(new Thread(this));
                this.currentRecordThread.get().start();
            }
        }
    }

    /**
     * @param callId
     * @param playMediaObject
     * @param outboundStream
     * @param recordMediaObject
     * @param properties
     * @throws StackException
     */
    public void record(Object callId, IMediaObject playMediaObject,
                       IOutboundMediaStream outboundStream,
                       IMediaObject recordMediaObject,
                       RecordingProperties properties) throws StackException {
        // TODO: Add something nice here
        log.info("MOCK: InboundMediaStreamMock.record is unimplemented");
        log.info("MOCK: InboundMediaStreamMock.record Callid " + callId.toString());
        log.info("MOCK: InboundMediaStreamMock.record MaxRecordingDuration " + properties.getMaxRecordingDuration());
        log.info("MOCK: InboundMediaStreamMock.record MaxSilence " + properties.getMaxSilence());
        log.info("MOCK: InboundMediaStreamMock.record MaxWaitBeforeRecord " + properties.getMaxWaitBeforeRecord());
        log.info("MOCK: InboundMediaStreamMock.record MinRecordingDuration " + properties.getMinRecordingDuration());
        log.info("MOCK: InboundMediaStreamMock.record SilenceDetectionForStart " + properties.isSilenceDetectionForStart());
        log.info("MOCK: InboundMediaStreamMock.record SilenceDetectionForStop " + properties.isSilenceDetectionForStop());
        log.info("MOCK: InboundMediaStreamMock.record WaitForRecordToFinish " + properties.isWaitForRecordToFinish());
        this.currentCallId.set(callId);
        this.currentMediaObject.set((MediaObject) recordMediaObject);
    }

    /**
     * Gets the length of time in milliseconds represented by the media
     * in a packet
     *
     * @return <code>pTime</code> for the media on this stream.
     * @throws IllegalStateException If this method is called before
     *                               {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes)}.
     */
    public int getPTime() {
        // Milliseconds ?
        log.info("MOCK: InboundMediaStream.getPTime");
        return 20;
    }

    /**
     * Gets the maximum amount of media that can be encapsulated in
     * each packet expressed in milliseconds. Should be an integer multiple of ptime.
     *
     * @return <code>maxPTime</code> for the media on this stream.
     *
     * @throws IllegalStateException If this method is called before
     *         {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes)}.
     */
    public int getMaxPTime() {
        // Milliseconds ?
        log.info("MOCK: InboundMediaStream.getMaxPTime");
        return 120;
    }

    public String getHost() {
        return "someHost";
    }

    /**
     * @return Current number of lost packets.
     * @throws IllegalStateException If this method is called before
     *                               {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes)}
     *                               or if <code>delete</code> has been
     *                               called on this stream.
     * @throws com.mobeon.masp.stream.StackException
     *                               If some error occured.
     */
    public int getCumulativePacketLost() throws StackException {
        log.info("MOCK: InboundMediaStream.getCumulativePacketLost");
        return 0;
    }

    /**
     * The loss fraction is defined as the number of packets lost in the
     * current reportinginterval, divided by the number expected. It is
     * expressed as the integer part after multiplying the loss fraction
     * by 256. Possible values are 0-255. If duplicates exists and the
     * number of received packets are greater than the number expected,
     * the loss fraction is set to zero.
     * <p/>
     * Example: If 1/4 of the packets were lost, the loss fraction would be
     * 1/4*256=64.
     *
     * @return Current loss fraction.
     * @throws IllegalStateException If this method is called before
     *                               {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes)}
     *                               or if <code>delete</code> has been
     *                               called on this stream.
     * @throws com.mobeon.masp.stream.StackException
     *                               If some error occured.
     */
    public short getFractionLost() throws StackException {
        log.info("MOCK: InboundMediaStream.getFractionLost");
        return 0;
    }

    public int getInboundBitRate() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setContentTypeMapper(ContentTypeMapper mContentTypeMapper) {
        // TODO: Do something useful here
        log.info("MOCK: InboundMediaStream.setContentTypeMapper");
        log.info("MOCK: InboundMediaStream.setContentTypeMapper unimplemented");
    }

    public void setResponseToRecord(CallManagerMock.EventType responseToRecord) {
        this.responseToRecord.set(responseToRecord);
    }

    public void invokeWhenRecord(Callable toInvokeWhenRecord) {
        this.toInvokeWhenRecord.set(toInvokeWhenRecord);
    }

    public void sendPictureFastUpdate(int i) {
        log.info("MOCK: InboundMediaStream.sendPictureFastUpdate: " + Integer.toString(i));
    }

    public int getSenderSSRC() {
        log.info("MOCK: InboundMediaStream.getSenderSSRC");
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void sendPictureFastUpdateRequest() {
        log.info("MOCK: InboundMediaStream.sendPictureFastUpdate");
    }

	@Override
	public void reNegotiatedSdp(RTPPayload dtmfPayLoad) throws StackException {
		// TODO Auto-generated method stub
		
	}
}
