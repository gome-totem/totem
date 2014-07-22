package org.z.core.es;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.data.analysis.SmartTokenizerFactory;
import org.z.global.dict.Global;
import org.z.global.factory.ModuleFactory;
import org.z.global.interfaces.DictInfo;
import org.z.global.interfaces.IndexServiceIntf;
import org.z.global.util.StringUtil;
import org.z.store.leveldb.LevelDB;
import org.z.store.redis.RedisFactory;
import org.z.store.redis.RedisSocket;

import redis.clients.jedis.ShardedJedis;

import com.mongodb.BasicDBObject;

public class ESDict implements DictInfo {
	protected static Logger logger = LoggerFactory.getLogger(ESDict.class);
	public static LevelDB runtimeProdDB = null;
	public static ESDict self;
	public static final String redisIp = "10.58.22.2";

	@Override
	public boolean init() {
		SmartTokenizerFactory.init();

		self = this;
		runtimeProdDB = new LevelDB("/server/es", "runtimeProdInfo");
		if (runtimeProdDB.init() == false) {
			logger.error("init  runtimeProdInfo[{}] Fail.", new String[] { "/server/runtimeProdInfo" });
		} else {
			logger.info("init  runtimeProdInfo[{}] Success.", new String[] { "/server/runtimeProdInfo" });
		}

		logger.info("========> EsDict success");
		return true;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	public static void clearDynamicDB(BasicDBObject req, ShardedJedis jedis) {

		if (!isClearCache(req.getString("fromIp")) || req == null || req.get("clearIds") == null) {
			// logger.info("clear ip need not clear or param null!");
			return;
		}

		List<String> clearIds = (List<String>) req.get("clearIds");
		for (String clearId : clearIds) {
			if (!clearId.contains("@")) {
				logger.warn("clear dynamicDB key [{}] invali format ,must contains @!", clearId);
				continue;
			}
			getLevelDB().delete(clearId.substring(0, clearId.lastIndexOf("@")), true);// fv@14sP@f
			String prefixKey[] = clearId.split("@");
			String dynamicData = get(prefixKey[0], prefixKey[1], prefixKey[2].trim().contains("f") ? false : true, jedis);
			// if(prefixKey[2].trim().contains("f"))
			// logger.info("clear dynamicDB key [{}],refresh loacl dynamic from redis f[{}]",new
			// Object[]{clearId,dynamicData==null?"null":dynamicData});
			// else{
			// //
			// logger.info("clear dynamicDB key [{}],refresh loacl dynamic from redis p [{}]",new
			// //
			// Object[]{clearId,dynamicData==null?"null":dynamicData.length()});
			// }

		}
	}

	public static void hdelete(byte[] key, byte[] fields, String ip, ShardedJedis jedis) {
		if (jedis == null)
			return;
		try {
			jedis.hdel(key, fields);
		} catch (Exception e) {
			logger.error("hdelete key " + key + " fail.", e);
		}
	}

	public static Long hset(byte[] key, byte[] field, byte[] value, String ip, ShardedJedis jedis) {
		if (jedis == null)
			return null;
		try {
			return jedis.hset(key, field, value);
		} catch (Exception e) {
			logger.error("hset key " + key + " fail.", e);
		}
		return null;
	}

	public static byte[] hget(byte[] key, byte[] field, String ip, ShardedJedis jedis) {
		if (jedis == null)
			return null;
		try {
			return jedis.hget(key, field);
		} catch (Exception e) {
			logger.error("hget key " + key + " fail.", e);
		}
		return null;
	}

	public static boolean delete(String prefix, String key, ShardedJedis jedis) {

		try {
			getLevelDB().delete(StringUtil.append(prefix, "@", key));
			String redisKeyField[] = splitKey(StringUtil.append(prefix, "@", key));
//			if (Global.ServerIP.equals("10.58.50.105") || Global.ServerIP.equals("10.58.50.106"))
//				RedisPool.getDefaultPool().hdelete(redisKeyField[0].getBytes(), redisKeyField[1].getBytes());
//			else {
				jedis.hdel(redisKeyField[0].getBytes(), redisKeyField[1].getBytes());
//			}

			// hdelete(redisKeyField[0].getBytes(), redisKeyField[1].getBytes(),
			// redisIp, socket);
		} catch (Exception e) {
			logger.error("dynamic delete key [{}]  fail! ,exception [{}]", new Object[] { key, e.getMessage() });
			return false;
		}

		return true;
	}

	/**
	 *
	 * @param prefix
	 * @param key
	 * @param value
	 * @param check
	 *            true 需要检查本地缓存 只是否一致 false 不需要检查 直接更新本地和redis 且其他服务器需要同步
	 * @return true 更新了本地和redis 需要同步到其他服务器 false 不需要同步
	 */
	public static boolean put(String prefix, String key, String value, boolean check, boolean isCompress, ShardedJedis jedis) {
		try {
			// logger.info("---------------"+(check==true?"facet":"pro"));

			//
			if (check) {
				String valueOld = getLevelDB().get(prefix, key);
				if ((StringUtils.isEmpty(valueOld) || "nullValue".equals(valueOld)) && value == null) {
					return false;
				} else if (!StringUtils.isEmpty(valueOld) && !"nullValue".equals(valueOld) && value != null && valueOld.equals(value)) {
					return false;
				}
				// System.out.println("put==========prefix="+prefix+"==========key="+key+"=======value="+value);
			}

			put(prefix, key, value, isCompress, jedis);
		} catch (Exception e) {
			logger.error("dynamic put key [{}] value [{}]  check [{}] fail! ,exception [{}]", new Object[] { key, value, check, e.getMessage() });
			return false;
		}
		return true;
	}

	private static void put(String prefix, String key, String value, boolean isCompress, ShardedJedis jedis) {
		try {
			if (value == null)
				value = "nullValue";
			getLevelDB().put(prefix, key, value);
			String redisKeyField[] = splitKey(StringUtil.append(prefix, "@", key));
//			if (Global.ServerIP.equals("10.58.50.105") || Global.ServerIP.equals("10.58.50.106"))
//				RedisPool.getDefaultPool().hset(redisKeyField[0].getBytes(), redisKeyField[1].getBytes(), isCompress ? StringUtil.compress(StringUtil.toBytes(value)) : StringUtil.toBytes(value));
//			else
				jedis.hset(redisKeyField[0].getBytes(), redisKeyField[1].getBytes(), isCompress ? StringUtil.compress(StringUtil.toBytes(value)) : StringUtil.toBytes(value));

			// hset(redisKeyField[0].getBytes(), redisKeyField[1].getBytes(),
			// isCompress ? StringUtil.compress(value.getBytes()) :
			// value.getBytes(), redisIp, socket);

		} catch (Exception e) {
			logger.error("dynamic put key [{}] value [{}]  fail! ,exception [{}]", new Object[] { key, value, e.getMessage() });
		}
	}

	public static String get(String prefix, String key, boolean isUncompress, ShardedJedis jedis) {
		// logger.info("pre="+prefix+"--key="+key+"--com"+isUncompress);
		if (StringUtils.isEmpty(key)) {
			logger.info("get key null");
			return null;
		}
		String value = getLevelDB().get(prefix, key);
		try {
			if (StringUtils.isEmpty(value) || value.toLowerCase().equals("null") || value.equals("nullValue")) {
				String redisKeyField[] = splitKey(StringUtil.append(prefix, "@", key));

				byte[] valueb = null;

//				if (Global.ServerIP.equals("10.58.50.105") || Global.ServerIP.equals("10.58.50.106")) {
//					valueb = RedisPool.getDefaultPool().hget(redisKeyField[0].getBytes(), redisKeyField[1].getBytes());
//				} else {
					valueb = jedis.hget(redisKeyField[0].getBytes(), redisKeyField[1].getBytes());
//				}

				// byte[] valueb = hget(redisKeyField[0].getBytes(),
				// redisKeyField[1].getBytes(), redisIp, socket);

				value = valueb != null ? StringUtil.toString((isUncompress ? StringUtil.uncompress(valueb) : valueb)) : null;
				// System.out.println("get=================="+value);
				if (!StringUtils.isEmpty(value) && !value.toLowerCase().equals("null")) {
					getLevelDB().put(prefix, key, value);
				} else {
					getLevelDB().put(prefix, key, "nullValue");
				}
			}
		} catch (Exception e) {
			logger.error("dynamic get key [{}]  fail! ,exception [{}]", new Object[] { key, e.getMessage() });
		}
		if ("nullValue".equals(value))
			value = null;

		return value;
	}

	private static String[] splitKey(String key) {
		String[] result = new String[2];
		int splitIndex = key.length() / 2;
		result[0] = new String(key.substring(0, splitIndex));
		result[1] = new String(key.substring(splitIndex, key.length()));
		return result;
	}

	private static LevelDB getLevelDB() {
		return (LevelDB) ((IndexServiceIntf) ModuleFactory.productIndex()).getDynamicDB();
	}

	private static boolean isClearCache(String fromIp) {
		return Global.indexType.equals("es") && !fromIp.equals(Global.localIP);
	}

	public static void main(String[] args) {
		RedisSocket redis = RedisFactory.get(redisIp);
		String key = "d@9010000127";
		int splitIndex = key.length() / 2;
		byte[] hget = redis.instance.hget(key.substring(0, splitIndex).getBytes(), key.substring(splitIndex, key.length()).getBytes());
		System.out.println(new String(StringUtil.uncompress(hget)));

		key = "f@14xw";
		splitIndex = key.length() / 2;
		hget = redis.instance.hget(key.substring(0, splitIndex).getBytes(), key.substring(splitIndex, key.length()).getBytes());
		System.out.println(new String(StringUtil.uncompress(hget)));

		key = "fv@14xw";
		splitIndex = key.length() / 2;
		hget = redis.instance.hget(key.substring(0, splitIndex).getBytes(), key.substring(splitIndex, key.length()).getBytes());
		System.out.println(new String(StringUtil.uncompress(hget)));
	}
}
