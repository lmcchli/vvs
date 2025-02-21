package com.mobeon.masp.stream;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: EERITEG
 * Date: 2007-jun-29
 * Time: 10:51:40
 * To change this template use File | Settings | File Templates.
 */
public class RTPPlayer {
    private String dumpfile;


    public static void main(String[] args) {
        try {
            int audioPort = Integer.parseInt(args[1]);
            String audioReference = null;
            RTPDump audioDump = null;

            if (audioPort > 0) {
                audioReference = args[3];
                audioDump = new RTPDump(audioReference);
                audioDump.load(audioReference);
            }

            int videoPort = Integer.parseInt(args[2]);
            String videoReference = null;
            RTPDump videoDump = null;
            if (videoPort > 0) {
                videoReference = (audioPort > 0 ? args[4] : args[3]);
                videoDump = new RTPDump(videoReference);
                videoDump.load(videoReference);
            }

            RTPPlayer p = new RTPPlayer();
            p.play(args[0], audioPort, videoPort, audioDump, videoDump, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void play(String host, int audioPort, int videoPort, RTPDump audioDump, RTPDump videoDump, boolean waitUntilFinished) {
        long audioStart = 0;
        long videoStart = 0;

        if (audioDump != null && videoDump != null) {
            long offset = RTPDump.calculateOffset(audioDump, videoDump);

            if (offset < 0) {
                videoStart = -offset;
            } else {
                audioStart = offset;
            }
        }

        System.out.println("Audiostart: " + audioStart);
        System.out.println("Videostart: " + videoStart);

        RTPDumpPlayer audioPlayer = null;
        if (audioDump != null) {
            audioPlayer = new RTPDumpPlayer(audioDump, host, audioPort, audioPort + 1, audioStart);
        }

        RTPDumpPlayer videoPlayer = null;
        if (videoDump != null) {
            videoPlayer = new RTPDumpPlayer(videoDump, host, videoPort, videoPort + 1, videoStart);
        }

        if (audioPlayer != null) {
            System.out.println("Playing Audio");
            audioPlayer.play();
        }

        if (videoPlayer != null) {
            System.out.println("Playing Video");
            videoPlayer.play();
        }

        if (waitUntilFinished) {
            if (audioPlayer != null) {
                audioPlayer.waitUntilFinished();
            }

            if (videoPlayer != null) {
                videoPlayer.waitUntilFinished();
            }
        }
    }
}