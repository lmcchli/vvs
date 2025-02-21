package com.mobeon.masp.mediatranslationmanager.mrcpstack;

public interface RtspConnectionFactory {
    public RtspConnection create(String hostName, int portNumber);
}
