package com.mobeon.ntf.out.outdial.verify;

import java.io.*;
import java.util.*;
import com.mobeon.common.commands.CommandHandler;
import com.mobeon.common.commands.Command;
import com.mobeon.common.commands.Operation;
import com.mobeon.common.commands.CommandException;
import com.mobeon.common.commands.CHLogger;

/**
 * Checks an outdial configuration file for errors and warnings.
 * Errors are reported for a configuration file that will not
 * work at all, this is mainly because it has syntax errors
 * or that the machine is missing the initial transition.
 * Warnings are given for configurations that might lead
 * to unexpected problems such as calling again after a user
 * has answered or paths.
 * The warnings are divided into High, Medium and Low priorites
 * depending on their consequences and the probablity that
 * the configuration is a mistake. The user of the program should
 * ensure that the reasons for all warnings, even low priority
 * ones are well understood and that the warning is either
 * fixed or known to not be a problem in the actual installation.
 */
public class Verifyer
    implements VerifyerBase
{

    private Properties props;

    /** Creates a new instance of Verifyer */
    public Verifyer(Properties props)
    {
        this.props = props;
    }

    public void validate(int level)
    {
        CommandHandler handler = null;
        try {
            handler = new CommandHandler(props);
            if (level >= LEVEL_ALL) {
                System.out.println("Created Handler, no of states = " +
                                   handler.getNoStates());
            }
        } catch (CommandException ce) {
            System.out.println("ERROR: Could not create handler : " + ce);
            return; // Cannot check anything more
        }
        new InitialTransitionVerifyer(handler).validate(level);
        new CodeVerifyer(handler).validate(level);
        new CommandVerifyer(handler).validate(level);
        new StateVerifyer(handler).validate(level);
    }

    public static void usage()
    {
        System.out.println("Usage: " + Verifyer.class.getName() +
            "<parameters> outdialconfigfile");
        System.out.println("Allowed parameters");
        System.out.println("-level Error|High|Medium|Low");
        System.out.println("   Only report problems at given level or higher");
    }

    private static int getLevel(String levelStr)
    {
        if (levelStr.equals("Error")) {
            return LEVEL_ERROR;
        } else if (levelStr.equals("High")) {
            return LEVEL_HIGH;
        } else if (levelStr.equals("Medium")) {
            return LEVEL_MEDIUM;
        } else if (levelStr.equals("Low")) {
            return LEVEL_LOW;
        } else if (levelStr.equals("All")) {
            return LEVEL_ALL;
        } else {
            System.out.println("WARNING : Unknown level " + levelStr +
                               "Showing all errors and warnings but no extra info");
            return LEVEL_LOW;
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        CHLogger.setLevel(CHLogger.FATAL); // Log as little as possible
        File f = new File(".");
        System.out.println("Dir : " + f.getCanonicalPath());
        if ((args.length % 2)== 0) {
            usage();
            System.exit(1);
        }
        int level = LEVEL_LOW;

        for (int count = 0; count < (args.length /2); count ++) {
            String arg = args[count*2];
            String argparam = args[count*2 + 1];
            if (arg.equals("-level")) {
                level = getLevel(argparam);
            } else if (arg.equals("-config")) {
                System.out.println("WARNING : Use of configfile not implemented yet");
            }
        }

        String dialConfig = args[args.length - 1];
        Properties props = new Properties();
        try {
            BufferedInputStream br = new BufferedInputStream(
                     new FileInputStream(dialConfig));
            props.load(br);
            if (level >= LEVEL_ALL) {
                System.out.println("-- Loaded proerties -- ");
                props.list(System.out);
                System.out.println("-------------------------");
            }
        } catch (IOException ioe) {
            System.out.println("ERROR: Could not load property file: " + dialConfig);
            System.out.println("  Reason: " + ioe);

            System.exit(1);
        }
        
        Verifyer v = new Verifyer(props);
        v.validate(level);
    }

}
