package org.z.common.cache;

import java.io.Serializable;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

class Thread1 extends Thread {
	public Cache<Integer> cache;
	private String key = null;
	private Integer value = null;

	public Thread1(Cache<Integer> cache, String key, Integer value) {
		this.cache = cache;
		this.key = key;
		this.value = value;
	}

	public void run() {
		this.cache.put(key, value);
	}
}

public class Cache<T> implements Serializable {
	private static final long serialVersionUID = -3864114727885057418L;
	protected int maxSize = 0;
	private final ConcurrentHashMap<String, CacheEntry<T>> entries = new ConcurrentHashMap<String, CacheEntry<T>>(1000);

	public Cache(int maxSize) {
		this.maxSize = maxSize;
	}

	public CacheEntry<T> put(String key, T value) {
		CacheEntry<T> entry = new CacheEntry<T>(key, value);
		entries.put(entry.key, entry);
		deleteOverflow();
		return entry;
	}

	public CacheEntry<T> get(String key) {
		CacheEntry<T> entry = entries.get(key);
		if (entry != null) {
			entry.touch();
		}
		return entry;
	}

	protected void deleteOverflow() {
		while (entries.size() > this.maxSize) {
			final CacheEntry<T> oldestEntry = Collections.min(entries.values());
			entries.remove(oldestEntry.key);
		}
	}

	public void clear() {
		entries.clear();
	}

	public static void main(String[] args) {
		Cache<Integer> cache = new Cache<Integer>(1000);
		for (int i = 0; i < 10000; i++) {
			new Thread1(cache, String.valueOf(i), Integer.valueOf(i)).start();
		}

	}

}
