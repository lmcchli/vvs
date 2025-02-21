package com.mobeon.masp.execution_engine;

import java.io.FilenameFilter;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-jan-11
 * Time: 14:00:31
 * To change this template use File | Settings | File Templates.
 */
public class AutoTestFilter implements FilenameFilter {
    final static Pattern PREFIX_PATTERN = Pattern.compile("^(.*?_\\d+)\\.xml$");
    final String prefix;

    public AutoTestFilter(String name) {
        Matcher prefixMatcher = PREFIX_PATTERN.matcher(name);
        if(prefixMatcher.matches()){
            prefix = prefixMatcher.group(1);
            return;
        }
        prefix = null;
    }

    public boolean accept(File dir, String name) {
        return prefix != null && name.startsWith(prefix) && !Character.isDigit(name.charAt(prefix.length()));
    }
}
