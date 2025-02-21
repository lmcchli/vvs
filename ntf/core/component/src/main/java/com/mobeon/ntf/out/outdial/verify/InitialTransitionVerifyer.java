package com.mobeon.ntf.out.outdial.verify;

import java.util.*;
import com.mobeon.common.commands.CommandHandler;
import com.mobeon.common.commands.Command;
import com.mobeon.common.commands.Operation;
import com.mobeon.common.commands.CommandException;
import com.mobeon.common.commands.State;
import com.mobeon.ntf.out.outdial.OdlInfo;

/**
 * Verifies things about the initial transition and state.
 * That thre is a transaction with the start code from
 * the initial state (possible by default) and that no other states has
 * that transition (except if given by default).
 */
public class InitialTransitionVerifyer
    implements VerifyerBase
{
    private CommandHandler handler;

    /** Creates a new instance of InitialTransitionVerifyer */
    public InitialTransitionVerifyer(CommandHandler handler)
    {
        this.handler = handler;
    }

    /**
     * Do our validations.
     */
    public void validate(int level)
    {
        doValidateInitialTransition(level);
        doValidateExtraInitialStateTransitions(level);
        doValidateExtraStartupTransitions(level);
        doValidateInitialNumber(level);
        doValidateGoInitial(level);
    }


    void doValidateInitialTransition(int level)
    {
        if (level >= LEVEL_ERROR) {
            System.out.println("Checking: Initial transition from initial state");
            try {
                Command cmd = handler.getCommand(handler.getInitialState(),
                                                 OdlInfo.EVENT_CODE_DEFAULT);
            } catch (CommandException e) {
                System.out.println("Error: Could not find initial transition " +
                                   "from initial state, Exception : " + e);
            }
         }
    }

    void doValidateExtraInitialStateTransitions(int level)
    {
        if (level >= LEVEL_LOW) {
            System.out.println("Checking: Extra transitions from initial");
            State[] states = handler.getStates();
            State initial = states[handler.getInitialState()];
            for (Iterator it = initial.getTransitions().keySet().iterator();
                 it.hasNext() ; ) {
                Integer key = (Integer)it.next();
                if (key.intValue() != OdlInfo.EVENT_CODE_DEFAULT) {
                    System.out.println("Warning/Low: Transition with code " +
                        key + " from initial state, this might not be used ");
                }
            }
        }
    }


    void doValidateExtraStartupTransitions(int level)
    {
        if (level >= LEVEL_LOW) {
            System.out.println("Checking: Extra startup transitions");
            State[] states = handler.getStates();
            for (int stateNo = 0; stateNo < states.length; stateNo++) {
                if (stateNo != handler.getInitialState() ) {
                    Command c = states[stateNo].getTransitionCommand(OdlInfo.EVENT_CODE_DEFAULT);
                    if (c != null) {
                        System.out.println("Warning/Low: Startup transition (" +
                            OdlInfo.EVENT_CODE_DEFAULT + ")" +
                            "from state " + stateNo + " which is not initial");
                        System.out.println("+++          Transition will never be followed");
                    }
                }
            }
        }
    }

    void doValidateInitialNumber(int level)
    {
        if (level >= LEVEL_LOW) {
            System.out.println("Checking: Initial State");
            int initial = handler.getInitialState();
            if (initial != 0) {
                System.out.println("Warning/Low: Initial state is not zero state");
            }
        }
    }

    void doValidateGoInitial(int level)
    {
        if (level >= LEVEL_MEDIUM) {
            State[] states = handler.getStates();
            for (int stateNo =0; stateNo<states.length; stateNo++) {
                checkGoInitial("state number " + stateNo, states[stateNo]);
            }
            checkGoInitial("default state",handler.getDefaultState());
        }
    }

    /**
     * Check for transitions back to initial state.
     */
    private void checkGoInitial(String stateName, State state)
    {
        HashMap transitions = state.getTransitions();
        for (Iterator it =  transitions.keySet().iterator(); it.hasNext() ; ) {
            Integer code = (Integer)it.next();
            Command c = (Command)transitions.get(code);
            if (c.getNextState() == handler.getInitialState()) {
                System.out.println("Warning/Medium: Transition from " +
                    stateName + " to initial state for code " + code);
                break;
            }
        }
    }

}
