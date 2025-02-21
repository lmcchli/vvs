package com.mobeon.masp.mediahandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.mobeon.masp.mediahandler.WavMediaHandler;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaLength.LengthUnit;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;

import junit.framework.*;

public class WavMediaHandlerTest extends TestCase {

	private WavMediaHandler wavMediaHandler;
	private IMediaObjectFactory mediaObjectFactory;

	final int PCM = 1;
	final int ALAW = 6;
	final int ULAW = 7;
	final int STANDARD_SAMPLERATE = 8000;
	final int OTHER_SAMPLERATE = 22000;
	final int AVGBPS = 8000;
	final int BLOCKALIGN = 1;
	final int BITSPERSAMPLE = 8;
	
	// These are the expected sizes of each chunk header 
	final int RIFF_HDR_SIZE = 12;
	final int FMT_HDR_SIZE = 26;
	final int DATA_HDR_SIZE = 8;
	
	public void setUp() throws MimeTypeParseException {
		Logger rootLog = Logger.getRootLogger();
		rootLog.setPriority(Priority.INFO);
		
		mediaObjectFactory = new MediaObjectFactory();
		wavMediaHandler = new WavMediaHandler(mediaObjectFactory,
				new MimeType("audio/wav"));
	}
	

	/////////////////////////////////////////////////////////////
	//// TEST CASES
	/////////////////////////////////////////////////////////////

	/**
	 * Positive tests of concatenate with combinations of different WAV-files.
	 */
	public void testConcatenate_Positive() throws Exception {

		// First list of different sample data
		List<byte[]> mo1List = new LinkedList<byte[]>();
		mo1List.add(new byte[]{});
		mo1List.add(new byte[]{0x11});
		mo1List.add(new byte[]{0x11,0x12});
		mo1List.add(new byte[]{0x11,0x12,0x13});
		mo1List.add(new byte[]{0x11,0x12,0x13,0x14});
		mo1List.add(new byte[]{0x11,0x12,0x13,0x14,0x15});
		mo1List.add(new byte[]{0x11,0x12,0x13,0x14,0x16});

		// Second list of different sample data
		List<byte[]> mo2List = new LinkedList<byte[]>();
		mo2List.add(new byte[]{});
		mo2List.add(new byte[]{0x21});
		mo2List.add(new byte[]{0x21,0x22});
		mo2List.add(new byte[]{0x21,0x22,0x23});
		mo2List.add(new byte[]{0x21,0x22,0x23,0x24});
		mo2List.add(new byte[]{0x21,0x22,0x23,0x24,0x25});
		mo2List.add(new byte[]{0x21,0x22,0x23,0x24,0x26});

		// List of supported compression codes 
		List<Integer> compressionCodes = new LinkedList<Integer>();
		compressionCodes.add(ALAW);
		compressionCodes.add(ULAW);

		// List of different buffer sizes to use
		List<Integer> bufferSizes = new LinkedList<Integer>();
		bufferSizes.add(1);
		bufferSizes.add(2);
		bufferSizes.add(3);
		bufferSizes.add(10);
		bufferSizes.add(100);
		bufferSizes.add(1000);
		bufferSizes.add(8000);

		
		// Run a test for every permutation of different input values above
		int counter = 0;
		for (int compressionCode : compressionCodes) {
			for (int bufferSize : bufferSizes) {
				for (byte[] s1 : mo1List) {
					for (byte[] s2 : mo2List) {
						counter++;
						System.out.println("Positive test #" + counter + 
								" of concatenate. compressionCode=" +
								compressionCode + " bufSize=" + bufferSize +
								" samples1Length=" + s1.length +
								" samples2Length=" + s2.length);
						byte[] concatSamples = concat(s1,s2);
						IMediaObject mo1 = createWavMo(s1, compressionCode, bufferSize);
						IMediaObject mo2 = createWavMo(s2, compressionCode, bufferSize);
						IMediaObject mo3 = wavMediaHandler.concatenate(mo1, mo2);
						validateWavMo(mo3, compressionCode, concatSamples, false);
						
					}
				}
			}
		}
		
	}

	/**
	 * Negative tests of concatenate. All concatenations are expected 
	 * to throw an IOException.
	 * @throws Exception
	 */
	public void testConcatenate_Negative() throws Exception {
		byte[] samples1 = 
		{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0x0d,0x0e,0x0f,
		0x11,0x12,0x13,0x14,0x15,0x16,0x17,0x18,0x19,0x1a,0x1b,0x1c,0x1d,0x1e,0x1f};

		byte[] samples2 = 
		{0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4a,0x4b,0x4c,0x4d,0x4e,0x4f,
		0x51,0x52,0x53,0x54,0x55,0x56,0x57,0x58,0x59,0x5a,0x5b,0x5c,0x5d,0x5e,0x5f};
		
		byte[] data1 = createDataChunk(samples1);
		byte[] data2 = createDataChunk(samples2);
		byte[] fmtOk1 = createFmtChunk(ULAW, 1, STANDARD_SAMPLERATE, 
				AVGBPS, BLOCKALIGN, BITSPERSAMPLE, 0);
		byte[] fmtOk2 = createFmtChunk(ALAW, 1, STANDARD_SAMPLERATE, 
				AVGBPS, BLOCKALIGN, BITSPERSAMPLE, 0);
		byte[] fmtOk3 = createFmtChunk(ULAW, 1, 16000, 
				16000, BLOCKALIGN, BITSPERSAMPLE, 0);
		byte[] fmtErr1 = createFmtChunk(ULAW, 2, STANDARD_SAMPLERATE, 
				AVGBPS, BLOCKALIGN, BITSPERSAMPLE, 0);
		byte[] fmtErr2 = createFmtChunk(ULAW, 1, STANDARD_SAMPLERATE, 
				AVGBPS, 2, BITSPERSAMPLE, 0);
		byte[] fmtErr3 = createFmtChunk(ULAW, 1, STANDARD_SAMPLERATE, 
				AVGBPS, BLOCKALIGN, 16, 0);
		
		LinkedList<InputStream> seq;
		SequenceInputStream seqIS;
		IMediaObject mo1;
		IMediaObject mo2;
		
		
		// Two valid media object that differ in type => IOException
		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtOk1.length+data1.length)));
		seq.add(new ByteArrayInputStream(fmtOk1));
		seq.add(new ByteArrayInputStream(data1));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo1 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtOk2.length+data2.length)));
		seq.add(new ByteArrayInputStream(fmtOk2));
		seq.add(new ByteArrayInputStream(data2));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo2 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		try {
			wavMediaHandler.concatenate(mo1, mo2);
			fail("Error: Expected an IOException here!");
		} catch (IOException e) {}


		// Two valid media object that differ in type => IOException
		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtOk3.length+data1.length)));
		seq.add(new ByteArrayInputStream(fmtOk3));
		seq.add(new ByteArrayInputStream(data1));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo1 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtOk2.length+data2.length)));
		seq.add(new ByteArrayInputStream(fmtOk2));
		seq.add(new ByteArrayInputStream(data2));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo2 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		try {
			wavMediaHandler.concatenate(mo1, mo2);
			fail("Error: Expected an IOException here!");
		} catch (IOException e) {}

		// Two valid media object that differ in type => IOException
		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtOk3.length+data1.length)));
		seq.add(new ByteArrayInputStream(fmtOk3));
		seq.add(new ByteArrayInputStream(data1));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo1 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtOk1.length+data2.length)));
		seq.add(new ByteArrayInputStream(fmtOk1));
		seq.add(new ByteArrayInputStream(data2));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo2 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		try {
			wavMediaHandler.concatenate(mo1, mo2);
			fail("Error: Expected an IOException here!");
		} catch (IOException e) {}

		
		// Two equal media object that are invalid => IOException
		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtErr1.length+data1.length)));
		seq.add(new ByteArrayInputStream(fmtErr1));
		seq.add(new ByteArrayInputStream(data1));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo1 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtErr1.length+data2.length)));
		seq.add(new ByteArrayInputStream(fmtErr1));
		seq.add(new ByteArrayInputStream(data2));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo2 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		try {
			wavMediaHandler.concatenate(mo1, mo2);
			fail("Error: Expected an IOException here!");
		} catch (IOException e) {}

		// Two equal media object that are invalid => IOException
		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtErr2.length+data1.length)));
		seq.add(new ByteArrayInputStream(fmtErr2));
		seq.add(new ByteArrayInputStream(data1));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo1 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtErr2.length+data2.length)));
		seq.add(new ByteArrayInputStream(fmtErr2));
		seq.add(new ByteArrayInputStream(data2));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo2 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		try {
			wavMediaHandler.concatenate(mo1, mo2);
			fail("Error: Expected an IOException here!");
		} catch (IOException e) {}

		// Two equal media object that are invalid => IOException
		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtErr3.length+data1.length)));
		seq.add(new ByteArrayInputStream(fmtErr3));
		seq.add(new ByteArrayInputStream(data1));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo1 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmtErr3.length+data2.length)));
		seq.add(new ByteArrayInputStream(fmtErr3));
		seq.add(new ByteArrayInputStream(data2));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo2 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		try {
			wavMediaHandler.concatenate(mo1, mo2);
			fail("Error: Expected an IOException here!");
		} catch (IOException e) {}

	}
	
	/**
	 * Test concatenate of two large WAV-files
	 * (Note that the WavMediaHandler currently have a max limit on size)
	 * @throws Exception
	 */
	public void testConcatenate_LargeFiles() throws Exception {

		int buf1Size = 3000001;
		int buf2Size = 1500000;
		byte[] s1 = new byte[buf1Size];
		byte[] s2 = new byte[buf2Size];
		for (int i=0; i<buf1Size; i++)
			s1[i]=(byte)(i&0xff);
		for (int i=0; i<buf2Size; i++)
			s2[i]=(byte)(i&0xff);
		
		byte[] concatSamples = concat(s1,s2);
		IMediaObject mo1 = createWavMo(s1, ULAW, 8000);
		IMediaObject mo2 = createWavMo(s2, ULAW, 8000);
		IMediaObject mo3 = wavMediaHandler.concatenate(mo1, mo2);
		validateWavMo(mo3, ULAW, concatSamples, true);
	}

	/**
	 * Test some special cases that are unusual but should work anyway.
	 *
	 * @throws Exception
	 */
	public void testConcatenate_SpecialCases() throws Exception {
		byte[] samples1 = 
		{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0x0d,0x0e,0x0f,
		0x11,0x12,0x13,0x14,0x15,0x16,0x17,0x18,0x19,0x1a,0x1b,0x1c,0x1d,0x1e,0x1f};

		byte[] samples2 = 
		{0x41,0x42,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4a,0x4b,0x4c,0x4d,0x4e,0x4f,
		0x51,0x52,0x53,0x54,0x55,0x56,0x57,0x58,0x59,0x5a,0x5b,0x5c,0x5d,0x5e,0x5f};
		
		byte[] data1 = createDataChunk(samples1);
		byte[] data2 = createDataChunk(samples2);
		byte[] fmt = createFmtChunk(ULAW, 1, STANDARD_SAMPLERATE, 
				AVGBPS, BLOCKALIGN, BITSPERSAMPLE, 0);
		byte[] other1 = createOtherChunk(30);
		byte[] other2 = createOtherChunk(2000);
		byte[] other3 = createOtherChunk(156014);
		
		LinkedList<InputStream> seq;
		SequenceInputStream seqIS;
		IMediaObject mo1;
		IMediaObject mo2;
		IMediaObject mo3;
		byte[] concatSamples;
		
		// Test reversing fmt and data chunks
		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmt.length+data1.length)));
		seq.add(new ByteArrayInputStream(data1));
		seq.add(new ByteArrayInputStream(fmt));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo1 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmt.length+data2.length)));
		seq.add(new ByteArrayInputStream(data2));
		seq.add(new ByteArrayInputStream(fmt));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo2 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		concatSamples = concat(samples1,samples2);
		mo3 = wavMediaHandler.concatenate(mo1, mo2);
		validateWavMo(mo3, ULAW, concatSamples, false);

		
		// Try inserting a few unknown chunks here and there
		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmt.length+data1.length+
				other1.length+other2.length+other3.length)));
		seq.add(new ByteArrayInputStream(other1));
		seq.add(new ByteArrayInputStream(data1));
		seq.add(new ByteArrayInputStream(other2));
		seq.add(new ByteArrayInputStream(fmt));
		seq.add(new ByteArrayInputStream(other3));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo1 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(createRiffHeader(4+fmt.length+data1.length+
				other1.length+other2.length+other3.length)));
		seq.add(new ByteArrayInputStream(other3));
		seq.add(new ByteArrayInputStream(fmt));
		seq.add(new ByteArrayInputStream(other1));
		seq.add(new ByteArrayInputStream(data2));
		seq.add(new ByteArrayInputStream(other2));
		seqIS = new SequenceInputStream(Collections.enumeration(seq));
		mo2 = mediaObjectFactory.create(seqIS, 1000, new MimeType("audio/wav"));

		concatSamples = concat(samples1,samples2);
		mo3 = wavMediaHandler.concatenate(mo1, mo2);
		validateWavMo(mo3, ULAW, concatSamples, false);
		
	}

	/**
	 * Test concatenating real MAS WAV-files.
	 * Only tests that no exception occurs. Does not actually compare
	 * the media. Uncomment the play line to listen on the concatenated result
	 * (on windows only?!)
	 *  
	 * @throws Exception
	 */
	public void testConcatenate_RealExamples() throws Exception {
		
		File file1 = new File("test//com//mobeon//masp//mediahandler//unknown.wav");
		File file2 = new File("test//com//mobeon//masp//mediahandler//welcome.wav");

		IMediaObject mo1 = mediaObjectFactory.create(file1);
		mo1.getMediaProperties().setContentType(new MimeType("audio/wav"));
		IMediaObject mo2 = mediaObjectFactory.create(file2);
		mo2.getMediaProperties().setContentType(new MimeType("audio/wav"));
		
		IMediaObject mo3;
		mo3 = wavMediaHandler.concatenate(mo1, mo2);

		assertEquals("audio/wav", mo3.getMediaProperties().getContentType().getBaseType());
    	
    	// Total length of  welcome.wav + unknown.wav should be approx. 3338ms
    	long millis = mo3.getMediaProperties().getLengthInUnit(LengthUnit.MILLISECONDS);
    	assertTrue("Length of appended media objects is wrong: " + millis, millis>3300 && millis <3400);

//		play(mo3);
	}
	


	
	/**
	 * Test performance by concatenating a 2:30 minute WAV-file 
	 * with a 0:30 minute WAV-file. Will print out the number of milliseconds
	 * that one concatenate takes, calculated as an average of 1000 repetitions.
	 * Note that this only gives a hint of the performance. Results may vary
	 * depending on hardware used and current load on the host.
	 * 
	 * @throws Exception
	 */
	public void testConcatenate_Performance() throws Exception {

		File file1 = new File("test//com//mobeon//masp//mediahandler//ulaw1_150s.wav");
		File file2 = new File("test//com//mobeon//masp//mediahandler//ulaw2_30s.wav");

		IMediaObject mo1 = mediaObjectFactory.create(file1);
		mo1.getMediaProperties().setContentType(new MimeType("audio/wav"));
		IMediaObject mo2 = mediaObjectFactory.create(file2);
		mo2.getMediaProperties().setContentType(new MimeType("audio/wav"));
		System.out.println(mo1.getMediaProperties());
		System.out.println(mo2.getMediaProperties());
		
		IMediaObject mo3 = null;
		
		// Test performance by running 1000 concatenations while measuring time
    	long start = System.nanoTime();
    	final long ITER = 1000;
    	for (int k=0;k<ITER;k++) {
    		 mo3 = wavMediaHandler.concatenate(mo1, mo2);
    	}
    	long delta = System.nanoTime()-start;
    	System.out.println("Average time for concatenate on this computer[ms]: " + 
    			(double)delta/((double)ITER*1000000.0));

    	assertEquals("audio/wav", mo3.getMediaProperties().getContentType().getBaseType());
    	
    	// Total length of  welcome.wav + unknown.wav should be approx. 180s
    	long millis = mo3.getMediaProperties().getLengthInUnit(LengthUnit.MILLISECONDS);
    	assertTrue("Length of appended media objects is wrong: " + millis, millis>179000 && millis <181000);

//		play(mo3);

	}
	

	
	/////////////////////////////////////////////////////////////
	//// HELPER FUNCTIONS
	/////////////////////////////////////////////////////////////
	
	private byte[] slice(byte[] input, int start, int end) {
		if (start<0 || start>end || end>input.length)
			throw new IndexOutOfBoundsException("Invalid index");
		
		byte[] b = new byte[end-start];
		for (int i=start; i<end; i++)
			b[i-start]=input[i];
		
		return b;
	}
	
	private byte[] concat(byte[] a, byte[] b) {
		byte[] concat = new byte[a.length + b.length];
		System.arraycopy(a, 0, concat, 0, a.length);
		System.arraycopy(b, 0, concat, a.length, b.length);
		return concat;
	}
	
    private String byte2hex(byte b) {
    	final char[] hdigits = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    	char[] hex = {hdigits[(b & 0xf0) >>> 4], hdigits[b & 0xf]};
    	return new String(hex);
    }

    private String bytes2hex(byte[] bytes) {
    	StringBuffer sb = new StringBuffer();
    	for (byte b : bytes) {
    		sb.append(" 0x");
    		sb.append(byte2hex(b));
    	}
    	return sb.toString();
    }
    
	private byte[] createRiffHeader(long chunkSize) {
		byte[] b = {0x52,0x49,0x46,0x46,0x00,0x00,0x00,0x00,0x57,0x41,0x56,0x45};
    	b[4] = ((byte)(chunkSize & 0xff));
    	b[5] = ((byte)((chunkSize>>>8) & 0xff));
    	b[6] = ((byte)((chunkSize>>>16) & 0xff));
    	b[7] = ((byte)((chunkSize>>>24) & 0xff));
		return b;
	}
	
	private byte[] createFmtChunk(int compressionCode,
			int numberOfChannels, long sampleRate,
			long avgBytesPerSecond, int blockAlign, int significantBitsPerSample,
			int extraFormatBytes) {
		
		int chunkDataSize;
		if ((extraFormatBytes & 1) == 1)
			chunkDataSize = 18+extraFormatBytes+1;
		else 
			chunkDataSize = 18+extraFormatBytes;
		
		byte[] b = new byte[8+chunkDataSize];
		
		b[0]=0x66; b[1]=0x6D; b[2]=0x74; b[3]=0x20;	// "fmt "
		
    	b[4] = ((byte)(chunkDataSize & 0xff));
    	b[5] = ((byte)((chunkDataSize>>>8) & 0xff));
    	b[6] = ((byte)((chunkDataSize>>>16) & 0xff));
    	b[7] = ((byte)((chunkDataSize>>>24) & 0xff));

		b[8] = ((byte)(compressionCode & 0xff));
    	b[9] = ((byte)((compressionCode>>>8) & 0xff));
    	
    	b[10] = ((byte)(numberOfChannels & 0xff));
    	b[11] = ((byte)((numberOfChannels>>>8) & 0xff));
    
    	b[12] = ((byte)(sampleRate & 0xff));
    	b[13] = ((byte)((sampleRate>>>8) & 0xff));
    	b[14] = ((byte)((sampleRate>>>16) & 0xff));
    	b[15] = ((byte)((sampleRate>>>24) & 0xff));

    	b[16] = ((byte)(avgBytesPerSecond & 0xff));
    	b[17] = ((byte)((avgBytesPerSecond>>>8) & 0xff));
    	b[18] = ((byte)((avgBytesPerSecond>>>16) & 0xff));
    	b[19] = ((byte)((avgBytesPerSecond>>>24) & 0xff));

    	b[20] = ((byte)(blockAlign & 0xff));
    	b[21] = ((byte)((blockAlign>>>8) & 0xff));

    	b[22] = ((byte)(significantBitsPerSample & 0xff));
    	b[23] = ((byte)((significantBitsPerSample>>>8) & 0xff));

    	b[24] = ((byte)(extraFormatBytes & 0xff));
    	b[25] = ((byte)((extraFormatBytes>>>8) & 0xff));

    	for (int i=26; i<(chunkDataSize+8); i++)
    		b[i]=0x00;
    	
    	return b;
		
	}

	private byte[] createDataChunk(byte[] data) {
		
		boolean padding = false;
		if ((data.length&1)==1)
			padding = true;
		
		byte[] b = new byte[data.length + 8 + (padding ? 1:0)];
		b[0]=0x64; b[1]=0x61;b[2]=0x74;b[3]=0x61; // "data"
    	b[4] = ((byte)(data.length & 0xff));
    	b[5] = ((byte)((data.length>>>8) & 0xff));
    	b[6] = ((byte)((data.length>>>16) & 0xff));
    	b[7] = ((byte)((data.length>>>24) & 0xff));
    	for (int i=8; i<data.length+8; i++)
    		b[i]=data[i-8];
    	
    	if (padding)
    		b[b.length-1] = 0x00;
    	
		return b;
	}


	/**
	 * Create a chunk of unknown type "abcd" with specified length
	 * @param len chunkSize
	 * @return
	 */
	private byte[] createOtherChunk(int len) {

		// Assert word alignment
		if ((len&1)==1)
			len++;
		
		byte[] b = new byte[len + 8];
		b[0]=0x61; b[1]=0x62;b[2]=0x63;b[3]=0x64; // "abcd"
    	b[4] = ((byte)(len & 0xff));
    	b[5] = ((byte)((len>>>8) & 0xff));
    	b[6] = ((byte)((len>>>16) & 0xff));
    	b[7] = ((byte)((len>>>24) & 0xff));
    	
		return b;
	}

	
	/**
	 * Create a new media object for testing purposes.
	 * 
	 * @param samples sample data to include.
	 * @param compressionCode the compression code to use (6=alaw, 7=ulaw)
	 * @param bufSize the byte buffer size to use when creating media object.
	 * @return a new media object
	 * 
	 * @throws MediaObjectException if the media object could not be created.
	 * @throws MimeTypeParseException should not happen 
	 */
	private IMediaObject createWavMo(byte[] samples,int compressionCode, int bufSize) 
		throws MediaObjectException, MimeTypeParseException {
		
		byte[] data = createDataChunk(samples);
		byte[] fmt = createFmtChunk(compressionCode, 1, STANDARD_SAMPLERATE, 
				AVGBPS, BLOCKALIGN, BITSPERSAMPLE, 0);
		byte[] riff = createRiffHeader(4+fmt.length+data.length);
		
		LinkedList<InputStream> seq = new LinkedList<InputStream>();
		seq.add(new ByteArrayInputStream(riff));
		seq.add(new ByteArrayInputStream(fmt));
		seq.add(new ByteArrayInputStream(data));
		SequenceInputStream seqIS = new SequenceInputStream(Collections.enumeration(seq));
		
		IMediaObject mo;
		mo = mediaObjectFactory.create(seqIS, bufSize, new MimeType("audio/wav"));
		
		return mo;
	}
	
	
	/**
	 * Verify the validity of the WAV-file in the given media object.
	 * Will validate:
	 *  - That the sample data in WAV-file exactely matches the given samples.
	 *  - That the WAV-file has word aligned length (a padding byte of zero is
	 *     expected if sample data length is odd).
	 *  - The compression code in the WAV-file matches the given compressionCode.
	 *  - All other parameters of the WAV-file fmt chunk matches the 
	 *    supported default values. 
	 *    
	 * @param mo the MediaObject to validate.
	 * @param compressionCode the compressionCode to expect.
	 * @param samples the audio samples to expect.
	 * @throws IOException if the media object could not be validated ok.
	 */
	private void validateWavMo(IMediaObject mo, int compressionCode, byte[] samples, 
			boolean bigFile) throws IOException {

		int moSize = (int)mo.getSize();
		int hdrSize = RIFF_HDR_SIZE + FMT_HDR_SIZE + DATA_HDR_SIZE; 
		
		
		// If length of samples is odd we expect one extra padding byte at end of data
		if ((samples.length&1)==1)
			assertEquals(samples.length+1, moSize-hdrSize);
		else
			assertEquals(samples.length, moSize-hdrSize);
		
				
		// Read all data from media object into a byte array
		InputStream in = mo.getInputStream();
		byte[] moData = new byte[moSize];
		int bytesRead = in.read(moData);
		int offset = 0;
		while (bytesRead>0 && bytesRead<moSize) {
			offset += bytesRead;
			bytesRead = in.read(moData,offset,moSize-bytesRead);
		}
		
		
		// Assert that we have actually read all bytes
		assertEquals(moSize, offset+bytesRead);

		
		// Assert that the stream is exhausted
		assertFalse(in.available()>0);

		
		// Slice out the different headers/chunks from the read byte array
		byte[] riff = slice(moData,0,12);
		byte[] expectedRiff = createRiffHeader(moSize-8);
		
		byte[] fmt = slice(moData,12,12+26);
		byte[] expectedFmt = createFmtChunk(compressionCode,1,
				STANDARD_SAMPLERATE,AVGBPS,BLOCKALIGN,BITSPERSAMPLE,0);
		
		byte[] data = slice(moData,12+26,moData.length);
		byte[] expectedData = createDataChunk(samples);

		
		// Compare RIFF header
		assertTrue("Expected:\n" + bytes2hex(expectedRiff) + "\nBut got:\n" + bytes2hex(riff), 
				Arrays.equals(expectedRiff, riff));

		// Compare fmt chunk
		assertTrue("Expected:\n" + bytes2hex(expectedFmt) + "\nBut got:\n" + bytes2hex(fmt), 
				Arrays.equals(expectedFmt, fmt));

		// Compare data chunk
		if (bigFile) {
			// If its a big file only compare the data, do not convert data into hex strings
			// since this would consume a lot of memory and time.
			assertTrue(Arrays.equals(expectedData, data));
		} else {
			assertTrue("Expected:\n" + bytes2hex(expectedData) + "\nBut got:\n" + bytes2hex(data), 
					Arrays.equals(expectedData, data));
		}
		
		// Verify length of media object in milliseconds
		assertEquals(samples.length/8, 
				mo.getMediaProperties().getLengthInUnit(LengthUnit.MILLISECONDS));
		
		// Assert media object is immutable
		assertTrue(mo.isImmutable());
//		System.out.println("Expected:\n" + bytes2hex(expectedData) + "\nGot:\n" + bytes2hex(data));
		
	}


	/**
	 * A primitive play method to play a media object. Only for testing purposes.
	 * @param mo
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */
	private void play(IMediaObject mo) throws UnsupportedAudioFileException, IOException {
		System.out.println("PLAY:");
		AudioFormat audioFormat;
		AudioInputStream ais;
		try {
			ais = AudioSystem.getAudioInputStream(mo.getInputStream());
			audioFormat = AudioSystem.getAudioFileFormat(mo.getInputStream()).getFormat();
			System.out.println("audioFormat=" + audioFormat);
		} catch (UnsupportedAudioFileException e1) {
			throw e1;
		} catch (IOException e1) {
			throw e1;
		}
		// If ulaw or alaw we have to convert to pcmu before playing...
		if ((audioFormat.getEncoding() == AudioFormat.Encoding.ULAW) ||
				(audioFormat.getEncoding() == AudioFormat.Encoding.ALAW)){
			AudioFormat pcmuFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					audioFormat.getSampleRate(),
					audioFormat.getSampleSizeInBits() * 2,
					audioFormat.getChannels(),
					audioFormat.getFrameSize() * 2,
					audioFormat.getFrameRate(),true);
			ais = AudioSystem.getAudioInputStream(pcmuFormat, ais);
			audioFormat = pcmuFormat;

		}

		try{
			DataLine.Info dataLineInfo = new DataLine.Info(
					SourceDataLine.class, audioFormat);

			SourceDataLine sourceDataLine = (SourceDataLine)AudioSystem.getLine(
					dataLineInfo);
			sourceDataLine.open(audioFormat);
			sourceDataLine.start();

			byte tempBuffer[] = new byte[10000];

			int cnt;
			//Keep looping until the input
			// read method returns -1 for
			// empty stream.
			while((cnt = ais.read(tempBuffer, 0,
					tempBuffer.length)) != -1){
				if(cnt > 0){
					//Write data to the internal
					// buffer of the data line
					// where it will be delivered
					// to the speaker.
					sourceDataLine.write(
							tempBuffer, 0, cnt);
				}//end if
			}//end while

			sourceDataLine.drain();
			sourceDataLine.close();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}
	}

}
