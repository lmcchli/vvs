/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.compiler.CompilerBookkeeping;
import com.mobeon.masp.execution_engine.Module;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class VXMLCompilerBookkeeping extends CompilerBookkeeping {
    /*
   HashMaps used during precompile passes to resolve inter module references
    */
    Map<String, Module> app_module = new HashMap<String, Module>();

    Map<String, List<Module>> unresolved_leaf_module = new HashMap<String, List<Module>>();


    public Module getApplicationModule(final String uri) {
        return app_module.get(uri);
    }

    public void addApplicationModule(final String uri, final Module module) {
        app_module.put(uri, module);
    }

    public List<Module> getUnresolvedLeafModules(final String uri) {
        return unresolved_leaf_module.get(uri);
    }

    public void addUnresolvedLeafModule(final String uri, final Module module) {
        List<Module> list = unresolved_leaf_module.get(uri);
        if (list == null) {
            list = new ArrayList<Module>();
            list.add(module);
            unresolved_leaf_module.put(uri, list);
        } else {
            list.add(module);
        }
    }

}
