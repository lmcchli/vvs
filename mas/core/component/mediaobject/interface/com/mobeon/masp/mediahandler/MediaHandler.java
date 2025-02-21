package com.mobeon.masp.mediahandler;

import java.io.IOException;
import jakarta.activation.MimeType;
import com.mobeon.masp.mediaobject.IMediaObject;

public interface MediaHandler {
	
	/**
	 * Get the MimeType that this media handler supports.
	 *  
	 * @return the MimeType supported.
	 */
	public MimeType getMimeType();
	
	/**
	 * Check support for the concatenate method.
	 * 
	 * @return true if this media handler supports the concatenate method,
	 * 	false otherwise. 
	 */
	public boolean hasConcatenate();
	
	/**
	 * Concatenates two media objects and returns the result in a new media object.
	 * @param mo1 first media object to concatenate.
	 * @param mo2 second media object to concatenate.
	 * 
	 * @return a new media object containing the concatenation of mo1 and mo2.
	 * @throws IOException - if the media objects could not be concatenated. 
	 */
    public IMediaObject concatenate(IMediaObject mo1, IMediaObject mo2) throws IOException;
	
    /**
     * Concatenates two AMR media objects and returns the result in a new AMR media object.
     * @param mo1 first media object to concatenate.
     * @param mo2 second media object to concatenate.
     * @param codec - the codec samr (nb) or sawb (wb)
     * 
     * @return a new AMR media object containing the concatenation of mo1 and mo2.
     * @throws IOException - if the media objects could not be concatenated. 
     */
    public IMediaObject concatenateAmr(IMediaObject mo1, IMediaObject mo2, String codec) throws IOException;

    /**
     * Transcode the 3gpp media objects into amr and back to 3GPP container
     * This has the effect of removing bad packets etc and smoothing the
     * Audio.
     * Develeoped to fix an issue with bad audio for a customer 
     * @param mo1 media object to cleanup.
     * 
     * @return a new 3GP container containing the AMR mo1 media object.
     * @throws IOException - if the media object cannot be transcoded. 
     */
    public IMediaObject ThreegppCleanup(IMediaObject mo1) throws IOException; 
}
