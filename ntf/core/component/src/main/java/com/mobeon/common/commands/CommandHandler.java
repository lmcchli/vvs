/*
 * CommandHandler.java
 *
 * Created on den 26 augusti 2004, 13:36
 */

package com.mobeon.common.commands;

import java.util.*;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;

/**
 * This class handles the state machine for commands.
 * The class knows about all states and transitions that
 * are possible. It can construct the state machine from
 * a property object that is given in the constructor.
 * Given a state and a reply code the handler can find out
 * the correct {@link Command} to return, taking default handling
 * into account. The next state to go to after the command has
 * been done is saved into the command.
 */
public class CommandHandler
{

    /** Code for no operation, should not be used in state machine. */
    public static final short OP_NOOP = 0;
    /**
     * Code for wait for phone to be on.
     * If parameter is used it should define the mechanism for waiting,
     * e.g. SMS type 0, checking in HLR.
     */
    public static final short OP_WAITON = 1;
    /** Code for wait a number for seconds.
     * The parameter defines the number of seconds.
     */
    public static final short OP_WAITTIME = 2;
    /**
     * Code for falling back to another notification type
     */
    public static final short OP_FALLBACK = 3;
    /**
     * Code for sending an SMS instead of out-dial when
     * odl is disabled for roaming. (roam phrase in cphr) +
     * Standard count/subject etc sms as defined in filter.
     */
    public static final short OP_ROAMSMS = 4;

    /**
     * Code for making an outdial call.
     */
    public static final short OP_CALL = 5;

    /** Allowed operation names. */
    public static final String[] OP_NAMES =
        {"NOOP", "WAITON", "WAIT", "FALLBACK","ROAMSMS", "CALL"};

    static
    {
        // Init state handling
        State.initOpcodes(OP_NAMES);
    }

    /** Final state. */
    public static int STATE_FINAL = -1;


    // -----------------------------------------------
    // Private data
    // -----------------------------------------------
    private int noResponseRetryPeriodHours = NORESPONSERETRYPERIOD_DEFAULT;
    private int noResponseRetryPeriodTime = -1;
    private int maxWaitHours = MAXWAIT_DEFAULT;
    private int maxWaitTime = -1;

    private int initialState = INITIAL_STATE_DEFAULT;
    private State defaultState;
    private State[] states;

    private static final int NORESPONSERETRYPERIOD_DEFAULT = 24;
    private static final int MAXWAIT_DEFAULT = 48;
    private static final int INITIAL_STATE_DEFAULT = 0;

    // Internal (temporary) event codes
    private static final int DEFAULT_EVENT_CODE = -1; // For default transition
    private static final int BAD_EVENT_CODE = -999; // Unparsable event code
    private static LogAgent logger = NtfCmnLogger.getLogAgent(CommandHandler.class);


    /**
     * Create a commandhandler with its state machine.
     * The state machine is defined by the property object
     * The allowed operations in the state machine are those
     * defined in the String array {@link #OP_NAMES}.
     * All operations can have integer parameters given after
     * the operation name (separated with space), the parameter
     * is stored in the resulting {@link Operation} object if it
     * is present but is not interpreted by this class.
     * Operations that do not have a parameter given in the state
     * machine configuration will have the default value 0 set.
     * @param props Properties that defines the state machine
     * @throws CommandException If state machine could not be created
     */
    public CommandHandler(Properties props)
        throws CommandException
    {
        int noOfStates = CommandUtil.getPropInt(props, "numberofstates", -1);
        if (noOfStates < 0) {
            throw new CommandException("Number of states must exist in properties and be positive");
        }
        states = new State[noOfStates];
        for (int i = 0; i < noOfStates; i++) {
            states[i] = new State();
        }
        defaultState = new State();
        Set<Object> keys = props.keySet();
        for (Iterator<Object> it = keys.iterator(); it.hasNext();) {
            String key = (String) it.next();
            if (key.equals("noresponseretryperiodhours")) {
                noResponseRetryPeriodHours = CommandUtil.getPropInt(props, key, NORESPONSERETRYPERIOD_DEFAULT);
                if (noResponseRetryPeriodHours == NORESPONSERETRYPERIOD_DEFAULT) {
                    calculateNoResponseRetryPeriodTime(props.getProperty(key));
                    if (noResponseRetryPeriodTime != -1) {
                        logger.debug("CommandHandler - noResponseRetryPeriodTime in ms (in minutes in .cfg) = " + noResponseRetryPeriodTime);
                    }
                }
                logger.debug("CommandHandler - noResponseRetryPeriodHours = " + noResponseRetryPeriodHours);
            } else if (key.equals("maxwaithours")) {
                maxWaitHours = CommandUtil.getPropInt(props, key, MAXWAIT_DEFAULT);
                if (maxWaitHours == MAXWAIT_DEFAULT) {
                	calculateMaxWaitTime(props.getProperty(key));
                	if (maxWaitTime != -1) {
                        logger.debug("CommandHandler - maxWaitTime in ms (in minutes in .cfg) = " + maxWaitTime);
                	}
                }
                logger.debug("CommandHandler - maxWaitHours = " + maxWaitHours);
            } else if (key.equals("initialstate")) {
                initialState = CommandUtil.getPropInt(props, key,
                                                      INITIAL_STATE_DEFAULT);
                logger.debug("CommandHandler - Initial State = " + initialState);
            } else if (key.equals("numberofstates")) {
                // Already handled
            } else if (key.startsWith("default.")) {
                String data = props.getProperty(key);
                updateDefault(key, data);
            } else if (key.startsWith("state.")) {
                String data = props.getProperty(key);
                updateState(key, data);
            } else {
            	logger.warn("CommandHandler.CommandHandler - Unknown property " + key);
            }

        }

        if (!states[0].hasTransitions() && defaultState.hasTransitions()) {
            states[0] = defaultState;
        }

        if (logger.isDebugEnabled()) {
        	String stateOprs = "";

        	HashMap<Integer, Command> transitions;

            for (int i = 0; i < noOfStates; i++) {
                stateOprs += "\nstate<" + i + ">";
                transitions = states[i].getTransitions();
                for (Iterator<Integer> it =  transitions.keySet().iterator(); it.hasNext() ; ) {
                    Integer code = it.next();
                    stateOprs += "\n" + "code <" + code + ">" + " oprs: <" + transitions.get(code) + ">";
                }

            }
        	logger.debug(stateOprs);
        }
    }

    /**
     * for testing, noResponseRetryPeriod can be specified in minute when defined as <time>m
     * @param noResponseRetryPeriod
     */
    private void calculateNoResponseRetryPeriodTime(String noResponseRetryPeriod) {
    	if (noResponseRetryPeriod.endsWith("m")) {
    	    noResponseRetryPeriod = noResponseRetryPeriod.substring(0, noResponseRetryPeriod.length()-1);
    		try {
        		this.noResponseRetryPeriodTime = Integer.parseInt(noResponseRetryPeriod) * 60 * 1000;
    		} catch (NumberFormatException e) {
    		    noResponseRetryPeriodTime = -1;
    		}
    	}
    }

    /**
     * for testing, max wait time can be specified in minute when defined as <time>m
     * @param maxTime String
     */
    private void calculateMaxWaitTime(String maxTime) {
        if (maxTime.endsWith("m")) {
            maxTime = maxTime.substring(0, maxTime.length()-1);
            try {
                this.maxWaitTime = Integer.parseInt(maxTime) * 60 * 1000;
            } catch (NumberFormatException e) {
                maxWaitTime = -1;
            }
        }
    }

    /**
     * Get number of initial state.
     * @return Initial state
     */
    public int getInitialState()
    {
        return initialState;
    }

    /**
     * Update information about default state.
     * @param key Key for state info, starting with 'default.'
     * @param data String
     * @throws CommandException If key or event code is malformed.
     */
    private void updateDefault(String key, String data) throws CommandException
    {
        StringTokenizer keyTokens = new StringTokenizer(key, ".");
        if (keyTokens.countTokens() != 2) {
            throw new CommandException("Malformed key for default state " + key +
                                       " expected 'default.<event>");
        }
        keyTokens.nextToken(); // Get rid of 'default'
        String eventCodeStr = keyTokens.nextToken();
        if (logger.isDebugEnabled()) {
        	logger.debug("CommandHandler, Updating Default " + "Code = " + eventCodeStr + " data = " + data);
        }

        int eventCode = tryParseInt(eventCodeStr, -1);
        if (eventCode < 0) {
            throw new CommandException("Malformed event code in " + data);
        }
        // Data is of format <nextstate>/<actions>
        StringTokenizer dataTokens = new StringTokenizer(data, "/");
        String nextStateStr = dataTokens.nextToken();
        String actions = "";
        if (dataTokens.hasMoreTokens()) actions = dataTokens.nextToken();
        updateOneState(defaultState, eventCode, nextStateStr, actions);

    }


    /**
     * Update information for one of the 'normal' states.
     *
     * @param key Found key, starts with 'state.'
     *        Format is state.<stateno>.<eventcode>
     * @param data Data for the key.
     *        Format is a ';' separated list of operations.
     * @throws CommandException if the data or key has bad format
     */
    private void updateState(String key, String data)
        throws CommandException
    {
        StringTokenizer keyTokens = new StringTokenizer(key, ".");
        if (keyTokens.countTokens() != 3) {
            throw new CommandException("Malformed key for state " + key);
        }
        keyTokens.nextToken(); // Get rid of 'state'
        String stateNoStr = keyTokens.nextToken();
        String eventCodeStr = keyTokens.nextToken();
        int stateNo = tryParseInt(stateNoStr, -1);
        if (stateNo < 0) {
            throw new CommandException("State no in key must be integer 0 or larger" + stateNoStr);
        }
        if (stateNo >= states.length) {
            throw new CommandException("State number too large: " + stateNo +
                                       "max value = " + (states.length - 1));
        }
        if (logger.isDebugEnabled()) {
        	logger.debug("CommandHandler, Updating State " + stateNo + " Code = " + eventCodeStr + " data = " + data);
        }

        // The eventcode can be either a number or the string "default".
        //
        int eventCode;
        if (eventCodeStr.equals("default")) {
            eventCode = DEFAULT_EVENT_CODE;
        } else {
            eventCode = tryParseInt(eventCodeStr, BAD_EVENT_CODE);
            // We should have a positive integer here, if negative it
            // either could not be parsed or a negative number was found
            // in the file, neither is allowed.
            if (eventCode < 0) {
                throw new CommandException("Event code must be 'default' or a positive integer");
            }
        }
        State state = states[stateNo];

        StringTokenizer dataTokens = new StringTokenizer(data, "/");
        String nextStateStr = dataTokens.nextToken();
        String actions = "";
        if (dataTokens.hasMoreTokens()) actions = dataTokens.nextToken();
        updateOneState(state, eventCode, nextStateStr, actions);
    }


    /**
     * Get the command to execute at a given state for a given result code.
     * The command is taken from the state if it is defined, if it is not
     * defined for the given state the default transition is returned.
     * @param stateNo Number of the state we are interested in
     * @param eventCode  The result code we have
     * @return The associated command. Null if no command found.
     * @throws CommandException i fstate not found or the event code
     *         was not found in either given state or default state.
     */
    public Command getCommand(int stateNo, int eventCode)
        throws CommandException
    {
        if ((stateNo < 0) || (stateNo >= states.length)) {
            throw new CommandException("State number out of range " + stateNo);
        }
        State state = states[stateNo];
        Command cmd = state.getTransitionCommand(eventCode);
        if (cmd == null) {
            // No transition for this state, try first default state
            // then default transition for this state
            cmd = defaultState.getTransitionCommand(eventCode);
            if (cmd == null) {
                cmd = state.getDefaultCommand();
                if (cmd == null) {
                    throw new CommandException("State " + stateNo +
                                               " has no transition for event " +
                                               eventCode);
                }
            }
        }
        return (Command)cmd.clone();

    }


    /**
     * returns one command with the operations listed after the current operation,
     * 	including the current one
     *
     *
     * @param stateNo int
     * @param eventCode int
     * @param oprCode int
     * @return Command
     * @throws CommandException exception
     */
    public Command getCommand(int stateNo, int eventCode, int oprCode)
    throws CommandException {

    if ((stateNo < 0) || (stateNo >= states.length)) {
        throw new CommandException("State number out of range " + stateNo);
    }

    State state = states[stateNo];
    Command cmd = state.getTransitionCommand(eventCode);
    if (cmd == null) {
        // No transition for this state, try first default state
        // then default transition for this state
        cmd = defaultState.getTransitionCommand(eventCode);
        if (cmd == null) {
            cmd = state.getDefaultCommand();
            if (cmd == null) {
                throw new CommandException("State " + stateNo +
                                           " has no transition for event " +
                                           eventCode);
            }
        }
    }

    Operation op = cmd.getCurrentOperation();
    if (op != null && oprCode != CommandHandler.OP_NOOP && op.getOpcode() != oprCode) {
    	cmd.operationDoneBefore(oprCode);
    }

    return (Command)cmd.clone();

}



    /**
     * Update a state with event transition.
     * @param state The state to update
     * @param eventCode Event code for action
     * @param nextStateStr String representation of next state
     * @param actions Action string
     * @throws CommandException If malformed next state or actions.
     */
    private void updateOneState(State state, int eventCode,
                                String nextStateStr, String actions)
        throws CommandException
    {
        int nextState;
        nextStateStr = nextStateStr.trim();
        if (nextStateStr.equalsIgnoreCase("end")) {
            nextState = State.STATE_END;
        } else {
            nextState = tryParseInt(nextStateStr, -1);
            if (nextState < 0) {
                throw new CommandException("Next state must be END or positive number" + nextStateStr);
            } else if (nextState >= states.length) {
                throw new CommandException("Next state is too large : " + nextStateStr);
            }

        }
        if (eventCode == DEFAULT_EVENT_CODE) {
            state.addDefaultTransition(actions, nextState);
        } else {
            state.addTransition(eventCode, actions, nextState);
        }
    }

    private static int tryParseInt(String str, int defaultValue)
    {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Get No Response Retry Period to use until Max Wait Hours (time) is reached in state machine.
     * @return Retry Period in hours.
     */
    public int getNoResponseRetryPeriodHours()
    {
        return noResponseRetryPeriodHours;
    }

    public int getNoResponseRetryPeriodTime()
    {
        return noResponseRetryPeriodTime;
    }

    /**
     * Get maximum time to wait in state machine.
     * @return Maximum time in hours.
     */
    public int getMaxWaitHours()
    {
    	return maxWaitHours;
    }

    public int getMaxWaitTime()
    {
    	return maxWaitTime;
    }

    /**
     * Get the number of states in machine (excluding default).
     * @return Number of states.
     */
    public int getNoStates()
    {
        return states.length;
    }

    /**
     * Get the states, use with care.
     */
    public State[] getStates()
    {
        return states;
    }

    /**
     * Get the default state, use with care.
     */
    public State getDefaultState()
    {
        return defaultState;
    }

}
