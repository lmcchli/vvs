package com.mobeon.masp.stream;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: EERITEG
 * Date: 2007-jun-29
 * Time: 10:51:40
 * To change this template use File | Settings | File Templates.
 */
public class RTPRecorder {
    private String dumpfile;


    public static void main(String[] args) {
        try {
            int audioPort = Integer.parseInt(args[1]);
            String audioReference = null;
            if (audioPort > 0)
                audioReference = args[3];

            int videoPort = Integer.parseInt(args[2]);
            String videoReference = null;
            if (videoPort > 0)
                videoReference = (audioPort > 0 ? args[4] : args[3]);

            RTPRecorder r = new RTPRecorder();
            r.record(args[0], audioPort, videoPort, audioReference, videoReference);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void record(String host, int audioPort, int videoPort, String audioReference, String videoReference) throws IOException {
        RTPDumpRecorder audioRecorder = null;
        RTPDumpRecorder videoRecorder = null;

        try {
            if (audioReference != null) {
                audioRecorder = new RTPDumpRecorder(host, audioPort, audioPort + 1, 20000);
                audioRecorder.record();
            }

            if (videoReference != null) {
                videoRecorder = new RTPDumpRecorder(host, videoPort, videoPort + 1, 20000);
                videoRecorder.record();
            }

            RTPDump audioDump = null;
            if (audioRecorder != null) {
                System.out.println("Waiting for audio");
                audioRecorder.waitUntilFinished();
                System.out.println("audio ready");
                audioDump = audioRecorder.getDump();
                audioDump.sortRTPPackets();
                audioDump.store(audioReference);
            }

            RTPDump videoDump = null;
            if (videoRecorder != null) {
                System.out.println("Waiting for video");
                videoRecorder.waitUntilFinished();
                System.out.println("video ready");
                videoDump = videoRecorder.getDump();
                videoDump.sortRTPPackets();
                videoDump.store(videoReference);
            }
        } finally {
            if (audioRecorder != null) {
                audioRecorder.close();
            }

            if (videoRecorder != null) {
                videoRecorder.close();
            }
        }
    }
}