package com.mobeon.masp.mediacontentmanager.condition;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import org.mozilla.javascript.*;

import java.util.Date;

/**
 * {@link ConditionInterpreter} that uses Rhino http://www.mozilla.org/rhino/
 * to evalutate expressions.
 * <p/>
 * Singleton.
 *
 * @author Mats Egland
 */
public class RhinoInterpreter implements ConditionInterpreter {
    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER = ILoggerFactory.getILogger(RhinoInterpreter.class);
    /**
     * The javascript scope this interpreter executes in.
     */
    private ScriptableObject globalScope;
    /**
     * The singleton instance.
     */
    private static RhinoInterpreter instance;

    /**
     * Private as singleton.
     */
    private RhinoInterpreter() {/*private in order to be singleton */}

    /**
     * Returns a singleton instance.
     *
     * @return The singleton RhinoInterpreter instance.
     */
    public static synchronized RhinoInterpreter getInstance() {
        if (instance == null) {
            instance = new RhinoInterpreter();
        }
        return instance;
    }

    public boolean interpretCondition(Condition cond, IMediaQualifier[] qualifiers)
            throws ConditionInterpreterException {

        String condString = cond.getCondition();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Passing expr to rhino:" + condString);
        }
        if (qualifiers != null) {
            for (IMediaQualifier q : qualifiers) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Qualifier name: " + q.getName() +
                            ", value: " + q.getValue());
                }
            }
        }
        Context cx = Context.enter();
        if (globalScope == null) {
            createGlobalScope(cx);
        }
        Scriptable localScope = createLocalScope(cx, globalScope, qualifiers);
        try {
            Object result = cx.evaluateString(localScope, condString, "<cmd>", 1, null);
            if (result instanceof Boolean) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Expression=" + condString + " was evalutated to " + result);
                }
                return (Boolean) result;
            } else {
                throw new ConditionInterpreterException("Failed to interpret expression:"
                        + condString
                        + ". Result is of illegal type " + result.getClass());
            }
        } catch (EcmaError e) {
            StringBuffer msg = createExceptionMessage(condString, cond, qualifiers);
            throw new ConditionInterpreterException(msg.toString(), e);
        } catch (EvaluatorException e) {
            StringBuffer msg = createExceptionMessage(condString, cond, qualifiers);
            throw new ConditionInterpreterException(msg.toString(), e);
        } finally {

            Context.exit();
        }
    }

    /**
     * Creates the local scope, i.e. the scope that will contain
     * the parameters for the current expression.
     *
     * @param cx
     * @param globalScope The parent global scope.
     */
    private Scriptable createLocalScope(
            Context cx,
            ScriptableObject globalScope,
            IMediaQualifier[] qualifiers) {
        LocalScope localScope = new LocalScope();
        localScope.setPrototype(globalScope);

        localScope.setParentScope(null);
        if (qualifiers != null && qualifiers.length > 0) {
            for (IMediaQualifier qualifier : qualifiers) {
                if (qualifier.getType() != IMediaQualifier.QualiferType.MediaObject) {
                    if (qualifier.getValueType() == Date.class) {
                        Object fObj = globalScope.get("javaToJsDate", globalScope);
                        Object functionArgs[] = {qualifier.getValue()};
                        Function f = (Function) fObj;
                        Object result = f.call(cx, globalScope, globalScope, functionArgs);
                        localScope.put(qualifier.getName(), localScope,
                                result);
                    } else {

                        localScope.put(qualifier.getName(), localScope,
                                qualifier.getValue());
                    }
                }
            }
        }
        return localScope;
    }

    /**
     * Builds a good message if the specified condition fails to be interpreted.
     *
     * @param condString The condition string.
     * @param cond       The condition.
     * @param qualifiers The input qualifiers.
     * @return A StringBuffer holding the message.
     */
    private StringBuffer createExceptionMessage(String condString, Condition cond, IMediaQualifier[] qualifiers) {
        StringBuffer msg = new StringBuffer("Failed to interpret expression [" + condString + "].");
        msg.append("The condition without input is [");
        msg.append(cond.getCondition());
        msg.append("].");
        if (qualifiers != null) {
            msg.append("The input qualifiers are:\n");
            for (IMediaQualifier q : qualifiers) {
                msg.append("\t");
                msg.append(q.getName());
                msg.append("=");
                msg.append(q.getValue());
                msg.append("\n");
            }
        }
        msg.append("There may be a mismatch between the input qualifiers and the condition.  " +
                "Check the condition!\n");
        return msg;
    }


    /**
     * Creates the global scope used by all threads. This scope
     * contains all the necessary data types.
     *
     * @param context
     */
    private synchronized void createGlobalScope(Context context) {
        if (globalScope == null) {
            this.globalScope = context.initStandardObjects();
            addTypes(context, globalScope);
        }
    }

    /**
     * Adds support for the following javascript constructors:
     * <ul>
     * <li>DateDM('yyyy-MM-dd')</li>
     * <li>CompleteDate('yyyy-MM-dd HH:mm:ss Z')</li>
     * <li>WeekDay('yyyy-MM-dd')</li>
     * <li>Time12('HH:mm:ss')</li>
     * <li>Time24('HH:mm:ss')</li>
     * <p/>
     * </ul>
     *
     * @param cx    The context to add support to.
     * @param scope The scriptable scope.
     */
    private void addTypes(Context cx, Scriptable scope) {

        cx.evaluateString(scope, "function DateDM(arg) {" +
                "var sdf = new java.text.SimpleDateFormat(\"yyyy-MM-dd\");" +
                "var date = new Date();" +
                "var javaDate = sdf.parse(arg);" +
                "date.setTime(javaDate.getTime());" +
                "return date;" +
                "}", "<cmd>", 1, null);
        //cx.evaluateString(scope, "WeekDay = DateDM",
        //        "<cmd>", 1, null);
        cx.evaluateString(scope, "function WeekDay(arg) {" +
                "var sdf = new java.text.SimpleDateFormat(\"yyyy-MM-dd\");" +
                "var date = new Date();" +
                "var javaDate = sdf.parse(arg);" +
                "date.setTime(javaDate.getTime());" +
                "return date;" +
                "}", "<cmd>", 1, null);
        cx.evaluateString(scope, "function CompleteDate(arg) {" +
                "var sdf = new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss Z\");" +
                "var date = new Date();" +
                "var javaDate = sdf.parse(arg);" +
                "date.setTime(javaDate.getTime());" +
                "return date;" +
                "}", "<cmd>", 1, null);
        cx.evaluateString(scope, "function Time12(arg) {" +
                "var sdf = new java.text.SimpleDateFormat(\"HH:mm:ss\");" +
                "var date = new Date();" +
                "var javaDate = sdf.parse(arg);" +
                "date.setTime(javaDate.getTime());" +
                "return date;" +
                "}", "<cmd>", 1, null);
        cx.evaluateString(scope, "function javaToJsDate(arg) {" +
                "var date = new Date();" +
                "date.setTime(arg.getTime());" +
                "return date;" +
                "}", "<cmd>", 1, null);
        //cx.evaluateString(scope, "Time24 = Time12",
        //        "<cmd>", 1, null);
        cx.evaluateString(scope, "function Time24(arg) {" +
                "var sdf = new java.text.SimpleDateFormat(\"HH:mm:ss\");" +
                "var date = new Date();" +
                "var javaDate = sdf.parse(arg);" +
                "date.setTime(javaDate.getTime());" +
                "return date;" +
                "}", "<cmd>", 1, null);
    }

    /**
     * A <code>ScriptableObject</code> that provides a local scope for each request to
     * interpretCondition.
     */
    private class LocalScope extends ScriptableObject {

        public String getClassName() {
            return "LocalScope";
        }
    }
}
