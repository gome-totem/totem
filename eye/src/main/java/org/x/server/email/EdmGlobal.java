package org.x.server.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class EdmGlobal {
	
	protected static Logger logger = LoggerFactory.getLogger(EdmGlobal.class);
	public static String edmurl = null;
	public static String edmusername = null;
	public static String edmpassword = null;
	public static String mhost = null;
	public static String mdb =  null;
	public static String musername = null;
	public static String mpassword = null;
	public static String chost = null;
	public static int port = 19753;
	public static String cdb =  null;
	public static String cusername = null;
	public static String cpassword = null;
	public static String coname = null;
	public static String wusername = null;
	public static String wpassword = null;
	public static int campaignId = 0;
	public static int mailingId = 0;
	public static String lcollname = null;
	public static String lhost = null;
	public static String ldb = null;
	public static String lusername = null;
	public static String lpassword = null;
	public static String sourceurl = null;
	public static long LDmessageId = 0;
	public static long LDsendPlanId = 0;
	
	
	static{
		  LDsendPlanId = EDMConfig.edm().getLongItem("LDsendPlanId", "-1");
		  LDmessageId = EDMConfig.edm().getLongItem("LDmessageId", "-1");
		  edmurl = EDMConfig.edm().getItem("edmurl", "");
		  edmusername = EDMConfig.edm().getItem("edmusername", "");
		  edmpassword = EDMConfig.edm().getItem("edmpassword", "");
		  mhost = EDMConfig.edm().getItem("mhost", "");
		  port = EDMConfig.edm().getIntItem("port", "");
		  mdb =  EDMConfig.edm().getItem("mdb", "");
		  musername = EDMConfig.edm().getItem("musername", "");
		  mpassword = EDMConfig.edm().getItem("mpassword", "");
		  chost = EDMConfig.edm().getItem("cloudhost", "");
		  cdb =  EDMConfig.edm().getItem("clouddb", "");
		  cusername = EDMConfig.edm().getItem("cloudusername", "");
		  cpassword =EDMConfig.edm().getItem("cloudpassword", "");
		  coname = EDMConfig.edm().getItem("cloudcollection", "");
		  wusername =EDMConfig.edm().getItem("wusername", "");
		  wpassword = EDMConfig.edm().getItem("wpassword", "");
		  campaignId =EDMConfig.edm().getIntItem("campaignId", "");
		  mailingId = EDMConfig.edm().getIntItem("mailingId", "");
		  lcollname = EDMConfig.edm().getItem("lcollname", "");
		  lhost = EDMConfig.edm().getItem("lhost", "");
		  ldb = EDMConfig.edm().getItem("ldb", "");
		  lusername = EDMConfig.edm().getItem("lusername", "");
	     lpassword =  EDMConfig.edm().getItem("lpassword", "");
	     sourceurl = EDMConfig.edm().getItem("sourceurl", "");
		
	}

	

}
