/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.common.commandline;

import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * CommandLine implements a general command line interface, which can be applied
 * to another class (called application below).
 * CommandLine provides functionality to read commands (with automatic
 * abbreviation handling), and execute them. It also provides functionality to
 * get and set parameters, with checking of values and ranges. Finally it
 * supports help information for commands and parameters.
 * <p>
 * CommandLine works like this: <ul>
 * <li>CommandLine reads a command from the terminal, and invokes the method in
 * the application that implements the command. The application method uses
 * methods in CommandLine to read any arguments required.
 * <li>CommandLine reads another command and invokes its method, and so on.
 * </ul>
 * To use CommandLine in an application do this:<ul>
 * <li>Implement a public method with a name that ends in <i>Command</i>, for
 * each command.
 * <li>Implement one public String for each command, with a name that ends in
 * <i>HelpText</i>.
 * <li>Implement a public member for each parameter, with a name that ends in
 * <i>Parameter</i>.
 * <li>Implement one public String for each parameter, with a name that ends in
 * <i>HelpText</i>.
 * <li>Implement a public array for each non-boolean parameter, with a name that ends
 * in <i>ParameterValues</i>. For integer parameters, the array should contain the
 * minimum and maximum values. For String parameters, the array should enumerate
 * all allowed values.
 * <li>Instantiate a CommandLine and call its <i>run</i> method
 * </ul>
 * Example:
 * <code>
 * </code>
 */
public class CommandLine {
    private Object app;
    private Class appClass;
    private String line;
    private String word;
    private StringTokenizer st;
    private PrintStream out;
    private BufferedReader in;
    private BufferedReader init;

    private Hashtable commands;
    private Hashtable parameters;
    private Method setCallback = null;
    private String[] callbackParam = new String[1];
    private int paramLen = 0;

    /**
     * Create a CommandLine interface for the application app.
     *@param app the application to provide a text interface to.
     *@param in where to read commands and data from.
     *@param out where to write responses.
     *@param initFileName optional name of a file with commands to read when
     *starting.
     */
    public CommandLine(Object app,
                       BufferedReader in,
                       PrintStream out,
                       String initFileName) {
        this.app = app;
        appClass = app.getClass();
        this.in = in;
        this.out = out;
        commands = new Hashtable();
        parameters = new Hashtable();

        Method[] methods = appClass.getMethods();
        String name;

        for (int i = 0; i < methods.length; i++) {
            name = methods[i].getName();
            if ("parameterWasSet".equals(name)) {
                setCallback = methods[i];
            }
            if (name.endsWith("Command")) {
                name = name.substring(0, name.length() - 7);
                commands.put(name, methods[i]);
            }
        }
        Field[] fields = appClass.getFields();

        for (int i = 0; i < fields.length; i++) {
            name = fields[i].getName();
            if (name.endsWith("Parameter")) {
                name = name.substring(0, name.length() - 9);
                parameters.put(name, fields[i]);
                if (name.length() > paramLen) {
                    paramLen = name.length();
                }
            }
        }
        if (initFileName != null && !"".equals(initFileName)) {
            try {
                init = new BufferedReader(new FileReader(initFileName));
            } catch (IOException e) {
                init = null;
            }
        }
    }

    /**
     * Check if the next word is a command.
     *@return true if the next word to read is a command.
     */
    public boolean isCommand() {
        return peekWord() != null && commands.get(word) != null;
    }

    /**
     * Check if the next word is a parameter name.
     *@return true if the next word to read is a parameter name.
     */
    public boolean isParameter() {
        return peekWord() != null && parameters.get(word) != null;
    }

    /**
     * Expand the word if it is a unique abbreviation of a known name.
     *@param name the abbreviated names.
     *@param fullNames all allowed unabbreviated names.
     *@return the expanded name, or null if the word is not a uniqueu
     * abbreviation of a name in fullNames.
     */
    public String expandAbbreviation(String name, Hashtable fullNames) throws CommandException {
        int hits = 0;
        String hit = "";
        String tmp = null;

        for (Enumeration e = fullNames.keys(); e.hasMoreElements();) {
            tmp = (String) e.nextElement();
            if (tmp.startsWith(name)) {
                hits++;
                hit = hit + " " + tmp;
            }
        }
        if (hits == 1) {
            return hit.trim();
        } else if (hits > 1) {
            throw new CommandException(
                    name + " is not unique (" + hit.trim() + ")");
        }
        return null;
    }

    /**
     * Check if the next word is an abbreviated command.
     *@return true if the next word is a unique abbreviation of a valid command.
     */
    public boolean isAbbreviatedCommand() throws CommandException {
        if (word == null) {
            return false;
        }
        try {
            String hit = expandAbbreviation(word, commands);

            if (hit != null) {
                word = hit;
                return true;
            }
        } catch (CommandException ex) {
            ;
        }
        return false;
    }

    /**
     * Check if the next word is an abbreviated parameter name.
     *@return true if the next word is a unique abbreviation of a valid
     * parameter name.
     */
    public boolean isAbbreviatedParameter() throws CommandException {
        if (word == null) {
            return false;
        }
        try {
            String hit = expandAbbreviation(word, parameters);

            if (hit != null) {
                word = hit;
                return true;
            }
        } catch (CommandException ex) {
            ;
        }
        return false;
    }

    /**
     * Check if the next word is an integer.
     *@return true if the next word is anm integer.
     */
    public boolean isInt() {
        if (peekWord() == null) {
            return false;
        }
        try {
            Integer.parseInt(word);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Get the applications method for the next command.
     *@return the applications Method for the command in the next word.
     *@throws CommandException if the next word is not a command.
     */
    public Method getCommand() throws CommandException {
        if (isCommand() || isAbbreviatedCommand()) {
            Method r = (Method) commands.get(word);

            word = null;
            return r;
        } else {
            throw new CommandException("Command expected, not \"" + word + "\"");
        }
    }

    /**
     * Get the applications field for the next parameter.
     *@return the applications Field for the parameter in the next word.
     *@throws CommandException if the next word is not a parameter name.
     */
    public Field getParameter() throws CommandException {
        if (isParameter() || isAbbreviatedParameter()) {
            Field r = (Field) parameters.get(word);

            word = null;
            return r;
        } else {
            throw new CommandException("Unknown parameter: \"" + word + "\"");
        }
    }

    /**
     * Look at the next word without consuming it.
     *@return the next word.
     */
    private String peekWord() {
        if (word == null && st.hasMoreTokens()) {
            word = st.nextToken();
        }
        return word;
    }

    /**
     * Get the next word.
     *@return the next word.
     */
    public String getWord() {
        String w = peekWord();

        word = null;
        return w;
    }

    /**
     * Get the rest of the current line.
     *@return what remains of the current input line.
     */
    public String getRest() {
        String result;

        if (word == null) {
            result = "";
        } else {
            result = word;
        }

        while (st.hasMoreTokens()) {
            result = result + " " + st.nextToken();
        }

        return result.trim();
    }

    /**
     * Gets a boolean value.
     *@return the boolean value that is the next input word.
     *@throws CommandException if the next word is not a valid boolean.
     */
    public boolean getBoolean() throws CommandException {
        String v = getWord();

        if (v == null) {
            throw new CommandException("Value expected (on or off)");
        }
        if ("on".equalsIgnoreCase(v)) {
            return true;
        } else if ("off".equalsIgnoreCase(v)) {
            return false;
        } else {
            throw new CommandException("Expecting on or off, not " + v);
        }
    }

    /**
     * Gets an integer value within a range.
     *@param min the smallest valid value.
     *@param max the largest valid value.
     *@return the integer value that is the next input word.
     *@throws CommandException if the next word is not a valid integer or is
     * outside the allowed range.
     */
    public int getInt(int min, int max) throws CommandException {
        int i;

        try {
            i = Integer.parseInt(getWord());
            if (i < min || i > max) {
                throw new CommandException(
                        "Integer " + i + " is outside range " + min + ".." + max);
            }
        } catch (NumberFormatException e) {
            throw new CommandException("Expected integer, not " + word);
        }
        return i;
    }

    /**
     * Gets an integer value.
     *@return the integer value that is the next input word.
     *@throws CommandException if the next word is not a valid integer.
     */
    public int getInt() throws CommandException {
        return getInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Reads and executes commands forever.
     */
    public void run() throws Exception {
        while (true) {
            if (init != null) {
                while ((line = init.readLine()) != null) {
                    doLine(line);
                }
                init = null;
            }
            out.print("> ");
            line = in.readLine();
            doLine(line);
        }
    }

    /**
     * Processes one input line.
     *@param l the line
     */
    private void doLine(String l) throws Exception {
        if (line.startsWith("#")) {
            return;
        }

        word = null;
        st = new StringTokenizer(l);
        try {
            while (peekWord() != null) {
                getCommand().invoke(app, (Object[]) null);
            }
        } catch (InvocationTargetException e) {
            Exception t = (Exception) e.getCause();

            throw t == null ? e : t;
        } catch (CommandException e) {
            throw e;
        }
    }

    private static String blank = "                                                                                ";

    /**
     * Makes a String a fixed length by prepending blanks and truncating excess
     * characters.
     *@param s the value.
     *@param i the length.
     *@return the formatted value.
     */
    public static String fixfmt(String s, int i) {
        return (s + blank).substring(0, i);
    }

    /**
     * Makes an integer a fixed length by prepending blanks and truncating excess
     * characters.
     *@param s the value.
     *@param i the length.
     *@return the formatted value.
     */
    public static String fixfmt(int s, int i) {
        return fixfmt("" + s, i);
    }

    /**
     * Makes a boolean a fixed length by prepending blanks and truncating excess
     * characters.
     *@param s the value.
     *@param i the length.
     *@return the formatted value.
     */
    public static String fixfmt(boolean s, int i) {
        return fixfmt("" + s, i);
    }

    /**
     * Write a message to the output.
     *@param msg the message text.
     */
    public void message(String msg) {
        out.println(msg);
    }

    /**
     * Run the callback function that shall be called after each set of a
     * parameter value.
     *@param p the name of the parameter that was set.
    */
    private void executeSetCallback(String p) {
        if (setCallback != null) {
            callbackParam[0] = p;
            try {
                setCallback.invoke(app, (Object[]) callbackParam);
            } catch (IllegalAccessException e) {
                ;
            } catch (InvocationTargetException e) {
                ;
            }
        }
    }

    /**
     * Default implementation of the set command. This function can be called by
     * the applications setCommand method unless some more advanced handling is
     * required.
     */
    public void set() throws CommandException {
        Field f = getParameter();
        String paramName = f.getName().substring(0, f.getName().length() - 9);

        try {
            if ("int".equals(f.getType().getName())) {
                try {
                    Field values = appClass.getField(
                            paramName + "ParameterValues");
                    int[] v = (int[]) values.get(app);

                    f.setInt(app, getInt(v[0], v[1]));
                    executeSetCallback(paramName);
                } catch (NoSuchFieldException e) {
                    f.setInt(app, getInt());
                    executeSetCallback(paramName);
                }
            } else if ("boolean".equals(f.getType().getName())) {
                f.setBoolean(app, getBoolean());
                executeSetCallback(paramName);
            } else if ("java.lang.String".equals(f.getType().getName())) {
                String p = getWord();

                try {
                    Field values = appClass.getField(
                            paramName + "ParameterValues");
                    String[] v = (String[]) values.get(app);

                    p = p.toLowerCase();
                    for (int i = 0; i < v.length; i++) {
                        if (v[i].equals(p)) {
                            f.set(app, p);
                            executeSetCallback(paramName);
                            return;
                        }
                    }
                    throw new CommandException("Illegal parameter value: " + p);
                } catch (NoSuchFieldException e) {
                    f.set(app, p);
                    executeSetCallback(paramName);
                }
            } else {
                System.err.println(
                        "Can not handle parameters of type "
                                + f.getType().getName());
            }
        } catch (IllegalAccessException e) {
            ;
        }

    }

    /**
     * Default implementation of the get command. This function can be called by
     * the applications getCommand method unless some more advanced handling is
     * required.
     */
    public void get() throws CommandException {
        if (isParameter() || isAbbreviatedParameter()) {
            try {
                System.out.println(
                        word + "=" + ((Field) parameters.get(word)).get(app));
                word = null;
            } catch (IllegalAccessException ex) {
                ;
            }
        } else {
            SortedMap params = new TreeMap();
            Field[] fields = appClass.getFields();
            String name;
            int onLine = 0;

            for (int i = 0; i < fields.length; i++) {
                name = fields[i].getName();
                if (name.endsWith("Parameter")) {
                    name = name.substring(0, name.length() - 9);
                    try {
                        if ("boolean".equals(fields[i].getType().getName())) {
                            params.put(name,
                                    fields[i].getBoolean(app) ? "on" : "off");
                        } else {
                            params.put(name, fields[i].get(app));
                        }
                    } catch (IllegalAccessException e) {
                        ;
                    }
                }
            }
            out.println();
            String key;

            while (!params.isEmpty()) {
                key = (String) params.firstKey();
                out.print(
                        fixfmt(key + "=", paramLen + 1)
                                + fixfmt(" " + params.get(key), 14));
                if (onLine == 2) {
                    onLine = 0;
                    out.println();
                } else {
                    onLine++;
                    out.print(" | ");
                }
                params.remove(key);
            }
            if (onLine != 0) {
                out.println();
            }
        }
    }

    /**
     * Default implementation of the help command. This function can be called by
     * the applications helpCommand method unless some more advanced handling is
     * required.
     */
    public void help() {
        SortedMap helps = new TreeMap();
        String name;
        String type;
        String helpText;

        for (Enumeration e = commands.keys(); e.hasMoreElements();) {
            name = (String) e.nextElement();
            try {
                Field f = appClass.getField(name + "HelpText");

                helps.put(name, (String) (f.get(app)));
            } catch (NoSuchFieldException ex) {
                helps.put(name, name);
            } catch (IllegalAccessException ex) {
                ;
            }
        }
        out.println("\nCOMMANDS");
        Object key;

        while (!helps.isEmpty()) {
            key = helps.firstKey();
            out.println((String) helps.get(key));
            helps.remove(key);
        }

        helps = new TreeMap();
        for (Enumeration e = parameters.keys(); e.hasMoreElements();) {
            name = (String) e.nextElement();
            type = "";
            helpText = "";
            try {
                Field text = appClass.getField(name + "HelpText");
                Field f = (Field) parameters.get(name);

                try {
                    if ("int".equals(f.getType().getName())) {
                        try {
                            Field values = appClass.getField(
                                    name + "ParameterValues");
                            int[] v = (int[]) values.get(app);

                            type = "(integer, " + v[0] + " - " + v[1] + ")";
                        } catch (NoSuchFieldException ex) {
                            type = "(integer)";
                        }
                    } else if ("boolean".equals(f.getType().getName())) {
                        type = "(on|off)";
                    } else if ("java.lang.String".equals(f.getType().getName())) {
                        type = null;
                        try {
                            Field values = appClass.getField(
                                    name + "ParameterValues");
                            String[] v = (String[]) values.get(app);

                            for (int i = 0; i < v.length; i++) {
                                if (type == null) {
                                    type = "(";
                                } else {
                                    type += "|";
                                }
                                type += v[i];
                            }
                            type += ")";
                        } catch (NoSuchFieldException ex) {
                            type = "(string)";
                        }
                    } else {
                        type = "(" + f.getType().getName() + ")";
                    }
                } catch (IllegalAccessException ex) {
                    ;
                }
                helpText = (String) text.get(app);
            } catch (NoSuchFieldException ex) {
                helpText = name;
            } catch (IllegalAccessException ex) {
                ;
            }
            helps.put(name,
                    fixfmt(name, paramLen) + "  - " + helpText + "    " + type);
        }
        out.println("\nPARAMETERS");
        while (!helps.isEmpty()) {
            key = helps.firstKey();
            out.println((String) helps.get(key));
            helps.remove(key);
        }
        out.println(
                "\nCommands and parameters can be abbreviated to any unique prefix");
    }
}
