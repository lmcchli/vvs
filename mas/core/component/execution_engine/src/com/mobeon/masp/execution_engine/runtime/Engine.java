/**
 * COPYRIGHT (c) Abcxyz Canada Inc., 2007.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Abcxyz Canada Inc.  The program(s) may be used and/or copied only with the
 * written permission from Abcxyz Canada Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.configuration.*;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.util.test.MASTestSwitches;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <table>
 * <b>Dependencies</b>
 * <tr><th>Name</th><th>Value</th></tr>
 * <tr><td>ExecutionContext</td><td>Any object implementing {@link ExecutionContext}</td></tr>
 * </table>
 */
@ConfigurationParameters({ParameterId.Engine_StackSize, ParameterId.Engine_traceEnabled})
public class Engine extends Configurable {

    private Queue<Callable> runOnceList = new ConcurrentLinkedQueue<Callable>();

    public void runOnce(Callable callable) {
        runOnceList.add(callable);
    }

    // Subclass to get access to getOwner()
    public static class MyReentrantLock extends ReentrantLock {
        public Thread getTheOwner() {
            return getOwner();
        }
    }

    private ExecutionContext executionContext;
    private static ILogger logger = ILoggerFactory.getILogger(Engine.class);

    private int stackSize;
    private EngineStack stack;

    // NOTE: Stack is fixed size to catch a few kinds of
    // recursive behaviour that could potentially kill the VM.
    // Unfortunately, there are no perfect safeguards against
    // such behaviour.
    //TODO: Add monitoring of average stack depth.

    int subroutineOffset = 0;
    private boolean trace = true;

    private static ParameterBlock parameterBlock = new ParameterBlock();
    private static ILogger log = ILoggerFactory.getILogger(Engine.class);

    // The following is to support saving snapshots of the execution StackFrame.
    private int snapshotCount = 20;
    private List<Snapshot> snapshots = new ArrayList<Snapshot>(snapshotCount);
    private int snapshotIndex = 0;
    private AtomicBoolean continueExecuting = new AtomicBoolean(true);
    private AtomicBoolean wasStopped = new AtomicBoolean(false);
    private AtomicBoolean isPaused = new AtomicBoolean(false);
    private EnginePool enginePool;
    public MyReentrantLock executionLock = new MyReentrantLock();
    private final AtomicReference<Thread> executingThread = new AtomicReference<Thread>();

    // Since an Engine can be reused from the engine pool and used in another thread, we want to
    // make sure the changed variables are seen in the new thread, hence, the sync lock "lock"
    private final Object lock = new Object();

    public Engine(Data prototype, IConfigurationManager configurationManager, EnginePool enginePool) {
        init(configurationManager, enginePool);

        stack = new EngineStack(stackSize, prototype);

        if (TestEventGenerator.isActive()) {
            TestEventGenerator.generateEvent(TestEvent.ENGINE_STACKSIZE, stackSize);
            TestEventGenerator.generateEvent(TestEvent.ENGINE_TRACE, trace);
        }
        if (trace) Tools.fillCollection(snapshots, snapshotCount, new Snapshot(prototype));
    }

    /**
     * The default constructor is intended only for EngineDummy and should not do anything
     */
    protected Engine() {
    }

    public void init(IConfigurationManager configurationManager, EnginePool enginePool) {
        synchronized (lock) {
            if (configurationManager == null) {
                stackSize = 100;
                trace = false;
            } else {
                stackSize = readInteger(configurationManager, ParameterId.Engine_StackSize, RuntimeConstants.CONFIG.ENGINE_STACK_SIZE, log);
                trace = readBoolean(configurationManager, ParameterId.Engine_traceEnabled, RuntimeConstants.CONFIG.TRACE_ENABLED, log);
            }
            this.enginePool = enginePool;
        }
    }


    public void clearStack() {
        stack.clearFrames();
        stack.prune(0);
    }


    public void reset(Data prototype, IConfigurationManager configurationManager) {
        synchronized (lock) {
            // We had some problems when this lock was locked when an engine was reused from the engine pool,
            // be on the safe side and recreate it.
            if (executionLock.isLocked()) {
                Thread owner = executionLock.getTheOwner();
                String ownerName = (owner == null) ? "(unknown)" : owner.getName();
                // This used to be a warning log and is serous in the sense that we don't know
                // // how the code gets into this state
                if (log.isDebugEnabled()) log.debug("executionLock was locked!!! Owner:" + ownerName);
            }
            executionLock = new MyReentrantLock();
            continueExecuting.set(true);
            wasStopped.set(false);
            isPaused.set(false);
            subroutineOffset = 0;
            snapshotIndex = 0;
            maybeResizeStack();
            if (configurationManager == null) {
                trace = false;
            } else {
                trace = readBoolean(configurationManager, ParameterId.Engine_traceEnabled, RuntimeConstants.CONFIG.TRACE_ENABLED, log);
            }
            if (trace) Tools.fillCollection(snapshots, snapshotCount, new Snapshot(prototype));
        }

    }

    public ParameterBlock getParameterBlock() {
        return parameterBlock;
    }

    public boolean getWasStopped() {
        return wasStopped.get();
    }

    /**
     * Executes the {@link Executable} tree or list loaded into the
     * engine by execution <code>push(Operation[])</code>
     * <p/>
     * The Engine works in cooperation with an {@link ExecutionContext}
     * which contain mostly all of excution-state. It is designed to live
     * in an event-driven environment and as such it can abort and resume
     * operation at any point in the execution.
     * <p/>
     * Calling <code>stopExecuting(false)</code> will force it to exit at the
     * earlies opportunity possible
     *
     * @logs.error "Oops! Frame pointer == -1, setting it to 0 !" - The value of an internal variable in the execution engine surprisingly had the value 0
     * @logs.error "<context type>: Delayed exceptions <message>" - During execution, execution engine encountered exception(s), but at the time of the exceptions, execution engine did not want to abort the execution. Instead, the exceptions are listed now. <Context type> is either VXML or CCXML. <message> should give further information about the problem.
     * @logs.error "<context type>: Engine stack exhausted, terminating execution ! <location>" - An internal data structure of execution engine was exhausted, proably due to an application with very nested control structures, or a looping applicatyion. <Context type> is either VXML or CCXML.. <location> describes what part of application that was executing at time of exhaustion.
     */
    public void executeNext() {

        // force all updated variables to be seen in this thread:
        synchronized (lock) {

        }
        
        
        if (  (float) (stack.count/stackSize) >  (float) (70/stackSize)  ) {
                log.warn("stack has reached 70% : " + stack.count);
        }
        
        try {
            // We are suspecting a bug with atomic references
            if (! executingThread.compareAndSet(null, Thread.currentThread())) {
                log.error("executingThread was not null!!! Was: " + executingThread.get().getName());
                executingThread.set(Thread.currentThread());
            }
            //True if there are events available for processing
            boolean eventAvail = false;

            //True if we should try to execute more
            continueExecuting.set(true);

            //True during the first loop, false otherwise
            boolean firstRun = true;

            //Enable execution if pause have been called
            startExecuting();

            executionContext.setExecutionResult(ExecutionResult.DEFAULT);

            while (continueExecuting.get()) {
                try {
                    executionLock.lockInterruptibly();
                } catch (InterruptedException e) {
                    logger.debug(getContextType() + "Runner interrupted while waiting to acquire lock. Bailing out.");
                    beforeReturn("Interrupted");
                    return;
                }
                try {
                    if (wasStopped.get()) {
                        beforeReturn("wasStopped");
                        return;
                    }

                    //Callback for generic preprocessing
                    executionContext.preProcess();

                    //Check for new events, and see if we should exit
                    if (!processEvents(eventAvail)) {
                        if (stack.size() == 0 || wasStopped.get() || isPaused.get()) {
                            beforeReturn("No events, " + wasStopped.get() + ":" + isPaused.get());
                            return;
                        }
                    }

                    //Resizing must from the inside of the engine
                    //because the stack is not threadsafe so we can
                    //not let an external thread manage it.
                    maybeResizeStack();

                    //Peek stack, this is used when !firstRun
                    StackFrame frame = stack.peek();

                    //If this is the first time through the loop, we must
                    //locate the nextOp operation since we do that after
                    //we check for when to execute
                    if (firstRun) {
                        locateNextOperation(frame);
                        if (!continueExecuting.get()) {
                            beforeReturn("continueExecuting is false");
                            return;
                        }
                        //Firstrun is set to false at the bottom of
                        //this loop.
                        frame = stack.peek();
                        if (frame.ptr == -1) {
                            logger.error("Oops! Frame pointer == -1, setting it to 0 !");
                            frame.ptr = 0;
                        }

                    }

                    //Retrieve nextOp op, and fixup ptr to point inside
                    //the frame. This is needed if a call was issued in
                    //processevents.
                    Executable nextOp = nextOperation(frame);

                    try {
                        if (!traceAndValidateOp(false, nextOp)) {
                            beforeReturn("traceAndValidateOp returned false");
                            return;
                        }

                        //Remember how this frame looked when we executed it.
                        if (trace)
                            takeSnapshot(frame);

                        //Execute this op
                        nextOp.execute(executionContext);

                        //Enable us to modify running context by injecting callbables to the engine,
                        //and thus saving us a lot of synchronization headaces
                        if (MASTestSwitches.isUnitTesting()) {
                            Callable c;
                            while ((c = runOnceList.poll()) != null) {
                                c.call();
                            }
                        }

                        if (executionContext.isInterrupted() || Thread.currentThread().isInterrupted()) {
                            shutdownAndLog("Engine was interrupted, terminating execution !");
                            beforeReturn("Engine was interrupted");
                            return;
                        }
                    } catch (DelayedExceptions de) {
                        log.error(getContextType() + "Delayed exceptions");
                        shutdownAndLog(de);
                        beforeReturn("Delayed exceptions");
                        return;
                    } catch (InterruptedException ie) {
                        shutdownAndLog("Engine was interrupted, InterruptedException caught, terminating execution !");
                        beforeReturn("InterruptedException");
                        return;
                    } catch (EngineStackExhausted ese) {
                        log.error(getContextType() + "Engine stack exhausted, terminating execution !" + getExecutionLocation());
                        shutdownAndLog (ese);
                        beforeReturn("Engine stack exhausted");
                    } catch (Throwable t) {
                        shutdownAndLog(t);
                        beforeReturn("Throwable");
                        return;
                    }

                    executionContext.postProcess();

                    eventAvail = processEvents(false);

                    // If continueExecuting is unset, we have become event
                    // driven for now. And will only start running once we
                    // get an event.
                    if (continueExecuting.get()) {
                        locateNextOperation(stack.peek());
                    }

                    //Indicate that the first run is complete
                    firstRun = false;
                } finally {
                    if (executionLock.isHeldByCurrentThread()) executionLock.unlock();
                }
            }
        } catch (EngineStackExhausted ese) {
            log.error(getContextType() + "Engine stack exhausted, terminating execution !" +
                    getExecutionLocation());
            shutdownAndLog(ese);
            beforeReturn("Engine stack exhausted");
        } finally {
            while (executionLock.isHeldByCurrentThread()) {
                log.warn("Unlocking executionLock in finally. Hold count: " + executionLock.getHoldCount());
                executionLock.unlock();
            }
            executingThread.set(null);
            if (log.isDebugEnabled()) log.debug(getContextType() + "Leaving executeNext:" + toString());
        }

    }

    public void releaseToPool(){
        beforeReturn("Engine is stopped");
    }

    private void beforeReturn(String reason) {
        if (log.isDebugEnabled()) log.debug(getContextType() + "Reason to return: " + reason);
        if (wasStopped.get()) {
            if (executionContext != null) {
                executionContext.setEngine(EngineDummy.instance());
                if (executionContext.isInterrupted()) {
                    // If this is the VXML Engine, this Engine may currently be shutdown by the CCXML thread.
                    // Do not return to thread pool, since that would cause this Engine to get reused during shutdown
                    if (log.isDebugEnabled()) log.debug(getContextType() + " was interrupted, does not return to pool");
                    return;
                }

            }
            clearStack();
            if (enginePool != null) enginePool.release(this);
            executionContext = null;
        }
    }

    /**
     * @param atomic Next operation atomic or not
     * @param nextOp The next operation
     * @return false if the next operation is null, true otherwise
     * @logs.error "Next operation is null !" - Execution engine has surprisingly found the the next operation to execute is null. This is most likely an internal error/bug.
     */
    private boolean traceAndValidateOp(boolean atomic, Executable nextOp) {
        if (nextOp != null) {
            if (log.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append(getContextType());
                sb.append(atomic ? "ATOMIC >> " : "NORMAL >> ");
                sb.ensureCapacity(stack.size()+sb.length()+50);
                for (int i = 0; i < stack.size(); i++) {
                    sb.append('.');
                }
                sb.append(nextOp.toString());
                log.trace(sb);
            }
        } else {
            log.error("Next operation is null !");
            executionContext.shutdown(true);
            dumpState();
            return false;
        }
        return true;
    }

    /**
     * @logs.error "<context type>: Delayed exception: <message>" - During execution, execution engine encountered an exception, but at the time of the exceptions, execution engine did not want to abort the execution. Instead, the exceptions are listed now. <Context type> is either VXML or CCXML. <message> should give further information about the problem.
     * @logs.error "Error occured while executing <message> - Execution of the application has resulted in an unexpected exception. <message> should give more information about the problem.
     */
    private void shutdownAndLog(Throwable t) {
        if (t instanceof DelayedExceptions) {
            DelayedExceptions de = (DelayedExceptions) t;
            for (Throwable ex : de.getExceptions()) {
                log.error(getContextType() + "Delayed exception: " + ex.getMessage(), ex);
            }
        } else {
            log.error(getContextType() + "Error occured while executing. " + getExecutionLocation(), t);
        }
        
        // to make sure we catch the exception during the dump
        try {
            dumpState();
        } catch (Exception e) {
            log.debug( "Exception caught : " + e + "while printing a dump");
        }
        executionContext.shutdownEverything();
    }

    // shutdown the session.
    private void shutdownSession(Throwable t) {
        if (t instanceof DelayedExceptions) {
            DelayedExceptions de = (DelayedExceptions) t;
            for (Throwable ex : de.getExceptions()) {
                log.error(getContextType() + "Delayed exception: " + ex.getMessage(), ex);
            }
        } else {
            log.error(getContextType() + "Error occured while executing. " + getExecutionLocation(), t);
        }
     
        executionContext.shutdownEverything();
    }
    
    
    /**
     * @logs.error "<Context type> Engine was interrupted, terminating execution !" - Execution engine was unexpectedly interrupted. <Context type> is either VXML or CCXML.
     */
    private void shutdownAndLog(String errorMessage) {
        executionContext.shutdownEverything();
        log.error(getContextType() + errorMessage);
    }

    private Executable nextOperation(StackFrame frame) {
        if (frame.ptr < 0) {
            frame.ptr = 0;
        }
        if (frame.singleOp != null)
            return frame.singleOp;
        else
            return frame.ops.get(frame.ptr);
    }

    private void maybeResizeStack() {
        if (stack.capacity() != stackSize) {
            if (log.isInfoEnabled())
                log.info("Resize stack from " + stack.capacity() + " to " + stackSize +
                         ". Note, never decreased");
            stackSize = stack.resize(stackSize);
            if (TestEventGenerator.isActive()) {
                TestEventGenerator.generateEvent(TestEvent.ENGINE_STACKSIZE, stackSize);
                TestEventGenerator.generateEvent(TestEvent.ENGINE_TRACE, trace);
            }
        }
    }

    private void dumpState() {
        if (log.isInfoEnabled()) log.info(getContextType() + "Dumping execution history");
        if (log.isInfoEnabled()) log.info(getContextType() + getExecutionLocation());

        if (trace) {
            //If we've wrapped around, print all ops
            //but start with the oldest
            if (snapshots.get(snapshotIndex).frame.ptr != -1) {
                for (int i = snapshotIndex; i < snapshots.size(); i++) {
                    if (log.isInfoEnabled()) log.info(getContextType() + snapshots.get(i));
                }
            }

            //And always print the newest
            for (int i = 0; i < snapshotIndex; i++) {
                if (log.isInfoEnabled()) log.info(getContextType() + snapshots.get(i));
            }
        } else {
            if (log.isInfoEnabled()) log.info(getContextType() + "Trace is false, does not dump snapshots");
        }


        if (log.isInfoEnabled()) log.info(getContextType() + "Dumping stack");
        List<StackFrame> stackFrames = stack.asList();
        for (int i = 0; i < stackFrames.size(); i++) {
            if (log.isInfoEnabled()) log.info(getContextType() + stackFrames.get(i));
        }

        if (log.isInfoEnabled()) log.info(getContextType() + "Dumping stack-correlated values");
        executionContext.dumpFrames(stackFrames);


        if (log.isInfoEnabled()) log.info(getContextType() + "Dumping pending events");
        executionContext.dumpPendingEvents();
    }

    private String getExecutionLocation() {
        if (snapshots.size() > 0 && snapshots.get(snapshotIndex).frame.ptr != -1) {
            Snapshot current = snapshots.get(snapshotIndex);
            Product p = current.frame.call;
            if (p != null) {
                Module executingModule = executionContext.getExecutingModule();
                URI documentURI = null;
                if (executingModule != null) {
                    documentURI = executingModule.getDocumentURI();
                }
                DebugInfo debugInfo = p.getDebugInfo();
                String tagName = p.getDebugInfo().getTagName();
                Object location = debugInfo.getLocation();
                return
                        "Was last executing near tag:" +
                                tagName +
                                ". Line and column:" + location +
                                ". Document URI:" +
                                documentURI;
            }
        }
        return "Unknown location. Execution not started yet?";
    }

    private void takeSnapshot(StackFrame frame) {
        Snapshot snapshot = snapshots.get(snapshotIndex);
        snapshot.stackDepth = stack.size() - 1;
        snapshot.assignFrame(frame);
        snapshotIndex = ++snapshotIndex % snapshotCount;
    }

    private boolean processEvents(boolean eventAvail) {
        if (!eventAvail) {
            eventAvail = executionContext.processEvents();
        }
        return eventAvail;
    }

    /**
     * Locate the operation to execute on the next
     * iteration of the execution loop inside <code>executeNext()</code>
     * <p/>
     * When this method decides it must pop a level from the engine stack
     * to continue execution it will also execute the <code>reportFrameLeft</code>
     * and <code>reportFrameReinstated</code> callbacks on the {@link ExecutionContext}
     * interface. The callback will report whatever value is in the extraData field
     * of the StackFrame to the ExecutionContext.
     *
     * @param frame The current stack frame.
     */
    private void locateNextOperation(StackFrame frame) {
        frame.ptr++;
        StackFrame initialFrame = frame;
        do {
            if (stack.size() == 1 && !frame.hasNext()) {
                stack.pop();
                executionContext.reportFrameLeft(frame.frameData, frame.call);
                continueExecuting.set(false);
                break;
            } else {
                if (frame.hasNext()) break;
            }
            //Pop and leave frame
            frame = stack.pop();
            executionContext.reportFrameLeft(frame.frameData, frame.call);

            //Peek on current frame
            frame = stack.peek();
            frame.ptr++;
            subroutineOffset = Math.max(0, subroutineOffset--);
        } while (!frame.hasNext());
        if (frame != initialFrame) executionContext.reportFrameReinstated(frame.frameData, frame.call);
    }

    /**
     * Informs the engine that it should stop executing as soon as
     * possible.
     * Under normal circumstances this is immediately after the
     * next {@link Executable} has executed. However, regardless off
     * the state of the engine, this method will always return at
     * once.
     */
    public void stopExecuting() {
        continueExecuting.set(false);
        wasStopped.set(true);
    }


    public void pauseExecuting() {
        continueExecuting.set(false);
        isPaused.set(true);
    }

    public void startExecuting() {
        continueExecuting.set(true);
        isPaused.set(false);
    }

    /**
     * Unwinds the {@link EngineStack} until the supplied product
     * is the current call context.
     * The supplied product must be an ancestor to the currently
     * executing node.
     *
     * @param prod                  Call context which the stack should be unwound
     * @param unwindSuppliedProduct if true, unwinds also the supplied prod.
     * @logs.error "Engine.unwind was called with null parameter". - A variable had unexceptedly the value null.
     */

    public boolean unwind(Product prod, boolean unwindSuppliedProduct) {
        if (prod == null) {
            logger.error(
                    "Engine.unwind was called with null parameter");
            return false;
        }
        if (unwindSuppliedProduct) {
            return unwindSupplied(prod);
        } else {
            return unwindNotSupplied(prod);
        }
    }

    /**
     * @param prod The product
     * @return false if the unwind point cannot be found, true otherwise
     * @logs.error "Couldn't find unwinding point !" - Execution engine was trying to unwind an internal data structure but the unwind point was not found.
     */
    private boolean unwindSupplied(Product prod) {
        // Manipulate the engine stack such that the next operation to execute
        // is the one before "prod".

        int prunePoint = stack.size();
        boolean done = false;
        for (int i = stack.size() - 1; i >= 0; i--) {
            StackFrame frame = stack.peek(i);
            if (frame.call == prod) {
                done = true;
                prunePoint = i;
                break;
            }
        }
        if (!done) {
            log.error(getContextType() + "Couldn't find unwinding point !");
            return false;
        }

        // unwinding point ok, leave all those frames
        for (int i = stack.size() - 1; i >= prunePoint; i--) {
            StackFrame frame = stack.peek(i);
            executionContext.reportFrameLeft(frame.frameData, frame.call);
        }
        //TODO changed from:         stack.prune(Math.max(0, prunePoint-1));

        stack.prune(Math.max(0, prunePoint));
        if (stack.size() > 0) {
            StackFrame frame = stack.peek();
            executionContext.reportFrameReinstated(frame.frameData, frame.call);
        } else {
            executionContext.reportFrameReinstated(null, null);
        }
        return true;
    }

    /**
     * @param prod The product
     * @return false is the unwind point cannot be found, true otherwise
     * @logs.error "Couldn't find unwinding point !" - Execution engine was trying to unwind an internal data structure but the unwind point was not found.
     */
    private boolean unwindNotSupplied(Product prod) {
        int previousCall = stack.size();
        boolean done = false;
        for (int i = stack.size() - 1; i >= 0; i--) {
            StackFrame frame = stack.peek(i);
            if (frame.call == prod) {
                // Reset the frame ptr
                frame.ptr = -1;
                done = true;
                break;
            }
            executionContext.reportFrameLeft(frame.frameData, frame.call);
            if (frame.call != null) previousCall = i;
        }

        if (!done) {
            log.error(getContextType() + "Couldn't find unwinding point !");
            return false;
        }

        stack.prune(Math.max(0, previousCall));

        //TODO: This is a hack to please the current FIA implementation, it should be repaired
        StackFrame predicateFrame = stack.peek();
        if (predicateFrame.call instanceof Predicate) {
            Predicate pred = (Predicate) predicateFrame.call;
            if (!pred.eval(this.executionContext)) {
                predicateFrame = stack.pop();
                executionContext.reportFrameLeft(predicateFrame.frameData, predicateFrame.call);
            }
        }
        if (stack.size() > 0) {
            StackFrame frame = stack.peek();
            executionContext.reportFrameReinstated(frame.frameData, frame.call);
        } else {
            executionContext.reportFrameReinstated(null, null);
        }
        return true;
    }

    /**
     * Push the supplied executables on the stack and use the
     * supplied product as marker for stack-unwinding.
     *
     * @param operations The operations to push on the stack
     * @param product The product
     */
    public void call(List<Executable> operations, Product product) {
        stack.push(operations, product);
        executionContext.reportFrameEntered(stack.peek().frameData, product);
    }

    /**
     * Push the supplied product on the stack and use it
     * as marker for stack-unwinding.
     *
     * @param product The product to push on the stack
     */
    public void call(Product product) {
        stack.push(product);
        executionContext.reportFrameEntered(stack.peek().frameData, product);
    }

    /**
     * Put list of executables on the stack.
     * These executables will be executed as soon as possible,
     * usually as the next opcode, unless an asynchronous event
     * has occurred.
     *
     * @param operations The operations to push on the stack
     */
    public void push(List<Executable> operations) {
        stack.push(operations);
        executionContext.reportFrameEntered(stack.peek().frameData, null);
    }

    @IntegerParameter(
            description = "Maximum allowed depth of stack",
            displayName = "Stack size",
            configName = "enginestacksize",
            parameter = ParameterId.Engine_StackSize,
            min = 10,
            max = 1000,
            defaultValue = 100)
    public void setStackSize(int stackSize) {
        if (validate(ParameterId.Engine_StackSize, stackSize)) {
            this.stackSize = stackSize;
        }
    }

    /**
     * Execute a list of executables without processing events
     * or exceptions.
     * <p/>
     * This is typically used to run code like constructors and
     * destructors where it's paramount that a best effort is done,
     * even in the face of errors. It's up to the operations executed
     * to ensure that no ill effects will arise from this behaviour.
     * <p/>
     * Exceptions that occur during execution will be collected and
     * a DelayedInterruptionException or DelayedExceptions exeption
     * will be throw thrown <em>after</em> execution is finished.
     *
     * @param atomicOperationsList The list of atomic operations
     * @param start Starting operation number
     * @param count The number of operation to execute
     */
    public void executeAtomic(List<? extends Executable> atomicOperationsList, int start, int count) {
        boolean wasInterrupted = false;
        List<Throwable> exceptions = null;
        int end = Math.min(count + start, atomicOperationsList.size());
        for (int i = start; i < end; i++) {
            try {
                Executable nextOp = atomicOperationsList.get(i);
                traceAndValidateOp(true, nextOp);
                nextOp.execute(executionContext);
            } catch (InterruptedException e) {
                wasInterrupted = true;
            } catch (Throwable t) {
                if (exceptions == null) {
                    exceptions = new ArrayList<Throwable>();
                }
                exceptions.add(t);
            }
        }
        if (wasInterrupted) throw new DelayedInterruptionException("Delayed interrupt");
        if (exceptions != null) throw new DelayedExceptions(exceptions);
    }

    public void executeAtomic(Operation[] exitOps) {
        executeAtomic(Arrays.asList(exitOps), 0, exitOps.length);
    }

    @BooleanParameter(
            description = "Trace of Engine execution",
            displayName = "Engine trace",
            configName = "traceenabled",
            parameter = ParameterId.Engine_traceEnabled,
            defaultValue = false)
    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public void setLogger(ILogger logger) {
        Engine.logger = logger;
    }

    protected String getContextType() {
        if (executionContext != null)
            return "(" + executionContext.getContextType() + ") ";
        else {
            return "(NO EXECUTION CONTEXT!!) ";
        }
    }

    public Engine create(IConfigurationManager configurationManager) {
        return enginePool.createEngine(configurationManager);
    }


    public Thread getExecutingThread() {
        return executingThread.get();
    }

    public boolean needsToRun() {
        boolean b = executionContext.getEventProcessor()
                .hasEventsInQ() || executionContext.getExecutionResult() == ExecutionResult.RUN_UNCONDITIONALLY;
        if (log.isDebugEnabled()) {
            if (b) {
                log.debug(getContextType() + "Needs to run. EventsInQ: " +
                        executionContext.getEventProcessor()
                                .hasEventsInQ() + ", executionResult:" + executionContext.getExecutionResult());
            } else {
                log.debug(getContextType() + "Does not need to run. EventsInQ: " +
                        executionContext.getEventProcessor()
                                .hasEventsInQ() + ", executionResult:" + executionContext.getExecutionResult());
            }
        }
        return b;

    }

    public String toString() {
        return "continueExecuting:" + continueExecuting.get() + ",wasStopped:" + wasStopped.get() + ",isPaused:" + isPaused
                .get();
    }
}
