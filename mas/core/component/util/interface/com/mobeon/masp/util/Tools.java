/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util;

import com.mobeon.masp.execution_engine.ApplicationConfiguration;
import com.mobeon.masp.util.test.MASTestSwitches;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Undefined;

public class Tools {
    public static long millisFrom(long start) {
        return System.currentTimeMillis() - start;
    }

    public static interface Fn1<A,B>{
        A f(B arg1);
    }
    public static interface Fn2<A,B,C>{
        A f(B arg1,C arg2);
    }
    public static interface Fn3<A,B,C,D>{
        A f(B arg1,C arg2,D arg3);
    }

    public static class foldL1_1 extends func<String,Fn2<String,String,String>,List<String>>{
        public String f(Fn2<String,String,String> a, List<String> b) {
            ArrayList<String> c = new ArrayList<String>(b);
            String a1 = null;
            while(c.size() > 0) {
                if(a1 == null) {
                    a1 = c.remove(0);
                    continue;
                }
                a1 = a.f(a1,c.remove(0));
            }
            return a1;
        }
    }

    public static class add_1 extends func<String,String,String> {
        public String f(String arg1, String arg2) {
            return arg1 + ", "+arg2;
        }
    }
    public static abstract class func<A,B,C> implements Fn2<A,B,C>{

        public static func<String,Fn2<String,String,String>,List<String>> foldL1() {
            return new foldL1_1();
        }
        public static func<String,String,String> add() {
            return new add_1();
        }
    }

    public static <A,B> B apply(List<A> la,B b,Fn2<B,B,A> fn) {
        final int laSize = la.size();
        for(int i=0;i<laSize;i++) {
            b = fn.f(b,la.get(i));
        }
        return b;
    }
    public static <A,B> List<B> map(Fn1<B,A> mapFn, List<A> list) {
        List<B> result = new ArrayList<B>(list.size());
        final int listSize = list.size();
        for(int i=0;i<listSize;i++) {
            result.add(mapFn.f(list.get(i)));
        }
        return result;
    }

    public static void println(Object line) {
        System.out.println(line);
    }

    public static void println() {
        System.out.println();
    }

    public static void print(Object line) {
        System.out.println(line);
    }

    public static boolean isNull(Object o) {
        return o == null;
    }

    public static boolean notNull(Object o) {
        return o != null;
    }

    public static boolean startsWithOrEquals(String value, String prefix) {
        if (value == null) return false;
        if (value.startsWith(prefix)) return true;
        return value.equals(prefix);
    }

    public static boolean isTrueProperty(String property) {
        if (property == null) return false;
        property = property.trim();
        return property.equalsIgnoreCase("true") || property.equalsIgnoreCase("yes") || property.equalsIgnoreCase("1");
    }

    public static <T> void fillCollection(Collection<T> coll, int count, Prototype<T> proto) {
        for (int i = 0; i < count; i++) {
            coll.add(proto.duplicate());
        }
    }

    public static <T> void fillCollection(Collection<T> coll, int count,  Callable<T> constructor) throws Exception {
        for (int i = 0; i < count; i++) {
            coll.add(constructor.call());
        }
    }
    public static <T> void fillArray(T[] coll, Callable<T> constructor) throws Exception {
        int count = coll.length;
        for (int i = 0; i < count; i++) {
            coll[i] = constructor.call();
        }
    }

    public static <T> void fillArray(T[] coll, Prototype<T> proto) {
        int count = coll.length;
        for (int i = 0; i < count; i++) {
            coll[i] = proto.duplicate();
        }
    }

    public static String relativize(String base) {
        String cwd = ApplicationConfiguration.getInstance().getWorkingDir();
        
        if (cwd.startsWith("/")) cwd = cwd.substring(1);
        if (cwd.endsWith("/")) cwd = cwd.substring(0, cwd.length() - 1);
        base = base.replaceAll("\\$WORKINGDIR", cwd);
        return base;
    }

    public static String readStackDump(Throwable e) {
        StringWriter out = new StringWriter();
        boolean outer = true;
        do {
            if (!outer) {
                out.write("-------- Cause for previous error follows --------");
            }
            outer = false;
            out.write(e.getMessage());
            e.printStackTrace(new PrintWriter(out));
        } while ((e = e.getCause()) != null);
        return out.getBuffer().toString();

    }

    public static Object nullToUndefined(Object value) {
        if (value == null) return Undefined.instance;
        else
            return value;
    }

    public static Object toStringOrUndefined(Object object) {
        if (object == null) return Undefined.instance;
        else
            return object.toString();
    }

    public static void defineToStringMethod(ScriptableObject scriptableJava) {
        Method toString = null;
        try {
            toString = scriptableJava.getClass().getMethod(
                    "jstoString");
        } catch (NoSuchMethodException e) {
            Ignore.exception(e);
        }
        FunctionObject toStringFunc = new FunctionObject("toString", toString, scriptableJava);
        scriptableJava.defineProperty("toString", toStringFunc, ScriptableObject.READONLY);
    }

    public static void commaSeparate(StringBuffer sb, Object[] array) {
        int size = array.length;
        for (int i = 0; i < size; i++) {
            sb.append(array[i]);
            if (i < size - 1) sb.append(", ");
        }
    }

    public static void commaSeparate(StringBuilder sb, Object[] array) {
        int size = array.length;
        for (int i = 0; i < size; i++) {
            sb.append(array[i]);
            if (i < size - 1) sb.append(", ");
        }
    }


    public static boolean isDefined(String str) {
        return str != null && str.length() > 0;
    }

    public static boolean isEmpty(String str) {
        return !isDefined(str);
    }

    public static String classToMnemonic(Class canonicalClass) {
        String mnemonic = canonicalClass.getCanonicalName();
        int lastDot = mnemonic.lastIndexOf('.');
        return mnemonic.substring(lastDot + 1);
    }

    public static Long parseCSS2Time(String maxtime) {
        Long result = null;
        try {
            if (maxtime.contains("ms")) {
                maxtime = maxtime.substring(0, maxtime.indexOf("ms"));
                result = Math.round(Double.parseDouble(maxtime));
            } else if (maxtime.contains("s")) {
                maxtime = maxtime.substring(0, maxtime.indexOf("s"));
                result = Math.round(Double.parseDouble(maxtime) * 1000);
            }
        } catch (NumberFormatException nfe) {
            Ignore.numberFormatException(nfe);
        }
        return result;
    }

    /**
     * Split an URI into the part before hash (#) or after.
     * If there is no # character, the uri string will be considered
     * to be the entire contents before #.
     * Example: splitDocumentAndFragment("kalle", true) yields "kalle".
     * Example: splitDocumentAndFragment("kalle", false) yields null.
     *
     * @param uri        the URI to analyze
     * @param beforeHash whether contents before or after # shall be returned
     * @return the resulting URI part
     */
    public static String splitDocumentAndFragment(String uri, boolean beforeHash) {
        if (uri == null) {
            return null;
        }
        int index = uri.indexOf('#');
        if (index == -1) {
            // Special case: no # in "uri"
            if (beforeHash) {
                return uri;
            } else {
                return null;
            }
        }
        if (beforeHash) {
            return uri.substring(0, index);
        } else {
            return uri.substring(index + 1);
        }
    }

    public static String documentURI(String uri) {
        return splitDocumentAndFragment(uri, true);
    }

    public static String fragmentOfURI(String uri) {
        return splitDocumentAndFragment(uri, false);
    }

    public static URI documentURI(URI uri) {
        try {
            if (uri != null)
                return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
        } catch (URISyntaxException e) {
            Ignore.uriSyntaxException(e);
        }
        return null;
    }

    public static URI toUri(String target) {
        URI uri;
        try {
            uri = new URI(target);
        } catch (URISyntaxException e) {
            uri = null;
        }
        return uri;
    }

    public static long secondsToMillis(int s) {
        return TimeUnit.MILLISECONDS
                .convert(s, TimeUnit.SECONDS);
    }

    public static float millisToSeconds(int ms) {
        return ms / 1000;
    }

    public static long toMillis(TimeValue t) {
        long time = t.getTime();
        switch (t.getUnit()) {
            case NANOSECONDS:
                return (time / 1000000);
            case MICROSECONDS:
                return (time / 1000);
            case MILLISECONDS:
                return time;
            case SECONDS:
                return (time * 1000);
        }
        return time;
    }

    public static boolean isPopulated(Map<?, ?> map) {
        return !map.isEmpty();
    }

    @SuppressWarnings({"unchecked"})
    public static <R> R readField(Class<?> clazz, String name) {
        try {
            return (R) clazz.getField(name).get(null);
        } catch (IllegalAccessException e) {
            Tools.ignoreException(e);
        } catch (NoSuchFieldException e) {
            Tools.ignoreException(e);
        } catch (NullPointerException e) {
            Tools.ignoreException(e);
        }
        return null;
    }

    public static <T> void writeField(Class<?> clazz, String name, T value) {
        try {
            clazz.getField(name).set(null, value);
        } catch (IllegalAccessException e) {
            Tools.ignoreException(e);
        } catch (NoSuchFieldException e) {
            Tools.ignoreException(e);
        } catch (NullPointerException e) {
            Tools.ignoreException(e);
        }
    }

    private static void ignoreException(Exception e) {
    }

    public static String outerCaller(int i, boolean debugEnabled) {
        return com.mobeon.masp.util.debug.Tools.outerCaller(++i,debugEnabled);
    }

    public static class Reflection {
        public static Method staticMethod(Class<?> cls,String name) {
            Method m = null;
            try {
                m  = cls.getMethod("beforeSuite",new Class[0]);
            } catch (NoSuchMethodException e) {
                Ignore.noSuchMethodException(e);
            }
            return m;
        }

        public static void call(Method method) {            
            if(method != null) {
                try {
                    method.invoke(null);
                } catch (IllegalAccessException e) {
                    Ignore.illegaleAccessException(e);
                } catch (InvocationTargetException e) {
                    if(e.getTargetException() instanceof RuntimeException) {
                        throw ((RuntimeException)e.getTargetException());
                    }
                }
            }
        }
    }
}
