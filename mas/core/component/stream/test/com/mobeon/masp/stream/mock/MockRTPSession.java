package com.mobeon.masp.stream.mock;

import com.mobeon.masp.stream.*;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

public class MockRTPSession implements RTPSession {
    IMediaStream stream;
    StackEventNotifier stackEventNotifier;
    Object requestId;
    private MimeType AUDIO_WAV;

    public void init(IMediaStream stream) {
        this.stream = stream;
        try {
            AUDIO_WAV = new MimeType("audio/wav");
        } catch (MimeTypeParseException e) {
            e.printStackTrace();
        }
    }

    public void play(int requestId, IMediaObject mediaObject, IOutboundMediaStream.PlayOption playOption, long cursor) throws StackException {
        stackEventNotifier.playFinished(requestId, PlayFinishedEvent.CAUSE.PLAY_FINISHED, 1);
    }

    public void record(Object callId, IMediaObject playMediaObject, IOutboundMediaStream outboundStream, IMediaObject recordMediaObject, RecordingProperties properties) throws StackException {
        requestId = callId;
        if (callId instanceof Integer) {
            switch ((Integer)callId) {
                case 1:
                    stackEventNotifier.recordFailed(requestId,
                            RecordFailedEvent.CAUSE.MIN_RECORDING_DURATION.ordinal(), null);
                    break;

                case 2:
                    recordMediaObject.getMediaProperties().setContentType(AUDIO_WAV);
                    recordMediaObject.getMediaProperties().setFileExtension("wav");
                    recordMediaObject.getMediaProperties().setSize(0);
                    recordMediaObject.setImmutable();
                    stackEventNotifier.recordFinished(requestId,
                            RecordFinishedEvent.CAUSE.STREAM_ABANDONED.ordinal(), null);
                    break;

                case 3:
                    recordMediaObject.getMediaProperties().setContentType(AUDIO_WAV);
                    recordMediaObject.getMediaProperties().setFileExtension("wav");
                    recordMediaObject.getMediaProperties().setSize(0);
                    recordMediaObject.setImmutable();
                    stackEventNotifier.recordFinished(requestId,
                            RecordFinishedEvent.CAUSE.STREAM_DELETED.ordinal(), null);
                    break;

                case 4:
                    recordMediaObject.getMediaProperties().setContentType(AUDIO_WAV);
                    recordMediaObject.getMediaProperties().setFileExtension("wav");
                    recordMediaObject.getMediaProperties().setSize(0);
                    recordMediaObject.setImmutable();
                    stackEventNotifier.recordFinished(requestId,
                            RecordFinishedEvent.CAUSE.RECORDING_STOPPED.ordinal(), null);
                    break;

                default:
                    break;
            }
        } else {
            recordMediaObject.getMediaProperties().addLengthInUnit(MediaLength.LengthUnit.MILLISECONDS, 17);            
            recordMediaObject.setImmutable();
            stackEventNotifier.recordFinished(requestId, 0, "");
        }
    }

    public void create(StreamContentInfo contentInfo, ConnectionProperties connectionProperties, StackEventNotifier eventNotifier, int localAudioPort, int localVideoPort,
                       IInboundMediaStream inboundStream) throws StackException {
        this.stackEventNotifier = eventNotifier;
    }

    public void delete(int requestId) {
        MediaStreamSupport s =(MediaStreamSupport)stream;

        s.releasePorts();
    }

    public long stop(Object callId) throws StackException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getCumulativePacketLost() throws StackException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public short getFractionLost() throws StackException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cancel() throws StackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void send(ControlToken[] tokens) throws StackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSkew(IMediaStream.SkewMethod method, long skew) throws StackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void join(boolean handleDtmfAtInbound, IOutboundMediaStream outboundStream, boolean forwardDtmfToOutbound) throws StackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unjoin(IOutboundMediaStream outboundStream) throws StackException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void sendPictureFastUpdate(int ssrc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getSenderSSRC() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
    public void reNegotiatedSdp(RTPPayload dtmfPayLoad) {
    }
}
