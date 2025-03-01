package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * A <code>TestSuite</code> is a <code>Composite</code> of Tests.
 * It runs a collection of test cases. Here is an example using
 * the dynamic test definition.
 * <pre>
 * TestSuite suite= new TestSuite();
 * suite.addTest(new MathTest("testAdd"));
 * suite.addTest(new MathTest("testDivideByZero"));
 * </pre>
 * Alternatively, a TestSuite can extract the tests to be run automatically.
 * To do so you pass the class of your TestCase class to the
 * TestSuite constructor.
 * <pre>
 * TestSuite suite= new TestSuite(MathTest.class);
 * </pre>
 * This constructor creates a suite with all the methods
 * starting with "test" that take no arguments.
 * <p/>
 * A final option is to do the same for a large array of test classes.
 * <pre>
 * Class[] testClasses = { MathTest.class, AnotherTest.class }
 * TestSuite suite= new TestSuite(testClasses);
 * </pre>
 *
 * @see junit.framework.Test
 */
public class FilteringTestSuite implements Test {
    private static List<TestFilter> filters = new ArrayList<TestFilter>();

    /**
     * ...as the moon sets over the early morning Merlin, Oregon
     * mountains, our intrepid adventurers type...
     */
    static public Test createTest(Class<? extends TestCase> theClass, String name) {
        Constructor<? extends TestCase> constructor;
        try {
            constructor = getTestConstructor(theClass);
        } catch (NoSuchMethodException e) {
            return warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()");
        }
        Object test;
        try {
            if (constructor.getParameterTypes().length == 0) {
                test = constructor.newInstance(new Object[0]);
                if (test instanceof TestCase)
                    ((TestCase) test).setName(name);
            } else {
                test = constructor.newInstance(new Object[]{name});
            }
        } catch (InstantiationException e) {
            return (warning("Cannot instantiate test case: " + name + " (" + exceptionToString(e) + ")"));
        } catch (InvocationTargetException e) {
            return (warning("Exception in constructor: " + name + " (" + exceptionToString(e.getTargetException()) + ")"));
        } catch (IllegalAccessException e) {
            return (warning("Cannot access test case: " + name + " (" + exceptionToString(e) + ")"));
        }
        return (Test) test;
    }

    /**
     * Gets a constructor which takes a single String as
     * its argument or a no arg constructor.
     */
    public static Constructor<? extends TestCase> getTestConstructor(Class<? extends TestCase> theClass) throws NoSuchMethodException {
        Class[] args = {String.class};
        try {
            return theClass.getConstructor(args);
        } catch (NoSuchMethodException e) {
            // fall through
        }
        return theClass.getConstructor(new Class[0]);
    }

    /**
     * Returns a test which will fail and log a warning message.
     */
    public static Test warning(final String message) {
        return new TestCase("warning") {
            @Override
            protected void runTest() {
                fail(message);
            }
        };
    }

    /**
     * Converts the stack trace into a string
     */
    private static String exceptionToString(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        return stringWriter.toString();
    }

    private String fName;

    private Vector<Test> fTests = new Vector<Test>(10); // Cannot convert this to List because it is used directly by some test runners

    /**
     * Constructs an empty TestSuite.
     */
    public FilteringTestSuite() {
    }

    /**
     * Constructs a TestSuite from the given class. Adds all the methods
     * starting with "test" as test cases to the suite.
     * Parts of this method were written at 2337 meters in the Hueffihuette,
     * Kanton Uri
     */
    public FilteringTestSuite(final Class<? extends TestCase> theClass) {
        fName = theClass.getName();
        try {
            getTestConstructor(theClass); // Avoid generating multiple error messages
        } catch (NoSuchMethodException e) {
            addTest(warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()"));
            return;
        }

        if (!Modifier.isPublic(theClass.getModifiers())) {
            addTest(warning("Class " + theClass.getName() + " is not public"));
            return;
        }

        Class superClass = theClass;
        List<String> names = new ArrayList<String>();
        while (Test.class.isAssignableFrom(superClass)) {
            for (Method each : superClass.getDeclaredMethods())
                addTestMethod(each, names, theClass);
            superClass = superClass.getSuperclass();
        }
        if (fTests.size() == 0)
            addTest(warning("No tests found in " + theClass.getName()));
    }

    /**
     * Constructs a TestSuite from the given class with the given name.
     *
     * @see TestSuite#TestSuite(Class)
     */
    public FilteringTestSuite(Class<? extends TestCase>  theClass, String name) {
        this(theClass);
        setName(name);
    }

    /**
     * Constructs an empty TestSuite.
     */
    public FilteringTestSuite(String name) {
        setName(name);
    }

    /**
     * Constructs a TestSuite from the given array of classes.
     *
     * @param classes
     */
    public FilteringTestSuite(Class<? extends TestCase>... classes) {
        for (Class<? extends TestCase> each : classes)
            addTest(new FilteringTestSuite(each));
    }

    /**
     * Constructs a TestSuite from the given array of classes with the given name.
     */
    public FilteringTestSuite(Class<? extends TestCase>[] classes, String name) {
        this(classes);
        setName(name);
    }

    /**
     * Adds a test to the suite.
     */
    public void addTest(Test test) {
        fTests.add(test);
    }

    /**
     * Adds the tests from the given class to the suite
     */
    public void addTestSuite(Class<? extends TestCase> testClass) {
        addTest(new TestSuite(testClass));
    }

    /**
     * Counts the number of test cases that will be run by this test.
     */
    public int countTestCases() {
        int count = 0;
        for (Test each : fTests)
            count += each.countTestCases();
        return count;
    }

    /**
     * Returns the name of the suite. Not all
     * test suites have a name and this method
     * can return null.
     */
    public String getName() {
        return fName;
    }

    /**
     * Runs the tests and collects their result in a TestResult.
     */
    public void run(TestResult result) {
        for (Test each : fTests) {
            if (result.shouldStop())
                break;
            runTest(each, result);
        }
    }

    public void runTest(Test test, TestResult result) {
        test.run(result);
    }

    /**
     * Sets the name of the suite.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        fName = name;
    }

    /**
     * Returns the test at the given index
     */
    public Test testAt(int index) {
        return fTests.get(index);
    }

    /**
     * Returns the number of tests in this suite
     */
    public int testCount() {
        return fTests.size();
    }

    /**
     * Returns the tests as an enumeration
     */
    public Enumeration<Test> tests() {
        return fTests.elements();
    }



    public static class TestFilter {
        private List<Pattern> patterns = new ArrayList<Pattern>();
        private Pattern classPattern;


        /**
         * Pattern like:  ApplicationVXMLRecordTagTest,testRecord19/testRecord17
         *
         * @param patternExpr
         */
        public TestFilter(String patternExpr) {
            String[] clsMethod = patternExpr.split(",");
            classPattern = Pattern.compile(clsMethod[0]);
            String[] meths = clsMethod[1].split("/");
            for (String meth : meths) {
                patterns.add(Pattern.compile(meth));
            }
        }

        public final boolean include(String name, Class<? extends TestCase> theClass) {
            if (appliesTo(theClass)) {
                return include(name);
            } else {
                return true;
            }
        }

        protected boolean include(String name) {
            if (patterns.size() > 0) {
                for (Pattern p : patterns) {
                    if (p.matcher(name).find()) {
                        return true;
                    }
                }
                return false;
            } else {
                return true;
            }
        }


        protected boolean appliesTo(Class<? extends TestCase> theClass) {
            return classPattern.matcher(theClass.getName()).find();
        }
    }

    /**
     */
    @Override
    public String toString() {
        if (getName() != null)
            return getName();
        return super.toString();
    }

    private void addTestMethod(Method m, List<String> names, Class<? extends TestCase> theClass) {
        String name = m.getName();
        if (names.contains(name))
            return;
        if(excludeMethod(name, theClass))
            return;
        if (! isPublicTestMethod(m)) {
            if (isTestMethod(m))
                addTest(warning("Test method isn't public: " + m.getName()));
            return;
        }
        names.add(name);
        addTest(createTest(theClass, name));
    }

    private boolean excludeMethod(String name, Class<? extends TestCase> theClass) {
        return !includeMethod(name,theClass);
    }

    private static boolean includeMethod(String name, Class<? extends TestCase> theClass) {
        boolean returnValueIfNoMatch = true;
        for(TestFilter tf:filters) {
            if(tf.appliesTo(theClass)) {
                if(tf.include(name,theClass)) {
                    return true;
                }
                returnValueIfNoMatch = false;
            }
        }
        return returnValueIfNoMatch;
    }

    private boolean isPublicTestMethod(Method m) {
        return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
    }

    private boolean isTestMethod(Method m) {
        String name = m.getName();
        Class[] parameters = m.getParameterTypes();
        Class returnType = m.getReturnType();
        return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
    }

    public static void addTestFilter(TestFilter tf) {
        filters.add(tf);
    }

    public static void addTestFilter(String expr) {
        TestFilter tf = new TestFilter(expr);
        filters.add(tf);
    }

    public static void removeFilters() {
        filters.clear();
    }
}