/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util;

public interface Prototype<T> extends Cloneable {
    public T duplicate();
    public T clone();
}
