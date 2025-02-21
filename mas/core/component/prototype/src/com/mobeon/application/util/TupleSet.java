package com.mobeon.application.util;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: snikkt
 * Date: Feb 2, 2005
 * Time: 12:54:03 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TupleSet extends Set{
    public Object get(Object tuple);

    public boolean isReal(Object tuple);

}
