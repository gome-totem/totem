package org.z.store.leveldb;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LevelDb {

	SOLR("" + "product/", "dynamic"), SCHEDULER("", "dynamic");

	private static Logger logger = LoggerFactory.getLogger(LevelDb.class);
	private static final int batchUpdate = 90;
	private DBFactory factory = null;
	private Options options = null;
	private Map<String, byte[]> updateCaches = new ConcurrentHashMap<String, byte[]>();
	private Map<String, Integer> deleteCaches = new ConcurrentHashMap<String, Integer>();
	private File workPath = null;
	private String path = null;
	private String name = null;
	private DB dbInstance = null;
	private Integer LockDelete = 0;
	private Integer LockUpdate = 1;
	private long saveTime = System.currentTimeMillis();
	private long deleteTime = System.currentTimeMillis();

	private LevelDb(String path, String name) {
		this.path = path;
		this.name = name;
		options = new Options().createIfMissing(true).cacheSize(100 * 1048576);
		factory = JniDBFactory.factory;
		init();
	}

	private boolean init() {
		workPath = new File(path + name);
		try {
			if (workPath.exists())
				FileUtils.deleteQuietly(new File(path + name + "/LOCK"));
			else
				FileUtils.forceMkdir(workPath);
			dbInstance = factory.open(workPath, options);
		} catch (IOException e) {
			logger.error("init", e);
			dbInstance = null;
		}
		return dbInstance != null;
	}

	public DB dbInstance() {
		return this.dbInstance;
	}

	public String getString(String key) {
		byte[] bytes = dbInstance.get(JniDBFactory.bytes(key));
		if (bytes == null)
			return null;
		 String date = null;
		try {
			date = new String(bytes,"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return date;
	}

	public byte[] get(String key) {
		if (this.deleteCaches.containsKey(key)) {
			return null;
		}
		byte[] v = updateCaches.get(key);
		if (v != null) {
			return v;
		}
		byte[] bytes = dbInstance.get(JniDBFactory.bytes(key));
		return bytes;
	}

	public void put(String prefix, String key, Object value) {
		put(prefix + "@" + key, value);
	}

	public void put(String key, Object value) {
		put(key, JniDBFactory.bytes(value.toString()));
	}

	public void put(String key, byte[] value) {
		synchronized (LockUpdate) {
			deleteCaches.remove(key);
			updateCaches.put(key, value);
			long end = System.currentTimeMillis();
			if (updateCaches.size() >= batchUpdate || end - saveTime >= 60000) {
				saveTime = end;
				this.batchWrite(updateCaches);
			}
		}
	}

	public void batchWrite(Map<String, byte[]> records) {
		if (records.size() == 0)
			return;
		WriteOptions wo = new WriteOptions().sync(true);
		WriteBatch batch = dbInstance.createWriteBatch();
		try {
			for (Iterator<Entry<String, byte[]>> i = records.entrySet().iterator(); i.hasNext();) {
				Entry<String, byte[]> entry = i.next();
				batch.put(JniDBFactory.bytes(entry.getKey()), entry.getValue());
			}
			dbInstance.write(batch, wo);
		} finally {
			records.clear();
			try {
				if (batch != null)
					batch.close();
			} catch (IOException e) {
				logger.error("init", e);
			}
		}
	}

	public void save() {
		synchronized (LockUpdate) {
			this.batchWrite(updateCaches);
		}
		synchronized (LockDelete) {
			this.batchDelete(deleteCaches.keySet());
		}
	}

	public void delete(String key) {
		delete(key, false);
	}

	public void delete(String key, boolean noDelay) {
		synchronized (LockDelete) {
			deleteCaches.put(key, 0);
			long end = System.currentTimeMillis();
			if (deleteCaches.size() >= batchUpdate || end - deleteTime >= 60000 || noDelay) {
				deleteTime = end;
				this.batchDelete(deleteCaches.keySet());
			}
		}
	}

	public void batchDelete(Set<String> records) {
		if (deleteCaches.size() == 0)
			return;
		WriteOptions wo = new WriteOptions().sync(true);
		WriteBatch batch = dbInstance.createWriteBatch();
		try {
			for (Iterator<String> i = records.iterator(); i.hasNext();)
				batch.delete(JniDBFactory.bytes(i.next()));
			dbInstance.write(batch, wo);
		} finally {
			deleteCaches.clear();
			try {
				batch.close();
			} catch (IOException e) {
				logger.error("batchDelete", e);
			}
		}
	}

	public void repair() {
		try {
			factory.repair(workPath, options);
		} catch (IOException e) {
			logger.error("repair", e);
		}
	}

	public void close() {
		try {
			dbInstance.close();
		} catch (IOException e) {
			logger.error("close", e);
		}
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000000; i++) {
			LevelDb.SCHEDULER.put("zhoudong" + i, "test" + i);
			if (i > 1000) {
				System.out.println(LevelDb.SCHEDULER.getString("zhoudong" + (i - 1000)));
			}
		}
		System.out.println(System.currentTimeMillis() - start);
	}

}