package org.z.global.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.z.global.io.Booleans;
import org.z.global.io.IOUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


public class PluginUtils {
	 private PluginUtils() {}
	    @SuppressWarnings("unchecked")
		static Plugin loadPlugin(String className, ImmutableMap<String,Object> settings, ClassLoader classLoader) {
	        try {
				Class<? extends Plugin> pluginClass = (Class<? extends Plugin>) classLoader.loadClass(className);
	            try {
	                return pluginClass.getConstructor(ImmutableMap.class).newInstance(settings);
	            } catch (NoSuchMethodException e) {
	                try {
	                    return pluginClass.newInstance();
	                } catch (Exception e1) {
	                    throw new RuntimeException("No constructor for [" + pluginClass + "]. A plugin class must " +
	                            "have either an empty default constructor or a single argument constructor accepting a " +
	                            "Settings instance");
	                }
	            }

	        } catch (Exception e) {
	            throw new RuntimeException("Failed to load plugin class [" + className + "]", e);
	        }
	    }
	    static List<File> pluginClassPathAsFiles(File pluginFolder) throws IOException {
	        List<File> cpFiles = Lists.newArrayList();
	        cpFiles.add(pluginFolder);

	        List<File> libFiles = Lists.newArrayList();
	        File[] files = pluginFolder.listFiles();
	        if (files != null) {
	            Collections.addAll(libFiles, files);
	        }
	        File libLocation = new File(pluginFolder, "lib");
	        if (libLocation.exists() && libLocation.isDirectory()) {
	            files = libLocation.listFiles();
	            if (files != null) {
	                Collections.addAll(libFiles, files);
	            }
	        }

	        // if there are jars in it, add it as well
	        for (File libFile : libFiles) {
	            if (libFile.getName().endsWith(".jar") || libFile.getName().endsWith(".zip")) {
	                cpFiles.add(libFile);
	            }
	        }

	        return cpFiles;
	    }

	    static boolean lookupIsolation(List<URL> pluginProperties, boolean defaultIsolation) throws IOException {
	        Properties props = new Properties();
	        InputStream is = null;
	        for (URL prop : pluginProperties) {
	            try {
	                props.load(prop.openStream());
	                return Booleans.parseBoolean(props.getProperty("isolation"), defaultIsolation);
	            } finally {
	                IOUtils.closeWhileHandlingException(is);
	            }
	        }
	        return defaultIsolation;
	    }

	    static List<URL> lookupPluginProperties(List<File> pluginClassPath) throws Exception {
	        if (pluginClassPath.isEmpty()) {
	            return Collections.emptyList();
	        }

	        List<URL> found = Lists.newArrayList();

	        for (File file : pluginClassPath) {
	            String toString = file.getName();
	            if (toString.endsWith(".jar") || toString.endsWith(".zip")) {
	                JarFile jar = new JarFile(file);
	                try {
	                    JarEntry jarEntry = jar.getJarEntry("totem-plugin.properties");
	                    if (jarEntry != null) {
	                        found.add(new URL("jar:" + file.toURI().toString() + "!/totem-plugin.properties"));
	                    }
	                } finally {
	                    IOUtils.closeWhileHandlingException(jar);
	                }
	            }
	            else {
	                File props = new File(file, "totem-plugin.properties");
	                if (props.exists() && props.canRead()) {
	                    found.add(props.toURI().toURL());
	                }
	            }
	        }

	        return found;
	    }

	    static URL[] convertFileToUrl(List<File> pluginClassPath) throws IOException {
	        URL[] urls = new URL[pluginClassPath.size()];
	        for (int i = 0; i < urls.length; i++) {
	            urls[i] = pluginClassPath.get(i).toURI().toURL();
	        }
	        return urls;
	    }

}
