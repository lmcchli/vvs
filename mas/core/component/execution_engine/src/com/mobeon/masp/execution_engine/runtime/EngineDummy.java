package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.Operation;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.configuration.ParameterBlock;
import com.mobeon.common.logging.ILogger;

import java.util.List;

/**
 * User: QMIAN
 * Date: 2006-jun-21
 * Time: 15:47:35
 */
class EngineDummy extends Engine {
    private static Engine engineDummy = null;

    private EngineDummy() {
        super();
    }

    public void init(IConfigurationManager configurationManager, EnginePool enginePool) {
    }

    public void reset() {
    }

    public ParameterBlock getParameterBlock() {
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public void executeNext() {
    }

    public void stopExecuting() {
    }

    public void pauseExecuting() {
    }

    public void startExecuting() {
    }

    public boolean unwind(Product prod, boolean unwindSuppliedProduct) {
        return false;
    }

    public void call(List<Executable> operations, Product product) {
    }

    public void call(Product product) {
    }

    public void push(List<Executable> operations) {
    }

    public void setStackSize(int stackSize) {
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
     * @param atomicOperationsList
     * @param start
     * @param count
     */
    public void executeAtomic(List<? extends Executable> atomicOperationsList, int start, int count) {
        super.executeAtomic(atomicOperationsList, start, count);
    }

    public void executeAtomic(Operation[] exitOps) {
        super.executeAtomic(exitOps);
    }

    public void setTrace(boolean trace) {
        super.setTrace(trace);
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        super.setExecutionContext(executionContext);
    }

    public void setLogger(ILogger logger) {
        super.setLogger(logger);
    }

    protected String getContextType() {
        return super.getContextType();
    }

    public static Engine instance() {
        synchronized (EngineDummy.class) {
            if (engineDummy == null)
                engineDummy = new EngineDummy();
            return engineDummy;
        }
    }
}
