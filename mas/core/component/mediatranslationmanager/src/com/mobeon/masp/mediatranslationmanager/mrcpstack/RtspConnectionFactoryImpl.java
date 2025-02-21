package com.mobeon.masp.mediatranslationmanager.mrcpstack;

public class RtspConnectionFactoryImpl implements RtspConnectionFactory {
    public RtspConnection create(String hostName, int portNumber) {
        return new RtspConnectionImpl(hostName, portNumber);
    }
}
