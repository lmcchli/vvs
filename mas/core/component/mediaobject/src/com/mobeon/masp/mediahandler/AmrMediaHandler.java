package com.mobeon.masp.mediahandler;

import java.io.*;
import java.nio.ByteBuffer;

import com.mobeon.masp.execution_engine.platformaccess.EventType;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessException;
import jakarta.activation.MimeType;
import com.mobeon.masp.mediahandler.MediaHandler;
import com.mobeon.masp.mediahandler.AmrMediaHandler;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.IMediaObjectIterator;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaObjectNativeAccess;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.abcxyz.messaging.transcoderfacade.Transcoder;
import com.abcxyz.messaging.transcoder.*;
import com.abcxyz.messaging.transcoder.threegpp.Converter3gpToAmr;
import com.abcxyz.messaging.transcoder.threegpp.ThreeGpDurationCalculator;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;

public class AmrMediaHandler implements MediaHandler {
	
	//Not sure if should keep this in final version
	//but for testing we can disable it.
	private static Boolean ThreegppCleanupDisable=false;
	static {
		try {
		ThreegppCleanupDisable = Boolean.valueOf(System.getProperty("com.mobeon.masp.mediahandler.AmrMediaHandler.ThreegppCleanup.Disable"));
		} catch (Throwable t) {
			//ignore, it will go to false.
		}
	}

    private final static int AMR_HEADER_SIZE = 6;
    private final static int AMR_WB_HEADER_SIZE = 9;
    private final long MAX_SIZE_FOR_CONCATENATE = 4800000; // 4.8MB = 10minutes of audio @ 64kbit/s
    private final IMediaObjectFactory mediaObjectFactory;
    private final MimeType mimeType;
    private static ILogger log = ILoggerFactory.getILogger(AmrMediaHandler.class);

    String callFrom = "AmrMediaHandler";
	private PlatformAccessException p;

    public AmrMediaHandler(IMediaObjectFactory mediaObjectFactory, MimeType mimeType) {
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
     * * Concatenate two media objects containing audio of 3GP-File type. The following limitations apply: - The two WAV-files must
     * have same format. - The WAV must contain one fmt-chunk and one data-chunk. - The data chunk must be a direct sub-chunk of the
     * RIFF chunk and not part of a wavl-chunk. - The fmt-chunk should be placed before the data-chunk for performance reasons. -
     * Chunks other than fmt and data are ignored. - The following compression codes are supported: 6(a-law) and 7(u-law)
     * 
     * @param mo1
     *        First media object.
     * @param mo2
     *        Second media object.
     * @return a new MediaObject containing the concatenation of mo1 and mo2.
     * @throws IOException
     *         - if the media objects could not be concatenated.
     */
    public IMediaObject concatenate(IMediaObject mo1, IMediaObject mo2) throws PlatformAccessException {

        // Limit max size for append. It shouldn't be possible to
        // crash MAS with out of memory just by sending in some large media objects...
        MimeType mt1 = mo1.getMediaProperties().getContentType();
        String codec = mt1.getParameter("codec");
        if (codec == null) { 
        	codec = "samr"; //amr-nb 
        } else {
        	codec=codec.toLowerCase();
        }
    	log.info("Entering in to ArmMediaHandler::concatenate; codec = " + codec);
    	boolean isWB = false;
    	if (codec.contains("sawb")) isWB = true;

        long totalSize = mo1.getSize() + mo2.getSize();
        if (totalSize > MAX_SIZE_FOR_CONCATENATE) {
        	throw new PlatformAccessException("AMR Appending failed:","The total size of the media objects to concatenate (" + totalSize
                    + ") is too big. Max allowed is: " + MAX_SIZE_FOR_CONCATENATE);
        }        

        InputStream is1 = mo1.getInputStream();
        InputStream is2 = mo2.getInputStream();

        try {
            // Convert the 3GP input stream to Amr stream
            byte[] b1 = getBytesFromStream(is1);
            byte[] b2 = getBytesFromStream(is2);  

            byte[] amrPart1=null;
            int amrPart1Length=0;
            if (b1 != null ) {
            	amrPart1 = Converter3gpToAmr.convert3gpToAmr(b1);
            	if (amrPart1==null) {
            		amrPart1Length=0;
            	} else {
            		amrPart1Length=amrPart1.length;
            	}
            }
            byte[] amrPart2=null;
            int amrPart2Length=0;
            if ( b2 !=null ) {
            	amrPart2 = Converter3gpToAmr.convert3gpToAmr(b2);
            	if (amrPart2 == null) {
            		amrPart2Length=0;
            	} else {
            		amrPart2Length=amrPart2.length;
            	}
            }
            int combinedLength=amrPart1Length + amrPart2Length - AMR_HEADER_SIZE;
            if (combinedLength < 0) {
            	log.warn("No media data to concat - throwing ioexception.");
            	throw new PlatformAccessException("AMR Appending failed:","No Data to copy to new mediaObject");
            }
            if(amrPart1Length == 0) {
            	log.error("Media object 1 does not contain valid amrdata, returning media object 2, throwin IOException");
            	throw new PlatformAccessException("AMR Appending failed:","Media Object 1 constains no valid data.");
            }             
            if(amrPart2Length == 0) {
            	log.warn("Media object 2 does not contain valid amrdata, returning media object 1 as nothing to append.");
            	return mo1;
            } 
            
            if (isWB) {
            	amrPart1 = Converter3gpToAmr.convert3gpToWbamr(b1);
            	amrPart2 = Converter3gpToAmr.convert3gpToWbamr(b2);
            } else {
            	amrPart1 = Converter3gpToAmr.convert3gpToAmr(b1);
            	amrPart2 = Converter3gpToAmr.convert3gpToAmr(b2);
            }
            
            byte[] combinedAmr = null;
            if (isWB) {
            	combinedAmr = new byte[amrPart1.length + amrPart2.length - AMR_WB_HEADER_SIZE];
            	concatToNewAMRWB(amrPart1, amrPart2, combinedAmr);
            } else { 
            	combinedAmr = new byte[amrPart1.length + amrPart2.length - AMR_HEADER_SIZE];
            	concatToNewAMR(amrPart1, amrPart2, combinedAmr);
            }
            // At this point, combinedAmr[] does contain the magic header, which is required by getAmrDuration() 
            int duration = ThreeGpDurationCalculator.getAmrDuration(combinedAmr, (LogAgent) log);

            // Transcode amr data to 3gp by adding 3gp header
            // Get the mfs OAMManager, could be any other manager, like ntf etc.
            OAMManager oam = CommonMessagingAccess.getInstance().getOamManager().getMfsOam();
            
            // Call transcoder convert from concatenated AMR stream
            Transcoder theTranscoder = new Transcoder(oam, callFrom); // Convert from amr input stream to 3gp output stream byte
            byte[] outputContents = null;
            if (isWB) {
            	outputContents = theTranscoder.convertByteArray(combinedAmr, "audio/amr-wb", "audio/3gpp;codec=sawb");
            }
            else {
            	outputContents = theTranscoder.convertByteArray(combinedAmr, "audio/amr", "audio/3gpp;codec=samr");
            }
            
            if (outputContents == null) {
            	log.error("AMR Appending failed (ffmpeg transcode failed) - throwing PlatformAccessException");
                throw new PlatformAccessException(
                        EventType.APPENDERROR, "AMR Appending failed: ", "Cannot concatenate media object");
            }
            
            IMediaObject mo3 = mediaObjectFactory.create();
            // Write 3gp data to mo3
            // transByteArrayToIMediaObject(outputContents, mo3);

            MediaObjectNativeAccess destNative = mo3.getNativeAccess();
            ByteBuffer destBuf = destNative.append(outputContents.length);
            destBuf.put(ByteBuffer.wrap(outputContents));

            // If content-type was set on first media object, copy it
            MimeType contentType = mo1.getMediaProperties().getContentType();
            if (contentType != null) {
                mo3.getMediaProperties().setContentType(contentType);
            }

            /**
             * Set duration in milliseconds for resulting media object.
             * This duration might be overwritten at storage time if a duration is provided
             * in com.mobeon.masp.mailbox.MessageContentProperties
             */
            mo3.getMediaProperties().addLengthInUnit(MediaLength.LengthUnit.MILLISECONDS, duration*1000);

            // If file type was set on first media object, copy it
            String fileExt = mo1.getMediaProperties().getFileExtension();
            if (fileExt != null) {
                mo3.getMediaProperties().setFileExtension(fileExt);
            }

            // Set resulting media object to immutable when finished
            mo3.setImmutable();

            return mo3;

        } catch (ConverterException e) {
        	log.error("failed to convert from amr to 3gpp exception (throwing ioException): ",e);
        	 p = new PlatformAccessException(
                     EventType.APPENDERROR, "AMR Appending failed: ", "Cannot concatenate media object");
        	 p.initCause(e);
        	 throw p;
        } catch (IOException e) {
        	log.error("Convert from InputStream to byte Array fails (throwing PlatformAccessException): ",e);
        	 p = new PlatformAccessException(
                     EventType.APPENDERROR, "AMR Appending failed: ", "convert from InputStream to byte Array fails");
        	 p.initCause(e);
        	 throw p;
        }
    }

    /**
     * * Concatenate two media objects containing audio of 3GP-File type. The following limitations apply: - The two amr files must
     * have same format. - 
     * 
     * @param mo1
     *        First media object.
     * @param mo2
     *        Second media object.
     * @param codec
     * 		  the codec of the amr, either samr (nb) or sawb (amrwb)     
     * @return a new MediaObject containing the concatenation of mo1 and mo2.
     * @throws IOException
     *         - if the media objects could not be concatenated.
     */
    public IMediaObject concatenateAmr(IMediaObject mo1, IMediaObject mo2,String codec) throws IOException {

        log.info("Entering in to ArmMediaHandler:concatenateAmr. codec = " + codec);

        long totalSize = mo1.getSize() + mo2.getSize();
        if (totalSize > MAX_SIZE_FOR_CONCATENATE) {
        	log.warn("The total size of the media objects to concatenate is too big, throwing IOexception.");
            throw new IOException("The total size of the media objects to concatenate (" + totalSize
                    + ") is too big. Max allowed is: " + MAX_SIZE_FOR_CONCATENATE);
        }

        InputStream is1 = mo1.getInputStream();
        InputStream is2 = mo2.getInputStream();

        try {
            // Convert the 3GP input stream to Amr stream
            byte[] b1 = getBytesFromStream(is1);
            if (b1 == null || b1.length < 8) {
            	//8 is the length of ftype box, absolute minimum size
            	//it is probably still to small as really we need the mdat etc
            	log.warn("media object 1 is to small, throwing PlatformAccessException, length:" + b1.length);
            	throw new PlatformAccessException(
            			EventType.APPENDERROR, "media object 1 is to small cannot concatenate media object");
            }
            
            byte[] b2 = getBytesFromStream(is2);  
            if (b2 == null || b2.length < 8) {
            	//8 is the length of ftype box, absolute minimum size
            	//it is probably still to small as really we need the mdat etc
            	log.warn("media object2 is to small or empty " + b2.length);
            	if (b1.length >= 8 ) {
            		log.warn("Returning the first media object only");
           		 	return mo1;
            	}
            }
            
            log.debug("concatenateAmr: Converter3gpToAmr.buildAmrLengthMdatArray");
            byte[] combinedAmr;
            try { 
            	combinedAmr = Converter3gpToAmr.buildAmrLengthMdatArray(b1, b2);
            } catch (ConverterException c) {
				log.warn("cannot build buildAmrLengthMdatArray due to exception: ",c);
				throw c;
			}
            
            IMediaObject mo3 = mediaObjectFactory.create();

            // Calculate duration from concatenated media object
            if (combinedAmr.length <= 8) {
            	//if the length is less than or equal to 8 it is just the mdat header.
            	log.warn("No Valid data in either media object, returning media object 1.");
            	return mo1;
            }
            log.debug("concatenateAmr: Calculate duration from concatenated media object");
            //int duration = ThreeGpDurationCalculator.getAmrDuration(combinedAmr, (LogAgent) log);

            // The method ThreeGpDurationCalculator.getAmrDuration() assumes the AMR/AMR-WB data starts with  
            // the magic header which is either !#AMR<LF> or !#AMR-WB<LF>.
            // So we shall insert the appropriate magic header if it's not already there
            byte[] first5Bytes = new byte[5];
            System.arraycopy(combinedAmr, 0, first5Bytes, 0, 5);
            String mayBeHeader = new String(first5Bytes);
            int duration = 0;
            if (mayBeHeader.equalsIgnoreCase("!#AMR")) { // AMR/AMR-WB magic header already in combinedAmr 
            	duration = ThreeGpDurationCalculator.getAmrDuration(combinedAmr, (LogAgent) log);
            } else { // // AMR/AMR-WB magic header not there; need to insert it
            	if (codec.contains("sawb")) {
                    byte[] combinedAmrWithHeaderformat = new byte[AMR_WB_HEADER_SIZE + combinedAmr.length - 8];
                    System.arraycopy(Converter3gpToAmr.getAmrWbMagicHeader(), 0, combinedAmrWithHeaderformat, 0, AMR_WB_HEADER_SIZE);
                    System.arraycopy(combinedAmr, 8, combinedAmrWithHeaderformat, AMR_WB_HEADER_SIZE, combinedAmr.length - 8);
                    duration = ThreeGpDurationCalculator.getAmrDuration(combinedAmrWithHeaderformat, (LogAgent) log);
            	} else {
                    byte[] combinedAmrWithHeaderformat = new byte[AMR_HEADER_SIZE + combinedAmr.length - 8];
                    System.arraycopy(Converter3gpToAmr.getAmrMagicHeader(), 0, combinedAmrWithHeaderformat, 0, AMR_HEADER_SIZE);
                    System.arraycopy(combinedAmr, 8, combinedAmrWithHeaderformat, AMR_HEADER_SIZE, combinedAmr.length - 8);
                    duration = ThreeGpDurationCalculator.getAmrDuration(combinedAmrWithHeaderformat, (LogAgent) log);
            	}
            }

            MediaObjectNativeAccess destNative = mo3.getNativeAccess();
            ByteBuffer destBuf = destNative.append(combinedAmr.length);
            destBuf.put(ByteBuffer.wrap(combinedAmr));

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

            /**
             * Set duration in milliseconds for resulting media object.
             * This duration might be overwritten at storage time if a duration is provided
             * in com.mobeon.masp.mailbox.MessageContentProperties
             */
            mo3.getMediaProperties().addLengthInUnit(MediaLength.LengthUnit.MILLISECONDS, duration*1000);

            // Set resulting media object to immutable when finished
            mo3.setImmutable();

            return mo3;

        } catch (ConverterException e) {
        	log.error("converterException (throwing ioException) ",e);
            throw new IOException("Internal error occured when converting amr format to 3gpp format: " + e.getMessage());
        } catch (IOException e) {
        	log.error("converterException (throwing ioException): ",e); 
            throw new IOException("Convert from InputStream to byte Array fails: " + e.getMessage());
        }

    }

    /* 
     * Function which converts a 3gpp media object to amr and back again to 3gpp
     * It seems to have been made to correct some issues back in the day...
     * Still called from some call flows.
     * To disable add -Dcom.mobeon.masp.mediahandler.AmrMediaHandler.ThreegppCleanupDisable=true in
     * /opt/moip/config/mas/masRuntimeSettings.env
     * export EXTRA_JVM_FLAGS="$EXTRA_JVM_FLAGS -Dcom.mobeon.masp.mediahandler.AmrMediaHandler.ThreegppCleanup.Disable=true""
     * */
    public IMediaObject ThreegppCleanup(IMediaObject mo1) throws IOException,PlatformAccessException {
    	
    	if (ThreegppCleanupDisable == true ) {
    		log.info("ThreegppCleanup() disabled with -Dcom.mobeon.masp.mediahandler.AmrMediaHandler.ThreegppCleanupDisable=true");
    		//just return the original one.
    		return mo1;
    	}

        log.info("Entering into ThreegppCleanup() convert 3gpp to amr and back again (clean up)");
        
        MimeType mt1 = mo1.getMediaProperties().getContentType();
        String codec = mt1.getParameter("codec");
        if (codec == null) { 
        	codec = "samr"; //amr-nb 
        } else {
        	codec=codec.toLowerCase();
        }
    	log.info("Codec for MediaObject: " + codec);
    	boolean isWB = false;
    	if (codec.contains("sawb")) isWB = true;
    	

        long totalSize = mo1.getSize();
        if (totalSize > MAX_SIZE_FOR_CONCATENATE) {
        	log.warn("The total size of the media object is to large.");
            throw new IOException("The total size of the media objects to convert (" + totalSize
                    + ") is too big. Max allowed is: " + MAX_SIZE_FOR_CONCATENATE);
        }

        InputStream is1 = mo1.getInputStream();
        

        try {
            // Convert the 3GP input stream to amr stream
            byte[] b1 = getBytesFromStream(is1);
            byte[] combinedAmr;
            if (isWB) {
            	log.debug("ThreegppCleanup() amr-wb(sawb) Entering into Convert3gpToAmr.convert3gpToWbAmr...");
            	combinedAmr = Converter3gpToAmr.convert3gpToWbamr(b1);
            } else {
            	log.debug("ThreegppCleanup() amr (samr) Entering into Convert3gpToAmr.convert3gpToAmr...");
            	combinedAmr = Converter3gpToAmr.convert3gpToAmr(b1);
            	
			} 
            if ( combinedAmr == null ) {
            	log.warn("ThreegppCleanup() cannot get valid data, unable fo find mdat data.");
            	return null;
            }

            // Calculate duration from concatenated media object (and remove header: 6 bytes)

            int duration = ThreeGpDurationCalculator.getAmrDuration(combinedAmr, (LogAgent) log);

            // Transcode amr data to 3gp by adding 3gp header
            // Get the mfs OAMManager, could be any other manager, like ntf etc.
            OAMManager oam = CommonMessagingAccess.getInstance().getOamManager().getMfsOam();
            
            // Call transcoder convert from concatenated AMR stream
            log.debug("ThreegppCleanup() Entering into Converter3gpToAmr.Call transcoder...");
            Transcoder theTranscoder = new Transcoder(oam, callFrom); // Convert from amr input stream to 3gp output stream byte
            byte[] outputContents;
            if (isWB) {
            	log.debug("ThreegppCleanup() amr-wb: Converting from amr file #!AMR-WB to 3gpp (sawb) using ffmpeg");
            	outputContents = theTranscoder.convertByteArray(combinedAmr, "audio/amr-wb", "audio/3gpp;codec=sawb");
            } else {
            	log.debug("ThreegppCleanup() amr-nb: Converting from amr file #!AMR to 3gpp (samr) using ffmpeg");
            	outputContents = theTranscoder.convertByteArray(combinedAmr, "audio/amr", "audio/3gpp;codec=samr");
            }
            
            if (outputContents == null) {
            	log.warn("ThreegppCleanup() AMR Appending failed: ffmpeg failed to convert from amr to 3gp(throwing platformAccesException).");
                throw new PlatformAccessException(
                        EventType.APPENDERROR, "AMR cleanup failed: ", "Cannot cleanup file");
            }

            IMediaObject mo3 = mediaObjectFactory.create();

            MediaObjectNativeAccess destNative = mo3.getNativeAccess();
            ByteBuffer destBuf = destNative.append(outputContents.length);
            destBuf.put(ByteBuffer.wrap(outputContents));

            // If content-type was set on first media object, copy it
            MimeType contentType = mo1.getMediaProperties().getContentType();
            if (contentType != null) {
                mo3.getMediaProperties().setContentType(contentType);
            }

            /**
             * Set duration in milliseconds for resulting media object.
             * This duration might be overwritten at storage time if a duration is provided
             * in com.mobeon.masp.mailbox.MessageContentProperties
             */
            mo3.getMediaProperties().addLengthInUnit(MediaLength.LengthUnit.MILLISECONDS, duration*1000);

            // If file type was set on first media object, copy it
            String fileExt = mo1.getMediaProperties().getFileExtension();
            if (fileExt != null) {
                mo3.getMediaProperties().setFileExtension(fileExt);
            }

            // Set resulting media object to immutable when finished
            mo3.setImmutable();
            
            log.debug("completed: ThreegppCleanup succesfully");
            return mo3;

        } catch (ConverterException e) {
            throw new IOException("Internal error occured when converting/cleanup amr/amr-wb format to from  3gpp format: " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("Convert from InputStream to byte Array fails: " + e.getMessage());
        }
    }

    
    /**
     * This function is not using right now. But may be used later. An Alternative way to convert an IMediaObject content into byte
     * Array Not using InputStream.
     * 
     * @param mo
     *        IMediaObject.
     * @return a byte array content
     * @throws IOException
     *         - if the IMediaObject can convert into byte array.
     */
    public byte[] transIMediaObjectToByteArray(IMediaObject mo) throws IOException {
        IMediaObjectIterator srcIterator = mo.getNativeAccess().iterator();
        ByteBuffer destBuffer = ByteBuffer.allocateDirect(0);

        while (srcIterator.hasNext()) {
            try {
                ByteBuffer bb = srcIterator.next();
                bb.rewind();

                ByteBuffer tmpBuffer = ByteBuffer.allocate(destBuffer.remaining() + bb.remaining());
                tmpBuffer.put(destBuffer);
                tmpBuffer.put(bb);
                tmpBuffer.rewind();

                destBuffer = ByteBuffer.allocate(tmpBuffer.remaining());
                destBuffer.put(tmpBuffer);

            } catch (MediaObjectException e) {
                throw new IOException("Internal error occured: " + e.getMessage());
            }

        }

        return destBuffer.array();
    }

    /**
     * Change from InputStream to byte array
     * 
     * @param is
     *        InputStream
     * @return a byte array that reads from InputStream
     * @throws IOException exception
     */

    public byte[] getBytesFromStream(InputStream is) throws IOException {
      	ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int count = 0;
        byte[] buffer = new byte[1024];
        while ((count = is.read(buffer)) != -1) {
            baos.write(buffer, 0, count);
        }
        return baos.toByteArray();
    }

    /**
     * Concat two AMR stream to a new Amr Stream
     * 
     * @param s1
     *        the first amr stream
     * @param s2
     *        the second amr stream
     * @param dest
     *        the combined amr stream
     */
    public void concatToNewAMR(byte[] s1, byte[] s2, byte[] dest) throws IOException {
        try {
            System.arraycopy(s1, 0, dest, 0, s1.length);
            System.arraycopy(s2, AMR_HEADER_SIZE, dest, s1.length, s2.length - AMR_HEADER_SIZE);
        } catch (Exception e) {
            IOException i = new IOException("Internal error occured: " + e.getMessage());
            i.initCause(e);
            throw i;
        }
    }
    /**
     * 
     * @param s1
     * @param s2
     * @param dest
     * @throws IOException
     */
    public void concatToNewAMRWB(byte[] s1, byte[] s2, byte[] dest) throws IOException {
        try {
            System.arraycopy(s1, 0, dest, 0, s1.length);
            System.arraycopy(s2, AMR_WB_HEADER_SIZE, dest, s1.length, s2.length - AMR_WB_HEADER_SIZE);
        } catch (Exception e) {
        	IOException i = new IOException("Internal error occured: " + e.getMessage());
            i.initCause(e);
            throw i;
        }
    }
}
