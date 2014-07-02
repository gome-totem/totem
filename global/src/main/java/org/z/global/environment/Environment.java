package org.z.global.environment;


import java.io.File;


public class Environment {
	
	 private final File pluginsFile;
	 
	 public Environment(){
		 pluginsFile = new File("");
	 }
	 
	 public File pluginsFile(){
		 return pluginsFile;
	 }
}
