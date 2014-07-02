package org.z.global.plugin;



import static org.z.global.util.FileSystemUtils.isAccessibleDirectory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.environment.Environment;
import org.z.global.io.Classes;
import org.z.global.io.IOUtils;
import org.z.global.io.Strings;
import org.z.global.object.MapBuilder;
import org.z.global.object.Tuple;
import org.z.global.util.TimeValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class PluginService {
	     private static Logger logger = LoggerFactory.getLogger(PluginService.class);
	    private static final String TOTEM_PLUGIN_PROPERTIES = "totem-plugin.properties";
	    private final ImmutableList<Tuple<PluginInfo, Plugin>> plugins;
	    @SuppressWarnings("unused")
		private final ImmutableMap<Plugin, List<OnModuleReference>> onModuleReferences;
	    private List<PluginInfo> cachedPluginsInfo;
	    private final TimeValue refreshInterval;
	    private long lastRefresh;
	    private final Environment environment;
	    private ImmutableMap<String, Object>  settings;
	    
	    static class OnModuleReference {
	        public final Class<? extends Module> moduleClass;
	        public final Method onModuleMethod;

	        OnModuleReference(Class<? extends Module> moduleClass, Method onModuleMethod) {
	            this.moduleClass = moduleClass;
	            this.onModuleMethod = onModuleMethod;
	        }
	    }
	    @SuppressWarnings("unchecked")
		public PluginService(ImmutableMap<String, Object>  settings,Environment environment) {
	    	this.environment = environment;
	    	this.settings =settings;
	    	 ImmutableList.Builder<Tuple<PluginInfo, Plugin>> tupleBuilder = ImmutableList.builder();
	         String[] defaultPluginsClasses = ((String)settings.get("plugin.types")).split(";");
	         for (String pluginClass : defaultPluginsClasses) {
	             Plugin plugin = PluginUtils.loadPlugin(pluginClass, settings, Classes.getDefaultClassLoader());
	             PluginInfo pluginInfo = new PluginInfo(plugin.name(), plugin.description(), hasSite(plugin.name()), true, PluginInfo.VERSION_NOT_AVAILABLE, false);
	             if (logger.isTraceEnabled()) {
	                 logger.trace("plugin loaded from settings [{}]", pluginInfo);
	             }
	             tupleBuilder.add(new Tuple<PluginInfo, Plugin>(pluginInfo, plugin));
	         }
	         tupleBuilder.addAll(loadPlugins());
	         this.plugins =tupleBuilder.build();
	         Map<String, Plugin> jvmPlugins = Maps.newHashMap();
	         List<String> sitePlugins = Lists.newArrayList();

	         for (Tuple<PluginInfo, Plugin> tuple : this.plugins) {
	             jvmPlugins.put(tuple.v2().name(), tuple.v2());
	             if (tuple.v1().isSite()) {
	                 sitePlugins.add(tuple.v1().getName());
	             }
	         }

	         ImmutableList<Tuple<PluginInfo, Plugin>> tuples = loadSitePlugins();
	         for (Tuple<PluginInfo, Plugin> tuple : tuples) {
	             sitePlugins.add(tuple.v1().getName());
	         }

	         String[] mandatoryPlugins = ((String)settings.get("plugin.mandatory")).split(",");
	         if (mandatoryPlugins != null) {
	             Set<String> missingPlugins = Sets.newHashSet();
	             for (String mandatoryPlugin : mandatoryPlugins) {
	                 if (!jvmPlugins.containsKey(mandatoryPlugin) && !sitePlugins.contains(mandatoryPlugin) && !missingPlugins.contains(mandatoryPlugin)) {
	                     missingPlugins.add(mandatoryPlugin);
	                 }
	             }
	             if (!missingPlugins.isEmpty()) {
	                 try {
						throw new Exception("Missing mandatory plugins [" + Strings.collectionToDelimitedString(missingPlugins, ", ") + "]");
					} catch (Exception e) {
						e.printStackTrace();
					}
	             }
	         }
	         logger.info("loaded {}, sites {}", jvmPlugins.keySet(), sitePlugins);
	         MapBuilder<Plugin, List<OnModuleReference>> onModuleReferences =MapBuilder.newMapBuilder();
	         for (Plugin plugin : jvmPlugins.values()) {
	             List<OnModuleReference> list = Lists.newArrayList();
	             for (Method method : plugin.getClass().getDeclaredMethods()) {
	                 if (!method.getName().equals("onModule")) {
	                     continue;
	                 }
	                 if (method.getParameterTypes().length == 0 || method.getParameterTypes().length > 1) {
	                     logger.warn("Plugin: {} implementing onModule with no parameters or more than one parameter", plugin.name());
	                     continue;
	                 }
	                 @SuppressWarnings("rawtypes")
					Class moduleClass = method.getParameterTypes()[0];
	                 if (!Module.class.isAssignableFrom(moduleClass)) {
	                     logger.warn("Plugin: {} implementing onModule by the type is not of Module type {}", plugin.name(), moduleClass);
	                     continue;
	                 }
	                 method.setAccessible(true);
	                 list.add(new OnModuleReference(moduleClass, method));
	             }
	             if (!list.isEmpty()) {
	                 onModuleReferences.put(plugin, list);
	             }
	         }
	         this.onModuleReferences = onModuleReferences.immutableMap();

	         this.refreshInterval = TimeValue.timeValueSeconds(10);
	    	 
		  }
	    private List<Tuple<PluginInfo,Plugin>> loadPlugins() {
	        File pluginsFile = environment.pluginsFile();
	        if (!isAccessibleDirectory(pluginsFile)) {
	            return Collections.emptyList();
	        }

	        List<Tuple<PluginInfo, Plugin>> pluginData = Lists.newArrayList();

	        boolean defaultIsolation = Boolean.FALSE;
	        ClassLoader esClassLoader = Classes.getDefaultClassLoader();
	        Method addURL = null;
	        boolean discoveredAddUrl = false;

	        File[] pluginsFiles = pluginsFile.listFiles();

	        if (pluginsFiles != null) {
	            for (File pluginRoot : pluginsFiles) {
	                if (isAccessibleDirectory(pluginRoot)) {
	                    try {
	                        logger.trace("--- adding plugin [" + pluginRoot.getAbsolutePath() + "]");
	                        // check isolation
	                        List<File> pluginClassPath = PluginUtils.pluginClassPathAsFiles(pluginRoot);
	                        List<URL> pluginProperties = PluginUtils.lookupPluginProperties(pluginClassPath);
	                        boolean isolated = PluginUtils.lookupIsolation(pluginProperties, defaultIsolation);

	                        if (isolated) {
	                            logger.trace("--- creating isolated space for plugin [" + pluginRoot.getAbsolutePath() + "]");
	                            PluginClassLoader pcl = new PluginClassLoader(PluginUtils.convertFileToUrl(pluginClassPath), esClassLoader);
	                            pluginData.addAll(loadPlugin(pluginClassPath, pluginProperties, pcl, true));
	                        } else {
	                            if (!discoveredAddUrl) {
	                                discoveredAddUrl = true;
	                                Class<?> esClassLoaderClass = esClassLoader.getClass();

	                                while (!esClassLoaderClass.equals(Object.class)) {
	                                    try {
	                                        addURL = esClassLoaderClass.getDeclaredMethod("addURL", URL.class);
	                                        addURL.setAccessible(true);
	                                        break;
	                                    } catch (NoSuchMethodException e) {
	                                        // no method, try the parent
	                                        esClassLoaderClass = esClassLoaderClass.getSuperclass();
	                                    }
	                                }
	                            }

	                            if (addURL == null) {
	                                logger.debug("failed to find addURL method on classLoader [" + esClassLoader + "] to add methods");
	                            }
	                            else {
	                                for (File file : pluginClassPath) {
	                                    addURL.invoke(esClassLoader, file.toURI().toURL());
	                                }
	                                pluginData.addAll(loadPlugin(pluginClassPath, pluginProperties, esClassLoader, false));
	                            }
	                        }
	                    } catch (Throwable e) {
	                        logger.warn("failed to add plugin [" + pluginRoot.getAbsolutePath() + "]", e);
	                    }
	                }
	            }
	        } else {
	            logger.debug("failed to list plugins from {}. Check your right access.", pluginsFile.getAbsolutePath());
	        }

	        return pluginData;
	    }
	    
	    private Collection<? extends Tuple<PluginInfo, Plugin>> loadPlugin(List<File> pluginClassPath, List<URL> properties, ClassLoader classLoader, boolean isolation) throws Exception {
	        List<Tuple<PluginInfo, Plugin>> plugins = Lists.newArrayList();

	        Enumeration<URL> entries = Collections.enumeration(properties);
	        while (entries.hasMoreElements()) {
	            URL pluginUrl = entries.nextElement();
	            Properties pluginProps = new Properties();
	            InputStream is = null;
	            try {
	                is = pluginUrl.openStream();
	                pluginProps.load(is);
	                String pluginClassName = pluginProps.getProperty("plugin");
	                if (pluginClassName == null) {
	                    throw new IllegalArgumentException("No plugin class specified");
	                }
	                String pluginVersion = pluginProps.getProperty("version", PluginInfo.VERSION_NOT_AVAILABLE);
	                Plugin plugin = PluginUtils.loadPlugin(pluginClassName, settings, classLoader);

	                File siteFile = new File(new File(environment.pluginsFile(), plugin.name()), "_site");
	                boolean isSite = isAccessibleDirectory(siteFile);
	                if (logger.isTraceEnabled()) {
	                    logger.trace("found a jvm plugin [{}], [{}]{}",new Object[]{plugin.name(), plugin.description(), isSite ? ": with _site structure" : ""});
	                }

	                PluginInfo pluginInfo = new PluginInfo(plugin.name(), plugin.description(), isSite, true, pluginVersion, isolation);
	                plugins.add(new Tuple<PluginInfo, Plugin>(pluginInfo, plugin));
	            } catch (Throwable e) {
	                logger.warn("failed to load plugin from [" + pluginUrl + "]", e);
	            } finally {
	                IOUtils.closeWhileHandlingException(is);
	            }
	        }

	        return plugins;
	    }
	    
	    private ImmutableList<Tuple<PluginInfo,Plugin>> loadSitePlugins() {
	        ImmutableList.Builder<Tuple<PluginInfo, Plugin>> sitePlugins = ImmutableList.builder();
	        List<String> loadedJvmPlugins = new ArrayList<String>();

	        // Already known jvm plugins are ignored
	        for(Tuple<PluginInfo, Plugin> tuple : plugins) {
	            if (tuple.v1().isSite()) {
	                loadedJvmPlugins.add(tuple.v1().getName());
	            }
	        }

	        // Let's try to find all _site plugins we did not already found
	        File pluginsFile = environment.pluginsFile();

	        if (!pluginsFile.exists() || !pluginsFile.isDirectory()) {
	            return sitePlugins.build();
	        }

	        for (File pluginFile : pluginsFile.listFiles()) {
	            if (!loadedJvmPlugins.contains(pluginFile.getName())) {
	                File sitePluginDir = new File(pluginFile, "_site");
	                if (isAccessibleDirectory(sitePluginDir)) {
	                    // We have a _site plugin. Let's try to get more information on it
	                    String name = pluginFile.getName();
	                    String version = PluginInfo.VERSION_NOT_AVAILABLE;
	                    String description = PluginInfo.DESCRIPTION_NOT_AVAILABLE;

	                    // We check if es-plugin.properties exists in plugin/_site dir
	                    File pluginPropFile = new File(sitePluginDir, TOTEM_PLUGIN_PROPERTIES);
	                    if (pluginPropFile.exists()) {

	                        Properties pluginProps = new Properties();
	                        InputStream is = null;
	                        try {
	                            is = new FileInputStream(pluginPropFile.getAbsolutePath());
	                            pluginProps.load(is);
	                            description = pluginProps.getProperty("description", PluginInfo.DESCRIPTION_NOT_AVAILABLE);
	                            version = pluginProps.getProperty("version", PluginInfo.VERSION_NOT_AVAILABLE);
	                        } catch (Exception e) {
	                            // Can not load properties for this site plugin. Ignoring.
	                            logger.debug("can not load {} file.", e, TOTEM_PLUGIN_PROPERTIES);
	                        } finally {
	                            IOUtils.closeWhileHandlingException(is);
	                        }
	                    }

	                    if (logger.isTraceEnabled()) {
	                        logger.trace("found a site plugin name [{}], version [{}], description [{}]",
	                                new Object[]{name, version, description});
	                    }
	                    sitePlugins.add(new Tuple<PluginInfo, Plugin>(new PluginInfo(name, description, true, false, version, false), null));
	                }
	            }
	        }

	        return sitePlugins.build();
	    }
	    
	    synchronized public List<PluginInfo> info() {
	        if (refreshInterval.millis() != 0) {
	            if (cachedPluginsInfo != null &&
	                    (refreshInterval.millis() < 0 || (System.currentTimeMillis() - lastRefresh) < refreshInterval.millis())) {
	                if (logger.isTraceEnabled()) {
	                    logger.trace("using cache to retrieve plugins info");
	                }
	                return cachedPluginsInfo;
	            }
	            lastRefresh = System.currentTimeMillis();
	        }

	        if (logger.isTraceEnabled()) {
	            logger.trace("starting to fetch info on plugins");
	        }
	        cachedPluginsInfo =  new ArrayList<PluginInfo>();

	        // We first add all JvmPlugins
	        for (Tuple<PluginInfo, Plugin> plugin : this.plugins) {
	            if (logger.isTraceEnabled()) {
	                logger.trace("adding jvm plugin [{}]", plugin.v1());
	            }
	            cachedPluginsInfo.add(plugin.v1());
	        }

	        // We reload site plugins (in case of some changes)
	        for (Tuple<PluginInfo, Plugin> plugin : loadSitePlugins()) {
	            if (logger.isTraceEnabled()) {
	                logger.trace("adding site plugin [{}]", plugin.v1());
	            }
	            cachedPluginsInfo.add(plugin.v1());
	        }

	        return cachedPluginsInfo;
	    }
	      private boolean hasSite(String name) {
	        File pluginsFile = environment.pluginsFile();

	        if (!pluginsFile.exists() || !pluginsFile.isDirectory()) {
	            return false;
	        }

	        File sitePluginDir = new File(pluginsFile, name + "/_site");
	        return isAccessibleDirectory(sitePluginDir);
	    }
	    public ImmutableList<Tuple<PluginInfo, Plugin>> plugins() {
	        return plugins;
	      }
}
