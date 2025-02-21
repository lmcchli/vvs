/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.configuration.moip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

/**
 * Internal utilities for the configuration manager.
 */
final class Utilities {

    // Disable constructor since the class is static only.
    private Utilities() {
    }

    /**
     * dotSplit will split a string into components delimited by dots '.'.
     * Empty component strings are removed.
     *
     * @param in The string that is to be split. If null return value will
     *           be an empty array.
     * @return An array containing all components of the string. The array will
     *         not contain any empty strings.
     */
    public static LinkedList<String> dotSplit(String in) {
        LinkedList<String> list = new LinkedList<String>();
        if (in != null && in.length() > 0) {
            for (String s : in.split("\\.")) {
                if (s.length() > 0) {
                    list.add(s);
                }
            }
        }
        return list;
    }

    /**
     * Remove the head element from an array.
     *
     * @param list The array to process.
     * @return An array containing the tail of the array.
     */
    public static String[] tail(String[] list) {
        if (list.length == 0) {
            return new String[0];
        }
        String[] ret = new String[list.length - 1];
        for (int i = 1; i < list.length; ++i) {
            ret[i - 1] = list[i];
        }
        return ret;
    }

    public static String createBackupName(String original) {
        return original + ".bak";
    }

    public static void backupFile(String original) throws IOException {
        copyFile(original, createBackupName(original));
    }

    public static void copyFile(String fromFile, String toFile) throws IOException {
        FileInputStream inFile = new FileInputStream(fromFile);
        FileOutputStream outFile = new FileOutputStream(toFile);

        FileChannel inChannel = inFile.getChannel();
        FileChannel outChannel = outFile.getChannel();

        int bytesWritten = 0;
        long byteCount = inChannel.size();
        while (bytesWritten < byteCount) {
            bytesWritten += inChannel.transferTo(bytesWritten,
                    byteCount - bytesWritten,
                    outChannel);
        }

        inFile.close();
        outFile.close();
    }
}
