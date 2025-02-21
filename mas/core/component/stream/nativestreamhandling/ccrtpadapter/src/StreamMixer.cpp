#include "StreamMixer.h"
#include "controltoken.h"
#include "jlogger.h"
#include "jniutil.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.StreamMixer";

//Called from thread in outbound session !!!!
size_t StreamMixer::addConnection(std::auto_ptr<StreamConnection>& connection)
{
    MutexLock lock(mNewConnectionMutex);
    size_t handle = (size_t) &(*connection);
    mNewConnections.push_back(connection.release());
    return handle;
}

void StreamMixer::removeConnection(size_t handle)
{
    StreamConnection *ptr = (StreamConnection*) handle;
    mConnections.erase(ptr);
    {
        MutexLock lock(mNewConnectionMutex);
        std::list<StreamConnection*>::iterator iter = mNewConnections.begin();
        for (; iter != mNewConnections.end(); iter++) {
            if (*iter == ptr) {
                mNewConnections.erase(iter);
                break;
            }
        }
    }
    ptr->close();
    delete ptr;
    ptr = NULL;
}

void StreamMixer::redirectPacket(uint32 timestamp, std::auto_ptr<const ost::AppDataUnit>& adu, bool isAudio)
{
    if (mExpectedConnections > 0) {
        MutexLock lock(mNewConnectionMutex);
        if (!mNewConnections.empty()) {
            std::list<StreamConnection*>::iterator iter = mNewConnections.begin();
            for (; iter != mNewConnections.end(); iter++) {
                (*iter)->open();
                mConnections.insert(*iter);
            }
            mExpectedConnections -= mNewConnections.size();
            mNewConnections.clear();
        }
    }
    if (mConnections.empty()) {
        return;
    }

    std::set<StreamConnection*>::iterator iter;
    for (iter = mConnections.begin(); iter != mConnections.end(); ++iter) {
        if (isAudio) {
            (*iter)->sendAudioPacket(timestamp, adu);
        } else {
            (*iter)->sendVideoPacket(timestamp, adu);
        }
    }
}

void StreamMixer::redirectDTMFPacket(uint32 timestamp, std::auto_ptr<const ost::AppDataUnit>& adu,
        int masterPayloadType, unsigned clockrate)
{
    if (mExpectedConnections > 0) {
        MutexLock lock(mNewConnectionMutex);
        if (!mNewConnections.empty()) {
            std::list<StreamConnection*>::iterator iter = mNewConnections.begin();
            for (; iter != mNewConnections.end(); iter++) {
                (*iter)->open();
                mConnections.insert(*iter);
            }
            mExpectedConnections -= mNewConnections.size();
            mNewConnections.clear();
        }
    }
    if (mConnections.empty()) {
        return;
    }

    std::set<StreamConnection*>::iterator iter;
    for (iter = mConnections.begin(); iter != mConnections.end(); ++iter) {
        (*iter)->sendDTMFPacket(timestamp, adu, masterPayloadType, clockrate);
    }
}

void StreamMixer::redirect(ControlToken* token)
{
    if (mConnections.empty()) {
        return;
    }

    std::set<StreamConnection*>::iterator iter;
    for (iter = mConnections.begin(); iter != mConnections.end(); ++iter) {
        (*iter)->sendControlToken(token);
    }
}

void StreamMixer::incomingConnection()
{
    mExpectedConnections++;
}

StreamMixer::StreamMixer(JNIEnv* env) :
        mNewConnectionMutex(), mExpectedConnections(0), mNewConnections(), mConnections()
{
    JLogger::jniLogDebug(env, CLASSNAME, "StreamMixer - create at %#x", this);
}

StreamMixer::~StreamMixer()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    std::set<StreamConnection*>::iterator iter;
    for (iter = mConnections.begin(); iter != mConnections.end(); ++iter) {
        delete *iter;
    }

    // Normally mNewConnections is empty because all new connections are transfered
    // to mConnections when redirecting packets. But it can happen that the destructor
    // is called before the redirection; in this case we need to delete connections to
    // free the allocated memory.
    for (std::list<StreamConnection*>::iterator listIter = mNewConnections.begin(); listIter != mNewConnections.end();
            ++listIter) {
        delete *listIter;
    }

    JLogger::jniLogDebug(env, CLASSNAME, "~StreamMixer - delete at %#x", this);
}
