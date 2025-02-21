package com.mobeon.ntf.out.outdial.verify;

import java.util.*;
import com.mobeon.common.commands.CommandHandler;
import com.mobeon.common.commands.Command;
import com.mobeon.common.commands.Operation;
import com.mobeon.common.commands.CommandException;
import com.mobeon.common.commands.State;
import com.mobeon.ntf.out.outdial.OdlInfo;

/**
 * Verifies the that states are reachable and used.
 */
public class StateVerifyer implements VerifyerBase
{
    private CommandHandler handler;
    private int[] allCodes = {
        OdlInfo.EVENT_CODE_COMPLETED,
        OdlInfo.EVENT_CODE_INITIATED,
        OdlInfo.EVENT_CODE_NUM_BLOCKED_OLD,
        OdlInfo.EVENT_CODE_NUM_BUSY_OLD,
        OdlInfo.EVENT_CODE_NOANSWER_OLD,
        OdlInfo.EVENT_CODE_NOT_REACHABLE_OLD,
        OdlInfo.EVENT_CODE_REQUEST_TIMEOUT,
        OdlInfo.EVENT_CODE_NOT_AVAILABLE,
        OdlInfo.EVENT_CODE_SYNTAX_ERROR,
        OdlInfo.EVENT_CODE_UNRECOGNIZED,
        OdlInfo.EVENT_CODE_LIMIT_EXCEEDED,
        OdlInfo.EVENT_CODE_NUMBER_NOTEXIST,
        OdlInfo.EVENT_CODE_INVALIND_NUM,
        OdlInfo.EVENT_CODE_UNKNOWN_ERR,
        OdlInfo.EVENT_CODE_NOMAILBOX,
        // Defined codes in 600-series
        OdlInfo.EVENT_CODE_BUSY,
        OdlInfo.EVENT_CODE_CALL_NOT_ANSWERED,
        OdlInfo.EVENT_CODE_DESTINATION_NOT_REACHABLE,
        OdlInfo.EVENT_CODE_DO_NOT_DISTURB,
        OdlInfo.EVENT_CODE_NETWORK_CONGESTION,
        // Undefined codes in 600-series
        601, 602, 604, 605, 606, 607, 608, 609, 611,
        612, 615, 616, 617, 618, 619, 621, 622, 623,
        624, 626, 627, 628, 629, 630, 631, 632,
        633, 634,
        // End of codes in 600-series
        OdlInfo.EVENT_CODE_DEFAULT,
        OdlInfo.EVENT_CODE_NOTIFDISABLED,
        OdlInfo.EVENT_CODE_LOCATION_FAILURE,
        OdlInfo.EVENT_CODE_CFU_ON,
        OdlInfo.EVENT_CODE_CFU_FAILURE,
        OdlInfo.EVENT_CODE_INTERRUPTED,
        OdlInfo.EVENT_CODE_PHONEON,
        OdlInfo.EVENT_CODE_PREPAID_FAILURE
    };


    /** Creates a new instance of InitialTransitionVerifyer */
    public StateVerifyer(CommandHandler handler)
    {
        this.handler = handler;
    }

    public void validate(int level)
    {
        doValidateStateReachability(level);
        doValidateStateUsed(level);
        doValidateLoops(level);
    }


    /**
     * Validate that all states are reachable for initial state.

     */
    private void doValidateStateReachability(int level)
    {
         if (level >= LEVEL_HIGH) {
            System.out.println("Checking: All states are reachable from initial");
            int noStates = handler.getNoStates();
            boolean statesReachable[] = new boolean[noStates]; // Reachable states
            boolean statesVisited[] = new boolean[noStates]; // States we checked from
            boolean finalReached = false;
            statesReachable[handler.getInitialState()] = true;
            // Find state to check from
            int visitThis = findStateToVisit(statesReachable, statesVisited);
            while (visitThis >= 0) {
                statesVisited[visitThis] = true;
                boolean reachFinal = checkReachability(visitThis, statesReachable);
                if (!finalReached) finalReached = reachFinal;
                visitThis = findStateToVisit(statesReachable, statesVisited);
            }

            if (!finalReached) {
                System.out.println("Warning/High: Final state will never be reached");
            }
            for (int i = 0; i<statesReachable.length; i++) {
                if (!statesReachable[i]) {
                    System.out.println("Warning/High: State number " + i +
                        " will never be reached");
                }
            }
        }
    }

    /**
     * Get a state to check reachability from.
     * The state should be reacheable but not yet visited.
     */
    private int findStateToVisit(boolean reachable[], boolean visited[])
    {
        for (int i = 0; i<reachable.length; i++) {
            if (reachable[i] && !visited[i]) {
                return i;
            }
        }
        return -1; // No state that we can reach that is not visisted yet
    }

    /**
     * Fill in reachable states.
     * Return if final state is reachable.
     */
    private boolean checkReachability(int fromStateNo, boolean reachable[])
    {
        boolean reachEndState = false;
        for (int codeIndex = 0; codeIndex < allCodes.length; codeIndex++) {
            try {
                Command cmd = handler.getCommand(fromStateNo, allCodes[codeIndex]);
                if (cmd.getNextState() == CommandHandler.STATE_FINAL) {
                    reachEndState = true;
                } else {
                    reachable[cmd.getNextState()] = true;
                }
            } catch (CommandException ce) {
                // Code not defined, warned about elseware
            }
        }
        return reachEndState;
    }




    /**
     * Validate that all states have own transitions, and not only
     * the defaults.
     */
    private void doValidateStateUsed(int level)
    {
        if (level >= LEVEL_MEDIUM) {
            System.out.println("Checking: All states meaningful");
            State[] states = handler.getStates();
            for (int stateNo = 0; stateNo < states.length; stateNo++) {
                HashMap transitions = states[stateNo].getTransitions();
                Set keys = transitions.keySet();
                if (keys.size() == 0) {
                    System.out.println("Warning/Medium: No unique " +
                        "transitions defined from state " + stateNo +
                        " only defaults will be used");
                }
            }

        }
    }

    /**
     * Tr represents one state transition. It has a from state, a to state and
     * one or more status codes that trigger the transition between the states.
     * Tr does not bother with actual status codes but only with the index in
     * allCodes of the status codes, from 0 and up.
     */
    private class Tr {
        int from;
        int to;
        boolean[] codeIxs;

        Tr(int from, int to) {
            this.from = from;
            this.to = to;
            codeIxs = new boolean[allCodes.length]; //True if code triggers this transition
        }

        /**
         * Add a triggering status code
         */
        void addCodeIx(int codeIx) {
            codeIxs[codeIx] = true;
        }

        /**
         * Determine whether this transition can only be triggered by "busy"
         * status codes.
         */
        boolean onlyTriggeredByBusy() {
            for (int codeIx = 0; codeIx < codeIxs.length; codeIx++) {
                if (codeIxs[codeIx]
                    && allCodes[codeIx] != OdlInfo.EVENT_CODE_NUM_BUSY_OLD
                    && allCodes[codeIx] != OdlInfo.EVENT_CODE_BUSY) {
                    return false;
                }
            }
            return true;
        }

        public String toString() {
            String result = String.format("%03d", from)
                + "->"
                + String.format("%03d", to)
                + ":";
            for (int codeIx = 0; codeIx < allCodes.length; codeIx++) {
                if (codeIxs[codeIx]) {
                    result = result + " " + allCodes[codeIx];
                }
            }
            return result;
        }
    }

    private void doValidateLoops(int level) {
        if (level >= LEVEL_HIGH) {
            int noStates = handler.getNoStates();
            Tr[][] transitions = new Tr[noStates][noStates];
            int from;
            int to;

            System.out.println("Checking: Loops in state machine");

            //Build array with one Tr object for each possible transition
            for (from = 0; from < noStates; from++) {
                for (int codeIx = 0; codeIx < allCodes.length; codeIx++) {
                    try {
                        //EVENT_CODE_DEFAULT is the only possible status from
                        //the initial state and can not occur in other states
                        if (allCodes[codeIx] == OdlInfo.EVENT_CODE_DEFAULT) {
                            if (from != handler.getInitialState()) continue;
                        } else {
                            if (from == handler.getInitialState()) continue;
                        }

                        to = handler.getCommand(from, allCodes[codeIx]).getNextState();
                        if (to != CommandHandler.STATE_FINAL) {
                            if (transitions[from][to] == null) {
                                transitions[from][to] = new Tr(from, to);
                            }
                            transitions[from][to].addCodeIx(codeIx);
                        }
                    } catch (CommandException e) {
                        //No transition from this state for this code
                    }
                }
            }

            printStateTransitionOverview(transitions);
            printStateTransitionList(transitions);

            System.out.println("\nPOSSIBLE LOOPS"
                               + (level < LEVEL_LOW
                                  ?" (excluding loops only triggered by busy status)"
                                  :""));
            printLoops(level,
                       handler.getInitialState(),
                       transitions,
                       new ArrayList<Integer>());
        }
    }

    /**
     * Print a square matrix showing the state transitions that can occur for at
     * least some status code.
     */
    private void printStateTransitionOverview(Tr[][] transitions) {
        System.out.println("\nSTATE TRANSITION OVERVIEW");
        System.out.print("  to");
        for (int to = 0; to < transitions.length; to++) {
            System.out.print(String.format("%3d", to));
        }
        System.out.println();
        System.out.print("____");
        for (int to = 0; to < transitions.length; to++) {
            System.out.print("___");
        }
        System.out.println("\nfrom|");
        for (int from = 0; from < transitions.length; from++) {
            System.out.print(String.format("%4d", from) + "|");
            for (int to = 0; to < transitions.length; to++) {
                System.out.print((transitions[from][to] != null) ?" X " :" . ");
            }
            System.out.println();
        }
    }

    /**
     * Print a list of all state transition that can occur together with  the
     * status codes that can trigger each transition.
     */
    private void printStateTransitionList(Tr[][] transitions) {
        System.out.println("\nSTATE TRANSITIONS AND STATUS CODES");
        System.out.println("Transition: Triggering status codes");
        for (int from = 0; from < transitions.length; from++) {
            for (int to = 0; to < transitions.length; to++) {
                if (transitions[from][to] != null) {
                    System.out.println("  " + transitions[from][to]);
                }
            }
        }
    }

    /**
     * Print a list of all sequences of states that constitute a loop.
     */
    private void printLoops(int level,
                            int state,
                            Tr[][] transitions,
                            ArrayList<Integer> stateTrail) {
        //Remember original trail so it can be restored after recursion, since
        //only the reference to stateTrail is copied through call-by-value and
        //all modifications are applied to the original object. This is a bit
        //inefficient but easier to get right than undoing the changes at the
        //end of the function.
        ArrayList<Integer> oldTrail = (ArrayList<Integer>) stateTrail.clone();

        boolean loopFound = stateTrail.contains(state);
        stateTrail.add(state);

        if (loopFound) {
            printTrail(stateTrail);
        } else {
            for (int to = 0; to < transitions.length; to++) {
                if (transitions[state][to] != null
                    && (level >= LEVEL_LOW
                        || !transitions[state][to].onlyTriggeredByBusy())) {
                    printLoops(level,
                               to,
                               transitions,
                               stateTrail);
                }
            }
        }
        //Restore original stateTrail before returning
        stateTrail.clear();
        stateTrail.addAll(oldTrail);
    }

    /**
     * Print one line with all the integers in an ArrayList.
     */
    private void printTrail(ArrayList<Integer> stateTrail) {
        for (Iterator<Integer> it = stateTrail.iterator();
             it.hasNext();
             System.out.print(" " + it.next()));
        System.out.println();
    }


}
