/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef VOICEACTIVITYDETECTOR_H_
#define VOICEACTIVITYDETECTOR_H_

#include <base_std.h>
#include <ccrtp/queuebase.h>

/**
 * Detects voice activity / silence.
 * 
 * @author Jorgen Terner
 */
class VoiceActivityDetector
{
public:
    /**
     * Creates a new VoiceActivityDetector.
     */
    VoiceActivityDetector();

    /**
     * Destructor.
     */
    ~VoiceActivityDetector();

    /**
     * Collects background noise information from the given packet.
     * 
     * @param adu Comfort noise packet.
     */
    void newCNPacket(const ost::AppDataUnit* adu);

    /**
     * Collects background noise information from the given packet.
     * 
     * @param adu Data packet.
     */
    void newPacket(const ost::AppDataUnit* adu);

    /**
     * Checks if the given packet can be considered as voice activity
     * or not.
     * 
     * @param adu Packet that should be analyzed.
     * 
     * @return <code>true</code> if the payload is considered as silence.
     */
    bool isSilence(const ost::AppDataUnit* adu);
};

#endif /*VOICEACTIVITYDETECTOR_H_*/
