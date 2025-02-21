/*
 * State.java
 *
 * Created on den 26 augusti 2004, 20:59
 */

package com.mobeon.common.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;

/**
 * Holds information about one state in the state machine.
 */
public class State
{

    /** State number for final state in state machines. */
    public static final int STATE_END = -1;

    /** Map from event code to Command */
    private HashMap<Integer, Command> transitions;
    /** Command to use if not explicit event code. */
    private Command defaultCommand = null;
    /** Map from opcode to opname. */
    private static String[] codeToOp;
    /** Map from opname to opcode. */
    private static HashMap<String, Short> opToCode;
    private static LogAgent logger = NtfCmnLogger.getLogAgent(State.class);


    /**
     * Initiate with known noperations.
     * @param knownOps String array with opcodes, ordered according to opcode
     */
    public static void initOpcodes(String[] knownOps)
    {
        codeToOp = new String[knownOps.length];
        opToCode = new HashMap<String, Short>();
        for (int i = 0; i < codeToOp.length; i++) {
            codeToOp[i] = knownOps[i].toLowerCase();
            opToCode.put(codeToOp[i], new Short((short) i));
        }
    }

    /**
     * Creates a new instance of State
     * The new state will have the transitions defined by the
     * given state number in the properties.
     *      */
    public State()
    {
        transitions = new HashMap<Integer, Command>();
    }

    public boolean hasTransitions() {
    	return transitions.size() > 0;
    }
    /**
     * Add a transition from this state.
     * The transition is to be done for the given event eventCode,
     * to given next state, using given operationss.
     * @param eventCode Event eventCode
     * @param operations ',' separated list of operations with optional int parameter.
     *        Example: <code>OPA 20,OPB,OPC 10</code> The operation names must
     *        exist in the knownOps array used to initialize the State handling.
     * @param nextState next state to go to after operations are done
     * @throws CommandException if operaion string was not well formed or
     *         if unknown operations was found
     */
    public void addTransition(int eventCode,
                              String operations,
                              int nextState)
        throws CommandException
    {
        List<Operation> ops = parseOperations(operations);
        Command cmd = new Command(nextState, ops);
        transitions.put(new Integer(eventCode), cmd);
    }

    /**
     * Add the default transition for this state.
     * The transition will be used when a transition is requested
     * for an event code that is not explicitly defined.
     * @param operations ',' separated list of operations with optional int parameter.
     *        Example: <code>OPA 20,OPB,OPC 10</code> The operation names must
     *        exist in the knownOps array used to initialize the State handling.
     * @param nextState next state to go to after operations are done
     * @throws CommandException if operaion string was not well formed or
     *         if unknown operations was found
     */
    public void addDefaultTransition(String operations, int nextState)
        throws CommandException
    {
        List<Operation> ops = parseOperations(operations);
        defaultCommand = new Command(nextState, ops);
    }

    /**
     * construct an operation's string formatted presentation for keeping one command's operations list
     *
     * @param command Command
     * @return string
     */
    public static String constructOperations(Command command) {
    	List<Object> ops = command.getOperations();
    	String operations = "";
    	int size = ops.size();

    	int i = 0;
        for (Iterator<Object> it = ops.iterator(); it.hasNext();) {
            Operation op = (Operation) it.next();
            if (i == (size -1)) {
                operations += op.getOpcode() + " " + op.getParam();
            } else {
                operations += op.getOpcode() + " " + op.getParam() + ";";
            }
            ops.add(op.clone());
        }

    	return operations;
    }

    /**
     * Get all operations from a string.
     * @param operations String with ',' separated operations
     * @return List of all found operations in order
     * @throws CommandException If unknown operation found, or parameter not an integer,
     *  or otherwise bad syntax.
     */
    private static List<Operation> parseOperations(String operations)
        throws CommandException
    {
        LinkedList<Operation> result = new LinkedList<Operation>();
        // Go through all individual operations
        StringTokenizer tokens = new StringTokenizer(operations, ";");
        while (tokens.hasMoreTokens()) {
            // Each operation is of the form OPNAME followed by an optional
            // space and integer parameter
            String opStr = tokens.nextToken();
            // Split into opname and parameter
            StringTokenizer cmdTokens = new StringTokenizer(opStr, " ");
            if (cmdTokens.hasMoreTokens()) {
                Operation op = parseOneOperation(cmdTokens, operations);
                logger.debug("Made operation : " + op);
                result.add(op);
            } else {
                // Badly formed command
                throw new CommandException("Bad operations string + " + operations);
            }
        }
        return result;
    }

    private static Operation parseOneOperation(StringTokenizer cmdTokens, String operations)
        throws CommandException
    {
        String opName = cmdTokens.nextToken().toLowerCase();
        String paramStr = "";
        if (cmdTokens.hasMoreTokens()) {
            // The parameter
            try {
                paramStr = cmdTokens.nextToken();
                // No more than two parts of op!
                if (cmdTokens.hasMoreTokens()) {
                    String extra = cmdTokens.nextToken();
                    throw new CommandException("Extra command token found in " + operations +
                                               " extra=" + extra);
                }
            } catch (NumberFormatException nfe) {
                throw new CommandException("Parameter not integer: " + paramStr);
            }
        }
        Short opCode = opToCode.get(opName);
        if (opCode == null) {
            throw new CommandException("Unknown opname : " + opName + " in " + operations);
        }
        Operation op = new Operation(opCode.shortValue(), opName, paramStr);
        return op;
    }


    /**
     * Get the command for an event code.
     * Get the Command that handles the transition from
     * this state given the event code.
     * @param eventCode event code
     * @return Command associated with the code. If no transition is explicitly
     *         defined for the given code null is returned.
     */
    public Command getTransitionCommand(int eventCode)
    {
        Command found = transitions.get(new Integer(eventCode));
        return found;
    }

    /**
     * Get the default command.
     */
    public Command getDefaultCommand()
    {
        return defaultCommand;
    }

    /**
     * Get the known transitions, used for verifying.
     */
    public HashMap<Integer, Command> getTransitions()
    {
        return transitions;
    }

    static public String getOprName (int code) {
    	if (code < codeToOp.length) {
    		return codeToOp[code];
    	} else {
    		return null;
    	}
    }

}
