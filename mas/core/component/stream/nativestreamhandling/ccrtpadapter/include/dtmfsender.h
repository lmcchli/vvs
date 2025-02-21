/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef DTMFSENDER_H_
#define DTMFSENDER_H_

#include <base_std.h>

#include <ccrtp/rtp.h>
#include <boost/ptr_container/ptr_deque.hpp>
#include <boost/ptr_container/ptr_list.hpp>

#include "platform.h"
#include "int.h"
#include "controltoken.h"

class OutboundSession;

/**
 * Sends DTMF events. This class implements the algorithm described in 
 * RFC 2833. It does not, however, implement the redundancy mechanism 
 * described in RFC 2198. Thus, this implementation always blocks an
 * ongoing audio transmission.
 * <p>
 * Note that this class claims ownership over all ControlToken instances
 * that are added. They are deleted after being sent.
 * 
 * @author J�rgen Terner
 */
class DTMFSender
{
private:
    DTMFSender(DTMFSender& rhs);
    DTMFSender& operator=(const DTMFSender& rhs);

    OutboundSession& mSession;
    /** 
     * <code>true</code> if the code is executed on a little-endian machine,
     * <code>false</code> if the machine is big-endian. 
     */
    bool mIsLittleEndian;

    /** 
     * Events currently in the queue. This member is part of the state, 
     * protected by mutex <code>mStateMutex</code>.
     */
    boost::ptr_deque<ControlToken> mTokens;

    /** 
     * The RTP timestamp after the last token was sent. This member is part 
     * of the state, protected by mutex <code>mStateMutex</code>.
     */
    uint32 mRtpTimestampAfterLastToken;

    unsigned m_masterPayloadType;
    unsigned m_masterClockRate;
    unsigned m_dtmfPayloadType;
    unsigned m_dtmfClockRate;
    unsigned m_pTime;

    /**
     * Writes the given payload information to the given buffer.
     * 
     * @param buffer   Payload buffer.
     * @param digit    Event digit.
     * @param volume   Event volume.
     * @param duration Current event duration. This is increased for
     *                 each packet if duration is > pTime.
     * @param endBit   <code>true</code> if this is the last packet of
     *                 the event.
     */
    void buildPayload(uint8 (&buffer)[4], int digit, int volume, int duration, bool endBit);

    /**
     * Writes a word (16 bits) to the given memory position.
     * 
     * @param ptr Pointer to allocated memory
     * @param uw  Reference to the word that shall be written.
     */
    void /*DTMFSender::*/writeW(uint8 *ptr, uint16_t &uw);

public:
    /**
     * Creates a new instance.
     * 
     * @param session     Session used to send events on.
     * @param contentInfo Contains payload type for DTMF events.
     */
    DTMFSender(JNIEnv *env, OutboundSession& session);

    /**
     * Destructor.
     */
    ~DTMFSender();

    void setMasterPayloadFormat(unsigned payloadType, unsigned clockRate);
    void setDtmfPayloadFormat(unsigned payloadType, unsigned clockRate);
    void setPTime(unsigned pTime);
    void setClockRate(unsigned clockRate);

    /**
     * Sends the next event if there is one waiting in the queue.
     * 
     * @param currentPayloadType Before the method ends, if 
     *                           <code>currentPayloadType<code> is != 
     *                           <code>NULL</code> and the payload type
     *                           has been modified, it must be reset to this 
     *                           type.
     * 
     * @return Duration of the sent token in timestamp units, zero if
     *         no token was sent.
     */
    uint32 sendNextToken();
    uint32 sendToken(ControlToken* token);

    /**
     * Add events to the queue.
     * 
     * @param The events that should be sent.
     */
    void addToSendList(std::auto_ptr<boost::ptr_list<ControlToken> >& tokens);

    /**
     * Gets the RTP timestamp after the last token was sent. This can be useful
     * if a promt is played directly after a token with a long duration is
     * sent. In this case, the timestamp of the prompt must be this timestamp
     * or later.
     * 
     * @return RTP timestamp after the last token was sent.
     */
    uint32 getRtpTimestampAfterLastToken();
};

#endif /*DTMFSENDER_H_*/
