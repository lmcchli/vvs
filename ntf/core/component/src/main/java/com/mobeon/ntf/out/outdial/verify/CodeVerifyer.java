package com.mobeon.ntf.out.outdial.verify;

import java.util.*;
import com.mobeon.common.commands.CommandHandler;
import com.mobeon.common.commands.Command;
import com.mobeon.common.commands.Operation;
import com.mobeon.common.commands.CommandException;
import com.mobeon.common.commands.State;
import com.mobeon.ntf.out.outdial.OdlInfo;


/**
 * Verify that all codes are handled and no unknown codes used.
 */
public class CodeVerifyer implements VerifyerBase
{
    private CommandHandler handler;
    private int[] allDefinedCodes = {
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
        OdlInfo.EVENT_CODE_PREPAID_FAILURE,
        // End of codes in 600-series
        OdlInfo.EVENT_CODE_DEFAULT,
        OdlInfo.EVENT_CODE_NOTIFDISABLED,
        OdlInfo.EVENT_CODE_LOCATION_FAILURE,
        OdlInfo.EVENT_CODE_CFU_ON,
        OdlInfo.EVENT_CODE_CFU_FAILURE,
        OdlInfo.EVENT_CODE_INTERRUPTED,
        OdlInfo.EVENT_CODE_PHONEON
    };



    private int[] allowedUndefinedCodes = {
        // Undefined codes in 600-series
        601, 602, 604, 605, 606, 607, 608, 609, 611,
        612, 615, 616, 617, 618, 619, 621, 622, 623,
        624, 626, 627, 628, 629, 630, 631, 632,
        633, 634,
    };

    /** Creates a new instance of InitialTransitionVerifyer */
    public CodeVerifyer(CommandHandler handler)
    {
        this.handler = handler;


    }

    public void validate(int level)
    {
        doValidateAllDefinedCodes(level);
        doValidateOkResponse(level);
        doValidateUnknownCodes(level);
    }


    private void doValidateAllDefinedCodes(int level)
    {
        if (level >= LEVEL_HIGH) {
            System.out.println("Checking: All codes handled in all states");
            for (int stateNo = 0; stateNo < handler.getNoStates(); stateNo++) {
                for (int i =0; i<allDefinedCodes.length; i++) {
                    int code = allDefinedCodes[i];
                    if ((code != OdlInfo.EVENT_CODE_DEFAULT) &&
                        (stateNo != handler.getInitialState() ) ) {
                        checkCode(stateNo,code);
                    }
                }
                // Also check that initial state has default code
                checkCode(handler.getInitialState(), OdlInfo.EVENT_CODE_DEFAULT);
            }
        }
    }

    private void checkCode(int stateNo, int code) {
        try {
            Command ignore = handler.getCommand(stateNo, code);
        } catch (CommandException cd) {
            System.out.println("Warning/High: " +
            "Code " + code + " not handled in state " +
            stateNo + " May lead to runtime errors");
        }
    }

    private List makeCodeList()
    {
        List codeList = new ArrayList();
        for (int i=0; i<allDefinedCodes.length; i++) {
            codeList.add(new Integer(allDefinedCodes[i]));
        }
        for (int i=0; i<allowedUndefinedCodes.length; i++) {
            codeList.add(new Integer(allowedUndefinedCodes[i]));
        }
        return codeList;
    }

    private void doValidateOkResponse(int level)
    {
        if (level >= LEVEL_HIGH) {
            System.out.println("Checking: Ok response, to end state and no actions");
            State[] states = handler.getStates();
            for (int stateNo =0; stateNo<states.length; stateNo++) {
                checkOkResponse("state number " + stateNo, states[stateNo]);
            }
            checkOkResponse("default state",
                              handler.getDefaultState());
        }
    }


    private void doValidateUnknownCodes(int level)
    {
        if (level >= LEVEL_MEDIUM) {
            System.out.println("Checking: No unknown codes");
            List codeList = makeCodeList();
            State[] states = handler.getStates();
            for (int stateNo =0; stateNo<states.length; stateNo++) {
                checkUnknownCodes("state number " + stateNo,
                                    states[stateNo], codeList);
            }
            checkUnknownCodes("default state",
                              handler.getDefaultState(),
                              codeList);
        }
    }

    /**
     * Check for OK responses not leading to END state.
     */
    private void checkOkResponse(String stateName, State state)
    {
        Command c = state.getTransitionCommand(OdlInfo.EVENT_CODE_COMPLETED);
        if (c == null) {
            // No mapping, this is handled elsewhere
            return;
        }
        if (c.getNextState() != CommandHandler.STATE_FINAL) {
            System.out.println("Warning/High: Transition on successful" +
            " call (" + OdlInfo.EVENT_CODE_COMPLETED + ") " +
            "from state " + stateName + " does not " +
            "lead to end state");
        }
        if (c.getOperationCount() > 0) {
            System.out.println("Warning/High: Operations made after " +
                "succesful call from state " + stateName);
        }
    }


    /**
     * Check for codes that is not used.
     */
    private void checkUnknownCodes(String stateName, State state, List codeList)
    {
        HashMap transitions = state.getTransitions();
        for (Iterator it =  transitions.keySet().iterator(); it.hasNext() ; ) {
            Integer code = (Integer)it.next();
            if (codeList.indexOf(code) < 0) {
                System.out.println("Warning/Medium: Unknown code " +
                    code + " found for state " + stateName);
            }
        }
    }

}
