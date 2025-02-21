/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

/**
 * A MediaResourceFilterer can be injected with a {@link MediaResourceFilter}.
 * A {@link MediaResourceFilter} matches a {@link IMediaContentResource} to the characteristics
 * given in a {@link MediaContentResourceProperties}.
 */
public interface MediaResourceFilterer {
    void setMediaResourceFilter(MediaResourceFilter filter);
}
