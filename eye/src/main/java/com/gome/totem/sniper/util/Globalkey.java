package com.gome.totem.sniper.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class Globalkey {

	public static final String CONFIG_STORE_LOG = "config-store-log";
	public static final String CONFIG_STORE_LOG_BAK = "config-store-log-bak";
	public static final String ROOT_GROUP = "root" ;
	public static final String PREFIX_ROUTER_CLASS = "router_server_" ;
	public static final String PREFIX_APP_CLASS = "app_server_" ;
	public static final String DICT_SERVER = "dict_server" ;
	
	public static Map<String, Timer> appTimer = new HashMap<String, Timer>() ;
	public static Map<String, Timer> routerTimer = new HashMap<String, Timer>() ;
	public static Map<String, String> liveAppInfo = new HashMap<String, String>() ;
	public static Map<String, String> liveRouterInfo = new HashMap<String, String>() ;
	public static long zooInfoCountReal = 0l ;
	public static long zooInfoCountCheck = 0l ;
	
	public static final Integer[] highLevelMethod = { MessageType.REGISTER,
			MessageType.CONNECT, MessageType.DISCONNECT, MessageType.UPDATE,
			MessageType.REMOVE, MessageType.COMMIT, MessageType.ONLINE, MessageType.OFFLINE };

	
	public static String EYEHOST = null;
	public static String WSHOST = null;
	public static String RouterIps = null;
	public static String AppIps = null;
	public static boolean DevelopMode = true;
	public static boolean MassageSend = false;
	public static boolean MassageTmp = true;
	public static String XM = null;
	public static String PH = null;
	public static String  XM_ETX= null;
	public static String  PH_ETX= null;
	public static String  XM_TMP= null;
	public static String  PH_TMP= null;
	public static String SshLogSrc = null;
	public static String RTUname = null;
	public static String RTPassd = null;
	public static String TotemServers = null;
	public static String TotemEnv = null;
	public static String ShiroConnUrl = null;
	public static String ShiroUname = null;
	public static String ShiroUpwd = null;
	public static String jdCats = null;
	
	public static String  esServers=null;
	public static String esDevelopers=null;
	public static boolean esSend=false;
	
	public static String driverClass=null;
	public static String hiveConnUrl=null;
	public static String hiveUname=null;
	public static String hiveUpwd=null;
	
	public static String detectAddress=null;
	
	public static String mailReceiver=null;
	
	public static String smartIp=null;
	public static String	smartPort=null;
	public static String	smartName=null;
	public static String	smartPass=null;
	public static String	smartDb=null;
	public static String	smartCollection=null;
	
	public static String	allServices=null;
	
	
	static {
		EYEHOST = EyeConf.eyeConf().getItem("eyehost", "");
		WSHOST = EyeConf.eyeConf().getItem("sockethost", "");
		RouterIps = EyeConf.eyeConf().getItem("RouterIps", "");
		AppIps = EyeConf.eyeConf().getItem("AppIps", "");
		DevelopMode = EyeConf.eyeConf().getBooleanItem("DevelopMode", true);
		XM = EyeConf.eyeConf().getItem("xm", "");
		PH = EyeConf.eyeConf().getItem("ph", "");
		XM_ETX = EyeConf.eyeConf().getItem("xm_etx", "");
		PH_ETX = EyeConf.eyeConf().getItem("ph_etx", "");
		XM_TMP = EyeConf.eyeConf().getItem("xm_tmp", "");
		PH_TMP = EyeConf.eyeConf().getItem("ph_tmp", "");
		MassageSend = EyeConf.eyeConf().getBooleanItem("MassageSend", false);
		SshLogSrc = EyeConf.eyeConf().getItem("SshLogSrc", "");
		RTUname = EyeConf.eyeConf().getItem("RTUname", "");
		RTPassd = EyeConf.eyeConf().getItem("RTPassd", "");
		TotemServers = EyeConf.eyeConf().getItem("totemServers", "");
		TotemEnv = EyeConf.eyeConf().getItem("totemEnv", "");
		ShiroConnUrl = EyeConf.eyeConf().getItem("shiroConnUrl", "");
		ShiroUname = EyeConf.eyeConf().getItem("shiroUname", "");
		ShiroUpwd = EyeConf.eyeConf().getItem("shiroUpwd", "");
		jdCats = EyeConf.eyeConf().getItem("jdCats", "");
		esServers=EyeConf.eyeConf().getItem("esServers", "");
		esDevelopers=EyeConf.eyeConf().getItem("esDevelopers", "");
		esSend=EyeConf.eyeConf().getBooleanItem("esSend", false);
		hiveConnUrl=EyeConf.eyeConf().getItem("hiveConnUrl", "");
		hiveUname=EyeConf.eyeConf().getItem("hiveUname", "");
		hiveUpwd=EyeConf.eyeConf().getItem("hiveUpwd", "");
		driverClass=EyeConf.eyeConf().getItem("HiveDriverClass", "");
		detectAddress=EyeConf.eyeConf().getItem("detectAddress", "");
		mailReceiver=EyeConf.eyeConf().getItem("mailReceiver", "");
		smartIp=EyeConf.eyeConf().getItem("smartIp", "");
		smartPort=EyeConf.eyeConf().getItem("smartPort", "");
		smartName=EyeConf.eyeConf().getItem("smartName", "");
		smartPass=EyeConf.eyeConf().getItem("smartPass", "");
		smartDb=EyeConf.eyeConf().getItem("smartDb", "");
		smartCollection=EyeConf.eyeConf().getItem("smartCollection", "");
		allServices=EyeConf.eyeConf().getItem("allServices", "");
	}
	
	public enum ServletStatus {
		success,login,logout,none,fail,empty
	}
}