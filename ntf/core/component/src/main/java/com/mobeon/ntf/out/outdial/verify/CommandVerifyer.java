package com.mobeon.ntf.out.outdial.verify;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.mobeon.common.commands.Command;
import com.mobeon.common.commands.CommandHandler;
import com.mobeon.common.commands.Operation;
import com.mobeon.common.commands.State;

/**
 * Verifies the commands for individual transitions.
 */
public class CommandVerifyer implements VerifyerBase
{
    private CommandHandler handler;

    /** Creates a new instance of InitialTransitionVerifyer */
    public CommandVerifyer(CommandHandler handler)
    {
        this.handler = handler;
    }

    public void validate(int level)
    {
        doValidateCallHandling(level);
    }


    /**
     * Validate that transitions that are not to the end states
     * always end with a call, and that call is alwyas at end
     * if it is part of a transition.
     */
    private void doValidateCallHandling(int level)
    {
         if (level >= LEVEL_HIGH) {
            System.out.println("Checking: Call Handling");
            State[] states = handler.getStates();
            for (int stateNo =0; stateNo<states.length; stateNo++) {
                checkCallHandling("state number " + stateNo, states[stateNo]);
            }
            checkCallHandling("default state",handler.getDefaultState());
        }
    }

    /**
     * Check that location of the call operation.
     * A call should exist and be last in all transitions not going to
     * the final state.
     * If going to final state the call does not need to exist, but if it
     * exists it should be last.
     */
    private void checkCallHandling(String stateName, State state)
    {
        HashMap transitions = state.getTransitions();
        for (Iterator it =  transitions.keySet().iterator(); it.hasNext() ; ) {
            Integer code = (Integer)it.next();
            Command c = (Command)transitions.get(code);
            List ops = c.getOperations();
            boolean isFinal = (c.getNextState() == CommandHandler.STATE_FINAL);
            for (Iterator opIt = ops.iterator(); opIt.hasNext(); ) {
                Operation op = (Operation)opIt.next();
                if (opIt.hasNext()) {
                    // Not last operation
                    if (op.getOpcode() == CommandHandler.OP_CALL) {
                        System.out.println("Warning/High: Transition with " +
                            "code " + code + " from " + stateName +
                            " has call that is not at the end of operations");
                    }
                } else {
                    if (!isFinal && (op.getOpcode() != CommandHandler.OP_CALL)) {
                        // Allowed to not end on call when to final
                        System.out.println("Warning/High: Transition with code " +
                        code + " from " + stateName +
                        " does not end with a call");
                    }
                }
            }
        }
    }

}
