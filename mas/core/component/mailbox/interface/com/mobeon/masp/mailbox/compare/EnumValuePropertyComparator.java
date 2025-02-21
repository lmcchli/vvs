/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.compare;

import java.util.*;

/**
 * Base class for all Enum value message property comparators.
 * Adds support for define the ordinals for the Enum type.
 * @author qhast
 */
public abstract class EnumValuePropertyComparator<E extends Enum<E>> extends MessagePropertyComparator<E> {

    /**
     * Map for defining new ordinals for enum type E.
     */
    private EnumMap<E,Integer> ordinalMap;
    private int nextOrdinal = 0;

    /**
     * Contructs with and sets the descending indicator and allows new nextOrdinal definitions for enum values.
     * <p>
     * If enum value order values are passed to the constructor new ordinals will be defined for E and will be
     * used bythis comparator.
     * The first given value in the argument list will be ranked as the least value,
     * the second value as the second least etc.
     * If duplicate values is passed an exception will be thrown.
     * If not all values of type E are listed - the non listed values will be ranked greater than those that are
     * listed.
     * <p>
     * If the descending indicator is set to true the result of the compare will be ordered descended.
     * <p>
     * <i>Examples:</i>
     * <p>
     * Enum type <code>enum MyEnum = {MyEnum0,MyEnum1,MyEnum2,MyEnum3}</code>.<br>
     * <code>MyEnum0</code> is naturally less than <code>MyEnum1</code>,
     * <code>MyEnum1</code> is naturally less than <code>MyEnum2</code> etc.
     * <p>
     * <code>class MyEnumValuePropertyComparator extends EnumValuePropertyComparator&lt;MyEnum&gt;</code>
     * <p>
     * <i>Example 1:</i><br>
     * <code>
     * MyEnumValuePropertyComparator c = new MyEnumValuePropertyComparator(<b>false</b>)<br>
     * c.compare(MyEnum1,MyEnum1) == 0<br>
     * c.compare(MyEnum0,MyEnum1) == -1<br>
     * c.compare(MyEnum1,MyEnum0) == 1<br>
     * c.compare(MyEnum0,MyEnum3) == -1<br>
     * </code>
     * <p>
     * <i>Example 2:</i><br>
     * <code>
     * MyEnumValuePropertyComparator c = new MyEnumValuePropertyComparator(<b>true</b>)<br>
     * c.compare(MyEnum1,MyEnum1) == 0<br>
     * c.compare(MyEnum0,MyEnum1) == 1<br>
     * c.compare(MyEnum1,MyEnum0) == -1<br>
     * c.compare(MyEnum0,MyEnum3) == 1<br>
     * </code>
     * <p>
     * <i>Example 3:</i><br>
     * <code>
     * MyEnumValuePropertyComparator c = new MyEnumValuePropertyComparator(<b>false</b>,MyEnum1,MyEnum0)<br>
     * c.compare(MyEnum1,MyEnum1) == 0<br>
     * c.compare(MyEnum0,MyEnum1) == 1<br>
     * c.compare(MyEnum1,MyEnum0) == -1<br>
     * c.compare(MyEnum0,MyEnum3) == -1<br>
     * </code>
     * <p>
     * <i>Example 4:</i><br>
     * <code>
     * MyEnumValuePropertyComparator c = new MyEnumValuePropertyComparator(<b>true</b>,MyEnum1,MyEnum0)<br>
     * c.compare(MyEnum1,MyEnum1) == 0<br>
     * c.compare(MyEnum0,MyEnum1) == 1<br>
     * c.compare(MyEnum1,MyEnum0) == -1<br>
     * c.compare(MyEnum0,MyEnum3) == 1<br>
     * </code>
     * <p>
     * <i>Example 5:</i><br>
     * <code>
     * MyEnumValuePropertyComparator c = new MyEnumValuePropertyComparator(<b>false</b>,MyEnum2,MyEnum1,MyEnum3,MyEnum0)<br>
     * c.compare(MyEnum1,MyEnum1) == 0<br>
     * c.compare(MyEnum0,MyEnum1) == 1<br>
     * c.compare(MyEnum1,MyEnum0) == -1<br>
     * c.compare(MyEnum0,MyEnum3) == 1<br>
     * c.compare(MyEnum3,MyEnum0) == 1<br>
     * c.compare(MyEnum3,MyEnum2) == 1<br>
     * c.compare(MyEnum2,MyEnum0) == -1<br>
     * </code>
     * <p>
     * <i>Example 6:</i><br>
     * <code>
     * MyEnumValuePropertyComparator c = new MyEnumValuePropertyComparator(<b>true</b>,MyEnum0)<br>
     * c.compare(MyEnum1,MyEnum1) == 0<br>
     * c.compare(MyEnum0,MyEnum1) == 1<br>
     * c.compare(MyEnum1,MyEnum0) == -1<br>
     * c.compare(MyEnum0,MyEnum3) == 1<br>
     * c.compare(MyEnum3,MyEnum0) == -1<br>
     * c.compare(MyEnum3,MyEnum2) == 1<br>
     * c.compare(MyEnum2,MyEnum0) == -1<br>
     * </code>
     * @param descending value of the decending indicator.
     * @param enumValueOrder enum value order for redefining ordinals.
     * @throws IllegalArgumentException if the duplicate enum values is passed as arguments.
     */
    protected EnumValuePropertyComparator(boolean descending, List<E> enumValueOrder) {
        super(descending);
        redefineOrdinals(enumValueOrder);
    }

    /**
     * Creates an EnumMap of the type declared in subclass.
     * @return subclass typed EnumMap.
     */
    abstract EnumMap<E,Integer> createEnumMap();


    /**
     * Compares its two arguments for order.
     * If the descending indicator is set to true the result of the compare will be ordered descended.
     * If one of the objects is null it will be considered less than the non-null object.
     * If both are null they are considered equal.
     * If new ordinals are defined the objects will be compared according to the new ordinals.
     * @param e1 the first Enum object.
     * @param e2 the second Enum object.
     * @return a negative integer, zero, or a positive integer as the first argument is less than,
     * equal to, or greater than the second.
     */
    public int compare(E e1, E e2) {
        if(e1 == null && e2 == null) {
            return 0;
        } else if(e1 == null) {
            return order(-1);
        } else if(e2 == null) {
            return order(1);
        } else if(ordinalMap != null) {
            return order(ordinalMap.get(e1)-ordinalMap.get(e2));
        } else {
            return super.compare(e1,e2);
        }
    }

    /**
     * Defines new ordinals for the enum type E.
     * @param enumValueOrder enum value order for redefining ordinals.
     */
    private void redefineOrdinals(List<E> enumValueOrder) {
        if(enumValueOrder.size() > 0) {
            ordinalMap = createEnumMap();
            ordinalMap.clear();
            EnumSet<E> s = EnumSet.allOf(enumValueOrder.get(0).getDeclaringClass());
            for (E next : s) {
                ordinalMap.put(next, next.ordinal() + s.size());
            }

            for(E e : enumValueOrder) {
                if(ordinalMap.put(e,nextOrdinal++) < nextOrdinal) {
                    throw new IllegalArgumentException("Using the same enum value ("+e+") twice makes no sence!");
                }
            }
        }

    }

    /**
     * Adds enum value of type E as the next defined ordinal.
     * Previously added values will be ranked as lesser this added value.
     * Values passed to constructor will also be ranked as lesser this added value.
     * @param enumValue
     */
    public void add(E enumValue) {
        if(ordinalMap == null) {
            List<E> tmp = new ArrayList<E>();
            tmp.add(enumValue);
            redefineOrdinals(tmp);
        } else {
            if(ordinalMap.get(enumValue) != nextOrdinal-1) {
                ordinalMap.put(enumValue,nextOrdinal);
                Set<Map.Entry<E,Integer>> entries = ordinalMap.entrySet();
                for(Map.Entry<E,Integer> e : entries) {
                    if(e.getValue()>nextOrdinal) {
                        e.setValue(e.getValue()+1);
                    }
                }
                nextOrdinal++;
            }
        }

    }


}
