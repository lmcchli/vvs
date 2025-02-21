/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.grammar.grammarfiletool;

import com.mobeon.masp.mediacontentmanager.xml.GrammarMapper;
import com.mobeon.masp.mediacontentmanager.xml.SaxMapperException;
import com.mobeon.masp.mediacontentmanager.grammar.RulesRecord;
import com.mobeon.masp.mediacontentmanager.grammar.GenericNumberBuilder;
import com.mobeon.masp.mediacontentmanager.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.NoSuchElementException;


/**
 * Tool used to verify grammar files.
 * <p/>
 * The tool takes as input a grammar.xml file and an input value file. Each
 * number in the input value file is decomposed according to the rules in the
 * grammar file. The output is printed to System.out.
 * <p/>
 * The numbers in the input value file should be listed in the following format:
 * <code>number;type;gender</code>, for example:
 * <pre>
 * 
 * 1002;Number;Male
 * 2006-02-01;DateDM;None
 * 22:34:00;Time24;None
 * </pre>
 *
 * @author mmawi
 */
public class GrammarFileTool {

    private static ILogger LOGGER =
            ILoggerFactory.getILogger(GrammarFileTool.class);

    /**
     * Represents an entry in the input data file.
     * The entry has a data value, a type and a gender.
     */
    private class InputDataEntry {
        private String value;
        private IMediaQualifier.QualiferType type;
        private IMediaQualifier.Gender gender;

        /**
         * Creates a new input value entry.
         *
         * @param value     The data value, e.g. a number, date or time.
         * @param type      The type of the value.
         * @param gender    The gender of the value.
         */
        public InputDataEntry(String value, IMediaQualifier.QualiferType type, IMediaQualifier.Gender gender) {
            this.value = value;
            this.type = type;
            this.gender = gender;
        }

        /**
         * Returns the value of the data in this entry.
         *
         * @return The value of the data.
         */
        public String getValue() {
            return value;
        }

        /**
         * Returns the type of the data in this entry.
         *
         * @return The type of data.
         */
        public IMediaQualifier.QualiferType getType() {
            return type;
        }

        /**
         * Returns the gender of the data in this entry.
         *
         * @return The gender of the data.
         */
        public IMediaQualifier.Gender getGender() {
            return gender;
        }
    }

    /**
     * The <code>RulesRecord</code>s read from the grammar file.
     */
    private List<RulesRecord> rulesRecordList;
    /**
     * The resulting list of spoken words after the number
     * has been decomposed.
     */
    private List<String> resultList;
    /**
     * The list of <code>InputDataEntry</code>s read from the input data file.
     */
    private List<InputDataEntry> inputDataList = new ArrayList<InputDataEntry>();

    /**
     * The grammar mapper used.
     */
    private static GrammarMapper grammarMapper = new GrammarMapper();
    /**
     * The number builder used.
     */
    private static IGenericNumberBuilder numberBuilder = new GenericNumberBuilder();

    /**
     * Reads the grammar file and stores the <code>RulesRecord</code>s in a list.
     *
     * @param grammarFileURL    The URL for the grammar file.
     * @throws Exception    If the grammar mapper fails to parse the file.
     */
    private void getRulesRecords(URL grammarFileURL) throws Exception {
        try {
            rulesRecordList = grammarMapper.fromXML(grammarFileURL);
        } catch (SaxMapperException e) {
            throw new Exception("ERROR: Unable to parse file: " + grammarFileURL.getFile(), e);
        }
    }

    /**
     * Read input data from a file and store the data in a list.
     * @param inputFile The input data file.
     */
    private void getInputData(String inputFile) {
        try {
            RandomAccessFile inFile = new RandomAccessFile(inputFile, "r");
            String tempString;
            while (true) {
                tempString = inFile.readLine();
                if (tempString == null) {
                    break;
                }
                if (tempString.startsWith("//")) {
                    //Skip this line
                    continue;
                }
                tempString = tempString.trim();
                String data;
                String typeStr;
                String genderStr;
                try {
                    StringTokenizer st = new StringTokenizer(tempString, ";");
                    data = st.nextToken();
                    typeStr = st.nextToken();
                    genderStr = st.nextToken();
                } catch (NullPointerException e) {
                    //Skip this line
                    continue;
                } catch (NoSuchElementException e) {
                    //Skip this line
                    System.out.println("ERROR: Syntax error in line: " + tempString);
                    continue;
                }
                IMediaQualifier.QualiferType type = null;
                try {
                    type = IMediaQualifier.QualiferType.valueOf(typeStr);
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: Could not create QualifierType from String: " + typeStr
                            + ". No such QualifierType.");
                }
                IMediaQualifier.Gender gender = null;
                try {
                    gender = IMediaQualifier.Gender.valueOf(genderStr);
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: Could not create Gender from String: " + genderStr
                            + ". No such Gender.");
                }
                if (type != null && gender != null) {
                    inputDataList.add(new InputDataEntry(data, type, gender));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Could not open file for reading, file not found: " + inputFile);
            //e.printStackTrace();
        } catch (EOFException e) {
            System.out.println("EOF reached.");
        } catch (IOException e) {
            System.out.println("ERROR: Failed to read file: " + inputFile);
            //e.printStackTrace();
        }
    }

    /**
     * Decomposes the numbers in the <code>InputDataEntry</code>s and
     * prints the resulting <code>MessageElement</code> references to
     * System.out.
     */
    private void decomposeNumbers() {
        for (InputDataEntry dataRecord : inputDataList) {
            for (IRulesRecord rulesRecord: rulesRecordList) {
                if (rulesRecord.compareRule(dataRecord.getGender(), dataRecord.getType())) {
                    Long number;
                    StringTokenizer st;
                    switch (dataRecord.getType()) {
                        case DateDM:
                            // Date in format "yyyy-MM-dd"
                            st = new StringTokenizer(dataRecord.getValue(), "-");
                            String year = st.nextToken(); //Year not used.
                            Long month = Long.valueOf(st.nextToken());
                            Long day =  Long.valueOf(st.nextToken());
                            number = day * 10000 + month * 100;
                            break;
                        case Number:
                            number = Long.valueOf(dataRecord.getValue());
                            break;
                        case Time12:
                            // Time in format "HH:mm:ss"
                            // postfix: 0 = AM, 7000 = PM
                            Long postfix = (long) 0;
                            st = new StringTokenizer(dataRecord.getValue(), ":");
                            Long hours12 = Long.valueOf(st.nextToken());
                            Long minutes12 = Long.valueOf(st.nextToken());
                            if (hours12 >= 12) {
                                if (hours12 > 12) {
                                    hours12 = hours12 - 12;
                                }
                                postfix = (long) 7000;
                            } else if (hours12 == 0) {
                                hours12 = (long) 12;
                            }

                            // NumberBuilder expects input in the form HHmm0000 for AM and HHmm7000 for PM.
                            number = hours12 * 1000000 + minutes12 * 10000 + postfix;
                            break;
                        case Time24:
                            // Time in format "HH:mm:ss"
                            st = new StringTokenizer(dataRecord.getValue(), ":");
                            Long hours24 = Long.valueOf(st.nextToken());
                            Long minutes24 = Long.valueOf(st.nextToken());
                            // NumberBuilder expects input in the form HHmm00
                            number = hours24 * 10000 + minutes24 * 100;
                            break;
                        default:
                            //todo throw some exception instead
                            number = (long) 0;
                            break;
                    }
                    resultList = numberBuilder.buildNumber(rulesRecord, number);

                    System.out.print(dataRecord.getValue() + "\t");
                    for (String element : resultList) {
                        System.out.print(" " + element);
                    }
                    System.out.println("");
                }
            }
        }
    }


    /**
     * Decompose each data entry in the input data file using the rules in the
     * grammar file. The resulting file name references are
     * printed to System.out.
     *  
     * @param args A grammar file and an input data file are required.
     */
    public static void main(String[] args) {
        File grammarFile;
        String inputFileName;
        if (args.length < 2) {
            System.out.println("Wrong number of arguments. " +
                    "Grammar file and input data file required.");
        } else {
            try {
                grammarFile = new File(args[0]);
                inputFileName = args[1];
                //grammarFile = new File("applications/mediacontentpackages/en_audio_1/grammar.xml");
                //inputFileName = "applications/mediacontentpackages/en_audio_1/input.txt";

                GrammarFileTool tool = new GrammarFileTool();
                tool.getRulesRecords(grammarFile.toURL());
                tool.getInputData(inputFileName);
                tool.decomposeNumbers();
            } catch (IndexOutOfBoundsException e) {
                System.out.println(e.getMessage());
            } catch (MalformedURLException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
