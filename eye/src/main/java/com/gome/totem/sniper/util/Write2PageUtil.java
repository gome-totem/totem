package com.gome.totem.sniper.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.z.global.environment.Const;
import org.z.store.mongdb.DataCollection;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

/**
 * @author Dong Zhou
 */
public class Write2PageUtil {

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
	
	public static void write(HttpServletResponse response, Object context) {
		if (context==null) {
			context="" ;
		}
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		Writer writer = null;
		try {
			writer = response.getWriter();
			writer.write(context.toString());
			writer.flush() ;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void writeJson(HttpServletResponse response, Boolean state, String context) {
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		Writer writer = null;
		try {
			writer = response.getWriter();
			BasicDBObject dbObject = new BasicDBObject() ;
			dbObject.append("state", state) ;
			dbObject.append("message", context) ;
			writer.write(dbObject.toString());
			writer.flush() ;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		BufferedWriter isr = null;
		String charset = "utf-8";
		try {
			isr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("/server/conf/1111.txt")), charset));
		} catch (IOException e) {
		}
		DBCollection table = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, "zdx_1111");
		DBCursor cur = table.find();
		while (cur.hasNext()) {
			BasicDBObject c = (BasicDBObject) cur.next();
			String catId = c.getString("catId", "");
			BasicDBList keys = (BasicDBList) c.get("keys");
			BasicDBList tmp = new BasicDBList();
			if(keys == null) continue;
			for (int i = 0, len = keys.size(); i < len; i++) {
				BasicDBObject key = (BasicDBObject) keys.get(i);
				String k = key.getString("key", "");
				
				if(!StringUtils.isEmpty(k)){
					tmp.add(k);
				}
			}
			
			if(isr != null){
				try {
					String str = tmp.toString().replace('[', ' ');
					str = str.replace(']', ' ');
					str = str.replace('\"', ' ');
					str = str.trim();
					isr.write(new String((catId + "," + str +"\t\n").getBytes(), charset));
					System.out.println(catId + "\t" + str);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			isr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
