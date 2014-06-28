
package org.z.global.plugin;


import java.util.Collection;
import java.util.HashMap;

public interface Plugin {

    /**
     * The name of the plugin.
     */
    String name();

    /**
     * The description of the plugin.
     */
    String description();

    /**
     * Node level modules (classes, will automatically be created).
     */
    Collection<Class<? extends Module>> modules();

    /**
     * Node level modules (instances)
     *
     * @param settings The node level settings.
     */
    Collection<? extends Module> modules(HashMap<String,Object> settings);

    /**
     * Node level services that will be automatically started/stopped/closed.
     */

    /**
     * Per index modules.
     */
    Collection<Class<? extends Module>> indexModules();

    /**
     * Per index modules.
     */
    Collection<? extends Module> indexModules(HashMap<String,Object> settings);

    /**
     * Per index services that will be automatically closed.
     */

    /**
     * Per index shard module.
     */
    Collection<Class<? extends Module>> shardModules();

    /**
     * Per index shard module.
     */
    Collection<? extends Module> shardModules(HashMap<String,Object> settings);

    /**
     * Per index shard service that will be automatically closed.
     */

    /**
     * Process a specific module. Note, its simpler to implement a custom <tt>onModule(AnyModule module)</tt>
     * method, which will be automatically be called by the relevant type.
     */
    void processModule(Module module);

    /**
     * Additional node settings loaded by the plugin
     */
    HashMap<String,Object> additionalSettings();
}
