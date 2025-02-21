package com.mobeon.masp.mediahandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.mobeon.masp.mediahandler.WavMediaFile;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.IMediaObjectIterator;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaObjectNativeAccess;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;


public class WavMediaFile {
    private static ILogger log = ILoggerFactory.getILogger(WavMediaFile.class);

	IMediaObjectFactory mediaObjectFactory = null;
	RiffChunk riff = null;
	FmtChunk fmt = null;
	DataChunk data = null;
	IMediaObject mo = null;
	
	long startOfSampleData = 0;
	long lengthOfSampleData = 0;

	public WavMediaFile(IMediaObjectFactory mediaObjectFactory) {
		this.mediaObjectFactory = mediaObjectFactory;
	}
	
	
	
	/**
	 * Check if the wav file type is supported. Currently the following requirements apply:
	 *  - Compression code must be either 6 or 7 (alaw or ulaw).
	 *  - The number of channels must be exactly 1.
	 *  - The number of significant bits per sample must be 8.
	 *  - The number of bytes per sample slice must be 1.
	 * @return true if the wav is supported, false otherwise.
	 */
	public boolean typeIsSupported() {
		
		if (fmt.compressionCode != 6 && fmt.compressionCode != 7) {
			if(log.isInfoEnabled())
				log.info("The wav compression code " + fmt.compressionCode + " is not supported. Currently supported are: alaw(=6) and ulaw(=7)");
			return false;
		}

		if (fmt.numberOfChannels != 1) {
			if(log.isInfoEnabled())
				log.info("Not supported: Number of channels must be 1.");
			return false;
		}
			
		if (fmt.blockAlign != 1 || fmt.significantBitsPerSample != 8) {
			if(log.isInfoEnabled())
				log.info("Not supported: significantBitsPerSample must be 8 and blockAlign must be 1.");
			return false;
		}

		return true;
	}

	
	/**
	 * Compare the type of this wav file with the given wav-file.
	 *
	 * @param wavFile WavMediaFile to compare with.
	 * @return true if the given wavFile has the same format as 
	 * this wavFile or false otherwise. 
	 */
	public boolean typeEquals(WavMediaFile wavFile) {
		if (	fmt != null && wavFile != null && wavFile.fmt != null &&
				fmt.compressionCode == wavFile.fmt.compressionCode &&
				fmt.numberOfChannels == wavFile.fmt.numberOfChannels &&
				fmt.sampleRate == wavFile.fmt.sampleRate &&
				fmt.avgBytesPerSecond == wavFile.fmt.avgBytesPerSecond &&
				fmt.blockAlign == wavFile.fmt.blockAlign &&
				fmt.significantBitsPerSample == wavFile.fmt.significantBitsPerSample) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get the number of bytes of sample data for this WAV-File.
	 * 
	 * @return number of bytes of sample data.
	 */
	public long getSampleLength() {
		return lengthOfSampleData;
	}

	/**
	 * Get the average number of bytes per second for this WAV-file.
	 * 
	 * @return averageBytesPerSecond
	 */
	public long getAverageBytesPerSecond() {
		return fmt.avgBytesPerSecond;
	}
	
	/**
	 * Try to parse a media object as a WAV-file.
	 * 
	 * @param mo IMediaObject to parse.
	 * @throws IOException If the media object could not be parsed.
	 */
	public void parseMediaObject(IMediaObject mo) throws IOException {

		this.mo = mo;
		InputStream is = mo.getInputStream();

		Chunk chunk = parseChunk(is, false);
		if (chunk instanceof RiffChunk) {
			riff = (RiffChunk)chunk;
		} else {
			throw new IOException("MediaObject does not contain a WAV file (RIFF chunk not found)");
		}

		if (!"WAVE".equals(riff.riffType)) {
			throw new IOException("MediaObject does not contain a WAV file (RIFF is not of WAVE type)");
		}


		// Start right after RIFF chunk header (always 12 bytes long)
		long currentChunkPos = 12;

		// Keep searching for chunks until end of stream or until 
		//  both fmt and data chunk have been found
		while (is.available()>0 && (fmt==null || data==null)) {
				
			// If the FMT chunk have already been found we
			// do not need to step through the dataChunk
			chunk = parseChunk(is, fmt == null);

			if (chunk instanceof FmtChunk) {
				fmt = (FmtChunk)chunk;
				if(log.isDebugEnabled())
					log.debug("Found " + fmt + " at position " + currentChunkPos);
				currentChunkPos += chunk.totalChunkSize;

			} else if (chunk instanceof DataChunk) {
				startOfSampleData = currentChunkPos+8;
				lengthOfSampleData = chunk.chunkDataSize;
				data = (DataChunk)chunk;
				if(log.isDebugEnabled())
					log.debug("Found " + data + " at position " + currentChunkPos);
				currentChunkPos += chunk.totalChunkSize;
				
			} else {
				if(log.isDebugEnabled())
					log.debug("Found " + chunk + " at position " + currentChunkPos);
				currentChunkPos += chunk.totalChunkSize;
				
			}


		}

		// Do we have what we need?
		if (fmt == null)
			throw new IOException("Cannot parse media object as wav file. Mandatory fmt chunk not found");
		if (data == null) 
			throw new IOException("Cannot parse media object as wav file. Mandatory data chunk not found");			
		
	}
	
	/**
	 * Append sample data from this media object to the given media object.
	 * 
	 * @param dest the media object to append to. This must be a mutable media object
	 * @param paddingNeeded Set to true if append should add an extra byte at end
	 * to make sure the resulting WAV-file have an even number of bytes.
	 * @throws IOException if the append fails for some reason.
	 */
	public void appendSampleDataTo(IMediaObject dest, boolean paddingNeeded) 
		throws IOException {
		
		// Skip header
		long bytesRead = 0;
		IMediaObjectIterator srcIterator = mo.getNativeAccess().iterator();
		MediaObjectNativeAccess destNative = dest.getNativeAccess();
		
		while (srcIterator.hasNext()) {
			try {
				ByteBuffer bb = srcIterator.next();
				bb.rewind();
				if (startOfSampleData >= bytesRead + bb.remaining()) {
					if (log.isDebugEnabled())
						log.debug("Skipping " + bb.remaining() + " bytes in buffer with header data only");
					// Skip over packets containing only header data
					// This would only happen if the used byte buffers are
					// extremely small...
					bytesRead += bb.remaining();
					continue;
				} 
				int pos = (int)(startOfSampleData-bytesRead);
				if (pos>0) {
					// Set position to where sample data begins within byte buffer
					if (log.isDebugEnabled())
						log.debug("Skipping " + pos + " bytes of header data in buffer");

					bb.position(pos);
					bytesRead += pos;
				}
				
				int totalRemaining = (int)(lengthOfSampleData+startOfSampleData-bytesRead);
				if (totalRemaining <= bb.remaining()) {
					bb.limit(bb.position() + totalRemaining);
					if (paddingNeeded) {
						if (log.isDebugEnabled())
							log.debug("Appending " + (totalRemaining+1) + 
									" bytes (including 1 padding byte)");
						ByteBuffer destBuf = destNative.append(totalRemaining+1);
						destBuf.put(bb);
						destBuf.put((byte)0x00);
					} else {
						if (log.isDebugEnabled())
							log.debug("Appending " + totalRemaining + " bytes");
						ByteBuffer destBuf = destNative.append(totalRemaining);
						destBuf.put(bb);
					}
					if (log.isDebugEnabled())
						log.debug("Append of one media object finished!");
					break;
					
				} else {
					if (log.isDebugEnabled())
						log.debug("Appending " + bb.remaining() + " bytes");
					bytesRead += bb.remaining();
					ByteBuffer destBuf = destNative.append(bb.remaining());
					destBuf.put(bb);
				}

				
			} catch (MediaObjectException e) {
				// Cannot happen (Ever heard that before? ;-)
				log.warn("Internal error occured. ", e);
				throw new IOException("Internal error occured: " + e.getMessage());
			}
		}
		
	}
	
	/**
	 * Append a wav file header corresponding to the type of this WavMediaFile.
	 * The appended header will have the same format properties as this WavMediaFile but 
	 * with the chunk sizes adjusted to match the given sampleDataSize.
	 * @param destMo the media object to append to.
	 * @param sampleDataSize the total size in bytes of the sample data that will later be 
	 * appended to the destination media object.
	 * @throws IOException is thrown if a header could not be appended.
	 */
	public void appendWavHeader(IMediaObject destMo, long sampleDataSize) 
		throws IOException {
		
		final int RIFF_SIZE = 12;
		final int FMT_SIZE = 26;
		final int DATAHDR_SIZE = 8;
		final int TOTALHDR_SIZE = RIFF_SIZE + FMT_SIZE + DATAHDR_SIZE;
		long dataChunkSize = sampleDataSize;

		long riffChunkSize;
		if ((sampleDataSize&1) == 1)
			riffChunkSize = TOTALHDR_SIZE - 8 + sampleDataSize+1;
		else 
			riffChunkSize = TOTALHDR_SIZE - 8 + sampleDataSize;
		
		
		try {
			ByteBuffer header = destMo.getNativeAccess().append(TOTALHDR_SIZE);
			
			// Put RIFF Header
			putChunkId("RIFF", header);
			putUIntLE(riffChunkSize,header);
			putChunkId("WAVE", header);
			
			// Put FMT Header
			putChunkId("fmt ", header);
			putUIntLE(FMT_SIZE-8, header);
			putUShortLE(fmt.compressionCode, header);
			putUShortLE(fmt.numberOfChannels, header);
			putUIntLE(fmt.sampleRate, header);
			putUIntLE(fmt.avgBytesPerSecond, header);
			putUShortLE(fmt.blockAlign, header);
			putUShortLE(fmt.significantBitsPerSample, header);
			putUShortLE(0, header); 	// Zero extra format bytes

			// Put DATA Header
			putChunkId("data", header);
			putUIntLE(dataChunkSize, header);
			
		} catch (Exception e) {
			throw new IOException("Failed to append WAV header to media object, " +
					"make sure the destination media object is mutable. " + 
					e.getMessage());
		}
		
	}
	
    
    private Chunk parseChunk(InputStream is, boolean skipToEndofDataChunk) throws IOException {

    	Chunk chunk;
    	
    	String chunkId = getChunkId(is);
    	long chunkDataSize = getUIntLE(is);
    	long totalChunkSize = chunkDataSize + 8;
    	
    	if ("RIFF".equals(chunkId)) {
    		RiffChunk riff = new RiffChunk();
    		riff.riffType = getChunkId(is);
    		chunk = riff;
    		
    	} else if ("fmt ".equals(chunkId)) {
    		FmtChunk fmt = new FmtChunk();
    		fmt.compressionCode = getUShortLE(is);
    		fmt.numberOfChannels = getUShortLE(is);
        	fmt.sampleRate = getUIntLE(is);
        	fmt.avgBytesPerSecond = getUIntLE(is);
        	fmt.blockAlign = getUShortLE(is);
        	fmt.significantBitsPerSample = getUShortLE(is);

    		long skipBytes = chunkDataSize+8-24;
    		skipBytes(is,skipBytes);
        	chunk = fmt;
        	
    	} else if ("data".equals(chunkId)) {

    		// Data Chunk
    		chunk = new DataChunk();
        	if (skipToEndofDataChunk) {
        		skipBytes(is,chunkDataSize);
        	}
    		
    	} else {
    		// Unknown RIFF chunk id, ignoring
    		chunk = new Chunk();
    		skipBytes(is, chunkDataSize);

    	}
    	
    	chunk.chunkId = chunkId;
    	chunk.chunkDataSize = chunkDataSize;
    	chunk.totalChunkSize = totalChunkSize;
    	
    	return chunk;
    }
    
    private void skipBytes(InputStream is, long skipBytes) throws IOException {
    	if (skipBytes > 0) {
    		if ((skipBytes & 1) == 1) {
    			// Skip one extra byte if skipBytes is odd
    			// since all RIFF chunks must be word aligned
    			skipBytes++;	
    		}
    		is.skip(skipBytes);
    	}
    }
    
    private long getUIntLE(InputStream is) throws IOException {
    	int b1 = is.read();
    	int b2 = is.read();
    	int b3 = is.read();
    	int b4 = is.read();
    	if (b1<0 || b2<0 || b3<0 || b4<0) {
    		throw new IOException("getUIntLE: Error, EOF reached");
    	}
    	return b1 | b2<<8 | b3<<16 | b4<<24;
    }
    
    private int getUShortLE(InputStream is) throws IOException {
    	int b1 = is.read();
    	int b2 = is.read();
    	if (b1<0 || b2<0) {
    		throw new IOException("getUShortLE: Error, EOF reached");
    	}
    	return b1 | b2<<8;
    }

    private String getChunkId(InputStream is) throws IOException {
    	char[] chunkId = new char[4];
    	int b1 = is.read();
    	int b2 = is.read();
    	int b3 = is.read();
    	int b4 = is.read();
    	if (b1<0 || b2<0 || b3<0 || b4<0) {
    		throw new IOException("getChunkId: Error, EOF reached");
    	}
    	chunkId[0]=(char)(b1);
    	chunkId[1]=(char)(b2);
    	chunkId[2]=(char)(b3);
    	chunkId[3]=(char)(b4);
    	return new String(chunkId);
    }

    private void putChunkId(String chunkId, ByteBuffer dest) throws IOException {
    	if (chunkId.length() != 4)
    		throw new IOException("putChunkId: ChunkId must be exactly four characters long");

    	dest.put((byte)chunkId.charAt(0));
    	dest.put((byte)chunkId.charAt(1));
    	dest.put((byte)chunkId.charAt(2));
    	dest.put((byte)chunkId.charAt(3));
    }
    
    private void putUIntLE(long value, ByteBuffer dest) throws IOException {
    	if (value<0 || value>0xffffffffl)
    		throw new IOException("putUIntLE: Value out of bounds " + value);
    	
    	dest.put((byte)(value & 0xff));
    	dest.put((byte)((value>>>8) & 0xff));
    	dest.put((byte)((value>>>16) & 0xff));
    	dest.put((byte)((value>>>24) & 0xff));
    }

    private void putUShortLE(int value, ByteBuffer dest) throws IOException {
    	if (value<0 || value>0xffff)
    		throw new IOException("putUShortLE: Value out of bounds" + value);
    	
    	dest.put((byte)(value & 0xff));
    	dest.put((byte)((value>>>8) & 0xff));
    }

	private class Chunk {
		long totalChunkSize;
		String chunkId;
		long chunkDataSize;
		
		public String toString() {
			return "chunkId=" + chunkId + ", chunkDataSize=" + chunkDataSize;
		}
	}

	private class RiffChunk extends Chunk {
		String riffType;
		
		public String toString() {
			return "RiffChunk{" + super.toString() 
			+ ", riffType=" + riffType + "}";
		}
	}

	private class FmtChunk extends Chunk {
    	int compressionCode;
    	int numberOfChannels;
    	long sampleRate;
    	long avgBytesPerSecond;
    	int blockAlign;
    	int significantBitsPerSample;
    	
    	public String toString() {
    		return "FmtChunk{" + super.toString() 
    		+ ", compressionCode=" + compressionCode
    		+ ", numberOfChannels=" + numberOfChannels
    		+ ", sampleRate=" + sampleRate
    		+ ", averageBytesPerSecond=" + avgBytesPerSecond
    		+ ", blockAlign=" + blockAlign
    		+ ", significantBitsPerSample=" + significantBitsPerSample
    		+ "}";
    	}
	}
	
	private class DataChunk extends Chunk {
		public String toString() {
			return "DataChunk{" + super.toString() + "}";
		}
	}


}

