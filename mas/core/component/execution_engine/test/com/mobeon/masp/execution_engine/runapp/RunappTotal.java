package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author David Looberger
 */
public abstract class RunappTotal extends TestCase {
    public RunappTotal(String event) {
        super(event);
    }

    public static Test suite() {
        TestSuite ts = new TestSuite();

        buildSuite(ts, "Application");
        return ts;
    }

    protected static void buildSuite(TestSuite ts, String filter) {
        Class[] classes;
        try {
            classes = getClasses(RunappTotal.class.getPackage().getName(), filter, false);
            for (Class cl : classes) {
                try {
                    Type superclass = cl.getSuperclass();
                    if (superclass != ApplicationBasicTestCase.class)
                        continue;

                    Constructor ctor = cl.getConstructor(String.class);
                    Object obj = ctor.newInstance("");
                    Method method = cl.getMethod("suite");
                    ts.addTest((Test) method.invoke(obj));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Class[] getClasses
            (String
                    pckgname, String
                    prefix, boolean includeInnerClasses) throws ClassNotFoundException {
        ArrayList<Class> classes = new ArrayList<Class>();
        // Get a File object for the package
        File directory = null;
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            String path = pckgname.replace('.', '/');
            URL resource = cld.getResource(path);
            if (resource == null) {
                throw new ClassNotFoundException("No resource for " + path);
            }
            directory = new File(resource.getFile());
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " (" + directory
                    + ") does not appear to be a valid package");
        }
        if (directory.exists()) {
            // Get the list of the files contained in the package
            String[] files = directory.list();
            for (int i = 0; i < files.length; i++) {
                // we are only interested in .class files
                if (files[i].endsWith(".class") && files[i].startsWith(prefix)) {
                    if (!includeInnerClasses && files[i].contains("$"))
                        continue;
                    // removes the .class extension
                    classes.add(Class.forName(pckgname + '.'
                            + files[i].substring(0, files[i].length() - 6)));
                }
            }
        } else {
            throw new ClassNotFoundException(pckgname
                    + " does not appear to be a valid package");
        }
        Class[] classesA = new Class[classes.size()];
        classes.toArray(classesA);
        return classesA;
    }

}
