package org.x.cloud.dict;

public class GlobalPage {
	// 配置文件路径
	public static final String URL_MAKER_PATH = "url.properties";
	public static final String SERVER_PATH = "server.properties";

	// 展现方式
	public static final String LIST = "1";
	public static final String PICT = "2";
	public static final String SHOP = "3";
	public static final String TILE = "0";
	public static final String PICTURESERVER;

	public static final String JSSERVER;
	public static final String JSSWWWERVER;
	public static final String CSSERVER;
	public static final String CSSWWWERVER;
	public static final String MALLSITE;
	public static final String GOMEDYNSITE;
	public static final String SATSITE;
	public static final String DYNSITE;
	public static final String BOOKSITE;
	public static final String CONTEXTPATH;
	public static final String SEOHEADDIFCAT;

	public static final String PFLODER;
	public static final String PSEPERATOR;
	public static final String PSUFFIX;
	public static final String GFLODER;
	public static final String GSEPERATOR;
	public static final String CFLODER;
	public static final String CSUFFIX;
	public static final String BFLODER;
	public static final String RANGRCONNECTOR;
	public static final String C8CATFLODER;
	public static final String C8SEARCHFLODER;
	public static final String DOMAIN;

	public static final String RESET;
	public static final String VOLUMEDESC;
	public static final String VOLUMEASC;
	public static final String PRICEDESC;
	public static final String PRICEASC;
	public static final String DATEDESC;
	public static final String POPULARDESC;
	public static final String EVALUATECOUNTDESC;
	public static final String STATICDOMAIN;
	 
	public static final String GOMEART;
	public static final String MULTIGOLD;
	public static final String FILTER_REQ_FACETS;
	
	public static final String ATG_MONGO_IP;
	public static final String ATG_MONGO_UNAME;
	public static final String ATG_MONGO_UPASS;

	static {

		PICTURESERVER = ConfigFile.server().getItem("pictureserver", ".gomein.net.cn/image/");
		JSSERVER = ConfigFile.server().getItem("jsserver", "js.gomein.net.cn/");
		JSSWWWERVER = ConfigFile.server().getItem("jsWwwServer", "js.gome.com.cn/");
		CSSERVER = ConfigFile.server().getItem("cssserver", "css.gomein.net.cn/");
		CSSWWWERVER = ConfigFile.server().getItem("cssWwwServer", "css.gome.com.cn/");
		MALLSITE = ConfigFile.server().getItem("mallSite", "http://mall.gome.com.cn/");
		GOMEDYNSITE = ConfigFile.server().getItem("gomeDynsite", "http://www.gome.com.cn/");
		SATSITE = ConfigFile.server().getItem("satSite", "http://www.gome.com.cn/");
		DYNSITE = ConfigFile.server().getItem("dynSite", "http://g.gome.com.cn/");
		BOOKSITE = ConfigFile.server().getItem("bookSite", "http://book.gome.com.cn/");
		DOMAIN = ConfigFile.server().getItem("domain", ".gome.com.cn");
		STATICDOMAIN = ConfigFile.server().getItem("staticDomain", "gomein.net.cn");
		CONTEXTPATH = ConfigFile.server().getItem("contextPath", "");
		SEOHEADDIFCAT = ConfigFile.server().getItem("seoHeadDifCats", "");
		
		ATG_MONGO_IP = ConfigFile.server().getItem("ATGMongo_ip", "");
		ATG_MONGO_UNAME = ConfigFile.server().getItem("ATGMongo_uname", "");
		ATG_MONGO_UPASS = ConfigFile.server().getItem("ATGMongo_upass", "");


		GOMEART = ConfigFile.server().getItem("gomeart", "");
		MULTIGOLD = ConfigFile.server().getItem("multigold", "");
		
		PFLODER = ConfigFile.url().getItem("pFloder", "");
		PSEPERATOR = ConfigFile.url().getItem("pSeperator", "");
		PSUFFIX = ConfigFile.url().getItem("pSuffix", "");
		GFLODER = ConfigFile.url().getItem("gFloder", "");
		GSEPERATOR = ConfigFile.url().getItem("gSeperator", "");
		CFLODER = ConfigFile.url().getItem("cFloder", "");
		CSUFFIX = ConfigFile.url().getItem("cSuffix", "");
		BFLODER = ConfigFile.url().getItem("bFloder", "");
		RANGRCONNECTOR = ConfigFile.url().getItem("rangeConnector", "");
		C8CATFLODER = ConfigFile.url().getItem("c8CatFloder", "");
		C8SEARCHFLODER = ConfigFile.url().getItem("c8SearchFloder", "");

		RESET = ConfigFile.url().getItem("00", "");
		VOLUMEDESC = ConfigFile.url().getItem("10", "");
		VOLUMEASC = ConfigFile.url().getItem("11", "");
		PRICEDESC = ConfigFile.url().getItem("20", "");
		PRICEASC = ConfigFile.url().getItem("21", "");
		DATEDESC = ConfigFile.url().getItem("30", "");
		POPULARDESC = ConfigFile.url().getItem("40", "");
		EVALUATECOUNTDESC = ConfigFile.url().getItem("50", "");
		
		FILTER_REQ_FACETS = "frf";
	}
}
