package org.x.cloud.dict;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.cloud.util.StringUtil;

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
	public static boolean DevelopMode = false;
	public static boolean solrSql = false;
	public static long SocketConnectTimeout = 0;
	public static byte[] ServerNameBytes = null;
	public static byte[] ServerIPBytes = null;
	public static HashMode AppConnMode = null;
	public static int ZooKeeperTimeout = 60 * 1000 * 1000;
	public static String BroadCastIP = null;
	public static String coo8Ip = null;
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
	public static int BeanstalkPort = 11300;
	public static final int DataPacketLimit = 1024;
	public static String currentEnv;
	public static String increaseIP = null;
	public static String gomeIp = null;
	public static String facetDataIP = null;
	public static boolean DebugVelocity = false;
	public static Integer NginxIP = null;
	public static String BeanstalkIP = null;
	public static boolean enabledBeanstalkIndex = false;
	public static String DictRootPath;
	public static String indexType=null;
	public static boolean  enableEmbed=true;
	public static String esIndexIp=null;
	public static String indexName=null;
	public static String indexName_reIndex=null;
	public static String scheduleClientIp=null;
	public static String PluginPath = null;
	public static String PdfPath = null;
	public static String ConfigPath = null;
	static {
		localName = StringUtil.getLocalName();
		ConfigPath = ConfigFile.rock().getItem("ConfigPath", "/workspace/plugins/");
		PluginPath = ConfigFile.rock().getItem("PluginPath", "/workspace/plugins/");
		PdfPath = ConfigFile.rock().getItem("PdfPath", "/server/pdf/");
		currentEnv = ConfigFile.rock().getItem("currentEnv", "local");
		ZooIP = ConfigFile.rock().getItem("ZooIP", localName);
		localIP = StringUtil.getLocalIP();
		workDir = StringUtil.currentPath();
		ServerName = ConfigFile.rock().getItem("ServerName", localName);
		BroadCastIP = ConfigFile.rock().getItem("BroadCastIP", "10.58.50.100");
		coo8Ip = ConfigFile.rock().getItem("coo8Ip", "10.58.13.41");
		ServerIP = ConfigFile.rock().getItem("ServerIP", localIP);
		byServiceNames = new LinkedHashMap<String, Integer>();
		byServiceIndexes = new LinkedHashMap<Integer, String>();
		DevelopMode = ConfigFile.rock().getBooleanItem("DevelopMode", false);
		solrSql = ConfigFile.rock().getBooleanItem("solrSql", false);
		DevelopName = ConfigFile.rock().getItem("DevelopName", Global.ServerName);
		ServerNameBytes = StringUtil.toBytes(ServerName);
		ServerIPBytes = StringUtil.toBytes(ServerIP);
		SocketConnectTimeout = ConfigFile.rock().getIntItem("AppConnectTimeout", "10000");
		AppConnMode = Global.HashMode.valueOf(ConfigFile.rock().getItem("AppConnectMode", "AppServer").trim().toLowerCase());
		increaseIP = ConfigFile.rock().getItem("increaseIP", localIP);
		gomeIp = ConfigFile.rock().getItem("gomeIp", localIP);
		DebugVelocity = ConfigFile.rock().getBooleanItem("DebugVelocity", false);
		facetDataIP = ConfigFile.rock().getItem("facetDataIP", localIP);
		NginxIP = Integer.parseInt(ConfigFile.rock().getItem("NginxIP", "0").toString());
		BeanstalkIP = ConfigFile.rock().getItem("beanstalkIP", "10.58.50.56");
		enabledBeanstalkIndex = ConfigFile.rock().getBooleanItem("enabledBeanstalkIndex", false);
		DictRootPath = ConfigFile.segment().getItem("DictRootPath", "/server/conf/split/");
		indexType=ConfigFile.rock().getItem("IndexType", "SOLR");
		enableEmbed=ConfigFile.rock().getBooleanItem("EnableEmbeded", true);
		esIndexIp=ConfigFile.rock().getItem("ESIndexIp", "10.57.41.215");
		indexName=ConfigFile.rock().getItem("IndexName", "product");
		indexName_reIndex=ConfigFile.rock().getItem("IndexName_reindex", "product");
		
		scheduleClientIp=ConfigFile.rock().getItem("scheduleClientIp", "10.57.41.215");
		
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
		datachange, dragon, skuChange, stateChange, prdCounter, runtimeProdInfo, click, dragonIncreat
	}

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

	public enum UserRole {
		Tourist, Member, Guider, Driver, Leader, TravelAgent, Landlord, Hotel, Accounter, CarAssistant, RoomAssistant, TripAssistant, TopicAssistant, RequireAssistant, ProductAssistant, Root, PushAssistant
	};

	public enum SecurityType {
		Email, Name, UserName, UserPassword, MongoUser, MongoPassword, MysqlUser, MysqlPassword
	}

	public enum SecurityMode {
		Do, Undo
	};

	public enum BizType {
		none, topic, comment, product, map, car, trip, room, order, finance, require, group
	}

	public enum Role {
		Tourist, Member, Guider, Driver, Leader, TravelAgent, Landlord, Hotel, Accounter, CarAssistant, RoomAssistant, TripAssistant, TopicAssistant, RequireAssistant, ProductAssistant, Root, PushAssistant
	}

	public enum IllegalType {
		common, serious
	}

	public static enum PayCode {
		Fee, Cut, Recharge, Refund, Withdraw, Confirm, Car, Trip, HotelSublet, Room, PhoneCard, SMS, Appoint, UserCard
	}

	public static enum PayState {
		NoSupport, NoRecord, Fail, ExpiredFail, Pending, AccountAbnormal, NoMoney, Success,
	}

	public static enum PayMode {
		PendingInc, Inc, Dec
	}

	public enum UserType {
		Create, Register, Sina, QQ, Net163, RenRen
	};

	public static enum OrderState {
		WaitAccept, AutoCancel, CustomerCancel, ProviderCancel, Accept, ContractCancel, ContractCancelAccept, Finish, Dispute, FinishWithoutComment
	}

	public enum TopicType {
		ci, he, wan, le, artical, help, note, shop, reqguide, reqcar, reqproduct, sharetrip
	}

	// ask:交流; book:预订; accept:接受预订; deny:拒绝预订; offer:特别优惠 ;
	public static enum ActionType {
		book, ask, accept, deny, offer, notify, system
	}

	public static enum RecordState {
		New, Check, Open, Close, Delete
	}

	public enum ImageType {
		Face, Auth, Picture, Snapshot, product
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

	/***
	 * LOOKING: 初始化状态, LEADING: 领导者状态, FOLLOWING: 跟随者状态,
	 * OBSERVING:观察者，参与投票，不参与选举
	 */
	public enum ServerState {
		LOOKING, FOLLOWING, LEADING, OBSERVING;
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
