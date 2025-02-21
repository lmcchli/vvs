/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.content;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.*;
import java.io.*;

/**
 * <p>
 * ContentSizePredicter can be used for predict the actual size of an MIME encoded section of data.
 * </p>
 * <p>
 * The size of MIME encoded data is often larger then the actual value, and the ratio is dependent on
 * the actual content (See below on Base64 and Quoted-Printable). Therefore it is not possible to
 * exactly calculate the actual size from the size of the encoded data. The actual size can however
 * be predicted using the size of the encoded data, encoding and content-type.
 * </p>
 * <p>
 * ContentSizePredicter can adaptivly learn the ratio between the size of the encoded data and the actual size.
 * For each combination of encoding/content-type ContentSizePredicter holds a list of dividers (representing
 * the the ratio). Each value is calculated from the the size of the encoded data and the actual size. When the
 * ContentSizePredicter is loaded these lists is empty (not existing actually).
 * Calls to {@link #learn(long, long, String, String)} will add the calculated divider for that combination
 * of encoding/content-type to that combination's list. The list will succedlingly grow up to 100 values.
 * When 100 values is reached ContentSizePredicter will start remove max and min values from the list before
 * adding a new value. Calls to {@link #predict(long, String, String)} uses the median value in the list as
 * divider to predict the actual size from the size of the encoded data. If no "lesson has been learned" prior to
 * the call, the list is seeded with a default value (mostly 1.0, but may differ for some encodings).
 * ContentSizePredicter can learn any combination of encoding/content-type. MIME defines <tt>7bit</tt>,
 * <tt>8bit</tt>, <tt>binary</tt>, <tt>base64</tt> and <tt>quoted-printable</tt>.
 * </p>
 * <p>
 * <b>Base64</b><br>
 * Full specifications for this form of base64 are contained in RFC 1421 and RFC 2045.
 * The scheme is defined to encode a sequence of octets (bytes).
 * The resultant base64-encoded data exceeds the original in length by the ratio 4:3.
 * As newlines are inserted in the encoded data every 76 characters, the actual length
 * of the encoded data is approximately 135.1% of the original. Extra trailing '=' characters may
 * also be added if the orignal data is not a multipel of three 8-bit bytes.
 * </p>
 * <p>
 * <b>Quoted-Printable</b><br>
 * The scheme is designed to encode data consisting primarly of text to transmit 8bit
 * data over a 7bit data path. Any 8-bit byte value may be encoded with 3 characters,
 * an "=" followed by two hexadecimal digits (0-9 or A-F) representing the byte's numeric value.
 * </p>
 * <p>
 * It's possible to log prediction statistics. Log level must be DEBUG and a file named
 * "com.mobeon.masp.util.content.ContentSizePredicter.csv" must be present in java home.
 * Then prediction stats will be written to the file.
 * </p>
 *
 * @author Håkan Stolt
 */
public class ContentSizePredicter  {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(ContentSizePredicter.class);

    /**
     * Prediction knowledge database.
     */
    private static final Map<String,EncodingStats> statsMap = new HashMap<String,EncodingStats>();
    public static final double BASE64_ENCODED_SIZE_DIVIDER_SEED = 1.351;
    public static final double DEFAULT_ENCODED_SIZE_DIVIDER_SEED = 1.0;
    private static final String PREDICTION_STATS_LOGFILE_NAME = ContentSizePredicter.class.getName()+".csv";
    private static boolean appendPredictionLogFile = false;

    /**
     * Calculates a predicted actual size from the encoded size, encoding and content-type.
     * If encoded size is zero or less the encoded size will be returned untouched.
     * Encoding and content-type cannot be null or empty.
     * @param encodedSize
     * @param encoding
     * @param contentType
     * @return the predicted actual size.
     */
    public synchronized static long predict(long encodedSize, String encoding, String contentType) {
        long result = encodedSize;
        if(encodedSize > 0) {
            double dSize = encodedSize;
            EncodingStats encodingStats = getEncodingStats(encoding, contentType);
            dSize = dSize/encodingStats.encodedSizeDivider;
            result = Math.round(dSize);
        }
        if(LOGGER.isDebugEnabled())LOGGER.debug("Predicted "+encoding+" encoded "+contentType+" data "+encodedSize +"->"+result);
        return result;
    }

    /**
     * Feeds the ContentSizePredicter's knowledge base of the ratio between the size of encoded and the actual size.
     * If encoded size or actual size is zero or less the values will be ignored.
     * Encoding and content-type cannot be null or empty.
     * @param encodededSize
     * @param actualSize
     * @param encoding
     * @param contentType
     */
    public synchronized static void learn(long encodededSize, long actualSize, String encoding, String contentType) {
        if(encodededSize > 0 && actualSize > 0) {
            EncodingStats encodingStats = getEncodingStats(encoding,contentType);
            double divider = (double)encodededSize/(double)actualSize; //Calculate "this lesson".
            if(encodingStats.dividerValues.size()>100) {
                //If knowledge base grows big remove max and min values.
                encodingStats.dividerValues.remove(encodingStats.dividerValues.size()-1);
                encodingStats.dividerValues.remove(0);
            }
            encodingStats.dividerValues.add(divider);
            if(encodingStats.dividerValues.size()>2) {
                //Get median
                Collections.sort(encodingStats.dividerValues);
                encodingStats.encodedSizeDivider = encodingStats.dividerValues.get(encodingStats.dividerValues.size()/2);
            } else if(encodingStats.dividerValues.size() == 2)  {
                //Calculate average
                encodingStats.encodedSizeDivider = (encodingStats.dividerValues.get(0)+encodingStats.dividerValues.get(1))/2;
            } else {
                //Use this "lesson".
                encodingStats.encodedSizeDivider = divider;
            }
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Prediction learned: "+encoding+" encoded "+contentType+" data "+encodededSize+" had actual size "+actualSize+".");
                LOGGER.debug("statsMap="+statsMap);
                log(encodededSize, encoding, contentType, actualSize, encodingStats);
            }
        }
    }

    private synchronized static EncodingStats getEncodingStats(String encoding, String contentType) {
        if(encoding == null || encoding.length()==0) {
            throw new IllegalArgumentException("encoding cannot be null or empty!");
        }
        if(contentType == null || contentType.length()==0) {
            throw new IllegalArgumentException("contentType cannot be null or empty!");
        }
        String key = encoding.toLowerCase()+"_"+contentType.toLowerCase();
        EncodingStats encodingStats = statsMap.get(key);
        if(encodingStats == null) {
            if(encoding.equalsIgnoreCase("base64")) {
                encodingStats = new EncodingStats(BASE64_ENCODED_SIZE_DIVIDER_SEED);
            } else {
                encodingStats = new EncodingStats(DEFAULT_ENCODED_SIZE_DIVIDER_SEED);
            }

            statsMap.put(key,encodingStats);
        }
        return encodingStats;
    }

    /**
     * Writes prediction stats to a file named "com.mobeon.masp.util.content.ContentSizePredicter.csv"
     * if present in java home.
     * @param encodededSize
     * @param encoding
     * @param contentType
     * @param actualSize
     * @param encodingStats
     */
    private static void log(long encodededSize, String encoding, String contentType, long actualSize, EncodingStats encodingStats) {
        long p = predict(encodededSize, encoding, contentType);
        double correctness = (double)actualSize/(double)p;
        File f = new File(PREDICTION_STATS_LOGFILE_NAME);
        if(f.exists()) {
            try {
                FileOutputStream statsfile = new FileOutputStream(f,appendPredictionLogFile);
                OutputStreamWriter osw = new OutputStreamWriter(statsfile);
                if(!appendPredictionLogFile) {
                    osw.write("ActualSize;PredictedSize;Correctness;Encoding;ContentType;EncodedSizeDivider;NumberDividerValues"+System.getProperty("line.separator"));
                    appendPredictionLogFile = true;
                }
                osw.write(actualSize+";"+p+";"+correctness+";"+encoding+";"+contentType+";"+encodingStats.encodedSizeDivider+";"+encodingStats.dividerValues.size()+System.getProperty("line.separator"));
                osw.flush();
                osw.close();
            } catch (IOException e) {
                LOGGER.debug("Exception while writing stats to "+f.getName(),e);
            }
        }
    }


    /**
     * Prediction data holder.
     */
    private static class EncodingStats implements Serializable {
        private List<Double> dividerValues = new ArrayList<Double>();
        private double encodedSizeDivider;

        private EncodingStats(double encodedSizeDividerSeed) {
            this.encodedSizeDivider = encodedSizeDividerSeed;
        }
    }
}
