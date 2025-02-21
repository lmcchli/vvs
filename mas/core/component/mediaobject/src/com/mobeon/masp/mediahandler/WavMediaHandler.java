package com.mobeon.masp.mediahandler;

import java.io.IOException;

import jakarta.activation.MimeType;

import com.mobeon.masp.mediahandler.MediaHandler;
import com.mobeon.masp.mediahandler.WavMediaFile;
import com.mobeon.masp.mediahandler.WavMediaHandler;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;

public class WavMediaHandler implements MediaHandler {

	private static ILogger log = ILoggerFactory.getILogger(WavMediaHandler.class);
	
	private final long MAX_SIZE_FOR_CONCATENATE = 4800000; // 4.8MB = 10minutes of audio @ 64kbit/s
	private final IMediaObjectFactory mediaObjectFactory;
	private final MimeType mimeType;
	
	
	public WavMediaHandler(IMediaObjectFactory mediaObjectFactory, MimeType mimeType) {
		this.mediaObjectFactory = mediaObjectFactory;
		this.mimeType = mimeType;
	}

	
	/**
	 * Return the mime type that this media handler supports.
	 * 
	 * @return the MimeType handler by this media handler
	 */
	public MimeType getMimeType() {
		return mimeType;
	}


	/**
	 * Return true if this media handler supports the concatenate method.
	 * 
	 * @return true if concatenate is supported, false otherwise.
	 */
	public boolean hasConcatenate() {
		return true;
	}
	
	/**
	 * Concatenate two media objects containing audio of WAV-File type.
	 * The following limitations apply:
	 *  - The two WAV-files must have same format.
	 *  - The WAV must contain one fmt-chunk and one data-chunk. 
	 *  - The data chunk must be a direct sub-chunk of the RIFF chunk 
	 *     and not part of a wavl-chunk.
	 *  - The fmt-chunk should be placed before the data-chunk for 
	 *     performance reasons.
	 *  - Chunks other than fmt and data are ignored.
	 *  - The following compression codes are supported: 6(a-law) and 7(u-law)
	 *   
	 * @param mo1 First media object.
	 * @param mo2 Second media object.
	 * @return a new MediaObject containing the concatenation of mo1 and mo2.
	 * @throws IOException - if the media objects could not be concatenated.
	 */
    public IMediaObject concatenate(IMediaObject mo1, IMediaObject mo2) 
    	throws IOException {
    	
    	// Limit max size for append. It shouldn't be possible to 
    	// crash MAS with out of memory just by sending in some large media objects...
    	long totalSize = mo1.getSize() + mo2.getSize(); 
    	if(totalSize > MAX_SIZE_FOR_CONCATENATE) {
    		throw new IOException("The total size of the media objects to concatenate ("
    				+ totalSize + ") is too big. Max allowed is: " + 
    				MAX_SIZE_FOR_CONCATENATE);
    	}
    	
    	// Parse first media object
    	WavMediaFile w1 = new WavMediaFile(mediaObjectFactory);
    	w1.parseMediaObject(mo1);

    	// Parse second media object
    	WavMediaFile w2 = new WavMediaFile(mediaObjectFactory);
    	w2.parseMediaObject(mo2);
    	
    	// Assert that it is a format that we support
    	if (!w1.typeIsSupported()) {
    		throw new IOException("Cannot concatenate. WAV-file type not supported.");
    	}

    	// Compare the format of the WAV-files contained in the two media objects
    	if (!w1.typeEquals(w2)) {
    		throw new IOException("Cannot concatenate. WAV-file formats differ.");
    	}
    	
    	long w1len = w1.getSampleLength();
    	long w2len = w2.getSampleLength();
    	long totLen = w1len + w2len;

    	if (log.isDebugEnabled())
    		log.debug("About to concatenate two media objects: Length of 1st=" + 
    				w1len + " Length of 2nd=" + w2len + " Total length=" + totLen);
    	
    	// Create a new empty media object
    	IMediaObject mo3 = mediaObjectFactory.create();
    	
    	// Append a header
    	w1.appendWavHeader(mo3, totLen);

    	boolean paddingNeeded;
    	if ((totLen & 1) == 1)
    		paddingNeeded = true;
    	else 
    		paddingNeeded = false;

    	// Append the sample data from mo1 if not empty
    	if (w1len > 0 && w2len == 0)
        	w1.appendSampleDataTo(mo3, paddingNeeded);
    	else if (w1len > 0)
    		w1.appendSampleDataTo(mo3, false);
    	
    	// Append the sample data from mo2 if not empty
    	if (w2len > 0)
    		w2.appendSampleDataTo(mo3, paddingNeeded);
    	
    	// Set length in milliseconds for resulting media object
    	long lenInMillis = 1000*totLen/w1.getAverageBytesPerSecond();
    	mo3.getMediaProperties().addLengthInUnit(
    			MediaLength.LengthUnit.MILLISECONDS, lenInMillis);
    	
    	// If content-type was set on first media object, copy it
    	MimeType contentType = mo1.getMediaProperties().getContentType();
    	if (contentType != null) {
    		mo3.getMediaProperties().setContentType(contentType);
    	}

    	// If file type was set on first media object, copy it
    	String fileExt = mo1.getMediaProperties().getFileExtension();
    	if (fileExt != null) {
    		mo3.getMediaProperties().setFileExtension(fileExt);
    	}
    	
    	// Set resulting media object to immutable when finished
    	mo3.setImmutable();
    	
    	return mo3;
    }

    public IMediaObject concatenateAmr(IMediaObject mo1, IMediaObject mo2, String codec) throws IOException {
        throw new IOException("concatenateAmr not supported in WavMediaHandler");
    }

    public IMediaObject ThreegppCleanup(IMediaObject mo1) throws IOException {
        throw new IOException("ThreegppCleanup not supported in WavMediaHandler");
    }
}
