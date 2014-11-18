package com.gome.totem.sniper.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class FileUtils {
	
	public static Properties readProperties(String name){
		
		Properties properties = new Properties() ;
		
		InputStream in = FileUtils.class.getClassLoader().getResourceAsStream(name) ;
		
		try {
			properties.load(in) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return properties ;
	}
	
	public static File makeFile(String dirname, String filename) {
		String pathname = dirname + "/" + filename;

		File dir = new File(dirname);
		if (!(dir.exists())) {
			dir.mkdirs();
		}

		File file = new File(pathname);
		if (!(file.exists()))
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.getMessage();
				e.printStackTrace();
			}
		return file;
	}
	
	public static void write2File(String pathName, String content, boolean append){
		
		File file = new File(pathName) ;
		
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs() ;
		}
		
		if (!file.exists()) {
			try {
				file.createNewFile() ;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileWriter writer = null;
		try {
			writer = new FileWriter(file, append) ;
			writer.write(content) ;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer!=null) {
				try {
					writer.flush() ;
					writer.close() ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String bufferedReadFile(String fileName) {
		StringBuffer content = new StringBuffer();
		InputStreamReader read = null ;
		try {
			read = new InputStreamReader(new FileInputStream(fileName));
			BufferedReader bufferedReader = new BufferedReader(read);
			String linetxt = null;
			while ((linetxt = bufferedReader.readLine()) != null) {
				content.append(linetxt.toString().trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (read!=null) {
				try {
					read.close() ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return content.toString();
	}
}