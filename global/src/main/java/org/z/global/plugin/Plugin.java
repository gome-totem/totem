
package org.z.global.plugin;


import java.util.Collection;
import java.util.HashMap;

public interface Plugin {

    String name();

    String description();
    Collection<Class<? extends Module>> modules();
    Collection<? extends Module> modules(HashMap<String,Object> settings);

    Collection<Class<? extends Module>> indexModules();

    Collection<? extends Module> indexModules(HashMap<String,Object> settings);

    Collection<Class<? extends Module>> shardModules();

    Collection<? extends Module> shardModules(HashMap<String,Object> settings);


    void processModule(Module module);

    HashMap<String,Object> additionalSettings();
}
