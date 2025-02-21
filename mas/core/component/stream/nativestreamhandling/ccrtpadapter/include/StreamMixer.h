#ifndef STREAMMIXER_H_
#define STREAMMIXER_H_

#include <cc++/config.h>
#include <cc++/thread.h>
#include <ccrtp/rtp.h>

#include <base_std.h>

#include "streamconnection.h"
#include "jni.h"

using namespace ost;

class ControlToken;

class StreamMixer
{
public:
    StreamMixer(JNIEnv* env);
    virtual ~StreamMixer();

    size_t addConnection(std::auto_ptr<StreamConnection>& connection);

    void removeConnection(size_t handle);

    void incomingConnection();

    void redirectPacket(uint32 timestamp, std::auto_ptr<const ost::AppDataUnit>& adu, bool isAudio);
    void redirectDTMFPacket(uint32 timestamp, std::auto_ptr<const ost::AppDataUnit>& adu, int masterPayloadType,
            unsigned clockrate);

    void redirect(ControlToken* token);

protected:

    ost::Mutex mNewConnectionMutex;
    int mExpectedConnections;
    std::list<StreamConnection*> mNewConnections;
    std::set<StreamConnection*> mConnections;

};

#endif /*STREAMMIXER_H_*/
