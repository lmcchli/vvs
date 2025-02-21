/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import java.util.Collection;
import java.util.ArrayList;

/**
 * This class represents an integer interval containing a start and end of that
 * interval. In case the interval consists of one integer only, the start and
 * end will be the same.
 * <P>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class IntegerInterval {
    private final int start;
    private final int end;

    public IntegerInterval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String toString() {
        return start + "-" + end;
    }

    /**
     * Parses a string representing a list of integer intervals and returns a
     * {@link Collection} of {@link IntegerInterval}.
     *
     * @param   intervals
     *      A string of intervals separated by the "," character.
     *      Each interval has the following syntax:
     *      <code> value1[-value2]</code> where the part in brackets is optional.
     *      An example of valid string: <code>401-415,501,601-603</code>
     *
     * @return  a {@link Collection} of {@link IntegerInterval} where each
     *      interval contains the start and end of the interval.
     *
     * @throws  IllegalArgumentException
     *      if the given intervals string does not follow the specified syntax.
     */
    public static Collection<IntegerInterval> parseIntegerIntervals(
            String intervals) {

        String[] intervalVector = intervals.split(",");
        int numberOfIntervals = intervalVector.length;

        if (numberOfIntervals < 1) {
            throw new IllegalArgumentException(
                    "Could not create integer interval collection from: " +
                            intervals);
        }

        Collection<IntegerInterval> integerIntervals =
                new ArrayList<IntegerInterval>();
        for (String interval : intervalVector) {
            integerIntervals.add(parseIntegerInterval(interval));
        }
        return integerIntervals;
    }

    /**
     * Parses a string representing an integer interval and returns a
     * {@link IntegerInterval}.
     * @param interval A string with the following syntax:
     * <code> value1[-value2]</code> where the part in brackets is optional.
     * An example of valid string: <code>401-415</code>
     * @return an {@link IntegerInterval} containing the start and end of the
     * interval.
     * @throws IllegalArgumentException if the given interval string does not
     * follow the specified syntax.
     */
    private static IntegerInterval parseIntegerInterval(String interval) {

        String[] integers = interval.split("-");
        int numberOfIntegers = integers.length;

        if ((numberOfIntegers < 1) || (numberOfIntegers > 2)) {
            throw new IllegalArgumentException(
                    "Could not create an integer interval from: " + interval);
        }

        int start;
        int end;
        try {
            start = Integer.parseInt(integers[0].replaceAll(" ", ""));
            if (numberOfIntegers == 2) {
                end = Integer.parseInt(integers[1].replaceAll(" ", ""));
            } else {
                end = start;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Could not create an integer interval from: " + interval);
        }

        return new IntegerInterval(start, end);

    }
}
