package org.x.server.common;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.environment.Const;
import org.z.store.mongdb.DataCollection;

import com.gome.totem.sniper.util.FileUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;


/**
 * @author xiong1989win@126.com
 *	execl类
 */
public class ZExcel {
	
	private static final Logger logger = LoggerFactory.getLogger(ZExcel.class);
	private String excelName = "/server/Demo.xslx";
	private String sheetName = "sheet1";
	public static WritableWorkbook wwb = null;
	public WritableSheet ws = null;
	public static Workbook book = null;
	public static Sheet sheet = null;
	
	/**
	 * Constructor 
	 * 默认创建Demo的excel表和sheet1表单
	 */
	public ZExcel() {
		wwb = createExcel(excelName);
		ws = wwb.createSheet(sheetName, 0);
	}
	
	/**
	 * Constructor 
	 * 创建excelName的excel表和sheet1表单
	 * @param excelName
	 */
	public ZExcel(String excelName){
		this.setFileName(excelName);
		wwb = createExcel(this.excelName);
		ws = wwb.createSheet(sheetName, 0);
	}
	
	/**
	 * Constructor 
	 * 创建excelName的excel表和sheetName表单,并指定位置为sheetPos的表单
	 * @param excelName
	 */
	public ZExcel(String excelName, String sheetName, int sheetPos){
		this.setFileName(excelName);
		this.setSheetName(sheetName);
		wwb = createExcel(this.excelName);
		this.ws = wwb.createSheet(sheetName, sheetPos);
	}
	
	/**
	 * 创建excel文件
	 * @param fileName
	 * @return 
	 */
	public static WritableWorkbook createExcel(String fileName){
		
		//检查文件路径是否存在，若无则创建
		File file = new File(fileName) ;
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs() ;
		}
		try {
			wwb = Workbook.createWorkbook(new File(fileName));
		} catch (IOException e) {
			logger.error("创建失败，文件创建失败！");
		}
		return wwb;
	}

	/**
	 * excel cell 写入单元
	 * @param r	excel cell的行的位置
	 * @param c	excel cell的列的位置
	 * @param content	需要插入cell的内容
	 */
	public void writeCell(int r, int c, String content){
		Label cell = new Label(c, r, content);
		if(this.ws == null){
			logger.error("未创建Sheet！");
		}else{
			try {
				this.ws.addCell(cell);
			} catch (RowsExceededException e) {
				logger.error("异常： {}", e.getMessage());
			} catch (WriteException e) {
				logger.error("写入异常！");
			}
		}
	} 
	
	/**
	 * 获取Workbook对象才能获取表单内容
	 * @param excelName
	 * @return
	 */
	public static Sheet getSheet(String excelName, int sheetPos ){
		File file = new File(excelName) ;
		
		if (!file.getParentFile().exists()) {
			logger.error("不存在此目录!");
			return null;
		}
		if(!file.exists()){
			logger.error("不存在此文件!");
			return null;
		}
		
		try {
			book = Workbook.getWorkbook(file);
		} catch (BiffException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		sheet = book.getSheet(sheetPos);
		return sheet;
	}
	
	/**
	 * 按行读取excel
	 * @param sheetPos sheet的位置
	 * @param r	行号
	 * @return
	 */
	public List<Object> readExcelByRow(int r){
		List<Object> list = new ArrayList<Object>();
		Cell cell = null;
		for (int i = 0, len = sheet.getColumns(); i < len; i++) {
			cell = sheet.getCell(r, i);
			list.add(cell.getContents());
		}
		return list;
	}
	
	/**
	 * 按列读取excel
	 * @param sheetPos sheet的位置
	 * @param c	列号
	 * @return
	 */
	public List<Object> readExcelByCol(int c){
		List<Object> list = new ArrayList<Object>();
		Cell cell = null;
		for (int i = 0, len = sheet.getRows(); i < len; i++) {
			cell = sheet.getCell(i, c);
			list.add(cell.getContents());
		}
		return list;
	}
	
	/**
	 * 读取指定的cell
	 * @param r 行号
	 * @param c 列号
	 * @return
	 */
	public static Object readExcelByCell(int r, int c){
		int rows = sheet.getRows();
		if(r > rows || r < 0){
			logger.error("不存在此行");
			return null;
		}
		
		int columns = sheet.getColumns();
		if(c > columns || c < 0){
			logger.error("不存在此列");
			return null;
		}
		
		Cell cell = sheet.getCell(c, r);
		return cell.getContents();
	}
	
	/**
	 * 写入excel，关闭excel
	 */
	public void writeAndClose(){
		if(wwb == null) return;
		try {
			wwb.write();
			wwb.close();
		} catch (WriteException e) {
			logger.error("写入异常！");
		} catch (IOException e) {
			logger.error("关闭失败！");
		}
	}
	
	/**
	 * 写入execl 
	 */
	public void write(){
		if(wwb == null) return;
		try {
			wwb.write();
		} catch (IOException e) {
			logger.error("写入失败！");
		}
	}
	
	/**
	 * 关闭写excel 
	 */
	public void closeWbook(){
		if(wwb == null) return;
		try {
			wwb.close();
		} catch (WriteException e) {
			logger.error("写入异常！");
		} catch (IOException e) {
			logger.error("关闭失败！");
		}
	}
	
	/**
	 * 关闭读excel 
	 */
	public static void closeRbook(){
		if(book != null) {
			book.close();
		}
	}
	
	public void setFileName(String fileName) {
		this.excelName = fileName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	
	/**
	 * @param host	ip地址
	 * @param port	端口号
	 * @param username 用户名
	 * @param pass	密码
	 * @param dataDB 数据库名
	 * @param table	数据表名
	 * @param pageNumber	页码
	 * @param pageSize	页面大小
	 * @return datas 返回list数据
	 * 
	 * **/
	public BasicDBList getConnection(String host, int port, String username, String pass, String dataDB, String table, int pageNumber, int pageSize){
		List<ServerAddress> address = new ArrayList<ServerAddress>();
		List<MongoCredential> credent = new ArrayList<MongoCredential>();
		
		try {
			address.add(new ServerAddress(host, port));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		credent.add(MongoCredential.createMongoCRCredential(username, dataDB, pass.toCharArray()));
		MongoClient mongoClient = new MongoClient(address, credent);
		DB db = mongoClient.getDB(dataDB);
		
		BasicDBList listDB = new BasicDBList();
		DBCollection dbc = db.getCollection(table);
		
		int count = 0;
		DBCursor curs = null;
		try {
			curs = dbc.find(null, null);
			count = pageNumber * pageSize < 0 ? 0 : pageNumber * pageSize;
			curs.skip(count);
			curs.limit(pageSize);
			
			while (curs.hasNext()) {
				BasicDBObject dObj = (BasicDBObject) curs.next();
				dObj.remove("_id");
				listDB.add(dObj);
			}
		} finally {
			curs.close();
		}
		
		return listDB;
	}
	
	/***
	 * 统计关键词点击在第2页的数据
	 * 
	 * **/
	public static void keysInSecondPage(String dbName, String path){
		List<ServerAddress> address = new ArrayList<ServerAddress>();
		List<MongoCredential> credent = new ArrayList<MongoCredential>();
		
		try {
			address.add(new ServerAddress("10.58.50.55", 19753));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		credent.add(MongoCredential.createMongoCRCredential("gome", "logs", "totem".toCharArray()));
		MongoClient mongoClient = new MongoClient(address, credent);
		DB db = mongoClient.getDB("logs");
		
		DBCollection dbc = db.getCollection(dbName);
		dbc.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		
		Set<String> dataSet = new HashSet<String>();
		DBCursor curs = null;
		try {
			curs = dbc.find(null, null);
			
			while (curs.hasNext()) {
				BasicDBObject dObj = (BasicDBObject) curs.next();
				dObj = (BasicDBObject) dObj.get("value");
				BasicDBList dList = (BasicDBList) dObj.get("keys");
				
				if(dList == null) continue;
				
				for (int i = 0, len = dList.size(); i < len; i++) {
					Object o = dList.get(i);
					dataSet.add(o.toString());
				}
			}
		} finally {
			curs.close();
		}
		
		for (String str : dataSet) {
			FileUtil.writeFile(path, str, true);
		}
	}
	

	public static void keysWithNoResult(String dbName, String path) {
		List<ServerAddress> address = new ArrayList<ServerAddress>();
		List<MongoCredential> credent = new ArrayList<MongoCredential>();
		
		try {
			address.add(new ServerAddress("10.58.50.55", 19753));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		credent.add(MongoCredential.createMongoCRCredential("gome", "logs", "totem".toCharArray()));
		MongoClient mongoClient = new MongoClient(address, credent);
		DB db = mongoClient.getDB("logs");
		
		DBCollection dbc = db.getCollection(dbName);
		dbc.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		
		Set<String> dataSet = new HashSet<String>();
		DBCursor curs = null;
		try {
			curs = dbc.find(null, null);
			
			while (curs.hasNext()) {
				BasicDBObject dObj = (BasicDBObject) curs.next();
				String key = dObj.getString("_id");
				dObj = (BasicDBObject) dObj.get("value");
				BasicDBList dList = (BasicDBList) dObj.get("keys");
				
				if(dList == null) continue;
				
				int count = 0;
				for (int i = 0, len = dList.size(); i < len; i++) {
					Object o = dList.get(i);
					if(o instanceof Double){
						count += ((Double)o).intValue();
					}
				}
				
				int ave = 0;
				if(dList != null && dList.size() != 0){
					ave = count / dList.size();
				}
				
				if(ave == 0){
					dataSet.add(key);
				}
			}
		} finally {
			curs.close();
		}
		
		for (String str : dataSet) {
			FileUtil.writeFile(path, str, true);
		}
	}
	
	/**
	 *  搜索的关键词但没有点击过的
	 * 
	 * **/
	public static void keysWithNoClick(String dbName1, String dbName2, String path) {
		List<ServerAddress> address = new ArrayList<ServerAddress>();
		List<MongoCredential> credent = new ArrayList<MongoCredential>();
		
		try {
			address.add(new ServerAddress("10.58.50.55", 19753));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		credent.add(MongoCredential.createMongoCRCredential("gome", "logs", "totem".toCharArray()));
		MongoClient mongoClient = new MongoClient(address, credent);
		DB db = mongoClient.getDB("logs");
		
		DBCollection dbc = db.getCollection(dbName1);
		dbc.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		
		HashMap<String, Integer> shard1 = new HashMap<String, Integer>();
		DBCursor curs = null;
		try {
			curs = dbc.find(null, null);
			
			while (curs.hasNext()) {
				BasicDBObject dObj = (BasicDBObject) curs.next();
				String key = dObj.getString("_id");
				int count = ((Double) dObj.get("value")).intValue();
				shard1.put(key, count);
			}
		} finally {
			curs.close();
		}
		
		Set<String> dataSet = new HashSet<String>();
		dbc = db.getCollection(dbName2);
		dbc.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		try {
			curs = dbc.find(null, null);
			
			while (curs.hasNext()) {
				BasicDBObject dObj = (BasicDBObject) curs.next();
				String key = dObj.getString("_id");
				dataSet.add(key);
			}
		} finally {
			curs.close();
		}
		
		for (String str : dataSet) {
			if(StringUtils.isEmpty(str)) continue;
			Integer s = shard1.get(str);
			
			if(s != null && s.intValue() > 0){
				shard1.remove(str);
			}
		}
		
		for (String str : shard1.keySet()) {
			FileUtil.writeFile(path, str, true);
		}
		
	}
	
	public static void main(String[] args) throws UnknownHostException {
		String excelName = "/doc/promo.xlsx";
		int mode = 6;
		switch(mode){
		case 1:
			sheet = ZExcel.getSheet(excelName, 0);
			int rows = sheet.getRows();
			for (int i = 0; i < rows; i++) {
				String skuId = (String) ZExcel.readExcelByCell(i, 0);
				String catId = (String) ZExcel.readExcelByCell(i, 1);
				DataCollection.update(Const.defaultDictMongo, Const.defaultDictMongoDB, "promo_score", new BasicDBObject("skuId",skuId), new BasicDBObject("catId",catId));
			}
			break;
		case 2:
			ZExcel zdx = new ZExcel(excelName,"促销因子", 0);
			DBCollection table = DataCollection.getCollection(Const.defaultDictMongo, Const.defaultDictMongoDB, "promo_score");
			DBCursor cur = table.find();
			int count = 0;
			while (cur.hasNext()) {
				BasicDBObject c = (BasicDBObject) cur.next();
				String skuId = c.getString("skuId", "");
				String score = c.getString("promoScore","");
//			BasicDBList keys = (BasicDBList) c.get("keys");
//			if(keys == null) continue;
				zdx.writeCell(count, 0, skuId);
				zdx.writeCell(count, 1, score);
//			for (int i = 0, len = keys.size(); i < len; i++) {
//				BasicDBObject key = (BasicDBObject) keys.get(i);
//				String k = key.getString("key", "");
//				
//				zdx.writeCell(count, i + 1, k);
//			}
				count ++;
			}
			zdx.writeAndClose();
			
//		System.out.println((String)zdx.readExcelByCell(0, 1));
			break;
		case 3:
			sheet = ZExcel.getSheet("/doc/promo.xlsx", 0);
			int row = sheet.getRows();
			Map<String,String> r = new HashMap<String, String>();
			for (int i = 0; i < row; i++) {
				String skuId = (String) ZExcel.readExcelByCell(i, 0);
				String score = (String) ZExcel.readExcelByCell(i, 1);
				r.put(skuId, score);
			}
			ZExcel.closeRbook();
			
			int rowss = sheet.getRows();
			Map<String,String> s = new HashMap<String, String>();
			for (int i = 0; i < rowss; i++) {
				String skuId = (String) ZExcel.readExcelByCell(i, 0);
				String catId = (String) ZExcel.readExcelByCell(i, 1);
				s.put(skuId, catId);
			}
			ZExcel.closeRbook();
			
			break;
		case 4:
			List<ServerAddress> address = new ArrayList<ServerAddress>();
			List<MongoCredential> credent = new ArrayList<MongoCredential>();
			
			address.add(new ServerAddress("10.58.50.55", 19753));
			
			credent.add(MongoCredential.createMongoCRCredential("gome", "logs", "totem".toCharArray()));
			MongoClient mongoClient = new MongoClient(address, credent);
			DB db = mongoClient.getDB("logs");
			
			List<DBObject> listDB = new ArrayList<DBObject>();
			DBCollection dbc = db.getCollection("usrsactionlogs");
			dbc.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
			
			int offset = 0, pageSize = 0;
			DBCursor curs = null;
			try {
				curs = dbc.find(null, null);
				offset = offset < 0 ? 0 : offset;
				curs.skip(offset);
				curs.limit(pageSize);
				
				while (curs.hasNext()) {
					BasicDBObject dObj = (BasicDBObject) curs.next();
					dObj.remove("_id");
					listDB.add(dObj);
				}
			} finally {
				curs.close();
			}
			
			List<ServerAddress> sAddress = new ArrayList<ServerAddress>();
			sAddress.add(new ServerAddress("10.58.50.53", 19753));
			MongoClient mongoCli = new MongoClient(sAddress, credent);
			DB dbC = mongoCli.getDB("dict");
			DBCollection dbColl = dbC.getCollection("cityMapM");
			dbColl.insert(listDB);
			break;
		case 5:
			List<ServerAddress> seeds = new ArrayList<ServerAddress>();
			List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
			
			seeds.add(new ServerAddress("10.58.50.53", 19753));
			credentialsList.add(MongoCredential.createMongoCRCredential("gome", "dict", "totem".toCharArray()));
			MongoClient zClient = new MongoClient(seeds, credentialsList);
			
			DB db1 = zClient.getDB("dict");
			DBCollection dbcoole = db1.getCollection("promo_score");
			if(dbcoole != null){
				dbcoole.remove(new BasicDBObject().append("catId", "cat10000092"));
				dbcoole.remove(new BasicDBObject().append("catId", "cat10005442"));
				dbcoole.remove(new BasicDBObject().append("catId", "cat10000094"));
				dbcoole.remove(new BasicDBObject().append("catId", "cat10000097"));
				dbcoole.remove(new BasicDBObject().append("catId", "cat10000093"));
				dbcoole.remove(new BasicDBObject().append("catId", "cat10000099"));
			}
			break;
		case 6:
//			keysInSecondPage("keysInSecondPage","/server/keysInSecondPage.txt");
//			keysInSecondPage("keysHigherLine6", "/server/keysHigherLine6.txt");
			keysWithNoResult("keysWithNoResult", "/server/keysWithNoResult.txt");
//			keysWithNoClick("keysWithNoClickShard1", "keysWithNoClickShard2", "/server/keysWithNoClick.txt");
		}
		
	}
}
