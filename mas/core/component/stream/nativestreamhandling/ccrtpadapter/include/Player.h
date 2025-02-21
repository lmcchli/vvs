#ifndef PLAYER_H_
#define PLAYER_H_

#include "playjob.h"
#include "jni.h"

class Player
{
public:
    Player(JNIEnv *env, OutboundSession& session);
    virtual ~Player();

    bool isPlaying();
    bool play(std::auto_ptr<PlayJob> job);
    void joined();
    long stop();
    void cancel();
    long getCursor();
    void onPlayFinished();
    void onTick();
    PlayJob& getJob();
protected:
    int mStreamsNotFinished;
    bool mIsPlaying;
    std::auto_ptr<PlayJob> mJob;
    OutboundSession& mSession;
};

#endif /*PLAYER_H_*/
