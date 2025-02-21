#include "Player.h"
#include "playjob.h"
#include "jlogger.h"
#include "jniutil.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.Player";

Player::Player(JNIEnv* env, OutboundSession& session) :
        mStreamsNotFinished(0), mIsPlaying(false), mJob(0), mSession(session)
{
    JLogger::jniLogDebug(env, CLASSNAME, "Player - create at %#x", this);
}

Player::~Player()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~Player - delete at %#x", this);
}

bool Player::isPlaying()
{
    return mIsPlaying;
}

bool Player::play(std::auto_ptr<PlayJob> job)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (isPlaying()) {
        job->sendPlayFailed("Another play was already in progress");
        return false;
    } else {
        mJob = job;
        try {
            mJob->init();
        } catch (std::exception& e) {
            JLogger::jniLogError(env, CLASSNAME, "Exception when initializing PlayJob: %s", e.what());
        } catch (...) {
            JLogger::jniLogError(env, CLASSNAME, "Unknown exception when initializing PlayJob.");
        }
        if (!mJob->isOk()) {
            mJob->sendPlayFailed("Failed to initialize PlayJob");
            return false;
        }
        mIsPlaying = true;
        mStreamsNotFinished = 1;
        if (mJob->isVideo()) {
            mStreamsNotFinished = 2;
        }
        mJob->putPacketsOnQueue();
    }
    return true;
}

void Player::joined()
{
    // The purpose of this call is to ensure progressing
    // PLAY will be properly terminatet when an outbound stream
    // is joined.
    if (mJob.get() != 0) {
        mJob->joined();
    }
}

long Player::stop()
{
    if (mIsPlaying) {
        if (mJob.get() != 0) {
            mJob->stop();
            mIsPlaying = false;
        }
        return getCursor();
    }
    return 0;
}

long Player::getCursor()
{
    return mJob->getCursor();
}

void Player::onTick()
{
    if (mIsPlaying && mJob.get() != 0)
        mJob->onTick();
}

void Player::cancel()
{
    //Very similar to stop, i think :)
}

void Player::onPlayFinished()
{
    mStreamsNotFinished--;
    if (mStreamsNotFinished <= 0) {
        mIsPlaying = false;
        mJob->sendPlayFinished();
    }
}

PlayJob& Player::getJob()
{
    return *mJob;
}
