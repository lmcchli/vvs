package com.mobeon.masp.mediahandler;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.IMediaObjectIterator;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaObjectNativeAccess;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;

//FIXME amr-wb
public class AmrMediaFile {

    //byte[] MDAT_LABEL = new byte[] { (byte) 0x6D, (byte) 0x64, (byte) 0x61, (byte) 0x74 }; //mdat 
    //byte[] AMR_HEADER = new byte[] { 0x23, 0x21, 0x41, 0x4d, 0x52, 0x0a }; //#!AMR
    IMediaObjectFactory mediaObjectFactory = null;
    IMediaObject mo = null;

    public AmrMediaFile(IMediaObjectFactory mediaObjectFactory) {
        this.mediaObjectFactory = mediaObjectFactory;
    }

    public void parseMediaObject(IMediaObject mo) {
        this.mo = mo;
    }

}