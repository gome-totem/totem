package org.z.global.environment;

import org.z.global.dict.Global;
import org.z.global.dict.Global.BizType;
import org.z.global.dict.Global.HashMode;
import org.z.global.util.DateUtil;



public class Const {
	public static String ERROR_EXIST = "-10004";
	public static final String SPLIT = "〓";
	public static final char[] PRINTABLE_ALPHABET = { '!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<',
			'?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c',
			'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~', };
	public static final char[] AlphaChars = { '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
			'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

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

	public static String defaultMysqlServer = null;
	public static String defaultMysqlDB = null;
	public static String defaultMongoServer = null;
	public static String defaultMongoDB = null;
	public static String defaultDictServer = null;
	public static String defaultDictDB = null;
	public static String defaultDataServer = null;
	public static String defaultDataDB = null;
	public static String defaultLogServer = null;
	public static String defaultLogDB = null;
	public static String ApiRouterServer = null;
	public static String ApiAppServer = null;
	public static String BroadCastIP = null;
	public static String ScheduleIP = null;
	public static String ConfigPath = null;
	public static String RootPath = null;
	public static String SiteName = null;
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
	public static String ESHome = null;
	public static int threadNum = 3;
	public static String Spliter = null;
	public static String SpliterDirect = null;
	public static String SpliterText = null;
	public static String SecurityClassName = null;
	public static String fullCategoryScheDule = null;
	public static String fullProductScheDule = null;
	public static String currentEnv = null;
	public static String coo8HeaderUrl = null;
	public static String coo8FooterUrl = null;
	public static String gomeHeaderUrl = null;
	public static String gomeFooterUrl = null;
	public static String coo8HeaderPath = null;
	public static String coo8FooterPath = null;
	public static String coo8HeaderScheDule = null;
	public static String coo8FooterSchedule = null;
	public static String gomeHeaderScheDule = null;
	public static String gomeFooterSchedule = null;
	public static String removeBigQSchedule = null;
	public static String suggestUpdateSchedule = null;
	public static String suggestFileSchedule = null;
	public static String relevanceFileSchedule = null;
	public static String relevanceSearchSchedule = null;
	public static String gomeSecondSearchSchedule = null;
	public static String gomeSeondFileSchedule = null;
	public static String buildRuleDictSchedule = null;
	// errorCorrect
	public static String buildErrorCorrectSchedule = null;
	public static String buildSortRuleSchedule = null;
	public static String dragonSchedule = null;
	public static String maslocSchedule = null;
	public static String specialSkuSchedule = null;
	public static String IndexweightSchedule = null;
	public static String clearCategoryDictSchedule = null;
	public static int appCount = 0;
	public static String indexDateSchedule = null;
	public static String deDragonSchedule = null;
	public static String TaoshRootCategory = null;
	public static String TaoshshowCate = null;
	public static int sortDateDays = 0;
	public static int cityMaxCount = 3800;
	public static String SearchLog = null;
	public static String UsrActionLog = null;
	public static String IndexLog = null;
	public static String dragonName = null;
	public static String dragonMasloc = null;
	public static String dragonTube = null;
	public static String dragonTubeName = null;
	public static String StockName = null;
	public static String AllStockName = null;
	public static String ScheduleDB = null;
	public static String synKnwoledgeDataSchedule = null;
	public static String synKnwoledgeDataScheduleIp = null;
	public static String title618 = null;

	public static String smarchBuyBeansIp = null; // 6596<

	static {
		defaultMysqlServer = Config.db().getItem("DefaultMysqlServer", "dict");
		defaultMysqlDB = Config.db().getItem("DefaultMysqlDB", "default");
		defaultLogServer = Config.db().getItem("DefaultLogServer", "x2");
		defaultLogDB = Config.db().getItem("DefaultLogDB", "logs");
		defaultMongoServer = Config.db().getItem("DefaultMongoServer", "dict");
		defaultMongoDB = Config.db().getItem("DefaultMongoDB", "yiqihi");
		defaultDictServer = Config.db().getItem("DefaultDictServer", Const.defaultMysqlServer);
		defaultDictDB = Config.db().getItem("DefaultDictDB", "dict");
		defaultDataServer = Config.db().getItem("defaultDataServer", Const.defaultMysqlServer);
		defaultDataDB = Config.db().getItem("defaultDataDB", "data");
		ConfigPath = Config.rock().getItem("ConfigPath", Global.workDir + "conf/");
		RootPath = Config.rock().getItem("RootPath", Global.workDir);
		SiteName = Config.rock().getItem("siteName", "/");
		TaoshRootCategory = Config.rock().getItem("TaoshRootCategory", "cat29425543");
		TaoshshowCate = Config.rock().getItem("TaoshshowCate", "cat29425544");
		ZooIP = Config.rock().getItem("ZooIP", Global.ServerIP);
		LevelDB_BatchUpdate = Config.rock().getIntItem("LevelDB_BatchUpdate", "90");
		LevelDB_BatchDelete = Config.rock().getIntItem("LevelDB_BatchDelete", "30");
		SolrHome = Config.rock().getItem("SolrHome", "/server/solr/");
		ESHome = Config.rock().getItem("ES_HOME", "/server/es/");
		SecurityClassName = Config.rock().getItem("SecurityClassName", "");
		DictIP = Config.rock().getItem("DictIP", "");
		AppConnMode = Global.HashMode.valueOf(Config.rock().getItem("AppConnectMode", "AppServer").trim().toLowerCase());
		AppConnectPoolEnable = Config.rock().getBooleanItem("AppConnectPoolEnable", true);
		AppConnectPoolMax = Config.rock().getIntItem("AppConnectPoolMax", "10240");
		AppConnectTimeout = Config.rock().getIntItem("AppConnectTimeout", "6");
		cityMaxCount = Config.rock().getIntItem("cityMaxCount", "3800");
		Spliter = Config.segment().getItem("Spliter", "。；;，！？,、?!　 ~()（）:：\n");
		SpliterDirect = Config.segment().getItem("SpliterDirect", "。；;，,！!？?、:：\n");
		SpliterText = Config.segment().getItem("SpliterText", "。！!？?");
		ApiRouterServer = Config.rock().getItem("api.router", "election leader");
		ApiAppServer = Config.rock().getItem("api.appserver", "election leader");
		BroadCastIP = Config.rock().getItem("BroadCastIP", "election leader");
		ScheduleIP = Config.rock().getItem("scheduleIP", "election leader");
		currentEnv = Config.rock().getItem("currentEnv", "sit");
		appCount = Config.rock().getIntItem("appCount", "12");
		SearchLog = Config.rock().getItem("SearchLog", "searchlogs");
		UsrActionLog = Config.rock().getItem("UseActionLog", "usrsactionlogs");
		IndexLog = Config.rock().getItem("IndexLog", "indexlogs");
		dragonName = Config.rock().getItem("dragonName", "dragonName");
		dragonMasloc = Config.rock().getItem("dragonMasloc", "dragonMasloc");
		StockName = Config.rock().getItem("StockName", "stock");
		dragonTube = Config.rock().getItem("dragonTube", "ATP");
		dragonTubeName = Config.rock().getItem("dragonTubeName", "ATPStack");
		AllStockName = Config.rock().getItem("AllStockName", "allStock");
		title618 = Config.rock().getItem("title618", "亮剑 618");
		sortDateDays = Config.rock().getIntItem("sortDateDays", "7");
		fullCategoryScheDule = Config.timer().getItem("categoryScheDule", "00 01 03 * * ?");
		fullProductScheDule = Config.timer().getItem("productScheDule", "00 01 04 * * ?");
		deDragonSchedule = Config.timer().getItem("deDragonSchedule", "00 0/3 * * * ?");
		coo8HeaderUrl = Config.timer().getItem("coo8HeaderUrl", "http://www.coo8.com/coo8/ec/browse/productlist/productListHeader.jsp");
		coo8FooterUrl = Config.timer().getItem("coo8FooterUrl", "http://www.coo8.com/coo8/ec/browse/productlist/productListFooter.jsp");
		coo8HeaderPath = Config.timer().getItem("coo8HeaderPath", "/app/search/search/html/header.html");
		coo8FooterPath = Config.timer().getItem("coo8FooterPath", "/app/search/search/html/footer.html");
		coo8HeaderScheDule = Config.timer().getItem("coo8HeaderScheDule", "10 0/30 * * * ?");
		coo8FooterSchedule = Config.timer().getItem("coo8FooterSchedule", "20 0/30 * * * ?");
		gomeHeaderUrl = Config.timer().getItem("gomeHeaderUrl", "http://10.58.22.40:7003/ec/homeus/n/common/header.html");
		gomeFooterUrl = Config.timer().getItem("gomeFooterUrl", "http://10.58.22.40:7003/ec/homeus/n/common/footer.html");
		gomeHeaderScheDule = Config.timer().getItem("gomeHeaderScheDule", "30 0/30* * * * ?");
		gomeFooterSchedule = Config.timer().getItem("gomeFooterSchedule", "40 0/30 * * * ?");
		removeBigQSchedule = Config.timer().getItem("removeBigQSchedule", "0 0 * * * ?");
		buildRuleDictSchedule = Config.timer().getItem("buildRuleDictSchedule", "0 0 0 * * ?");
		// errorCorrect
		buildErrorCorrectSchedule = Config.timer().getItem("buildErrorCorrectSchedule", "0 30 23 * * ?");

		buildSortRuleSchedule = Config.timer().getItem("buildSortRuleSchedule", "0 0 11 * * ?");

		suggestUpdateSchedule = Config.timer().getItem("suggestUpdateSchedule", "00 01 12 * * ?");
		suggestFileSchedule = Config.timer().getItem("suggestFileSchedule", "00 01 08 * * ?");
		relevanceSearchSchedule = Config.timer().getItem("relevanceSearchSchedule", "00 01 13 * * ?");
		relevanceFileSchedule = Config.timer().getItem("relevanceFileSchedule", "00 01 09 * * ?");
		gomeSecondSearchSchedule = Config.timer().getItem("relevanceSearchSchedule", "00 01 14 * * ?");
		gomeSeondFileSchedule = Config.timer().getItem("relevanceFileSchedule", "00 01 10 * * ?");
		IndexweightSchedule = Config.timer().getItem("IndexweightSchedule", "00 0/30 * * * ?");
		clearCategoryDictSchedule = Config.timer().getItem("clearCategoryDictSchedule", "0 0/30 * * * ?");
		
		dragonSchedule = Config.timer().getItem("dragonSchedule", "00 0/2 * * * ?");
		maslocSchedule = Config.timer().getItem("maslocSchedule", "00 0/30 * * * ?");
		specialSkuSchedule = Config.timer().getItem("dragonSchedule", "00 01 02 * * ?");
		indexDateSchedule = Config.timer().getItem("indexDateSchedule", "00 15 20 * * ?");
		ScheduleDB = Config.rock().getItem("scheduleDB", "/server/schedule/");
		synKnwoledgeDataSchedule = Config.timer().getItem("synKnwoledgeDataSchedule", "0 0 */7 * * ?");
		synKnwoledgeDataScheduleIp = Config.timer().getItem("synKnwoledgeDataScheduleIp", "10.58.50.54");

		smarchBuyBeansIp = Config.rock().getItem("smartBuyBeansIp", "10.57.0.93");

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
