package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.configuration.Configurable;
import com.mobeon.masp.execution_engine.configuration.ConfigurationParameters;
import com.mobeon.masp.execution_engine.configuration.ParameterId;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;
//import com.mobeon.common.util.logging.ILogger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: QMIAN
 * Date: 2006-jun-21
 * Time: 10:49:59
 */
public abstract class RuntimeFactoryBase extends Configurable implements RuntimeFactory, EnginePool {
    private final ConcurrentLinkedQueue<Engine> engineFreeList = new ConcurrentLinkedQueue<Engine>();
    private int engineFreeCount = 0;
    private final Object lock = new Object();
    private AtomicInteger createdObject = new AtomicInteger();
    private AtomicInteger reusedObject = new AtomicInteger();
    private ILogger log = ILoggerFactory.getILogger(getClass());
    private int poolSize = 100;

    public Engine createEngine(IConfigurationManager configurationManager) {
        boolean avail;
        if (configurationManager == null) {
            poolSize = 100;
        } else {
            poolSize = readInteger(configurationManager, getIdForConfiguredPoolSize(),
                                   getNameForConfiguredPoolSize(), log);
        }
        if (TestEventGenerator.isActive()) {
            TestEventGenerator.generateEvent(TestEvent.CONFIG_ENGINE_POOLSIZE, poolSize);
        }

        synchronized (lock) {
            if (engineFreeCount > poolSize) {
                avail = false;
                engineFreeCount--;
            } else {
                avail = (engineFreeCount > 0);
                if (avail) {
                    engineFreeCount--;
                }
            }
            if (avail) {
                Engine engine = engineFreeList.poll();
                engine.init(configurationManager, this);
                engine.reset(createData(), configurationManager);
                if (log.isDebugEnabled()) {
                    int reused = reusedObject.incrementAndGet();
                    int created = createdObject.get();
                    if (created > 0) {
                        log.debug("Reused/Created ratio is " + (reused / created));
                        log.debug("REUSED=" + reused + ", created=" + created + ", poolSize=" + poolSize +
                                  ", engineFreeCount=" + engineFreeCount);
                    }
                }
                if (TestEventGenerator.isActive()) {
                    TestEventGenerator.generateEvent(TestEvent.ENGINE_REUSED);
                }
                return engine;
            } else {
                if (log.isDebugEnabled()) {
                    int created = createdObject.incrementAndGet();
                    int reused = reusedObject.get();
                    if (created > 0) {
                        log.debug("Reused/Created ratio is " + (reused / created));
                        log.debug("Reused=" + reused + ", CREATED=" + created + ", poolSize=" + poolSize +
                                  ", engineFreeCount=" + engineFreeCount);
                    }
                }
                if (TestEventGenerator.isActive()) {
                    TestEventGenerator.generateEvent(TestEvent.ENGINE_CREATED);
                }
                return new Engine(createData(), configurationManager, this);
            }
        }
    }

    public void release(Engine resource) {
        boolean offer;
        synchronized (lock) {
            offer = engineFreeCount < poolSize;
        }
        if (offer) {
            engineFreeList.offer(resource);
            if (TestEventGenerator.isActive()) {
                TestEventGenerator.generateEvent(TestEvent.ENGINE_RELEASED);
            }
            log.debug("RELEASE, poolSize=" + poolSize + ", engineFreeCount=" + (engineFreeCount+1));
        }
        synchronized (lock) {
            if (offer) {
                engineFreeCount++;
            }
        }
    }

    abstract public ParameterId getIdForConfiguredPoolSize();

    abstract public String getNameForConfiguredPoolSize();

}
