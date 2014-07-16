package org.z.global.dict;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServiceName;

public class Global {

	protected static Logger logger = LoggerFactory.getLogger(Global.class);

	public static final char SPLITCHAR = '☉';
	public static LinkedHashMap<Integer, String> byServiceIndexes = null;
	public static LinkedHashMap<String, Integer> byServiceNames = null;
	public static String ZooIP = null;
	public static String ServerName = null;
	public static String ServerIP = null;
	public static String localName = null;
	public static String localIP = null;
	public static String workDir = null;
	public static String DevelopName = null;
	public static String PluginPath = null;
	public static String PdfPath = null;
	public static boolean DevelopMode = false;
	public static long SocketConnectTimeout = 0;
	public static byte[] ServerNameBytes = null;
	public static byte[] ServerIPBytes = null;
	public static HashMode AppConnMode = null;
	public static int ZooKeeperTimeout = 60 * 1000 * 1000;
	public static String BroadCastIP = null;
	public static String redisIp = null;
	public static int ZooKeeperPort = 19750;
	public static int MemcachedPort = 19751;
	public static int MongoPort = 19753;
	public static int MQRouterPublishPort = 19755;
	public static int MQRouterBoardcastPort = 19752;
	public static int MQRouterPort = 19757;
	public static int MQAppPublishPort = 19758;
	public static int MQAppServerPort = 19759;
	public static int SocketRouterPort = 19760;
	public static int SocketCallPort = 19761;
	public static int SocketAppPort = 19763;
	public static int RomotePort = 19766;
	public static int RedisPort = 6379;
	public static final int DataPacketLimit = 1024;
	public static String currentEnv;
	public static String zooUserName = null;
	public static String zooPassword = null;
	public static String indexType=null;
	public static String indexName_reIndex=null;
	public static String indexName=null;

	static {
		localName = StringUtil.getLocalName();
		PluginPath = ConfigFile.rock().getItem("PluginPath", "/workspace/plugins/");
		PdfPath = ConfigFile.rock().getItem("PdfPath", "/server/pdf/");
		currentEnv = ConfigFile.rock().getItem("currentEnv", "local");
		ZooIP = ConfigFile.rock().getItem("ZooIP", localName);
		localIP = StringUtil.getLocalIP();
		workDir = StringUtil.currentPath();
		ServerName = ConfigFile.rock().getItem("ServerName", localName);
		BroadCastIP = ConfigFile.rock().getItem("BroadCastIP", "10.58.50.100");
		indexType=ConfigFile.rock().getItem("IndexType", "es");
		redisIp = ConfigFile.rock().getItem("redisIp", "10.58.50.100");
		ServerIP = ConfigFile.rock().getItem("ServerIP", localIP);
		byServiceNames = new LinkedHashMap<String, Integer>();
		byServiceIndexes = new LinkedHashMap<Integer, String>();
		DevelopMode = ConfigFile.rock().getBooleanItem("DevelopMode", false);
		DevelopName = ConfigFile.rock().getItem("DevelopName", Global.ServerName);
		zooUserName = ConfigFile.rock().getItem("ZooUserName", "");
		zooPassword = ConfigFile.rock().getItem("ZooPassword", "");
		indexName_reIndex=ConfigFile.rock().getItem("IndexName_reindex", "product");
		ServerNameBytes = StringUtil.toBytes(ServerName);
		ServerIPBytes = StringUtil.toBytes(ServerIP);
		indexName=ConfigFile.rock().getItem("IndexName", "product");
		SocketConnectTimeout = ConfigFile.rock().getIntItem("AppConnectTimeout", "10000");
		AppConnMode = Global.HashMode.valueOf(ConfigFile.rock().getItem("AppConnectMode", "AppServer").trim().toLowerCase());
		logger.info("device localIP={} & localName={} & WorkDir={}", new String[] { localIP, localName, workDir });
	}

	public static enum SocketState {
		success, fail
	}

	public static enum SocketHeadTag {
		json, chunk, nextchunk
	}

	public static enum SocketDataType {
		json, bytes, string
	}

	public static enum SocketConnectType {
		stream, http, websocket, jms
	}

	public static enum LogLevel {
		info, error, warn, debug
	}

	public static enum ModuleMessageType {
		datachange, dragon, productPrice, promoScore, facetchange
	};

	public static enum MessageSource {
		fromClient, fromRouter, fromApp, fromSocket
	};

	public static enum MessageMode {
		sync, async
	}

	public enum DictConnectMode {
		file, server;
	}

	public enum HashMode {
		router, appserver
	}

	public enum LockMode {
		Lock, UnLock
	}

	public enum SocketMode {
		xsocket, netty
	};

	public enum Role {
		Tourist, Member, Guider, Driver, Leader, TravelAgent, Landlord, Hotel, Accounter, CarAssistant, RoomAssistant, TripAssistant, TopicAssistant, RequireAssistant, ProductAssistant, Root, PushAssistant, CustomerService,AirTicket
	};

	public enum SecurityType {
		Email, Name, UserName, UserPassword, MongoUser, MongoPassword, MysqlUser, MysqlPassword
	}

	public enum SecurityMode {
		Do, Undo
	};

	public enum SearchMode {
		Sync, Async
	}

	public enum TripItemType {
		food, activity, sight, history, shop, sex,
	}

	public enum BizType {
		ask, topic, comment, product, activity, car, trip, room, order, finance, require, group
	}

	public enum CodeType {
		contient, country, location, category, serviceTag, priceTag, priceRange, language, belongCity
	}

	public enum IllegalType {
		common, serious
	}

	public static enum PayCode {
		Fee, Recharge, Refund, Withdraw, PhoneCard, SMS, UserCard, GoldCoin, Order,AirTicket
	}

	public static enum PayState {
		NoSupport, NoRecord, Fail, ExpiredFail, Pending, AccountAbnormal, NoMoney, Success
	}

	public static enum PayMode {
		PendingInc, Inc, Dec
	}

	public enum UserType {
		Create, Register, Sina, QQ, Net163, RenRen
	};

	public static enum OrderState {
		WaitAccept, AutoCancel, CustomerCancel, ProviderCancel, Accept, ContractCancel, ContractCancelAccept, Pending, Paid, Checked, Running, Close, Dispute
	}

	public enum TopicType {
		ci, he, wan, le, artical, help, note, shop, reqguide, reqcar, reqproduct, sharetrip
	}

	// talk:交流; book:预订; accept:接受预订; deny:拒绝预订; offer:特别优惠 ;
	public static enum ActionType {
		inquiry, book, talk, accept, deny, offer, notify, system
	}

	public static enum DialogueType {
		service, talk, order
	}

	public static enum RecordState {
		New, Check, Open, Close, Delete, Expire
	}

	public enum ImageType {
		Face, Auth, Picture, Snapshot, Product, Tips
	}

	public enum IndexType {
		none, trip, car, product, require, topic
	}

	public static IndexType IndexTypeBy(BizType type) {
		switch (type) {
		case trip:
			return IndexType.trip;
		case car:
			return IndexType.car;
		case product:
			return IndexType.product;
		case require:
			return IndexType.require;
		case topic:
			return IndexType.topic;
		default:
			return IndexType.none;
		}
	}

	public static ServiceName ServiceNameBy(IndexType type) {
		switch (type) {
		case trip:
		case car:
			return ServiceName.TripIndex;
		case product:
			return ServiceName.ProductIndex;
		case require:
			return ServiceName.RequireIndex;
		case topic:
			return ServiceName.TopicIndex;
		default:
			return ServiceName.TripIndex;
		}
	}

}
