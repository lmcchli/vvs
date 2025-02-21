/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * The enums herein corresponds to IDs for all Requests/Events/Responses
 * that are handled by the MRCP (TTS/ASR) states.
 */

public enum MrcpEventId {
    SPEAK,
    STOP,
    SPEAK_COMPLETE,
    BARGE_IN_OCCURRED,
    CONTROL,
    PAUSE,
    RESUME,
    SPEECH_MARKER,
    RECOGNIZE,
    RECOGNITION_COMPLETE_SUCCESS,
    RECOGNITION_COMPLETE_NO_INPUT,
    RECOGNITION_COMPLETE_NO_MATCH,
    RECOGNITION_COMPLETE_FAIL,
    GET_RESULT,
    START_OF_SPEECH,
    DEFINE_GRAMMAR,
    RECOGNITION_START_TIMERS
}
