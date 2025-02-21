/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * Tells how a recording is to be done.
 *
 * @author Jörgen Terner
 */
public class RecordingProperties {
    public enum RecordingType {
        UNKNOWN,
        AUDIO,
        VIDEO
    }

    /**
     * If <code>true</code>, recording will start when silence stops.
     * If <code>false</code>, recording will start immediately.
     */
    private boolean silenceDetectionForStart;

    /**
     * If <code>true</code>, recording will stop when silence is detected.
     */
    private boolean silenceDetectionForStop;

    /**
     * Maximum wait time in milliseconds before recording has started.
     */
    private int maxWaitBeforeRecord;

    /**
     * Maximum recording duration in milliseconds. If the recording duration
     * exceeded the maximum specified, the recording will stop and only
     * the media recording up to the maximum duration will be saved.
     */
    private int maxRecordingDuration;

    /**
     * Minimum recording duration in milliseconds. If the recording duration
     * is less than specified, the result of the recording is considered
     * being "No Recording".
     */
    private int minRecordingDuration;

    /**
     * If <code>true</code>, record waits for recording to finish before
     * returning. If <code>false</code>, record returns immediately and
     * an event will signal that recording has finished.
     */
    private boolean waitForRecordToFinish;

    private RecordingType recordingType = RecordingType.UNKNOWN;

    /**
     * Maximum silence time in seconds before recording automatically stops.
     */
    private int maxSilence;

    private int timeout;

    public boolean isSilenceDetectionForStart() {
        return silenceDetectionForStart;
    }

    public void setSilenceDetectionForStart(boolean silenceDetectionForStart) {
        this.silenceDetectionForStart = silenceDetectionForStart;
    }

    public boolean isSilenceDetectionForStop() {
        return silenceDetectionForStop;
    }

    public void setSilenceDetectionForStop(boolean silenceDetectionForStop) {
        this.silenceDetectionForStop = silenceDetectionForStop;
    }

    public int getMaxWaitBeforeRecord() {
        return maxWaitBeforeRecord;
    }

    public void setMaxWaitBeforeRecord(int maxWaitBeforeRecord) {
        this.maxWaitBeforeRecord = maxWaitBeforeRecord;
    }

    public int getMaxRecordingDuration() {
        return maxRecordingDuration;
    }

    public void setMaxRecordingDuration(int maxRecordingDuration) {
        this.maxRecordingDuration = maxRecordingDuration;
    }

    public int getMinRecordingDuration() {
        return minRecordingDuration;
    }

    public void setMinRecordingDuration(int minimumRecordingDuration) {
        this.minRecordingDuration = minimumRecordingDuration;
    }

    public boolean isWaitForRecordToFinish() {
        return waitForRecordToFinish;
    }

    public void setWaitForRecordToFinish(boolean waitForRecordToFinish) {
        this.waitForRecordToFinish = waitForRecordToFinish;
    }

    public int getMaxSilence() {
        return maxSilence;
    }

    public void setMaxSilence(int maxSilence) {
        this.maxSilence = maxSilence;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public RecordingType getRecordingType() {
        return recordingType;
    }

    public void setRecordingType(RecordingType recordingType) {
        this.recordingType = recordingType;
    }
}
