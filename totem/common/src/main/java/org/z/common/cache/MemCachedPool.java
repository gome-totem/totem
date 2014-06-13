package org.z.common.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import com.schooner.MemCached.ObjectTransCoder;
import com.schooner.MemCached.SchoonerSockIOPool;
import com.schooner.MemCached.TransCoder;

public class MemCachedPool {
	public static final String DEF_POOL_NAME = "default";
	public static final String DEF_DEFAULT_ENCODING = "UTF-8";
	public static final boolean DEF_IS_TCP = true;
	public static final boolean DEF_IS_BINARY_PROTOCAL = false;
	public static final boolean DEF_IS_SANITIZE_KEYS = true;
	public static final boolean DEF_IS_PRIMITIVE_AS_STRING = false;

	public static final int DEF_POOL_MULTIPLIER = 3;
	public static final int DEF_INIT_CONN = 5;
	public static final int DEF_MIN_CONN = 5;
	public static final int DEF_MAX_CONN = 100;
	public static final long DEF_MAX_IDLE = 1000 * 60 * 5;
	public static final long DEF_MAX_BUSY_TIME = 1000 * 30;
	public static final long DEF_MAINT_SLEEP = 1000 * 30;
	public static final int DEF_SOCKET_TO = 1000 * 3;
	public static final int DEF_SOCKET_CONNECT_TO = 1000 * 3;
	public static final boolean DEF_IS_FAILOVER = true;
	public static final boolean DEF_IS_FAILBACK = true;
	public static final boolean DEF_IS_NAGLE = false;
	public static final boolean DEF_IS_ALIVE_CHECK = false;
	public static final int DEF_HASHING_ALG = SchoonerSockIOPool.NATIVE_HASH;
	public static final int DEF_BUFFER_SIZE = 1024 * 1025;

	public static final TransCoder DEF_TRANS_CODER = new ObjectTransCoder();

	private String poolName = DEF_POOL_NAME;
	private String defaultEncoding = DEF_DEFAULT_ENCODING;
	private boolean tcp = DEF_IS_TCP;
	private boolean binaryProtocal = DEF_IS_BINARY_PROTOCAL;
	private boolean sanitizeKeys = DEF_IS_SANITIZE_KEYS;
	private boolean primitiveAsString = DEF_IS_PRIMITIVE_AS_STRING;

	private int initConn = DEF_INIT_CONN;
	private int minConn = DEF_MIN_CONN;
	private int maxConn = DEF_MAX_CONN;
	private long maxIdle = DEF_MAX_IDLE;
	private long maxBusyTime = DEF_MAX_BUSY_TIME;
	private long maintSleep = DEF_MAINT_SLEEP;
	private int socketTO = DEF_SOCKET_TO;
	private int socketConnectTO = DEF_SOCKET_CONNECT_TO;
	private boolean failover = DEF_IS_FAILOVER;
	private boolean failback = DEF_IS_FAILBACK;
	private boolean nagle = DEF_IS_NAGLE;
	private boolean aliveCheck = DEF_IS_ALIVE_CHECK;
	private int hashingAlg = DEF_HASHING_ALG;
	private int bufferSize = DEF_BUFFER_SIZE;

	private TransCoder transCoder = DEF_TRANS_CODER;

	private String[] servers = null;
	private Integer[] weights = null;

	private SockIOPool pool = null;
	private MemCachedClient mcc = null;

	private static Map<String, MemCachedPool> instances = new HashMap<String, MemCachedPool>();

	public MemCachedPool() {
		String[] s = { "" };
		this.servers = s;
		this.initialize();
	}


	/** 获取默认缓存池实例（如果没有则进行创建） */
	public static MemCachedPool getInstance() {
		return getInstance(DEF_POOL_NAME);
	}

	/** 获取指定名称的缓存池实例（如果没有则进行创建） */
	synchronized public static MemCachedPool getInstance(String poolName) {
		MemCachedPool cache = instances.get(poolName);

		if (cache == null) {
			cache = new MemCachedPool();
			cache.poolName = poolName;

			instances.put(cache.poolName, cache);
		}

		return cache;
	}

	/** 删除默认缓存池实例 */
	public static boolean removeInstance() {
		return removeInstance(DEF_POOL_NAME);
	}

	/** 删除指定名称的缓存池实例 */
	synchronized public static boolean removeInstance(String poolName) {
		MemCachedPool cache = instances.remove(poolName);

		if (cache != null)
			cache.unInitialize();

		return cache != null;
	}

	/** 删除所有缓存池 */
	synchronized public static void shutdown() {
		Set<Map.Entry<String, MemCachedPool>> entries = instances.entrySet();

		for (Map.Entry<String, MemCachedPool> entry : entries)
			entry.getValue().unInitialize();

		instances.clear();
	}

	/** 获取默认缓存池内部的 {@link SockIOPool} 对象 */
	public static SockIOPool getSockIOPool() {
		return getSockIOPool(DEF_POOL_NAME);
	}

	/** 获取指定名称的缓存池内部的 {@link SockIOPool} 对象 */
	public static SockIOPool getSockIOPool(String poolName) {
		SockIOPool pool = null;
		MemCachedPool cache = instances.get(poolName);

		if (cache != null)
			pool = cache.pool;

		return pool;
	}

	/** 获取默认缓存池内部的 {@link MemCachedClient} 对象 */
	public static MemCachedClient getCachedClient() {
		return getCachedClient(DEF_POOL_NAME);
	}

	/** 获取指定名称的缓存池内部的 {@link MemCachedClient} 对象 */
	public static MemCachedClient getCachedClient(String poolName) {
		MemCachedClient client = null;
		MemCachedPool cache = instances.get(poolName);

		if (cache != null)
			client = cache.mcc;

		return client;
	}

	/** 根据默认缓存池内部的 {@link SockIOPool} 创建相应的 {@link MemCachedClient} 对象 */
	public static MemCachedClient createCachedClient() {
		return createCachedClient(DEF_POOL_NAME);
	}

	/**
	 * 根据默认缓存池内部的 {@link SockIOPool} 创建相应的 {@link MemCachedClient} 对象，并指定是否使用
	 * TCP 连接
	 */
	public static MemCachedClient createCachedClient(boolean tcp) {
		return createCachedClient(DEF_POOL_NAME, tcp);
	}

	/** 根据指定名称的缓存池内部的 {@link SockIOPool} 创建相应的 {@link MemCachedClient} 对象 */
	public static MemCachedClient createCachedClient(String poolName) {
		MemCachedPool cache = instances.get(poolName);

		if (cache != null)
			return createCachedClient(poolName, cache.tcp);

		return null;
	}

	/**
	 * 根据指定名称的缓存池内部的 {@link SockIOPool} 创建相应的 {@link MemCachedClient} 对象，并指定是否使用
	 * TCP 连接
	 */
	public static MemCachedClient createCachedClient(String poolName, boolean tcp) {
		MemCachedClient client = null;
		MemCachedPool cache = instances.get(poolName);

		if (cache != null) {
			client = new MemCachedClient(cache.poolName, tcp, cache.binaryProtocal);

			client.setDefaultEncoding(cache.defaultEncoding);
			client.setPrimitiveAsString(cache.primitiveAsString);
			client.setSanitizeKeys(cache.sanitizeKeys);
			client.setTransCoder(cache.transCoder);
		}

		return client;
	}

	/** 初始化缓存池并创建缓存池内部的 {@link SockIOPool} 和 {@link MemCachedClient} 对象 */
	synchronized public boolean initialize() {
		if (!hasInitialized()) {
			pool = SockIOPool.getInstance(poolName, tcp);

			pool.setAliveCheck(aliveCheck);
			pool.setBufferSize(bufferSize);
			pool.setFailback(failback);
			pool.setFailover(failover);
			pool.setHashingAlg(hashingAlg);
			pool.setInitConn(initConn);
			pool.setMaintSleep(maintSleep);
			pool.setMaxBusyTime(maxBusyTime);
			pool.setMaxConn(maxConn);
			pool.setMaxIdle(maxIdle);
			pool.setMinConn(minConn);
			pool.setNagle(nagle);
			pool.setSocketConnectTO(socketConnectTO);
			pool.setSocketTO(socketTO);
			pool.setServers(servers);
			pool.setWeights(weights);
			pool.initialize();

			mcc = createCachedClient(poolName, tcp);

			return true;
		}

		return false;
	}

	/** 检查缓存池是否已被初始化 */
	public boolean hasInitialized() {
		return pool != null && pool.isInitialized();
	}

	/** 关闭缓存池，并清除缓存池内部的 {@link SockIOPool} 和 {@link MemCachedClient} 对象 */
	synchronized public void unInitialize() {
		if (hasInitialized()) {
			pool.shutDown();

			pool = null;
			mcc = null;
		}
	}

	/** 参考：{@link MemCachedClient#setDefaultEncoding} */
	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	/** 参考：{@link MemCachedClient#setDefaultEncoding} */
	public void setDefaultEncoding(String defaultEncoding) {
		if (hasInitialized())
			throw new IllegalStateException("the MemCached Pool had been initialized, can not modify attrs.");

		this.defaultEncoding = defaultEncoding;
	}

	/** 获取：是否使用 TCP 协议 */
	public boolean isTcp() {
		return tcp;
	}

	/** 设置：是否使用 TCP 协议 */
	public void setTcp(boolean tcp) {
		checkModifyAttrs();

		this.tcp = tcp;
	}

	/** 获取：是否使用二进制协议 */
	public boolean isBinaryProtocal() {
		return binaryProtocal;
	}

	/** 设置：是否使用二进制协议 */
	public void setBinaryProtocal(boolean binaryProtocal) {
		checkModifyAttrs();

		this.binaryProtocal = binaryProtocal;
	}

	/** 参考：{@link MemCachedClient#setSanitizeKeys} */
	public boolean isSanitizeKeys() {
		return sanitizeKeys;
	}

	/** 参考：{@link MemCachedClient#setSanitizeKeys} */
	public void setSanitizeKeys(boolean sanitizeKeys) {
		checkModifyAttrs();

		this.sanitizeKeys = sanitizeKeys;
	}

	/** 参考：{@link MemCachedClient#setPrimitiveAsString} */
	public boolean isPrimitiveAsString() {
		return primitiveAsString;
	}

	/** 参考：{@link MemCachedClient#setPrimitiveAsString} */
	public void setPrimitiveAsString(boolean primitiveAsString) {
		checkModifyAttrs();

		this.primitiveAsString = primitiveAsString;
	}

	/** 参考：{@link SockIOPool#getInitConn} */
	public int getInitConn() {
		return initConn;
	}

	/** 参考：{@link SockIOPool#setInitConn} */
	public void setInitConn(int initConn) {
		checkModifyAttrs();

		this.initConn = initConn;
	}

	/** 参考：{@link SockIOPool#getMinConn} */
	public int getMinConn() {
		return minConn;
	}

	/** 参考：{@link SockIOPool#setMinConn} */
	public void setMinConn(int minConn) {
		checkModifyAttrs();

		this.minConn = minConn;
	}

	/** 参考：{@link SockIOPool#getMaxConn} */
	public int getMaxConn() {
		return maxConn;
	}

	/** 参考：{@link SockIOPool#setMaxConn} */
	public void setMaxConn(int maxConn) {
		checkModifyAttrs();

		this.maxConn = maxConn;
	}

	/** 参考：{@link SockIOPool#getMaxIdle} */
	public long getMaxIdle() {
		return maxIdle;
	}

	/** 参考：{@link SockIOPool#setMaxIdle} */
	public void setMaxIdle(long maxIdle) {
		checkModifyAttrs();

		this.maxIdle = maxIdle;
	}

	/** 参考：{@link SockIOPool#setMaxBusyTime} */
	public long getMaxBusyTime() {
		return maxBusyTime;
	}

	/** 参考：{@link SockIOPool#setMaxBusyTime} */
	public void setMaxBusyTime(long maxBusyTime) {
		checkModifyAttrs();

		this.maxBusyTime = maxBusyTime;
	}

	/** 参考：{@link SockIOPool#getMaintSleep} */
	public long getMaintSleep() {
		return maintSleep;
	}

	/** 参考：{@link SockIOPool#setMaintSleep} */
	public void setMaintSleep(long maintSleep) {
		checkModifyAttrs();

		this.maintSleep = maintSleep;
	}

	/** 参考：{@link SockIOPool#getSocketTO} */
	public int getSocketTO() {
		return socketTO;
	}

	/** 参考：{@link SockIOPool#setSocketTO} */
	public void setSocketTO(int socketTO) {
		checkModifyAttrs();

		this.socketTO = socketTO;
	}

	/** 参考：{@link SockIOPool#getSocketConnectTO} */
	public int getSocketConnectTO() {
		return socketConnectTO;
	}

	/** 参考：{@link SockIOPool#setSocketConnectTO} */
	public void setSocketConnectTO(int socketConnectTO) {
		checkModifyAttrs();

		this.socketConnectTO = socketConnectTO;
	}

	/** 参考：{@link SockIOPool#getFailover} */
	public boolean isFailover() {
		return failover;
	}

	/** 参考：{@link SockIOPool#setFailover} */
	public void setFailover(boolean failover) {
		checkModifyAttrs();

		this.failover = failover;
	}

	/** 参考：{@link SockIOPool#getFailback} */
	public boolean isFailback() {
		return failback;
	}

	/** 参考：{@link SockIOPool#setFailback} */
	public void setFailback(boolean failback) {
		checkModifyAttrs();

		this.failback = failback;
	}

	/** 参考：{@link SockIOPool#getNagle} */
	public boolean isNagle() {
		return nagle;
	}

	/** 参考：{@link SockIOPool#setNagle} */
	public void setNagle(boolean nagle) {
		checkModifyAttrs();

		this.nagle = nagle;
	}

	/** 参考：{@link SockIOPool#getAliveCheck} */
	public boolean isAliveCheck() {
		return aliveCheck;
	}

	/** 参考：{@link SockIOPool#setAliveCheck} */
	public void setAliveCheck(boolean aliveCheck) {
		checkModifyAttrs();

		this.aliveCheck = aliveCheck;
	}

	/** 参考：{@link SockIOPool#getHashingAlg} */
	public int getHashingAlg() {
		return hashingAlg;
	}

	/** 参考：{@link SockIOPool#setHashingAlg} */
	public void setHashingAlg(int hashingAlg) {
		checkModifyAttrs();

		this.hashingAlg = hashingAlg;
	}

	/** 参考：{@link SockIOPool#getBufferSize} */
	public int getBufferSize() {
		return bufferSize;
	}

	/** 参考：{@link SockIOPool#setBufferSize} */
	public void setBufferSize(int bufferSize) {
		checkModifyAttrs();

		this.bufferSize = bufferSize;
	}

	/** 参考：{@link MemCachedClient#setTransCoder} */
	public TransCoder getTransCoder() {
		return transCoder;
	}

	/** 参考：{@link MemCachedClient#setTransCoder} */
	public void setTransCoder(TransCoder transCoder) {
		checkModifyAttrs();

		this.transCoder = transCoder;
	}

	/** 参考：{@link SockIOPool#getServers} */
	public String[] getServers() {
		return servers;
	}

	/** 参考：{@link SockIOPool#setServers} */
	public void setServers(String... servers) {
		checkModifyAttrs();

		this.servers = servers;
	}

	/** 参考：{@link SockIOPool#getWeights} */
	public Integer[] getWeights() {
		return weights;
	}

	/** 参考：{@link SockIOPool#setWeights} */
	public void setWeights(Integer... weights) {
		checkModifyAttrs();

		this.weights = weights;
	}

	/** 参考：{@link SockIOPool} */
	public String getPoolName() {
		return poolName;
	}

	private void checkModifyAttrs() {
		if (hasInitialized())
			throw new IllegalStateException("the MemCached Pool had been initialized, can not modify attrs.");
	}
}

