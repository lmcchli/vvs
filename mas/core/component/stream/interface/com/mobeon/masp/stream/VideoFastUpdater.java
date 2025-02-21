/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

/**
 * Interface to a component able to request Video Fast Updates.
 * A video fast update request is sent for a video stream in order to receive an
 * I-frame as soon as possible.
 *
 * @author Malin Flodin
 */
public interface VideoFastUpdater {

    /**
     * Sends a Picture Fast Update request which will result in an I-frame on
     * the video stream as soon as possible.
     */
    public void sendPictureFastUpdateRequest();
}
