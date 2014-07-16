package org.z.global.environment;

import org.z.global.dict.Global;
import org.z.global.dict.Global.BizType;
import org.z.global.dict.Global.HashMode;
import org.z.global.util.DateUtil;



public class Const {
	public static String ERROR_EXIST = "-10004";
	public static final String SPLIT = "〓";
	public static final char[] PRINTABLE_ALPHABET = { '!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9', ':', ';', '<', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', };
	public static final char[] AlphaChars = { '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r',
			's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public static final int HEARTBEAT_LIVENESS = 5;
	public static String RouterLock = "/routerlock";
	public static String AppLock = "/applock";
	public static final int HEARTBEAT_INTERVAL = 3000;
	public static final double RATE_MILE_TO_KM = 1.609344;
	public static final int HEARTBEAT_EXPIRY = HEARTBEAT_LIVENESS * HEARTBEAT_INTERVAL;

	public static long Hour = 3600000;
	public static long Day1Milis = 24 * Hour;
	public static long Day2Milis = 48 * Hour;
	public static long Day3Milis = 72 * Hour;
	public static final long Time2010 = DateUtil.convertCalendar("2010-1-1").getTimeInMillis();

	public static final int INTERVAL_INIT = 1000; // Initial reconnect
	public static final int INTERVAL_MAX = 32000; // After exponential back off

	public static final byte[] LINE_DELIMITER_BYTE = "\r\n".getBytes();
	public static final String LINE_DELIMITER = "\r\n";
	public static int ExpireHours = 24;
   public static String topicslike = "topicslike"; 
	public static String defaultMysqlServer = null;
	public static String defaultMysqlDB = null;
	public static String defaultMongoServer = null;
	public static String defaultMongoDB = null;
	public static String defaultDictMysql = null;
	public static String defaultDictMongo = null;
	public static String defaultDictMysqlDB = null;
	public static String defaultDictMongoDB = null;
	public static String defaultLogServer = null;
	public static String defaultLogDB = null;
	public static String BroadCastIP = null;
	public static String ScheduleIP = null;
	public static String ConfigPath = null;
	public static String RootPath = null;
	public static String SiteName = null;
	public static String ESHome = null;
	public static String ZooIP = null;
	public static int LevelDB_BatchUpdate = 90;
	public static int LevelDB_BatchDelete = 30;
	public static String DictIP = null;
	public static String AppId = null;
	public static HashMode AppConnMode = null;
	public static boolean AppConnectPoolEnable = false;
	public static int AppConnectPoolMax = 10240;
	public static int AppConnectTimeout = 6;
	public static String SolrHome = null;
	public static int threadNum = 3;
	public static String Spliter = null;
	public static String SpliterDirect = null;
	public static String SpliterText = null;
	public static String footerUrl = null;
	public static String headerUrl = null;
	public static String footerPath = null;
	public static String headerPath = null;
	public static String headerScheDule = null;
	public static String footerSchedule = null;
	public static String SecurityClassName = null;
	public static String SocketHandlerClassName = null;
	public static int ticketProvideId = 0;
	public static String DictRootPath =null;
	static {
		defaultMysqlServer = Config.db().getItem("DefaultMysqlServer", "dict");
		defaultMysqlDB = Config.db().getItem("DefaultMysqlDB", "yiqihi");
		defaultLogServer = Config.db().getItem("DefaultLogServer", "x2");
		defaultLogDB = Config.db().getItem("DefaultLogDB", "logs");
		defaultMongoServer = Config.db().getItem("DefaultMongoServer", "dict");
		defaultMongoDB = Config.db().getItem("DefaultMongoDB", "yiqihi");
		defaultDictMysql = Config.db().getItem("DefaultDictMysql", Const.defaultMysqlServer);
		defaultDictMysqlDB = Config.db().getItem("DefaultDictMysqlDB", "dict");
		defaultDictMongo = Config.db().getItem("DefaultDictMongo", Const.defaultMongoServer);
		defaultDictMongoDB = Config.db().getItem("DefaultDictMongoDB", Const.defaultMongoServer);
		ConfigPath = Config.rock().getItem("ConfigPath", Global.workDir + "conf/");
		RootPath = Config.rock().getItem("RootPath", Global.workDir);
		SiteName = Config.rock().getItem("siteName", "/");
		ZooIP = Config.rock().getItem("ZooIP", Global.ServerIP);
		LevelDB_BatchUpdate = Config.rock().getIntItem("LevelDB_BatchUpdate", "90");
		LevelDB_BatchDelete = Config.rock().getIntItem("LevelDB_BatchDelete", "30");
		SolrHome = Config.rock().getItem("SolrHome", "/server/solr/");
		DictRootPath = Config.rock().getItem("DictRootPath", "/server/conf/");
		SecurityClassName = Config.rock().getItem("SecurityClassName", "");
		SocketHandlerClassName = Config.rock().getItem("SocketHandlerClassName", "org.x.service.handler.SocketHandler");
		DictIP = Config.rock().getItem("DictIP", "");
		AppConnMode = Global.HashMode.valueOf(Config.rock().getItem("AppConnectMode", "AppServer").trim().toLowerCase());
		AppConnectPoolEnable = Config.rock().getBooleanItem("AppConnectPoolEnable", true);
		AppConnectPoolMax = Config.rock().getIntItem("AppConnectPoolMax", "10240");
		AppConnectTimeout = Config.rock().getIntItem("AppConnectTimeout", "6");
		Spliter = Config.segment().getItem("Spliter", "。；;，！？,、?!　 ~()（）:：\n");
		SpliterDirect = Config.segment().getItem("SpliterDirect", "。；;，,！!？?、:：\n");
		SpliterText = Config.segment().getItem("SpliterText", "。！!？?");
		BroadCastIP = Config.rock().getItem("BroadCastIP", "election leader");
		ScheduleIP = Config.rock().getItem("ScheduleIP", "election leader");
		ESHome = Config.rock().getItem("ES_HOME", "/server/es/");
		ticketProvideId =Config.rock().getIntItem("ticketProvideId", "3");
	}

	public static int feeBy(BizType type) {
		int fee = 0;
		if (type == BizType.car) {
			fee = 0;
		} else if (type == BizType.trip) {
			fee = 0;
		} else if (type == BizType.room) {
			fee = 0;
		} else if (type == BizType.product) {
			fee = 0;
		}
		return fee;
	}

	public static int commissionBy(BizType type) {
		int rate = 0;
		if (type == BizType.car) {
			rate = 20;
		} else if (type == BizType.trip) {
			rate = 20;
		} else if (type == BizType.room) {
			rate = 20;
		} else if (type == BizType.product) {
			rate = 20;
		}
		return rate;
	}

}
