package com.mobeon.common.logging;

import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

import java.util.LinkedList;
import java.util.List;

/**
 * A Log4J2 filter, using the session information stored by the Log4JLogger class
 * in order to decide if a log entry shall be forwarded in the logging chain or not.
 */
@Plugin(name = "SessionFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public class SessionFilter extends AbstractFilter {
	
	//set factory to create the filter.
	@PluginFactory
    public static SessionFilter createFilter(@PluginAttribute("traceItem") final String traceItemv ) {
		final String traceItem = traceItemv == null ? "" : traceItemv;
        return new SessionFilter(traceItem);
    }
	
    class MdcItem {
        String name;
        String value;

        public MdcItem(String name, String value) {
            this.name = name;
            this.value = value;
        }

        boolean equals(String name, String value) {
            return this.name.equals(name) && this.value.equals(value);
        }
    }

    static List<MdcItem> mdcItems = new LinkedList<MdcItem>();

    public SessionFilter() {
        mdcItems.clear();
    }
    
    public SessionFilter(final String traceItem) {
    	setTraceItem(traceItem);
    }
    
    protected void setTraceItem(String traceItem) {
        traceItem = traceItem.trim();
        if (traceItem.length() > 0 && traceItem.contains(":")) {
            int separator = traceItem.indexOf(':');
            String name = traceItem.substring(0, separator);
            String value = traceItem.substring(separator + 1);
            mdcItems.add(new MdcItem(name, value));
        }
        
    }
    
    /**
     * @param event
     * @return Indication for the filter chain in Log4J whether the log item should be dropped or not.
     *         Should be NEUTRAL, ACCEPT or DENY.
     */
    private Result decide(ReadOnlyStringMap eventMDC) {
        Result result = Result.NEUTRAL; // If no match
        if (mdcItems.isEmpty()) {
        	return result;
        }
        
        if (!acceptMdcEntry(eventMDC)) {
            result = Result.DENY;
        } else {
            result = Result.ACCEPT;
        }
        return result;
    }

    //If any of the values match
    private boolean acceptMdcEntry(ReadOnlyStringMap eventMDC) {
        boolean nameMatch = false;
        if (mdcItems.size() == 0) {
            return true;
        }
        for (MdcItem mdcItem : mdcItems) {
            String value = (String) eventMDC.getValue(mdcItem.name);
            if (value != null) {
                nameMatch = true;
                if (value.equals(mdcItem.value)) {
                    // Found a matching value
                    return true;
                }
            }
        }
        return !nameMatch;
    }
    
    @Override
    public Result filter(final LogEvent event) {
    	ReadOnlyStringMap eventMDC = event.getContextData();
    	return decide(eventMDC);
    }
}
