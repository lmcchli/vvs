/*
* COPYRIGHT Abcxyz Communication Inc. Montreal 2010
* The copyright to the computer program(s) herein is the property
* of ABCXYZ Communication Inc. Canada. The program(s) may be used
* and/or copied only with the written permission from ABCXYZ
* Communication Inc. or in accordance with the terms and conditions
* stipulated in the agreement/contact under which the program(s)
* have been supplied.
*---------------------------------------------------------------------
* Created on 7-Apr-2010
*/
package com.mobeon.ntf.out.mediaconversion;

import com.mobeon.common.util.M3Utils;
import com.mobeon.ntf.util.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Media converter that converts one file to another file.
 */
public abstract class FileToFileConverter {
    
    static private Logger log = Logger.getLogger(FileToFileConverter.class);

    protected String IN_FORMAT;
    protected String OUT_FORMAT;
    protected String OUT_MIME;
    private static String TEMPDIR = "/opt/moip/ntf/mediaconversion";

    /**
     * Constructor.
     */
    protected FileToFileConverter(String IN_FORMAT, String OUT_FORMAT, String OUT_MIME) {
        this.IN_FORMAT = IN_FORMAT;
        this.OUT_FORMAT = OUT_FORMAT;
        this.OUT_MIME = OUT_MIME;
    }

    protected abstract int convert (String inFileName, String outFileName, int wantedLength);

    /**
     * Convert the contents of the stream from one format to another by writing
     * the stream to a file, call a specialised converter and reading a file with
     * the result.
     *@param is the stream with data.
     *@param wantedLength the wanted maximum length (in millisecond) of the
     * result.
     *@return the conversion result.
     */
    public ConversionResult convert(InputStream is, int wantedLength) {
        ConversionResult result = null;
        String inFileName = null;
        String outFileName = null;
        String tempFileBase = null;
        File tempDir = new File(TEMPDIR);
        try {
            log.logMessage("Starting conversion", Logger.L_DEBUG);

            File inFile = File.createTempFile("conv", "." + IN_FORMAT,
                                              new File(TEMPDIR));
            inFileName = inFile.getCanonicalPath();
            tempFileBase = inFileName.substring(0, inFileName.length() - IN_FORMAT.length() - 1);
            outFileName = tempFileBase + "." + OUT_FORMAT;

            long fileStart = System.currentTimeMillis();
            int inputSize = copyStream(is, new FileOutputStream(inFile));
            if (inputSize == 0) {
                return new ConversionResult(2, "Nothing to convert - the attachment is empty");
            }

            long convertStart = System.currentTimeMillis();
            /***
             *** And here is the call to the actual conversion in the subclass.
             ***/
            int ret = convert(inFileName, outFileName, wantedLength);
            long convertEnd = System.currentTimeMillis();
            log.logMessage(("FileToFileConverter: Conversion to \"" + outFileName + "\" finished with result " + ret), Logger.L_DEBUG);

            if (ret == 0) {
                File outFile = new File(outFileName);
                File durFile = new File(outFileName + ".dur");
                if (outFile.exists() && outFile.length() > 0) {
                    int milliseconds = -1;
                    if (durFile.exists()) {
                        try {
                            milliseconds =
                                Integer.parseInt((new BufferedReader
                                                  (new FileReader(durFile))).readLine());
                        } catch (Exception e) {
                            log.logMessage("Could not get duration from file: " + e, Logger.L_DEBUG);
                        }
                    } else {
                        /*bytes * bits per byte * milliseconds per second / bits per second */
                        milliseconds = (int) (outFile.length() * 8 * 1000 / 4750);
                    }
                    log.logMessage("FileToFileConverter: Successfully converted " + IN_FORMAT + " ("
                                  + inputSize + " bytes) to " + OUT_FORMAT + " ("
                                  + outFile.length() + " bytes = " + milliseconds + " ms) in "
                                  + (convertStart - fileStart) + "+"
                                  + (convertEnd - convertStart) + " milliseconds.", Logger.L_DEBUG);

                    byte[] b = new byte[(int)outFile.length()];
                    new FileInputStream(outFile).read(b);
                    result = new ConversionResult(OUT_MIME, new ByteArrayInputStream(b),
                                                  milliseconds, (int) (convertEnd - fileStart));
                } else {
                    result = new ConversionResult(1, "Conversion result was empty");
                }
            } else {
                result = new ConversionResult(1, "Could not convert "
                                              + IN_FORMAT + " to " + OUT_FORMAT);
            }
        } catch (Exception e) {
            log.logMessage("FileToFileConverter: Could not convert message from " + IN_FORMAT + " to "
                      + OUT_FORMAT + ": " + M3Utils.stackTrace(e)
                      + "\ntempdir=" + TEMPDIR
                      + " infile=" + inFileName
                      + " outfile=" + outFileName, Logger.L_ERROR);
            result = new ConversionResult(1, "Could not convert "
                                          + IN_FORMAT + " to " + OUT_FORMAT);
        } finally {
            try {
                File[] tempFiles = tempDir.listFiles();
                for (int i = 0; i < tempFiles.length; i++) {
                    if (tempFiles[i].getCanonicalPath().startsWith(tempFileBase)) {
                        tempFiles[i].delete();
                    }
                }
            } catch (Exception e) { ; }
        }
        return result;
    }

    /**
     * Copy one stream to another. Used to copy the stream from XMP to a file
     * for the converter program.
     *@param is the source stream.
     *@param os the destination stream.
     *@return the number of bytes copied.
     */
     protected static int copyStream(InputStream is, OutputStream os) {
        int result = 0;
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        try {
            while ((bytesRead = is.read(buffer)) != -1) {
                result += bytesRead;
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.logMessage("FileToFileConverter: Could not copy WAV stream to file: " + e, Logger.L_ERROR);
            result = -1;
        } finally {
            try {
		is.close();
		os.close();
	    } catch (IOException e) { ; }
        }
        return result;
    }



    public static void main(String[] args) {
        WavToAmrConverter a = (WavToAmrConverter) WavToAmrConverter.get();

        a.printit();
        a.printit();
    }
}
