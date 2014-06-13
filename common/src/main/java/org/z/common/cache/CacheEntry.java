package org.z.common.cache;

import java.io.Serializable;

public class CacheEntry<T> implements Comparable<CacheEntry<T>>, Serializable {

	private static final long serialVersionUID = 588400350259242484L;
	public final String key;
	public final T value;
	private long lastAccess;

	public CacheEntry(final String key, final T value) {
		this.key = key;
		this.value = value;
		lastAccess = System.currentTimeMillis();
	}

	public int compareTo(final CacheEntry<T> other) {
		long v = lastAccess - other.lastAccess;
		return (int) v;
	}

	public void touch() {
		lastAccess = System.currentTimeMillis();
	}

	public String toString() {
		return key + "[" + value.toString() + "]";
	}

}


