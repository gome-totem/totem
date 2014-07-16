package org.z.store.leveldb;

import static org.fusesource.leveldbjni.JniDBFactory.bytes;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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
import org.z.global.environment.Const;
import org.z.global.util.StringUtil;


public class LevelDB {
	private String path = null;
	private String name = null;
	protected DB instance = null;
	private File workPath = null;
	protected static Logger logger = LoggerFactory.getLogger(LevelDB.class);
	protected static org.iq80.leveldb.Logger log = null;
	protected static DBFactory factory = JniDBFactory.factory;
	protected static Options options = new Options().createIfMissing(true).cacheSize(100 * 1048576);
	protected ConcurrentHashMap<String, String> updateCaches = new ConcurrentHashMap<String, String>();
	protected ConcurrentHashMap<String, Integer> deleteCaches = new ConcurrentHashMap<String, Integer>();
	protected Integer LockDelete = 0;
	protected Integer LockUpdate = 1;
	protected long deleteTime = System.currentTimeMillis();
	public LevelDB(String path, String name) {
		this.name = name;
		this.path = path;
	}

	public DB instance() {
		return this.instance;
	}

	public boolean init() {
		workPath = new File(path + name);
		try {
			if (workPath.exists()) {
				FileUtils.deleteQuietly(new File(path + name + "/LOCK"));
			} else {
				FileUtils.forceMkdir(workPath);
			}
			instance = factory.open(workPath, options);
		} catch (IOException e) {
			logger.error("init", e);
			instance = null;
		}
		return instance != null;
	}

	public void repair() {
		try {
			factory.repair(workPath, options);
		} catch (IOException e) {
		}
	}

	public String get(String prefix, String key) {
		return get(StringUtil.append(prefix, "@", key));
	}

	public String get(String key) {
		if (this.deleteCaches.contains(key)) {
			return null;
		}
		String v = updateCaches.get(key);
		if (v != null) {
			return v;
		}
		byte[] bytes = instance.get(bytes(key));
		if (bytes == null) {
			return null;
		}
		return StringUtil.toString(bytes);
	}

	public void put(String key, Object value) {
		synchronized (LockUpdate) {
			deleteCaches.remove(key);
			updateCaches.put(key, String.valueOf(value));
			if (updateCaches.size() >= Const.LevelDB_BatchUpdate) {
				this.batchWrite(updateCaches);
			}
		}
	}

	public void put(String prefix, String key, Object value) {
		put(StringUtil.append(prefix, "@", key), value);
	}

	public void save() {
		synchronized (LockUpdate) {
			if (updateCaches.size() == 0) {
				return;
			}
			this.batchWrite(updateCaches);
			updateCaches.clear();
		}
		synchronized (LockDelete) {
			if (deleteCaches.size() == 0) {
				return;
			}
			this.batchDelete(deleteCaches.keySet());
			deleteCaches.clear();
		}
	}

	public void delete(String key) {
		synchronized (LockDelete) {
			deleteCaches.put(key, 0);
			if (deleteCaches.size() >= Const.LevelDB_BatchDelete) {
				this.batchDelete(deleteCaches.keySet());
				deleteCaches.clear();
			}
		}
	}
	public void delete(String key,boolean noDelay) {
		synchronized (LockDelete) {
			deleteCaches.put(key, 0);
			long end = System.currentTimeMillis();
			if (deleteCaches.size() >= Const.LevelDB_BatchDelete || end - deleteTime >= 60000||noDelay) {
				deleteTime = end;
				this.batchDelete(deleteCaches.keySet());
				deleteCaches.clear();
			}
		}
	}

	public void batchWrite(ConcurrentHashMap<String, String> records) {
		WriteOptions wo = new WriteOptions().sync(true);
		WriteBatch batch = instance.createWriteBatch();
		try {
			for (Iterator<Entry<String, String>> i = records.entrySet().iterator(); i.hasNext();) {
				Entry<String, String> entry = i.next();
				batch.put(bytes(entry.getKey()), StringUtil.toBytes(entry.getValue()));
			}
			instance.write(batch, wo);
		} finally {
			records.clear();
			try {
				batch.close();
			} catch (IOException e) {
			}
		}
	}

	public void batchDelete(Set<String> records) {
		WriteOptions wo = new WriteOptions().sync(true);
		WriteBatch batch = instance.createWriteBatch();
		try {
			for (Iterator<String> i = records.iterator(); i.hasNext();) {
				String key = i.next();
				batch.delete(bytes(key));
			}
			instance.write(batch, wo);
		} finally {
			try {
				batch.close();
			} catch (IOException e) {
			}
		}
	}

	public void close() {
		try {
			this.instance.close();
		} catch (IOException e) {
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LevelDB db = new LevelDB(Const.ConfigPath, "system");
		if (db.init() == false) {
			return;
		}
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000000; i++) {
			db.put("xiaoming" + i, "test" + i);
			if (i > 1000) {
				db.get("xiaoming" + (i - 1000));
			}
		}
		System.out.println(System.currentTimeMillis() - start);
	}
	// 455773
	// 430904
	// 416920
	// 468618，1.7
	// 466639, 1.7
	// 432978，1.7
	// 543752，1.5

}
