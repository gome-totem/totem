package org.z.global.environment;


import java.io.File;



public class Environment {
	
	 private final File pluginsFile;
	 
	 public Environment(){
		 pluginsFile = new File("/workspace/shard/test");
	 }
	 
	 public File pluginsFile(){
		 return pluginsFile;
	 }
}
