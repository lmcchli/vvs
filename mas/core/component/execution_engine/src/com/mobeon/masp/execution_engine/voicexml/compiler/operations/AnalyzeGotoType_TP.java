package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.Tools;
import static com.mobeon.masp.util.Tools.*;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jan 4, 2006
 * Time: 11:30:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnalyzeGotoType_TP extends VXMLOperationBase {

    public enum GotoType {
        ROOT_TO_LEAF_WITHIN_APP,
        INTER_APPLICATION,
        LEAF_TO_LEAF_WITHIN_APP,
        LEAF_TO_ROOT_WITHIN_APP,
        WITHIN_DOC_NO_REFETCH,
        UNKNOWN}

    static ILogger logger = ILoggerFactory.getILogger(AnalyzeGotoType_TP.class);

    Product containingForm;

    public AnalyzeGotoType_TP(Product containingForm) {
        this.containingForm = containingForm;
    }

    public void execute(VXMLExecutionContext context) throws InterruptedException {
        List<Value> values = context.getValueStack().popToMark();
        Value v = values.get(0); // target is the first item

        String target = (String) v.accept(context, Visitors.getAsStringVisitor());
        if (logger.isInfoEnabled()) {
            logger.info("Goto to " + target + " requested");
        }

        ModuleCollection moduleCollection = context.getModuleCollection();


        String documentURI = documentURI(target);
        String fragment = fragmentOfURI(target);

        Module targetModule = findModule(context, documentURI, moduleCollection, values);
        if (targetModule == null) {
            badfetch(context, target);
            return;
        }
        Product targetProduct = findTargetProduct(targetModule, fragment);
        if (targetProduct == null) {
            badfetch(context, target);
            return;
        }

        // We need to unwind the engine stack to a certain point,
        // depending on what type of goto this is.
        GotoType t = findGotoType(
                context.getExecutingModule(), targetModule, documentURI);

        //Bail out if we don't know what to do.
        if (t == GotoType.UNKNOWN) {
            badfetch(context, target);
            return;
        }
        Product unwindTarget = findUnwindTarget(context, t, targetModule);

        if (unwindTarget == null) {
            badfetch(context, target);
            return;
        } else {
            context.getValueStack().push(unwindTarget);
            context.getValueStack().pushMark();
            if (t == GotoType.WITHIN_DOC_NO_REFETCH) {
                context.getValueStack().
                        push(targetProduct);
            } else {
                context.getValueStack().push(targetModule.getSpecialProduct(
                        Module.VXML_PRODUCT));
                context.setProductToBeExecuted(targetProduct);
            }
        }
    }

    private Product findTargetProduct(Module targetModule, String productName) {
        // If there is no name of the product it means that we get the first
        // in the module.
        if (isEmpty(productName)) {
            return targetModule.getSpecialProduct(Module.FIRST_DIALOG);
        } else {
            return targetModule.getNamedProduct(productName);
        }
    }

    /**
     * @param context
     * @param targetURI
     * @logs.error "Goto from <fromURI> to <toURI is invalid" - The application requsted a goto from <fromURI> to <toURI> but this URI is invalid, maybe since <toURI> does not exist.
     */
    private void badfetch(ExecutionContext context,
                          String targetURI) {
        String msg = "Goto from " + context.getExecutingModule().getDocumentURI()
                + " to " + targetURI + " is invalid";
        logger.error(msg);
        context.getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH,
                msg, DebugInfo.getInstance());
    }

    private Product findUnwindTarget(VXMLExecutionContext context, GotoType t, Module targetModule) {
        switch (t) {
            case WITHIN_DOC_NO_REFETCH:

                return context.getExecutingModule().getSpecialProduct(
                        Module.DIALOG_TRAMPOLINE_PRODUCT);
            case ROOT_TO_LEAF_WITHIN_APP:
            case LEAF_TO_LEAF_WITHIN_APP:
                Module m = rootOf(context.getExecutingModule());
                return m.getSpecialProduct(
                        Module.DIALOG_TRAMPOLINE_PRODUCT);
            case LEAF_TO_ROOT_WITHIN_APP:
                return getRootProduct(context);
            case INTER_APPLICATION:
                //We know of the root
                if (rootOf(targetModule) != null) {
                    //Unwind entire stack
                    return context.getExecutingModule().getProduct();
                }
        }
        return null;
    }

    Module findModule(VXMLExecutionContext executionContext, String uriPartBeforeHash,
                      ModuleCollection moduleCollection, List<Value> values) {

        // It can either be the currently executing module or one of the others.
        if (isEmpty(uriPartBeforeHash)) {
            return executionContext.getExecutingModule();
        }

        String maxage = getMaxage(values, executionContext);
        String fetchTimeout = getFetchTimeout(values, executionContext);
        return executionContext.getResourceLocator().getDocument(Tools.relativize(uriPartBeforeHash), maxage, fetchTimeout, moduleCollection);
    }

    /**
     * Finds the element of the {@link GotoType} best describing how
     * to go from the currently executing module to {@param B}.
     * <br/>
     * <ol><li>A isFragmentOnly(uriOfB) WITHIN_DOC
     * <br><em>No refetch.</em></li>
     * <li>hasNoRoot(A) hasNoRoot(B) root root
     * <br><em>Can be considered as, and covers inter-application goto also.</em></li>
     * <li>rootOf(A) rootOf(B) leaf leaf</li>
     * <li>rootOf(A) B  leaf root</li>
     * <li>A rootOf(B)  root leaf</li>
     * </ol>
     *
     * @param A      The module to go from ( the source )
     * @param B      The module to go to ( the target )
     * @param uriOfB The complete uri of target, including any fragmen identifier.
     */
    private GotoType findGotoType(Module A, Module B, String uriOfB) {
        if (B == A && isFragmentOnly(uriOfB)) {
            return GotoType.WITHIN_DOC_NO_REFETCH;
        }

        if (hasNoRoot(A) && hasNoRoot(B))
            return GotoType.INTER_APPLICATION;

        if (rootOf(A) == rootOf(B))
            return GotoType.LEAF_TO_LEAF_WITHIN_APP;

        if (rootOf(A) == B)
            return GotoType.LEAF_TO_ROOT_WITHIN_APP;

        if (A == rootOf(B))
            return GotoType.ROOT_TO_LEAF_WITHIN_APP;

        if (applicationURI(B) == null)
            return GotoType.INTER_APPLICATION;

        return GotoType.UNKNOWN;
    }

    private boolean hasNoRoot(Module a) {
        return rootOf(a) == null;
    }

    private String applicationURI(Module A) {
        return A.getDocumentAttribute(Constants.VoiceXML.APPLICATION);
    }

    private boolean isFragmentOnly(String uri) {
        return (uri == null) || (uri.length() == 0);
    }

    public String arguments() {
        return "";
    }

    private Product getRootProduct(ExecutionContext context) {

        // Find the root product according to the "application" attribute

        Module root = rootOf(context.getExecutingModule());
        if (root != null) {
            return root.getSpecialProduct(
                    Module.VXML_PRODUCT);
        }
        return null;
    }

    private Module rootOf(Module module) {

        // Find the root module according to the "application" attribute
        String applicationAttr = applicationURI(module);
        if (isEmpty(applicationAttr)) {
            // the executing module is root
            return module;
        } else {
            return module.getParent().get(applicationAttr);
        }
    }

    /**
     * Retrieves maxage parameter, either from an attribute or a property
     *
     * @param values
     * @param ex
     * @return the maxage string
     */
    private String getMaxage(List<Value> values, VXMLExecutionContext ex) {
        String maxage = null;
        if (values.size() > 2) {
            maxage = values.get(2).toString();
        }
        if (maxage == null) {
            maxage = ex.getProperty(Constants.VoiceXML.DOCUMENTMAXAGE);
        }
        return maxage;
    }

    /**
     * Retrieves fetchtimeout parameter, either from an attribute or a property
     *
     * @param values
     * @param ex
     * @return the fetchtimeout string
     */
    private String getFetchTimeout(List<Value> values, VXMLExecutionContext ex) {
        String fetchTimeout = null;
        if (values.size() > 1) {
            fetchTimeout = values.get(1).toString();
        }
        if (fetchTimeout == null) {
            fetchTimeout = ex.getProperty(Constants.VoiceXML.FETCHTIMEOUT);
        }
        return fetchTimeout;
    }
}
