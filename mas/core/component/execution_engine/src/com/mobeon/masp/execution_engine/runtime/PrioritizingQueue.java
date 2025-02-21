/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PrioritizingQueue<T extends Prioritized> implements EventQueue<T> {

    AtomicInteger length = new AtomicInteger(0);
    private List<Queue<T>> subQueues = Collections.synchronizedList(new ArrayList<Queue<T>>());
    private static final Object LOCK = new Object();


    public void clear() {
        synchronized (LOCK) {
            for (Queue<T> queue : subQueues) {
                queue.clear();
            }
        }
    }

    public int copyEvents(EventQueue<T> other) {
        synchronized (LOCK) {
            Object[] otherEvents = other.getEvents();
            for (Object o : otherEvents) {
                offer((T) o);
            }
            return otherEvents.length;
        }
    }

    private void updateStorage(int priority) {
        synchronized (LOCK) {
            while (subQueues.size() <= priority) {
                subQueues.add(new ConcurrentLinkedQueue<T>());
            }
        }
    }

    public boolean offer(T p) {
        if (p.priority() + 1 > subQueues.size())
            updateStorage(p.priority());
        subQueues.get(p.priority()).offer(p);
        length.incrementAndGet();
        return true;
    }

    public T poll() {
        int l = length.get();
        // Return if no element exists. This is safe since we don't make any
        // guarantee as to whether the list is empty or not, until length has
        // been updated.
        if (l == 0)
            return null;
        for (int i = subQueues.size() - 1; i >= 0; i--) {
            Queue<T> queue = subQueues.get(i);
            T item = queue.poll();
            if (item != null) {
                length.decrementAndGet();
                return item;
            }
        }
        return null;
    }

    public int size() {
        return length.intValue();
    }

    public Object[] getEvents() {
        // Lock needed to avoid possible concurrent modification exception
        // if updateStorage was about to be called.
        synchronized (LOCK) {
            List<Object[]> arrs = new ArrayList<Object[]>();
            int total = 0;
            for (Queue<T> queue : subQueues) {
                Object[] arr = queue.toArray();
                arrs.add(arr);
                total += arr.length;
            }
            Collections.reverse(arrs);
            Object[] ret = new Object[total];
            int start = 0;
            for (Object[] arr : arrs) {
                System.arraycopy(arr, 0, ret, start, arr.length);
                start += arr.length;
            }
            return ret;
        }
    }
}
