package org.z.store.mongdb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;

public class DBFileSystem {

	protected static Logger logger = LoggerFactory.getLogger(DBFileSystem.class);
	private DB currentDb = null;

	public DBFileSystem(String serverName, String dbName) {
		this.currentDb = DataCollection.getMongoDB(serverName, dbName);
	}

	public String saveFile(InputStream in, String fileName, String contentType, DBObject metaData) {
		if (currentDb == null)
			return null;
		GridFS f = new GridFS(currentDb);
		GridFSFile mongofile = f.createFile(in, fileName);
		mongofile.put("filename", fileName);
		mongofile.put("metadata", metaData);
		mongofile.put("contentType", contentType);
		mongofile.save();
		Object id = mongofile.getId();
		return id.toString();
	}

	public void saveFile(File file, String filename, String contentType, DBObject metaData) {
		if (currentDb == null)
			return;
		GridFS f = new GridFS(currentDb);
		try {
			GridFSFile mongofile = f.createFile(file);
			mongofile.put("filename", filename);
			mongofile.put("metadata", metaData);
			mongofile.put("contentType", contentType);
			java.util.Calendar c = java.util.Calendar.getInstance();
			c.setTimeInMillis(System.currentTimeMillis());
			mongofile.put("updateDate", c.getTime());
			mongofile.save();
		} catch (IOException e) {
			logger.error("saveFile", e);
		}
	}

	public String saveFile(byte[] bytes, BasicDBObject record) {
		if (currentDb == null)
			return null;
		GridFS gridFS = new GridFS(currentDb);
		GridFSFile file = gridFS.createFile(bytes);
		for (Iterator<Entry<String, Object>> i = record.entrySet().iterator(); i.hasNext();) {
			Entry<String, Object> entry = i.next();
			file.put(entry.getKey(), entry.getValue());
		}
		file.save();
		ObjectId oId = (ObjectId) file.getId();
		return oId.toString();
	}

	public String saveFile(byte[] bytes, String filename, String contentType, DBObject metaData) {
		if (currentDb == null)
			return null;
		GridFS gridFS = new GridFS(currentDb);
		GridFSFile gridFSFile = gridFS.createFile(bytes);
		gridFSFile.put("filename", filename);
		gridFSFile.put("metadata", metaData);
		gridFSFile.put("contentType", contentType);
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		gridFSFile.put("updateDate", c.getTime());
		gridFSFile.save();
		ObjectId oId = (ObjectId) gridFSFile.getId();
		return oId.toString();
	}

	public GridFSDBFile findFileById(String id) {
		if (currentDb == null)
			return null;
		GridFS f = new GridFS(currentDb);
		GridFSDBFile file = f.findOne(new ObjectId(id));
		return file;
	}

	public InputStream findFileInputStreamById(String id) {
		if (currentDb == null)
			return null;
		GridFSDBFile file = findFileById(id);
		if (file == null)
			return null;
		return file.getInputStream();
	}

	public List<GridFSDBFile> findFilesByName(String fileName) {
		if (currentDb == null)
			return null;
		GridFS f = new GridFS(currentDb);
		List<GridFSDBFile> list = f.find(fileName);
		return list;
	}

	public List<GridFSDBFile> find(DBObject query) {
		if (currentDb == null)
			return null;
		GridFS f = new GridFS(currentDb);
		List<GridFSDBFile> list = f.find(query);
		return list;
	}

	public GridFSDBFile findFileByName(String filename) {
		if (currentDb == null)
			return null;
		GridFS f = new GridFS(currentDb);
		return f.findOne(filename);
	}

	public GridFSDBFile search(DBObject object) {
		if (currentDb == null)
			return null;
		GridFS f = new GridFS(currentDb);
		return f.findOne(object);
	}

	public long getFileCount() {
		if (currentDb == null)
			return 0;
		GridFS f = new GridFS(currentDb);
		return f.getFileList().count();
	}

	public List<GridFSDBFile> getAllFiles() {
		if (currentDb == null)
			return null;
		GridFS f = new GridFS(currentDb);
		return f.find(new BasicDBObject());
	}

	public void removeFile(String filename) {
		if (currentDb == null)
			return;
		GridFS f = new GridFS(currentDb);
		f.remove(filename);
	}

	public void removeFile(BasicDBObject query) {
		if (currentDb == null)
			return;
		GridFS f = new GridFS(currentDb);
		f.remove(query);
	}

	public void removeFile(ObjectId id) {
		if (currentDb == null)
			return;
		GridFS f = new GridFS(currentDb);
		f.remove(id);
	}

	public void removeAllFile() {
		if (currentDb == null)
			return;
		GridFS f = new GridFS(currentDb);
		f.remove(new BasicDBObject());
	}

	public void removeAllFile(String prefixFileName) {
		if (currentDb == null)
			return;
		BasicDBObject qField = new BasicDBObject();
		try {
			Pattern p = Pattern.compile(prefixFileName);
			qField.append("filename", p);
		} catch (Exception e) {
		}

		GridFS f = new GridFS(currentDb);
		f.remove(qField);
	}

	public static void main(String[] args) {
		DBFileSystem service = new DBFileSystem("image", "images");
		GridFSDBFile file = service.findFileById("4c2f1d1a0dc2e99f1f14d077");
		String fileName = "xiaoming@xeach.com|1.jpg";
		file = service.findFileByName(fileName);
		System.out.println(file == null ? "" : file.toString());
		service.saveFile("jason test".getBytes(), fileName, "img", BasicDBObjectBuilder.start().add("userId", "111").get());
	}
}
