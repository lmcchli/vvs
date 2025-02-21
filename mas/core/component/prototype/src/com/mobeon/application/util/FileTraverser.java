/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * User: kalle
 * Date: Jan 31, 2005
 * Time: 5:37:33 PM
 */
public class FileTraverser
{
    private static final String getFileExtention(File file)
    {
        int pos = file.getName().lastIndexOf(".");
        if (pos > -1)
            return file.getName().substring(pos);
        else return null;
    }

    public static final File[] findFiles(File root, final String[] extentionFilter, final boolean recursive)
    {
        ArrayList list = new ArrayList();

        FileFilter fileFilter = new FileFilter()
        {
            public boolean accept(File file)
            {
                String fileExtention = getFileExtention(file);

                for (int i=0; i<extentionFilter.length; i++)
                    if (extentionFilter[i].equalsIgnoreCase(fileExtention))
                        return true;

                return false;
            }
        };

        addFiles(fileFilter, root, list, recursive);

        File[] ret = new File[list.size()];
        for (int i=0; i<list.size(); i++)
            ret[i] = (File)list.get(i);
        return ret;
    }

    private static final void addFiles(FileFilter filter, File root, ArrayList list, final boolean recursive)
    {
        File[] matchingFiles = root.listFiles(filter);
        if (matchingFiles != null)
        {
            for (int i=0; i<matchingFiles.length; i++)
            list.add(matchingFiles[i]);
        }

        if (recursive)
        {
            File[] subDirs = root.listFiles(new FileFilter()
            {
                public boolean accept(File file)
                {
                    if (recursive && file.isDirectory())
                        return true;
                    else
                        return false;
                }
            });

            if (subDirs != null)
            {
                for (int i=0; i<subDirs.length; i++)
                    addFiles(filter, subDirs[i], list, recursive);
            }
        }

    }

    public static final void main(String[] args)
        throws Exception
    {
        File[] f = findFiles(new File("/tmp"), new String[]{"txt", "xml", "wav"}, true);
        for (int i=0; i<f.length; i++)
            System.out.println(f[i].getAbsolutePath());
    }

}
