/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.util.test.MASTestSwitches;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

public class DebugInfo {

    private static class DebugInfoProvider {
        public String getKind() { return testResult(""); }
        public String getName() { return testResult(""); }
        public String getLocation() { return testResult(""); }
        public String asString(){ return testResult(this.getClass().getName()); }
    }

    private static class ElementInfoProvider extends DebugInfoProvider {
        private CompilerElement element;
        public ElementInfoProvider(CompilerElement compilerElement) { this.element = compilerElement; }
        public String getKind() { return testResult("Tag"); }
        public String getName() { return testResult(element.getName()); }
        public String getLocation() { return testResult(element.getLine()+","+element.getColumn()); }
        public String asString() { return testResult(getKind()+ "{ tag='" +getName() +"' location=("+getLocation()+") }"); }
    }

    private static class ConnectionInfoProvider extends DebugInfoProvider {
        private Connection connection;
        public ConnectionInfoProvider(Connection connection) { this.connection = connection;  }
        public String getKind() { return testResult("Connection"); }
        public String getConnectionId() { return testResult(connection.getBridgePartyId()); }
        public String asString() { return testResult(getKind()+"{ id="+getConnectionId()+" }"); }

    }

    public static String testResult(String str) {
        if(MASTestSwitches.isCompilerTesting())
            return "UnitTest";
        else
            return str;
    }

    private DebugInfoProvider provider;

    public DebugInfo(DebugInfoProvider provider) {
        this.provider = provider;
    }

    public String getTagName() {
        return provider.getName();
    }

    public Object getLocation() {
        return provider.getLocation();
    }

    public static DebugInfo getInstance(CompilerElement element) {
        return new DebugInfo(new ElementInfoProvider(element));
    }
    public static DebugInfo getInstance() {
        return new DebugInfo(new DebugInfoProvider());
    }

    public static DebugInfo getInstance(Connection connection) {
        return new DebugInfo(new ConnectionInfoProvider(connection));

    }


    public String toString() {
       return provider.asString();
    }
}
