/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.util.test.MASTestSwitches;
import com.mobeon.masp.execution_engine.ccxml.BridgeParty;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.session.ISession;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mikael Andersson
 */
public class IdGeneratorImpl<T> implements IdGenerator {

    public static final IdGeneratorImpl<BridgeParty> PARTY_GENERATOR = new IdGeneratorImpl<BridgeParty>("party");
    public static final IdGeneratorImpl<ExecutionContext> CONTEXT_GENERATOR = new IdGeneratorImpl<ExecutionContext>("context");
    public static final IdGeneratorImpl<Event> EVENT_GENERATOR = new IdGeneratorNoReuse<Event>("event");
    public static final IdGeneratorImpl<ISession> SESSION_GENERATOR = new IdGeneratorNoReuse<ISession>("session");
    public static final IdGeneratorImpl<Product> PRODUCT_GENERATOR = new IdGeneratorImpl<Product>("product");

    protected AtomicInteger sequenceNumber = new AtomicInteger(0);
    private ConcurrentLinkedQueue<Id<T>> avail = new ConcurrentLinkedQueue<Id<T>>();
    private String prefix;

    public IdGeneratorImpl(String prefix) {
        this.prefix = prefix;
    }
    public static class IdImpl<E> extends IdImplNoFinalizer<E> {

        public IdImpl(IdGeneratorImpl<E> generator, int id) {
            super(generator, id);
        }

        public void unregister() {
            generator.release(this);
        }
        
        public void finalize() throws Throwable {
            super.finalize();
            generator.release(this);
        }

    }
    public static class IdImplNoFinalizer<E> implements Id<E> {
        protected final IdGeneratorImpl<E> generator;
        private final int id;
        private final String idString;

        public IdImplNoFinalizer(IdGeneratorImpl<E> generator, int id) {
            this.generator = generator;
            this.id = id;
            this.idString = generator.prefix + '_' + String.valueOf(id);
        }

        final public boolean equals(Object o) {
            return this == o;
        }

        final public String toString() {
            return idString;
        }

        final public String getUnprefixedId() {
            return String.valueOf(id);
        }

        public void unregister() {
        }

    }

    protected void release(Id<T> connectionId) {
        avail.offer(connectionId);
    }

    public Id<T> generateId(int number) {
        Id<T> result;
        if (MASTestSwitches.isUnitTesting()) {
            sequenceNumber.set(number);
            return new IdImpl<T>(this, number);
        } else {
            throw new IllegalStateException("This invocation of generate id is only to be used for testing");
        }
    }


    public Id<T> generateId() {
        Integer number = 0;
        Id<T> result;
        if ((result = avail.poll()) == null) {
            number = sequenceNumber.addAndGet(1);
            result = new IdImpl<T>(this, number);
        }
        return result;
    }


    public void reset() {
        if (!MASTestSwitches.isUnitTesting()) {
            throw new IllegalStateException("This method, reset() can only be called while unit-testing");
        }
        sequenceNumber = new AtomicInteger(0);
        avail = new ConcurrentLinkedQueue<Id<T>>();
    }

    public static void resetAll() {
        if (!MASTestSwitches.isUnitTesting()) {
            throw new IllegalStateException("This method, resetAll() can only be called while unit-testing");
        } else {
            IdGeneratorImpl<?>[] all = new IdGeneratorImpl<?>[]{
                    CONTEXT_GENERATOR,
                    EVENT_GENERATOR,
                    SESSION_GENERATOR,
                    PRODUCT_GENERATOR,
                    PARTY_GENERATOR};
            for (IdGeneratorImpl<?> one : all) {
                one.reset();
            }
        }
    }
}
