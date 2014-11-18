package com.gome.totem.sniper.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author root
 *
 */
public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	/**
	 * 创建文件，无格式文件
	 * @param fileName 文件名称
	 * @param isNew 强制创建
	 * @return
	 */
	public static File createFile(String fileName, boolean isNew){
		//创建文件
		File file = new File(fileName);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs() ;
		}
		
		if(isNew){
			file.delete();
		}
		if (!(file.exists())){
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.error("创建文件失败！ {}", e.getMessage());
			}
		}
		return file;
	}
	
	/**
	 * 写文件，可选追加内容或是替换内容
	 * @param fileName 完整文件名
	 * @param content	写内容
	 * @param isAdd	追加还是替换
	 */
	public static void writeFile(String fileName, String content, boolean isAdd){
		File file = new File(fileName) ;
		
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs() ;
		}
		
		if (!file.exists()) {
			try {
				file.createNewFile() ;
			} catch (IOException e) {
				logger.error("创建文件失败！ {}", e.getMessage());
			}
		}
        try {
            FileWriter writer = new FileWriter(fileName, isAdd);
            writer.write(content);
            writer.write("\r\n");
            writer.close();
        } catch (IOException e) {
        	logger.error("关闭流失败！");
        }
	}
	
	public static void main(String[] args) {
//		String[] strs = new String[]{"hello","你好"};
//		for(String str : strs){
//			writeFile("/server/conf/hhh/11.txt", str, false);
//		}
	}
}
